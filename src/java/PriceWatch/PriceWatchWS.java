/**
 * File: PriceWatchWS.java
 *
 * This class contains the multiple operations of Price Watch.
 *
 * @author Sushil Mohite
 */
package PriceWatch;

import com.mongodb.client.FindIterable;
import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.bson.Document;
import org.json.simple.JSONObject;

/**
 * REST Web Service
 *
 * @author Sushil Mohite
 */
@Path("resource")
public class PriceWatchWS {
    
    PWLogic pwLogic;

    /**
     * Creates a new instance of PriceWatchWS
     * @throws java.io.IOException
     */
    public PriceWatchWS() throws IOException {
        pwLogic = new PWLogic();
    }

    /**
     * PUT method for updating or creating an instance of PriceWatchWS
     *
     * @param gasStationId
     * @param fuelType
     * @param price
     * @return 
     * @throws java.io.IOException 
     */
    @GET
    @Path("product")
    @Consumes("text/plain")
    public String updatePrice(@QueryParam("gasStationId") int gasStationId, @QueryParam("fuelType") String fuelType, @QueryParam("price") double price) throws IOException {
        boolean result = false;
        try {
            ProductInfo productInfo = new ProductInfo();
            productInfo.setGasStationId(gasStationId);
            productInfo.setFuelType(fuelType);
            productInfo.setPrice(price);
            
            pwLogic.updateLatestPrice(productInfo);         
            
            if (price <= pwLogic.getSafeValue(productInfo.getGasStationId(), productInfo.getFuelType())) {
                pwLogic.evaluate(productInfo);
            }
            
            result = true;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        JSONObject obj = new JSONObject();
        obj.put("result", result);
        return obj.toJSONString();
    }

    /**
     * PUT method for updating or creating an instance of PriceWatchWS
     * 
     * @param userName
     * @param password
     * @param gasStationId
     * @param fuelType
     * @param targetAddress
     * @param targetLat
     * @param targetLong
     * @param price
     * @param distance
     * @param myLat
     * @param myLong
     * @return 
     * @throws java.io.IOException
     */
    @GET
    @Path("client")
    @Consumes("text/plain")
    public String subscribe(@QueryParam("userName") String userName, @QueryParam("password") String password, @QueryParam("gasStationId") int gasStationId, @QueryParam("fuelType") String fuelType, @QueryParam("targetAddress") String targetAddress, @QueryParam("targetLat") double targetLat, @QueryParam("targetLong") double targetLong, @QueryParam("price") double price, @QueryParam("distance") double distance, @QueryParam("myLat") double myLat, @QueryParam("myLong") double myLong) throws IOException {
        String userId = null;
        try {
            if (pwLogic.isUserRegistered(userName, password)) {
                Trigger trigger = new Trigger(userName);
                trigger.setGasStationId(gasStationId);
                trigger.setFuelType(fuelType);
                trigger.setTargetAddress(targetAddress);
                trigger.setTargetLatitude(targetLat);
                trigger.setTargetLongitude(targetLong);
                trigger.setPrice(price);
                trigger.setDistance(distance);
                trigger.setMyLatitude(myLat);
                trigger.setMyLongitude(myLong);
                userId = pwLogic.addTrigger(trigger);
            
                pwLogic.evaluateContainersForGasStationId(trigger.getGasStationId());
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        JSONObject obj = new JSONObject();
        obj.put("userId", userId);
        return obj.toJSONString();
    }
    
    /**
     * 
     * @param userName
     * @param password
     * @param mylat
     * @param mylong
     * @return
     * @throws IOException 
     */
    @GET
    @Path("client/location")
    @Consumes("text/plain")
    public String updateLocation(@QueryParam("userName") String userName, @QueryParam("password") String password, @QueryParam("mylat") double mylat, @QueryParam("mylong") double mylong) throws IOException {
        boolean result = false;
        try {
            if (pwLogic.isUserRegistered(userName, password)) {
                LocationInfo locationInfo = new LocationInfo();
                locationInfo.setUserName(userName);
                locationInfo.setMyLat(mylat);
                locationInfo.setMyLong(mylong);
        
                pwLogic.updateMyLocation(locationInfo);
                pwLogic.evaluateLocation(locationInfo);
                result = true;
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        JSONObject obj = new JSONObject();
        obj.put("result", result);
        return obj.toJSONString();
    }
    
    @GET
    @Path("register")
    @Consumes("text/plain")
    public String register(@QueryParam("userName") String userName, @QueryParam("password") String password) {
        boolean result = false;
        try {
            result = pwLogic.registerUser(userName, password);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        JSONObject obj = new JSONObject();
        obj.put("result", result);
        return obj.toJSONString();
    }
    
    @GET
    @Path("login")
    @Consumes("text/plain")
    public String login(@QueryParam("userName") String userName, @QueryParam("password") String password) {
        boolean result = false;
        try {
            // Confirm the username and password
            if (pwLogic.isUserRegistered(userName, password)) {
                // Construct a JSON string of all the trigger related data and return
                return pwLogic.getUserTriggerData(userName);
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        JSONObject obj = new JSONObject();
        obj.put("result", result);
        return obj.toJSONString();
    }
    
    @GET
    @Path("delete")
    @Consumes("text/plain")
    public String delete(@QueryParam("userName") String userName, @QueryParam("password") String password, @QueryParam("triggerId") String triggerId) {
        boolean result = false;
        try {
            // Confirm the username and password
            if (pwLogic.isUserRegistered(userName, password)) {
                // Delete trigger
                result = pwLogic.deleteTrigger(triggerId);
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        JSONObject obj = new JSONObject();
        obj.put("result", result);
        return obj.toJSONString();
    }
    
    @GET
    @Path("display")
    @Consumes("text/plain")
    public String getData() {
        FindIterable<Document> iterable = pwLogic.triggerCol.find();
        FindIterable<Document> iterable2 = pwLogic.containerCol.find();
        FindIterable<Document> iterable3 = pwLogic.latestPriceCol.find();
        FindIterable<Document> iterable4 = pwLogic.usersCol.find();
        
        StringBuilder s = new StringBuilder();
        s.append("Triggers");
        for (Document document : iterable) {
            System.out.println(document);
            s.append(document);
            s.append('\n');
        }
        
        s.append("Safe Value Container");
        for (Document document : iterable2) {
            System.out.println(document);
            s.append(document);
            s.append('\n');
        }
        
        s.append("Latest Price");
        for (Document document : iterable3) {
            System.out.println(document);
            s.append(document);
            s.append('\n');
        }
        
        s.append("Users");
        for (Document document : iterable4) {
            System.out.println(document);
            s.append(document);
            s.append('\n');
        }
        
        return s.toString();
    }
}
