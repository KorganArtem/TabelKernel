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
import ru.kor.yaApi.TakePayYa;
import ru.kor.yaApi.Worker;
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
        Worker wrk = new Worker();
        System.out.println("Start add accrual!");
        wsql.addAccrual();
        wsql.tmpToDrivers();
        TakePayYa tpy; 
        tpy = new TakePayYa(wrk.getDriverListInYa());
        System.out.println("Start take YA pay!");
        tpy.takePay();
        System.out.println("Start check new driver in YA!");
        wrk.checkNewDriver();
        System.out.println("Start update driver balance in YA!");
        wrk.getAllBalance();
       long endTime = (new Date().getTime()/1000);
        System.out.println("Time taken:"+(endTime-startTime));
    }
    
}
