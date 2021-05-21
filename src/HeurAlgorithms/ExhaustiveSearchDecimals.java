package HeurAlgorithms;


import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import Computabillity.Computation;
import OPHSAttributes.Hotel;
import OPHSAttributes.POI;



public class ExhaustiveSearchDecimals {
	private static  ArrayList<ArrayList<POI>> optimal= new ArrayList<ArrayList<POI>>();
	private static int optimal_score=0;
	private TreeMap<Double,ArrayList<POI>> start_visitors;
	private static TreeMap<Integer,ArrayList<ArrayList<POI>>> best_in_branch=new TreeMap<Integer,ArrayList<ArrayList<POI>>>();
	private final Hotel start;
	private final Hotel finish;
	private final double Td;
	public static TreeMap<Integer, ArrayList<ArrayList<POI>>> getBest_in_branch() {
		return best_in_branch;
	}
	
	private double time_needed;

	public ExhaustiveSearchDecimals(Hotel start, Hotel finish,double Td,boolean print){
		this.start_visitors=new TreeMap<Double,ArrayList<POI>>();
		this.start=start;
		this.finish=finish;
		this.Td=Td;
		Preprocess(Td);
		DFS(start,Td,print);
		//printData();
		optimal.clear();
		optimal_score=0;
		System.out.println("Exhaustive Search needed: "+time_needed+" seconds");
	}

	/**
	 * Clears the data structures used in the execution. Used when solving multiple instances.
	 */
	public void refresh(){
		optimal_score=0;
		optimal.clear();
		start_visitors.clear();
		best_in_branch.clear();
		best_in_branch=new TreeMap<Integer,ArrayList<ArrayList<POI>>>();
		for(POI p : POI.getPoi_population()){
			p.visitors.clear();
		}
	}
	
	/**
	 * Initializes branches to store the best solution found in each single op.
	 */
	private void initializeBranches(){
		for(POI p : POI.getPoi_population()){
			best_in_branch.put(p.node_id, new ArrayList<ArrayList<POI>>());
		}for(POI p : POI.getPoi_population()){
			if(best_in_branch.get(p.node_id)==null){
				System.out.println("YES");
			}
		}
	}
	
	/**
	 * Preprocesses and initializes all data structures that will be used during the execution.
	 * @param Td the time budget
	 */
	public  void Preprocess(double Td){
		for(POI p : POI.getPoi_population()){
			p.ComputeVisitorsIntegersDecimals(Td, finish);
		}
		
		start_visitors.put(Td,new ArrayList<POI>());
		for(POI p : POI.getPoi_population()){
			
			double to=Computation.EuDi(start,p);
			double end=Computation.EuDi(p,finish);
			if(to+end<=Td){
				start_visitors.get(Td).add(p);
					
			}
		}
		initializeBranches();
	}
	
	public void printbesties(){
		System.out.println("-------------------------------------------------------------------");
		System.out.println("Time asked: "+Td);
		for (Map.Entry<Integer,ArrayList<ArrayList<POI>>> entry : best_in_branch.entrySet()) {
			ArrayList<ArrayList<POI>> paths=entry.getValue();
			for(ArrayList<POI> list: paths){
				String x=Computation.ListToString(list);
				int score=CalcScore(list);
				 System.out.println(" Node : " + entry.getKey() + " Seq: " + x+" Score: "+score+" Length: "+this.getRealPathLength(list));
				 break;
			}
		}
		System.out.println("-------------------------------------------------------------------");
	}
	
