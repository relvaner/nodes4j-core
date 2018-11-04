package nodes4j.core.pa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import nodes4j.core.Node;

public class Process<T, R> {
	protected Node<T, R> node;
	
	protected Map<UUID, Object> result; // initial set over ProcessManager
	protected Map<String, UUID> aliases; // initial set over ProcessManager
	
	protected ProcessAction<T, R> processAction;
	
	public Process() {
		this(null);
	}
	
	public Process(String alias) {
		super();
		
		node = new Node<>(alias);
		node.id = UUID.randomUUID();
		node.sucs = new ArrayList<>();
		
		processAction = new ProcessAction<>(this);
	}
	
	public Process(Function<List<T>, List<R>> mapper, BinaryOperator<List<R>> accumulator) {
		this();
		
		node.operations.mapAsList = mapper;
		node.operations.accumulator = accumulator;
	}
	
	public UUID getId() {
		return node.id;
	}
	
	public ProcessAction<T, R> data(List<T> data, int min_range) {
		return processAction.data(data, min_range);
	}
	
	public ProcessAction<T, R> data(List<T> data) {
		return processAction.data(data);
	}
	
	public ProcessAction<T, R> filter(Predicate<T> predicate) {
		return processAction.filter(predicate);
	}
	
	public ProcessAction<T, R> map(Function<T, R> mapper) {
		return processAction.map(mapper);
	}
	
	public ProcessAction<T, R> forEach(Consumer<T> action) {
		return processAction.forEach(action);
	}
	
	public ProcessAction<T, R> mapAsList(Function<List<T>, List<R>> mapper) {
		return processAction.mapAsList(mapper);
	}
	
	public ProcessAction<T, R> reduce(BinaryOperator<List<R>> accumulator) {
		return processAction.reduce(accumulator);
	}	
	
	public ProcessAction<?, ?> sortedASC() {
		return processAction.sortedASC();
	}
	
	public ProcessAction<?, ?> sortedDESC() {
		return processAction.sortedDESC();
	}
			
	public <S> Process<T, S> sequence(Process<T, S> process) {
		node.sucs.add(process.node);
		process.result = result;
		
		return process;
	}
	
	public Process<?, ?> sequence(List<Process<?, ?>> processes) {
		Process<?, ?> parent = this;
		if (processes!=null) {
			for (Process<?, ?> p : processes) {
				parent.node.sucs.add(p.node);
				parent = p;
				p.result = result;
			}
		}
			
		return parent;
	}
	
	public Process<?, ?> sequence(Process<?, ?>... processes) {
		return sequence(Arrays.asList(processes));
	}
	
	public Process<T, R> parallel(List<Process<T, ?>> processes) {
		if (processes!=null)
			for (Process<T, ?> p : processes)
				sequence(p);
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Process<T, R> parallel(Process<T, ?>... processes) {
		return parallel(Arrays.asList(processes));
	}
	
	public List<?> getResult() {
		return (List<?>)result.get(node.id);
	}
}
