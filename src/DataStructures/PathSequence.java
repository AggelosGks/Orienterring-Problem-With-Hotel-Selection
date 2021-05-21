package DataStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

import OPHSAttributes.Edge;
import OPHSAttributes.Hotel;
import OPHSAttributes.HotelPoiEEdge;
import OPHSAttributes.HotelPoiSEdge;
import OPHSAttributes.POI;
import OPHSAttributes.PoiPoiEdge;

public class PathSequence {
	public final ArrayList<Edge> path;
	private double amount_distr;
	private double utility;
	private final double limit;
	private final Hotel start;
	private final Hotel end;
	
	public PathSequence(double limit,Hotel start,Hotel end) {
		this.path = new ArrayList<Edge>();
		this.amount_distr = 0;
		this.utility=0;
		this.limit=limit;
		this.start=start;
		this.end=end;
	}

	public double getAmount_distr() {
		return amount_distr;
	}

	public void setAmount_distr(double amount_distr) {
		this.amount_distr = amount_distr;
	}

	public void calculateUtil(){
		double utility=0;
		double available=amount_distr;
		HashMap<Edge,Double> map=new HashMap<Edge,Double>();
		for(Edge e : path){
			map.put(e, Math.min(available, e.getDistance()));
			available=available-map.get(e);
			if(available<=0){
				break;
			}
		}
		Iterator m = map.entrySet().iterator();
		while (m.hasNext()) {
			Map.Entry pair = (Map.Entry)m.next();
	        Edge e=(Edge) pair.getKey();
	        Double dist=(Double) pair.getValue();
	        
	        utility=utility+dist*e.getWeight();
	    }
		this.utility=utility;
	}

	public double getUtility() {
		return utility;
	}

	public void setUtility(double utility) {
		this.utility = utility;
	}
	
	public Edge extractFirstIncompleteEdge(){
		Edge edgereturn=null;
		for(Edge edge : path){
			if(!edge.isFull()){
				edgereturn=edge;
				break;
			}
		}
		return edgereturn;
	}
	
	private ArrayList<POI> getPartition(){
		ArrayList<POI> partition=new ArrayList<POI>();
		for(Edge e : path){
			if(e instanceof PoiPoiEdge){
				partition.add((POI)((PoiPoiEdge)e).start);
			}
		}
		return partition;
	}
	
	public void flowPath(double value){
		Edge edge=this.extractFirstIncompleteEdge();
		double available=edge.getRemainder();
		double feasible=Math.min(available,value);
		edge.setCovered(feasible);
		amount_distr=amount_distr+feasible;
		if(edge.isFull()){//remaining value for flow
			Edge next=nextPick(edge);
			double remain=value-feasible;
			next.setCovered(remain);
			this.path.add(next);
		}
	}
	
	public Edge nextPick(Edge edge){
		POI last=(POI)edge.getFinalNodeOfEdge();
		ArrayList<POI> path_partition=getPartition();//get partition
		PriorityQueue<PoiPoiEdge> q=new PriorityQueue<PoiPoiEdge>(last.getStart_other());//get visitors
		ArrayList<POI> feasible_visitors=last.collect(limit-amount_distr, start, last);//get feasible next ones
		if(feasible_visitors==null){//no visitors available ensure feasibility due to collection mode
			return new HotelPoiEEdge(last,end);
		}else{
			for(PoiPoiEdge e : q){//clean up
				if(!(feasible_visitors.contains(e.end))||(path_partition.contains(e.end))){//next poi should belong to feasible visitors and not in partition
					q.remove(e);
				}
			}
		}
		return q.remove();
	}
	
	
	
	public void printPath(){
		String x="";
		for(Edge edge : this.path){
			if(edge instanceof HotelPoiSEdge){
				x=x+" "+"H"+edge.start.node_id+"- P"+edge.end.node_id;
			}else if(edge instanceof HotelPoiEEdge){
				x=x+" "+"P"+edge.start.node_id+"- H"+edge.end.node_id;
			}else{
				x=x+" "+"P"+edge.start.node_id+"- P"+edge.end.node_id;
			}
		}
		System.out.println(x);
	}
}
