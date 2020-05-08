package uaic.fii.solver.ga.search.neighbourhood;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Route;
import uaic.fii.model.Solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MoveStation extends AbstractNeighbourhoodGeneration {

    public MoveStation(EVRPTWInstance instance) {
        super(instance);
    }

    @Override
    public List<Solution> generate(Solution solution) {
        List<Solution> neighbours = new ArrayList<>();
        for (int i = 0; i < solution.getRoutes().size(); i++) {
            Route route = solution.getRoutes().get(i);
            for (int j = 2; j < route.getNodes().size() - 1; j++) {
                if (instance.isRechargingStation(route.getNodes().get(j))) {
                    Optional<Solution> neighbour = moveRechargingStation(solution, i, j);
                    neighbour.ifPresent(neighbours::add);
                }
            }
        }
        return neighbours;
    }

    private Optional<Solution> moveRechargingStation(Solution solution, int routeIndex, int nodeIndex) {
        Solution neighbour = solution.deepClone();
        Route route = neighbour.getRoutes().get(routeIndex);

        Collections.swap(route.getNodes(), nodeIndex, nodeIndex - 1);

        if (route.isFeasible()) {
            return Optional.of(neighbour);
        }
        return Optional.empty();
    }
}
