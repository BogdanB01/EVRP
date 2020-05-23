package uaic.fii.solver.ga.search.neighbourhood;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Route;
import uaic.fii.model.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MergeRoutes extends AbstractNeighbourhoodGeneration {

    public MergeRoutes(EVRPTWInstance instance) {
        super(instance);
    }

    @Override
    public List<Solution> generate(Solution solution) {
        List<Solution> neighbours = new ArrayList<>();
        for (int i = 0; i < solution.getRoutes().size(); i++) {
            for (int j = 0; j < solution.getRoutes().size(); j++) {
                if (i == j) {
                    continue;
                }
                Optional<Solution> neighbour = mergeRoutes(solution, i, j);
                neighbour.ifPresent(neighbours::add);
            }
        }
        return neighbours;
    }

    private Optional<Solution> mergeRoutes(Solution solution, int firstRouteIndex, int secondRouteIndex) {
        Solution neighbour = solution.deepClone();
        Route firstRoute = neighbour.getRoutes().get(firstRouteIndex);
        Route secondRoute = neighbour.getRoutes().get(secondRouteIndex);

        firstRoute.appendRoute(secondRoute);
        neighbour.getRoutes().remove(secondRouteIndex);

        if (firstRoute.isFeasible()) {
            return Optional.of(neighbour);
        }
        return Optional.empty();
    }
}
