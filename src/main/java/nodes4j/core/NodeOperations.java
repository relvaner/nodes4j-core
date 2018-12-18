package nodes4j.core;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class NodeOperations<T, R> {
	/* lazy  */
	public Predicate<T> filterOp;
	public Function<T, R> mapOp;
	public Consumer<T> forEachOp;
	/* eager */
	public Function<List<T>, List<R>> flatMapOp;
	public BinaryOperator<List<R>> reduceOp;
}
