## Nodes4j ##

At `nodes4j` nodes correspond to the processes, based on the process algebra [[1](#1)]. Several processes can be executed both sequentially and in parallel. Figure 1 shows the general workflow of a process. The incoming data of the process P1 is first split evenly and then mapped accordingly. Then the results are merged (Reduce) and sent to the processes P2, P3 and P4 (Hub). The `MapReduce` process is executed in parallel. The advantage of this approach lies in the loose coupling of the nodes or processes. They can be easily exchanged and replaced by others.

<img src="doc/images/workflow.jpg" alt="Schematic representation of the workflow of nodes4j" width="300" height="170"/>

Fig. 1: Schematic representation of the workflow of `nodes4j`

If several processes are connected in sequence at `nodes4j`, this is a linear pipeline. When arranged in parallel around a concurrent pipeline (nonlinear pipeline [[2](#2)]). The process could be optimized by a vertical scaling, if appropriate hardware would be available. This means that the corresponding pipeline layout would need to be duplicated repeatedly in order to be able to scale it vertically. The processes can be arranged to a directional acyclic graph (Figure 2).

<img src="doc/images/dag.jpg" alt="Directional acyclic graph (DAG)" width="274" height="246"/>

Fig. 2: Directional acyclic graph (DAG)

## Implementation ##

The core components of `nodes4j` are `Process`, `NodeActor` and `TaskActor`. A process instantiates a `NodeActor` at startup. The `NodeActor` is the receiving point of the data to be processed. `TaskActors` then process the data (parallel data processing). `Process` provides a variety of operation to apply to the data.

### NodeActor ###

If child nodes are present, they are initialized accordingly. This automatically creates the structure of a directed graph. 
- Upon receipt of the `DATA` message, the list of data is evenly distributed to the newly generated `TaskActors` for parallel processing. 
- If the `NodeActor` is at a final end point of the graph, the result of the processing is stored under its current `UUID` or alias. 
- When all final endpoints of the graph have completed their calculations, the `ActorSystem` is currently automatically shutting down.

### TaskActor ###

- First, on the partial data, the registered node operations (filtering, mapping, etc.) are performed.
- In the second step, the reduce operation is initiated, which may include a binary operation (e.g.). See the lower tree-like communication structure during the reduce operation of the participating `TaskActors`. The communication is asynchronous. The structure of the merge (see Figure 3) enables an optimal load distribution to the involved `TaskActors`.
- The result may be passed on to successor nodes.

<img src="doc/images/tree.jpg" alt="Representation of the tree-like communication structure during the reduction process." width="305" height="278"/>

Fig. 3: Representation of the tree-like communication structure during the reduction process.

## References ##
[1]<a name="1"/> Baeten, J.C.M., Basten, T. & Reniers, M.A (2009). Process Algebra. Equational Theories of Communicating Processes. Band 50. Cambridge University Press.  
[2]<a name="2"/> Mattson, Timothy G., Snaders, Beverly A. & Massingill, Berna L. (2004). A Pattern Language for Parallel Programming. Addison Wesley. Online im Internet: http://www.cise.ufl.edu/research/ParallelPatterns/PatternLanguage/AlgorithmStructure/Pipeline.htm  

Page to be updated 11/09/2018