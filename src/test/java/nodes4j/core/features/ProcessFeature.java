package nodes4j.core.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import nodes4j.core.pa.Process;
import nodes4j.core.pa.ProcessManager;
import nodes4j.core.pa.utils.SortProcess;
import nodes4j.core.pa.utils.SortType;

import static org.junit.Assert.*;
import static actor4j.core.utils.ActorLogger.logger;

public class ProcessFeature {
	protected CountDownLatch testDone = new CountDownLatch(1);
	
	protected final Integer[] precondition_numbers = { 3, 2, 1, 1, 0, 2, 45, 78, 99, 34, 31, 8, 1, 123, 14, 9257, -10, -15 };
	protected List<Integer> preConditionList;
	
	@Before
	public void before() {
		testDone = new CountDownLatch(1);
		
		preConditionList = new ArrayList<>();
		preConditionList.addAll(Arrays.asList(precondition_numbers));
	}
	
	@Test(timeout=5000)
	public void test_desc() {
		final Double[] postcondition_numbers = { 9357.0, 223.0, 199.0, 178.0, 145.0, 134.0, 131.0, 114.0, 108.0, 103.0, 102.0, 102.0, 101.0, 101.0, 101.0 };
		List<Double> postConditionList = new ArrayList<>();
		postConditionList.addAll(Arrays.asList(postcondition_numbers));
		
		Process<Integer, Double> process = new Process<>();
		process
			.data(preConditionList)
			.filter(v -> v>0)
			.map(v -> v+100d)
			.forEach(System.out::println)
			.sortedDESC();
			
		ProcessManager manager = new ProcessManager();
		manager
			.onTermination(() -> { 
				assertEquals(postConditionList, manager.getFirstResult()); 
				logger().debug(manager.getFirstResult()); 
				testDone.countDown();})
			.start(process);
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test(timeout=5000)
	public void test_asc() {
		final Double[] postcondition_numbers = { 101.0, 101.0, 101.0, 102.0, 102.0, 103.0, 108.0, 114.0, 131.0, 134.0, 145.0, 178.0, 199.0, 223.0, 9357.0 };
		List<Double> postConditionList = new ArrayList<>();
		postConditionList.addAll(Arrays.asList(postcondition_numbers));
		
		Process<Integer, Double> process = new Process<>();
		process
			.data(preConditionList)
			.filter(v -> v>0)
			.map(v -> v+100d)
			.forEach(System.out::println)
			.sortedASC();
			
		ProcessManager manager = new ProcessManager();
		manager
			.onTermination(() -> { 
				assertEquals(postConditionList, manager.getFirstResult()); 
				logger().debug(manager.getFirstResult()); 
				testDone.countDown();})
			.start(process);
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test(timeout=5000)
	public void test_sequence_asc() {
		final Integer[] postcondition_numbers = { -15, -10, 0, 1, 1, 1, 2, 2, 3, 8, 14, 31, 34, 45, 78, 99, 123, 9257 };
		List<Integer> postConditionList = new ArrayList<>();
		postConditionList.addAll(Arrays.asList(postcondition_numbers));
		
		Process<Integer, Integer> process = new Process<>();
		process
			.data(preConditionList, 5);
		
		process.sequence(new SortProcess<>(SortType.SORT_ASCENDING));
			
		ProcessManager manager = new ProcessManager();
		manager
			.onTermination(() -> { 
				assertEquals(postConditionList, manager.getFirstResult()); 
				logger().debug(manager.getFirstResult()); 
				testDone.countDown();})
			.start(process);
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test(timeout=5000)
	public void test_sequence_asc_alias() {
		final Integer[] postcondition_numbers = { -15, -10, 0, 1, 1, 1, 2, 2, 3, 8, 14, 31, 34, 45, 78, 99, 123, 9257 };
		List<Integer> postConditionList = new ArrayList<>();
		postConditionList.addAll(Arrays.asList(postcondition_numbers));
		
		Process<Integer, Integer> process = new Process<>("process_main");
		process
			.data(preConditionList, 5);
		
		process.sequence(new SortProcess<>("process_sort_asc", SortType.SORT_ASCENDING));
			
		ProcessManager manager = new ProcessManager(true);
		manager
			.onTermination(() -> { 
				logger().debug("Data (process_main): "+manager.getData("process_main"));
				logger().debug("Data (process_sort_asc): "+manager.getData("process_sort_asc"));
				assertEquals(postConditionList, manager.getResult("process_sort_asc")); 
				logger().debug("Result (process_sort_asc): "+manager.getResult("process_sort_asc")); 
				testDone.countDown();})
			.start(process);
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

/*
try {
	BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"));
    writer.write(manager.getFirstResult().toString());
    writer.close();
} catch (IOException e) {
	e.printStackTrace();
}
*/
