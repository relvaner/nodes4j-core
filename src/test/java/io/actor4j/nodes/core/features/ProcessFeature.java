/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.actor4j.nodes.core.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorRuntime;
import io.actor4j.core.ActorSystemFactory;
import io.actor4j.nodes.core.Process;
import io.actor4j.nodes.core.ProcessManager;
import io.actor4j.nodes.core.utils.SortProcess;
import io.actor4j.nodes.core.utils.SortType;

import static org.junit.Assert.*;
import static io.actor4j.core.logging.ActorLogger.*;

public class ProcessFeature {
	protected CountDownLatch testDone = new CountDownLatch(1);
	
	protected final Integer[] precondition_numbers = { 3, 2, 1, 1, 0, 2, 45, 78, 99, 34, 31, 8, 1, 123, 14, 9257, -10, -15 };
	protected List<Integer> preConditionList;
	
	protected ActorSystemFactory actorRuntime;
	
	@Before
	public void before() {
		testDone = new CountDownLatch(1);
		
		preConditionList = new ArrayList<>();
		preConditionList.addAll(Arrays.asList(precondition_numbers));
		
		actorRuntime = ActorRuntime.factory();
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
			//.forEach(System.out::println)
			.sortedDESC();
			
		ProcessManager manager = new ProcessManager();
		manager
			.onTermination(() -> { 
				assertEquals(postConditionList, manager.getFirstResult()); 
				logger().log(DEBUG, manager.getFirstResult().toString()); 
				testDone.countDown();})
			.start(actorRuntime, process);
		
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
			//.forEach(System.out::println)
			.sortedASC();
			
		ProcessManager manager = new ProcessManager();
		manager
			.onTermination(() -> { 
				assertEquals(postConditionList, manager.getFirstResult()); 
				logger().log(DEBUG, manager.getFirstResult().toString()); 
				testDone.countDown();})
			.start(actorRuntime, process);
		
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
				logger().log(DEBUG, manager.getFirstResult().toString()); 
				testDone.countDown();})
			.start(actorRuntime, process);
		
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
				logger().log(DEBUG, "Data (process_main): "+manager.getData("process_main"));
				logger().log(DEBUG, "Data (process_sort_asc): "+manager.getData("process_sort_asc"));
				assertEquals(postConditionList, manager.getResult("process_sort_asc")); 
				logger().log(DEBUG, "Result (process_sort_asc): "+manager.getResult("process_sort_asc")); 
				testDone.countDown();})
			.start(actorRuntime, process);
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test(timeout=5000)
	public void test_sequence_parallel() {
		final Integer[] postcondition_numbers1 = { -15, -10, 0, 1, 1, 1, 2, 2, 3, 8, 14, 31, 34, 45, 78, 99, 123, 9257 };
		final Integer[] postcondition_numbers2 = { 9257, 123, 99, 78, 45, 34, 31, 14, 8, 3, 2, 2, 1, 1, 1, 0, -10, -15 };
		final Integer[] postcondition_numbers3 = { 9257, 123, 99, 78, 45, 34, 31, 14, 8 };
		List<Integer> postConditionList1 = new ArrayList<>();
		postConditionList1.addAll(Arrays.asList(postcondition_numbers1));
		List<Integer> postConditionList2 = new ArrayList<>();
		postConditionList2.addAll(Arrays.asList(postcondition_numbers2));
		List<Integer> postConditionList3 = new ArrayList<>();
		postConditionList3.addAll(Arrays.asList(postcondition_numbers3));
		
		Process<Integer, Integer> process = new Process<>("process_main");
		process
			.data(preConditionList, 5);
		
		Process<Integer, Integer> process_sort1 = new SortProcess<Integer>("process_sort_asc1", SortType.SORT_ASCENDING);
		Process<Integer, Integer> process_sort2 = new SortProcess<Integer>("process_sort_asc2", SortType.SORT_DESCENDING);
		
		process.parallel(process_sort1, process_sort2);
		
		Process<Integer, Integer> process_filter = new Process<>("process_filter");
		process_filter.filter((v) -> v>5);
		process_sort2.sequence(process_filter);
			
		ProcessManager manager = new ProcessManager(true);
		manager
			.onTermination(() -> { 
				//logger().debug("Data (process_main): "+manager.getData("process_main"));
				//logger().debug("Data (process_sort_asc): "+manager.getData("process_sort_asc"));
				assertEquals(postConditionList1, manager.getResult("process_sort_asc1")); 
				assertEquals(postConditionList2, manager.getData("process_filter")); 
				assertTrue(postConditionList3.containsAll(manager.getResult("process_filter")));
				logger().log(DEBUG, "Result (process_sort_asc1): "+manager.getResult("process_sort_asc1")); 
				logger().log(DEBUG, "Result (process_sort_asc2): "+manager.getData("process_filter")); 
				logger().log(DEBUG, "Result (process_filter): "+manager.getResult("process_filter")); 
				testDone.countDown();})
			.start(actorRuntime, process);
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test(timeout=5000)
	public void test_merge() {
		final Integer[] postcondition_numbers1 = { 14, 31, 34, 45, 78, 99, 123, 9257 };
		final Integer[] postcondition_numbers2 = { -15, -10, 0, 1, 1, 1, 2, 2, 3, 8 };
		final Integer[] postcondition_numbers3 = { 2, 2, 2, 3, 3, 4, 9, 15, 32, 35, 46, 80, 101 };
		List<Integer> postConditionList1 = new ArrayList<>();
		postConditionList1.addAll(Arrays.asList(postcondition_numbers1));
		List<Integer> postConditionList2 = new ArrayList<>();
		postConditionList2.addAll(Arrays.asList(postcondition_numbers2));
		List<Integer> postConditionList3 = new ArrayList<>();
		postConditionList3.addAll(Arrays.asList(postcondition_numbers3));
		
		Process<Integer, Integer> process_main = new Process<>("process_main");
		process_main
			.data(preConditionList);
		
		Process<Integer, Integer> process_a = new Process<>("process_a");
		process_a
			.filter((v) -> v>50 && v<100)
			.map((v) -> v+2);
		Process<Integer, Integer> process_b = new Process<>("process_b");
		process_b
			.filter((v) -> v>0 && v<=50)
			.map((v) -> v+1);
		Process<Integer, Integer> process_sort_asc = new SortProcess<Integer>("process_sort_asc", SortType.SORT_ASCENDING);
		
		process_main.parallel(process_a, process_b);
		process_sort_asc.merge(process_a, process_b);
		
		ProcessManager manager = new ProcessManager(true);
		manager
			.onTermination(() -> { 
				logger().log(DEBUG, "Data (process_a): "+manager.getData("process_a")); 
				logger().log(DEBUG, "Data (process_a): "+process_a.getData()); 
				logger().log(DEBUG, "Data (process_b): "+manager.getData("process_b")); 
				logger().log(DEBUG, "Data (process_sort_asc): "+manager.getData("process_sort_asc")); 
				logger().log(DEBUG, "Result (process_sort_asc): "+manager.getResult("process_sort_asc")); 
				assertTrue(preConditionList.containsAll(manager.getData("process_a")));
				assertTrue(preConditionList.containsAll(manager.getData("process_b")));
				//logger().debug("Result (process_a): "+manager.getResult("process_a")); 
				//logger().debug("Result (process_b): "+manager.getResult("process_b")); 
				//assertTrue(postConditionList1.containsAll(manager.getResult("process_a")));
				//assertTrue(postConditionList2.containsAll(manager.getResult("process_b")));
				assertEquals(postConditionList3, manager.getResult("process_sort_asc")); 
				//assertEquals(postConditionList3, process_sort_asc.getResult()); TODO: BUG
				testDone.countDown();})
			.start(actorRuntime, process_main);
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test(timeout=5000)
	public void test_multiple_root() {
		final Integer[] precondition_numbers1 = { 3, 2, 1, 1, 0, 2, 45, 78, 99, 34 };
		final Integer[] precondition_numbers2 = { 31, 8, 1, 123, 14, 9257, -10, -15 };
		final Integer[] postcondition_numbers = { -15, -10, 0, 1, 1, 1, 2, 2, 3, 8, 14, 31, 34, 45, 78, 99, 123, 9257 };
		List<Integer> preConditionList1 = new ArrayList<>();
		preConditionList1.addAll(Arrays.asList(precondition_numbers1));
		List<Integer> preConditionList2 = new ArrayList<>();
		preConditionList2.addAll(Arrays.asList(precondition_numbers2));
		List<Integer> postConditionList = new ArrayList<>();
		postConditionList.addAll(Arrays.asList(postcondition_numbers));
		
		Process<Integer, Integer> process_a = new Process<>("process_a");
		process_a
			.data(preConditionList1);
		Process<Integer, Integer> process_b = new Process<>("process_b");
		process_b
			.data(preConditionList2);
		
		Process<Integer, Integer> process_sort_asc = new SortProcess<Integer>("process_sort_asc", SortType.SORT_ASCENDING);
		process_sort_asc.merge(process_a, process_b);
		
		ProcessManager manager = new ProcessManager(true);
		manager
			.onTermination(() -> { 
				logger().log(DEBUG, "Data (process_a): "+manager.getData("process_a")); 
				logger().log(DEBUG, "Data (process_a): "+process_a.getData()); 
				logger().log(DEBUG, "Data (process_b): "+manager.getData("process_b")); 
				logger().log(DEBUG, "Data (process_sort_asc): "+manager.getData("process_sort_asc")); 
				logger().log(DEBUG, "Result (process_sort_asc): "+manager.getResult("process_sort_asc")); 
				assertTrue(preConditionList1.containsAll(manager.getData("process_a")));
				assertTrue(preConditionList2.containsAll(manager.getData("process_b")));
				assertEquals(postConditionList, manager.getResult("process_sort_asc")); 
				testDone.countDown();})
			.start(actorRuntime, process_a, process_b);
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test(timeout=5000)
	public void test_stream_desc() {
		final Double[] postcondition_numbers = { 9357.0, 223.0, 199.0, 178.0, 145.0, 134.0, 131.0, 114.0, 108.0, 103.0, 102.0, 102.0, 101.0, 101.0, 101.0 };
		List<Double> postConditionList = new ArrayList<>();
		postConditionList.addAll(Arrays.asList(postcondition_numbers));
		
		Process<Integer, Double> process = new Process<>();
		process
			.data(preConditionList)
			.stream(s -> s.filter(v -> v>0).map(v -> v+100d))
			.sortedDESC();
			
		ProcessManager manager = new ProcessManager();
		manager
			.onTermination(() -> { 
				assertEquals(postConditionList, manager.getFirstResult()); 
				logger().log(DEBUG, manager.getFirstResult().toString()); 
				testDone.countDown();})
			.start(actorRuntime, process);
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test(timeout=5000)
	public void test_streamRx_desc() {
		final Double[] postcondition_numbers = { 9357.0, 223.0, 199.0, 178.0, 145.0, 134.0, 131.0, 114.0, 108.0, 103.0, 102.0, 102.0, 101.0, 101.0, 101.0 };
		List<Double> postConditionList = new ArrayList<>();
		postConditionList.addAll(Arrays.asList(postcondition_numbers));
		
		Process<Integer, Double> process = new Process<>();
		process
			.data(preConditionList)
			.streamRx(o -> o.filter(v -> v>0).map(v -> v+100d))
			.sortedDESC();
			
		ProcessManager manager = new ProcessManager();
		manager
			.onTermination(() -> { 
				assertEquals(postConditionList, manager.getFirstResult()); 
				logger().log(DEBUG, manager.getFirstResult().toString()); 
				testDone.countDown();})
			.start(actorRuntime, process);
		
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
