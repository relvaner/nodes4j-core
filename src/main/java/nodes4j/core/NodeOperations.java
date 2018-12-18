package nodes4j.core;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.reactivex.Observable;

public class NodeOperations<T, R> {
	/* lazy  */
	public Predicate<T> filterOp;
	public Function<T, R> mapOp;
	public Consumer<T> forEachOp;
	/* eager */
	public Function<List<T>, List<R>> flatMapOp;
	public BinaryOperator<List<R>> reduceOp;
	
	public Function<Stream<T>, List<R>> streamOp; /* Java Streams */
	public Function<Observable<T>, Observable<R>> streamRxOp; /* RxJava */
}
