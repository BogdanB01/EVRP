package uaic.fii.model;

import java.util.*;
import java.util.stream.Collectors;

public class Route {

    private EVRPTWInstance instance;
    private List<Node> nodes;

    public Route(EVRPTWInstance instance) {
        this.instance = instance;
        this.nodes = new ArrayList<>(Collections.singleton(instance.getDepot()));
    }

    public Route(List<Node> nodes) {
        this.nodes = nodes;
    }

    public Route(EVRPTWInstance instance, Route route) {
        this.instance = instance;
        this.nodes = new ArrayList<>(route.getNodes());
    }

    public Route(EVRPTWInstance instance, List<Node> nodes) {
        this.instance = instance;
        this.nodes = nodes;
    }

    public Node getStart() {
        return nodes.get(0);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Node getEnd() {
        return nodes.get(nodes.size() - 1);
    }

    public void pop() {
        nodes.remove(nodes.size() - 1);
    }
    public void addNode(Node node) {
        nodes.add(node);
    }

    public boolean isComplete() {
        Node depot = instance.getDepot();
        return nodes.get(0).equals(depot) && nodes.get(nodes.size() - 1).equals(depot) && ! nodes.subList(1, nodes.size() - 1).contains(depot);
    }

    public boolean isFeasible() {
        Node start = nodes.get(0);
        double elapsedTime = instance.getTimeWindow(start).getStart() + instance.getServiceTime(start);
        double battery = instance.getVehicleEnergyCapacity();
        double capacity = instance.getVehicleCapacity() - instance.getDemand(start);

        for (int i = 1; i < nodes.size(); i++) {
            Node current = nodes.get(i);
            Node prev = nodes.get(i - 1);
            battery -= instance.getTravelDistance(prev, current) * instance.getVehicleEnergyConsumption();
            elapsedTime += instance.getTravelTime(prev, current);
            elapsedTime += Math.max(instance.getTimeWindow(current).getStart() - elapsedTime, 0);
            capacity -= instance.getDemand(current);
            if (elapsedTime > instance.getTimeWindow(current).getEnd() || capacity < 0 || battery < 0) {
                return false;
            }
            elapsedTime += instance.getServiceTime(current);
            if (instance.isRechargingStation(current)) {
                elapsedTime += (instance.getVehicleEnergyCapacity() - battery) * instance.getRechargingRate(current);
                battery = instance.getVehicleEnergyCapacity();
            }
        }
        return true;
        //return !areTimeWindowsViolated() && !isTankCapacityViolated() && !isPayloadCapacityViolated();
    }

    public boolean isEmpty() {
        return nodes.size() <= 1;
    }

    public double getPayloadCapacity() {
        double totalDemand = 0;
        for (Node node : nodes) {
            if (instance.isCustomer(node)) {
                totalDemand += instance.getDemand(node);
            }
        }
        return totalDemand;
    }

    public boolean isPayloadCapacityViolated() {
        return getPayloadCapacity() > instance.getVehicleCapacity();
    }

    public boolean isTankCapacityViolated() {
        Node last = null;
        double tankCapacity = instance.getVehicleEnergyCapacity();
        for (Node node : nodes) {
            if (last != null) {
                double distance = instance.getTravelDistance(last, node);
                double consumption = distance * instance.getVehicleEnergyConsumption();

                tankCapacity -= consumption;
                if (tankCapacity < 0) {
                    return true;
                }
                if (instance.isRechargingStation(node)) {
                    tankCapacity = instance.getVehicleEnergyCapacity();
                }
            }
            last = node;
        }
        return false;
    }

    public boolean areTimeWindowsViolated() {
        double elapsedTime = instance.getTimeWindow(nodes.get(0)).getStart() + instance.getServiceTime(nodes.get(0));
        for (int i = 1; i < nodes.size(); i++) {
            Node currentNode = nodes.get(i);
            Node previousNode = nodes.get(i - 1);

            elapsedTime += instance.getTravelTime(previousNode, currentNode) / instance.getVehicleAverageVelocity();

            if (elapsedTime > instance.getTimeWindow(currentNode).getEnd()) {
                return true;
            }

            if (instance.isRechargingStation(currentNode)) {
                double missingEnergy = instance.getVehicleEnergyCapacity() - calculateRemainingTankCapacity(currentNode);
                double refillTime = missingEnergy * instance.getRechargingRate(currentNode);
                elapsedTime += refillTime;
            }

            double waitingTime = Math.max(instance.getTimeWindow(currentNode).getStart() - elapsedTime, 0);
            elapsedTime += waitingTime;
            elapsedTime += instance.getServiceTime(currentNode);
        }
        return false;
    }

    public double calculateArrivingTimeAtCustomer(Node customer) {
        double elapsedTime = instance.getTimeWindow(nodes.get(0)).getStart() + instance.getServiceTime(nodes.get(0));
        if (customer.equals(nodes.get(0))) {
            return elapsedTime;
        }
        for (int i = 1; i < nodes.size(); i++) {
            Node currentNode = nodes.get(i);
            Node previousNode = nodes.get(i - 1);

            elapsedTime += elapsedTime + instance.getTravelTime(previousNode, currentNode) / instance.getVehicleAverageVelocity();

            double waitingTime = Math.max(instance.getTimeWindow(currentNode).getStart() - elapsedTime, 0);
            elapsedTime += waitingTime;

            if (customer.equals(currentNode)) {
                return elapsedTime;
            }

            if (instance.isRechargingStation(currentNode)) {
                double missingEnergy = instance.getVehicleEnergyCapacity() - calculateRemainingTankCapacity(currentNode);
                double refillTime = missingEnergy * instance.getRechargingRate(currentNode);
                elapsedTime += refillTime;
            }

            elapsedTime += instance.getServiceTime(currentNode);
        }
        return 0;
    }

    public double getTotalDistance() {
        double distance = 0;
        Node last = null;
        for (Node node : nodes) {
            if (last != null) {
                distance += instance.getTravelDistance(last, node);
            }
            last = node;
        }
        return distance;
    }

    public double calculateRemainingTankCapacity(Node until) {
        Node last = null;
        double tankCapacity = instance.getVehicleEnergyCapacity();
        for (Node node : nodes) {
            if (last != null) {
                double distance = instance.getTravelDistance(last, node);
                double consumption = distance * instance.getVehicleEnergyConsumption();
                tankCapacity -= consumption;

                if (until.equals(node)) {
                    return tankCapacity;
                }

                if (instance.isRechargingStation(node)) {
                    tankCapacity = instance.getVehicleEnergyCapacity();
                }
            }
            last = node;
        }
        return tankCapacity;
    }

    public Integer getSize() {
        return nodes.size();
    }

    public Set<Customer> getCustomers() {
        Set<Customer> customers = new HashSet<>();
        for (Node node : nodes) {
            if (instance.isCustomer(node)) {
                customers.add(instance.getCustomer(node));
            }
        }
        return customers;
    }

    public void removeNode(Node node) {
        this.nodes.remove(node);
    }

    public Route deepClone() {
        List<Node> clonedNodes = nodes.stream()
                .map(Node::deepClone).collect(Collectors.toList());
        return new Route(instance, clonedNodes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(nodes, route.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes);
    }

    @Override
    public String toString() {
        return "Route{" +
                "nodes=" + nodes +
                '}';
    }
}
