package OPHSAttributes;

import java.util.ArrayList;

import Computabillity.Computation;

public class Edge {
	private static final ArrayList<Edge> total_edges = new ArrayList<Edge>();
	private static int id_share = 0;
	public final Node start;
	public final Node end;
	public final double distance;
	protected double covered;
	protected double weight;
	public final int id;

	public Edge(Node start,Node end) {
		this.start=start;
		this.end=end;
		this.distance=Computation.EuDi(start, end);
		this.covered=0;
		id_share++;
		this.id = id_share;
	}

	public int getId() {
		return id;
	}

	public static ArrayList<Edge> getTotal_edges() {
		return total_edges;
	}


	public double getDistance() {
		return distance;
	}

	public boolean isFull() {
		if(covered==distance){
			return true;
		}else{
			return false;
		}
	}
	
	public double getRemainder() {
		return distance-covered;
	}
	
	
	public void setCovered(double covered) {
		this.covered = covered;
	}

	public Node getFinalNodeOfEdge(){
		return end;
	}

	public double getCovered() {
		return covered;
	}

	public double getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return "Edge [start=" + start.node_id + ", end=" + end.node_id + ", distance=" + distance + ", covered=" + covered + ", weight="
				+ weight + ", id=" + id + "]";
	}
	
	
}
