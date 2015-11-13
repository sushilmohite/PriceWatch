/**
 * File: PWLogic.java
 *
 * This class contains the main logic of Price Watch implementation.
 *
 * @author Sushil Mohite
 */
package PriceWatch;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class PWLogic implements Serializable {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    MongoCollection triggerCol, containerCol, latestPriceCol, usersCol;

    public PWLogic() {
        mongoClient = new MongoClient();
        database = mongoClient.getDatabase(ServerConfig.MONGO_DB);
        triggerCol = database.getCollection(ServerConfig.TRIGGER_COLLECTION);
        containerCol = database.getCollection(ServerConfig.SAFE_VALUE_COLLECTION);
        latestPriceCol = database.getCollection(ServerConfig.LATEST_PRICE_COLLECTION);
        usersCol = database.getCollection(ServerConfig.USERS_COLLECTION);
    }

    /**
     * This method adds a new trigger with a predicate
     *
     * @param trigger
     * @return 
     */
    public String addTrigger(final Trigger trigger) {
        Document doc = new Document(Fields.USERNAME, trigger.getUserName())
                        .append(Fields.GAS_STATION_ID, trigger.getGasStationId())
                        .append(Fields.FUEL_TYPE, trigger.getFuelType())
                        .append(Fields.TARGET_ADDRESS, trigger.getTargetAddress())
                        .append(Fields.TARGET_LAT, trigger.getTargetLatitude())
                        .append(Fields.TARGET_LONG, trigger.getTargetLongitude())
                        .append(Fields.PRICE, trigger.getPrice())
                        .append(Fields.DISTANCE, trigger.getDistance())
                        .append(Fields.MY_LAT, trigger.getMyLatitude())
                        .append(Fields.MY_LONG, trigger.getMyLongitude());
        triggerCol.insertOne(doc);
        return doc.get(Fields.ID).toString();
    }

    /**
     * Evaluates whether any triggers should be fired or not.
     *
     * @param productInfo
     */
    public void evaluate(final ProductInfo productInfo) {
        FindIterable<Document> triggers = triggerCol.find(new Document(Fields.GAS_STATION_ID, productInfo.getGasStationId())
                .append(Fields.FUEL_TYPE, productInfo.getFuelType()));
        for (Document trigger : triggers) {
            if (productInfo.getPrice() <= trigger.getDouble(Fields.PRICE)) {
                double distance = calculateDistance(trigger.getDouble(Fields.MY_LAT), 
                        trigger.getDouble(Fields.MY_LONG), 
                        trigger.getDouble(Fields.TARGET_LAT), 
                        trigger.getDouble(Fields.TARGET_LONG));
                if (distance <= trigger.getDouble(Fields.DISTANCE)) {  
                    //Send notification
                    sendNotification();
                }
            }
        }
        
        // Calculate Safe Values
        setSafeValue(productInfo);
    }
    
    // TODO
    private void sendNotification() {
        System.out.println("Notification sent");
    }
    
    public void evaluateLocation(final LocationInfo locationInfo) {
        FindIterable<Document> triggers = triggerCol.find(new Document(Fields.USERNAME, locationInfo.getUserName()));
        for (Document trigger : triggers) {
            double distance = calculateDistance(trigger.getDouble(Fields.MY_LAT), 
                    trigger.getDouble(Fields.MY_LONG), 
                    trigger.getDouble(Fields.TARGET_LAT), 
                    trigger.getDouble(Fields.TARGET_LONG));
            if (distance <= trigger.getDouble(Fields.DISTANCE)) {
                Document latestPrice = (Document) latestPriceCol.find(new Document(Fields.ID, 
                        trigger.getInteger(Fields.GAS_STATION_ID))).first();                
                if ((latestPrice.getDouble(trigger.getString(Fields.FUEL_TYPE)) <= trigger.getDouble(Fields.PRICE)) && 
                        (trigger.getDouble(Fields.PRICE) > 0)) {
                    // Send notification
                    sendNotification();
                }
            }
        }
    }
    
    public void updateMyLocation(final LocationInfo locationInfo) {
        triggerCol.updateMany(new Document(Fields.USERNAME, locationInfo.getUserName()), 
                new Document(ServerConfig.MONGO_UPDATE_KEY, new Document(Fields.MY_LAT, locationInfo.getMyLat())
                        .append(Fields.MY_LONG, locationInfo.getMyLong())));
    }
    
    private void setSafeValue(final ProductInfo productInfo) {
        FindIterable<Document> triggers = triggerCol.find(new Document(Fields.GAS_STATION_ID, productInfo.getGasStationId())
                .append(Fields.FUEL_TYPE, productInfo.getFuelType()));
        double highMin = 0;
        for (Document trigger : triggers) {
            double triggerPrice = trigger.getDouble(Fields.PRICE);
            if (triggerPrice > highMin) {
                highMin = triggerPrice;
            }
        }
        
        containerCol.updateOne(new Document(Fields.ID, productInfo.getGasStationId()), 
                new Document(ServerConfig.MONGO_UPDATE_KEY, new Document(productInfo.getFuelType(), highMin)));
    }
    
    public boolean registerUser(String userName, String password) {
        Document user = (Document) usersCol.find(new Document(Fields.USERNAME, userName)).first();
        
        if (user == null) {
            usersCol.insertOne(new Document(Fields.USERNAME, userName).append(Fields.PASSWORD, password));
            return true;
        }
        return false;
    }
    
    public boolean isUserRegistered(final String userName, final String password) {
        Document user = (Document) usersCol.find(new Document(Fields.USERNAME, userName).append(Fields.PASSWORD, password)).first();
        return user != null;
    }
    
    private void notifyUpdateService(final int gasStationId) {
        try {
            Socket socket = new Socket(ServerConfig.UPDATE_SERVICE_IP, ServerConfig.UPDATE_SERVICE_PORT);
            
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(gasStationId);
                out.flush();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void updateLatestPrice(final ProductInfo productInfo) {
        latestPriceCol.updateOne(new Document(Fields.ID, productInfo.getGasStationId()), 
                new Document(ServerConfig.MONGO_UPDATE_KEY, new Document(productInfo.getFuelType(), productInfo.getPrice())));
    }
    
    public double getSafeValue(final int gasStationId, final String fuelType) {
        Document container = (Document) containerCol.find(new Document(Fields.ID, gasStationId)).first();
        return container.getDouble(fuelType);
    }
    
    public void evaluateContainersForGasStationId(final int gasStationId) {
        Document container = (Document) containerCol.find(new Document(Fields.ID, gasStationId)).first();
        if (container == null) {
            containerCol.insertOne(new Document(Fields.ID, gasStationId)
                    .append(Fields.REG, ServerConfig.INITIAL_PRICE)
                    .append(Fields.MID, ServerConfig.INITIAL_PRICE)
                    .append(Fields.PRE, ServerConfig.INITIAL_PRICE));
                
            latestPriceCol.insertOne(new Document(Fields.ID, gasStationId)
                    .append(Fields.REG, ServerConfig.INITIAL_PRICE)
                    .append(Fields.MID, ServerConfig.INITIAL_PRICE)
                    .append(Fields.PRE, ServerConfig.INITIAL_PRICE));
                
            //Send data to update price service
            notifyUpdateService(gasStationId);
        }
    }
    
    public boolean deleteTrigger(String triggerId) {
        ObjectId objId = new ObjectId(triggerId);
        Document trigger = (Document) triggerCol.find(new Document(Fields.ID, objId)).first();
        
        if (trigger != null) {
            triggerCol.deleteOne(trigger);
            return true;
        }
        return false;
    }
    
    public String getUserTriggerData(String userName) {
        FindIterable<Document> triggers = triggerCol.find(new Document(Fields.USERNAME, userName));
        // Create array of triggers
        JSONArray userTriggers = new JSONArray();
        for (Document trigger : triggers) {
            JSONObject obj = new JSONObject();
            obj.put(Fields.ID, trigger.getObjectId(Fields.ID).toString());
            obj.put(Fields.TARGET_ADDRESS, trigger.getString(Fields.TARGET_ADDRESS));
            obj.put(Fields.PRICE, trigger.getDouble(Fields.PRICE));
            obj.put(Fields.DISTANCE, trigger.getDouble(Fields.DISTANCE));
            userTriggers.add(obj);
        }
        
        JSONObject finalObj = new JSONObject();
        finalObj.put(Fields.RESULT, true);
        finalObj.put(Fields.TRIGGERS, userTriggers);
        return finalObj.toJSONString();
    }

    /*
     * To calculate distance between user and Market
     */
    private double calculateDistance(double latitudeMobile, double longitudeMobile, double latitudeMarket, double longitudeMarket) {
        double t = longitudeMobile - longitudeMarket;
        double distance = Math.sin(degree2radians(latitudeMobile)) * Math.sin(degree2radians(latitudeMarket)) + Math.cos(degree2radians(latitudeMobile)) * Math.cos(degree2radians(latitudeMarket)) * Math.cos(degree2radians(t));
        distance = Math.acos(distance);
        distance = (distance * 180.0 / Math.PI);
        distance = distance * 60 * 1.1515;
        return (distance);
    }

    /*
     * To convert degrees to radians            
     */
    private double degree2radians(double deg) {
        double radians = (deg * Math.PI / 180.0);
        return radians;
    }
}
