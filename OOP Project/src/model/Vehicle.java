package model;

public class Vehicle {
    private String vehicleID;
    private String type;
    private int capacity;
    private String status;

    public Vehicle(String vehicleID, String type, int capacity, String status) {
        this.vehicleID = vehicleID;
        this.type = type;
        this.capacity = capacity;
        this.status = status;
    }

    public String getVehicleID() { return vehicleID; }
    public String getType() { return type; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }

    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setStatus(String status) { this.status = status; }
}