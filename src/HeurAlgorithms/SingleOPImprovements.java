package HeurAlgorithms;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.TreeMap;
import Computabillity.Computation;
import OPHSAttributes.Hotel;
import OPHSAttributes.POI;

public class SingleOPImprovements {
	private static ArrayList<ArrayList<POI>> optimal = new ArrayList<ArrayList<POI>>();
	private ArrayList<Double> calls = new ArrayList<Double>();
	private TreeMap<Double, ArrayList<POI>> start_visitors;
	private Hotel start;
	private Hotel end;
	private double Td;
	private double time_needed;
	private double checker = 0;
	private double last_call = 0;
	private ArrayList<POI> candidates;
	private final double step_size;

	public SingleOPImprovements(Hotel start, Hotel finish, double Td) {
		this.start = start;
		this.end = finish;
		this.Td = Td;
		this.checker = Td + 0.1;
		this.start_visitors = new TreeMap<Double, ArrayList<POI>>();
		this.candidates = new ArrayList<POI>(POI.getPoi_population());
		this.step_size = 0.1;
		System.out.println("Start Hotel: " + start.node_id + " Finish Hotel: " + end.node_id + " time budget: " + Td);
		Preprocess();

		optimal.clear();
	}

	public void refresh() {
		optimal.clear();
		start_visitors.clear();
		for (POI p : candidates) {
			p.visitors.clear();
			p.D_bound_scores.clear();
			p.D_times_paths.clear();
		}
		candidates.clear();
	}

	public void Preprocess() {
		for (POI p : candidates) {
			p.ComputeVisitorsIntegersDecimals(Td, this.end);
			p.ComputeMinDistDecimals(this.end);
			p.preprocessBoundsDecimals();
			p.preprocessPathsDecimals();
		}
	}

	// extracts the first value of Td for which we have a feasible solution
	public double computeStartingTime() {
		double min = Double.MAX_VALUE;
		for (POI p : candidates) {// for all pois
			if (Computation.EuDi(start, p) + Computation.EuDi(p, end) < min) {
				min = Computation.EuDi(start, p) + Computation.EuDi(p, end);
			}
		}
		return min;
	}

