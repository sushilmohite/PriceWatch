/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UpdatePrice;

import PriceWatch.Fields;
import PriceWatch.ServerConfig;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Sushil Mohite
 */
public class Sender extends Thread {
    
    MongoCollection latestPriceCol;
    
    public Sender(MongoCollection latestPriceCol) {
        this.latestPriceCol = latestPriceCol;
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                FindIterable<Document> iterable = latestPriceCol.find();
                        
                for (Document doc : iterable) {
                    URL gasFeedUrl = new URL(Config.GAS_FEED_URL + Config.GAS_STATION_API + doc.getInteger("_id") + "/" + Config.GAS_FEED_KEY);
        
                    HttpURLConnection connection = (HttpURLConnection) gasFeedUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    StringBuffer stringBuffer;
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;
                        stringBuffer = new StringBuffer();
                        while ((line = in.readLine()) != null) {
                            stringBuffer.append(line);
                        }
                    }
                    
                    System.out.println(stringBuffer);
                    
                    // Parse, Update price and notify Price Watch if necessary
                    JSONParser parser = new JSONParser();
                    JSONObject response = (JSONObject) parser.parse(stringBuffer.toString());
                    response = (JSONObject)response.get("details");
                    
                    extractPrice(response);
                    
                    //System.out.println(response.get("reg_price"));
                    //System.out.println(response.get("mid_price"));
                    //System.out.println(response.get("pre_price"));
                }
                
                this.sleep(10000);
            } catch (IOException | ParseException | InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
    
    private void extractPrice(JSONObject response) {
        int gasStationId = (int) response.get("id");
        double reg_price = (double) response.get("reg_price");
        double mid_price = (double) response.get("mid_price");
        double pre_price = (double) response.get("pre_price");
        
        checkPrice(gasStationId, "reg", reg_price);
        checkPrice(gasStationId, "mid", mid_price);
        checkPrice(gasStationId, "pre", pre_price);
    }
    
    private void checkPrice(int gasStationId, String fuelType, double price) {
        try {
            Document latestPrice = (Document) latestPriceCol.find(new Document("_id", gasStationId));
            if (latestPrice.getDouble(fuelType) != price) {
                //Send update
                URL priceWatchUrl = new URL(Config.PRICE_WATCH_URL + Config.PRICE_WATCH_API 
                        + "gasStationId=" + gasStationId 
                        + "&" + "fuelType=" + fuelType
                        + "&" + "price=" + price);
        
                HttpURLConnection connection = (HttpURLConnection) priceWatchUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                StringBuilder stringBuffer;
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    stringBuffer = new StringBuilder();
                    while ((line = in.readLine()) != null) {
                        stringBuffer.append(line);
                    }
                }
                
                // check if true
                System.out.println(stringBuffer.toString());
            }
            latestPriceCol.updateOne(new Document(Fields.ID, gasStationId), 
                new Document(ServerConfig.MONGO_UPDATE_KEY, new Document(fuelType, price)));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
