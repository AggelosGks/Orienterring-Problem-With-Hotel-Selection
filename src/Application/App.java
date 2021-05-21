package Application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Map;
import java.util.TreeMap;

import Computabillity.Computation;
import Computabillity.FullySequentialStrategy;
import Computabillity.TourPopulationStructure;
import OPHSAttributes.Node;
import OPHSAttributes.OPHSGraph;
import OPHSAttributes.Edge;
import OPHSAttributes.Hotel;
import OPHSAttributes.POI;
import OPHSAttributes.Tour;
import OPHSAttributes.Trip;
import DataStructures.HotelSequence;
import DisplayPanels.GraphMap;
import DisplayPanels.MainFrame;
import HeurAlgorithms.AllFeasiblePathsAlgorithm;
import HeurAlgorithms.ExhaustiveSearch;
import HeurAlgorithms.ExhaustiveSearchDecimals;
import HeurAlgorithms.SingleOPIntegers;
import HeurAlgorithms.SingleOPDecimals;

public class App {
	public final static String Instances_path = "C:\\Users\\aggelos\\Desktop\\Aggelos\\JavaWorkspace\\OPHS\\Instances";
	public final static String TEMP = "C:\\Users\\aggelos\\Desktop\\Aggelos\\JavaWorkspace\\OPHS\\Instances\\Presentation\\32_T2_0_15.ophs";
	public final static String TEMP2 = "C:\\Users\\aggelos\\Desktop\\Aggelos\\JavaWorkspace\\OPHS\\Instances\\Presentation\\32_T2_0_30.ophs";
	public final static String TEMP3 = "C:\\Users\\aggelos\\Desktop\\Aggelos\\JavaWorkspace\\OPHS\\Instances\\Presentation\\32_T2_0_45.ophs";
	public final static int dif=0;
	File folder = null;
	static File[] listOfFiles = null;

	public static void main(String args[]) {
		ReadInstanceData(TEMP3);
		OP();
		new MainFrame();
	}// end main

