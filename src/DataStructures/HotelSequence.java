package DataStructures;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import Computabillity.Computation;
import OPHSAttributes.Hotel;
import OPHSAttributes.Trip;

public class HotelSequence {//implements the data structure of a hotel sequence
	
	
	private static final TreeMap<Integer,Double> trips_constraints=Trip.getTripids_TimeConstraints();
	private static final ArrayList<HotelSequence> total_feasible=new ArrayList<HotelSequence>();
	private final ArrayList<Hotel> sequence;
	
	
	/**
	 * Assigns the sequence to the private field, adds this sequence to the total population
	 * @return 
	 */
	public HotelSequence(ArrayList<Hotel> intermediate){
		this.sequence=new ArrayList<Hotel>();
		for(Hotel hotel : intermediate){
			this.sequence.add(hotel);
		}
		
		this.sequence.add(Hotel.getFinalDepot());
		//System.out.println(this.sequence.size());
		if(this.feasibleUnderTimeConstr()){
			total_feasible.add(this);
		}
	}
	/**
	 * Checks if a hotel sequence is feasible sequence for a tour.
	 * @return true if the sequence is feasible false otherwise
	 */
	public boolean feasibleUnderTimeConstr(){
		boolean feasible=true;
		
		for(Map.Entry<Integer,Double> entry : trips_constraints.entrySet()) {
			Integer trip_id = entry.getKey();
			Double limit = entry.getValue();
			Hotel start=this.sequence.get(trip_id);
			Hotel end=this.sequence.get(trip_id+1);
			double dist=Computation.EuDi(start, end);
			if(!(dist<=limit)){
				feasible=false;
				break;
			}
			
		}
		return feasible;
	}

	public static ArrayList<HotelSequence> getTotalFeasible() {
		return total_feasible;
	}

	public ArrayList<Hotel> getSequence() {
		return sequence;
	}
	
	
	
}
