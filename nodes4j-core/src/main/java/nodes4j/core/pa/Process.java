package nodes4j.core.pa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import actor4j.core.Actor;
import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import nodes4j.core.Node;
import nodes4j.core.NodeActor;
import nodes4j.core.exceptions.DataException;
import nodes4j.function.BinaryOperator;
import nodes4j.function.Consumer;
import nodes4j.function.Function;
import nodes4j.function.Predicate;
import nodes4j.core.pa.utils.SortProcess;
import nodes4j.core.pa.utils.SortType;

import static nodes4j.core.ActorMessageTag.DATA;

public class Process<T, R> {
	protected ActorSystem system;
	protected UUID root;
	protected Runnable onTermination;
	
	protected Node<T, R> node;
	protected Map<UUID, Object> result;
	
	public Process() {
		super();
		
		node = new Node<>();
		node.id = UUID.randomUUID();
		node.sucs = new ArrayList<>();
		result = new ConcurrentHashMap<>();
	}
	
	public Process(Function<List<T>, List<R>> mapper, BinaryOperator<List<R>> accumulator) {
		this();
		
		node.operations.mapAsList = mapper;
		node.operations.accumulator = accumulator;
	}
	
	public UUID getId() {
		return node.id;
	}
	
	public Process<T, R> data(List<T> data, int min_range) {
		checkData(data);
		
		node.data = data;
		node.min_range = min_range;
		
		return this;
	}
	
	public Process<T, R> data(List<T> data) {
		return data(data, -1);
	}
	
	public Process<T, R> filter(Predicate<T> predicate) {
		node.operations.filter = predicate;
		return this;
	}
	
	public Process<T, R> map(Function<T, R> mapper) {
		node.operations.mapper = mapper;
		return this;
	}
	
	public Process<T, R> forEach(Consumer<T> action) {
		node.operations.action = action;
		return this;
	}
	
	public Process<T, R> mapAsList(Function<List<T>, List<R>> mapper) {
		node.operations.mapAsList = mapper;
		return this;
	}
	
	public Process<T, R> reduce(BinaryOperator<List<R>> accumulator) {
		node.operations.accumulator = accumulator;
		return this;
	}
	
	public Process<T, R> sortedASC() {
		sequence(new SortProcess<>(SortType.SORT_ASCENDING));
		return this;
	}
	
	public Process<T, R> sortedDESC() {
		sequence(new SortProcess<>(SortType.SORT_DESCENDING));
		return this;
	}
		
	public Process<T, R> onTermination(Runnable onTermination) {
		this.onTermination = onTermination;
		
		return this;
	}
		
	public <S> Process<T, R> sequence(Process<T, S> process) {
		node.sucs.add(process.node);
		process.result = result;
		
		return this;
	}
	
	public Process<T, R> sequence(List<Process<?, ?>> processes) {
		if (processes!=null) {
			Process<?, ?> parent = this;
			for (Process<?, ?> p : processes) {
				parent.node.sucs.add(p.node);
				parent = p;
				p.result = result;
			}
		}
			
		return this;
	}
	
	public Process<T, R> sequence(Process<?, ?>... processes) {
		return sequence(Arrays.asList(processes));
	}
	
	public Process<T, R> merge(List<Process<T, ?>> processes) {
		if (processes!=null)
			for (Process<T, ?> p : processes)
				sequence(p);
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Process<T, R> merge(Process<T, ?>... processes) {
		return merge(Arrays.asList(processes));
	}
	
	public void start() {
		system = new ActorSystem("nodes4j");
		node.nTasks = Runtime.getRuntime().availableProcessors()/*stand-alone*/;
		node.isRoot = true;
		/*
		root = system.addActor(NodeActor.class, "root", node, result);
		*/
		root = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new NodeActor<T, R>("root", node, result);
			}
		});

		system.send(new ActorMessage<>(null, DATA, root, root));
		system.start(onTermination);
	}
	
	public void stop() {
		if (system!=null)
			system.shutdown();
	}
	
	public List<R> getResult() {
		return getResult(node.id);
	}
	
	@SuppressWarnings("unchecked")
	public List<R> getResult(UUID id) {
		return (List<R>)result.get(id);
	}
	
	@SuppressWarnings("unchecked")
	public List<R> getFirstResult() {
		if (result.values().iterator().hasNext())
			return (List<R>)result.values().iterator().next();
		else
			return null;
	}
	
	protected void checkData(List<T> data) {
		if (data==null)
			throw new DataException();
	}
}
