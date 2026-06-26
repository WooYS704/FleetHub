package model;

public class Bus extends Vehicle {
    private String routeNumber;
    private String driverName;

    public Bus(String vehicleID, int capacity, String status, String routeNumber, String driverName) {
        super(vehicleID, "Bus", capacity, status);
        this.routeNumber = routeNumber;
        this.driverName = driverName;
    }

    public String getRouteNumber() { return routeNumber; }
    public String getDriverName() { return driverName; }
}