package model;

public class Train extends Vehicle {
    private String lineName;
    private int coachCount;

    public Train(String vehicleID, int capacity, String status, String lineName, int coachCount) {
        super(vehicleID, "Train", capacity, status);
        this.lineName = lineName;
        this.coachCount = coachCount;
    }

    public String getLineName() { return lineName; }
    public int getCoachCount() { return coachCount; }
}