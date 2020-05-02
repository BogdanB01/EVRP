package uaic.fii.solver.ga;

import uaic.fii.solver.model.EVRPTWInstance;
import uaic.fii.solver.model.Route;
import uaic.fii.solver.model.Solution;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TwoOptStar extends AbstractNeighbourhoodGeneration {

    public TwoOptStar(EVRPTWInstance instance) {
        super(instance);
    }

    @Override
    protected Optional<Solution> generate(Solution solution) {
        int size = solution.getNumberOfRoutes();

        List<Integer> routeIndices = range(0, size);
        List<int []> routeCombinations = combinations(routeIndices);

        Collections.shuffle(routeCombinations);

        for (int[] combination : routeCombinations) {
            Route route1 = solution.getIthRoute(combination[0]);
            Route route2 = solution.getIthRoute(combination[1]);


        }
        return Optional.empty();
    }
}
