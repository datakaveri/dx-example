package com.example.IoT.models;

public class Device {
    private String deviceId;
    private String domain;
    private String state;
    private String city;
    private Location location;
    private String deviceType;

    // Getters and setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    // Inner class for Location
    public static class Location {
        private String type;
        private double[] coordinates;

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public double[] getCoordinates() { return coordinates; }
        public void setCoordinates(double[] coordinates) { this.coordinates = coordinates; }
    }
}
