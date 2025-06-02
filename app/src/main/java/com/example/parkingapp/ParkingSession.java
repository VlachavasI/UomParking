package com.example.parkingapp;

public class ParkingSession {
    private String licensePlate;
    private String location;
    private String duration;
    private long startTime;
    private long endTime;
    private double cost;
    private String date;

    // Constructor with basic fields (as used in the database query)
    public ParkingSession(String licensePlate, String location, String duration) {
        this.licensePlate = licensePlate;
        this.location = location;
        this.duration = duration;
        this.cost = 0.0; // Default cost
        this.date = getCurrentDate();
    }

    // Full constructor for more detailed sessions
    public ParkingSession(String licensePlate, String location, String duration,
                          long startTime, long endTime, double cost, String date) {
        this.licensePlate = licensePlate;
        this.location = location;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.cost = cost;
        this.date = date;
    }

    // Getters
    public String getLicensePlate() {
        return licensePlate;
    }

    public String getLocation() {
        return location;
    }

    public String getDuration() {
        return duration;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public double getCost() {
        return cost;
    }

    public String getDate() {
        return date;
    }

    // Setters
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Utility methods
    public int getDurationInMinutes() {
        try {
            String[] parts = duration.split(" ");
            if (parts.length >= 2) {
                int value = Integer.parseInt(parts[0]);
                String unit = parts[1].toLowerCase();

                if (unit.contains("hour")) {
                    return value * 60;
                } else if (unit.contains("minute")) {
                    return value;
                }
            }
        } catch (NumberFormatException e) {
            // If parsing fails, try to extract just the number
            try {
                return Integer.parseInt(duration.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        return 0;
    }

    public String getFormattedCost() {
        return String.format("$%.2f", cost);
    }

    public String getFormattedDuration() {
        int minutes = getDurationInMinutes();
        if (minutes >= 60) {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + (hours == 1 ? " hour" : " hours");
            } else {
                return hours + (hours == 1 ? " hour " : " hours ") +
                        remainingMinutes + (remainingMinutes == 1 ? " minute" : " minutes");
            }
        } else {
            return minutes + (minutes == 1 ? " minute" : " minutes");
        }
    }

    private String getCurrentDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    // Override toString for debugging
    @Override
    public String toString() {
        return "ParkingSession{" +
                "licensePlate='" + licensePlate + '\'' +
                ", location='" + location + '\'' +
                ", duration='" + duration + '\'' +
                ", cost=" + cost +
                ", date='" + date + '\'' +
                '}';
    }

    // Override equals and hashCode for proper object comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ParkingSession that = (ParkingSession) obj;
        return Double.compare(that.cost, cost) == 0 &&
                startTime == that.startTime &&
                endTime == that.endTime &&
                java.util.Objects.equals(licensePlate, that.licensePlate) &&
                java.util.Objects.equals(location, that.location) &&
                java.util.Objects.equals(duration, that.duration) &&
                java.util.Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(licensePlate, location, duration, startTime, endTime, cost, date);
    }
}