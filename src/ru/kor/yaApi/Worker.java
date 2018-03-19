/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kor.yaApi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import tabelkarnel.wrk.WorkerSQL;

/**
 *
 * @author korgan
 */
public class Worker {
    WorkerSQL sw = null;
    String apiKey = "d8435ac61cda4b9fa760e8e6c0ccbc1e";
    public Worker() throws ClassNotFoundException, SQLException{
        sw = new WorkerSQL();
    }
    public Map getDriverListInYa() throws MalformedURLException, IOException, SQLException{
        Map<String, Integer> driverBalanceList = new HashMap<String, Integer>();
        String url = "https://taximeter.yandex.rostaxi.org/api/driver/balance?apikey="+apiKey;
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JsonObject driverList = jsonMaker(response.toString());
        for(Map.Entry<String, JsonElement> entry : driverList.entrySet()){
            driverBalanceList.put(entry.getKey(), entry.getValue().getAsInt());
        }
        System.out.println("I have take driver/balance list");
        return driverBalanceList;
    }
    private void driverDataGet(String driverId) throws MalformedURLException, IOException{
        String url = "https://taximeter.yandex.rostaxi.org/api/driver/get?apikey="+apiKey+"&id="+driverId;

        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JsonObject driverData = jsonMaker(response.toString());
        JsonObject driver = (JsonObject) driverData.get("driver");
        int driverIdInSRM = 0;
        String phone = "";
        try{
            phone = driver.get("Phones").toString().substring(3, 13);
            
            driverIdInSRM = sw.getDriverIdInTable(phone);
            System.out.println("\t First Name:" +driver.get("FirstName").getAsString()
                    + " LastName:" +driver.get("LastName").getAsString()
                    +" PhoneInYa:" +driver.get("Phones").getAsString()
                    + " PhoneForSearch:" +phone
                    +" Balance:"+ driverData.get("balance")
                    +" IdINCRS:"+ driverIdInSRM); 
            if(driverIdInSRM!=0){
                sw.setYaId(driverId, driverIdInSRM);
                System.err.println("Write into drivers list");
            }
            else{
                sw.addTMPList(driverId, driver.get("FirstName").getAsString(), 
                        driver.get("LastName").getAsString(), driver.get("Phones").getAsString());
                System.err.println("Write into drivers list");
            }
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    } 
    private JsonObject jsonMaker(String inputString){
        JsonParser parser = new JsonParser();
        JsonObject jsonObj;
        jsonObj = (JsonObject) parser.parse(inputString);
        return jsonObj;
    }

    public void getAllBalance() throws SQLException, IOException {
        Map driverBalanceList = this.getDriverListInYa();
        Set keysDriver = driverBalanceList.keySet();
        for(Object key : keysDriver){
            sw.updateDriverBalance((String) key, driverBalanceList.get(key).toString());
        }
    }
    public void getAllBalance(Map driverBalanceList) throws SQLException, IOException {
        Set keysDriver = driverBalanceList.keySet();
        for(Object key : keysDriver){
            sw.updateDriverBalance((String) key, driverBalanceList.get(key).toString());
        }
    }
    public void checkNewDriver() throws SQLException, IOException{
       Map driverBalanceList = this.getDriverListInYa();
       int counter = 0;
        Set keysDriver = driverBalanceList.keySet();
        for(Object key : keysDriver){
            counter++;
            if(!sw.checkYaIdInBase((String) key)){
                System.out.println("PP:"+counter + "  ID:" +key + "  Balance:"+driverBalanceList.get(key));
                System.out.println("\t New driver");
                driverDataGet((String) key);
            }
        } 
        getAllBalance(driverBalanceList);
    }
}


