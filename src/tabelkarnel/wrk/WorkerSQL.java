/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tabelkarnel.wrk;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException; 
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author korgan
 */
public class WorkerSQL {
    public String url;
    public String login;
    public String pass;
    public Connection con;
    Map config;
    boolean iscon;
    public WorkerSQL() throws ClassNotFoundException, SQLException{
        ConfigurationReader cr = new ConfigurationReader();
        config=cr.readFile();
        url="jdbc:mysql://"+config.get("dbhost")+":"+config.get("dbport")+"/"+config.get("dbname")+"?useUnicode=true&characterEncoding=UTF-8";
        try
        {
            Class.forName("com.mysql.jdbc.Driver"); 
            login=config.get("dbuser").toString();
            pass=config.get("dbpassword").toString();
            con = DriverManager.getConnection(url, login, pass);
            iscon = true;
        }
        catch(SQLException ex)
        {
            System.out.println("Mysql ERROR: "+ex.getMessage());
        }
    }
    public void addAccrual() throws SQLException{
        Statement lockTable = con.createStatement();
        ResultSet rsLock = lockTable.executeQuery("SELECT `paramValue` FROM `param` WHERE `paramName` = 'workingAccural'");
        if(rsLock.next()){
            int marker = Integer.parseInt(rsLock.getString("paramValue"));
            if(marker==0){
                Statement stLock = con.createStatement();
                stLock.execute("UPDATE `param` SET `paramValue` = '1'");
                stLock.close();
            }
            else{
                rsLock.close();
                lockTable.close();
                return;
            }
            rsLock.close();
            lockTable.close();    
        }
        Statement stGetDriverAndRent = con.createStatement();
        ResultSet rsGetDriverAndRent = stGetDriverAndRent.executeQuery("SELECT `drivers`.*, TO_DAYS(current_date())-TO_DAYS(driverStartDate)+1 as `dayWork` FROM `drivers` "
                + "WHERE `driver_deleted`=0  "
               + "AND `driver_id` NOT IN (SELECT `driverId` FROM `pay` WHERE `type`=2 and `date` > CURDATE())");
               System.out.println("Got driver list");
        while(rsGetDriverAndRent.next()){
            int rentSum;
            int dayOff;
            int number = rsGetDriverAndRent.getInt("dayWork");
            int ost=1;
            if(rsGetDriverAndRent.getInt("driverDayOffPeriod")!=0)
                ost = number % rsGetDriverAndRent.getInt("driverDayOffPeriod");
            if(ost==0){
                rentSum=0;
                dayOff = 1;
            }
            else{
                rentSum=rsGetDriverAndRent.getInt("driver_day_rent")*-1;  
                dayOff = 0;
            }
            if(rsGetDriverAndRent.getInt("driverDayOffPeriod")==0){
                System.out.print("Not will have dayoff ");
            }
            System.out.println("Driver id = "+rsGetDriverAndRent.getInt("driver_id")+" Driver Last Name = "+rsGetDriverAndRent.getString("driver_lastname")+" WorkDay = "+ number +"  RentSum = "+rentSum);
            Statement stAddAccrual = con.createStatement();
            int balanceNow = rsGetDriverAndRent.getInt("driver_current_debt")+rentSum;
            stAddAccrual.execute("INSERT INTO `pay` (`type`, `date`, `source`, `sum`, `driverId`, `balance`) "
                    + "VALUES ('2', NOW(), 0, '"+rentSum+"', '"+rsGetDriverAndRent.getInt("driver_id")+"', "+balanceNow+")");
            Statement stUpdateCurrentDebt = con.createStatement();
            stUpdateCurrentDebt.execute("UPDATE `drivers` SET `driver_current_debt`=(SELECT sum(`sum`) FROM `pay` WHERE driverId="+rsGetDriverAndRent.getInt("driver_id")+" and type!=3), "
                    + "`driverDayOff`='"+dayOff+"' "
                    + " WHERE `driver_id`="+rsGetDriverAndRent.getInt("driver_id"));
            stAddAccrual.close();
            stUpdateCurrentDebt.close();
        }
        rsGetDriverAndRent.close();
        stGetDriverAndRent.close();
        Statement stUnLock = con.createStatement();
        stUnLock.execute("UPDATE `param` SET `paramValue` = '0'");
        stUnLock.close();
    }
    public int getDriverIdInTable(String phone) throws SQLException{
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT `driver_id` FROM `drivers` WHERE `driver_phone_number` LIKE '%"+phone+"%' AND `driver_deleted`=0");
        if(rs.next()){
            int id =rs.getInt("driver_id");
            rs.close();
            st.close();
            return id;
        }
        rs.close();
        st.close();
        return 0;
    }
    public void setYaId(String yaId, int driverId) throws SQLException{
        Statement st = con.createStatement();
        st.execute("UPDATE `drivers` SET `yaId`='"+yaId+"' WHERE driver_id="+driverId);
    }

