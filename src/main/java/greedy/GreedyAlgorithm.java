package greedy;

import exception.InfeasibleRouteException;
import model.*;

import java.util.*;

public class GreedyAlgorithm {

    private EVRPTWInstance instance;
    private List<Customer> customers;

    public GreedyAlgorithm(EVRPTWInstance instance) {
        this.instance = instance;
        this.customers = instance.getCustomers();
        //this.customers.sort(Customer.WINDOW_END_COMPARATOR);
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

    private Customer getClosestNeighbour(Node node) {
        return customers.stream()
                .min(Comparator.comparing(c -> instance.getTravelDistance(node, c)))
                .orElseThrow(NoSuchElementException::new);
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

    private void generateFeasibleFuelConsumptionRouteToNode(Route route, Node to) {
        route.addNode(to);
        if (route.isTankCapacityViolated()) {
            route.removeNode(to);
           /// route
        }
    }


    public boolean isFeasibleInsertion(Route route, Node to) {
        Route clone = new Route(instance, route);
        clone.addNode(to);
        return clone.isFeasible();
    }

    public void solve() {
        List<Route> routes = new ArrayList<>();
        while (customers.size() != 0) {
            Customer customer = customers.get(0);
           // while (isFeasibleInsertion(route))
        }
    }
}
