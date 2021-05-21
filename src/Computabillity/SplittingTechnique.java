package Computabillity;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeMap;

import HeurAlgorithms.SingleOPDecimals;
import OPHSAttributes.Hotel;
import OPHSAttributes.Node;
import OPHSAttributes.POI;
import OPHSAttributes.Tour;
import OPHSAttributes.Trip;
import OPHSAttributes.TripTriplet;

public class SplittingTechnique {
	public final static ArrayList<Tour> tours=new ArrayList<Tour>();
	public final static boolean[][] solved=new boolean[Hotel.getHotel_population().size()][Hotel.getHotel_population().size()];
	public final TreeMap<Integer,ArrayList<TripTriplet>> all;
	public  ArrayList<TripTriplet> m;
	
	public SplittingTechnique(){
		this.all=new TreeMap<Integer,ArrayList<TripTriplet>>();//subproblem, described by which denotes the first trip of the triplet
		this.m=new ArrayList<TripTriplet>();//list of triplets to be assigned to an instance
	}
	
	/**
	 * Implements the partial sequential strategy, for each trip i=1,3,5,7,..,D-1 solves all triplets
	 * and stores them.
	 */
	public void SolveSubProblems(){
		ArrayList<Integer> trips=Computation.split();
		for(int i : trips){
			CreateTriplets(i);
			for(TripTriplet t:this.m){
				t.setF_trip(solveCore(t.first,t.second,t.trip_id));
				t.setS_trip(inlineSolving(t.second,t.third,t.trip_id+1,removeFirstPartition(t)));
				t.computeScore();
				if(t.first.id_triplets.get(t.trip_id)==null){
					t.first.id_triplets.put(t.trip_id, new PriorityQueue<TripTriplet>());
				}
				t.first.id_triplets.get(t.trip_id).add(t);
			}
		}
	}
	public void PrintTriplets(){
		for(Hotel h : Hotel.getHotel_population()){
			h.PrintTriplets();
		}
	}
	/**
	 * Creates triplets to be assigned to a sub tour. The first id indicates the first 
	 * trip of the triplet.
	 * @param first_id
	 */
	public void CreateTriplets(int first_id){
		ArrayList<TripTriplet> list=new ArrayList<TripTriplet>();
		for(Hotel h : Hotel.getHotel_population()){
			if(h.visitors_limits.get(first_id)!=null){
				for(Hotel hh : h.getKNearestFromSet(first_id)){//for all contained
					if(h.visitors_limits.get(first_id).contains(hh)){
						if(hh.visitors_limits.get(first_id+1)!=null){
							if(first_id+1==Trip.getNumber_of_trips()-1){
								if(hh.visitors_limits.get(first_id+1).contains(Hotel.getFinalDepot())){
									TripTriplet triplet=new TripTriplet(first_id,h,hh,Hotel.getFinalDepot());
									list.add(triplet);
								}
							}else{
								for(Hotel hhh : hh.getKNearestFromSet(first_id+1)){
									if(hh.visitors_limits.get(first_id+1).contains(hhh)){
										TripTriplet triplet=new TripTriplet(first_id,h,hh,hhh);
										list.add(triplet);
									}
								}
							}
						}
					}
				}
			}
		}
		this.m=list;
		all.put(first_id, this.m);
	}
	
	/**
	 * Creates triplets to be assigned to a sub tour. The first id indicates the first 
	 * trip of the triplet. Rreturns a list containing these triplets
	 * @param first_id
	 */
	public ArrayList<TripTriplet> createTriplets(int first_id){
		ArrayList<TripTriplet> list=new ArrayList<TripTriplet>();
		for(Hotel h : Hotel.getHotel_population()){
			if(h.visitors_limits.get(first_id)!=null){
				for(Hotel hh : h.getKNearestFromSet(first_id)){//for all contained
					if(h.visitors_limits.get(first_id).contains(hh)){
						if(hh.visitors_limits.get(first_id+1)!=null){
							if(first_id+1==Trip.getNumber_of_trips()-1){
								if(hh.visitors_limits.get(first_id+1).contains(Hotel.getFinalDepot())){
									TripTriplet triplet=new TripTriplet(first_id,h,hh,Hotel.getFinalDepot());
									list.add(triplet);
								}
							}else{
								for(Hotel hhh : hh.getKNearestFromSet(first_id+1)){
									if(hh.visitors_limits.get(first_id+1).contains(hhh)){
										TripTriplet triplet=new TripTriplet(first_id,h,hh,hhh);
										list.add(triplet);
									}
								}
							}
						}
					}
				}
			}
		}
		this.m=list;
		return this.m;
	}
	
