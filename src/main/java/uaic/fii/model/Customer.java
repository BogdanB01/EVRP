package uaic.fii.model;

public class Customer extends Node {
    private final String name;
    private Location location;
    private final double demand;
    private final TimeWindow timeWindow;
    private final double serviceTime;

    public Customer(int id, String name, double x, double y, double start, double end, double demand,
             double serviceTime) {
        super(id);
        this.name = name;
        this.location = new Location(x, y);
        this.demand = demand;
        this.timeWindow = new TimeWindow(start, end);
        this.serviceTime = serviceTime;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public double getDemand() {
        return demand;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public double getServiceTime() {
        return serviceTime;
    }

    @Override
    public String toString() {
        return name;
    }
}