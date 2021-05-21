package Application;

import java.util.ArrayList;
import java.util.TreeMap;

import Computabillity.Computation;
import OPHSAttributes.Edge;
import OPHSAttributes.Hotel;
import OPHSAttributes.Node;
import OPHSAttributes.OPHSGraph;
import OPHSAttributes.POI;
import OPHSAttributes.Tour;
import OPHSAttributes.Trip;



public class Instance {
	

	
	//instance fields
	private final  int number_of_trips;//number of trips allowed
	private final int number_of_poi;//number of points of interest
	private  final int number_of_extrahotels;//number of extra hotels
	private  final double tour_constraint;//Tmax
	private final ArrayList<Double> trips_constraints;//d time constraints
	private final ArrayList<Double> cord_scor;//cordinates for nodes plus score 
	


	public Instance(ArrayList<Double> data){
		this.number_of_poi=data.get(0).intValue()-2;
		this.number_of_extrahotels=data.get(1).intValue();
		this.number_of_trips=data.get(2).intValue();
		this.tour_constraint=data.get(3).intValue();
		this.trips_constraints=new ArrayList<Double>(data.subList(4, 4+number_of_trips));
		this.cord_scor=new ArrayList<Double>(data.subList(4+number_of_trips,data.size()));
		this.CreateInstance();
	}

	
	/**
	 Separates hotel cordinates and score from all nodes of graph.Each hotel is assigned with with 3 values, x,y cordinates and zero score.
	 @return h_data : A list containing 3*h double/integer numbers.
	 */
	public ArrayList<Double> SeparateHotelData(){
		ArrayList<Double> h_data=new ArrayList<Double>();
		int total_hotels=this.number_of_extrahotels+2;
		for(int j=0; j<total_hotels*3; j++){
			h_data.add(this.cord_scor.get(j));
		}
		return h_data;
	}
	
	/**
	 Separates poi cordinates and score from all nodes of graph.Each poi is assigned with 3 values, x,y cordinates and non-zero score.
	 @return p_data : A list containing 3*p double/integer numbers.
	 */
	public ArrayList<Double> SeparatePOIData(){
		ArrayList<Double> p_data=new ArrayList<Double>();
		int total_hotels=this.number_of_extrahotels+2;
		for(int j=total_hotels*3; j<this.cord_scor.size(); j++){
			p_data.add(this.cord_scor.get(j));
		}
		return p_data;
	}
	
	/**
	 *This method created the current instance of the problem modified by the file given.
	 At start each static field of each class is computed.
	 Then hotel population is created.Hotels unique id's are represented by number from 0 to h+1, 
	 where h is the number of extra hotels.Start and final depot are represented by 0 and 1 respectively. 
	 After this, POI population is created.POI unique id's are represented by numbers from h+2 to h+n-1
	 In each population creation an integer variable is computed saving the upper bound of ids.
	 @variable index_counter: This variable indicates the index on list about x and y coordinates for each node.
		It is increased by 3(x,y,score) each time an instance is created.Hotel's score is zero so it is handled by the constructor of the class
	
	 */
	public void CreateInstance(){
		ArrayList<Double> hotels=this.SeparateHotelData();
		ArrayList<Double> pois=this.SeparatePOIData();
		CreateHotels(hotels);
		CreatePOIS(pois);
		initializefirstStructure();
		CreateTripConstraints();
		AssignTourLimit();
		AssignNoOfTrips();
		//TestInstanceIntegration();
		
	}
	 
	
	
	
	
	
	/**
	 Creates h hotels nodes.
	 @param hotel data : The list containing cordinates and score for the h hotels.
	 */
	public void CreateHotels(ArrayList<Double> hotel_data){
		int hotel_id=0;
		for(int j=0; j<hotel_data.size(); j=j+3){
			new Hotel(hotel_data.get(j),hotel_data.get(j+1),hotel_id);
			hotel_id++;
		}
		if(hotel_id!=this.number_of_extrahotels+2){
			System.err.println("Error while creating hotels!");
		}else{
			//System.out.println("Successfull hotel creation");
		}
	}
	
	public void initializefirstStructure(){
		for(Node node : Node.getTotal_population()){
			node.getNeighbourDistance();
		}
	}
	
	/**
	 Creates n*n edges.
	 */
	
	
	public void CreateGraph(){
		new OPHSGraph(Node.getTotal_population(),Edge.getTotal_edges());
	}
	
	/**
	 Creates p poi nodes.
	 @param poi data : The list containing cordinates and score for the p pois.
	 */
	public void CreatePOIS(ArrayList<Double> poi_data){
		int p_id=this.number_of_extrahotels+2;
		int p_counter=0;
		for(int j=0; j<poi_data.size(); j=j+3){
			new POI(poi_data.get(j),poi_data.get(j+1),p_id,poi_data.get(j+2).intValue());
			p_id++;
			p_counter++;
		}
		if(p_counter!=this.number_of_poi){
			System.err.println("Error while creating pois!");
		}else{
			//System.out.println("Successfull poi creation");
		}
		
	}

	/**
	 Creates d limits, each one assigned with a unique id representing its order on tour..
	 */
	public void CreateTripConstraints(){
		TreeMap<Integer,Double> id_limits=new TreeMap<Integer,Double>();
		for(int i=0; i<this.number_of_trips; i++){
			id_limits.put(i,trips_constraints.get(i));
		}
		Trip.setTripids_TimeConstraints(id_limits);
	}
	
	/**
	 Assing tour available length
	 */
	public void AssignTourLimit(){
		Tour.settour_length_budget(this.tour_constraint);
	}

	public void AssignNoOfTrips(){
		Trip.setNumber_of_trips(this.number_of_trips);
	}
	public void TestInstanceIntegration(){
		ArrayList<Node> node_population=new ArrayList<Node>(Node.getTotal_population());
		for(Node n : node_population){
			if(n instanceof Hotel){
				System.out.println("Id : "+n.getNode_id()+" x =  "+n.getX_cord()+" y = "+n.getY_cord());
			}else{
				System.out.println("Id : "+n.getNode_id()+" x =  "+n.getX_cord()+" y = "+n.getY_cord()+" score = "+((POI)n).getScore());
			}
		}
		int hotels_created=Hotel.getHotel_population().size();
		int pois_created=POI.getPoi_population().size();
		if(hotels_created==this.number_of_extrahotels+2 && pois_created==this.number_of_poi){
			System.out.println("Hotels and POIS created sucessfully!");
		}
		
	}


	/**
	 * Prints all the data of the problem.
	 * For all nodes its coordinates.
	 */
	public static void printData(){
		ArrayList<Node> h=new ArrayList<Node>();
		for(Node n : Node.getTotal_population()){
			h.add(n);
			for(Node x: Node.getTotal_population()){
				if(!h.contains(x)){
					System.out.println(n.node_id+" "+x.node_id+" D: "+Computation.EuDiRound(n,x)+" DR: "+Computation.EuDi(n,x));
				
				}
			}
		}
		for(POI p : POI.getPoi_population()){
			System.out.println(p.node_id+" "+p.getScore());
			
		}
	}
	
}
