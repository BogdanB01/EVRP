package uaic.fii.solver.ga.search.neighbourhood;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Node;
import uaic.fii.model.Route;
import uaic.fii.model.Solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TwoOrOpt extends AbstractNeighbourhoodGeneration {

    public TwoOrOpt(EVRPTWInstance instance) {
        super(instance);
    }

    @Override
    public List<Solution> generate(Solution solution) {
        List<Solution> neighbours = new ArrayList<>();
        for (int i = 0; i < solution.getRoutes().size(); i++) {
            Route route = solution.getRoutes().get(i);
            if (route.getSize() >= 5) {
                for (int j = 0; j < route.getSize() - 2; j++) {
                    if (instance.isCustomer(route.getNodes().get(j)) && instance.isCustomer(route.getNodes().get(j + 1))) {
                        if (j > 1) {
                            for (int k = 1; k < j; k++) {
                                Optional<Solution> neighbour = performOrOptBackwards(solution, i, j, k);
                                neighbour.ifPresent(neighbours::add);
                            }
                        }
                        if (j + 1 != route.getSize() - 2) {
                            for (int k = j + 2; k < route.getSize() - 1; k++) {
                                Optional<Solution> neighbour = performOrOptForwards(solution, i, j, k);
                                neighbour.ifPresent(neighbours::add);
                            }
                        }
                    }
                }
            }
        }
        return neighbours;
    }

    private Optional<Solution> performOrOptBackwards(Solution solution, int routeIndex, int indexOfChainFirstNode, int insertBeforeIndex) {
        Solution neighbor = solution.deepClone();
        Route route = neighbor.getRoutes().get(routeIndex);
        Node node1 = route.getNodes().get(indexOfChainFirstNode);
        Node node2 = route.getNodes().get(indexOfChainFirstNode + 1);
        List<Node> chain = Arrays.asList(node1, node2);
        route.getNodes().removeAll(chain);
        route.getNodes().addAll(insertBeforeIndex, chain);
        if (route.isFeasible()) {
            return Optional.of(neighbor);
        }
        return Optional.empty();
    }

    private Optional<Solution> performOrOptForwards(Solution solution, int routeIndex, int indexOfChainFirstNode, int insertAfterIndex) {
        Solution neighbour = solution.deepClone();
        Route route = neighbour.getRoutes().get(routeIndex);
        Node node1 = route.getNodes().get(indexOfChainFirstNode);
        Node node2 = route.getNodes().get(indexOfChainFirstNode + 1);
        List<Node> chain = Arrays.asList(node1, node2);
        route.getNodes().removeAll(chain);
        route.getNodes().addAll(insertAfterIndex - 1, chain);
        if (route.isFeasible()) {
            return Optional.of(neighbour);
        }
        return Optional.empty();
    }
}
