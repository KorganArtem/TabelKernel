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
    String apiKey = "d8435ac61cda4b9fa760e8e6c0ccbc1e";
    WorkerSQL sw = new WorkerSQL();
    public TakePayYa( Map dB) throws ClassNotFoundException, SQLException{
        driverBalanceYa = dB;
        driverList = sw.getDriverBalance();
    }
    public void takePay() throws IOException, SQLException{
        Set keysDriver = driverList.keySet();
        for(Object key : keysDriver){
            Map row = (Map) driverList.get(key);
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
                if(minusYaBalance("cf652a31be7e47fa8cd0c56512111932", 0)){
                    sw.addPayDriver(91, takenSum, 2);
                }
            }
        }
    }
    boolean minusYaBalance(String yaId, int sum) throws MalformedURLException, IOException{
        String url = "https://taximeter.yandex.rostaxi.org/api/driver/balance/minus?apikey="+apiKey
                +"&driver="+yaId+"&sum="+sum+"&description=getSystem";

        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        if(connection.getResponseCode()==200)
            return true;
        else
            return false;
    }
}