	/**
	 * Implements a DFS traversal to the search tree
	 * @param start
	 * @param time_left
	 * @param print
	 */
	public void DFS(Hotel start,double time_left,boolean print){
		
		long s=System.nanoTime();
		ArrayList<POI> vis=start_visitors.get(time_left);
		
		
		Stack<POI> stack=new Stack<POI>();
//		POI p : vis 
		for(int i=vis.size()-1; i>=0; i--){
			stack.push(vis.get(i));
		}
		if(print){
			System.out.println("First level has: "+Computation.ListToString(vis)+" for time: "+time_left);
		}
		while(!stack.isEmpty()){
			ArrayList<POI> path=new ArrayList<POI>();
			POI p=stack.pop();
			if(print){
				System.out.println("*************OUT**************");
			}
			
			path.add(p);
			recursive(p,time_left-Computation.EuDi(start,p),path,print);
		}
		if(print){
			System.out.println("==========================");
		}
		
		for(ArrayList<POI> list: optimal){
			System.out.println(Computation.ListToString(list)+" Score: "+optimal_score);
			if(Computation.checkFeasibility(list, time_left)){
				System.out.println("Feasible");
			}
		}
		long f=System.nanoTime();
		long time=f-s;
		double seconds = (double)time / 1000000000.0;
		this.time_needed=seconds;
		System.out.println("Time needed: "+seconds);
		System.out.println("==========================");
		

		
	}

	
	public double getRealPathLength(ArrayList<POI> sequence){
		double length=Computation.EuDi(start, sequence.get(0));
		for(int j=1; j<=sequence.size()-1; j++){
			length=length+Computation.EuDi(sequence.get(j-1), sequence.get(j));
		}
		length=length+Computation.EuDi(sequence.get(sequence.size()-1), finish);
		return length;
	}
	
/**
 * Implements the recursive definition.
 * @param p the point calling the function
 * @param timeleft the available amount of time 
 * @param current_path the current path cosntructed
 * @param print
 * @return
 */
	public POI recursive(POI p,double timeleft,ArrayList<POI> current_path,boolean print){
		if(print){
			System.out.println("Dialegei o : "+p.node_id+ " gia xrono "+timeleft);
			System.out.println("Path : "+Computation.ListToString(current_path));
		}
		
	
		ArrayList<POI> visitors=new ArrayList<POI>();
		if(p.getVisitors(timeleft,finish)!=null){
			visitors=new ArrayList<POI>(p.getVisitors(timeleft,finish));
			for(POI x : current_path){
				if(visitors.contains(x)){
					visitors.remove(x);
				}
			}
		}
		if(p.getVisitors(timeleft,finish)==null||visitors.isEmpty()){
			if(print){
				System.out.println("O idios o : "+p.node_id+ " gruanei  ");
			}
			
			return p;//base case
			
		}else{
			POI candidate=null;
			if(print){
				System.out.println("Visitors: "+Computation.ListToString(visitors));
			}
		
			for(POI v : visitors){
					ArrayList<POI> temp=new ArrayList<POI>(current_path);
					temp.add(v);
					candidate=recursive(v,timeleft-Computation.EuDi(p,v),temp,print);
					int score=CalcScore(temp);
					boolean stop=true;//controls no re adding optimal path
					int id=temp.get(0).node_id;
					
					if(best_in_branch.get(id).isEmpty()){
						best_in_branch.get(id).add(temp);
					}else{
						
						int s=CalcScore(best_in_branch.get(id).get(0));//saved score
						if(score>s){//current greater than saved
							best_in_branch.get(id).clear();
							best_in_branch.get(id).add(temp);
						}else if(score==s){//save both for longest common factor
							best_in_branch.get(id).add(temp);
						}
					}
					if(score>optimal_score){
						//System.out.println("previous,new :"+optimal_score+"  "+score);
						optimal_score=score;
						if(!optimal.isEmpty()){//if solutions with less score exist
							optimal.clear();//remove these solutions
						}
						optimal.add(new ArrayList<POI>(temp));//add new one
						stop=false;
						if(print){
							System.out.println("***************NEW OPT:"+Computation.ListToString(temp));
						}
					}
					if(score==optimal_score&&stop){
						optimal.add(new ArrayList<POI>(temp));//just add solution
					}
			}
			return candidate;
		}
	}
	
	public static int CalcScore(ArrayList<POI> list){
		if(list==null){
			return 0;
		}else{
			int score=0;
			for (POI p : list){
				score=score+p.getScore();
			}
			return score;
		}
		
	}
	
}
