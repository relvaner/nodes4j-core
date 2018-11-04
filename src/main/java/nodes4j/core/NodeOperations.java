package nodes4j.core;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class NodeOperations<T, R> {
	/* lazy  */
	public Predicate<T> filter;
	public Function<T, R> mapper;
	public Consumer<T> action;
	/* eager */
	public Function<List<T>, List<R>> mapAsList;
	public BinaryOperator<List<R>> accumulator;
}
