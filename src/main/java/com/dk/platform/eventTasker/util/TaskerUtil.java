package com.dk.platform.eventTasker.util;

import com.dk.platform.ems.ConnConf;
import com.dk.platform.ems.util.EmsUtil;
import com.tibco.tibjms.TibjmsConnectionFactory;

import javax.jms.*;

import com.dk.platform.ems.util.tibjmsPerfCommon;
import com.tibco.tibjms.admin.TibjmsAdminException;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class TaskerUtil extends tibjmsPerfCommon{

    private TibCompletionListener completionListener;
    private Connection connection;
    private Session session;
    private MemoryStorage memoryStorage = MemoryStorage.getInstance();


    public TaskerUtil(){
        if(connection == null){
            try {
                this.connection = new EmsUtil(ConnConf.EMS_URL.getValue(), ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue()).getEmsConnection();
            } catch (TibjmsAdminException e) {
                e.printStackTrace();
            }
        }
        try {
            this.session = this.connection.createSession(Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        completionListener = new TibCompletionListener();
    }

    public TaskerUtil(EmsUtil emsUtil) {
        this.connection = emsUtil.getEmsConnection();
        try {
            this.session = this.connection.createSession(Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        completionListener = new TibCompletionListener();

    }

    public void sendHealthCheckMessage() {
        // Destination Information.
        String toManager = ConnConf.EMS_MNG_QUEUE_NAME.getValue();

        // Send Message. Get All Work Queue.
        String msg = "";
        for(String k : memoryStorage.getQueueMap().keySet()){
            msg = msg.concat(k+",");
        }

        // Set-up For Properties.
        Map<String,String> properties = new HashMap<>();
        properties.put(ConnConf.MSG_TYPE.getValue(), ConnConf.TSK_HLTH_MNG_VAL.getValue());
        properties.put(ConnConf.TSK_HLTH_FROM_PROP.getValue(),memoryStorage.getPROCESS_NAME());
        try {
            this.sendSyncQueueMessage(toManager, msg, properties, true);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void sendASyncQueueMessage(String destinationName, String sendMesage, Map<String,String> properties,
                                     boolean isPersistent) throws JMSException {

        this.sendMessage(destinationName, sendMesage, properties, 0, true, true, isPersistent);
    }

    public void sendSyncQueueMessage(String destinationName, String sendMesage, Map<String,String> properties,
                           boolean isPersistent) throws JMSException {

        this.sendMessage(destinationName, sendMesage, properties, 0, false, true, isPersistent);
    }

    public void sendQueueMessage(String destinationName, String sendMesage, Map<String,String> properties,
                                 long jmsTimeToLive, boolean async, boolean isPersistent) throws JMSException {

        this.sendMessage(destinationName, sendMesage, properties, 0, async, true, isPersistent);
    }


    /**
     * @param destinationName
     * @param sendMesage
     * @param properties
     * @param jmsTimeToLive
     * @param async
     * @param isQueue
     * @param isPersistent
     * @throws JMSException
     */
    private void sendMessage(String destinationName, String sendMesage, Map<String,String> properties,
                             long jmsTimeToLive, boolean async, boolean isQueue, boolean isPersistent) throws JMSException {


        // Create Destination.
        Destination destination = (isQueue) ? this.session.createQueue(destinationName) : this.session.createTopic(destinationName);

        // Create Message Producer.
        MessageProducer msgProducer = this.session.createProducer(destination);

        // Set-up Persistent Message
        if(isPersistent) {
            msgProducer.setDeliveryMode(javax.jms.DeliveryMode.PERSISTENT);
        }else {
            msgProducer.setDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
        }

        // Set-Up TTL Message
        if(jmsTimeToLive > 0) {msgProducer.setTimeToLive(jmsTimeToLive);}

        // Set-Up Message.
        TextMessage msg = this.session.createTextMessage();
        msg.setText(sendMesage);

        // Message Properties
        if(properties != null){
            for(String key : properties.keySet()){
                msg.setStringProperty(key, properties.get(key));
            }
        }

        // Sync or Async
        if(async){
            msgProducer.send(msg, completionListener);
        } else {
            msgProducer.send(msg);
        }


        msg = null;
        if(msgProducer != null){
            msgProducer.close();
            msgProducer = null;
        }
    }


    class TibCompletionListener implements javax.jms.CompletionListener{

        @Override
        public void onCompletion(Message message) {

        }

        @Override
        public void onException(Message message, Exception e) {

        }
    }

}