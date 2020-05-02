package uaic.fii.solver.ga;

import uaic.fii.solver.model.EVRPTWInstance;
import uaic.fii.solver.model.Node;
import uaic.fii.solver.model.Route;
import uaic.fii.solver.model.Solution;

import java.util.Collections;
import java.util.Optional;

public class SwapCustomers extends AbstractNeighbourhoodGeneration {

    public SwapCustomers(EVRPTWInstance instance) {
        super(instance);
    }

    @Override
    public Optional<Solution> generate(Solution solution) {
        int size = solution.getNumberOfRoutes();
        int randomIndex = RANDOM.nextInt(size - 1);
        for (int i = 0; i < size; i++) {
            int routeIndex = (randomIndex + i) % size;
            Route route = solution.getIthRoute(routeIndex);
            int routeSize = route.getSize();
            for (int j = 0; j < routeSize * routeSize; j++) {
                int fromIndex = RANDOM.nextInt(routeSize - 1);
                int toIndex = RANDOM.nextInt(routeSize - 1);

                if (fromIndex == toIndex) {
                    toIndex = (toIndex + 1) % routeSize;
                }

                Node fromNode = route.getNodes().get(fromIndex);
                Node toNode = route.getNodes().get(toIndex);

                if (instance.isCustomer(fromNode) && instance.isCustomer(toNode)) {
                    Solution neighbour = solution.copy();
                    Route alteredRoute = neighbour.getIthRoute(routeIndex);
                    Collections.swap(alteredRoute.getNodes(), fromIndex, toIndex);

                    if (alteredRoute.isFeasible() && neighbour.getTotalDistance() < solution.getTotalDistance()) {
                        return Optional.of(neighbour);
                    }
                }
            }
        }

        return Optional.empty();
    }
}
