/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.actor4j.nodes.core.internal;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.immutable.ImmutableList;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorFactory;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupList;
import io.actor4j.core.utils.ActorGroupSet;
import io.actor4j.nodes.core.exceptions.DataException;

import static io.actor4j.core.utils.CommPattern.*;
import static io.actor4j.nodes.core.internal.ActorMessageTag.*;

public class NodeActor<T, R> extends Actor {
	protected Node<T, R> node;
	protected ActorGroup hubGroup;
	protected int dest_tag;
	
	protected boolean debugDataEnabled;
	// ThreadSafe
	protected Map<UUID, List<?>> debugData;
	// ThreadSafe
	protected Map<UUID, List<?>> result;
	// ThreadSafe
	protected Map<String, UUID> aliases;
	
	protected Set<UUID> waitForChildren;
	protected int waitForParents;
	
	protected static final Object lock = new Object();
	
	public NodeActor(String name, Node<T, R> node, Map<UUID, List<?>> result, Map<String, UUID> aliases, boolean debugDataEnabled, Map<UUID, List<?>> debugData) {
		super(name);
		
		this.node = node;
		
		this.debugDataEnabled = debugDataEnabled;
		this.debugData = debugData;
		this.result = result;
		this.aliases = aliases;
		
		waitForChildren = new HashSet<>(node.sucs.size());
		waitForParents = node.pres!=null ? node.pres.size() : 0;
		
		hubGroup = new ActorGroupSet();
	}
	
	public NodeActor(String name, Node<T, R> node, Map<UUID, List<?>> result, Map<String, UUID> aliases) {
		this(name, node, result, aliases, false, null);
	}
	
	@Override
	public void preStart() {
		setAlias("node-"+node.id.toString());
		
		if (aliases!=null && node.alias!=null)
			aliases.put(node.alias, node.id);
		if (debugDataEnabled && debugData!=null && node.data!=null)
			debugData.put(node.id, node.data);
		
		if (node.data==null)
			node.data = new LinkedList<>();

		if (node.sucs!=null)
			for (Node<?, ?> suc : node.sucs) {
				UUID ref = null;
				if (suc.pres.size()>1) {
					// uses Double-Check-Idiom a la Bloch
					ref = getSystem().getActorFromAlias("node-"+suc.id.toString());
					if (ref==null) {
						synchronized(lock) {
							ref = getSystem().getActorFromAlias("node-"+suc.id.toString());
							if (ref==null) {
								suc.nTasks = node.nTasks; // ATTENTION
								ref = addChild(new ActorFactory() {
									@Override
									public Actor create() {
										return new NodeActor<>("node-"+suc.id.toString(), suc, result, aliases, debugDataEnabled, debugData);
									}
								});
							}
						}
					}
				}
				else {
					suc.nTasks = node.nTasks; // ATTENTION
					ref = addChild(new ActorFactory() {
						@Override
						public Actor create() {
							return new NodeActor<>("node-"+suc.id.toString(), suc, result, aliases, debugDataEnabled, debugData);
						}
					});
				}
				
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
		if (message.tag()==DATA) {
			waitForParents--;
			
			if (message.value()!=null && message.value() instanceof ImmutableList)
				node.data.addAll(((ImmutableList<T>)message.value()).get());
			
			if (waitForParents<=0) {
				if (node.sucs==null || node.sucs.size()==0) {
					hubGroup.add(self());
					dest_tag = RESULT;
				}
				else
					dest_tag = DATA;
				
				if (debugDataEnabled && debugData!=null)
					debugData.put(node.id, node.data);
				
				ActorGroupList group = new ActorGroupList();
				checkData(node.data);
				node.nTasks = adjustSize(node.nTasks, node.data.size(), node.min_range);
				for (int i=0; i<node.nTasks; i++) {
					UUID task = addChild(() -> new TaskActor<>("task-"+UUID.randomUUID().toString(), node.operations, group, hubGroup, dest_tag));
					group.add(task);
				}
				scatter(node.data, TASK, this, new ActorGroupSet(group));
			}
		}
		else if (message.tag()==RESULT) {
			if (result!=null)
				result.put(node.id, ((ImmutableList<R>)message.value()).get());
			
			if (node.isRoot)
				getSystem().shutdown();
			else {
				if (!node.pres.isEmpty()) // has more parents
					for (Node<?, ?> pre : node.pres) 
						sendViaAlias(ActorMessage.create(null, SHUTDOWN, self(), null), "node-"+pre.id.toString());
				else
					send(ActorMessage.create(null, SHUTDOWN, self(), getParent()));
			}
		}
		else if (message.tag()==SHUTDOWN) {
			waitForChildren.remove(message.source());
			
			if (waitForChildren.isEmpty()) {
				if (node.isRoot)
					getSystem().shutdown(); // TODO: unsafe
				else
					send(ActorMessage.create(null, SHUTDOWN, self(), getParent()));
			}
		}
	}
	
	protected void checkData(List<T> data) {
		if (data==null)
			throw new DataException();
	}
}
