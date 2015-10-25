package nodes4j.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.mutable.MutableObject;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;
import nodes4j.function.BinaryOperator;

import static actor4j.core.utils.CommPattern.*;
import static nodes4j.core.ActorMessageTag.*;

public class TaskActor<T, R> extends Actor {
	protected NodeOperations<T, R> operations;
	protected BinaryOperator<List<R>> defaultAccumulator;
	protected ActorGroup group;
	protected ActorGroup hubGroup;
	protected ActorMessageTag dest_tag;
	
	protected MutableObject result;
	protected int level;
	
	public TaskActor(String name, NodeOperations<T, R> operations, ActorGroup group, ActorGroup hubGroup, ActorMessageTag dest_tag) {
		super(name);
		
		this.operations = operations;
		defaultAccumulator = new BinaryOperator<List<R>>() {
			@Override
			public List<R> apply(List<R> left, List<R> right) {
				List<R> result = new ArrayList<>(left.size()+right.size());
				result.addAll(left);
				result.addAll(right);		
				return result;
			}
		};
		this.group = group;
		this.hubGroup = hubGroup;
		this.dest_tag = dest_tag;
		
		result = new MutableObject();
	}

	@SuppressWarnings("unchecked")
	protected void treeReduction(ActorMessage<?> message) {
		int grank = group.indexOf(self());
		if (grank%(1<<(level+1))>0) { 
			int dest = grank-(1<<level);
			//System.out.printf("[level: %d] rank %d has sended a message (%s) to rank %d%n", level, group.indexOf(getSelf()), result.getValue().toString(), dest);
			send(new ActorMessage<>(result.getValue(), REDUCE, self(), group.get(dest)));
			stop();
		}
		else if (message.tag==REDUCE.ordinal()){
			List<R> buf = (List<R>)message.valueAsList();
			//System.out.printf("[level: %d] rank %d has received a message (%s) from rank %d%n", level, group.indexOf(getSelf()), buf.toString(), group.indexOf(getSender()));
			if (operations.accumulator!=null)
				result.setValue(operations.accumulator.apply((List<R>)result.getValue(), buf));
			else
				result.setValue(    defaultAccumulator.apply((List<R>)result.getValue(), buf));
			
			level++;
			message.tag = TASK.ordinal();
			treeReduction(message);
		}
		else {
			int source = grank+(1<<level);
			if (source>group.size()-1)
				if (grank==0) {
					broadcast(new ActorMessage<>(result.getValue(), dest_tag, self(), null), this, hubGroup);
					stop();
					return;
				} else {
					level++;
					treeReduction(message);
				}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.tag==TASK.ordinal()) {
			if (operations.mapAsList!=null)
				result.setValue(operations.mapAsList.apply((List<T>)message.valueAsList()));
			else {
				List<R> list = new ArrayList<>(message.valueAsList().size());
				for (T t : (List<T>)message.valueAsList()) {
					if (operations.filter!=null)
						if (!operations.filter.test(t))
							continue;
					if (operations.mapper!=null)
						list.add(operations.mapper.apply(t));
					else
						list.add((R)t);
					if (operations.action!=null)
						operations.action.accept(t);	
				}
				result.setValue(list);
			}

			level = 0;
		} 
		
		if (message.tag==TASK.ordinal() || message.tag==REDUCE.ordinal())
			treeReduction(message);
	}
}