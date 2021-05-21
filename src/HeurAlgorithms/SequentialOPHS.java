package HeurAlgorithms;


import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.TreeMap;
import Computabillity.Computation;
import OPHSAttributes.Hotel;
import OPHSAttributes.Node;
import OPHSAttributes.NodeWithScore;
import OPHSAttributes.NodeWithScoreInversed;
import OPHSAttributes.POI;


public class SequentialOPHS {
	private static  ArrayList<ArrayList<POI>> optimal= new ArrayList<ArrayList<POI>>();
	private ArrayList<Double> calls=new ArrayList<Double>();
	private TreeMap<Double,ArrayList<POI>> start_visitors;
	private Hotel start;
	private Hotel end;
	private double Td;
	private double time_needed;
	private double checker=0;
	private double last_call=0;
	private  ArrayList<POI> candidates;
	private final double step_size;

	public SequentialOPHS(Hotel start, Hotel finish,double Td){
		this.start=start;
		this.end=finish;
		this.Td=Td;
		this.checker=Td+0.1;
		this.start_visitors=new TreeMap<Double,ArrayList<POI>>();
		System.out.println(" ");
		System.out.println("Start Hotel: "+start.node_id+" Finish Hotel: "+end.node_id+" time budget: "+Td);
		this.candidates=new ArrayList<POI>(collectIntermediate());
		this.step_size=0.1;
		Preprocess();
		
		optimal.clear();
	}
	public SequentialOPHS(Hotel start, Hotel finish,double Td,TreeMap<Integer,Boolean> inline_part){
		this.start=start;
		this.end=finish;
		this.Td=Td;
		this.checker=Td+0.1;
		this.start_visitors=new TreeMap<Double,ArrayList<POI>>();
		System.out.println(" ");
		System.out.println("Start Hotel: "+start.node_id+" Finish Hotel: "+end.node_id+" time budget: "+Td+" Sequential");
		this.candidates=new ArrayList<POI>(collectIntermediateUnderPartition(inline_part));
		
		this.step_size=0.1;
		Preprocess();
		
		optimal.clear();
	}
	
	public void refresh(){
		optimal.clear();
		start_visitors.clear();
		for(POI p : candidates){
			p.visitors.clear();
			p.D_bound_scores.clear();
			p.D_times_paths.clear();
		}
		candidates.clear();
	}
	
	
	public ArrayList<POI> collectIntermediate(){
		ArrayList<POI> collection=new ArrayList<POI>();
		ArrayList<Node> list=null;
		double lb=8*Td;
		int denom=0;
		boolean control=true;
		while(lb>2*Td&&control){
			denom++;
			list=new ArrayList<Node>();
			list.add(start);
			Node x=Node.CreateMidNode(start,end);
			double radius=computeRadius(denom);//get radius
			for(POI p : POI.getPoi_population()){
				if(Computation.EuDi(p,x)<radius){
					list.add(p);
				}
			}
			list.add(end);
			if(list.size()==2){
				control=false;
			}
			LowerBoundHK Hk=new LowerBoundHK(list);
			Hk.execute();
			lb=Hk.getFinal_bound();
		}
		list.remove(start);
		list.remove(end);
		for(Node node : list){
			if(node instanceof POI){
				collection.add((POI)node);
			}
		}
		return collection;
	}
	
	public ArrayList<POI> collectIntermediateUnderPartition(TreeMap<Integer,Boolean> map){
		ArrayList<POI> collection=new ArrayList<POI>();
		ArrayList<Node> list=null;
		double lb=8*Td;
		int denom=0;
		boolean control=true;
		if(Td>0){
			while(lb>2*Td&&	control){
				denom++;
				list=new ArrayList<Node>();
				list.add(start);
				Node x=new Node((start.getX_cord()+end.getX_cord())/2.0,(start.getY_cord()+end.getY_cord())/2.0);//create median
				double radius=computeRadius(denom);//get radius
				for(POI p : POI.getPoi_population()){
					if(map.get(p.node_id)!=null){
						if(Computation.EuDi(p,x)<radius&&!map.get(p.node_id)){//poi is used in previous trip
							list.add(p);
						}
					}else{
						if(Computation.EuDi(p,x)<radius){//poi is used in previous trip
							list.add(p);
						}
					}
				}
				list.add(end);
				if(list.size()==2){
					control=false;
				}
				LowerBoundHK Hk=new LowerBoundHK(list);
				Hk.execute();
				lb=Hk.getFinal_bound();
			}
			list.remove(start);
			list.remove(end);
			for(Node node : list){
				if(node instanceof POI){
					collection.add((POI)node);
				}
			}
		}
		
		return collection;
	}
	
