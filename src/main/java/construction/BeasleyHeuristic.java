package construction;

import exception.InfeasibleRouteException;
import model.*;
import org.javatuples.Pair;

import java.util.*;

public class BeasleyHeuristic {

    private EVRPTWInstance instance;
    private double[][] costs;
    private Route[][] routes;
    private GiantRouteConstructionStrategy strategy;

    public BeasleyHeuristic(EVRPTWInstance instance, GiantRouteConstructionStrategy strategy) {
        this.instance = instance;
        this.strategy = strategy;
        int dimension = instance.getCustomers().size() + 1;
        this.costs = new double[dimension][dimension];
        this.routes = new Route[dimension][dimension];
        Arrays.stream(this.costs).forEach(e -> Arrays.fill(e, Double.MAX_VALUE));
    }

    public List<Route> solve() {
        List<Node> giantRoute = strategy.construct();
        for (int i = 0; i < giantRoute.size(); i++) {
            for (int j = i + 1; j < giantRoute.size() + 1; j++) {
                Optional<Route> route = generateRoute(giantRoute, i, j);
                if (!route.isPresent()) {
                    break;
                }
                double cost = route.get().getTotalDistance();
                costs[i][j] = cost;
                routes[i][j] = route.get();
            }
        }

        ShortestPathSolver solver = new ShortestPathSolver(costs);
        List<Pair<Integer, Integer>> result = solver.solve();

        List<Route> solution = new ArrayList<>();
        for (Pair<Integer, Integer> res : result) {
            solution.add(routes[res.getValue0()][res.getValue1()]);
        }

        return solution;
    }

    private Optional<Route> generateRoute(List<Node> giantRoute, int i, int j) {
        Route route = new Route(instance);
        try {
            generateFeasibleRouteToNode(route, giantRoute.get(i));
            while (i != (j - 1)) {
                i += 1;
                generateFeasibleRouteToNode(route, giantRoute.get(i));
            }
            generateFeasibleRouteToNode(route, instance.getDepot());
        } catch (InfeasibleRouteException e) {
            return Optional.empty();
        }
        return Optional.of(route);
    }

    private void generateFeasibleRouteToNode(Route route, Node to) throws InfeasibleRouteException {
        route.addNode(to);

        while (!route.isFeasible()) {
            insertStationBefore(route, to);
        }

        if (instance.getDepot().id != to.id &&
                route.calculateRemainingTankCapacity(route.getEnd()) < instance.getVehicleEnergyCapacity() / 2) {
            insertStationBefore(route, to);

            if (!route.isFeasible()) {
                route.removeNode(route.getEnd());
                route.removeNode(route.getEnd());
                route.addNode(to);
            }
        }
    }

    private Optional<RechargingStation> getClosestCharger(Route route, Node to) {
        Node lastNode = route.getEnd();
        double capacity = route.calculateRemainingTankCapacity(lastNode);
        double maxDistance = capacity / instance.getVehicleEnergyConsumption();

        return instance.getRechargingStations().stream()
                .filter(e -> instance.getTravelDistance(lastNode, e) <= maxDistance
                        && !route.getNodes().contains(e)
                        && e.id != lastNode.id)
                .min(Comparator.comparing(e -> instance.getTravelDistance(e, to)));
    }

    private void insertStationBefore(Route route, Node node) throws InfeasibleRouteException {
        route.removeNode(node);
        RechargingStation rechargingStation = getClosestCharger(route, node)
                .orElseThrow(InfeasibleRouteException::new);
        route.addNode(rechargingStation);
        route.addNode(node);
    }

}