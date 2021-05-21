package HeurAlgorithms;

import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import OPHSAttributes.Hotel;
import OPHSAttributes.HotelEdge;
import OPHSAttributes.OPHSGraph;
import OPHSAttributes.Trip;
import DataStructures.GraphTree;
import DataStructures.Root;
import DataStructures.TreeNode;
import DataStructures.Leaf;

public class AllFeasiblePathsAlgorithm {
	private final static int number_of_Trips = Trip.getNumber_of_trips();
	private final OPHSGraph Graph;
	private final TreeMap<Integer,PriorityQueue<HotelEdge>> dictionary;
	
	
	
	public AllFeasiblePathsAlgorithm(OPHSGraph Graph) {
		this.Graph = Graph;
		this.dictionary=new TreeMap<Integer,PriorityQueue<HotelEdge>>();
		for(int i=0; i<Trip.getNumber_of_trips(); i++){
			this.dictionary.put(i, new PriorityQueue<HotelEdge>());
		}
	}
	
	

	public OPHSGraph getGraph() {
		return Graph;
	}



	/**
	 * This method is responsible for intializing each tree map structure for
	 * every hotel. Computes all the feasible paths of D size under time
	 * constraints.
	 */
	private void ComputeVisitorsLimits() {
		int trip_id = 0;
		ArrayList<Hotel> ls = new ArrayList<Hotel>();
		ArrayList<Hotel> hs = new ArrayList<Hotel>();
		hs.add(Hotel.getStartDepot());
		while (trip_id < number_of_Trips) {// while all number of trips
											// completed
			ls = new ArrayList<Hotel>(hs);
			hs.clear();
			for (Hotel hotel : ls) {
				ArrayList<Hotel> set;
				if (hotel.isStartFinishDepot()) {
					set = hotel.getHotelVisitorsUnderRadiusForDepots(trip_id);
				} else {
					set = hotel.getHotelVisitorsUnderRadius(trip_id);
				}

				hotel.visitors_limits.put(trip_id, set);
				ListsUnion(hs, new ArrayList<Hotel>(set));// add only
															// non-included
															// hotels max size
															// of list is hotel
															// population
			}
			trip_id++;
		}
	}

	public void ComputePreviousLimits() {
		for (int trip = 0; trip < Trip.getNumber_of_trips(); trip++) {
			for (Hotel set_previous : Hotel.getHotel_population()) {
				ArrayList<Hotel> previous_hotels = new ArrayList<Hotel>();
				for (Hotel previous : Hotel.getHotel_population()) {
					if (!set_previous.equals(previous)) {
						if (previous.visitors_limits.get(trip) != null) {
							if (previous.visitors_limits.get(trip).contains(set_previous)) {
								previous_hotels.add(previous);
							}
						}
					}
				}
				set_previous.previous_limits.put(trip, new ArrayList<Hotel>(previous_hotels));
			}
		}

	}

	public void createDictionary(){
		for(int i=0; i<Trip.getNumber_of_trips(); i++){//for each trip
			for(Hotel hotel : Hotel.getHotel_population()){//for all hotels
				if(hotel.visitors_limits.get(i)!=null){//if visitors exist
					for(Hotel end : hotel.visitors_limits.get(i)){//iterate
						HotelEdge h_edge=new HotelEdge(hotel, end,i);//compute edge
						dictionary.get(i).add(h_edge);//sort and save
					}
					
				}
			}
		}
		printDictionary();
	}


	public static void ListsUnion(ArrayList<Hotel> hs, ArrayList<Hotel> set) {
		for (Hotel h : set) {
			if (!hs.contains(h)) {
				hs.add(h);
			}
		}
	}

	public static void MergeListsTreeNodes(ArrayList<TreeNode> layer, ArrayList<TreeNode> visitors) {
		if (layer.isEmpty()) {

			layer = visitors;
		} else {
			for (TreeNode node : visitors) {
				if (!layer.contains(node)) {
					layer.add(node);
				}
			}
		}
	}

	public static void PrintTrees() {
		ArrayList<Hotel> h_pop = new ArrayList<Hotel>(Hotel.getHotel_population());
		for (int i = 0; i < number_of_Trips; i++) {
			for (Hotel h : h_pop) {
				if (h.visitors_limits.get(i) != null) {
					String x = "";
					for (Hotel hh : h.visitors_limits.get(i)) {
						x = x + " " + hh.getNode_id();
					}
					System.out.println("For trip : " + i + " with start : " + h.getNode_id() + " can visit : " + x);
				}
			}
			System.out.println("--------------------------------------");
		}
	}

	public static void PrintTreesPrevious() {
		for (int i = 0; i < number_of_Trips; i++) {
			for (Hotel hotel : Hotel.getHotel_population()) {
				if (hotel.previous_limits.get(i) != null) {
					String x = "";
					for (Hotel p : hotel.previous_limits.get(i)) {
						x = x + " " + p.getNode_id();
					}
					System.out.println(
							"For trip " + i + " node:" + hotel.getNode_id() + " can be selected as end by: " + x);
				}

			}
			System.out.println("--------------------------------------");
		}
	}

	public void execute(){
		this.ComputeVisitorsLimits();
		this.ComputePreviousLimits();
		//PrintTrees();
		System.out.println(" ");
		//PrintTreesPrevious();
	}
	public GraphTree CreateOphsTree() {
		this.ComputeVisitorsLimits();
		Root root = new Root(Hotel.getStartDepot());// root of tree
		GraphTree tree = new GraphTree(root);// create graph tree
		int trip_id = 1;
		while (trip_id < number_of_Trips) {
			ArrayList<TreeNode> start_layer = tree.layer_nodes.get(trip_id);// get
																			// start
																			// hotels
																			// for
																			// this
																			// trip
			for (TreeNode start : start_layer) {
				ArrayList<Hotel> visitors = start.getNode().visitors_limits.get(trip_id);// get
																							// visitors
				((Leaf) start).getFeasibleNonPrevious(visitors);// compute and
																// store
																// feasible as
																// leaves;
				if (tree.layer_nodes.get(trip_id + 1) == null) {
					tree.layer_nodes.put(trip_id + 1, new ArrayList<TreeNode>(start.getLeaves()));
				} else {
					tree.layer_nodes.get(trip_id + 1).addAll(start.getLeaves());
				}
			}
			trip_id++;
		}
		return tree;
	}
	
	public TreeMap<Integer, PriorityQueue<HotelEdge>> getDictionary() {
		return dictionary;
	}

	private void printDictionary(){
		for(Map.Entry<Integer,PriorityQueue<HotelEdge>> entry : dictionary.entrySet()) {
			Integer key = entry.getKey();
			System.out.println("Trip is: "+key); 
			PriorityQueue<HotelEdge> value = entry.getValue();
			  while(!value.isEmpty()){
				  HotelEdge edge=value.remove();
			System.out.println(edge.toString());
			  }
			  System.out.println("------------------------------"); 
			}
	}
	


}
