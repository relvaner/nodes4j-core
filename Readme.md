[![Build Status](https://travis-ci.org/relvaner/nodes4j-core.svg?branch=master)](https://travis-ci.org/relvaner/nodes4j-core)
[![Coverage Status](https://coveralls.io/repos/github/relvaner/nodes4j-core/badge.svg?branch=master)](https://coveralls.io/github/relvaner/nodes4j-core?branch=master)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://www.apache.org/licenses/)

> This library has been **moved to [Actor4j - Streams]([https://link.to.xyz](https://github.com/relvaner/actor4j/tree/master/actor4j-streams))**. ##

## Nodes4j ##

At `nodes4j` nodes correspond to the processes, following the process algebra [[1](#1)]. Several processes can be executed both sequentially and in parallel. Figure 1 shows the general workflow of a process. The incoming data of the process P1 is first split evenly and then mapped accordingly. Then the results are merged (Reduce) and sent to the processes P2, P3 and P4 (Hub). The `MapReduce` process is executed in parallel. The advantage of this approach lies in the loose coupling of the nodes or processes. They can be easily exchanged and replaced by others.

<img src="doc/images/workflow.jpg" alt="Schematic representation of the workflow of nodes4j" width="450" height="255"/>

Fig. 1: Schematic representation of the workflow of `nodes4j`

If several processes are connected in sequence at `nodes4j`, this is a linear pipeline. When arranged in parallel around a concurrent pipeline (nonlinear pipeline [[2](#2)]). The process could be optimized by a vertical scaling, if appropriate hardware would be available. This means that the corresponding pipeline layout would need to be duplicated repeatedly in order to be able to scale it vertically. The processes can be arranged to a directional acyclic graph (Figure 2).

<img src="doc/images/dag.jpg" alt="Directional acyclic graph (DAG)" width="329" height="295"/>

Fig. 2: Directional acyclic graph (DAG)

## Implementation ##

The core components of `nodes4j` are `Process`, `NodeActor` and `TaskActor`. A process instantiates a `NodeActor` at startup. The `NodeActor` is the receiving point of the data to be processed. `TaskActors` then process the data in parallel. `Process` provides a variety of operation to apply to the data.

### NodeActor ###

If child nodes are present, they are initialized accordingly. This automatically creates the structure of a directed graph. 
- Upon receipt of the `DATA` message, the list of data is evenly distributed to the newly generated `TaskActors` for parallel processing. 
- If the `NodeActor` is at a final end point of the graph, the result of the processing is stored under its current `UUID` or alias. 
- When all final endpoints of the graph have completed their calculations, the `ActorSystem` is currently automatically shutdown.

### TaskActor ###

- First, on the partial data, the registered node operations (filtering, mapping, etc.) are performed.
- In the second step, the reduce operation is initiated, which may include a binary operation (e.g., new result of p0 x p1 -> p0*). See the lower tree-like communication structure during the reduce operation of the participating `TaskActors`. The communication is asynchronous. The structure of the merge process (see Figure 3) enables an optimal load distribution to the involved `TaskActors`.
- The result may be passed on to successor nodes.

<img src="doc/images/tree.jpg" alt="Representation of the tree-like communication structure during the reduction process." width="366" height="334"/>

Fig. 3: Representation of the tree-like communication structure during the reduction process

## Internal DSL ##

```java
process1
    // intra-process operation
    .data(...)
    .filter(...)
    .map(...)
    .forEach(...)
    .reduce(…)
    
    // or
    .data(...)
    .flatMap(...)
    .reduce(…)
    
    // or with Java Stream API
    .data(...)
    .stream(s -> s.filter(...).map(...))
    .reduce(…)
    
    // or with RxJava 2
    .data(...)
    .streamRx(o -> o.filter(...).map(...))
    .reduce(…)
    
    // sorting as inter-process operation
    .sortedASC() or .sortedDESC()

    // inter-process operation
    .sequence(process2, process3.parallel(process5, process6, process7));
        
process8.merge(process6, process7);
```
## Example ##

```java
Process<Integer, Integer> process_main = new Process<>("process_main");
process_main
	.data(List.of(14, 31, 34, 45, 78, 99, 123, 9257));
		
Process<Integer, Integer> process_a = new Process<>("process_a");
process_a
	.filter((v) -> v>50 && v<100)
	.map((v) -> v+2);
Process<Integer, Integer> process_b = new Process<>("process_b");
process_b
	.filter((v) -> v>0 && v<=50)
	.map((v) -> v+1);
Process<Integer, Integer> process_sort_asc = new SortProcess<Integer>("process_sort_asc",
	SortType.SORT_ASCENDING);		
		
process_main.parallel(process_a, process_b);
process_sort_asc.merge(process_a, process_b);
		
ProcessManager manager = new ProcessManager(true);
manager
	.onTermination(() -> { 
		logger().debug("Data (process_a): "+manager.getData("process_a")); 
		logger().debug("Data (process_a): "+process_a.getData()); 
		logger().debug("Data (process_b): "+manager.getData("process_b")); 
		logger().debug("Data (process_sort_asc): "+manager.getData("process_sort_asc")); 
		logger().debug("Result (process_sort_asc): "+manager.getResult("process_sort_asc")); 
	})
	.start(process_main);
```

## License ##
This framework is released under an open source Apache 2.0 license.

## Announcement ##
This software framework is currently under an prototype state.

## References ##
[1]<a name="1"/> Baeten, J.C.M., Basten, T. & Reniers, M.A (2009). Process Algebra. Equational Theories of Communicating Processes. Volume 50. Cambridge University Press.  
[2]<a name="2"/> Mattson, Timothy G., Snaders, Beverly A. & Massingill, Berna L. (2004). A Pattern Language for Parallel Programming. Addison Wesley. http://www.cise.ufl.edu/research/ParallelPatterns/PatternLanguage/AlgorithmStructure/Pipeline.htm  

Page to be updated 11/09/2018
