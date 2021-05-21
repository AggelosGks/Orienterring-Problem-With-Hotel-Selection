package HeurAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import Computabillity.Computation;
import OPHSAttributes.Node;
import OPHSAttributes.POI;

// http://stackoverflow.com/questions/7159259/optimized-tsp-algorithms
public class LowerBoundHK {
	// number of cities
	private final int V;
	// cost matrix
	private final double[][] distances;
	private double final_bound;
	// matrix of adjusted costs
	private double[][] adjustedCosts;
	HeldKarpNode bestNode = new HeldKarpNode();
	private final TreeMap<Integer, Node> dictionary;
	private HashMap<Node, ArrayList<Node>> tree;
	private ArrayList<ArrayList<Node>> paths;
	private final Node end;
	private final Node start;

	/**
	 * Constructor details
	 * 
	 * @param ls contains the nodes 
	 * 8
	 */
	public LowerBoundHK(List<Node> ls) {
		this.start = ls.get(0);
		this.end = ls.get(ls.size() - 1);
		this.dictionary = new TreeMap<Integer, Node>();
		this.paths = new ArrayList<ArrayList<Node>>();
		this.tree = new HashMap<Node, ArrayList<Node>>();
		V = ls.size();
		for (int i = 0; i < ls.size(); i++) {
			dictionary.put(i, ls.get(i));
		}
		distances = new double[V][V];
		for (int i = 0; i < V; i++) {
			for (int j = 0; j < V; j++) {
				distances[i][j] = Computation.EuDi(ls.get(i), ls.get(j));
			}
		}

	}

	//

	/**
	 * Checks if the output of the HK algorithm is path or a tree
	 * @return
	 */
	public boolean isPath() {
		boolean isPath = true;
		for (Node node : tree.keySet()) {
			if (node instanceof POI) {
				if (tree.get(node).size() != 1) {// only one outgoing edge
					isPath = false;
				}
			}
		}
		return isPath;
	}
	
	/**
	 * Extract the ouput of the algorithm if it a path.
	 * @return a list containing the order of the nodes of the path
	 */
	public ArrayList<Node> extractPath() {
		Node lookingfor = null;
		ArrayList<Node> path = new ArrayList<Node>();
		path.add(start);
		lookingfor = tree.get(start).get(0);
		while (path.size() != V) {
			for (Node node : tree.keySet()) {
				if (node.node_id == lookingfor.node_id) {
					path.add(lookingfor);
					if (!tree.get(lookingfor).isEmpty()) {
						lookingfor = tree.get(lookingfor).get(0);
					}
				}
			}
		}
		return path;
	}
	/**
	 * Extract partial paths of the tree.
	 * Specifically, if the ouput is a tree, c
	 */
	public void extractPaths() {
		int counter = 0;
		ArrayList<ArrayList<Node>> found_paths = new ArrayList<ArrayList<Node>>();
		TreeMap<Integer, Boolean> partition = new TreeMap<Integer, Boolean>();
		for (Node node : tree.keySet()) {
			partition.put(node.node_id, false);
		}
		ArrayList<Node> path = new ArrayList<Node>();// init path
		Node lookingfor = null;
		path.add(start);
		counter++;
		partition.put(start.node_id, true);
		lookingfor = tree.get(start).get(0);// not null there is only one edge
		int value = 0;
		if (start.node_id == end.node_id) {
			value = V - 1;// minus one node cause start end is the same
		} else {
			value = V;
		}
		while (counter < value) {
			for (Node node : tree.keySet()) {
				if (node.node_id == lookingfor.node_id) {
					path.add(lookingfor);
					counter++;
					partition.put(lookingfor.node_id, true);
					if (!tree.get(lookingfor).isEmpty()) {
						lookingfor = tree.get(lookingfor).get(0);
					} else {// there is no path we reached a dead end
						found_paths.add(new ArrayList<Node>(path));
																	// path
						path.clear();
						lookingfor = findDisconnected(partition);
						if (lookingfor == null) {
							break;// we hit the end counter is equal to V
							// break just to end the inner for loop
						}
					}
				}
			}
		}
		this.paths = found_paths;
	}

