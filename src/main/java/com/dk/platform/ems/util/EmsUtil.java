package com.dk.platform.ems.util;

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


    public QueueInfo getDestination(String pattern) throws TibjmsAdminException {

        TibjmsAdmin tibjmsAdmin = new TibjmsAdmin(serverUrl, username, password);

        return tibjmsAdmin.getQueue(pattern);

    }

    public static void main(String[] args) throws TibjmsAdminException {

        String url = "tcp://localhost:7222";
        String user = "admin";
        String pwd = "";

        EmsUtil emsUtil = new EmsUtil(url, user, pwd);

        emsUtil.getDestination("h");

    }

}
