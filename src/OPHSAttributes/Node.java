
package OPHSAttributes;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeMap;

import Computabillity.Computation;




public class Node {

	private static int number_nodes=0;
	private static final ArrayList<Node> total_population=new ArrayList<Node>();
	
	//instance fields
	public final  int node_id;//unique id
	protected final double x_cordinate;//x cord
	protected final double y_cordinate;//y cord
	protected PriorityQueue<NodeWithDist> dist_to_nodes;//data structure nearest visitors
	protected ArrayList<Hotel> hood;//set of visitors
	
	/**
	Common constructor for node
	@param x : the x cordinate of node
	@param y : the y cordinate of node
	@param id : the id of node 
 */ 
	public Node(double x,double y,int id){
		number_nodes++;
		this.node_id=id;
		this.x_cordinate=x;
		this.y_cordinate=y;
		this.dist_to_nodes=new PriorityQueue<NodeWithDist>();
		this.hood=new ArrayList<Hotel>();
		total_population.add(this);

	}

	/**
	Constructor for mid node between two primary node on graph. Node does not trully exist.
 */
	public Node(double x,double y){
		this.x_cordinate=x;
		this.y_cordinate=y;
		this.node_id=-1;
	}
	
	
	//Getters and Setters
	public static int getNumber_nodes() {
		return number_nodes;
	}

	public static void setNumber_nodes(int number_nodes) {
		Node.number_nodes = number_nodes;
	}

	public int getNode_id() {
		return node_id;
	}

	public double getX_cord() {
		return x_cordinate;
	}

	public double getY_cord() {
		return y_cordinate;
	}

	public ArrayList<Hotel> getHood() {
		return hood;
	}

	public static ArrayList<Node> getTotal_population() {
		return total_population;
	}

	/**
	 * Selectes the nearest visitor of the node calling the function
	 * @param hotel_population
	 * @return
	 */
	public Hotel getNearestHotel(ArrayList<Hotel> hotel_population){
		
		PriorityQueue<NodeWithDist> temp=new PriorityQueue<NodeWithDist>(this.dist_to_nodes);//defensive copy of 
		Node c=null;
		boolean control=true;
		while(control){
			if(temp.peek()==null){
				throw new AssertionError("Elimination of Hotels to select");
			}
			c=temp.remove().node;
			if(c instanceof Hotel){//check if it is hotel
			
				if(hotel_population.contains(c)){//check that exists in current pop given
					control=false;
				}
			}
		}
		return (Hotel)c;
	}
	
	
	
	


	
	//both itself added cause start and final depot may be used as intermediate
	public  void getNeighbourDistance(){
		for(Node runner:total_population){
				double dist=Computation.EuDi(this, runner);
				NodeWithDist temp=new NodeWithDist(runner,dist);
				this.dist_to_nodes.add(temp);
				
		}
	}
	
	/**
	 * 
	 * This method computes all feasible visitors for the node calling the method under certain
	 * trip limits computed according to the trip id.One loop handles all the feasible visitors for
	 * the current trip.The second loop is responsible for the forecast.The idea of forecast is that
	 * until the prelast trip all visitors should have an intermediate hotel.
	 * As far as the second loop is concerned we have to control the selection with 
	 * an if/else because start or finish depot may act as selection and forecast hotel in the same 
	 * iteration.Notice that the same action is performed in both cases with a slight difference that 
	 * if selection hotel is the final or start depot we allow the forecast hotel to take the same 
	 * value but in any other case not.(e.g consider the path 0-2-1-1 for a 4-trip instance, while
	 * computing visitors for hotel 2 in the second trip,s_hotel sometime eventually  will represent 
	 * the final depot, therefore we should allow f_hotel to instantly take the same value)
	 * The algorithm is implemented according to the constraints of the classic variant of the OPHS problem.
	 * where only final and start depot (0 and 1 respectively) can be visited more than once.
	 * 
	 * @param trip_id
	 * @return A list with all feasible visits
	 * 
	 */
	public  ArrayList<Hotel> getHotelVisitorsUnderRadius(int trip_id) {
		ArrayList<Hotel> set = new ArrayList<Hotel>();
		ArrayList<Hotel> h_pop =(ArrayList<Hotel>) Hotel.getHotel_population().clone();	
		Hotel final_dep=Hotel.getFinalDepot();
		TreeMap<Integer,Double> id_constraints=new TreeMap<Integer,Double>(Trip.getTripids_TimeConstraints());
		int no_trips=Trip.getNumber_of_trips();
		double Tmax=Tour.gettour_length_budget();
		if (trip_id < no_trips - 1) {//until pre-last trip
			double push_radius = id_constraints.get(trip_id);
			double pull_radius = Tmax - Trip.SumUpConstraints2(trip_id);
			double next_push_radius = id_constraints.get(trip_id + 1);
			double next_pull_radius = Tmax-Trip.SumUpConstraints2(trip_id + 1);
			for (Hotel s_hotel : h_pop) {// select hotel
				double push_length=Computation.EuDi(this, s_hotel);//move distance
				double pull_length=Computation.EuDi(s_hotel,final_dep);//end distance
				if ( push_length<= push_radius&&pull_length<= pull_radius&& s_hotel.getNode_id() != this.getNode_id()) {
					// forecast start
					if(s_hotel.isStartFinishDepot()){//forecast and selection hotel may carry the same value
						for (Hotel f_hotel : h_pop) {
							double fpush_length=Computation.EuDi(s_hotel, f_hotel);//forecast move distance
							double fpull_length=Computation.EuDi(f_hotel,final_dep);//forecast end distance
							if (fpush_length <= next_push_radius&&  fpull_length<= next_pull_radius) {//feasible intermediate exists
								set.add(s_hotel);
								break;
							}
						}
					}else{
						for (Hotel f_hotel : h_pop) {//forecast and selection hotel may not carry the same value
							double fpush_length=Computation.EuDi(s_hotel, f_hotel);//forecast move distance
							double fpull_length=Computation.EuDi(f_hotel,final_dep);//forecast end distance
							if (fpush_length <= next_push_radius&&  fpull_length<= next_pull_radius&& s_hotel.getNode_id() != f_hotel.getNode_id()) {//feasible intermediate exists
								set.add(s_hotel);
								break;
							}
						}
					}
					
				}
			}
		} else {// last trip
			double push_radius = id_constraints.get(trip_id);
			if (Computation.EuDi(this, final_dep) <= push_radius) {
				set.add(final_dep);
			}
		}
		return set;
	}
	
