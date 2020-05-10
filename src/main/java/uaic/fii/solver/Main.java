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
import uaic.fii.solver.greedy.BeasleyHeuristic;
import uaic.fii.solver.greedy.KNearestNeighborsMinEndTime;
import uaic.fii.solver.greedy.KNearestNeighborsMinReadyTime;
import uaic.fii.solver.verifier.SchneiderLoader;
import uaic.fii.util.Algorithm;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Main {



    private static boolean isSolved(File file) {
        String base = "C:\\Users\\BogdanBo\\Documents\\Facultate\\Dizertatie\\src\\main\\resources\\solution\\exact";
        File solution = new File(String.join(File.separator, base, file.getName()));
        return solution.exists();
    }

    public static void runExactAlgorithm() {
        File inputDirectory = new File("input");
        File[] instances = inputDirectory.listFiles();
        for (File file : instances) {
            System.out.println(String.format("Executing %s instance", file.getName()));

            if (isSolved(file)) {
                System.out.println(String.format("Skipping %s instance because it was already solved!", file.getName()));
                continue;
            }

            EVRPTWInstance instance;
            try {
                instance = new SchneiderLoader().load(file);
                GurobiModel model = new EVRPTWModel(instance);
                Solution solution = model.solve();
                solution.saveToFile();
            } catch (IOException | GRBException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        runExactAlgorithm();
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
