package HeurAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.TreeMap;
import Computabillity.Computation;
import OPHSAttributes.Hotel;
import OPHSAttributes.POI;

public class SingleOPIntegers {
	private static ArrayList<ArrayList<POI>> optimal = new ArrayList<ArrayList<POI>>();
	private TreeMap<Double, ArrayList<POI>> start_visitors;
	private Hotel start;
	private Hotel end;
	private double Td;
	private double time_needed;

	public SingleOPIntegers(Hotel start, Hotel finish, double Td) {
		this.start = start;
		this.end = finish;
		this.Td = Td;
		this.start_visitors = new TreeMap<Double, ArrayList<POI>>();
		Preprocess();
		optimal.clear();
	}

	public void refresh() {
		optimal.clear();
		start_visitors.clear();
		for (POI p : POI.getPoi_population()) {
			p.visitors.clear();
			p.bound_scores.clear();
			p.times_paths.clear();

		}
	}

	public void Preprocess() {
		for (POI p : POI.getPoi_population()) {
			p.ComputeVisitorsIntegers(Td, this.end);
			p.ComputeMinDistIntegers(this.end);
			p.preprocessBoundsIntegers();
			p.preprocessPathsIntegers();
		}
	}

	// extracts the first value of Td for which we have a feasible solution
	public double computeStartingTime() {
		double min = Double.MAX_VALUE;
		for (POI p : POI.getPoi_population()) {// for all pois
			if (Computation.EuDi(start, p) + Computation.EuDi(p, end) < min) {
				min = Computation.EuDi(start, p) + Computation.EuDi(p, end);
			}
		}
		return min;
	}

	// Implements the basic bounding computation
	public void execute(boolean print) {
		long in = System.nanoTime();
		int added = 0;
		double t = computeStartingTime();// first feasible solution
		t = Computation.round(t, 0);
		while (t <= Td) {// viewpoint at value t
			Stack<POI> stack = new Stack<POI>();// create stack
			for (POI p : ComputeFirstLevelsORT(t)) {// add first layer	// candidates O(n)
				stack.push(p);
			}
			while (!stack.isEmpty()) {// until O(n) solutions have been created
				int max_score = Integer.MIN_VALUE;
				POI max_vis;
				ArrayList<POI> opt_seq = new ArrayList<>();
				POI candidate = stack.pop();// first candidate
				double time_left = t - Computation.EuDiRound(start, candidate);
				ArrayList<POI> visitors = candidate.visitors.get((int) time_left);
				HashMap<POI, Integer> map = new HashMap<POI, Integer>();
				max_score = 0;
				max_vis = null;
				opt_seq.clear();
				opt_seq = null;
				if (visitors != null) {// if visitors exist
					for (POI p : visitors) {// for all O(n-1) visitors
						double pers_left = time_left - Computation.EuDiRound(candidate, p);
						if (p.times_paths.get((int) pers_left) != null) {								// path
							added = 0;
							ArrayList<POI> p_sequence = new ArrayList<POI>(p.times_paths.get((int) pers_left));// get
							int bs = p.bound_scores.get((int) pers_left);

							if (p_sequence.contains(candidate)) {
								map.put(p, bs - candidate.getScore());
								added = map.get(p);
								p_sequence.remove(candidate);
							} else {
								map.put(p, bs);// stores best candidate
								added = map.get(p);
							}
							if (added > max_score) {
								max_score = added;
								max_vis = p;
								opt_seq = new ArrayList<POI>(p_sequence);
							}
						} else {// there is no path to extend
							if (p.getScore() > max_score) {
								max_score = p.getScore();
								max_vis = p;
							}
						}
					} //
				}
				candidate.bound_scores.put((int) time_left, candidate.getScore() + max_score);
				ArrayList<POI> path = new ArrayList<POI>();
				if (max_vis != null) {
					path.add(max_vis);
				}
				if (opt_seq != null) {
					for (POI p : opt_seq) {
						path.add(p);
					}
				}
			}
			t++;// increase
		}
		long out = System.nanoTime();
		long time = out - in;
		double seconds = (double) time / 1000000000.0;
		this.time_needed = seconds;
		System.out.println("Flow Search Gradually Time needed: " + seconds);
	}