	public double computeRadius(int denom){
		return Math.max(Computation.EuDi(start,end),Td-Computation.EuDi(start,end))/denom;
	}
	
	public  void Preprocess(){
		for(POI p : candidates){
			p.ComputeVisitorsIntegersDecimals(Td, this.end);
			p.ComputeMinDistDecimals(this.end);
			p.preprocessBoundsDecimals();
			p.preprocessPathsDecimals();
		}
	}
	
	//extracts the first value of Td for which we have a feasible solution
	public double computeStartingTime(){
		double min=Double.MAX_VALUE;
		for(POI p : candidates){//for all pois
			if(Computation.EuDi(start,p)+Computation.EuDi(p,end)<min){//find one that minimizes the total dist
				min=Computation.EuDi(start,p)+Computation.EuDi(p,end);
			}
		}
		return min;
	}
	
	//Implements the basic bounding computation
	
	
	public void executeCorrect(boolean print){
		long in=System.nanoTime();
		int added=0;
		double t=computeStartingTime();//first feasible solution
		t=Computation.roundAndPrepare(t,2);//to avoid duplicate entries on the treemap.
		double step=step_size;
		if(print){
			System.out.println("Instance starts at: "+t);
		}
		boolean control=true;
		while(t<=checker&&control){//viewpoint at value t
			Stack<POI> stack=new Stack<POI>();//create stack
			if(print){
				System.out.println("----First level for "+ t+" amount of time has");
			}
			//
			//System.out.println(t);
			String level="";
			for(POI p : ComputeFirstLevelsORT(t)){//add first layer candidates O(n)
				stack.push(p);
				if(print){
					level=level+"-"+Integer.toString(p.node_id);
				}
			}
			if(print){
				System.out.println(level);
			}
			while(!stack.isEmpty()){//until O(n) solutions have been created
				int max_score=Integer.MIN_VALUE;
				POI max_vis;
				ArrayList<POI> opt_seq=new ArrayList<>();
				POI candidate=stack.pop();//first candidate
				double time_left=t-Computation.EuDi(start, candidate);
				if(print){
					System.out.println("*******Branch: "+candidate.node_id +" for amount of time: "+time_left);
				}
				ArrayList<POI> visitors=candidate.getVisitors(time_left,end);
				//sort them according the time allowed to spent. min time goes first example 8037 -17
				if(print){
					if(visitors!=null){
						System.out.println("	Visitors are: "+Computation.ListToString(visitors));
					}else{
						System.out.println("	No visitors");
					}
				}
				max_score=0;
				max_vis=null;
				opt_seq.clear();
				opt_seq=null;
				if(visitors!=null){//if visitors exist
					for(POI p : visitors){//for all O(n-1) visitors
						double pers_left=time_left-Computation.EuDi(candidate,p);
						if(print){
							System.out.println("	Node examined: "+p.node_id+" amount "+pers_left);
						}
						if(p.getPath(pers_left)!=null){//if there is a path
							added=0;
							ArrayList<POI> p_sequence=new ArrayList<POI>(p.getPath(pers_left));//get path
							if(print){
								System.out.println("   	   Visitor: "+p.node_id+" examined for: "+pers_left+" kai exei: "+Computation.ListToString(p_sequence));
							}
							if(p_sequence.contains(candidate)){//if root of branch contained 
								ArrayList<POI> prefix=new ArrayList<POI>();
								prefix.add(p);// p is by definition different from candidate
								//amount of time last point of prefix has to spent
								double remain=computeTimeToSpend(candidate,prefix,t);//allakse to tsampa ipologizei
								if(print){
									System.out.println(" 		Prefix constructed: "+ Computation.ListToString(prefix)+" remain: "+remain);
								}
								p_sequence.remove(candidate);//sequence
								ArrayList<POI> subopt=balanceFilling(p_sequence,prefix.get(prefix.size()-1),remain,candidate);
								if(subopt==null){//nothing to add, root returns itself 3-9-2-3->3-9-2-X
									added=CalcScore(prefix);//get score so far
									if(print){
										System.out.println("	added="+added);
									}
									prefix.remove(p);
									p_sequence=new ArrayList<POI>(prefix);
								}else{//there is a path or a poi after prefixing 
									if(print){
										System.out.println("		The sequence is: "+Computation.ListToString(subopt));
									}
									subopt.remove(0);
									
									p_sequence=new ArrayList<POI>();
									for(POI z : prefix){
										p_sequence.add(z);
									}
									for(POI z : subopt){
										p_sequence.add(z);
									}
									added=CalcScore(p_sequence);
									p_sequence.remove(p);
								}
							//no prefixing
							}else{//exact bound
								int bs=p.getBound(pers_left);
								added=bs;
								if(print){
									System.out.println("	added="+added);
								}
							}
							if(added>max_score){
								max_score=added;
								max_vis=p;
								opt_seq=new ArrayList<POI>(p_sequence);
							}
						}else{//there is no path to extend
							if(p.getScore()>max_score){
								max_score=p.getScore();
								max_vis=p;
							}
						}
					}//
				}
				candidate.D_bound_scores.put(time_left,candidate.getScore()+max_score);
				ArrayList<POI> path=new ArrayList<POI>();
				if(max_vis!=null){
					if(print){
						System.out.println(" max visitor is "+max_vis.node_id);
					}
					path.add(max_vis);
				}
				if(opt_seq!=null){
					for(POI p : opt_seq){
						path.add(p);
					}
				}
				candidate.D_times_paths.put(time_left, path);
				if(print){
					System.out.println(" -->Poi : "+candidate.node_id+" for time: "+time_left+" bound: "+candidate.D_bound_scores.get(time_left)+" sequence: "+Computation.ListToString(candidate.D_times_paths.get(time_left)));
				}
			}
			if(print){
				System.out.println("------------------------------------------");
			}
			//printbesties();
			calls.add(t);
			last_call=t;
			t=t+step;//increase
			if(t>checker){
				control=false;
			}
			
		}
		long out=System.nanoTime();
		long time=out-in;
		double seconds = (double)time / 1000000000.0;
		this.time_needed=seconds;
		System.out.println("Algorithm Time needed: "+time_needed);
		
	}

	

	
	public ArrayList<POI> balanceFilling(ArrayList<POI> p_seq,POI last,double time,POI root){
		int bound=last.getBound(time);
		TreeMap<Integer,Boolean> partition=new TreeMap<Integer,Boolean>();
		for(POI p : p_seq){
			partition.put(p.node_id,true);
		}
		ArrayList<POI>excluded=new ArrayList<POI>();
		if(last.getVisitors(time,end)!=null){//has to be different 
			excluded=new ArrayList<POI>(last.getVisitorsUnderPartition(time,end,partition));//excluded=visitors-p_seq
			if(excluded.contains(root)){
				excluded.remove(root);//remove root now allowed to repick
			}
		}
		ArrayList<POI> mark=new ArrayList<POI>();
		for(POI p : excluded){
			if(!candidates.contains(p)){
				mark.add(p);
			}
		}
		for(POI d : mark){
			excluded.remove(d);
		}
		PriorityQueue<NodeWithScore> out=new PriorityQueue<NodeWithScore>();//visitors excluded are picked by max
		PriorityQueue<NodeWithScoreInversed> in=new PriorityQueue<NodeWithScoreInversed>();//visitors in the sequence are picked by min
		for(POI p : excluded){
			partition.put(p.node_id,false);
			out.add(new NodeWithScore(p));
		}
		for(POI p : p_seq){
			in.add(new NodeWithScoreInversed(p));
		}
		ArrayList<Node> s=new ArrayList<Node>(p_seq);
		s.add(0,last);
		s.add(end);
		TreeMap<Integer,Boolean> out_p=new TreeMap<Integer,Boolean>();
		TreeMap<Integer,Boolean> in_p=new TreeMap<Integer,Boolean>();
		int best_found=Integer.MIN_VALUE;
		ArrayList<Node> found=null;
		boolean condition=true;
		while(condition){
			double l=Computation.partialPathLength(s);	
			if(l>time){//infeasible, only deletion
				if(!in.isEmpty()){
					POI remove=in.remove().node;
					s.remove(remove);
					if(out_p.get(remove.node_id)==null){
						out.add(new NodeWithScore(remove));
					}
				}else{//no more deletions can happen
					condition=false;
				}
			}else{//feasible
				int score=CalcScoreComplete(s);
				if(score>best_found){
					best_found=score;
					found=new ArrayList<Node>(s);
				}
				if(score<bound){
					//addition
					if(!out.isEmpty()){
						POI add=out.remove().node;
						out_p.put(add.node_id,true);
						int index=findInsertion(add,s);
						s.add(index+1,add);
						if(in_p.get(add.node_id)==null){//have not considered before in <in>
							in.add(new NodeWithScoreInversed(add));
						}
						
					}else{//no more additions can happen
						condition=false;
					}
				}else{
					//deletion
					if(!in.isEmpty()){
						POI remove=in.remove().node;
						in_p.put(remove.node_id,true);
						s.remove(remove);
						if(out_p.get(remove.node_id)==null){
							out.add(new NodeWithScore(remove));
						}
					}else{//no more deletions can happen
						condition=false;
					}
				}
			}
		}
		
		ArrayList<POI> found_pois=null;
		if(found!=null&&found.size()>2){//we exclude start and end
			found_pois=new ArrayList<POI>();
			for(int i=1; i<found.size()-1; i++){
				found_pois.add((POI)found.get(i));
				//System.out.println("Found: "+Computation.ListToString(found_pois));
			}
		}
		return found_pois;
	}