	public void printT(TreeMap<Integer, Boolean> partition) {
		for (Integer i : partition.keySet()) {
			System.out.println(i + " " + partition.get(i));
		}
	}
	
	
	/**
	 * Finds the fist disconnected node from the output
	 */
	public Node findDisconnected(TreeMap<Integer, Boolean> partition) {
		Node found = null;
		// rintT(partition);
		for (Node node : tree.keySet()) {
			if (partition.get(node.node_id) != null) {
				if (partition.get(node.node_id)) {// node exists in some path
					for (Node neigh : tree.get(node)) {
						if (partition.get(neigh.node_id) != null) {
							if (partition.get(neigh.node_id) == false) {
								found = neigh;
							}
						}
					}
				}
			}
		}
		return found;

	}

	public void degrees() {
		HashMap<Node, Integer> degrees = new HashMap<Node, Integer>();
		for (Node node : tree.keySet()) {
			if (tree.get(node).size() > 1) {// branchin occurs
				degrees.put(node, tree.get(node).size());
			}
		}
	}
	/**
	 * Extracts all paths formed by the Held-Karp algorithm.
	 * Might be considered for future use in the K-Stroll. Do not delete.
	 * @return
	 */
	public ArrayList<ArrayList<Node>> getPaths() {
		return paths;
	}
	/**
	 *Forms the first complete trip solution. That is a path including the first point of interest
	 *and the final depot
	 * @return a list representing this path.
	 */
	public ArrayList<Node> formComplete() {
		ArrayList<Node> complete = new ArrayList<Node>();
		ArrayList<Node> start_part = null;
		ArrayList<Node> end_part = null;
		for (ArrayList<Node> l : paths) {
			if (l.get(l.size() - 1).node_id == end.node_id && l.get(0).node_id == start.node_id) {
				return l;
			} else if (l.get(l.size() - 1).node_id == end.node_id) {// thats the
																	// end
				end_part = new ArrayList<Node>(l);
			} else if (l.get(0).node_id == start.node_id) {// that the start
				start_part = new ArrayList<Node>(l);
			}
		}
		System.out.println(start_part.size());
		Node connection = null;
		Node start_end = end_part.get(0);
		for (Node node : start_part) {// search starting path
			if (tree.get(node).size() > 1) {// if node with 2 outgoing edges
				for (Node con : tree.get(node)) {// for each visitor
					if (con.node_id == start_end.node_id) {// if is the link
						connection = node;
						break;
					}
				}
			}
		}
		complete = new ArrayList<Node>(start_part.subList(0, start_part.indexOf(connection) + 1));
		complete.addAll(end_part);
		return complete;
	}

	public boolean lookforNode(Node look) {
		boolean found = false;
		for (Node node : tree.keySet()) {
			if (node.node_id == look.node_id) {
				found = true;
			}
		}
		return found;
	}

	public void execute() {
		bestNode.lb = Double.MAX_VALUE;
		HeldKarpNode currentNode = new HeldKarpNode();
		// intialize structures
		currentNode.non_partition = new boolean[V][V];
		adjustedCosts = new double[V][V];
		// Execute Held-Karp
		computeHKLowerBound(currentNode);
	}

	public TreeMap<Integer, Node> getDictionary() {
		return dictionary;
	}

	public double getFinal_bound() {
		return final_bound;
	}

	// Implements the main HK lower bound
	private void computeHKLowerBound(HeldKarpNode node) {
		node.num = new double[V];
		node.lb = Double.MIN_VALUE;
		node.degree = new int[V];
		node.visitors = new int[V];
		double lambda = 0.1;
		while (lambda > 1e-06) {
			double previousLowerBound = node.lb;
			PrimsMST(node);
			if (!(node.lb < bestNode.lb))
				return;
			if (!(node.lb < previousLowerBound))
				lambda *= 0.9;
			int denominator = 0;
			for (int i = 1; i < V - 1; i++) {
				int degree = node.degree[i] - 2;
				denominator += degree * degree;
			}
			if (denominator == 0) {
				return;// do it with boolean
			}
			double t = lambda * node.lb / denominator;
			for (int i = 1; i < V - 1; i++) {
				node.num[i] += t * (node.degree[i] - 2);
			}
		}
	}

