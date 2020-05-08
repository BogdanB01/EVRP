package uaic.fii.solver;

import gurobi.GRBException;
import uaic.fii.model.Route;
import uaic.fii.model.Solution;
import uaic.fii.solver.exact.EVRPTWModel;
import uaic.fii.model.EVRPTWInstance;
import uaic.fii.solver.ga.GeneticAlgorithm;
import uaic.fii.solver.ga.search.TabuSearch;
import uaic.fii.solver.verifier.RoutesLoader;
import uaic.fii.solver.verifier.SchneiderLoader;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        File instanceFile = new File("input/c101C5.txt");
        // File solutionFile = new File("C:\\Users\\BogdanBo\\Documents\\Facultate\\Dizertatie\\src\\main\\resources\\solution\\exact\\c101C5.txt");
        EVRPTWInstance instance;
        try {
            instance = new SchneiderLoader().load(instanceFile);
            GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(instance);
            geneticAlgorithm.run();
            Solution solution = geneticAlgorithm.getSolution();

            System.out.println(solution.getCost());

            TabuSearch tabuSearch = new TabuSearch(instance);
            Solution improved = tabuSearch.execute(solution);

            System.out.println(improved.getCost());
        //    model.solve();

        } catch(IOException e) {
            e.printStackTrace();
        } /* catch (GRBException e) {
            e.printStackTrace();
        }*/


    }
}
