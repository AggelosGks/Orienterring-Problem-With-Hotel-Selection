package OPHSAttributes;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeMap;

import Computabillity.Computation;

public class POI extends Node {

	private static final ArrayList<POI> poi_population = new ArrayList<POI>();
	
	// instance fields
	private int score;
	private final PriorityQueue<PoiPoiEdge> start_other;// in this queue the
														// start node is the
														// same for all edges
	private final PriorityQueue<PoiPoiEdge> other_finish;// in this queue the
															// finish node is
															// the same for all
															// edges
	private double min_dist;
	public  TreeMap<Integer, ArrayList<POI>> visitors;
	public  TreeMap<Integer, Integer> bound_scores;
	public  TreeMap<Integer, ArrayList<POI>> times_paths;
	
	public  TreeMap<Double, Integer> D_bound_scores;
	public  TreeMap<Double, ArrayList<POI>> D_times_paths;

	public POI(double x, double y, int id, int score) {
		super(x, y, id);
		this.score = score;
		
		this.start_other = new PriorityQueue<PoiPoiEdge>();
		this.other_finish = new PriorityQueue<PoiPoiEdge>();
		this.visitors = new TreeMap<Integer, ArrayList<POI>>();
		this.bound_scores = new TreeMap<Integer, Integer>();
		this.times_paths=new TreeMap<Integer,ArrayList<POI>>();
		this.D_bound_scores = new TreeMap<Double, Integer>();
		this.D_times_paths=new TreeMap<Double,ArrayList<POI>>();
		poi_population.add(this);
	}

	// getters and setters
	public double getMin_dist() {
		return min_dist;
	}

	public void setMin_dist(double min_dist) {
		this.min_dist = min_dist;
	}

	public void ComputeMinDistIntegers(Hotel end) {
		double min=Double.MAX_VALUE;
		for(POI p : poi_population){//for every connected poi 
			if(p.node_id!=this.node_id){
				if(Computation.EuDiRound(this, p)+Computation.EuDiRound(p,end)<min){//which minimizes the complete solution
					min=Computation.EuDiRound(this, p)+Computation.EuDiRound(p,end);
				}
			}
		}
		this.min_dist=min;
	}
	public void ComputeMinDistDecimals(Hotel end) {
		double min=Double.MAX_VALUE;
		for(POI p : poi_population){//for every connected poi 
			if(p.node_id!=this.node_id){
				if(Computation.EuDi(this, p)+Computation.EuDi(p,end)<min){//which minimizes the complete solution
					min=Computation.EuDi(this, p)+Computation.EuDi(p,end);
				}
			}
		}
		this.min_dist=min;
	}
	
/**
 * Initializes N data structures to store the bounds
 */
	public void preprocessBoundsIntegers(){
		for(int i=1; i<this.min_dist; i++){
			this.bound_scores.put(i,0);//no additional score to be collected
		}
	}
	/**
	 * Initiliazes N data structures to store the paths corresponding to each bound
	 */
	public void preprocessPathsIntegers(){
		for(int i=1; i<this.min_dist; i++){
			this.bound_scores.put(i,null);//no additional poi to be added
		}
	}
	/**
	 * Implementation for decimals
	 */
	public void preprocessBoundsDecimals(){
		double step=POI.min_distOfGraph();
		for(double i=0; i<this.min_dist; i=i+step){
			this.D_bound_scores.put(i,0);//no additional score to be collected
		}
	}
	public void preprocessPathsDecimals(){
		double step=POI.min_distOfGraph();
		for(double i=0; i<this.min_dist; i=i+step){
			this.D_times_paths.put(i,null);//no additional score to be collected
		}
	}
	
	public double getLastKey(double time){

		
		double last_key=0;
		for(Entry<Double, Integer> entry : this.D_bound_scores.entrySet()) {
		Double key = entry.getKey();
			if(Computation.accurateRound(key,2)>time){
				break; 
			}else{
				last_key=key; 
			}
		}
		return last_key;
	}
	
	public int getBound(double time){
		int bound=0;
		double last=this.getLastKey(time);
		bound=this.D_bound_scores.get(last);
		return bound;
	}
	public ArrayList<POI> getPath(double time){
		ArrayList<POI> path=new ArrayList<POI>();
		double last=this.getLastKey(time);
		path=this.D_times_paths.get(last);
		return path;
	}
	