	// Implements the one tree computation
	private void PrimsMST(HeldKarpNode node) {
		// compute adjusted costs
		node.lb = 0.0;
		Arrays.fill(node.degree, 0);
		for (int i = 0; i < V; i++) {
			for (int j = 0; j < V; j++){
				adjustedCosts[i][j] = node.non_partition[i][j] ? Double.MAX_VALUE : distances[i][j] + node.num[i] + node.num[j];
			}
				
		}
		int firstNeighbor = pickFirstCheapest();
		int secondNeighbor = pickSecondCheapest();
		updateBound(node, 0, firstNeighbor);
		Arrays.fill(node.visitors, firstNeighbor);
		node.visitors[firstNeighbor] = 0;
		double[] minCost = adjustedCosts[firstNeighbor].clone();
		for (int k = 2; k < V - 1; k++) {
			int z_d;
			for (z_d = 1; z_d < V - 1; z_d++) {
				if (node.degree[z_d] == 0){
					break;
				}	
			}
			for (int z = z_d + 1; z < V - 1; z++) {
				if (node.degree[z] == 0 && minCost[z] < minCost[z_d])
					z_d = z;
			}
			updateBound(node, node.visitors[z_d], z_d);
			for (int j = 1; j < V - 1; j++) {
				if (node.degree[j] == 0 && adjustedCosts[z_d][j] < minCost[j]) {
					minCost[j] = adjustedCosts[z_d][j];
					node.visitors[j] = z_d;
				}
			}
		}
		updateBound(node, V - 1, secondNeighbor);
		node.visitors[V - 1] = secondNeighbor;

		node.lb = node.lb;
		final_bound = node.lb;
		FormOutput(node);
	}

	// initializes the tree structure
	public void FormOutput(HeldKarpNode node) {
		tree.clear();
		for (int i = 0; i < V; i++) {
			tree.put(dictionary.get(i), new ArrayList<Node>());// initialize
																// tree
																// structure
		}
		for (int i = 0; i < V; i++) {
			if (i != 0) {
				tree.get(dictionary.get(node.visitors[i])).add(dictionary.get(i));
			}
		}
		final_bound = node.lb;

	}

	public void printTree() {
		for (Entry<Node, ArrayList<Node>> entry : tree.entrySet()) {
			System.out.println(entry.getKey().node_id + " outgoing " + Computation.ListToStringNodes(entry.getValue()));
		}
	}
	/**
	 * Picks the cheapest edge to connect the initial node
	 * @return the id of the node
	 */
	public int pickFirstCheapest() {
		double min = Double.MAX_VALUE;
		int index = 0;
		for (int i = 1; i < V - 1; i++) {
			if (adjustedCosts[0][i] < min) {
				min = adjustedCosts[0][i];
				index = i;
			}
		}
		return index;
	}
	/**
	 * Picks the cheapest edge to connect the final depot
	 * @return the id of the node
	 */
	public int pickSecondCheapest() {
		double min = Double.MAX_VALUE;
		int index = 0;
		for (int i = 1; i < V - 1; i++) {
			if (adjustedCosts[V - 1][i] < min) {
				min = adjustedCosts[V - 1][i];
				index = i;
			}
		}
		return index;
	}
	
	/**
	 * Updates degrees, total weight, and visitors for the output.
	 * 
	 */
	private void updateBound(HeldKarpNode node, int i, int j) {
		node.lb += adjustedCosts[i][j];//increase the bound 
		node.degree[i]++;// increase degree of node for future iterations
		node.degree[j]++;// increase degree of node for future iterations
	}
}

class HeldKarpNode {
	public boolean[][] non_partition;
	// Held--Karp solution
	public double[] num;
	public double lb;
	public int[] degree;
	public int[] visitors;
}

class NodeComparator implements Comparator<HeldKarpNode> {
	public int compare(HeldKarpNode a, HeldKarpNode b) {
		return Double.compare(a.lb, b.lb);
	}
}
