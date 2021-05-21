package OPHSAttributes;

import Computabillity.Computation;

public class TripTriplet implements Comparable<TripTriplet> {
	public final int trip_id;
	public final Hotel first;
	public final Hotel second;
	public final Hotel third;
	private Trip f_trip;
	private Trip s_trip;
	private int score;
	
	public TripTriplet(int trip_id, Hotel first, Hotel second,Hotel third) {
		super();
		this.trip_id = trip_id;
		this.first = first;
		this.second = second;
		this.third=third;
		this.score=first.getScore()+second.getScore();
		this.f_trip=null;
		this.s_trip=null;
	}	
	
	public void print(){
		System.out.println(Computation.ListToStringNodes(f_trip.getPermutation()));
		System.out.println(Computation.ListToStringNodes(s_trip.getPermutation()));
	}
	
	public Trip getF_trip() {
		return f_trip;
	}
	
	

	public void setF_trip(Trip f_trip) {
		this.f_trip = f_trip;
	}
	
	public void computeScore(){
		int s=this.f_trip.CalcTripScore();
		this.score=s+this.s_trip.CalcTripScore();
	}

	public Trip getS_trip() {
		return s_trip;
	}

	public void setS_trip(Trip s_trip) {
		this.s_trip = s_trip;
	}

	public void printTriplet(){
		System.out.println("First: "+first.node_id+" Second: "+second.node_id+" Third:"+third.node_id);
	}



	@Override
	public int compareTo(TripTriplet other) {
		if (score < other.score)
			return 1;
		else if (score > other.score)
			return -1;
		else
			return 0;
		
	}


	

}