	public void executeCorrect(boolean print) {
		long in = System.nanoTime();
		int added = 0;
		double t = computeStartingTime();// first feasible solution
		t = Computation.round(t, 0);
		while (t <= Td) {// viewpoint at value t
			Stack<POI> stack = new Stack<POI>();// create stack
			String level = "";
			for (POI p : ComputeFirstLevelsORT(t)) {// add first layer
													// candidates O(n)
				stack.push(p);
				if (print) {
					level = level + "-" + Integer.toString(p.node_id);
				}
			}
			while (!stack.isEmpty()) {// until O(n) solutions have been created
				int max_score = Integer.MIN_VALUE;
				POI max_vis;
				ArrayList<POI> opt_seq = new ArrayList<>();
				POI candidate = stack.pop();// first candidate
				double time_left = t - Computation.EuDiRound(start, candidate);
				ArrayList<POI> visitors = candidate.visitors.get((int) time_left);
				// sort them according the time allowed to spent. min time goes
				// first example 8037 -17
				max_score = 0;
				max_vis = null;
				opt_seq.clear();
				opt_seq = null;
				if (visitors != null) {// if visitors exist
					for (POI p : visitors) {// for all O(n-1) visitors
						double pers_left = time_left - Computation.EuDiRound(candidate, p);
						if (p.times_paths.get((int) pers_left) != null) {									// path
							added = 0;
							ArrayList<POI> p_sequence = new ArrayList<POI>(p.times_paths.get((int) pers_left));// get
							if (p_sequence.contains(candidate)) {
								int position = getPrefix(p_sequence, candidate);
								ArrayList<POI> prefix = new ArrayList<POI>();
								prefix.add(p);// p is by definition different
								if (position != -1) {
									for (int i = 0; i <= position; i++) {
										prefix.add(p_sequence.get(i));
									}
								}
								// amount of time last point of prefix has to
								// spent
								int remain = computeTimeToSpend(candidate, prefix, (int) t);
								// extend prefix
								ArrayList<POI> subopt = pickSubOptimal(prefix.get(prefix.size() - 1), prefix, remain,
										candidate, print);
								if (subopt == null) {
									added = CalcScore(prefix);
									prefix.remove(p);
									p_sequence = new ArrayList<POI>(prefix);
								} else {// there is a path or a poi after
										// prefixing
									
									p_sequence = new ArrayList<POI>();
									for (POI z : prefix) {
										p_sequence.add(z);
									}
									for (POI z : subopt) {
										p_sequence.add(z);
									}
									added = CalcScore(p_sequence);
									p_sequence.remove(p);
								}

								// no prefixing
							} else {// exact bound
								int bs = p.bound_scores.get((int) pers_left);
								added = bs;
							}
							if (added > max_score) {
								max_score = added;
								max_vis = p;
								opt_seq = new ArrayList<POI>(p_sequence);
							}
						} else {// there is no path to extend
							if (p.getScore() > max_score) {
								max_score = p.getScore();
								max_vis = p;
							}
						}
					} 
				}
				candidate.bound_scores.put((int) time_left, candidate.getScore() + max_score);
				ArrayList<POI> path = new ArrayList<POI>();
				if (max_vis != null) {
					path.add(max_vis);
				}
				if (opt_seq != null) {
					for (POI p : opt_seq) {
						path.add(p);
					}
				}
				candidate.times_paths.put((int) time_left, path);
			}
			t++;
		}
		long out = System.nanoTime();
		long time = out - in;
		double seconds = (double) time / 1000000000.0;
		this.time_needed = seconds;
		System.out.println("Flow Search Gradually Time needed: " + time_needed);
		// printData();
	}