	public void printALL(){
		for(Integer k : all.keySet()){
			if(all.get(k)!=null){
				System.out.println("Trip: "+k);
				for(TripTriplet triplet : all.get(k)){
					triplet.printTriplet();
				}
			}
		}
	}
	
	/**
	 * used in inline solving for looking at each trip at most once. 
	 * It retrieves the path from a-b if we are asked for b-a.
	 * @param start
	 * @param end
	 * @param trip_id
	 * @return
	 */
	public Trip extractPathBetweenHotels(Hotel start,Hotel end,int trip_id){
		Trip path=null;
		for(Trip trip : start.trips.get(trip_id)){
			if(trip.getEnd().node_id==end.node_id){
				path=trip;
			}
		}
		return path;
	}
	
	
public void solveFirst(){
		
	}
	
	public Trip coreSolving(Hotel start,Hotel end, int max_trip_index,int asked){
		Trip saved=null;
		SingleOPDecimals innerop=new SingleOPDecimals(start,end,Trip.getLength_limitById(max_trip_index));
			innerop.eXecute(false);
			for(int i=0; i<Trip.getNumber_of_trips(); i++){
				if(start.visitors_limits.get(i)!=null){
					if(start.visitors_limits.get(i).contains(end)){
						double trip_limit=Trip.getLength_limitById(i);
						ArrayList<POI> seq=innerop.extractBest(innerop.extractCallLimit(trip_limit));
						Trip trip=new Trip(start,end,i,seq);
						if(i==asked){//asked has to be one of them since i runs from 0 to D-1
							saved=trip;
						}
						if(start.trips.get(i)==null){
							start.trips.put(i,new ArrayList<Trip>());//put it in the start's list 
						}
						
						start.trips.get(i).add(trip);
					}
				}
				if(end.visitors_limits.get(i)!=null){
					if(end.visitors_limits.get(i).contains(start)){
						double trip_limit=Trip.getLength_limitById(i);
						ArrayList<POI> seq=innerop.extractBest(innerop.extractCallLimit(trip_limit));
						Trip trip=new Trip(start,end,i,seq);
						Trip reversed=new Trip(trip.getEnd(),start,i,Computation.reverse(seq));//reverse sequence
						if(end.trips.get(i)==null){
							end.trips.put(i,new ArrayList<Trip>());//put it in the end's list
						}
						end.trips.get(i).add(reversed);
					}
				}
			}
			
			innerop.refresh();
			return saved;
	}

	public Trip inlineSolving(Hotel start,Hotel end, int asked,TreeMap<Integer,Boolean> map){
		SingleOPDecimals innerop=new SingleOPDecimals(start,end,Trip.getLength_limitById(asked),map);
		innerop.eXecute(false);
		Trip trip=new Trip(start,end,asked,innerop.extractBest());
		innerop.refresh();
		return trip;
	}
	
	
	
		
	public Trip solveCore(Hotel s,Hotel e,int asked){
			if(!solved[s.node_id][e.node_id]){
				solved[s.node_id][e.node_id]=true;
				solved[e.node_id][s.node_id]=true;
				int trip=Hotel.getMaxTripLimitBetween(s,e);
				return coreSolving(s,e,trip,asked);
			}else{
				return extractPathBetweenHotels(s,e,asked);
			}
	}
		
	public TreeMap<Integer,Boolean> removeFirstPartition(TripTriplet t){
			TreeMap<Integer,Boolean> map=new TreeMap<Integer,Boolean>();
			for(Node node : t.getF_trip().getPermutation()){
				if(node instanceof POI){
					map.put(node.node_id,true);
				}
			}
			return map;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void CreateTours(){
		ArrayList<Integer> tripakia=Computation.split();
		int first=tripakia.get(0);
		for(Hotel h : Hotel.getHotel_population()){
			build(h,first);
		}
	}
	
	
	public void build(Hotel h,int i){
		Tour tour=new Tour();
		while(i<Trip.getNumber_of_trips()){
			System.out.println("trip: "+i+" hotel "+h.node_id);
			if(h.id_triplets.get(i)!=null){
				PriorityQueue<TripTriplet> pq=new PriorityQueue<TripTriplet>(h.id_triplets.get(i));
				TripTriplet triplet=pq.remove();
				tour.addTriptoTour(triplet.getF_trip());
				tour.addTriptoTour(triplet.getS_trip());
				h=triplet.getS_trip().getEnd();
				i=i+2;
			}else{
				System.out.println("null");
		
				i=Trip.getNumber_of_trips();
			}
		}
		tour.PrintTour();
		tours.add(tour);
	}
	
	public void printTours(){
		for(Tour tour : tours){
			System.out.println(tour.toString());
		}
	}
	
	
	
	
	

	
	
	

	
	
	

}
 