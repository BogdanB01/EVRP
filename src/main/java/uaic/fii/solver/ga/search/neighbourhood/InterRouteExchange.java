package uaic.fii.solver.ga.search.neighbourhood;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Node;
import uaic.fii.model.Route;
import uaic.fii.model.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InterRouteExchange extends AbstractNeighbourhoodGeneration {

    public InterRouteExchange(EVRPTWInstance instance) {
        super(instance);
    }

    @Override
    public List<Solution> generate(Solution solution) {
        List<Solution> neighbours = new ArrayList<>();
        for (int i = 0; i < solution.getRoutes().size(); i++) {
            Route route = solution.getRoutes().get(i);
            for (int j = i; j < solution.getRoutes().size(); j++) {
                Route secondRoute = solution.getRoutes().get(j);
                for (int m = 1; m < route.getSize() - 1; m++) {
                    if (instance.isRechargingStation(route.getNodes().get(m))) {
                        continue; // skip exchange for Recharging stations
                    }
                    for (int n = 1; n < secondRoute.getSize() - 1; n++) {
                        if (instance.isRechargingStation(secondRoute.getNodes().get(n))) {
                            continue; // skip exchange for Recharging stations
                        }
                        if (n != m || route.getNodes().get(m).id != secondRoute.getNodes().get(n).id) {
                            Optional<Solution> neighbour = performNodeSwap(solution, i, m, j, n);
                            neighbour.ifPresent(neighbours::add);
                        }
                    }
                }
            }
        }
        return neighbours;
    }

    private Optional<Solution> performNodeSwap(Solution solution, int routeIndex, int nodeOfFirstRoute, int secondRouteIndex, int nodeOfSecondRoute) {
        Solution neighbour = solution.deepClone();
        Node node1 = neighbour.getRoutes().get(routeIndex).getNodes().get(nodeOfFirstRoute);
        Node node2 = neighbour.getRoutes().get(secondRouteIndex).getNodes().get(nodeOfSecondRoute);

        neighbour.getRoutes().get(routeIndex).getNodes().set(nodeOfFirstRoute, node2);
        neighbour.getRoutes().get(secondRouteIndex).getNodes().set(nodeOfSecondRoute, node1);
        if (neighbour.isFeasible()) {
            return Optional.of(neighbour);
        }
        return Optional.empty();
    }
}
