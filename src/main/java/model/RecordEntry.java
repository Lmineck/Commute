package model;

public class RecordEntry {
    private final String arrivalTime;
    private final String leaveTime;
    private final String workDuration;

    public RecordEntry(String arrivalTime, String leaveTime, String workDuration) {
        this.arrivalTime = arrivalTime;
        this.leaveTime = leaveTime;
        this.workDuration = workDuration;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getLeaveTime() {
        return leaveTime;
    }

    public String getWorkDuration() {
        return workDuration;
    }
}
