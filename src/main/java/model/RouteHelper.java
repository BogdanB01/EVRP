package model;

import java.util.Comparator;

public class RouteHelper {

    private EVRPTWInstance instance;

    public RouteHelper(EVRPTWInstance instance) {
        this.instance = instance;
    }

    private boolean isChargeable(Route route) {
       Node last = route.getEnd();
       RechargingStation charger = getNearestCharger(last);
       double battery = route.calculateRemainingTankCapacity(last)
               - instance.getTravelDistance(last, charger) * instance.getVehicleEnergyConsumption();
       return battery >= 0 && sufficientTimeForCharging(route, charger);
    }

    private boolean sufficientTimeForCharging(Route route, RechargingStation charger) {
        Route clonedRoute = new Route(instance, route);
        clonedRoute.addNode(charger);
        clonedRoute.addNode(instance.getDepot());
        return clonedRoute.isFeasible();
    }

    public boolean isInsertionFeasible(Route route, Node node, boolean relaxedInsertion) {
        Route clonedRoute = new Route(instance, route);
        clonedRoute.addNode(node);
        if (!clonedRoute.isFeasible()) return false;
        return isDepotReachable(clonedRoute) || relaxedInsertion && isChargeable(clonedRoute);
    }

    public boolean isDepotReachable(Route route) {
        double battery = route.calculateRemainingTankCapacity(route.getEnd())
                - instance.getTravelDistance(route.getEnd(), instance.getDepot()) * instance.getVehicleEnergyConsumption();
        return battery >= 0;
    }

    public RechargingStation getNearestCharger(Node node) {
        return instance.getRechargingStations().stream()
                .min(Comparator.comparing(e -> instance.getTravelDistance(node, e)))
                .orElse(null);
    }

    public RechargingStation getNearestChargerBetweenNodes(Node node1, Node node2) {
        RechargingStation nearestCharger = null;
        double minimumDistance = Double.MAX_VALUE;
        for (RechargingStation charger : instance.getRechargingStations()) {
            if (charger.getName().equals("S0") || node1.id == charger.id || node2.id == charger.id) continue;
            double distance = instance.getTravelDistance(node1, charger) + instance.getTravelDistance(charger, node2);
            if (distance < minimumDistance) {
                minimumDistance = distance;
                nearestCharger = charger;
            }
        }
        return nearestCharger;
    }
}