	public void ComputeVisitorsIntegers(Double limit, Hotel finish) {
		for (int t = 0; t <= limit; t++) {//gradually increase
			String x = "";
			for (POI z : poi_population) {//for all pois 
				if (z.node_id != this.node_id) {

					double to = Computation.EuDiRound(this, z);//to go 
					double end = Computation.EuDiRound(z, finish);//to finish
					if (to + end <= t) {
						x = x + " " + Integer.toString(z.node_id);
						if (this.visitors.get(t) == null) {
							this.visitors.put(t, new ArrayList<POI>());
						}
						this.visitors.get(t).add(z);
					}
				}
			}
			// System.out.println(t+" Node "+this.node_id+" "+x);
		}

	}
	public void ComputeVisitorsIntegersDecimals(Double limit, Hotel finish) {
		for (int t = 0; t <= limit; t++) {//gradually increase
			String x = "";
			for (POI z : poi_population) {//for all pois 
				if (z.node_id != this.node_id) {

					double to = Computation.EuDi(this, z);//to go 
					double end = Computation.EuDi(z, finish);//to finish
					if (to + end <= t) {
						x = x + " " + Integer.toString(z.node_id);
						if (this.visitors.get(t) == null) {
							this.visitors.put(t, new ArrayList<POI>());
						}
						this.visitors.get(t).add(z);
					}
				}
			}
			// System.out.println(t+" Node "+this.node_id+" "+x);
		}

	}
	
	
	public ArrayList<POI> getVisitors(double time,Hotel f){
		int int_value=(int)time;
		ArrayList<POI> visitors=null;
		if(time>0){
			if(this.visitors.get(int_value)!=null){
				visitors=new ArrayList<POI>(this.visitors.get(int_value));
			}	
			int int_next=int_value+1;
			if(this.visitors.get(int_next)!=null){
				visitors=new ArrayList<POI>();
				for(POI p : this.visitors.get(int_next)){
					double go=Computation.EuDi(this, p);
					double finish=Computation.EuDi(p, f);
					double total=go+finish;
					if(Computation.round(total,1)<=time){
						if(!visitors.contains(p)){
							visitors.add(p);
							}
					}
				}
			}
		}
		return visitors;
	}
 
	
	public ArrayList<POI> getVisitorsUnderPartition(double time,Hotel f,TreeMap<Integer,Boolean> map){
		int int_value=(int)time;
		ArrayList<POI> visitors=null;
		if(time>0){
			if(this.visitors.get(int_value)!=null){
				visitors=new ArrayList<POI>(this.visitors.get(int_value));
			}	
			int int_next=int_value+1;
			if(this.visitors.get(int_next)!=null){
				visitors=new ArrayList<POI>();
				for(POI p : this.visitors.get(int_next)){
					double go=Computation.EuDi(this, p);
					double finish=Computation.EuDi(p, f);
					double total=go+finish;
					if(Computation.round(total,1)<=time){
						if(!visitors.contains(p)&&map.get(p.node_id)!=null){
							if(!map.get(p.node_id)){
								visitors.add(p);
							}
							}
					}
				}
			}
		}
		return visitors;
	}

	public static void TestQueues() {
		for (POI p : poi_population) {
			System.out.println("Node : " + p.node_id + " -->");
			System.out.println("------------------------------");
			PriorityQueue<PoiPoiEdge> q = new PriorityQueue<PoiPoiEdge>(p.start_other);
			while (q.size() > 0) {
				PoiPoiEdge l = q.remove();
				System.out.println(l.end.node_id + " " + l.weight);
			}
			System.out.println("------------------------------");
		}
		for (POI p : poi_population) {
			System.out.println("Node : " + p.node_id + " <--");
			System.out.println("------------------------------");
			PriorityQueue<PoiPoiEdge> q = new PriorityQueue<PoiPoiEdge>(p.other_finish);
			while (q.size() > 0) {
				PoiPoiEdge l = q.remove();
				System.out.println(l.start.node_id + " " + l.weight);
			}
			System.out.println("------------------------------");
		}
	}

	public static void fillQueues() {
		for (POI poi : poi_population) {
			for (POI poi2 : poi_population) {
				if (poi2.node_id != poi.node_id) {
					new PoiPoiEdge(poi, poi2);
				}
			}
		}

	}

	public static int getNumber_poi() {
		return poi_population.size();
	}

	public PriorityQueue<PoiPoiEdge> getStart_other() {
		return start_other;
	}

	public PriorityQueue<PoiPoiEdge> getOther_Finish() {
		return other_finish;
	}

	public static int calculateMaximumScore() {
		int score = 0;
		for (POI poi : poi_population) {
			score = score + poi.getScore();
		}
		return score;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public static ArrayList<POI> getPoi_population() {

		return poi_population;
	}

	public static POI GetPOIById(int id) {
		POI ret = null;
		for (POI p : poi_population) {
			if (p.node_id == id) {
				ret = p;
			}
		}
		return ret;
	}

	// end getters and setters
	public static void printCord() {
		for (POI e : poi_population) {
			System.out.println(e.node_id + " " + e.x_cordinate + " " + e.y_cordinate);
		}
	}


	
	public static double min_distOfGraph(){
		double min_dist=Double.MAX_VALUE;
		for(POI p1 : POI.getPoi_population()){
			for(POI p2 : POI.getPoi_population()){
				if(Computation.EuDi(p1,p2)<min_dist&&p1.node_id!=p2.node_id){
					min_dist=Computation.EuDi(p1,p2);
				}
			}
		}
		return min_dist;
	}
}