	/**
	 * Implements the bounding algorithm. Increases step gradually and solves N sub-orient problems
	 * @param print
	 */
	public void eXecute(boolean print) {
		int reduce = 0;
		long in = System.nanoTime();
		int added = 0;
		double t = computeStartingTime();// first feasible solution
		t = Computation.roundAndPrepare(t, 2);
		double step = step_size;
		boolean control = true;
		while (t <= checker && control) {// viewpoint at value t
			Stack<POI> stack = new Stack<POI>();// create stack
			String level = "";
			for (POI p : ComputeFirstLevelsORT(t)) {
				stack.push(p);
			}
			while (!stack.isEmpty()) {// until O(n) solutions have been created
				int max_score = Integer.MIN_VALUE;
				POI max_vis;
				ArrayList<POI> opt_seq = new ArrayList<>();
				POI candidate = stack.pop();// first candidate
				double time_left = t - Computation.EuDi(start, candidate);
				ArrayList<POI> visitors = candidate.getVisitors(time_left, end);
				max_score = 0;
				max_vis = null;
				opt_seq.clear();
				opt_seq = null;
				if (visitors != null) {// if visitors exist
					for (POI p : visitors) {// for all O(n-1) visitors
						double pers_left = time_left - Computation.EuDi(candidate, p);
						if (p.getPath(pers_left) != null) {// if there is a path
							added = 0;
							ArrayList<POI> p_sequence = new ArrayList<POI>(p.getPath(pers_left));// get
							if (p_sequence.contains(candidate)) {
								reduce++;
								ArrayList<POI> prefix = new ArrayList<POI>();
								prefix.add(p);
								double remain = computeTimeToSpend(candidate, prefix, t);
								ArrayList<POI> subopt = pickSubOptimal(prefix.get(prefix.size() - 1), prefix, remain,
										candidate, print);
								if (subopt == null) {
									added = CalcScore(prefix);
									prefix.remove(p);
									p_sequence = new ArrayList<POI>(prefix);
								} else {
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
							} else {// exact bound
								int bs = p.getBound(pers_left);
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
				candidate.D_bound_scores.put(time_left, candidate.getScore() + max_score);
				ArrayList<POI> path = new ArrayList<POI>();
				if (max_vis != null) {
					path.add(max_vis);
				}
				if (opt_seq != null) {
					for (POI p : opt_seq) {
						path.add(p);
					}
				}
				candidate.D_times_paths.put(time_left, path);
			}
			calls.add(t);
			last_call = t;
			t = t + step;// increase
			if (t > checker) {
				control = false;
			}
		}
		long out = System.nanoTime();
		long time = out - in;
		double seconds = (double) time / 1000000000.0;
		this.time_needed = seconds;
		System.out.println("Flow Search Gradually Time needed: " + time_needed + " " + reduce);
	}
	// The point after the prefix contained the root. This means that the last
	// point of the prefix is allowed to repick

	public ArrayList<POI> pickSubOptimal(POI last, ArrayList<POI> prefix, double time, POI root, boolean print) {
		ArrayList<POI> opt_seq = null;// seq to be returned6+
		if (last.getVisitors(time, end) != null) {// has to be different
			ArrayList<POI> visitors = new ArrayList<POI>(last.getVisitors(time, end));// copy																		// visitors
			if (visitors.contains(root)) {
				visitors.remove(root);// remove root now allowed to repick
			}
			int max_score = Integer.MIN_VALUE;
			for (POI p : visitors) {// repick
				if (!prefix.contains(p)) {
					double p_left = time - Computation.EuDi(last, p);
					if (p.getVisitors(p_left, end) != null) {// if p has a path
																// to submit
						int score = 0;
						ArrayList<POI> p_seq = new ArrayList<POI>();
						if (p.getPath(p_left) != null) {
							p_seq = new ArrayList<POI>(p.getPath(p_left));
							if (print) {
								System.out.println("	       Visitor: " + p.node_id + " with time: " + p_left
										+ " seq: " + Computation.ListToString(p_seq));
							}
							if (p_seq.contains(root)) {
								p_seq.remove(root);
							}
							for (POI z : prefix) {
								if (p_seq.contains(z)) {
									p_seq.remove(z);
								}
							}
						}
						p_seq.add(0, p);
						double new_left = p_left - run(p_seq);
						ArrayList<POI> extension = new ArrayList<POI>();
						if (p_seq.get(p_seq.size() - 1).getVisitors(new_left, end) != null) {// last
							ArrayList<POI> extended_visitors = new ArrayList<POI>(
									p_seq.get(p_seq.size() - 1).getVisitors(new_left, end));
							extended_visitors = refresh(prefix, p_seq, extended_visitors);															// duplicates
							if (extended_visitors.contains(root)) {
								extended_visitors.remove(root);
							}
							if (!extended_visitors.isEmpty()) {
								for (POI poi : extended_visitors) {
									extension = extendDecimals(Union(prefix, p_seq),
											new_left - Computation.EuDi(p_seq.get(p_seq.size() - 1), poi), poi, root);
									
								}
							}
						}
						p_seq = Union(p_seq, extension);
						score = CalcScore(p_seq);
						
						if (score > max_score) {
							max_score = score;
							opt_seq = new ArrayList<POI>(p_seq);
						}
					} else {// no path
						if (p.getScore() > max_score) {
							max_score = p.getScore();
							opt_seq = new ArrayList<POI>();
							opt_seq.add(p);
						}
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

	public ArrayList<POI> extendDecimals(ArrayList<POI> tabu, double time, POI p, POI root) {

		ArrayList<POI> path = new ArrayList<POI>();
		if (p.getPath(time) == null) {// no path to return
			path.add(p);
		} else {
			path = new ArrayList<POI>(p.getPath(time));
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

	public double extractCallLimit(double limit) {
		double l = 0;
		for (double call : calls) {
			if (Computation.accurateRound(call, 2) == Computation.accurateRound(limit, 2)) {
				l = call;
			}
		}
		return l;
	}

	public double run(ArrayList<POI> seq) {
		double dist = 0;
		for (int i = 0; i < seq.size() - 1; i++) {
			dist = dist + Computation.EuDi(seq.get(i), seq.get(i + 1));
		}
		return dist;
	}

	// calculates the distance from initial hotel to candidate and then
	// concatenates the sequence.
	// Returns the diferrenece between t and the length of this partial path
	public double computeTimeToSpend(POI candidate, ArrayList<POI> sequence, double t) {
		double length = Computation.EuDi(start, candidate);
		length = length + Computation.EuDi(candidate, sequence.get(0));
		for (int j = 1; j <= sequence.size() - 1; j++) {
			length = length + Computation.EuDi(sequence.get(j - 1), sequence.get(j));
		}
		double remain = t - length;
		return remain;
	}

	public static double getRealPathLength(Hotel start, Hotel end, ArrayList<POI> sequence) {
		double length = 0;
		if (sequence != null) {
			if (!sequence.isEmpty()) {
				length = Computation.EuDi(start, sequence.get(0));
				for (int j = 1; j <= sequence.size() - 1; j++) {
					length = length + Computation.EuDi(sequence.get(j - 1), sequence.get(j));
				}
				length = length + Computation.EuDi(sequence.get(sequence.size() - 1), end);
			}
		}
		return length;
	}

	// computes the first level of the search tree O(n) size
	public ArrayList<POI> ComputeFirstLevelsORT(double t) {
		PriorityQueue<FirstLevelPair> pq = new PriorityQueue<FirstLevelPair>();
		ArrayList<POI> first_level = new ArrayList<POI>();
		ArrayList<POI> util = new ArrayList<POI>();
		for (POI p : candidates) {
			if (Computation.EuDi(start, p) + Computation.EuDi(p, end) <= t) {
				double l = t - Computation.EuDi(start, p);
				pq.add(new FirstLevelPair(p, l));
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
		System.out.println("Time asked: " + last_call);
		for (POI p : candidates) {
			double distance = Computation.EuDi(start, p);
			double value = last_call - distance;
			if (p.D_times_paths.get(value) != null) {
				ArrayList<POI> list = p.D_times_paths.get(value);
				list.add(0, p);
				if (CalcScore(list) > max_score) {
					max_score = CalcScore(list);
					opt = new ArrayList<POI>(list);
				}
				System.out.println("Node: " + p.node_id + " Seq: " + Computation.ListToString(list) + " Score: "
						+ CalcScore(list) + " Real Length: " + getRealPathLength(start, end, list));
			}
		}
		System.out.println("--------------------------------------------------------------------");
		System.out.println("Optimal: " + Computation.ListToString(opt) + " Score: " + max_score + " Real Length: "
				+ getRealPathLength(start, end, opt));
	}

	public void printbesties(double t) {
		ArrayList<POI> opt = new ArrayList<POI>();
		int max_score = Integer.MIN_VALUE;
		System.out.println("-------------------------------------------------------------------");
		System.out.println("Time asked: " + t);
		for (POI p : candidates) {
			double distance = Computation.EuDi(start, p);
			double value = t - distance;
			if (p.D_times_paths.get(value) != null) {
				ArrayList<POI> list = p.D_times_paths.get(value);
				list.add(0, p);
				if (CalcScore(list) > max_score) {
					max_score = CalcScore(list);
					opt = new ArrayList<POI>(list);
				}
				System.out.println("Node: " + p.node_id + " Seq: " + Computation.ListToString(list) + " Score: "
						+ CalcScore(list) + " Real Length: " + getRealPathLength(start, end, list));
			}
		}
		System.out.println("--------------------------------------------------------------------");
		System.out.println("Optimal: " + Computation.ListToString(opt) + " Score: " + max_score + " Real Length: "
				+ getRealPathLength(start, end, opt));
	}

	public ArrayList<POI> extractBest() {
		ArrayList<POI> opt = new ArrayList<POI>();
		int max_score = Integer.MIN_VALUE;
		for (POI p : candidates) {
			double distance = Computation.EuDi(start, p);
			double value = last_call - distance;
			if (p.D_times_paths.get(value) != null) {
				ArrayList<POI> list = p.D_times_paths.get(value);
				list.add(0, p);
				if (CalcScore(list) > max_score) {
					max_score = CalcScore(list);
					opt = new ArrayList<POI>(list);
				}
			}
		}
		return opt;
	}
	/**
	 * Extracts the best solution for an intermediate value of time 
	 * @param t denotes the time
	 * @return
	 */
	public ArrayList<POI> extractBest(double t) {
		ArrayList<POI> opt = new ArrayList<POI>();
		int max_score = Integer.MIN_VALUE;
		for (POI p : candidates) {
			double distance = Computation.EuDi(start, p);
			double value = t - distance;
			if (p.D_times_paths.get(value) != null) {
				ArrayList<POI> list = p.D_times_paths.get(value);
				list.add(0, p);
				if (CalcScore(list) > max_score) {
					max_score = CalcScore(list);
					opt = new ArrayList<POI>(list);
				}
			}
		}
		return opt;
	}

	private class FirstLevelPair implements Comparable<FirstLevelPair> {

		public final POI node;
		public final double value;

		public FirstLevelPair(POI node, double value) {
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
