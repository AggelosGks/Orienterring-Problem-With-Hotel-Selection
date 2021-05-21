package OPHSAttributes;

import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import Computabillity.Computation;
import OPHSAttributes.Node;



public class Hotel extends Node{
	
	private static final int K=15;
	private  static  final ArrayList<Hotel> hotel_population=new ArrayList<Hotel>();
	public final TreeMap<Integer, ArrayList<Hotel>> visitors_limits;
	public final TreeMap<Integer, ArrayList<Hotel>> previous_limits;
	public final TreeMap<Integer,ArrayList<Trip>> trips;
	public final ArrayList<Hotel> k_visitors;
	public final TreeMap<Integer,PriorityQueue<TripTriplet>> id_triplets;
	public final PriorityQueue<HotelPoiEEdge> finish;
	public final PriorityQueue<HotelPoiSEdge> start;
	
	
	private int score;
	
	public Hotel(double x,double y,int id){
		super(x,y,id);
		this.score=0;
		hotel_population.add(this);
		visitors_limits=new TreeMap<Integer, ArrayList<Hotel>>();
		previous_limits=new TreeMap<Integer, ArrayList<Hotel>>();
		this.finish=new PriorityQueue<HotelPoiEEdge>();
		this.start=new PriorityQueue<HotelPoiSEdge>();
		this.trips=new TreeMap<Integer,ArrayList<Trip>> ();
		this.k_visitors=new ArrayList<Hotel>();
		this.id_triplets=new TreeMap<Integer,PriorityQueue<TripTriplet>>();
	}
	public Hotel(int id){
		super(0,0,id);
		this.score=0;
		hotel_population.add(this);
		visitors_limits=new TreeMap<Integer, ArrayList<Hotel>>();
		previous_limits=new TreeMap<Integer, ArrayList<Hotel>>();
		this.finish=new PriorityQueue<HotelPoiEEdge>();
		this.start=new PriorityQueue<HotelPoiSEdge>();
		this.trips=new TreeMap<Integer,ArrayList<Trip>> ();
		this.k_visitors=new ArrayList<Hotel>();
		this.id_triplets=new TreeMap<Integer,PriorityQueue<TripTriplet>>();
	}
	
	public void PrintTriplets(){
		for (Map.Entry<Integer, PriorityQueue<TripTriplet>> entry : id_triplets.entrySet()) {
			for(TripTriplet triplet : entry.getValue()){
				triplet.print();
			}
		     
		}
	}

	
	
	//setters and getters
	public static ArrayList<Hotel> getHotel_population() {
		return hotel_population;
	}

	public  void fillQueues(){
		for(POI p : POI.getPoi_population()){
			this.finish.add(new HotelPoiEEdge(p,this));
			this.start.add(new HotelPoiSEdge(this,p));
		}
	}

