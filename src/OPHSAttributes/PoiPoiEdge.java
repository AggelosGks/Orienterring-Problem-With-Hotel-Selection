package OPHSAttributes;

public class PoiPoiEdge extends Edge implements Comparable<PoiPoiEdge>{
	
	
	public PoiPoiEdge(POI from, POI to) {
		super(from,to);
		this.weight = ((double)to.getScore())/this.distance;
		//from.getStart_other().add(this);//add the edge for start node as external
		//to.getOther_Finish().add(this);//add the edge for finish node as internal
	}
	
	@Override
	public String toString() {
		return "POIEdge [from=" + start.node_id + ", to=" + end.node_id + ", weight=" + weight + ", distance=" + distance + "]";
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


	public double getCovered() {
		return covered;
	}



	@Override
	public int compareTo(PoiPoiEdge o) {
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
