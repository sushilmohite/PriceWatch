/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UpdatePrice;

/**
 *
 * @author Sushil Mohite
 */
public class Config {
    public final static String PRICE_WATCH_URL = "http://localhost:8080";
    public final static String PRICE_WATCH_API = "/PriceWatch/webresources/resource?";
    
    public final static String GAS_FEED_URL = "http://api.mygasfeed.com";
    public final static String GAS_STATION_API = "/stations/details/";
    public final static String GAS_FEED_KEY = "mwl3tdh66y.json?";
    
    public final static int UPDATE_SERVICE_PORT = 25;
    
    public final static double INITIAL_PRICE = -1.0;
    public final static String MONGO_DB = "test2";
    public final static String LATEST_PRICE_COLLECTION = "latest_test";
    public final static String MONGO_UPDATE_KEY = "$set";
}