	// The point after the prefix contained the root. This means that the last
	// point of the prefix is allowed to repick
	public ArrayList<POI> pickSubOptimal(POI last, ArrayList<POI> prefix, int time, POI root, boolean print) {
		ArrayList<POI> opt_seq = null;// seq to be returned6+
		
		if (last.visitors.get(time) != null) {// has to be different
			ArrayList<POI> visitors = new ArrayList<POI>(last.visitors.get(time));																	// visitors
			if (visitors.contains(root)) {
				visitors.remove(root);// remove root now allowed to repick
			}
			int max_score = Integer.MIN_VALUE;
			for (POI p : visitors) {// repick
				if (!prefix.contains(p)) {
					double p_left = time - Computation.EuDiRound(last, p);
					if (p.times_paths.get((int) p_left) != null) {
						int score = 0;
						ArrayList<POI> p_seq = new ArrayList<POI>(p.times_paths.get((int) p_left));// get
						if (p_seq.contains(root)) {
							p_seq.remove(root);
						}
						for (POI z : prefix) {
							if (p_seq.contains(z)) {
								p_seq.remove(z);
							}
						}
						p_seq.add(0, p);
						int new_left = (int) p_left - run(p_seq);
						System.out.println(new_left);
						ArrayList<POI> extension = new ArrayList<POI>();
						if (p_seq.get(p_seq.size() - 1).visitors.get(new_left) != null) {
							ArrayList<POI> extended_visitors = new ArrayList<POI>(
									p_seq.get(p_seq.size() - 1).visitors.get(new_left));
							extended_visitors = refresh(prefix, p_seq, extended_visitors);													// duplicates
							if (extended_visitors.contains(root)) {
								extended_visitors.remove(root);
							}
							if (!extended_visitors.isEmpty()) {
								for (POI poi : extended_visitors) {
									extension = extend(Union(prefix, p_seq),
											new_left - (int) Computation.EuDiRound(p_seq.get(p_seq.size() - 1), poi),
											poi, root);
								}
							}
						}
						p_seq = Union(p_seq, extension);
						score = CalcScore(p_seq);
						if (print) {
							System.out.println(
									"		 The score is : " + score + "seq ; " + Computation.ListToString(p_seq));
						}
						if (score > max_score) {
							max_score = score;
							opt_seq = new ArrayList<POI>(p_seq);
						}
					} else {// no path

					}
				}
			}

		}
		return opt_seq;
	}

	public ArrayList<POI> Union(ArrayList<POI> A, ArrayList<POI> B) {
		ArrayList<POI> give = new ArrayList<POI>();
		for (POI p : A) {
			give.add(p);
		}
		for (POI p : B) {
			give.add(p);
		}
		return give;
	}

	public ArrayList<POI> extend(ArrayList<POI> tabu, int time, POI p, POI root) {
		ArrayList<POI> path = new ArrayList<POI>();
		if (p.times_paths.get(time) == null) {// no path to return
			System.out.println("Yes");
			path.add(p);
		} else {
			path = new ArrayList<POI>(p.times_paths.get(time));
			path.add(0, p);
			if (path.contains(root)) {
				path.remove(root);
			}
			for (POI z : tabu) {
				if (path.contains(z)) {
					path.remove(z);
				}
			}
		}

		return path;
	}

	public ArrayList<POI> refresh(ArrayList<POI> prefix, ArrayList<POI> p_seq, ArrayList<POI> extended_visitors) {
		for (POI p : prefix) {
			if (extended_visitors.contains(p)) {
				extended_visitors.remove(p);
			}
		}
		for (POI p : p_seq) {
			if (extended_visitors.contains(p)) {
				extended_visitors.remove(p);
			}
		}
		return extended_visitors;
	}

	public int run(ArrayList<POI> seq) {
		double dist = 0;
		for (int i = 0; i < seq.size() - 1; i++) {
			dist = dist + Computation.EuDiRound(seq.get(i), seq.get(i + 1));
		}
		return (int) dist;
	}

	// calculates the distance from initial hotel to candidate and then
	// concatenates the sequence.
	// Returns the diferrenece between t and the length of this partial path
	public int computeTimeToSpend(POI candidate, ArrayList<POI> sequence, int t) {
		int length = (int) Computation.EuDiRound(start, candidate);
		length = length + (int) Computation.EuDiRound(candidate, sequence.get(0));
		for (int j = 1; j <= sequence.size() - 1; j++) {
			length = length + (int) Computation.EuDiRound(sequence.get(j - 1), sequence.get(j));
		}
		int remain = (int) t - length;
		return remain;
	}

