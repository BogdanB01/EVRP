package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EVRPTWInstance {
    private String name;
    private Depot depot;
    private List<Customer> customers;
    private Map<String, Customer> customerMap;
    private List<RechargingStation> rechargingStations;
    private Map<String, RechargingStation> rechargingStationMap;
    private List<Node> nodes;
    private BEVehicleType vehicleType;

    public EVRPTWInstance(String name, Depot depot, List<RechargingStation> rechargingStations,
                   List<Customer> customers, BEVehicleType vehicleType) {
        this.name = name;
        this.depot = depot;

        this.customerMap = new HashMap<>(customers.size() + 1, 1.0f);
        for(Customer c : customers) {
            this.customerMap.put(c.getName(), c);
        }
        this.customers = customers;

        this.rechargingStationMap = new HashMap<>(rechargingStations.size() + 1, 1.0f);
        for(RechargingStation r : rechargingStations) {
            this.rechargingStationMap.put(r.getName(), r);
        }
        this.rechargingStations = rechargingStations;

        this.nodes = new ArrayList<>(rechargingStations.size() + customers.size() + 1);
        nodes.add(depot);
        nodes.addAll(rechargingStations);
        nodes.addAll(customers);
        this.vehicleType = vehicleType;
    }

    public String getName() {
        return name;
    }

    public boolean isDepot(Node n) {
        return n.id == 0;
    }

    public boolean isRechargingStation(Node n) {
        return n.id > 0 && n.id < rechargingStations.size() + 1;
    }

    public boolean isCustomer(Node n) {
        return n.id > 0 && n.id > rechargingStations.size() && n.id < rechargingStations.size() + customers.size() + 1;
    }

    private boolean isMandatory(Node n) {
        return n.id > rechargingStations.size();
    }

    public RechargingStation getRechargingStation(String name) {
        return rechargingStationMap.get(name);
    }

    public RechargingStation getRechargingStation(Node n) {
        return rechargingStations.get(n.id - 1);
    }

    public Customer getCustomer(String name) {
        return customerMap.get(name);
    }

    public Customer getCustomer(Node n) {
        return customers.get(n.id - (rechargingStations.size() + 1));
    }

    public Depot getDepot() {
        return depot;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<RechargingStation> getRechargingStations() {
        return rechargingStations;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int getNumNodes() {
        return nodes.size();
    }

    public int getNumCustomers() {
        return customers.size();
    }

    public int getMaxVehicles() {
        return 12;
    }

    public double getVehicleCapacity() {
        return vehicleType.loadCapacity;
    }

    public double getVehicleEnergyCapacity() {
        return vehicleType.energyCapacity;
    }

    public double getVehicleAverageVelocity() {
        return vehicleType.averageVelocity;
    }

    public double getVehicleEnergyConsumption() {
        return vehicleType.energyConsumption;
    }

    private double calculateEuclideanDistance(Node n1, Node n2) {
        final Node.Location n1Loc = getLocation(n1);
        final Node.Location n2Loc = getLocation(n2);
        return Math.sqrt(Math.pow(n1Loc.x - n2Loc.x, 2)
                + Math.pow(n1Loc.y - n2Loc.y, 2));
    }

    public double getTravelDistance(Node n1, Node n2) {
        return calculateEuclideanDistance(n1, n2);
    }

    public double getTravelTime(Node n1, Node n2) {
        return getTravelDistance(n1, n2);
    }

    public double getDemand(Node node) {
        if (!isMandatory(node)) {
            return 0;
        }
        return customers.get(node.id - (rechargingStations.size() + 1)).getDemand();
    }

    private Node.Location getLocation(Node node) {
        if (node.id == depot.id) {
            return depot.getLocation();
        } else if (node.id > rechargingStations.size()) {
            return customers.get(node.id - (rechargingStations.size() + 1)).getLocation();
        }
        return rechargingStations.get(node.id - 1).getLocation();
    }

    public Node.TimeWindow getTimeWindow(Node node) {
        if (node.id == depot.id) {
            return depot.getTimeWindow();
        }
        else if (node.id > rechargingStations.size()) {
            return customers.get(node.id - (rechargingStations.size() + 1)).getTimeWindow();
        }
        return rechargingStations.get(node.id - 1).getTimeWindow();
    }

    public double getServiceTime(Node node) {
        if (!isMandatory(node)) return 0;
        return customers.get(node.id - (rechargingStations.size() + 1)).getServiceTime();
    }

    public double getRechargingRate(Node node) {
        if (!isRechargingStation(node)) return 0;
        return rechargingStations.get(node.id - 1).getRechargingRate();
    }

    static class VehicleType {
        final int id;
        final String name;

        private VehicleType(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class BEVehicleType extends VehicleType {
        final double energyCapacity, energyConsumption, loadCapacity, averageVelocity, fixedCosts;

        public BEVehicleType(int id, String name, double fuelCapacity, double fuelConsumption, double loadCapacity,
                             double averageVelocity,double fixedCosts) {
            super(id, name);
            this.energyCapacity = fuelCapacity;
            this.energyConsumption = fuelConsumption;
            this.loadCapacity = loadCapacity;
            this.averageVelocity = averageVelocity;
            this.fixedCosts = fixedCosts;
        }

        public double getLoadCapacity() {
            return loadCapacity;
        }
    }
}