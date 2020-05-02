package uaic.fii.solver.model;

public class Depot extends Node {
    private final String name;
    private final Location location;
    private final TimeWindow timeWindow;

    public Depot(int id, String name, double x, double y, double start, double end) {
        super(id);
        this.name = name;
        this.location = new Location(x, y);
        this.timeWindow = new TimeWindow(start, end);
    }

    public Depot(Depot depot) {
        this.name = depot.getName();
        this.location = depot.getLocation();
        this.timeWindow = depot.getTimeWindow();
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    @Override
    public String toString() {
        return name;
    }
}
