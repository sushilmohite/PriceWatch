/**
 * File: Trigger.java
 *
 * This object of this class represents a trigger created for a user.
 *
 * @author Sushil Mohite
 */
package PriceWatch;

import java.io.Serializable;

public class Trigger implements Serializable {

    private final String userName;
    private int gasStationId;
    private String gasStationName;
    private String fuelType;
    private String address;
    private double targetLatitude;
    private double targetLongitude;
    private double price;
    private double distance;
    private double myLatitude;
    private double myLongitude;
    
    public Trigger(String userName) {
        this.userName = userName;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the gasStationId
     */
    public int getGasStationId() {
        return gasStationId;
    }

    /**
     * @param gasStationId the gasStationId to set
     */
    public void setGasStationId(int gasStationId) {
        this.gasStationId = gasStationId;
    }

    /**
     * @return the fuelType
     */
    public String getFuelType() {
        return fuelType;
    }

    /**
     * @param fuelType the fuelType to set
     */
    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    /**
     * @return the targetLatitude
     */
    public double getTargetLatitude() {
        return targetLatitude;
    }

    /**
     * @param targetLatitude the targetLatitude to set
     */
    public void setTargetLatitude(double targetLatitude) {
        this.targetLatitude = targetLatitude;
    }

    /**
     * @return the targetLongitude
     */
    public double getTargetLongitude() {
        return targetLongitude;
    }

    /**
     * @param targetLongitude the targetLongitude to set
     */
    public void setTargetLongitude(double targetLongitude) {
        this.targetLongitude = targetLongitude;
    }

    /**
     * @return the price
     */
    public double getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * @return the myLatitude
     */
    public double getMyLatitude() {
        return myLatitude;
    }

    /**
     * @param myLatitude the myLatitude to set
     */
    public void setMyLatitude(double myLatitude) {
        this.myLatitude = myLatitude;
    }

    /**
     * @return the myLongitude
     */
    public double getMyLongitude() {
        return myLongitude;
    }

    /**
     * @param myLongitude the myLongitude to set
     */
    public void setMyLongitude(double myLongitude) {
        this.myLongitude = myLongitude;
    }

    /**
     * @return the targetAddress
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the gasStationName
     */
    public String getGasStationName() {
        return gasStationName;
    }

    /**
     * @param gasStationName the gasStationName to set
     */
    public void setGasStationName(String gasStationName) {
        this.gasStationName = gasStationName;
    }
}
