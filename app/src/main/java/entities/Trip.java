package entities;


import com.google.firebase.database.Exclude;

import java.sql.Time;
import java.util.Date;

public class Trip {


    //public static Long TripCounter = 0L;

    public String getTripID() {
        return TripID;
    }

    public void setTripID(String tripID) {
        TripID = tripID;
    }

    public void setDropOffTime(String dropOffTime) {
        this.dropOffTime = dropOffTime;
    }

    public enum TripStatus {AVAILABLE, INPROCESS, DONE}

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPickUpTime() {
        return pickUpTime;
    }
    public String getDropOffTime() {
        return dropOffTime;
    }

    public void setPickUpTime(String pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerNumber() {
        return passengerNumber;
    }

    public void setPassengerNumber(String passengerNumber) {
        this.passengerNumber = passengerNumber;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public TripStatus getStatus() {
        return TripStatus;
    }

    public void setStatus(TripStatus status) {
        this.TripStatus = status;
    }

    public String getDriverID() {
        return DriverID;
    }

    public void setDriverID(String driverID) {
        DriverID = driverID;
    }

    private TripStatus TripStatus;
    private String DriverID;
    private String TripID;
    private String currentLocation;
    private String destination;
    private String pickUpTime;
    private String dropOffTime;
    private String passengerName;
    private String passengerNumber;
    private String passengerEmail;

    public Trip(String DriverID, String currentLocation, String destination, String pickUpTime, String dropOffTime, String passengerName, String passengerNumber, String passengerEmail) {
        this.TripStatus = TripStatus.AVAILABLE;
        this.currentLocation = currentLocation;
        this.destination = destination;
        this.pickUpTime = pickUpTime;
        this.dropOffTime = dropOffTime;
        this.passengerName = passengerName;
        this.passengerNumber = passengerNumber;
        this.passengerEmail = passengerEmail;
        this.DriverID = DriverID;
    }

    public Trip(Trip t) {
        this.TripID = t.TripID;
        this.TripStatus = t.TripStatus;
        this.currentLocation = t.currentLocation;
        this.destination = t.destination;
        this.pickUpTime = t.pickUpTime;
        this.dropOffTime = t.dropOffTime;
        this.passengerName = t.passengerName;
        this.passengerNumber = t.passengerNumber;
        this.passengerEmail = t.passengerEmail;
        this.DriverID = t.DriverID;
    }
    public Trip()
    {}
}

