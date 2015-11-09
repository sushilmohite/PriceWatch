/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PriceWatch;

/**
 *
 * @author Sushil Mohite
 */
public class LocationInfo {
    private String userName;
    private double myLat;
    private double myLong;

    /**
     * @return the userId
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the myLat
     */
    public double getMyLat() {
        return myLat;
    }

    /**
     * @param myLat the myLat to set
     */
    public void setMyLat(double myLat) {
        this.myLat = myLat;
    }

    /**
     * @return the myLong
     */
    public double getMyLong() {
        return myLong;
    }

    /**
     * @param myLong the myLong to set
     */
    public void setMyLong(double myLong) {
        this.myLong = myLong;
    }
}
