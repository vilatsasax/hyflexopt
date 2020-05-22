package Examples;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

/**
 * This class presents an example hyper-heuristic demonstrates basic functionality. 
 */

public class GreedyVariableNeighborhoodSearch extends HyperHeuristic {
	
	/**
	 * creates a new ExampleHyperHeuristic object with a random seed
	 */
	public GreedyVariableNeighborhoodSearch(long seed){
		super(seed);
	}
	
	/**
	 * This method defines the strategy of the hyper-heuristic
	 * @param problem the problem domain to be solved
	 */
	public void solve(ProblemDomain problem) {

		//ambil angka low level heuristic untuk proses shaking di VNS
		int[] mutation_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
		int[] crossover_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
		
		// start inisialisasi menggunakan greedy algo
		// dimisalkan ada 10 total solusi
		int solutionmemorysize = 10;
		// set size memory sesuai solution memory size
		problem.setMemorySize(solutionmemorysize);
		// membuat array untuk obj function
		double[] current_obj_function_values = new double[solutionmemorysize];
		//simpan index best solution
		int best_solution_index = 0;
		// simpan best solution
		double best_solution_value = Double.POSITIVE_INFINITY;
		// melakukan proses inisialisasi solusi menggunakan greedy randomized
		int k = 1;//ambil opsi solusi pertama
		int subset = 0;
		for (int x = 0; x < solutionmemorysize; x++) {
			if (current_obj_function_values[current_obj_function_values.length - 1] > k) {
				subset = (int) Math.min(solutionmemorysize, current_obj_function_values[current_obj_function_values.length - 1] - k + 1);
			}
			problem.initialiseSolution(subset);
			current_obj_function_values[subset] = problem.getFunctionValue(subset);
			if (current_obj_function_values[subset] < best_solution_value) {
				best_solution_value = current_obj_function_values[subset];
				best_solution_index = x;
			} else {
				best_solution_index = 0;
			}
			k = k + 1;
		}
		// end of greedy

		// proses shaking mengambil contoh dari kelas ExampleHyperHeuristic3 sekaligus memulai Variable Neighborhood Search
		while (!hasTimeExpired()) {
			//parameter
			int k2 = 1;
			//start shaking
			while (k2 < solutionmemorysize) {
				// mengambil angka random
				int rand = getRandom(1, 2);
				if (rand == 1) {
					// mengambil nilai solusi disimpan di variabel i2 dan j2
					int i2 = getRandom(1, solutionmemorysize); 
					int j2 = getRandom(1, solutionmemorysize); 
					
					//ambil contoh mutasi di Example3
					int heuristic_to_apply = 0;
					if (mutation_heuristics != null) {
						heuristic_to_apply = mutation_heuristics[rng.nextInt(mutation_heuristics.length)];
					} else {
						heuristic_to_apply = rng.nextInt(problem.getNumberOfHeuristics());
					}
					
					current_obj_function_values[best_solution_index] = problem.applyHeuristic(heuristic_to_apply, best_solution_index, best_solution_index);
					if (current_obj_function_values[best_solution_index] > best_solution_value) {
						best_solution_value = Double.POSITIVE_INFINITY;
						if (current_obj_function_values[k2] < best_solution_value) {
							best_solution_value = current_obj_function_values[i2];
							best_solution_index = i2;
						} else {
							best_solution_index = 0;
						}
					}
				} else {
					// mengambil nilai solusi disimpan di variabel i2 dan j2
					int i2 = getRandom(1, solutionmemorysize);
					int j2 = getRandom(solutionmemorysize + 1, solutionmemorysize);
					int heuristic_to_apply = 0;
					// mengambil angka random
					int rand2 = getRandom(1, 2);
					if (rand2 == 1) {
						//ambil contoh mutasi di Example3
						if (mutation_heuristics != null) {
							heuristic_to_apply = mutation_heuristics[rng.nextInt(mutation_heuristics.length)];
						} else {
							heuristic_to_apply = rng.nextInt(problem.getNumberOfHeuristics());
						}
						current_obj_function_values[best_solution_index] = problem.applyHeuristic(heuristic_to_apply, best_solution_index, best_solution_index);
						if (current_obj_function_values[best_solution_index] > best_solution_value) {
							best_solution_value = Double.POSITIVE_INFINITY;
							if (current_obj_function_values[i2] < best_solution_value) {
								best_solution_value = current_obj_function_values[i2];
								best_solution_index = i2;
							} else {
								best_solution_index = 0;
							}
						}
					} else {
						// interchange i2 & j2
						if (crossover_heuristics != null) {
							while (true) {
								if (i2 == j2) {
									j2 = rng.nextInt(solutionmemorysize);
								} else {
									heuristic_to_apply = crossover_heuristics[rng.nextInt(crossover_heuristics.length)];
									if(i2 != 0) {
										current_obj_function_values[i2] = problem.applyHeuristic(heuristic_to_apply, i2, j2, i2);
									}
		
									if (i2 == best_solution_index) {
										if (current_obj_function_values[i2] > best_solution_value) {
											best_solution_value = Double.POSITIVE_INFINITY;
											for (int x = 0; x < solutionmemorysize; x++) {
												if (current_obj_function_values[x] < best_solution_value) {
													best_solution_value = current_obj_function_values[x];
													best_solution_index = x;
												}
											}
										}
									} else if (current_obj_function_values[i2] < best_solution_value) {
										best_solution_value = current_obj_function_values[i2];
										best_solution_index = i2;
									}
								}
							}
						}
					}
				}
				k = k - 1;
			} 
			// end of shaking
			
		}
	}
	
	/**
	 * this method must be implemented, to provide a different name for each hyper-heuristic
	 * @return a string representing the name of the hyper-heuristic
	 */
	public String toString() {
		return "Greedy Randomized - Variable Neighborhood Search";
	}
	
	/**
	 * fungsi untuk melakukan greedy randomized algorithm
	 * @return
	 */
	public void greedyRandom(int paramq, ProblemDomain problem, double[] set) {
		int k = 1;
		int i = 0;
		while (i < set.length) {
			int subset = (int) Math.min(paramq, set[set.length - 1] - k + 1);
			problem.initialiseSolution(subset);
			k = k + 1;
			i = i + 1;
		}
	}
	
	/**
	 * fungsi untuk ambil angka acak dari range tertentu
	 */
	private static int getRandom(int min, int max) {
		if (min < max) {
			Random r = new Random();
			return r.nextInt((max - min) + 1) + min;
		} else {
			return 0;
		}
		
	}
}	
