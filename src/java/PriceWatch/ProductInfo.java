/**
 * File: ProductInfo.java
 * 
 * This class represents information of a product.
 * 
 * @author Sushil Mohite
 */
package PriceWatch;

import java.io.Serializable;

public class ProductInfo implements Serializable {
    
    private int gasStationId;
    private String fuelType;
    private double price;

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
    
}