	public static double getTripPathLength(Hotel start, Hotel end, ArrayList<POI> sequence) {
		double length = Computation.EuDiRound(start, sequence.get(0));
		for (int j = 1; j <= sequence.size() - 1; j++) {
			length = length + Computation.EuDiRound(sequence.get(j - 1), sequence.get(j));
		}
		length = length + Computation.EuDiRound(sequence.get(sequence.size() - 1), end);
		return length;
	}

	public static double getRealPathLength(Hotel start, Hotel end, ArrayList<POI> sequence) {
		double length = Computation.EuDi(start, sequence.get(0));
		for (int j = 1; j <= sequence.size() - 1; j++) {
			length = length + Computation.EuDi(sequence.get(j - 1), sequence.get(j));
		}
		length = length + Computation.EuDi(sequence.get(sequence.size() - 1), end);
		return length;
	}

	// computes the first level of the search tree O(n) size
	public ArrayList<POI> ComputeFirstLevel(double t) {
		ArrayList<POI> first_level = new ArrayList<POI>();
		for (POI p : POI.getPoi_population()) {
			if (Computation.EuDiRound(start, p) + Computation.EuDiRound(p, end) <= t) {
				first_level.add(p);
			}
		}
		return first_level;
	}

	// computes the first level of the search tree O(n) size
	public ArrayList<POI> ComputeFirstLevelsORT(double t) {
		PriorityQueue<FirstLevelPair> pq = new PriorityQueue<FirstLevelPair>();
		ArrayList<POI> first_level = new ArrayList<POI>();
		ArrayList<POI> util = new ArrayList<POI>();
		for (POI p : POI.getPoi_population()) {
			if (Computation.EuDiRound(start, p) + Computation.EuDiRound(p, end) <= t) {
				double l = t - Computation.EuDiRound(start, p);
				pq.add(new FirstLevelPair(p, (int) l));
			}
		}
		while (!pq.isEmpty()) {
			util.add(pq.remove().node);
		}
		for (int j = util.size() - 1; j >= 0; j--) {
			first_level.add(util.get(j));
		}
		return first_level;
	}

	public static int CalcScore(ArrayList<POI> list) {
		if (list == null) {
			return 0;
		} else {
			int score = 0;
			for (POI p : list) {
				score = score + p.getScore();
			}
			return score;
		}

	}

	public static int getPrefix(ArrayList<POI> sequence, POI p) {
		int position = 0;
		for (int i = 0; i < sequence.size(); i++) {
			if (sequence.get(i).node_id == p.node_id) {
				position = i - 1;
			}
		}
		return position;
	}

	public void printbesties() {
		ArrayList<POI> opt = new ArrayList<POI>();
		int max_score = Integer.MIN_VALUE;
		System.out.println("-------------------------------------------------------------------");
		System.out.println("Time asked: " + Td);
		for (POI p : POI.getPoi_population()) {
			double distance = Computation.EuDiRound(start, p);
			double value = Td - distance;
			if (p.times_paths.get((int) value) != null) {
				ArrayList<POI> list = p.times_paths.get((int) value);
				list.add(0, p);
				if (CalcScore(list) > max_score) {
					max_score = CalcScore(list);
					opt = new ArrayList<POI>(list);
				}
				System.out.println("Node: " + p.node_id + " Seq: " + Computation.ListToString(list) + " Score: "
						+ CalcScore(list) + " Length: " + getTripPathLength(start, end, list) + " Real Length: "
						+ getRealPathLength(start, end, list));
			}
		}
		System.out.println("--------------------------------------------------------------------");
		System.out.println("Optimal: " + Computation.ListToString(opt) + " Score: " + max_score + " Length: "
				+ getTripPathLength(start, end, opt) + " Real Length: " + getRealPathLength(start, end, opt));
	}

	private class FirstLevelPair implements Comparable<FirstLevelPair> {

		public final POI node;
		public final int value;

		public FirstLevelPair(POI node, int value) {
			this.node = node;
			this.value = value;
		}

		public int compareTo(FirstLevelPair other) {
			// check distances first
			if (value < other.value)
				return -1;
			else if (value > other.value)
				return 1;
			else if (value < other.value) {
				return 1;
			} else if (value > other.value) {
				return -1;
			} else
				return 0;
		}

	}

}
