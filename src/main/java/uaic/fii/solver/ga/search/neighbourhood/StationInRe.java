package uaic.fii.solver.ga.search.neighbourhood;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.RechargingStation;
import uaic.fii.model.Route;
import uaic.fii.model.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StationInRe extends AbstractNeighbourhoodGeneration {

    public StationInRe(EVRPTWInstance instance) {
        super(instance);
    }

    @Override
    public List<Solution> generate(Solution solution) {
        List<Solution> neighbours = new ArrayList<>();
        for (int i = 0; i < solution.getRoutes().size(); i++) {
            Route route = solution.getRoutes().get(i);
            for (int j = 1; j < route.getNodes().size(); j++) {
                if (instance.isRechargingStation(route.getNodes().get(j)) && route.getSize() > 3) {
                    Optional<Solution> neighbour = performStationRemoval(solution, i, j);
                    neighbour.ifPresent(neighbours::add);
                } else {
                    if ( ! instance.isRechargingStation(route.getNodes().get(j - 1))) {
                        for (RechargingStation rechargingStation : instance.getRechargingStations()) {
                            Optional<Solution> neighbour = performStationInsertion(solution, i, j, rechargingStation);
                            neighbour.ifPresent(neighbours::add);
                        }
                    }
                }
            }
        }
        return neighbours;
    }

    private Optional<Solution> performStationRemoval(Solution solution, int routeIndex, int nodeIndex) {
        Solution neighbour = solution.deepClone();
        Route route = neighbour.getRoutes().get(routeIndex);
        route.getNodes().remove(nodeIndex);
        if (route.isFeasible()) {
            return Optional.of(neighbour);
        }
        return Optional.empty();
    }

    private Optional<Solution> performStationInsertion(Solution solution, int routeIndex, int nodeIndex, RechargingStation station) {
        Solution neighbour = solution.deepClone();
        Route route = neighbour.getRoutes().get(routeIndex);
        route.getNodes().add(nodeIndex, station);
        if (route.isFeasible()) {
            return Optional.of(neighbour);
        }
        return Optional.empty();
    }

}