	public ArrayList<Hotel> getKNearestFromSet(int i){
		int add=0;
		ArrayList<Hotel>  visitors=new ArrayList<Hotel>();
		PriorityQueue<NodeWithDist> pq=new PriorityQueue<NodeWithDist>();
		if(this.visitors_limits.get(i)!=null){
			for(Hotel h : this.visitors_limits.get(i)){
				pq.add(new NodeWithDist(h,Computation.EuDi(this,h)));
			}
		}
		boolean control=true;
		while(add<K&&control){
			if(pq.peek()!=null){
				visitors.add((Hotel)pq.remove().node);
				add++;
			}else{
				control=false;
			}
		}
		return visitors;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	public static Hotel getStartDepot(){
		Hotel initial=null;
		for(Hotel e: hotel_population){
			if(e.node_id==0){
				initial=e;
			}
		}
		return initial;
		
	}

	public static Hotel getFinalDepot(){
		Hotel last_depot=null;
		for(Hotel c:hotel_population){
			if(c.node_id==1){
				last_depot=c; 
			}
		}
		return last_depot;
	}
	
	public static Hotel getHotelById(int id){
		Hotel to_return=null;
		for(Hotel runner : hotel_population){
			if(runner.node_id==id){
				to_return= runner;
				break;
			}
		}
		return to_return;
	}
	
	public static int getNumberOfExtraHotels(){
		return hotel_population.size()-2;
	}
	
		
	public boolean isStartFinishDepot(){
		boolean startfinish=false;
		if(this.node_id==1||this.node_id==0){
			startfinish=true;
		}
		return startfinish;
			
			
	}
		
	public POI pickPOI(ArrayList<POI> hood){
		POI start_one=null;
		double max=Integer.MIN_VALUE;
		for(POI p : hood){
			double value=(p.getScore()/Computation.EuDi(this, p));
			if(value>max){
				start_one=p;
				max=value;
			}
		}
		return start_one;
	}
	
	public void pickKnearestHotels(int k){
		PriorityQueue<NodeWithDist> pq=new PriorityQueue<NodeWithDist>();
		for(Hotel h : hotel_population){
			if(h.node_id!=this.node_id){
				pq.add(new NodeWithDist(h,Computation.EuDi(this,h)));
			}
		}
		while(this.k_visitors.size()<k){
			Hotel peek=(Hotel)pq.peek().node;
			boolean proceed=true;
			if(peek.k_visitors!=null){
				if(peek.k_visitors.contains(this)){
					proceed=false;
				}
			}
			if(proceed){
				this.k_visitors.add(peek);
			}
			pq.remove();
		}
		System.out.println(this.node_id+ "     " +Computation.ListToStringHotels(this.k_visitors));
	}
	
	public void pickKnearestDepots(int k){
		
		PriorityQueue<NodeWithDist> pq=new PriorityQueue<NodeWithDist>();
		for(Hotel h : hotel_population){
			pq.add(new NodeWithDist(h,Computation.EuDi(this,h)));
		}
		while(this.k_visitors.size()<k){
			Hotel peek=(Hotel)pq.peek().node;
			boolean proceed=true;
			if(peek.k_visitors!=null){
				if(peek.k_visitors.contains(this)){
					proceed=false;
				}
			}
			if(proceed){
				this.k_visitors.add(peek);
			}
			pq.remove();
		}
	}
	
	public static void printkNearest(){
		for(Hotel h : hotel_population){
			System.out.println(h.node_id+" "+Computation.ListToStringHotels(h.k_visitors));
		}
	}


	public TreeMap<Integer, ArrayList<Hotel>> getVisitors_limits() {
			return visitors_limits;
		}
	/**
	 * Selectes k nearest visitors for the extra hotels
	 */
	public static void preprocessHotels(){
		for(Hotel h : getHotel_population()){
			if(h.node_id!=0&&h.node_id!=1){
				h.pickKnearestHotels(K);
			}
			
		}
	}
	/**
	 * Selectes k nearest visitors for the depots
	 */
	public static void preprocessDepots(){
		getStartDepot().pickKnearestDepots(K);
		getFinalDepot().pickKnearestDepots(K);
	}
	/**
		 * Checks the visitors hotel sets. For two givens hotel implements the
		 * b belong to V_a^i and a belongs to a to V_b^i. Finds the maximum Ti that satisfies
		 * that satisfies this relationship. Used in non and partial sequential solving. In order
		 * to obtain intermediate values.
		 * @param start
		 * @param end
		 * @return
		 */
		//case Td+1>Td and x has y for d , y has x for d+1 but x does not have y for d+1
	public static int getMaxTripLimitBetween(Hotel start, Hotel end){
			double max_limit=Double.MIN_VALUE;
			int max_index=-1;
			for(int i=0; i<Trip.getNumber_of_trips(); i++){
				if(start.visitors_limits.get(i)!=null){
					if(start.visitors_limits.get(i).contains(end)){
						if(Trip.getLength_limitById(i)>max_limit){
							max_limit=Trip.getLength_limitById(i);
							max_index=i;
						}
					}
				}
			}
			for(int i=0; i<Trip.getNumber_of_trips(); i++){
				if(end.visitors_limits.get(i)!=null){
					if(end.visitors_limits.get(i).contains(start)){
						if(Trip.getLength_limitById(i)>max_limit){
							max_limit=Trip.getLength_limitById(i);
							max_index=i;
						}
					}
				}
			}
			return max_index;
		}

}
