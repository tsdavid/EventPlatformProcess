package com.dk.platform;

import com.tibco.tibjms.admin.TibjmsAdminException;

import javax.jms.JMSException;
import javax.jms.Message;

public interface Receiver {

    /**
     * Message Handle Method.
     * When Receiver Receive Message, run handleMessage method.
     * @param message           :           Message.
     */
    void handleMessage(Message message);


    /**
     * Create Connection with EMS.
     * Receiver has to make connection with EMS, to Receive Some staff from there.
     * @throws TibjmsAdminException     :       Exception when Initiate {@link com.dk.platform.ems.util.EmsUtil}
     * @throws JMSException             :       Exception when Create Session {@link javax.jms.Session}
     */
    void createEmsConnection() throws TibjmsAdminException, JMSException;
}
