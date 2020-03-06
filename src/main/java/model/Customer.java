package model;

import java.util.Comparator;

public class Customer extends Node {
    private final String name;
    private Location location;
    private final double demand;
    private final TimeWindow timeWindow;
    private final double serviceTime;

    public static final Comparator<Customer> WINDOW_END_COMPARATOR =
            Comparator.comparing(c -> c.getTimeWindow().getEnd());

    public static final Comparator<Customer> ANGLE_COMPARATOR =
            Comparator.comparing(Customer::getAngleFromOrigin);

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

    public void translateByDepot(Depot depot) {
        this.location.x -= depot.getLocation().x;
        this.location.y -= depot.getLocation().y;
    }

    public double getAngleFromOrigin() {
        double angle = Math.toDegrees(Math.atan2(this.location.y, this.location.x));
        return angle < 0 ? angle + 360 : angle;
    }

    @Override
    public String toString() {
        return name;
    }
}