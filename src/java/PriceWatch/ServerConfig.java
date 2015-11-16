/**
 * File: ServerConfig.java
 *
 * @author Sushil Mohite
 */
package PriceWatch;

/**
 *
 * @author Sushil Mohite
 */
public class ServerConfig {
    public final static String UPDATE_SERVICE_IP = "localhost";
    public final static int UPDATE_SERVICE_PORT = 25;
    
    public final static double INITIAL_PRICE = -1.0;
    public final static String MONGO_DB = "test";
    public final static String TRIGGER_COLLECTION = "test1";
    public final static String SAFE_VALUE_COLLECTION = "safe_test";
    public final static String LATEST_PRICE_COLLECTION = "latest_test";
    public final static String USERS_COLLECTION = "users_test";
    public final static String MONGO_UPDATE_KEY = "$set";
    
    public final static String TARGET_SEQ = "_";
    public final static String REPLACEMENT_SEQ = " ";
}
