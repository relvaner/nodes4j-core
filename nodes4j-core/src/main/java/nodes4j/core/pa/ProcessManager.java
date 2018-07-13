package nodes4j.core.pa;

import static nodes4j.core.ActorMessageTag.DATA;

import java.util.List;
import java.util.UUID;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.immutable.ImmutableList;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import nodes4j.core.NodeActor;

public class ProcessManager {
	protected ActorSystem system;
	protected Runnable onTermination;
	
	protected Process<?, ?> mainProcess;
	
	public ProcessManager onTermination(Runnable onTermination) {
		this.onTermination = onTermination;
		
		return this;
	}
	
	public void start(Process<?, ?> process) {
		mainProcess = process;
		
		system = new ActorSystem("nodes4j");
		mainProcess.node.nTasks = Runtime.getRuntime().availableProcessors()/*stand-alone*/;
		mainProcess.node.isRoot = true;
		
		UUID root = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new NodeActor<>("root", mainProcess.node, mainProcess.result);
			}
		});

		system.send(new ActorMessage<>(null, DATA, root, root));
		system.start(null, onTermination);
	}
	
	public void stop() {
		if (system!=null)
			system.shutdown();
	}
	
	public List<?> getResult(UUID id) {
		return (List<?>)mainProcess.result.get(id);
	}
	
	public List<?> getFirstResult() {
		if (mainProcess.result.values().iterator().hasNext())
			return ((ImmutableList<?>)mainProcess.result.values().iterator().next()).get();
		else
			return null;
	}
}