	/**
	 * Same implementation with a slight difference that start and finish hotel may consider
	 * themselves as visitors in an iteration (s_hotel).Notice that in the first and second 
	 * if we do not check about the node calling the method and the 
	 * selection hotel to be equal.
	 * @param trip_id
	 * @return
	 */
	public  ArrayList<Hotel> getHotelVisitorsUnderRadiusForDepots(int trip_id) {
		ArrayList<Hotel> set = new ArrayList<Hotel>();
		ArrayList<Hotel> h_pop =(ArrayList<Hotel>) Hotel.getHotel_population().clone();	
		Hotel final_dep=Hotel.getFinalDepot();
		TreeMap<Integer,Double> id_constraints=new TreeMap<Integer,Double>(Trip.getTripids_TimeConstraints());
		int no_trips=Trip.getNumber_of_trips();
		double Tmax=Tour.gettour_length_budget();
		if (trip_id < no_trips - 1) {//until pre-last trip
			double push_radius = id_constraints.get(trip_id);
			double pull_radius = Tmax - Trip.SumUpConstraints2(trip_id);
			double next_push_radius = id_constraints.get(trip_id + 1);
			double next_pull_radius = Tmax-Trip.SumUpConstraints2(trip_id + 1);
			for (Hotel s_hotel : h_pop) {// select hotel
				double push_length=Computation.EuDi(this, s_hotel);//move distance
				double pull_length=Computation.EuDi(s_hotel,final_dep);//end distance
				if ( push_length<= push_radius&&pull_length<= pull_radius) {
					for (Hotel f_hotel : h_pop) {// forecast start
						double fpush_length=Computation.EuDi(s_hotel, f_hotel);//forecast move distance
						double fpull_length=Computation.EuDi(f_hotel,final_dep);//forecast end distance
						if (fpush_length <= next_push_radius&&fpull_length<= next_pull_radius) {//feasible intermediate exists
							set.add(s_hotel);
							break;
						}
					}
				}
			}
		} else {// last trip
			double push_radius = id_constraints.get(trip_id);
			if (Computation.EuDi(this, final_dep) <= push_radius) {
				set.add(final_dep);
			}
		}
		return set;
	}

	
	

	
	public static Node CreateMidNode(Hotel start,Hotel finish){
		double xcor=(start.getX_cord()+finish.getX_cord())/2D;
		double ycor=(start.getY_cord()+finish.getY_cord())/2D;
		return new Node(xcor,ycor);
		
	}

	public ArrayList<POI> collect(double limit,Node start,Node end){
		ArrayList<POI> feasible=new ArrayList<POI>();
		for(POI poi : POI.getPoi_population()){
			double distance_to=Computation.EuDi(start, poi);
			double distance_end=Computation.EuDi(poi,end);
			if(distance_to+distance_end<=limit){
				feasible.add(poi);
			}
		}
		return feasible;
	}

	
}
