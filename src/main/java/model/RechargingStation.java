package model;

public class RechargingStation extends Node {
    private final String name;
    private final Location location;
    private final TimeWindow timeWindow;
    private final double rechargingRate;

    public RechargingStation(int id, String name, double x, double y, double start, double end,
                      double rechargingRate) {
        super(id);
        this.name = name;
        this.location = new Location(x, y);
        this.timeWindow = new TimeWindow(start, end);
        this.rechargingRate = rechargingRate;
    }

    public RechargingStation(RechargingStation rechargingStation) {
        this.name = rechargingStation.getName();
        this.location = rechargingStation.getLocation();
        this.timeWindow = rechargingStation.getTimeWindow();
        this.rechargingRate = rechargingStation.getRechargingRate();
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

    public double getRechargingRate() {
        return rechargingRate;
    }

    @Override
    public String toString() {
        return name;
    }
}

