/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kor.yaApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import tabelkarnel.wrk.WorkerSQL;

/**
 *
 * @author Artem
 */
public class TakePayYa {
    Map<String, Integer> driverBalanceYa = new HashMap<>();
    Map<Integer, Map> driverList = new HashMap<>();
    String apiKey;
    WorkerSQL sw = new WorkerSQL();
    public TakePayYa( String key) throws ClassNotFoundException, SQLException{
        apiKey = key;
        driverList = sw.getDriverBalance();
    }
    public void takePay(Map dB) throws IOException, SQLException{
        driverBalanceYa = dB;
        Set keysDriver = driverList.keySet();
        for(Object key : keysDriver){
            Map row = (Map) driverList.get(key);
            try{
                int yaBalance = (int) driverBalanceYa.get(row.get("yaId"));
                int driverDebt = (int) row.get("debt");
                int takenSum = 0;
                if(yaBalance > 200 && (yaBalance-200) > 100){
                    if((driverDebt*-1) < (yaBalance-200)){
                        takenSum = driverDebt*-1;
                    }
                    else{
                        takenSum =(yaBalance-200);
                    }
                    System.out.println("DriverId: "+key + "  DriverDebt:" + row.get("debt")
                        + "  DriverYaId: " + row.get("yaId")+" yaBalance:"+driverBalanceYa.get(row.get("yaId")));
                    System.out.println("I will take pay:"+takenSum);
                    if(minusYaBalance((String) row.get("yaId"), takenSum)){
                        sw.addPayDriver((int) key, takenSum, 2);
                    }
                }
            }
            catch(Exception ex){
                System.out.println(ex.getMessage()+"");
            }
        }
    }
    boolean minusYaBalance(String yaId, int sum) throws MalformedURLException, IOException{
        String url = "https://taximeter.yandex.rostaxi.org/api/driver/balance/minus?apikey="+apiKey
                +"&driver="+yaId+"&sum="+sum+"&description=Arenda";

        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        if(connection.getResponseCode()==200){
            return true;
        }
        else{
            System.err.println("No taken!!!"+connection.getResponseCode());
            return false;
        }
    }
}
