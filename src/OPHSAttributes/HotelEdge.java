package OPHSAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class HotelEdge extends Edge implements Comparable<HotelEdge>{

	public  double weight;
	public final int trip_id;
	private static final HashMap<Hotel,ArrayList<HotelEdge>> dictionary=new HashMap<Hotel,ArrayList<HotelEdge>>();
	public static final TreeMap<Double,HotelEdge> edges_sorted=new TreeMap<Double,HotelEdge>();
	public static final ArrayList<HotelEdge> edges=new ArrayList<HotelEdge>();
	
	public HotelEdge(Hotel from, Hotel to,int trip_id){
		super(from,to);
		this.weight=0;
		this.trip_id=trip_id;
		if(dictionary.get(from)==null){
			dictionary.put(from, new ArrayList<HotelEdge>());	
		}
		dictionary.get(from).add(this);
		edges.add(this);
	}
	
	
	public void calculateWeight(HashMap<Hotel,Integer> values){
			double sum=(double)(values.get(start)+values.get(end));
			double limit=Trip.getLength_limitById(trip_id);
			double diff=limit-distance;
			this.weight=0.5*sum+0.5*diff;
			//calculate
		
	}

	public static HotelEdge getEdge(Hotel start,Hotel end){
		HotelEdge returnedge=null;
		for(HotelEdge edge : dictionary.get(start)){
			if(edge.end.equals(end)){
				returnedge=edge;
			}
		}
		return returnedge;
	}

	@Override
	public int compareTo(HotelEdge o) {
		// TODO Auto-generated method stub
		if(this.weight>o.weight){
			return -1;
		}else if(this.weight<o.weight){
			return 1;
		}else{
			return 0;
		}
	}


	@Override
	public String toString() {
		return "HotelEdge [from=" + start.node_id + ", to=" + end.node_id + ", weight=" + weight + ", distance=" + distance + ", trip_id="
				+ trip_id + "]";
	}
	
	
	
}
