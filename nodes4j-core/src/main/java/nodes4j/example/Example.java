package nodes4j.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nodes4j.function.Consumer;
import nodes4j.function.Function;
import nodes4j.function.Predicate;
import nodes4j.core.pa.Process;

public class Example {

	public static void main(String[] args) {
		List<Integer> list = new ArrayList<>();
		list.addAll(Arrays.asList(3, 2, 1, 1, 0, 2, 45, 78, 99, 34, 31, 8, 1, 123, 14, 9257, -10, -15));
				
		final Process<Integer, Double> mainProcess = new Process<>();
		
		/*
		mainProcess
			.data(list)
			.filter(new Predicate<Integer>() {
				@Override
				public boolean test(Integer value) {
					return (value>0);
				}
			})
			.map(new Function<Integer, Double>() {
				@Override
				public Double apply(Integer value) {
					return value+100d;
				}
			})
			.forEach(new Consumer<Integer>() {
				@Override
				public void accept(Integer value) {
					System.out.println(value);
				}})
			.sortedDESC()
			.onTermination(new Runnable() {
				@Override
				public void run() {
					System.out.println(mainProcess.getFirstResult().toString());
				}
			})
			.start();
		*/
		/* Java 8 */
		mainProcess
			.data(list)
			.filter(v -> v>0)
			.map(v -> v+100d)
			.forEach(System.out::println)
			.sortedASC()
			.onTermination(() -> System.out.println(mainProcess.getFirstResult().toString()))
			.start();
	}
}
