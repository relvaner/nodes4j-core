package nodes4j.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import actor4j.core.utils.ActorGroup;
import actor4j.core.utils.ActorGroupAsList;
import nodes4j.core.exceptions.DataException;

import static actor4j.core.utils.CommPattern.*;
import static nodes4j.core.ActorMessageTag.*;

public class NodeActor<T, R> extends Actor {
	protected Node<T, R> node;
	protected ActorGroup hubGroup;
	protected ActorMessageTag dest_tag;
	
	protected Map<UUID, Object> result;
	
	protected List<UUID> waitForChildren;
	
	public NodeActor(String name, Node<T, R> node, Map<UUID, Object> result) {
		super(name);
		
		this.node = node;
		this.result = result;
		
		waitForChildren = new ArrayList<>(node.sucs.size());
		
		hubGroup = new ActorGroup();
	}
	
	@Override
	public void preStart() {
		if (node.sucs!=null)
			for (Node<?, ?> suc : node.sucs) {
				suc.nTasks = node.nTasks; // ATTENTION
				/*
				UUID ref = addChild(NodeActor.class, "node"+UUID.randomUUID().toString(), suc, result);
				*/
				final Node<?, ?> f_suc = suc;
				UUID ref = addChild(new ActorFactory() {
					@Override
					public Actor create() {
						return new NodeActor<>("node-"+UUID.randomUUID().toString(), f_suc, result);
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
			
			if (message.value!=null)
				node.data = (List<T>)message.valueAsList();
			ActorGroupAsList group = new ActorGroupAsList();
			checkData(node.data);
			node.nTasks = adjustSize(node.nTasks, node.data.size(), node.min_range);
			for (int i=0; i<node.nTasks; i++) {
				UUID task = addChild(TaskActor.class, "task-"+UUID.randomUUID().toString(), node.operations, group, hubGroup, dest_tag);
				group.add(task);
			}
			scatter(node.data, TASK.ordinal(), this, new ActorGroup(group));
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
