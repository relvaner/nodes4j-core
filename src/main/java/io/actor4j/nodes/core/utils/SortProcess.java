package io.actor4j.nodes.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import io.actor4j.nodes.core.Process;

public class SortProcess<T extends Comparable<? super T>> extends Process<T, T> {
	public SortProcess(final SortType type) {
		this(null, type);
	}
	
	public SortProcess(String name, final SortType type) {
		super(name);

		flatMap(new Function<List<T>, List<T>>() {
			@Override
			public List<T> apply(List<T> list) {
				List<T> result = new ArrayList<>(list);
				
				if (type == SortType.SORT_ASCENDING)
					Collections.sort(result);
				else
					Collections.sort(result, Collections.reverseOrder());
				
				return result;
			}});
						
		reduce(new BinaryOperator<List<T>>() {
			@Override
			public List<T> apply(List<T> left, List<T> right) {
				List<T> result = new ArrayList<>(left.size()+right.size());
						
				if (left.size()>0 && right.size()>0)
					if (type == SortType.SORT_ASCENDING) {
						if (left.get(left.size()-1).compareTo(right.get(0)) < 0) {
							result.addAll(left);
							result.addAll(right);
									
							return result;
						}
					}
					else if (left.get(left.size()-1).compareTo(right.get(0)) > 0) {
						result.addAll(left);
						result.addAll(right);
									
						return result;
					}
										
				int leftPos = 0, rightPos = 0;
				for (int i=0; i<left.size()+right.size(); i++) {
					if (type == SortType.SORT_ASCENDING) {
						if ( (leftPos<left.size()) && (rightPos==right.size() || left.get(leftPos).compareTo(right.get(rightPos))<0) ) {
							result.add(left.get(leftPos));
							leftPos++;
						}
						else {
							result.add(right.get(rightPos));
							rightPos++;
						}
					}
					else {
						if ( (leftPos<left.size()) && (rightPos==right.size() || left.get(leftPos).compareTo(right.get(rightPos))>0) ) {
							result.add(left.get(leftPos));
							leftPos++;
						}
						else {
							result.add(right.get(rightPos));
							rightPos++;
						}
					}	
				}
							
				return result;
			}});
	}
}
