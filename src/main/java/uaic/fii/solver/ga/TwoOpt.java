package uaic.fii.solver.ga;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Node;
import uaic.fii.model.Route;
import uaic.fii.model.Solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TwoOpt extends AbstractNeighbourhoodGeneration {

    public TwoOpt(EVRPTWInstance instance) {
        super(instance);
    }

    @Override
    protected Optional<Solution> generate(Solution solution) {
        int size = solution.getNumberOfRoutes();
        List<Integer> routeIndices = range(0, size);
        Collections.shuffle(routeIndices);

        for (int routeIndex : routeIndices) {
            Route route = solution.getIthRoute(routeIndex);
            List<int[]> cutPoints = cartesianProduct(routeIndices.subList(1, size - 1),
                    routeIndices.subList(0, size - 1));
            Collections.shuffle(cutPoints);

            for (int[] cutPoint : cutPoints) {
                if (cutPoint[0] < cutPoint[1] && cutPoint[1] - cutPoint[0] > 1) {
                    Solution neighbour = solution.copy();
                    List<Node> nodes = new ArrayList<>(route.getNodes());

                    List<Node> part1 = nodes.subList(0, cutPoint[0]);
                    List<Node> part2 = nodes.subList(cutPoint[0], cutPoint[1]);
                    List<Node> part3 = nodes.subList(cutPoint[1], route.getSize());

                    Collections.reverse(part2);

                    nodes = Stream.of(part1, part2, part3)
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    Route alteredRoute = new Route(instance, nodes);
                    neighbour.setIthRoute(routeIndex, alteredRoute);

                    if (alteredRoute.isFeasible() && neighbour.getTotalDistance() < solution.getTotalDistance()) {
                        return Optional.of(neighbour);
                    }
                }
            }

        }
        return Optional.empty();
    }

}