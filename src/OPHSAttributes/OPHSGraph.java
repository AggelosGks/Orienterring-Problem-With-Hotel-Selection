package OPHSAttributes;

import java.util.ArrayList;

public class OPHSGraph {
	private final ArrayList<Node> Nodes;
	private final ArrayList<Edge> Edges;
	private static OPHSGraph graph;//autosave itself

	public OPHSGraph(ArrayList<Node> Nodes,ArrayList<Edge> Edges){
		this.Nodes=Nodes;
		this.Edges=Edges;
		graph=this;
	}

	public static OPHSGraph getGraph() {
		return graph;
	}

	public static void setGraph(OPHSGraph graph) {
		OPHSGraph.graph = graph;
	}

	public ArrayList<Node> getNodes() {
		return Nodes;
	}

	public ArrayList<Edge> getEdges() {
		return Edges;
	}

}
