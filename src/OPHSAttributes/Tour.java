package OPHSAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import DataStructures.HotelSequence;


public class Tour {
	
	//class fields
	private static  double tour_length_budget;
	
	//instance fields
	private ArrayList<Trip> trips;
	private int tour_score;
	private double tour_length;
	

	public Tour() {
		this.trips = new ArrayList<Trip>();
		this.tour_score = 0;
		this.tour_length = 0;
	}
	//constructor for Tour given a finite hotel sequence used in MetaHeuristic VNS implementation
	public Tour(ArrayList<Hotel> sequence){
		this.trips = new ArrayList<Trip>();
		this.tour_score = 0;
		this.tour_length = 0;
		int number_of_trips=Trip.getNumber_of_trips();
		for(int i=0; i<number_of_trips; i++){
			Hotel start=sequence.get(i);
			Hotel end=sequence.get(i+1);
			Trip temp=new Trip(start,end,i);
			this.trips.add(temp);
		}
	}
	
	public Tour(HotelSequence sequence){
		this.trips=new ArrayList<Trip>();
		this.tour_score=0;
		this.tour_length=0;
		for(int i=0; i<Trip.getNumber_of_trips(); i++){
			Trip trip=new Trip(sequence.getSequence().get(i), sequence.getSequence().get(i+1), i);
			this.trips.add(trip);
		}
	}
	//setters and getters 

	public int getNumberOfTrips(){
		if(this.trips.isEmpty()){
			return 0;
		}else{
			return this.trips.size();
		}
	}
	


	public ArrayList<Trip> getTrips() {
		return trips;
	}
	public static double gettour_length_budget() {
		return tour_length_budget;
	}
	@Override
	public String toString() {
		int occ=0;
		ArrayList<Trip> infeasibles=new ArrayList<Trip>();
		for(Trip runner:this.trips){
			if(runner.isTripFeasible()){
				occ++;
				System.out.println(runner.toString());
			}else{
				infeasibles.add(runner);
			}
		}
		if(occ==Trip.getNumber_of_trips()){
			System.out.println("TOUR IS FEASIBLE!");
		}else{
			System.out.println("This is the infeasibles tours : ");
			for(Trip run:infeasibles){
				String cause="";
				if(run.getLength()>run.getLength_limit()){
					cause=" Due to length constraint";
				}
				 if(run.getId()==Trip.getNumber_of_trips()-1){
					if(run.getEnd()!=Hotel.getFinalDepot()){
						cause=cause+"Due to wrong final depot";
					}
				}
				 if(run.getLength_limit()==0.0){
					cause=cause+"Due to no available move constraint";
				}
				
				System.out.println(run.toString()+cause);
			}
			System.out.println("*********************TOUR IS INFEASIBLE!************************");
		}
		return  "[tour_score=" + tour_score
				+ ", tour_length=" + tour_length +"tour limit="+Tour.tour_length_budget+" POIS visited : "+Integer.toString(this.calcPOIVisited())+"]";
	}

	public String SolutionToString(){
		String tour_perm="";
		for(Trip trip : this.trips){
			String trip_permut="";
			for(Node e:trip.getPermutation()){
				if(e instanceof POI){
					trip_permut=trip_permut+"-"+Integer.toString(e.node_id)+"-";
				}
				else{
					trip_permut=trip_permut+"<"+Integer.toString(e.node_id)+">";
				}
			}
			if(tour_perm.equals("")){
				tour_perm=trip_permut;
			}else{
				tour_perm=tour_perm+", "+trip_permut;
			}
		}
		return tour_perm;
	}
	
	public void PrintTour(){
		
			for(Trip trip : trips){
				System.out.println(trip.toString());
			}
		
		System.out.println(this.getTour_score());
	}
	
	public static void settour_length_budget(double tour_length_budget) {
		Tour.tour_length_budget = tour_length_budget;
	}
	public void setTrips(ArrayList<Trip> trips) {
		this.trips = trips;
	}
	
	public Trip getTrip(int trip_id){
		Trip t=null;
		for(Trip trip : this.getTrips()){
			if(trip.getId()==trip_id){
				t= trip;
				break;
			}
		}
		return t;
	}

	public void addTriptoTour(Trip trip){
		this.trips.add(trip);
	}
	
	public int getTour_score() {
		return tour_score;
	}
	public void setTour_score(int tour_score) {
		this.tour_score = tour_score;
	}
	public double getTour_length() {
		return tour_length;
	}
	public ArrayList<Hotel> getHotelSequenceTour(){
		ArrayList<Hotel> h_sequence=new ArrayList<Hotel>();
		h_sequence.add(Hotel.getStartDepot());
		for(Trip runner:this.trips){
			if(runner.getId()<Trip.getNumber_of_trips()-1){
				Hotel end_add=runner.getEnd();
				h_sequence.add(end_add);
			}
		}
		
		h_sequence.add(Hotel.getFinalDepot());
	
		return h_sequence;
	}
	public void setTour_length(double tour_length) {
		this.tour_length = tour_length;
	}
	public Trip getLastTrip(){
		if(this.trips.isEmpty()){
			return null;
		}else{
			Trip last_trip=this.trips.get(this.trips.size()-1);
			return last_trip;
		}
	}
	public int calcPOIVisited(){
		int total_poi=0;
		for(Trip runner:this.trips){
			total_poi=total_poi+runner.calcNumOfPOI();
		}
		return total_poi;
	}
	public int calcTourScore(){
		int total_score=0;
		if(this.trips.isEmpty()){
			this.tour_score=0;
			return 0;
		}else{
			for(Trip e:this.trips){
				total_score=total_score+e.CalcTripScore();
			}
			this.tour_score=total_score;
			return total_score;
		}
	}
	
public double calcTourLength(){
		double total_length=0;
		if(this.trips.isEmpty()){
			this.tour_length=0;
			return 0;
		}else{
			for(Trip e:this.trips){
				total_length=total_length+e.CalcTripLength();
			}
			this.tour_length=total_length;
			return total_length;
		}
	}
/**
 * Checks whether a Tour is feasible or Not	
 * @return true if the tours is feasible false otherwise
 */

	public boolean isfeasible(){
		boolean feasible=true;
		for(Trip t : this.getTrips()){
			if(!t.isTripFeasible()){
				feasible=false;
			}
		}
		if(feasible){
			HashMap<Trip,ArrayList<POI>> m=new HashMap<Trip,ArrayList<POI>>();
			HashMap<POI,Boolean> map=new HashMap<POI,Boolean>();
			for(POI p : POI.getPoi_population()){
				map.put(p,false);
			}
			for(Trip t : this.getTrips()){
				m.put(t,new ArrayList<POI>());
				for(Node node : t.getPermutation()){
					if(node instanceof POI){
						if(map.get((POI)node)){
							m.get(t).add((POI)node);
						}else{
							map.put((POI)node, true);
						}
						
					}
				}
			}
			for(Trip t : this.getTrips()){
				if(m.get(t)!=null){
					for(POI p : m.get(t)){
						t.getPermutation().remove(p);
					}
				}
			}
			
		}
		return feasible;
		
	}
	


	public ArrayList<POI> NodesOfTour(){
		ArrayList<POI> nodes=new ArrayList<POI>();
		for(Trip trip : this.getTrips()){
			for(Node node : trip.getPermutation()){
				if(node instanceof POI){
					nodes.add((POI)node);
				}
			}
		}
		return nodes;
	}

	
	//end setters and getters
}
