package OPHSAttributes;


public class HotelPoiSEdge extends Edge implements Comparable<HotelPoiSEdge>{
	
	
	public HotelPoiSEdge(Hotel from, POI to) {
		super(from,to);
		this.weight = ((double)to.getScore())/this.distance;
	}


	public double getCovered() {
		return covered;
	}
	
	public boolean isFull(){
		if(covered==distance){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isOverfilled(){
		if(covered>distance){
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

	public void setCovered(double covered) {
		this.covered = covered;
	}


	@Override
	public int compareTo(HotelPoiSEdge o) {
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
