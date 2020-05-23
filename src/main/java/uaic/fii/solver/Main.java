package uaic.fii.solver;

import gurobi.GRBException;
import uaic.fii.model.Node;
import uaic.fii.model.Route;
import uaic.fii.model.Solution;
import uaic.fii.solver.exact.EVRPTWModel;
import uaic.fii.model.EVRPTWInstance;
import uaic.fii.solver.exact.GurobiModel;
import uaic.fii.solver.ga.GeneticAlgorithm;
import uaic.fii.solver.ga.search.TabuSearch;
import uaic.fii.solver.greedy.*;
import uaic.fii.solver.verifier.SchneiderLoader;
import uaic.fii.util.Algorithm;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {

    private static  List<String> instances = Arrays.asList(
        "c101C5.txt", "c103C5.txt", "c206C5.txt", "c208C5.txt", "r104C5.txt", "r105C5.txt", "r202C5.txt", "r203C5.txt", "rc105C5.txt", "rc108C5.txt", "rc204C5.txt", "rc208C5.txt",
          /*  "c101C10.txt", */ "c104C10.txt", "c202C10.txt", "c205C10.txt", "rc205C10.txt"
            , "c101_21.txt", "c102_21.txt", "c103_21.txt", "c104_21.txt", "c105_21.txt"
            , "r101_21.txt", "r102_21.txt", "r103_21.txt", "r104_21.txt", "r105_21.txt"
            , "rc101_21.txt", "rc102_21.txt", "rc103_21.txt", "rc104_21.txt", "rc105_21.txt"
    );

    private static boolean isSolved(Algorithm algorithm, String instance) {
        String base = "C:\\Users\\BogdanBo\\Documents\\Facultate\\Dizertatie\\src\\main\\resources\\solution";
        File solution = new File(String.join(File.separator, base, algorithm.getSaveLocation(), instance));
        return solution.exists();
    }

    public static void runGeneticAlgorithm() {
        instances = Arrays.asList(
                "c101C5.txt",
                "c103C5.txt",
                "c206C5.txt",
                "c208C5.txt",
                "r105C5.txt",
                "r202C5.txt",
                "rc105C5.txt",
                "rc208C5.txt"
        );
        for (String instance : instances) {
            System.out.println(String.format("Executing %s instance", instance));
            if (isSolved(Algorithm.GA, instance)) {
                System.out.println(String.format("Skipping %s instance because it was already solved!", instance));
                continue;
            }
            EVRPTWInstance evrptwInstance;
            try {
                evrptwInstance = new SchneiderLoader().load(new File(String.join(File.separator, "input", instance)));
                GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(evrptwInstance);
                geneticAlgorithm.run();
                Solution solution = geneticAlgorithm.getSolution();
                solution.saveToFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void runExactAlgorithm() {
        instances = Arrays.asList(
                "c101C5.txt",
                "c103C5.txt",
                "c206C5.txt",
                "c208C5.txt",
                "r105C5.txt",
                "r202C5.txt",
                "rc105C5.txt",
                "rc208C5.txt",

                "c101C10.txt",
                "c104C10.txt",
                "c202C10.txt",
                "c205C10.txt",
                "r201C10.txt",
                "r203C10.txt",
                "rc102C10.txt",
                "rc108C10.txt",

                "c103C15.txt",
                "c106C15.txt",
                "c202C15.txt",
                "c208C15.txt",
                "r209C15.txt",
                "r202C15.txt",
                "rc103C15.txt",
                "rc202C15.txt"
        );
        for (String instance : instances) {
            if (isSolved(Algorithm.EXACT, instance)) {
                System.out.println(String.format("Skipping %s instance because it was already solved!", instance));
                continue;
            }

            System.out.println(String.format("Executing %s instance", instance));

            EVRPTWInstance evrptwInstance;
            try {
                evrptwInstance = new SchneiderLoader().load(new File(String.join(File.separator, "input", instance)));
                GurobiModel model = new EVRPTWModel(evrptwInstance);
                Solution solution = model.solve();
                solution.saveToFile();
            } catch (IOException | GRBException e) {
                e.printStackTrace();
            }
        }
    }

    public static void runGreedyAlgorithm(Algorithm algorithm) {
        instances = Arrays.asList(
                "c101C5.txt",
                "c103C5.txt",
                "c206C5.txt",
                "c208C5.txt",
                "r105C5.txt",
                "r202C5.txt",
                "rc105C5.txt",
                "rc208C5.txt"
        );

            for (String instance : instances) {
                if (isSolved(algorithm, instance)) {
                    System.out.println(String.format("Skipping %s instance because it was already solved!", instance));
                    continue;
                }

                System.out.println(String.format("Executing %s instance", instance));
                EVRPTWInstance evrptwInstance;
                try {
                    evrptwInstance = new SchneiderLoader().load(new File(String.join(File.separator, "input", instance)));

                    GiantRouteConstructionStrategy strategy;
                    if (algorithm == Algorithm.KNN_MIN_DUE_TIME) {
                        strategy = new KNearestNeighborsMinEndTime(evrptwInstance);
                    } else if (algorithm == Algorithm.KNN_MIN_READY_TIME) {
                        strategy = new KNearestNeighborsMinReadyTime(evrptwInstance);
                    } else if (algorithm == Algorithm.NN_MIN_DUE_TIME) {
                        strategy = new NearestNeighborMinEndTime(evrptwInstance);
                    } else {
                        strategy = new NearestNeighborMinStartTime(evrptwInstance);
                    }
                    BeasleyHeuristic heuristic = new BeasleyHeuristic(evrptwInstance, strategy);
                    List<Route> routes = heuristic.solve();
                    Solution solution = new Solution(evrptwInstance, algorithm, routes);
                    solution.saveToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    public static void main(String[] args) {

        // runExactAlgorithm();
        runGeneticAlgorithm();
        //File instanceFile = new File("input/c101C5.txt");
        // File solutionFile = new File("C:\\Users\\BogdanBo\\Documents\\Facultate\\Dizertatie\\src\\main\\resources\\solution\\exact\\c101C5.txt");
       /* EVRPTWInstance instance;
        try {
            instance = new SchneiderLoader().load(instanceFile);
            BeasleyHeuristic heuristic = new BeasleyHeuristic(instance, new KNearestNeighborsMinReadyTime(instance));

            List<Route> routes = heuristic.solve();
            Solution solution = new Solution(instance, Algorithm.GREEDY, routes);

            System.out.println("Before tabu search");
            for (Route route : routes) {
                System.out.println(route + " " + route.isFeasible() + " " + route.isComplete());
            }

            System.out.println(routes.stream().mapToDouble(Route::getTotalDistance).sum());

            System.out.println("After tabu search");
            TabuSearch tabuSearch = new TabuSearch(instance);
            Solution improved = tabuSearch.execute(solution);

            for (Route route : improved.getRoutes()) {
                route.getNodes()
                        .forEach(e -> {
                            System.out.print(instance.getById(e.id) + " ");
                        });
                System.out.println(route.isComplete());
                System.out.println();
            }

            System.out.println(improved.getRoutes().stream().mapToDouble(Route::getTotalDistance).sum());

            long count = improved.getRoutes().stream()
                    .map(Route::getNodes)
                    .flatMap(Collection::stream)
                    .filter(instance::isCustomer)
                    .count();*/

            /* GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(instance);
            geneticAlgorithm.run();
            Solution solution = geneticAlgorithm.getSolution();

            System.out.println(solution.getCost());

            TabuSearch tabuSearch = new TabuSearch(instance);
            Solution improved = tabuSearch.execute(solution);

            System.out.println(improved.getCost());*/
        //    model.solve();

      /*  } catch(IOException e) {
            e.printStackTrace();
        }*/ /* catch (GRBException e) {
            e.printStackTrace();
        }*/


    }
}
