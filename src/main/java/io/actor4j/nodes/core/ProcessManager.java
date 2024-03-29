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
package io.actor4j.nodes.core;

import static io.actor4j.nodes.core.internal.ActorMessageTag.DATA;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.ActorSystemFactory;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorFactory;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupSet;
import io.actor4j.nodes.core.internal.NodeActor;

public class ProcessManager {
	protected ActorSystem system;
	protected Runnable onTermination;
	
	protected Map<UUID, List<?>> data;
	protected Map<UUID, List<?>> result;
	protected Map<String, UUID> aliases;
	
	protected boolean debugDataEnabled;
	
	public ProcessManager() {
		this(false);
	}
	
	public ProcessManager(boolean debugDataEnabled) {
		super();
		
		data = new ConcurrentHashMap<>();
		result = new ConcurrentHashMap<>();
		aliases = new ConcurrentHashMap<>();
		
		this.debugDataEnabled = debugDataEnabled;
	}
	
	public ProcessManager onTermination(Runnable onTermination) {
		this.onTermination = onTermination;
		
		return this;
	}
	
	public void start(ActorSystemFactory factory, Process<?, ?> process) {
		data.clear();
		result.clear();
		aliases.clear();
		
		ActorSystemConfig config = ActorSystemConfig.builder()
			.name("nodes4j")
			.build();
		system = ActorSystem.create(factory, config);
		process.node.nTasks = Runtime.getRuntime().availableProcessors()/*stand-alone*/;
		process.node.isRoot = true;
		process.data = data;
		process.result = result;
		process.aliases = aliases;
		
		UUID root = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new NodeActor<>("node-"+process.node.id.toString(), process.node, result, aliases, debugDataEnabled, data);
			}
		});

		system.send(ActorMessage.create(null, DATA, root, root));
		system.start(null, onTermination);
	}
	
	public void start(ActorSystemFactory factory, List<Process<?, ?>> processes) {
		data.clear();
		result.clear();
		aliases.clear();
		
		ActorSystemConfig config = ActorSystemConfig.builder()
			.name("nodes4j")
			.build();
		system = ActorSystem.create(factory, config);
		int nTasks = Runtime.getRuntime().availableProcessors()/*stand-alone*/;
		ActorGroup group = new ActorGroupSet();
		for (Process<?, ?> process : processes) {
			process.node.nTasks = nTasks;
			process.node.isRoot = true;
			process.data = data;
			process.result = result;
			process.aliases = aliases;
			
			group.add(system.addActor(new ActorFactory() {
				@Override
				public Actor create() {
					return new NodeActor<>("node-"+process.node.id.toString(), process.node, result, aliases, debugDataEnabled, data);
				}
			}));
		}
		
		system.broadcast(ActorMessage.create(null, DATA, system.SYSTEM_ID(), null), group);
		system.start(null, onTermination);
	}
	
	public void start(ActorSystemFactory factory, Process<?, ?>... processes) {
		start(factory, Arrays.asList(processes));
	}
	
	/*
	public void stop() {
		if (system!=null)
			system.shutdownWithActors(true);
	}
	*/
	
	public List<?> getData(UUID id) { 
		return data.get(id);
	}
	
	public List<?> getData(String alias) {
		List<?> result = null;
		
		UUID id = aliases.get(alias);
		if (id!=null)
			result = getData(id);
		
		return result;
	}
	
	public List<?> getResult(UUID id) {
		return result.get(id);
	}
	
	public List<?> getFirstResult() {
		if (result.values().iterator().hasNext())
			return result.values().iterator().next();
		else
			return null;
	}
	
	public List<?> getResult(String alias) {
		List<?> result = null;
		
		UUID id = aliases.get(alias);
		if (id!=null)
			result = getResult(id);
		
		return result;
	}
}
