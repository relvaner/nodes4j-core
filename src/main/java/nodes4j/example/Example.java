package nodes4j.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nodes4j.core.pa.Process;
import nodes4j.core.pa.ProcessManager;

public class Example {

	public static void main(String[] args) {
		List<Integer> list = new ArrayList<>();
		list.addAll(Arrays.asList(3, 2, 1, 1, 0, 2, 45, 78, 99, 34, 31, 8, 1, 123, 14, 9257, -10, -15));
				
		Process<Integer, Double> exampleProcess = new Process<>();
		exampleProcess
			.data(list)
			.filter(v -> v>0)
			.map(v -> v+100d)
			.forEach(System.out::println)
			.sortedDESC();
			
		ProcessManager manager = new ProcessManager();
		manager
			.onTermination(() -> System.out.println(manager.getFirstResult().toString()))
			.start(exampleProcess);
	}
}
