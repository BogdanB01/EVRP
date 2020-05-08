package uaic.fii.solver.ga.search.neighbourhood;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Node;
import uaic.fii.model.Route;
import uaic.fii.model.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InterRouteRelocate extends AbstractNeighbourhoodGeneration {

    public InterRouteRelocate(EVRPTWInstance instance) {
        super(instance);
    }

    @Override
    public List<Solution> generate(Solution solution) {
        List<Solution> neighbours = new ArrayList<>();
        for (int i = 0; i < solution.getRoutes().size(); i++) {
            Route route = solution.getRoutes().get(i);
            if (route.getSize() > 3) {
                for (int j = i; j < solution.getRoutes().size(); j++) {
                    Route secondRoute = solution.getRoutes().get(j);
                    for (int m = 1; m < route.getSize() - 1; m++) {
                        for (int n = 1; n < secondRoute.getSize() - 1; n++) {
                            if (i == j && n <= m) {
                                continue;
                            }
                            Optional<Solution> neighbour = performRelocation(solution, i, m, j, n);
                            neighbour.ifPresent(neighbours::add);
                        }
                    }
                }
            }
        }
        return neighbours;
    }

    private Optional<Solution> performRelocation(Solution solution, int routeIndex, int nodeOfFirstRoute, int secondRouteIndex, int nodeOfSecondRoute) {
        Solution neighbour = solution.deepClone();
        Node node = neighbour.getRoutes().get(routeIndex).getNodes().get(nodeOfFirstRoute);
        neighbour.getRoutes().get(secondRouteIndex).getNodes().add(nodeOfSecondRoute, node);
        neighbour.getRoutes().get(routeIndex).getNodes().remove(nodeOfFirstRoute);
        if (neighbour.isFeasible()) {
            return Optional.of(neighbour);
        }
        return Optional.empty();
    }
}
