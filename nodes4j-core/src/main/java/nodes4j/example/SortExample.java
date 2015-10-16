package nodes4j.example;

/*
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
*/
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.Random;

import nodes4j.core.pa.utils.SortProcess;
import nodes4j.core.pa.utils.SortType;

public class SortExample {

	public static void main(String[] args) {
		List<Integer> list = new ArrayList<>();
		list.addAll(Arrays.asList(3, 2, 1, 1, 0, 2, 45, 78, 99, 34, 31, 8, 1, 123, 14, 9257, -10, -15));
		/*
		Random r = new Random();
		for (int i=0; i<1000000; i++)
			list.add(r.nextInt(100000));
		*/
		
		
		final SortProcess<Integer> sortProcess = new SortProcess<>(SortType.SORT_ASCENDING);
		sortProcess
			.data(list, 5)
			.onTermination(new Runnable() {
				@Override
				public void run() {
					System.out.println(sortProcess.getResult().toString());
					/*
					try {
						BufferedWriter writer = new BufferedWriter( new FileWriter("result.txt"));
					    writer.write(sortProcess.getResult().toString());
					    writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					*/
				}
			})
			.start();
	}
}
