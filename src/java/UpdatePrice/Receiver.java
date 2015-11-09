/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UpdatePrice;

import com.mongodb.client.MongoCollection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import org.bson.Document;

/**
 *
 * @author Sushil Mohite
 */
public class Receiver extends Thread {
    
    MongoCollection latestPriceCol;
    
    public Receiver(MongoCollection latestPriceCol) {
        this.latestPriceCol = latestPriceCol;
    }
    
    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(Config.UPDATE_SERVICE_PORT);
            
            while (true) {
                new HandleRequest(ss.accept()).start();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    private class HandleRequest extends Thread {
        
        Socket socket;
        
        public HandleRequest(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder stringBuffer = new StringBuilder();
                String line;
                
                while ((line = in.readLine()) != null) {
                    stringBuffer.append(line);
                }
                in.close();
                
                int gasStationId = Integer.parseInt(stringBuffer.toString());
                
                Document latestPrice = (Document) latestPriceCol.find(new Document("_id", gasStationId)).first();
                if (latestPrice == null) {
                    latestPriceCol.insertOne(new Document("_id", gasStationId)
                            .append("reg", Config.INITIAL_PRICE)
                            .append("mid", Config.INITIAL_PRICE)
                            .append("pre", Config.INITIAL_PRICE)); 
                }
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
    
}
