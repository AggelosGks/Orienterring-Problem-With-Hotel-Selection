package OPHSAttributes;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import Computabillity.Computation;


public class Trip {
	
	//static fields
	private static int number_of_trips;
	private  static TreeMap<Integer,Double> tripids_TimeConstraints;//a data structure saving the time budget for a trip given its id


	//instance fields
	private int id;
	private double trip_length;
	private int trip_score;
	private ArrayList<Node> permutation;
	private Hotel start_hotel;
	private Hotel end_hotel;
	private double length_limit;
	
	
	public Trip(Hotel start,int id){

		this.id=id;
		this.permutation=new ArrayList<Node>();
		this.permutation.add(start);
		this.start_hotel=start;
		this.end_hotel=null;
		this.trip_length=0;
		this.trip_score=0;
		this.length_limit=tripids_TimeConstraints.get(this.id);
		
	}
	//constructor for Trip with know start-end hotel, used in MetaHeuristic VNS implementation
	public Trip(Hotel start,Hotel end,int id){
		this.id=id;
		this.permutation=new ArrayList<Node>();
		this.permutation.add(start);
		this.permutation.add(end);
		this.start_hotel=start;
		this.end_hotel=end;
		this.trip_length=0;
		this.trip_score=0;
		this.length_limit=tripids_TimeConstraints.get(this.id);
	}
	public Trip(Hotel start,Hotel end,int id,ArrayList<POI> sequence){
		this.id=id;
		this.permutation=new ArrayList<Node>(sequence);
		this.permutation.add(0, start);
		this.permutation.add(end);
		this.start_hotel=start;
		this.end_hotel=end;
		this.trip_length=CalcTripLength();
		this.trip_score=CalcTripScore();
		this.length_limit=tripids_TimeConstraints.get(this.id);
	}
	
	//getters and setters


	public double getLength_limit() {
		return length_limit;
	}
	//use it if current trip is not constructed yet and forecast needed,notice that this is a static method,may be called if no instances have be
	public static double getLength_limitById(int id){
		double limit=tripids_TimeConstraints.get(id);
		return limit;
	}
	
	public void addNodetoPermut(Node e){
		this.permutation.add(e);
	}

	public void setLength_limit(double length_limit) {
		this.length_limit = length_limit;
	}

	public static int getNumber_of_trips() {
		return number_of_trips;
	}

	public static void setNumber_of_trips(int number_of_trips) {
		Trip.number_of_trips = number_of_trips;
	}

	public static TreeMap<Integer, Double> getTripids_TimeConstraints() {
		return tripids_TimeConstraints;
	}
	
	

	public static void setTripids_TimeConstraints(TreeMap<Integer, Double> tripids_TimeConstraints) {
		Trip.tripids_TimeConstraints = tripids_TimeConstraints;
	}
//getters and setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getLength() {
		
		return trip_length;
	}

	public void setLength(int length) {
		this.trip_length = length;
	}

	public int getScore() {
		return trip_score;
	}

	public void setScore(int score) {
		this.trip_score = score;
	}

	public ArrayList<Node> getPermutation() {
		return permutation;
	}

	public void setPermutation(ArrayList<Node> permutation) {
		this.permutation = permutation;
	}

	public Hotel getStart() {
		return start_hotel;
	}

	public void setStart(Hotel start) {
		this.start_hotel = start;
	}

	public Hotel getEnd() {
		return end_hotel;
	}

	public void setEnd(Hotel end) {
		this.end_hotel = end;
	}

	//end getters and setters
	public Hotel GetTripStart(){
		Hotel start=null;
		System.out.println("FDFDFDFDFDF");
		for(Node e:this.permutation){
			
			if(e instanceof Hotel){
				start=(Hotel) e;
				break;
			}
		}
		return start;
	}
	public Hotel GetTripEnd(){
		if(this.permutation.get(permutation.size()-1) instanceof Hotel){
			return (Hotel)permutation.get(permutation.size()-1);
		}
		else{
			System.err.println("Error on permutation about end hotel");
			return null;
		}
	}
	public double CalcTripLength(){
		if(this.permutation.isEmpty()){
			this.trip_length=0;
			return trip_length;
		}else{
			double total_dist=0;
			for(int i=0; i<this.permutation.size()-1;i++){
				Node from=this.permutation.get(i);
				Node to=this.permutation.get(i+1);
				total_dist=total_dist+Computation.EuDi(from, to);
			}
			this.trip_length=total_dist;
			return trip_length;
		}
		
	}
	
	public int CalcTripScore(){
		int total_score=0;
		if(this.permutation.isEmpty()){
			return 0;
		}else{
			for(Node runner : permutation){
				if(runner instanceof POI){
					total_score=total_score+((POI) runner).getScore();
				}
			}
			this.trip_score=total_score;
			return total_score;
		}
	
	}
	public double getTripTimeBudget(){
		return tripids_TimeConstraints.get(this.id);
	}
	public int calcNumOfPOI(){
		return (this.permutation.size()-2);
	}
	public boolean isTripFeasible(){
		if(this.id==number_of_trips-1){//if trip is last one
			if(this.GetTripEnd()==Hotel.getFinalDepot()){//if not ends with last depot then not feasible
				System.out.println(this.length_limit+" "+this.trip_length);
				if(this.length_limit>=Computation.accurateRound(this.trip_length,2)){
					
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}else{
			if(this.length_limit>=Computation.accurateRound(this.trip_length,2)){
				return true;
			}else{
				return false;
			}
		}
		
	}
	@Override
	public String toString() {
		String permut="";
		for(Node e:this.permutation){
			if(e instanceof POI){
				permut=permut+"-"+Integer.toString(e.node_id)+"-";
			}
			else{
				permut=permut+"<"+Integer.toString(e.node_id)+">";
			}
		}
		
	
		return "Trip [id=" + id + ", length=" + trip_length
				+ ", score=" + trip_score +  ", length_limit=" + length_limit+ ", Path=" + permut + "]";
	}
	
	
	public static double SumUpConstraints(int trip_id){
		double length_consumed=0;
		for(Map.Entry<Integer,Double> entry : tripids_TimeConstraints.entrySet()) {
			if(entry.getKey()>trip_id){
				length_consumed=length_consumed+entry.getValue();
			}
		}
		return length_consumed;
	}
	public static double SumUpConstraints2(int trip_id){
		double length_consumed=0;
		for(Map.Entry<Integer,Double> entry : tripids_TimeConstraints.entrySet()) {
			if(entry.getKey()<=trip_id){
				length_consumed=length_consumed+entry.getValue();
			}
		}
		return length_consumed;
	}
	

	
	public void assingNewPermutationOfPOIs(ArrayList<POI> poi_permutation){
		ArrayList<Node> new_permutation=new ArrayList<Node>();
		new_permutation.add(this.getStart());
		for(POI poi : poi_permutation){
			new_permutation.add(poi);
		}
		new_permutation.add(this.getEnd());
		this.setPermutation(new_permutation);
	}
	
}
