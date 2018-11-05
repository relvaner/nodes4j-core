package nodes4j.core;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Node<T, R> {
	public UUID id;
	public String alias;
	public List<T> data;
	public NodeOperations<T, R> operations;
	public int nTasks;
	public int min_range;
	public Set<Node<?, ?>> sucs; // Set<Node<R, ?>>
	public Set<Node<?, ?>> pres; // Set<Node<?, T>>
	public boolean isRoot;
	
	public Node(String alias) {
		super();
		
		this.alias = alias;
		operations = new NodeOperations<>();
	}
	
	public Node() {
		this(null);
	}
}