    public boolean checkYaIdInBase(String yaId) throws SQLException {
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT `driver_id` FROM `drivers` WHERE `yaId` ='"+yaId+"'");
        if(rs.next()){
            if(rs.getInt("driver_id")!=0) {
               rs.close();
               st.close();
               return true;
            }
        }
        rs.close();
        st.close();
        Statement sttmp = con.createStatement();
        ResultSet rstmp = sttmp.executeQuery("SELECT * FROM `tmp_driver` WHERE `yaId` ='"+yaId+"'");
        if(rstmp.next()){
               rstmp.close();
               rstmp.close();
               return true;
        }
        return false;
    }

    public Map getDriverYaIdList() throws SQLException {
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM `drivers` WHERE `driver_deleted`=0 AND yaId IS NOT NULL");
        Map<Integer, String> driverList = new HashMap<Integer, String>();
        while(rs.next()){
            driverList.put(rs.getInt("driver_id"), rs.getString("yaId"));
        }
        return driverList;
    }
    public void updateDriverBalance(String yaId, String balance) throws SQLException{
        Statement st = con.createStatement();
        System.out.println("UPDATE ");
        st.execute("UPDATE drivers SET `yaBalace`="+balance+" WHERE `yaId`='"+yaId+"'");
    }

    public void addTMPList(String driverId, String firstName, String lastName, String phone) throws SQLException {
        Statement st = con.createStatement();
        st.execute("INSERT INTO `tmp_driver` (`phone`, `firstName`, `lastName`, `yaId`) "
                + " VALUES ('"+phone+"', '"+firstName+"', '"+lastName+"', '"+driverId+"')");
    }

    public Map getDriverBalance() throws SQLException {
        Map driverList;
        driverList = new HashMap<Integer, Map>();
        try (Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT `driver_id`, `driver_current_debt`, `yaId`  FROM `drivers` "
                    + "WHERE `yaId` IS NOT NULL AND `driver_current_debt` < 0 AND `driver_deleted` = 0");
            while(rs.next()){
                Map row = new HashMap<Integer, String>();
                row.put("yaId", rs.getString("yaId"));
                row.put("debt", rs.getInt("driver_current_debt"));
                driverList.put(rs.getInt("driver_id"), row);
            }   
            rs.close();
        }
        return driverList;
    }
    public void addPayDriver(int driverId, int sum, int source) throws SQLException{
        try{
            int balanceDriver = 0;
            if(source!=6){
                Statement stGetBalance = con.createStatement();
                ResultSet rsGetBalance = stGetBalance.executeQuery("SELECT `driver_current_debt` FROM `drivers` "
                    + "WHERE `driver_id`="+driverId);
                if(rsGetBalance.next())
                    balanceDriver=rsGetBalance.getInt("driver_current_debt")+sum;
            }
            Statement st = con.createStatement();
            st.execute("INSERT INTO `pay` (`type`, `date`, `source`, `sum`, `driverId`, `user`, `balance`) "
                    + "VALUES ('1', NOW(), '"+source+"', '"+sum+"', '"+driverId+"', '0', '"+balanceDriver+"')");
            st.close();
            if(source==6){
               Statement stUpdateCurrentDebt = con.createStatement();
                stUpdateCurrentDebt.execute("UPDATE `drivers` SET `driver_deposit`=`driver_deposit`-"+sum+" WHERE `driver_id`="+driverId);
                stUpdateCurrentDebt.close(); 
            }
            Statement stUpdateCurrentDebt = con.createStatement();
            stUpdateCurrentDebt.execute("UPDATE `drivers` SET `driver_current_debt`=(SELECT sum(`sum`) FROM `pay` WHERE driverId="+driverId+" and type!=3) WHERE `driver_id`="+driverId);
            stUpdateCurrentDebt.close();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}
