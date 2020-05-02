package uaic.fii.model;

import uaic.fii.solver.model.Customer;
import uaic.fii.solver.model.Depot;
import uaic.fii.solver.model.RechargingStation;

import java.util.List;

public class Instance {
    private String name;
    private String path;
    private List<Customer> customers;
    private List<RechargingStation> rechargingStations;
    private Depot depot;

    public Instance(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public Instance(String name, String path, List<Customer> customers, List<RechargingStation> rechargingStations, Depot depot) {
        this(name, path);
        this.customers = customers;
        this.rechargingStations = rechargingStations;
        this.depot = depot;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public List<RechargingStation> getRechargingStations() {
        return rechargingStations;
    }

    public void setRechargingStations(List<RechargingStation> rechargingStations) {
        this.rechargingStations = rechargingStations;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
