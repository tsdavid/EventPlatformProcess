package com.dk.platform.ems.util;

import com.dk.platform.ems.ConnConf;
import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.TibjmsConnectionFactory;
import com.tibco.tibjms.admin.QueueInfo;
import com.tibco.tibjms.admin.TibjmsAdmin;
import com.tibco.tibjms.admin.TibjmsAdminException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.Arrays;

import static java.lang.System.out;
// Connection Manage
public class EmsUtil extends tibjmsPerfCommon{

    private ConnectionFactory factory;
    private Connection connection;
    private TibjmsAdmin tibjmsAdmin;

    public EmsUtil(String Url, String user, String pwd) throws TibjmsAdminException {

        super.serverUrl = Url;
        super.username = user;
        super.password = pwd;
        this.setTibjmsAdminConnection();
        try {
            this.setEmsConnection(Url, user, pwd);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void setTibjmsAdminConnection(){

        if(tibjmsAdmin != null){ return;}
        try {
            tibjmsAdmin = new TibjmsAdmin(ConnConf.EMS_URL.getValue(), ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue());

        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
    }

    private void setEmsConnection(String url, String user, String pwd) throws JMSException {

        if(connection == null){

            if(factory == null){
                factory = new TibjmsConnectionFactory(url);
            }
            connection = factory.createConnection(user, pwd);
            connection.start();

            out.println(" Complete to Create EMS Connection.. Connection ID: " + connection.getClientID());
        }
    }


    public Connection getEmsConnection() {
        if(this.connection == null){
            try {
                this.setEmsConnection(super.serverUrl, super.username, super.password);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

//    public static Integer getReceiverCount(String queueName){
//
//        if(tibjmsAdmin == null) {
//            setTibjmsAdminConnection();
//        }
//
//        try {
//            QueueInfo queueInfo = tibjmsAdmin.getQueue(queueName);
//            if(queueInfo == null) return null;
//            return queueInfo.getReceiverCount();
//
//        } catch (TibjmsAdminException e) {
//            e.printStackTrace();
//        }
//        return  null;
//
//    }

    /**
     * When Manager Start to Active. First thing. Are they alive?
     * @return String, TASKER Queue's Name.
     */
//    public static String[] GetQueueNames(String prefix) {
//
//        // Get QueueInfo[] from tibjmsAdmin.
//        QueueInfo[] queueInfos = null;
//        try {
//            queueInfos = tibjmsAdmin.getQueues(prefix);
//
//        } catch (TibjmsAdminException e) {
//            e.printStackTrace();
//        }
//
//        // If QueueInfo null, return null.
//        if (queueInfos == null) return null;
//
//
//        // Convert QueueInfo into QueueName(String)
//        String[] strings = new String[queueInfos.length];
//        for(int i=0; i < queueInfos.length; i++){
//            strings[i] = queueInfos[i].getName();
//        }
//
//        return strings;
//
//    }


//    public QueueInfo[] getDestinations(String pattern) throws TibjmsAdminException {
//        try{
//            if(tibjmsAdmin == null)
//                this.setAdminInstance();
//        }catch (Exception e){
//
//        }
//        return tibjmsAdmin.getQueues(pattern);
//    }


    /**
     * Get Active Manger on EMS.
     *
     * 1. getQueueInfo with Manager Queue prefix.
     * 2. if more than 2 queue is on EMS, then select Active one.
     *      => There is no case duplicate Manager Queue. Because Manager Queue will be only one and manager process will receive on queue. using EMS Exclusive.
     * 3. Return Current Manager Name.
     *
     * @return
     */
    @Deprecated
    public String getActiveManager(){

        try{

            return tibjmsAdmin.getQueue(ConnConf.EMS_MNG_QUEUE_NAME.getValue()).getName();
        }catch (Exception e){

            return null;
        }
    }

    public String[] getQueueNames(String prefix) {

        try{
            QueueInfo[] queueInfos = tibjmsAdmin.getQueues(prefix.concat("*"));
            String [] arr = new String[queueInfos.length];
            int i = 0;
            for(QueueInfo queueInfo : queueInfos){
                arr[i++] = queueInfo.getName();
            }
            return arr;

        }catch ( Exception e) {
            return null;
        }

    }

    public QueueInfo[] getQueueInfos(String prefix) {

        try {
            return tibjmsAdmin.getQueues(prefix.concat("*"));
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
            return null;
        }
    }



    public static void main(String[] args) throws TibjmsAdminException {

        String url = "tcp://localhost:7222";
        String user = "admin";
        String pwd = "";

        EmsUtil emsUtil = new EmsUtil(url, user, pwd);

        TibjmsAdmin tibjmsAdmin = new TibjmsAdmin(url, user, pwd);
//        System.out.println(
//                tibjmsAdmin.getQueues("F1_EEP_MGR_TSK_*")
//        );
        Arrays.stream(tibjmsAdmin.getQueues("F1.EEP.MGR.TSK.*")).forEach(i -> {
            System.out.println(i.getName());
        });

    }

}
