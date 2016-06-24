package nodes4j.core.pa;

import java.util.List;

import nodes4j.core.exceptions.DataException;
import nodes4j.core.pa.utils.SortProcess;
import nodes4j.core.pa.utils.SortType;
import nodes4j.function.BinaryOperator;
import nodes4j.function.Consumer;
import nodes4j.function.Function;
import nodes4j.function.Predicate;

public class ProcessAction<T, R> {
	protected Process<T, R> process;
	
	public ProcessAction(Process<T, R> process) {
		this.process = process;
	}
	
	public ProcessAction<T, R> data(List<T> data, int min_range) {
		checkData(data);
		
		process.node.data = data;
		process.node.min_range = min_range;
		
		return this;
	}
	
	public ProcessAction<T, R> data(List<T> data) {
		return data(data, -1);
	}
	
	public ProcessAction<T, R> filter(Predicate<T> predicate) {
		process.node.operations.filter = predicate;
		return this;
	}
	
	public ProcessAction<T, R> map(Function<T, R> mapper) {
		process.node.operations.mapper = mapper;
		return this;
	}
	
	public ProcessAction<T, R> forEach(Consumer<T> action) {
		process.node.operations.action = action;
		return this;
	}
	
	public ProcessAction<T, R> mapAsList(Function<List<T>, List<R>> mapper) {
		process.node.operations.mapAsList = mapper;
		return this;
	}
	
	public ProcessAction<T, R> reduce(BinaryOperator<List<R>> accumulator) {
		process.node.operations.accumulator = accumulator;
		return this;
	}	
	
	public ProcessAction<?, ?> sortedASC() {
		process.sequence(new SortProcess<>(SortType.SORT_ASCENDING));
		return this;
	}
	
	public ProcessAction<?, ?> sortedDESC() {
		process.sequence(new SortProcess<>(SortType.SORT_DESCENDING));
		return this;
	}
	
	protected void checkData(List<T> data) {
		if (data==null)
			throw new DataException();
	}
}
