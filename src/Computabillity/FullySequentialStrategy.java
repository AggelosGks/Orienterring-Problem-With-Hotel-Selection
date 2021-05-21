package Computabillity;

import java.util.ArrayList;
import java.util.TreeMap;

import HeurAlgorithms.SequentialOPHS;
import OPHSAttributes.POI;
import OPHSAttributes.Tour;
import OPHSAttributes.Trip;

public class FullySequentialStrategy {
	public static Tour optimal;
	public static int optimal_score = 0;
	public final TreeMap<Integer, Boolean> dictionary;
	public final Tour tour;
	public final ArrayList<Trip> trips;

	public FullySequentialStrategy(Tour tour) {
		this.dictionary = new TreeMap<Integer, Boolean>();
		for (POI p : POI.getPoi_population()) {
			dictionary.put(p.node_id, false);
		}
		this.tour = tour;
		this.trips = new ArrayList<Trip>();
	}

	public void execute() {
		solveFirst();
		solveRest();
		tour.setTrips(trips);
		tour.calcTourLength();
		tour.calcTourScore();
		tour.isfeasible();
		if (tour.getTour_score() > optimal_score) {
			optimal = tour;
			optimal_score = tour.getTour_score();
			}
		}	
	

	public void solveFirst() {
		Trip t = tour.getTrip(0);
		double limit = Trip.getLength_limitById(0);
		SequentialOPHS x = new SequentialOPHS(t.getStart(), t.getEnd(), limit);
		x.executeCorrect(false);
		ArrayList<POI> seq = x.extractBest();
		updateDictionary(seq);
		Trip trip = new Trip(t.getStart(), t.getEnd(), 0, seq);
		trips.add(trip);
		x.refresh();
	}

	public void solveRest() {
		for (int i = 1; i < Trip.getNumber_of_trips(); i++) {
			Trip t = tour.getTrip(i);
			double limit = Trip.getLength_limitById(i);
			SequentialOPHS x = new SequentialOPHS(t.getStart(), t.getEnd(), limit, dictionary);
			x.executeCorrect(false);
			ArrayList<POI> seq = x.extractBest();
			updateDictionary(seq);
			Trip trip = new Trip(t.getStart(), t.getEnd(), i, seq);
			trips.add(trip);
			x.refresh();
		}
	}

	public void updateDictionary(ArrayList<POI> seq) {
		for (POI p : seq) {
			dictionary.put(p.node_id, true);
		}
	}
}
