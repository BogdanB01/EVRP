package uaic.fii.solver;

import uaic.fii.solver.exact.EVRPTWModel;
import uaic.fii.solver.ga.GeneticAlgorithm;
import uaic.fii.solver.model.EVRPTWInstance;
import uaic.fii.solver.verifier.SchneiderLoader;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        File instanceFile = new File("input/c202C10.txt");
        EVRPTWInstance instance;
        try {
            instance = new SchneiderLoader().load(instanceFile);

            EVRPTWModel model = new EVRPTWModel(instance);
        //    model.solve();

          //  route.isFeasible();

           /* BeasleyHeuristic beasleyHeuristic = new BeasleyHeuristic(instance, giantRoute);
            List<Route> result = beasleyHeuristic.solve();
            result.forEach(e -> System.out.println(e.getNodes()));
            System.out.println(result.stream().mapToDouble(Route::getTotalDistance).sum());*/


            GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(instance);
            geneticAlgorithm.run();
            geneticAlgorithm.printProperties();
            geneticAlgorithm.printResults();
        } catch(IOException e) {
            System.err.println("Error: error while parsing the instance file (" + instanceFile.getPath() + ")\n"
                    + "is this an actual E-VRPTW instance file?");
        }/* catch (GRBException e) {
            e.printStackTrace();
        }/* catch (GRBException e) {
            e.printStackTrace();
        }*/
    }
}
