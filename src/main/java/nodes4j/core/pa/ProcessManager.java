package nodes4j.core.pa;

import static nodes4j.core.ActorMessageTag.DATA;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import actor4j.core.utils.ActorGroup;
import actor4j.core.utils.ActorGroupSet;
import nodes4j.core.NodeActor;

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
	
	public void start(Process<?, ?> process) {
		data.clear();
		result.clear();
		aliases.clear();
		
		system = new ActorSystem("nodes4j");
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

		system.send(new ActorMessage<>(null, DATA, root, root));
		system.start(null, onTermination);
	}
	
	public void start(List<Process<?, ?>> processes) {
		data.clear();
		result.clear();
		aliases.clear();
		
		system = new ActorSystem("nodes4j");
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
		
		system.broadcast(new ActorMessage<>(null, DATA, system.SYSTEM_ID, null), group);
		system.start(null, onTermination);
	}
	
	public void start(Process<?, ?>... processes) {
		start(Arrays.asList(processes));
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
