/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tabelkarnel.wrk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author korgan
 */
public class ConfigurationReader {
    public Map readFile(){ 
        Map allConf = new HashMap<String, String>();
        try(BufferedReader fr = new BufferedReader( new FileReader("/table/main.cf"))){
            String line;
            while((line = fr.readLine())!=null){
                //System.out.println(line);
                if(line.split("#")[0].length()>1){
                    String paramLine = line.split("#")[0];
                    String[] parametr = paramLine.split("=");
                    if(parametr.length>1){
                        //System.out.println(parametr[0]+"-->"+parametr[1]);
                        allConf.put(parametr[0], parametr[1]);
                    }
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return allConf;
    }
}