	public int findInsertion(POI candidate,ArrayList<Node> list){
		double min=Double.MAX_VALUE;
		int index=0;
		for(int i=0; i<list.size()-1; i++){
			double l1=Computation.EuDi(list.get(i),candidate);
			double l2=Computation.EuDi(candidate, list.get(i+1));
			if(l1+l2<min){
				min=l1+l2;
				index=i;
			}
		}
		return index;
	}
	
	
	public double extractCallLimit(double limit){
		double l=0;
		for(double call : calls){
			if(Computation.accurateRound(call, 2)==Computation.accurateRound(limit, 2)){
				l=call;
			}
		}
		return l;
	}
	
	//calculates the distance from initial hotel to candidate and then concatenates the sequence. 
	//Returns the diferrenece between t and the length of this partial path
	public double computeTimeToSpend(POI candidate, ArrayList<POI> sequence,double t){
		double length=Computation.EuDi(start, candidate);
		length=length+Computation.EuDi(candidate, sequence.get(0));
		for(int j=1; j<=sequence.size()-1; j++){
			length=length+Computation.EuDi(sequence.get(j-1), sequence.get(j));
		}
		double remain=t-length;
		return remain;
	}
	
	public static double getRealPathLength(Hotel start, Hotel end, ArrayList<POI> sequence){
		double length=0;
		if(sequence!=null){
			if(!sequence.isEmpty()){
				length=Computation.EuDi(start, sequence.get(0));
				for(int j=1; j<=sequence.size()-1; j++){
					length=length+Computation.EuDi(sequence.get(j-1), sequence.get(j));
				}
				length=length+Computation.EuDi(sequence.get(sequence.size()-1), end);
			}
		}
		return length;
	}
	
