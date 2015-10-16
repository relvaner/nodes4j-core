package nodes4j.core;

import java.util.List;

import nodes4j.function.BinaryOperator;
import nodes4j.function.Consumer;
import nodes4j.function.Function;
import nodes4j.function.Predicate;

public class NodeOperations<T, R> {
	/* lazy  */
	public Predicate<T> filter;
	public Function<T, R> mapper;
	public Consumer<T> action;
	/* eager */
	public Function<List<T>, List<R>> mapAsList;
	public BinaryOperator<List<R>> accumulator;
}
