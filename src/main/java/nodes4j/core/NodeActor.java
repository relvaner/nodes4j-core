package nodes4j.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.immutable.ImmutableList;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import actor4j.core.utils.ActorGroup;
import actor4j.core.utils.ActorGroupList;
import actor4j.core.utils.ActorGroupSet;
import nodes4j.core.exceptions.DataException;

import static actor4j.core.utils.CommPattern.*;
import static nodes4j.core.ActorMessageTag.*;

public class NodeActor<T, R> extends Actor {
	protected Node<T, R> node;
	protected ActorGroup hubGroup;
	protected ActorMessageTag dest_tag;
	
	protected boolean debugDataEnabled;
	// ThreadSafe
	protected Map<UUID, List<?>> debugData;
	// ThreadSafe
	protected Map<UUID, Object> result;
	// ThreadSafe
	protected Map<String, UUID> aliases;
	
	protected List<UUID> waitForChildren;
	
	public NodeActor(String name, Node<T, R> node, Map<UUID, Object> result, Map<String, UUID> aliases, boolean debugDataEnabled, Map<UUID, List<?>> debugData) {
		super(name);
		
		this.node = node;
		
		this.debugDataEnabled = debugDataEnabled;
		this.debugData = debugData;
		this.result = result;
		this.aliases = aliases;
		
		waitForChildren = new ArrayList<>(node.sucs.size());
		
		hubGroup = new ActorGroupSet();
	}
	
	public NodeActor(String name, Node<T, R> node, Map<UUID, Object> result, Map<String, UUID> aliases) {
		this(name, node, result, aliases, false, null);
	}
	
	@Override
	public void preStart() {
		/*
		if (node.alias!=null)
			setAlias(node.alias);
		*/
		if (aliases!=null && node.alias!=null)
			aliases.put(node.alias, node.id);
		if (debugDataEnabled && debugData!=null && node.data!=null)
			debugData.put(node.id, node.data);
		
		if (node.sucs!=null)
			for (Node<?, ?> suc : node.sucs) {
				suc.nTasks = node.nTasks; // ATTENTION
				
				UUID ref = addChild(new ActorFactory() {
					@Override
					public Actor create() {
						return new NodeActor<>("node-"+UUID.randomUUID().toString(), suc, result, aliases, debugDataEnabled, debugData);
					}
				});
				hubGroup.add(ref);
				waitForChildren.add(ref);
			}
	}

	protected int adjustSize(int size, int arr_size, int min_range) {
		if (min_range>0) {
			int max_size  = arr_size/min_range;
			if (max_size==0)
				max_size = 1;
			size = (size>max_size ? max_size : size);
		}
		
		if (arr_size<size)
			size = arr_size;
		
		return size;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.tag==DATA.ordinal()) {
			if (node.sucs==null || node.sucs.size()==0) {
				hubGroup.add(self());
				dest_tag = RESULT;
			}
			else
				dest_tag = DATA;
			
			if (message.value!=null && message.value instanceof ImmutableList) {
				node.data = ((ImmutableList<T>)message.value).get();
				if (debugDataEnabled && debugData!=null)
					debugData.put(node.id, node.data);
			}
			ActorGroupList group = new ActorGroupList();
			checkData(node.data);
			node.nTasks = adjustSize(node.nTasks, node.data.size(), node.min_range);
			for (int i=0; i<node.nTasks; i++) {
				UUID task = addChild(() -> new TaskActor<>("task-"+UUID.randomUUID().toString(), node.operations, group, hubGroup, dest_tag));
				group.add(task);
			}
			scatter(node.data, TASK.ordinal(), this, new ActorGroupSet(group));
		}
		else if (message.tag==RESULT.ordinal()) {
			if (result!=null)
				result.put(node.id, message.value);
			
			if (node.isRoot)
				getSystem().shutdown();
			else
				send(new ActorMessage<>(null, SHUTDOWN, self(), getParent()));
		}
		else if (message.tag==SHUTDOWN.ordinal()) {
			waitForChildren.remove(message.source);
			
			if ( waitForChildren.size()==0) {
				if (node.isRoot)
					getSystem().shutdown();
				else
					send(new ActorMessage<>(null, SHUTDOWN, self(), getParent()));
			}
		}
	}
	
	protected void checkData(List<T> data) {
		if (data==null)
			throw new DataException();
	}
}
