package nodes4j.core;

import java.util.List;
import java.util.UUID;

public class Node<T, R> {
	public UUID id;
	public List<T> data;
	public NodeOperations<T, R> operations;
	public int nTasks;
	public int min_range;
	public List<Node<?, ?>> sucs; // List<Node<T, ?>>
	public boolean isRoot;
	
	public Node() {
		super();
		
		operations = new NodeOperations<>();
	}
}
