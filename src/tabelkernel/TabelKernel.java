/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tabelkernel;

import java.io.IOException;
import java.net.ProtocolException;
import java.sql.SQLException;
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
        WorkerSQL wsql = new WorkerSQL();
        Worker wrk = new Worker();
        wsql.addAccrual();
        wrk.checkNewDriver();
        wrk.getAllBalance();
    }
    
}