	/**
	 * This method is responsible for reading instance data from a text file.
	 * After the method is terminated, a instance variable is created.
	 * 
	 * @param filename
	 *            the specified path of the text file.
	 */
	public static void ReadInstanceData(String filename) {
		ArrayList<Double> holder = new ArrayList<Double>();
		BufferedReader br = null;
		String line = "";// (1)
		String saver = "";
		try {
			br = new BufferedReader(new FileReader(filename));// KSEKINAEI
																// ANAGNWSI
																// ARXEIOU
			while ((line = br.readLine()) != null) {// string line reads
				if (line.trim().length() > 0) {
					line = line + " ";
					if (!line.contains("----------------------------------------")) {
						String[] ary = line.split("");
						for (String c : ary) {
							if (Computation.isNumerical(c)) {
								saver = saver + c;
							} else {
								if (!(saver.equals(""))) {
									holder.add(Double.parseDouble(saver));
								}
								saver = "";
							}
						}
					}
				}
			}

			new Instance(holder);
		}

		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method is responsible for clear all data structures after solving an
	 * instance. Mostly used when multiple instances are selected to be solved.
	 */
	public static void RefreshAttributes() {
		// empty populations for next instance
		Node.getTotal_population().clear();
		Hotel.getHotel_population().clear();
		POI.getPoi_population().clear();
		Edge.getTotal_edges().clear();
		OPHSGraph.setGraph(null);
		HotelSequence.getTotalFeasible().clear();
	}

	/**
	 * Retrieves all set names for Instances folder on project.
	 * 
	 * @static_variable instances_path : a string with the path of the folder in
	 *                  project.
	 */
	public static ArrayList<String> getSetNames() {
		ArrayList<String> set_names = new ArrayList<String>();
		File folder = new File(Instances_path);
		File[] sub_folder = folder.listFiles();
		for (File f : sub_folder) {
			if (f.isDirectory()) {
				String name = f.getName();
				set_names.add(name);
			}
		}
		return set_names;
	}

	/**
	 * Method to retrieve all instances names for a certain set.
	 * 
	 * @param set_file
	 *            : A file representing the folder of the set.
	 */
	public static ArrayList<String> getInstancesNames(File set_file) {
		ArrayList<String> inst_names = new ArrayList<String>();
		File[] instances = set_file.listFiles();
		for (File f : instances) {
			if (f.isFile()) {
				inst_names.add(f.getName());
			}
		}
		return inst_names;
	}

	/**
	 * Writes the optimal values for a given trip limit. 
	 * Used in the single OP with integer weights for solving multiple trips.
	 * @param time the T representing the budget  time
	 */
	public static void writeOptValuesTxt(int time){
		 BufferedWriter writer = null;
	        try {
	            //create a temporary file
	            
	            String name=TEMP.substring(TEMP.length()-1-12,TEMP.length()-5);
	            // This will output the full path where the file will be written to...
	           String path="C:\\Users\\aggelos\\Desktop\\Aggelos\\JavaWorkspace\\OPHS\\Optimal_Values";
	           File newFile = new File(path, name+".txt");
	           writer = new BufferedWriter(new FileWriter(newFile,true));
	           writer.newLine(); 
	           TreeMap<Integer,ArrayList<ArrayList<POI>>> opts=ExhaustiveSearch.getBest_in_branch();
	           for (Map.Entry<Integer,ArrayList<ArrayList<POI>>> entry : opts.entrySet()) {
	   			ArrayList<ArrayList<POI>> paths=entry.getValue();
	   			for(ArrayList<POI> list: paths){
	   				String x=Computation.ListToString(list);
	   				int score=ExhaustiveSearch.CalcScore(list);
	   				double length=0;
	   				for(int i=0; i<list.size()-1; i++){
	   					length=length+Computation.EuDiRound(list.get(i), list.get(i+1));
	   				}
	   				Hotel start=Hotel.getStartDepot();
	   				Hotel end=Hotel.getFinalDepot();
	   				length=length+Computation.EuDiRound(start, list.get(0))+Computation.EuDiRound(list.get(list.size()-1), end);
	   				double v=(int)time-Computation.EuDiRound(start, list.get(0));
	   				writer.write(" Node: " + entry.getKey() +" Time: "+ v+" Path: " + x+" Score: "+score +" Length : "+length);
	   				writer.newLine(); 
	   			}
	   		}
	            writer.write(Integer.toString(time));
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
	

	public static void SOPSolveGradually(){
		AllFeasiblePathsAlgorithm x = new AllFeasiblePathsAlgorithm(OPHSGraph.getGraph());
		x.execute();
		 Hotel start=Hotel.getStartDepot(); 
		  Hotel end=Hotel.getFinalDepot();
		  for(int i=35; i>=6; i--){
			double first_limit=Trip.getTripids_TimeConstraints().get(0)-i; 
			first_limit=Computation.round(first_limit, 0);
			ExhaustiveSearch innerop=new ExhaustiveSearch(start,end,first_limit,false); 
			writeOptValuesTxt((int)first_limit);//write optimal valiues
			if(i!=0){
				innerop.refresh();	
			}
				 
		  }
		  Instance.printData();
		  Computation.writeHoodsTxt();//write hood
	}
	

	public static void SingleOPExhaustIntegers(){
		
		AllFeasiblePathsAlgorithm x = new AllFeasiblePathsAlgorithm(OPHSGraph.getGraph());
		x.execute();
		 Hotel start=Hotel.getStartDepot(); 
		  Hotel end=Hotel.getFinalDepot();
			double first_limit=Trip.getTripids_TimeConstraints().get(0)-dif; 
			first_limit=Computation.round(first_limit, 0);
			ExhaustiveSearch innerop=new ExhaustiveSearch(start,end,first_limit,true);
			innerop.printbesties();
			innerop.refresh();
	}
	
	/**
	 * Implements the exhaustive search for a single OP 
	 */
	public static void SingleOPExhaustDecimals(){
		
		AllFeasiblePathsAlgorithm x = new AllFeasiblePathsAlgorithm(OPHSGraph.getGraph());
		x.execute();
		 Hotel start=Hotel.getStartDepot(); 
		  Hotel end=Hotel.getFinalDepot();
			double first_limit=Trip.getTripids_TimeConstraints().get(0)-dif; 
			first_limit=Computation.accurateRound(first_limit,2);
			first_limit=first_limit+0.099999999;
			ExhaustiveSearchDecimals innerop=new ExhaustiveSearchDecimals(start,end,first_limit,false);
			innerop.printbesties();
			innerop.refresh();
	}
	
	public static void OP(){
		Hotel start=Hotel.getStartDepot();
		Hotel end=Hotel.getFinalDepot();
		double first_limit=Trip.getLength_limitById(0);
		SingleOPDecimals innerop=new SingleOPDecimals(start,end,first_limit);
		innerop.eXecute(false);
		innerop.printbesties();
		ArrayList<POI> sequence=innerop.extractBest();
		Trip trip =new Trip(start,end,0,sequence);
		Tour tour=new Tour();
		tour.addTriptoTour(trip);
		GraphMap.setTour(tour);
	}

	/**Solves the single OP for two hotels
	 * with integer weights.
	 * 
	 */
	public static void SingleOPFastIntegers(){
		
		AllFeasiblePathsAlgorithm x = new AllFeasiblePathsAlgorithm(OPHSGraph.getGraph());
		x.execute();
		 Hotel start=Hotel.getStartDepot(); 
		  Hotel end=Hotel.getFinalDepot();
			double first_limit=Trip.getTripids_TimeConstraints().get(0)-dif; 
			first_limit=Computation.round(first_limit, 0);
			SingleOPIntegers innerop=new SingleOPIntegers(start,end,first_limit);
			innerop.executeCorrect(false);
			innerop.refresh();
	}
	

	/**
	 * Solves the single OP between two specified hotels. Used only for evaluation.
	 */
	public static void SingeOPFast(){
		Hotel start=Hotel.getHotelById(4);
		Hotel end=Hotel.getHotelById(8);
		double first_limit=Trip.getLength_limitById(2);
		SingleOPDecimals innerop=new SingleOPDecimals(start,end,first_limit);
		innerop.eXecute(false);
		innerop.printbesties();
		innerop.refresh();
	}
	/**
	 * Solves a OPHS instance fully sequentially.
	 * First creates a population of tours and then solves each
	 * tour sequentially.
	 */
	public static void OPHSFullSeq(){
		long start=System.nanoTime();
		AllFeasiblePathsAlgorithm y = new AllFeasiblePathsAlgorithm(OPHSGraph.getGraph());
		y.execute();
		TourPopulationStructure x=new TourPopulationStructure();
		x.fillHorizontally();
		int[][]pop=x.getPopulation();
		ArrayList<Tour> initial_pop=new ArrayList<Tour>();
		for(int i=0; i<pop.length; i++){
			ArrayList<Hotel> h=new ArrayList<Hotel>();
			for(int j=0; j<Trip.getNumber_of_trips()+1; j++){
				h.add(Hotel.getHotelById(pop[i][j]));
			}
			System.out.println(Computation.ListToStringHotels(h));
			initial_pop.add(new Tour(h));
		}
		
		for(Tour tour : initial_pop){
			FullySequentialStrategy l=new FullySequentialStrategy(tour);
			l.execute();
		}
		long end=System.nanoTime();
		long diff=end-start;
		double sec=(double)diff / 1000000000.0;
		System.out.println("Seconds: "+sec);
		FullySequentialStrategy.optimal.PrintTour();
	}
	
	/**
	 * Solves a OPHS instance fully sequentially.
	 * First creates a population of tours and then solves each
	 * tour sequentially.
	 */
	public static void SingeOPFast(Hotel start,Hotel end, int max_trip_index){
		SingleOPDecimals innerop=new SingleOPDecimals(start,end,Trip.getLength_limitById(max_trip_index));
			innerop.eXecute(false);
			for(int i=0; i<Trip.getNumber_of_trips(); i++){
				if(start.visitors_limits.get(i)!=null){
					if(start.visitors_limits.get(i).contains(end)){
						double trip_limit=Trip.getLength_limitById(i);
						
						ArrayList<POI> seq=innerop.extractBest(innerop.extractCallLimit(trip_limit));
						Trip trip=new Trip(start,end,i,seq);
						if(start.trips.get(i)==null){
							start.trips.put(i,new ArrayList<Trip>());//put it in the start's list 
						}
						start.trips.get(i).add(trip);
						Trip rev=new Trip(trip.getEnd(),start,i,Computation.reverse(seq));//reverse sequence
						if(end.trips.get(i)==null){
							end.trips.put(i,new ArrayList<Trip>());//put it in the end's list
						}
						end.trips.get(i).add(rev);
						
					}
				}
			}
			innerop.refresh();
	}
	
	/**
	 * Solves a OPHS instance non sequentially.
	 * Solves all possible On^2 trips by looking at each trip once. Solves
	 * for the maximum value of trip between each pair and retrieves the solution for intermediate values
	 *
	 */
	public static void OPHSFast(){
		long start=System.nanoTime();
		AllFeasiblePathsAlgorithm x = new AllFeasiblePathsAlgorithm(OPHSGraph.getGraph());
		x.execute();
		Hotel.preprocessDepots();
		Hotel.preprocessHotels();
		Hotel.printkNearest();
		Depots();
		Computation.Hotels();
		long end=System.nanoTime();
		long time=(end-start);
		double secs=(double)time/1000000000.0;
		System.out.println("OPHS time needed: "+secs);
		reveal();
	}
	
	
	public static void reveal(){
		
			for(int i=0; i<Trip.getNumber_of_trips(); i++){
				for(Hotel h: Hotel.getHotel_population()){
				if(h.trips.get(i)!=null){
					for(Trip trip : h.trips.get(i)){
						System.out.println("Start: "+trip.getStart().node_id+" End: "+trip.getEnd().node_id+" Trip id: "+i+" Seq: "+Computation.ListToStringNodes(trip.getPermutation())+" Length: "+trip.getLength());
					}
				}
				
			}
		}
	}
	
	
	public static void Preprocess(){
		Hotel.preprocessDepots();
		Hotel.preprocessHotels();
	}
	public static void Depots(){
		Hotel start_depot=Hotel.getStartDepot();
		Hotel final_depot=Hotel.getFinalDepot();
		for(Hotel hotel: start_depot.k_visitors){
			int max_trip_index=-1;
			if(hotel.node_id!=0){
				max_trip_index=Computation.getMaxTripLimit(Hotel.getStartDepot(),hotel);
				if(max_trip_index!=-1){
					SingeOPFast( Hotel.getStartDepot(), hotel, max_trip_index );
				}
			}
		}
		for(Hotel hotel: final_depot.k_visitors){
			int max_trip_index=-1;
			if(hotel.node_id!=0){
				max_trip_index=Computation.getMaxTripLimit(Hotel.getFinalDepot(),hotel);
				if(max_trip_index!=-1){
					SingeOPFast( Hotel.getFinalDepot(), hotel, max_trip_index );
				}
			}
		}
	}
	
	
}
