
package Computabillity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

import Application.App;
import OPHSAttributes.Node;
import OPHSAttributes.Hotel;
import OPHSAttributes.POI;
import OPHSAttributes.Trip;



public class Computation {

public static double roundAndPrepare(double t, int precision){
	t=Computation.accurateRound(t,precision);
	t=t+0.099999999;
	return t;
}

//implements the Euclideian Distance
public static double EuDi(Node from,Node to){
	
	double x_diff=Math.pow((from.getX_cord()-to.getX_cord()), 2);
	double y_diff=Math.pow((from.getY_cord()-to.getY_cord()), 2);
	double result=Math.sqrt(x_diff+y_diff);
	return result;//distance
}

//calculates an estimate on the score between two hotels
public static int estimate(Hotel start,Hotel end,double limit){
	double radius=Math.max(Computation.EuDi(start,end),limit-Computation.EuDi(start,end))/2;
	Node x=new Node((start.getX_cord()+end.getX_cord())/2.0,(start.getY_cord()+end.getY_cord())/2.0);//create median
	int score=0;
	for(POI p : POI.getPoi_population()){
		if(Computation.EuDi(p,x)<radius){//add hotels within the circle
			score=score+p.getScore();
		}
	}
	return score;
}



//splits the whole number of trips to D/2 sub tours of size two each
public static ArrayList<Integer>  split(){
	ArrayList<Integer> trip_ids=new ArrayList<Integer>();
	int trips=Trip.getNumber_of_trips();
	if((trips % 2)==0){
		for(int i=0; i<Trip.getNumber_of_trips(); i=i+2){
			trip_ids.add(i);
		}
	}else{
		for(int i=1; i<Trip.getNumber_of_trips(); i=i+2){
			trip_ids.add(i);
		}
	}
	return trip_ids;
}



//calculates the distance of a partial path
public static double partialPathLength(ArrayList<Node> list){
	double length=0;
	for(int j=0; j<list.size()-1; j++){
		length=length+Computation.EuDi(list.get(j), list.get(j+1));
	}
	return length;
}

//calc score of a partial path
public static int CalcScore(ArrayList<Node> list){
	int score=0;
	for(Node e : list){
		if(e instanceof POI){
			score=score+((POI)e).getScore();
		}
	}
	return score;
}

//calculates the par
public static double partialPathLengthP(ArrayList<POI> list){
	double length=0;
	for(int j=0; j<list.size()-1; j++){
		length=length+Computation.EuDi(list.get(j), list.get(j+1));
	}
	return length;
}
//rounds the euclideian distance. Used for the OP with integer weights.
public static double EuDiRound(Node from,Node to){
	double x_diff=Math.pow((from.getX_cord()-to.getX_cord()), 2);
	double y_diff=Math.pow((from.getY_cord()-to.getY_cord()), 2);
	double result=Math.sqrt(x_diff+y_diff);
	return round(result,0);
}

//returns the rounded distance as an integer
public static int EuDiRoundInt(Node from,Node to){
	
	double x_diff=Math.pow((from.getX_cord()-to.getX_cord()), 2);
	double y_diff=Math.pow((from.getY_cord()-to.getY_cord()), 2);
	double result=Math.sqrt(x_diff+y_diff);
	return (int)round(result,0);
}

//truncates the result up to precision places
public static double accurateRound(double x,int precision){
	String string_value=new String(Double.toString(x));
	int index=string_value.indexOf(".");
	string_value=string_value.substring(index,index+precision);
	double v=((int)x)+Double.parseDouble(string_value);
	
	return v;
}

//chechks if a partial path is feasible under a certain time limit
public static boolean checkFeasibility(ArrayList<POI> seq, Double limit){
	boolean feasible=false;
	double distance=0;
	Hotel start=Hotel.getStartDepot();
	Hotel end=Hotel.getFinalDepot();
	distance=distance+EuDi(start,seq.get(0));
	double distroud=0;
	distroud=EuDiRound(start,seq.get(0));
	
	
	for(int j=1; j<=seq.size()-1; j++){
		distroud=distroud+EuDiRound(seq.get(j-1),seq.get(j));
		distance=distance+EuDi(seq.get(j-1),seq.get(j));
		
	}
	
	distance=distance+EuDi(seq.get(seq.size()-1),end);
	
	distroud=distroud+EuDiRound(seq.get(seq.size()-1),end);
	if(distroud<=limit){
		feasible=true;
	}
	System.out.println(distance+" "+(distroud));
	System.out.println(limit);
	return feasible;
}

//get the gap between the rounded and real distance used on the OP for integers
public static double getDiference(Node start,Node end){
	double dif=EuDi(start,end)-EuDiRound(start,end);
	return dif;
	
}
public static String ListToString(ArrayList<POI> list){
	String x="";
	for(POI p  : list){
		if(p!=null){
			if(x.equals("")){
				x=Integer.toString(p.node_id);
			}else{
				x=x+" "+Integer.toString(p.node_id);
			}
		}
		
	}
	return x;
	
}

//reverses a partial path. Used in sequential and partial sequential strategies
public static ArrayList<POI> reverse(ArrayList<POI> list){
	ArrayList<POI> rev=new ArrayList<POI>();
	Stack<POI> stack=new Stack<POI>();
	for(POI p : list){
		stack.add(p);//stack mantains the order LIFO
	}
	while(!stack.isEmpty()){
		rev.add(stack.pop());//add in reverse sequence
	}
	return rev;
}
/**
 * Outputs the list as a string
 * @param list
 * @return x the string representing the list
 */
public static String ListToStringNodes(ArrayList<Node> list){
	String x="";
	for(Node p  : list){
		if(p!=null){
			if(x.equals("")){
				x=Integer.toString(p.node_id);
			}else{
				x=x+" "+Integer.toString(p.node_id);
			}
		}
		
	}
	return x;
	
}
/**
 * Outputs the list as a string, list only contains hotels
 * @param list
 * @return x the string representing the list
 */
public static String ListToStringHotels(ArrayList<Hotel> list){
	String x="";
	for(Node p  : list){
		if(p!=null){
			if(x.equals("")){
				x=Integer.toString(p.node_id);
			}else{
				x=x+" "+Integer.toString(p.node_id);
			}
		}
		
	}
	return x;
	
}
/**
 * Rounds the distance up if decimal > 0.5 and down otherwise
 * @param list
 * @return d the rounded distance
 */
public static double round(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();

    long factor = (long) Math.pow(10, places);
    value = value * factor;
    long tmp = Math.round(value);
    return (double) tmp / factor;
}

public static int getMaxTripLimit(Hotel start, Hotel end){
	double max_limit=Double.MIN_VALUE;
	int max_index=-1;
	for(int i=0; i<Trip.getNumber_of_trips(); i++){
		if(start.visitors_limits.get(i)!=null){
			if(start.visitors_limits.get(i).contains(end)){
				if(Trip.getLength_limitById(i)>max_limit){
					max_limit=Trip.getLength_limitById(i);
					max_index=i;
				}
			}
		}
	}
	return max_index;
}

/**
 * Creates all On^2 pairs of hotels. In actuality we dont look trips a-b and b-a cause
 * we check if b belongs to the neighbours set of a for i  and vice versa. If this holds
 * we can solve trip a-b and obtain b-a by reversing the tour. In practice, total number of pairs
 * ( n (n-1 ))/2
 */
public static void Hotels(){
	ArrayList<Hotel> forbidden=new ArrayList<Hotel>();
	for(Hotel start : Hotel.getHotel_population()){
		if(start.node_id!=0&&start.node_id!=1){
			for(Hotel end : start.k_visitors){
				int max_trip=-1;
				if(end.node_id!=0&&!forbidden.contains(end)){
					if(end.node_id!=start.node_id){
						max_trip=Hotel.getMaxTripLimitBetween(start,end);
						if(max_trip!=-1){
							App.SingeOPFast(start,end,max_trip);
						}
					}
				}
			}
			forbidden.add(start);
		}
		
	}
}

/**
 * Performs a check for one char, to check if it is numerical or not.
 * 
 * @param onechar
 * @return true if the char is a number, a dot or a dash, false otherwise.
 */
public static boolean isNumerical(String onechar) {
	if (onechar.equals(".") || onechar.equals("-")) {
		return true;
	}
	try {
		Integer.parseInt(onechar);
		return true;
	} catch (Exception e) {
		return false;
	}
}

public static void writeHoodsTxt(){
	 BufferedWriter writer = null;
        try {
            //create a temporary file
            
            String name=App.TEMP.substring(App.TEMP.length()-1-12,App.TEMP.length()-5);
            // This will output the full path where the file will be written to...
           String path="C:\\Users\\aggelos\\Desktop\\Aggelos\\JavaWorkspace\\OPHS\\Hoods";
           File newFile = new File(path, name+".txt");
           
            writer = new BufferedWriter(new FileWriter(newFile));
            for(POI p1 : POI.getPoi_population()){
            	writer.newLine(); 
            	 for (Map.Entry<Integer,ArrayList<POI>> entry : p1.visitors.entrySet()) {
 	    			String x="";
 	    			for(POI p : entry.getValue()){
 	    				x=x+"-"+Integer.toString(p.node_id);
 	    			}
 	    			writer.write("POI: "+p1.node_id+" Key: " + entry.getKey() + " Value: " + x);
 	    			writer.newLine(); 
            	 }
            }
            writer.write("Hello world!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
}




}
