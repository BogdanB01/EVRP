package uaic.fii.solver.ga.search.neighbourhood;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Node;
import uaic.fii.model.Route;
import uaic.fii.model.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TwoOptArcExchange extends AbstractNeighbourhoodGeneration {

    public TwoOptArcExchange(EVRPTWInstance instance) {
        super(instance);
    }

    @Override
    public List<Solution> generate(Solution solution) {
        List<Solution> neighbours = new ArrayList<>();
        for (int i = 0; i < solution.getRoutes().size(); i++) {
            Route firstRoute = solution.getRoutes().get(i);
            for (int j = i + 1; j < solution.getRoutes().size(); j++) {
                Route secondRoute = solution.getRoutes().get(j);
                for (int k = 1; k < firstRoute.getSize() - 1; k++) {
                    for (int m = 1; m < secondRoute.getSize() - 1; m++) {
                        Optional<Solution> neighbour = performTwoOptExchange(solution, i, k, j, m);
                        neighbour.ifPresent(neighbours::add);
                    }
                }
            }
        }
        return neighbours;
    }

    private Optional<Solution> performTwoOptExchange(Solution solution, int firstRouteIndex, int firstRouteNodeIndex, int secondRouteIndex, int secondRouteNodeIndex) {
        Solution neighbour = solution.deepClone();
        Route firstRoute = neighbour.getRoutes().get(firstRouteIndex);
        Route secondRoute = neighbour.getRoutes().get(secondRouteIndex);

        List<Node> newFirstRouteNodes = new ArrayList<>(firstRoute.getNodes().subList(0, firstRouteNodeIndex + 1));
        newFirstRouteNodes.addAll(secondRoute.getNodes().subList(secondRouteNodeIndex, secondRoute.getSize()));

        List<Node> newSecondRouteNodes = new ArrayList<>(secondRoute.getNodes().subList(0, secondRouteNodeIndex));
        newSecondRouteNodes.addAll(firstRoute.getNodes().subList(firstRouteNodeIndex + 1, firstRoute.getSize()));

        firstRoute.getNodes().clear();
        firstRoute.getNodes().addAll(newFirstRouteNodes);

        secondRoute.getNodes().clear();
        secondRoute.getNodes().addAll(newSecondRouteNodes);

        if (newFirstRouteNodes.size() <= 2) {
            neighbour.getRoutes().remove(firstRouteIndex);
        }

        if (newSecondRouteNodes.size() <= 2) {
            neighbour.getRoutes().remove(secondRouteIndex);
        }
        if (neighbour.isFeasible()) {
            return Optional.of(neighbour);
        }
        return Optional.empty();
    }
}
