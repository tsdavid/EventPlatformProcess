package com.dk.platform.ems.util;

import com.dk.platform.ems.ConnConf;
import com.tibco.tibjms.TibjmsConnectionFactory;
import com.tibco.tibjms.admin.QueueInfo;
import com.tibco.tibjms.admin.TibjmsAdmin;
import com.tibco.tibjms.admin.TibjmsAdminException;

import javax.jms.Connection;
import javax.jms.JMSException;

public class EmsUtil extends tibjmsPerfCommon{

    private TibjmsAdmin tibjmsAdmin;

    public EmsUtil(String Url, String user, String pwd) throws TibjmsAdminException {

        super.serverUrl = Url;
        super.username = user;
        super.password = pwd;
        this.setAdminInstance();
    }

    private void setAdminInstance() throws TibjmsAdminException {
        this.tibjmsAdmin = new TibjmsAdmin(serverUrl, username, password);
    }


    public QueueInfo[] getDestinations(String pattern) throws TibjmsAdminException {
        try{
            if(tibjmsAdmin == null)
                this.setAdminInstance();
        }catch (Exception e){

        }
        return tibjmsAdmin.getQueues(pattern);
    }


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
    public String getActiveManager(){
        try{
            if(tibjmsAdmin == null)
                this.setAdminInstance();
        }catch (Exception e){

        }

        try{

            return tibjmsAdmin.getQueue(ConnConf.EMS_MNG_QUEUE_NAME.getValue()).getName();
        }catch (Exception e){

            return null;
        }
    }

    public static void main(String[] args) throws TibjmsAdminException {

        String url = "tcp://localhost:7222";
        String user = "admin";
        String pwd = "";

        EmsUtil emsUtil = new EmsUtil(url, user, pwd);

//        emsUtil.getDestination("h");

    }

}
