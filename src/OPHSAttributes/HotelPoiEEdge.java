package OPHSAttributes;

import java.util.ArrayList;


public class HotelPoiEEdge extends Edge implements Comparable<HotelPoiEEdge>{
	public static final ArrayList<PoiPoiEdge> edges=new ArrayList<PoiPoiEdge>();
	
	
	public HotelPoiEEdge(POI from, Hotel to) {
		super(from,to);
		this.weight =0;
	}
	
	public boolean isFull(){
		if(covered==distance){
			return true;
		}else{
			return false;
		}
	}
	public double getRemainder(){
		double diff=covered-distance;
		covered=distance;
		return diff;
	}

	public double getCovered() {
		return covered;
	}

	public void setCovered(double covered) {
		this.covered = covered;
	}

	@Override
	public int compareTo(HotelPoiEEdge o) {
		// TODO Auto-generated method stub
		if(this.weight>o.weight){
			return -1;
		}else if(this.weight<o.weight){
			return 1;
		}else{
			return 0;
		}
	}
	
	
}
