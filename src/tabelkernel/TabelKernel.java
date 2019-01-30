/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tabelkernel;

import java.io.IOException;
import java.net.ProtocolException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import ru.kor.yaApi.Worker;
import ru.kor.yaApi.WorkerNew;
import tabelkarnel.wrk.WorkerSQL;

/**
 *
 * @author korgan
 */
public class TabelKernel {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException, ProtocolException, IOException {
        // TODO code application logic here
        long startTime = (new Date().getTime()/1000);
        WorkerSQL wsql = new WorkerSQL();
        
        for(int ind=0; ind < args.length; ind++){
            if(args[ind].equals("-a")){
                System.out.println("Start add accrual!");
                wsql.addAccrual();
                continue;
            }
            if(args[ind].equals("-y")){
                WorkerNew wrk = new WorkerNew(args[ind+1]);
                Map<String, Integer> getDriverListInYa = wrk.getDriverListInYa();
                for(Entry<String, Integer> entr : getDriverListInYa.entrySet()){
                    int yaBalance = getDriverListInYa.get(entr.getKey());
                    String phone = wrk.getDriverPhone(entr.getKey());
                    double driverDebt = wsql.getDriverBalance(phone);
                    double takenSum = 0;
                    if(driverDebt==0)
                        continue;
                    if(yaBalance > 300){
                        if((driverDebt*-1) < (yaBalance-200)){
                            takenSum = driverDebt*-1;
                        }
                        else{
                            takenSum =(yaBalance-200);
                        }
                        if(wrk.minusYaBalance(entr.getKey(), (int) takenSum))
                            wsql.addPayDriver(wsql.getDriverId(phone), (int) takenSum, 2);
                    }
                    
                    System.out.println("\t"+phone + "  yaBalanc: "+yaBalance+" driverDebt: "+driverDebt+" takenSum: "+takenSum);
                }
                /*Worker wrk = new Worker(args[ind+1]);
                TakePayYa tpy; 
                tpy = new TakePayYa(args[ind+1]);
                System.out.println("Start take YA pay!");
                tpy.takePay(wrk.getDriverListInYa());
                System.out.println("Start check new driver in YA!");
                wrk.checkNewDriver();
                System.out.println("Start update driver balance in YA!");
                wrk.getAllBalance();*/
                
            }
        }
        wsql.tmpToDrivers();
       long endTime = (new Date().getTime()/1000);
        System.out.println("Time taken:"+(endTime-startTime));
    }
    
}
