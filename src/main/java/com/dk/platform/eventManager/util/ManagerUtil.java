package com.dk.platform.eventManager.util;


import com.dk.platform.ems.ConnConf;
import com.dk.platform.ems.util.EmsUtil;
import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.TibjmsConnectionFactory;
import com.tibco.tibjms.admin.QueueInfo;
import com.tibco.tibjms.admin.TibjmsAdmin;
import com.tibco.tibjms.admin.TibjmsAdminException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import static java.lang.System.*;

public class ManagerUtil {

    static ConnectionFactory factory;
    static Connection connection;
    static TibjmsAdmin tibjmsAdmin;

    public static void setConnection(String url, String user, String pwd) throws JMSException {

        if(connection == null){

            if(factory == null){
                factory = new TibjmsConnectionFactory(url);
            }
            connection = factory.createConnection(user, pwd);
            connection.start();

            out.println(" Complete to Create EMS Connection.. Connection ID: " + connection.getClientID());
        }
    }

    public static void setEmsConnection() throws JMSException {

        if(connection == null){

            if(factory == null){
                factory = new TibjmsConnectionFactory(ConnConf.EMS_URL.getValue());
            }
            connection = factory.createConnection(ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue());
            connection.start();

            out.println(" Complete to Create EMS Connection.. Connection ID: " + connection.getClientID());
        }
    }

    public static void setTibjmsAdminConnection(){

        if(tibjmsAdmin != null){ return;}
        try {
            tibjmsAdmin = new TibjmsAdmin(ConnConf.EMS_URL.getValue(), ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue());

        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }


    }

    public ManagerUtil(){

    }


    public static Connection getConnection(){
        return connection;
    }


    public static Integer getReceiverCount(String queueName){

        if(tibjmsAdmin == null) {
            setTibjmsAdminConnection();
        }

        try {
            QueueInfo queueInfo = tibjmsAdmin.getQueue(queueName);
            if(queueInfo == null) return null;
            return queueInfo.getReceiverCount();

        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
        return  null;

    }


    /**
     * When Manager Start to Active. First thing. Are they alive?
     * @return String, TASKER Queue's Name.
     */
    public static String[] GetQueueNames(String prefix) {

        // Get QueueInfo[] from tibjmsAdmin.
        QueueInfo[] queueInfos = null;
        try {
            queueInfos = tibjmsAdmin.getQueues(prefix);

        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }

        // If QueueInfo null, return null.
        if (queueInfos == null) return null;


        // Convert QueueInfo into QueueName(String)
        String[] strings = new String[queueInfos.length];
        for(int i=0; i < queueInfos.length; i++){
            strings[i] = queueInfos[i].getName();
        }

        return strings;

    }





}
