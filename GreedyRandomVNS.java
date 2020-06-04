package Examples;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import java.util.Random;

/**
 * This class presents an example hyper-heuristic demonstrates basic functionality. 
 * it first determines how many low level heuristics exist for the problem domain to be solved.
 * it applies a random low level hyper-heuristic to the current solution.
 * if there is an improvement in solution quality the new solution is accepted.
 * if the new solution is worse than the current solution, it is accepted with a 50% probability.
 * <p>
 * the easiest way to create your own strategy is to modify the solve method of this class.
 * <p>
 * we suggest that the reader goes through the example code of this class, and then reads the 
 * notes below, which provide further clarification.
 * <p>
 * It is important to note that every call to the hasTimeExpired() method records the best fitness function value found
 * so far by the hyper-heuristic. This is a mechanism which ensures that only solutions found within the time limit are 
 * used for scoring, and it is intended to ensure that there is NO BENEFIT to exceeding the time limit.
 * Every time a low level heuristic is applied with ProblemDomain.applyHeuristic(), the ProblemDomain
 * object updates the "best fitness found so far". However, this is ONLY recorded for scoring purposes when the 
 * hasTimeExpired() method is called. For this reason it is beneficial to put as many hasTimeExpired() checks into your
 * hyper-heuristic code as possible. Ideally, for many hyper-heuristics, this will be after every application of a 
 * low level heuristic, rather than at the end of each iteration (every iteration may contain multiple low level 
 * heuristic applications).
 * 
 * In the extreme case, if there are no calls to the hasTimeExpired() method, then the hyper-heuristic will iterate for as
 * long as the user wants. It could obtain a much better solution than the other competitors by using much more time. However,
 * this solution will never be recorded for scoring purposes because the hasTimeExpired() method was never called. While we 
 * have taken these reasonable steps to negate any advantage from exceeding the time limit, we admit that we may not have
 * thought of everything! Therefore we issue this warning: attempting to obtain advantage by exceeding the time limit is 
 * definitely not in the spirit of the competiton, and will result in disqualification.
 * <p>
 * we only initialise the solution at index 0 in the memory,
 * we do not need to initialise the solution at index 1 because a solution
 * is subequently created and placed there when the randomly selected low level heuristic is applied.
 * the solution at index 0 needs to be initialised because the chosen 
 * heuristic must be applied to it. if it is not initialised then there would be nothing to modify.
 * <p>
 * using two indices of memory is the standard method to handle a single point search.
 * it is not the only way however. in this example we treat 0 as the current solution,
 * and a new candidate solution is stored temporarily at index 1. This is just the
 * method by which we choose to handle the solutions, but it is up to the user to manage the solution memory.
 * there is technically no reason why we cannot set the memory size to ten, and only use indices 9 and 10.
 */

public class GreedyRandomVNS extends HyperHeuristic {
	
	/**
	 * creates a new ExampleHyperHeuristic object with a random seed
	 */
	public GreedyRandomVNS(long seed){
		super(seed);
	}
	
	/**
	 * This method defines the strategy of the hyper-heuristic
	 * @param problem the problem domain to be solved
	 */
	public void solve(ProblemDomain problem) {
		int[] local_search_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
		//int[] mutation_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
		int[] crossover_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
		
		int solutionmemorysize = 10;
		problem.setMemorySize(solutionmemorysize);
		double[] current_obj_function_values = new double[solutionmemorysize];
		int best_solution_index = 0;
		double best_solution_value = Double.POSITIVE_INFINITY;
		for (int x = 0; x < solutionmemorysize; x++) {
			problem.initialiseSolution(x);
			current_obj_function_values[x] = problem.getFunctionValue(x);
			if (current_obj_function_values[x] < best_solution_value) {
				best_solution_value = current_obj_function_values[x];
				best_solution_index = x;
			}
		}
		
		while (!hasTimeExpired()) {
			//start
			
			//proses shaking
			for (int x = 0; x < solutionmemorysize; x++) {
				Random rand = new Random();
				int index_rand = rand.nextInt((2 - 1) + 1) + 1;
				if(index_rand == 1) {
					Random rand2 = new Random();
					int i = rand2.nextInt(10);
					best_solution_value = current_obj_function_values[i];
					best_solution_index = i;
				} else {
					Random rand3 = new Random();
					int j = rand3.nextInt(((9 - x + 1) - 1) + 1) + 1;
					if (index_rand == 1) {
						best_solution_value = current_obj_function_values[j];
						best_solution_index = j;
					} else {
						if (crossover_heuristics != null) {
							int solution_index_1 = rng.nextInt(solutionmemorysize);
							int solution_index_2 = rng.nextInt(solutionmemorysize);
							while (true) {
								if (solution_index_1 == solution_index_2) {
									solution_index_2 = rng.nextInt(solutionmemorysize);
								} else {
									break;
								}
							}
							int heuristic_to_apply = crossover_heuristics[rng.nextInt(crossover_heuristics.length)];
							current_obj_function_values[solution_index_1] = problem.applyHeuristic(heuristic_to_apply, solution_index_1, solution_index_2, solution_index_1);
							if (solution_index_1 == best_solution_index) {
								if (current_obj_function_values[solution_index_1] > best_solution_value) {
									best_solution_value = Double.POSITIVE_INFINITY;
									for (int z = 0; z < solutionmemorysize; z++) {
										if (current_obj_function_values[z] < best_solution_value) {
											best_solution_value = current_obj_function_values[z];
											best_solution_index = z;
										}
									}
								}
							} else if (current_obj_function_values[solution_index_1] < best_solution_value) {
								best_solution_value = current_obj_function_values[solution_index_1];
								best_solution_index = solution_index_1;
							}
						}
					}
				}
			}
			
			//proses best improvement (VNS) menggunakan local search pada hyflex
			int heuristic_to_apply = 0;
			if (local_search_heuristics != null) {
				heuristic_to_apply = local_search_heuristics[rng.nextInt(local_search_heuristics.length)];
			} else {
				heuristic_to_apply = rng.nextInt(problem.getNumberOfHeuristics());
			}
			current_obj_function_values[best_solution_index] = problem.applyHeuristic(heuristic_to_apply, best_solution_index, best_solution_index);
			
			//proses neighbourhood change
			if (current_obj_function_values[best_solution_index] < best_solution_value) {
				best_solution_value = current_obj_function_values[best_solution_index];
				for (int x = 0; x < solutionmemorysize; x++) {
					if (current_obj_function_values[x] < best_solution_value) {
						best_solution_value = current_obj_function_values[x];
						best_solution_index = x;
					}
				}
			}
			//end
		}
	}
	
	/**
	 * this method must be implemented, to provide a different name for each hyper-heuristic
	 * @return a string representing the name of the hyper-heuristic
	 */
	public String toString() {
		return "Greedy Variable Neighborhood Search";
	}
}
