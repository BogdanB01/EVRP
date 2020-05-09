package uaic.fii.solver;

import gurobi.GRBException;
import uaic.fii.model.Node;
import uaic.fii.model.Route;
import uaic.fii.model.Solution;
import uaic.fii.solver.exact.EVRPTWModel;
import uaic.fii.model.EVRPTWInstance;
import uaic.fii.solver.ga.GeneticAlgorithm;
import uaic.fii.solver.ga.search.TabuSearch;
import uaic.fii.solver.greedy.BeasleyHeuristic;
import uaic.fii.solver.greedy.KNearestNeighborsMinEndTime;
import uaic.fii.solver.verifier.RoutesLoader;
import uaic.fii.solver.verifier.SchneiderLoader;
import uaic.fii.util.Algorithm;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        File instanceFile = new File("input/c202C15.txt");
        // File solutionFile = new File("C:\\Users\\BogdanBo\\Documents\\Facultate\\Dizertatie\\src\\main\\resources\\solution\\exact\\c101C5.txt");
        EVRPTWInstance instance;
        try {
            instance = new SchneiderLoader().load(instanceFile);
            BeasleyHeuristic heuristic = new BeasleyHeuristic(instance, new KNearestNeighborsMinEndTime(instance));

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
                    .count();

            System.out.println("Customers in instance " + instance.getNumCustomers());
            System.out.println("Customers in improved " + count);


            /* GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(instance);
            geneticAlgorithm.run();
            Solution solution = geneticAlgorithm.getSolution();

            System.out.println(solution.getCost());

            TabuSearch tabuSearch = new TabuSearch(instance);
            Solution improved = tabuSearch.execute(solution);

            System.out.println(improved.getCost());*/
        //    model.solve();

        } catch(IOException e) {
            e.printStackTrace();
        } /* catch (GRBException e) {
            e.printStackTrace();
        }*/


    }
}
