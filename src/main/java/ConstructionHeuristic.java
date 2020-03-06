import model.EVRPTWInstance;
import model.Node;
import model.RechargingStation;
import model.Route;
import org.javatuples.Pair;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ConstructionHeuristic {

    private static final Logger logger = Logger.getLogger(ConstructionHeuristic.class.getName());

    private EVRPTWInstance instance;

    public ConstructionHeuristic(EVRPTWInstance instance) {
        this.instance = instance;
    }

    private Map<Pair<Node, Node>, Double> calculateSavings() {
        Map<Pair<Node, Node>, Double> savings = new HashMap<>();
        for (Node firstNode : instance.getNodes()) {
            for (Node secondNode : instance.getNodes()) {
                if (firstNode.equals(secondNode)) continue;

                if (!instance.isCustomer(firstNode) || !instance.isCustomer(secondNode)) continue;

                if (instance.getDemand(firstNode) + instance.getDemand(secondNode) > instance.getVehicleCapacity()) {
                    logger.info(String.format("Edge (%s, %s) discarded because of too high demand!",
                            firstNode.id, secondNode.id));
                    continue;
                }

                if (instance.getTimeWindow(firstNode).getStart() + instance.getServiceTime(firstNode)
                        + instance.getTravelTime(firstNode, secondNode) > instance.getTimeWindow(secondNode).getEnd()) {
                    logger.info(String.format("Edge (%s, %s) discarded because after serving node %s," +
                                    " at earliest time + serviceTime + travelTime exceeds latest serviceTime of %s",
                            firstNode.id, secondNode.id, firstNode.id, secondNode.id));
                    continue;
                }

                if (instance.getTimeWindow(firstNode).getStart() + instance.getServiceTime(firstNode)
                        + instance.getTravelTime(firstNode, secondNode) + instance.getServiceTime(secondNode)
                        + instance.getTravelTime(secondNode, instance.getDepot()) > instance.getTimeWindow(instance.getDepot()).getEnd()) {
                    logger.info(String.format("Discarded edge (%s, %s) because after service %s, %s can't be " +
                            "served in time to make it back to the depot", firstNode.id, secondNode.id, firstNode.id, secondNode.id));
                    continue;
                }

                Node depot = instance.getDepot();
                double a = instance.getTravelDistance(depot, firstNode) + instance.getTravelDistance(firstNode, depot)
                        + instance.getTravelDistance(depot, secondNode) + instance.getTravelDistance(secondNode, depot);
                double b = instance.getTravelDistance(depot, firstNode) + instance.getTravelDistance(firstNode, secondNode)
                        + instance.getTravelDistance(secondNode, depot);
                savings.put(Pair.with(firstNode, secondNode), a - b);
            }
        }
        return savings;
    }

    /**
     * Preprocessing step to remove infeasible arcs
     */
    public void doPreprocessing(EVRPTWInstance instance) {
        Set<Node> infeasibleArcs = new HashSet<>();
        for (Node node1 : instance.getNodes()) {
            for (Node node2 : instance.getNodes()) {
                if (node1 == node2) continue;

                // qv + qw > C
                if (instance.getDemand(node1) + instance.getDemand(node2) > instance.getVehicleCapacity()) {
                    logger.info(String.format("Edge (%s, %s) discarded because of too high demand", node1.id, node2.id));
                    continue;
                }

                // ev + sv + tvw > lw
                if (instance.getTimeWindow(node1).getStart() + instance.getServiceTime(node1)
                        + instance.getTravelTime(node1, node2) > instance.getTimeWindow(node2).getEnd()) {
                    logger.info(String.format("Edge (%s, %s) discarded because after serving node %s," +
                                    " at earliest time + serviceTime + travelTime exceeds latest serviceTime of %s",
                                        node1.id, node2.id, node1.id, node2.id));
                    continue;
                }

                // ev + sv + tvw + sw + twn+1 >= l0
                if (instance.getTimeWindow(node1).getStart() + instance.getServiceTime(node1)
                        + instance.getTravelTime(node1, node2) + instance.getServiceTime(node2)
                        + instance.getTravelTime(node2, instance.getDepot()) > instance.getTimeWindow(instance.getDepot()).getEnd()) {
                    logger.info(String.format("Discarded edge (%s, %s) because after service %s, %s can't be " +
                                    "served in time to make it back to the depot", node1.id, node2.id, node1.id, node2.id));
                    continue;
                }

                if ( ! instance.isCustomer(node1) && ! instance.isCustomer(node2) ) {
                    continue;
                }

                // r * (djv + dvw + dwi) >= Q

                List<RechargingStation> chargers = instance.getRechargingStations();

                boolean allowedArc = false;
                for (RechargingStation charger1 : chargers) {
                    if (allowedArc) break;
                    for (RechargingStation charger2 : chargers) {
                        if (instance.getVehicleEnergyConsumption() *
                                (instance.getTravelDistance(charger1, node1)
                                        + instance.getTravelDistance(node1, node2)
                                        + instance.getTravelDistance(node2, charger2)) <= instance.getVehicleEnergyCapacity()) {
                            allowedArc = true;
                            break;
                        }
                    }
                }
                if ( ! allowedArc ) {
                    logger.info(String.format("Edge (%s, %s) discarded because there is no drivable route from any " +
                                    "charger to %s, %s and back to any charger, without going over the fuel capacity",
                                        node1.id, node2.id, node1.id, node2.id));
                }
            }
        }
    }

    private Node getNearestCharger(Node node) {
        RechargingStation nearestCharger = null;
        double minimumDistance = Double.MAX_VALUE;
        for (RechargingStation charger : instance.getRechargingStations()) {
            if (charger.id == node.id) continue;
            double distance = instance.getTravelDistance(node, charger);
            if (distance < minimumDistance) {
                minimumDistance = distance;
                nearestCharger = charger;
            }
        }
        return nearestCharger;
    }

    private Node getNearestChargerBetweenNodes(Node node1, Node node2) {
        RechargingStation nearestCharger = null;
        double minimumDistance = Double.MAX_VALUE;
        for (RechargingStation charger : instance.getRechargingStations()) {
            if (charger.id == node1.id || charger.id == node2.id) continue;
            double distance = instance.getTravelDistance(node1, charger) + instance.getTravelDistance(charger, node2);
            if (distance < minimumDistance) {
                minimumDistance = distance;
                nearestCharger = charger;
            }
        }
        return nearestCharger;
    }

    private double calculateRouteDistance(List<Node> route, Node start, Node end) {
        double cost = 0;
        if (route.size() == 0) return cost;
        cost += instance.getTravelDistance(start, route.get(0));
        for (int i = 0; i < route.size() - 1; i++) {
            cost += instance.getTravelDistance(route.get(i), route.get(i + 1));
        }
        cost += instance.getTravelDistance(route.get(route.size() - 1), end);
        return cost;
    }

    private double getConsumptionForRoute(List<Node> nodes) {
        List<Node> partialRoute = new ArrayList<>();
        Node lastCharger = instance.getDepot();
        double maximumLength = 0;
        for (Node node : nodes) {
            if (instance.isRechargingStation(node)) {
                double partialLength = calculateRouteDistance(partialRoute, lastCharger, node);
                if (partialLength > maximumLength) {
                    maximumLength = partialLength;
                }
                partialRoute = new ArrayList<>();
                lastCharger = node;
            } else {
                partialRoute.add(node);
            }
        }
        double partialLength = calculateRouteDistance(partialRoute, lastCharger, instance.getDepot());
        if (partialLength > maximumLength) {
            maximumLength = partialLength;
        }
        return maximumLength * instance.getVehicleEnergyConsumption();
    }

    private Pair<Integer, Integer> getMostConsumingEdge(List<Node> route) {
        double maxDistance = Double.MIN_VALUE;
        Pair<Integer, Integer> edge = null;
        for (int i = 0; i < route.size() - 1; i++) {
            double distance = instance.getTravelDistance(route.get(i), route.get(i + 1));
            if (distance > maxDistance) {
                maxDistance = distance;
                edge = Pair.with(i, i + 1);
            }
        }
        return edge;
    }

    private List<Node> insertCharger(List<Node> route, int index, boolean useHeuristic) {
        boolean feasibleInsertion = false;
        Node latestCharger = null;
        List<Node> tempRoute = new ArrayList<>(route);

        int[] reversedRange = IntStream.rangeClosed(1, index).map(i -> index - i + 1).toArray();
        for (int i : reversedRange) {
            Node charger = getNearestCharger(tempRoute.get(i));
            latestCharger = charger;
            // insert charger
            tempRoute.add(i + 1, charger);
            feasibleInsertion = instance.getVehicleEnergyCapacity() >= getConsumptionForRoute(tempRoute.subList(0, index + 2));
            if (feasibleInsertion) break;
            // remove charger
            tempRoute.remove(i + 1);
        }

        // if feasible insertion not found or it is deport or it is charger right before the depot
        if (latestCharger != null && latestCharger.id == instance.getDepot().id) {
            tempRoute.clear();
        } else if ( ! feasibleInsertion && useHeuristic) {
            // heuristic insertion
            Pair<Integer, Integer> edge = getMostConsumingEdge(tempRoute.subList(0, index + 1));
            Node nearestCharger = getNearestChargerBetweenNodes(tempRoute.get(edge.getValue0()), tempRoute.get(edge.getValue1()));
            tempRoute.add(edge.getValue0(), nearestCharger);
        } else if ( !feasibleInsertion && tempRoute.equals(route)) {
            tempRoute.clear();
        }
        return tempRoute;
    }

    private List<Node> insertChargers(List<Node> route) {
        // add the depot
        route.add(0, instance.getDepot());
        route.add(instance.getDepot());

        int i = 0;
        double currentEnergy = instance.getVehicleEnergyCapacity();
        while (i < route.size() - 1) {
            Node currentNode = route.get(i);
            if (instance.isRechargingStation(currentNode) && ! instance.isDepot(currentNode)) {
                currentEnergy = instance.getVehicleEnergyCapacity();
            }

            Node nextNode = route.get(i + 1);
            double nextEnergy = currentEnergy - instance.getTravelDistance(currentNode, nextNode) * instance.getVehicleEnergyConsumption();
            if (nextEnergy < 0) {
                route = insertCharger(route, i + 1, true);
                if (route.size() == 0 || route.size() > 100) {
                    System.out.println(route.size());
                    return new ArrayList<>();
                }
                currentEnergy = instance.getVehicleEnergyCapacity();
            } else {
                currentEnergy = nextEnergy;
            }
            i++;
        }

        // remove depots
        route.remove(0);
        route.remove(route.size() - 1);

        return route;
    }

    private void printRoute(List<Node> route) {
        for (Node node : route) {
            if (instance.isDepot(node)) {
                System.out.print(instance.getDepot().getName() + " ");
            } else if (instance.isCustomer(node)) {
                System.out.print(instance.getCustomer(node).getName() + " ");
            } else if (instance.isRechargingStation(node)){
                System.out.print(instance.getRechargingStation(node).getName() + " ");
            }
        }
        System.out.println();
    }

    private List<Route> getInitialRoutes() {
        List<Route> routes = new ArrayList<>();
        for (Node node : instance.getNodes()) {
            if (instance.isCustomer(node)) {
                routes.add(new Route(insertChargers(new ArrayList<>(Collections.singleton(node)))));
            }
        }
        return routes;
    }

    private Map<Pair<Node, Node>, Double> calculateSavings(List<Route> routes) {
        Map<Pair<Node, Node>, Double> savings = new HashMap<>();
        for (Route firstRoute : routes) {
            for (Route secondRoute : routes) {
                if (firstRoute.equals(secondRoute)) continue;
                if (instance.isRechargingStation(secondRoute.getStart())) continue;
                double a = instance.getTravelDistance(firstRoute.getEnd(), instance.getDepot());
                double b = instance.getTravelDistance(instance.getDepot(), secondRoute.getStart());
                double c = instance.getTravelDistance(firstRoute.getEnd(), secondRoute.getStart());
                double saving = a + b - c;
                if (saving > 0) {
                    savings.put(Pair.with(firstRoute.getEnd(), secondRoute.getStart()), saving);
                }
            }
        }
        return savings;
    }

    private Optional<Pair<Route, Route>> searchRoutes(Pair<Node, Node> saving, List<Route> routes) {
        boolean foundFirst = false;
        boolean foundSecond = false;
        Route firstRoute = null;
        Route secondRoute = null;
        for (Route route : routes) {
            if (route.getEnd().equals(saving.getValue0())) {
                foundFirst = true;
                firstRoute = route;
            }
            if (route.getStart().equals(saving.getValue1())) {
                foundSecond = true;
                secondRoute = route;
            }
            if (foundFirst && foundSecond) break;
        }
        if (foundFirst && foundSecond && !firstRoute.equals(secondRoute)) {
            return Optional.of(Pair.with(firstRoute, secondRoute));
        }
        return Optional.empty();
    }

    private boolean doesNotHaveCustomers(Route route) {
        for (Node node : route.getNodes()) {
            if (instance.isCustomer(node)) {
                return false;
            }
        }
        return true;
    }

    public List<Route> clarkeAndWrightsAlgorithm() {
        List<Route> routes = getInitialRoutes();
        Map<Pair<Node, Node>, Double> savings = calculateSavings(routes);

        routes.forEach(e -> printRoute(e.getNodes()));
        Map<Pair<Node, Node>, Double> sortedSavings = new LinkedHashMap<>();
        savings.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedSavings.put(x.getKey(), x.getValue()));

        for (Map.Entry<Pair<Node, Node>, Double> entry : sortedSavings.entrySet()) {
            Optional<Pair<Route, Route>> pair = searchRoutes(entry.getKey(), routes);
            if (pair.isPresent()) {
                List<Node> firstRoute = pair.get().getValue0().getNodes();
                List<Node> secondRoute = pair.get().getValue1().getNodes();
                System.out.println("This routes found " + firstRoute + " " + secondRoute);
                List<Node> combinedRoute = Stream.of(firstRoute, secondRoute).flatMap(List::stream).collect(Collectors.toList());
                combinedRoute = insertChargers(combinedRoute);
                if (isRouteValid(combinedRoute)) {
                    System.out.println("Merging routes " + firstRoute + " " + secondRoute);
                    routes.remove(pair.get().getValue0());
                    routes.remove(pair.get().getValue1());
                    routes.add(new Route(combinedRoute));
                }
            }
        }
        for (Route route : routes) {
            printRoute(route.getNodes());
        }
        return routes;
    }

    private boolean isRouteValid(List<Node> route) {
        double currentCapacity = 0;
        double currentTime = 0;
        double currentEnergy = instance.getVehicleEnergyCapacity();
        for (int i = 0; i < route.size() - 1; i++) {
            Node node = route.get(i);
            if (instance.isRechargingStation(node)) {
                double loadTime = (instance.getVehicleEnergyCapacity() - currentEnergy) * instance.getRechargingRate(node);
                currentCapacity = instance.getVehicleEnergyCapacity();
                currentTime += loadTime;
            }

            Node nextNode = route.get(i + 1);

            double nextTime = currentTime + instance.getServiceTime(node) + instance.getTravelTime(route.get(i), route.get(i + 1));
            if (nextTime > instance.getTimeWindow(route.get(i + 1)).getEnd()) {
                logger.info("Rejected because time window is violated!");
                return false;
            }

            // it is possible to arrive earlier at one customer
            currentTime = Math.max(nextTime, instance.getTimeWindow(nextNode).getStart());

            // check capacity constraint
            double nextCapacity = currentCapacity + instance.getDemand(node);
            if (nextCapacity > instance.getVehicleCapacity()) {
                logger.info("Rejected because capacity constraint is violated!");
                return false;
            }
            currentCapacity = nextCapacity;
            currentEnergy -= instance.getTravelDistance(node, nextNode) * instance.getVehicleEnergyConsumption();
        }
        return route.size() > 0;
    }

    public void test() {
        Map<Pair<Node, Node>, Double> savings = calculateSavings();
        Map<Pair<Node, Node>, Double> sortedSavings = new LinkedHashMap<>();
        savings.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedSavings.put(x.getKey(), x.getValue()));
        for (Map.Entry<Pair<Node, Node>, Double> entry : sortedSavings.entrySet()) {
            System.out.println(entry.getValue());
        }
    }
}