	//computes the first level of the search tree O(n) size
	public ArrayList<POI> ComputeFirstLevelsORT(double t){
		PriorityQueue<FirstLevelPair> pq=new PriorityQueue<FirstLevelPair>();
		ArrayList<POI> first_level=new ArrayList<POI>();
		ArrayList<POI> util=new ArrayList<POI>();
		for(POI p : candidates){
			if(Computation.EuDi(start, p)+Computation.EuDi(p,end)<=t){
				double l=t-Computation.EuDi(start, p);
				pq.add(new FirstLevelPair(p,l));
			}
		}
		while(!pq.isEmpty()){
			util.add(pq.remove().node);
		}
		for(int j=util.size()-1; j>=0; j--){
			first_level.add(util.get(j));
		}
		return first_level;
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
	
	public static int CalcScoreComplete(ArrayList<Node> list){
		if(list==null){
			return 0;
		}else{
			int score=0;
			for (Node p : list){
				if(p instanceof POI){
					score=score+((POI)p).getScore();
				}
			}
			return score;
		}
	}

	
	public void printbesties(){
		ArrayList<POI> opt=new ArrayList<POI>();
		int max_score=Integer.MIN_VALUE;
		System.out.println("-------------------------------------------------------------------");
		System.out.println("Time asked: "+last_call);
		for(POI p : candidates){
			double distance=Computation.EuDi(start, p);
			double value=last_call-distance;
			if(p.D_times_paths.get(value)!=null){
				ArrayList<POI> list=p.D_times_paths.get(value);
				list.add(0,p);
				if(CalcScore(list)>max_score){
					max_score=CalcScore(list);
					opt=new ArrayList<POI>(list);
				}
				System.out.println("Node: "+p.node_id+" Seq: "+Computation.ListToString(list)+" Score: "+CalcScore(list)+" Real Length: "+getRealPathLength(start,end,list));
			}
		}
		System.out.println("--------------------------------------------------------------------");
		System.out.println("Optimal: "+Computation.ListToString(opt)+" Score: "+max_score+" Real Length: "+getRealPathLength(start,end,opt));
	}
	
	public void printbesties(double t){
		ArrayList<POI> opt=new ArrayList<POI>();
		int max_score=Integer.MIN_VALUE;
		System.out.println("-------------------------------------------------------------------");
		System.out.println("Time asked: "+t);
		for(POI p : candidates){
			double distance=Computation.EuDi(start, p);
			double value=t-distance;
			if(p.D_times_paths.get(value)!=null){
				ArrayList<POI> list=p.D_times_paths.get(value);
				list.add(0,p);
				if(CalcScore(list)>max_score){
					max_score=CalcScore(list);
					opt=new ArrayList<POI>(list);
				}
				System.out.println("Node: "+p.node_id+" Seq: "+Computation.ListToString(list)+" Score: "+CalcScore(list)+" Real Length: "+getRealPathLength(start,end,list));
			}
		}
		System.out.println("--------------------------------------------------------------------");
		System.out.println("Optimal: "+Computation.ListToString(opt)+" Score: "+max_score+" Real Length: "+getRealPathLength(start,end,opt));
	}
	
	public ArrayList<POI> extractBest(){
		ArrayList<POI> opt=new ArrayList<POI>();
		int max_score=Integer.MIN_VALUE;
		for(POI p : candidates){
			double distance=Computation.EuDi(start, p);
			double value=last_call-distance;
			if(p.D_times_paths.get(value)!=null){
				ArrayList<POI> list=p.D_times_paths.get(value);
				list.add(0,p);
				if(CalcScore(list)>max_score){
					max_score=CalcScore(list);
					opt=new ArrayList<POI>(list);
				}
			}
		}
		return opt;
	}
	
	public ArrayList<POI> extractBest(double t){
		ArrayList<POI> opt=new ArrayList<POI>();
		int max_score=Integer.MIN_VALUE;
		for(POI p : candidates){
			double distance=Computation.EuDi(start, p);
			double value=t-distance;
			if(p.D_times_paths.get(value)!=null){
				ArrayList<POI> list=p.D_times_paths.get(value);
				if(CalcScore(list)>max_score){
					max_score=CalcScore(list);
					opt=new ArrayList<POI>(list);
				}
			}
		}
		return opt;
	}
	
	private  class FirstLevelPair implements Comparable<FirstLevelPair> {
		
		public final POI node;
		public final double value;
		
		
		public FirstLevelPair(POI node,double value) {
			this.node = node;
			this.value=value;
		}
		
		
		
		public int compareTo(FirstLevelPair other) {
			//check distances first
			if (value < other.value)
				return -1;
			else if (value > other.value)
				return 1;
			else if(value<other.value){
				return 1;
			}else if(value>other.value){
				return -1;
			}
			else
				return 0;
		}
		
	}

}
