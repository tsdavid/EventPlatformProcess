package com.dk.platform.eventTasker.util;

import com.tibco.tibjms.TibjmsConnectionFactory;

import javax.jms.*;

import com.dk.platform.ems.util.tibjmsPerfCommon;

import java.util.Map;

public class TaskerUtil extends tibjmsPerfCommon{

    private TibjmsConnectionFactory tibjmsConnectionFactory;
    private Session session;

    private Connection connection;
    private TibCompletionListener completionListener;

    public TaskerUtil(String url){
        if(this.connection == null){
            serverUrl = url;
            this.prepareConnection();
        }
        completionListener = new TibCompletionListener();
    }

    private void prepareConnection(){

        try{
            tibjmsConnectionFactory = new TibjmsConnectionFactory(super.serverUrl);
            this.connection = tibjmsConnectionFactory.createConnection(super.username, super.password);
            this.connection.start();
            this.session  = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // TODO Sessiom Auto  Ack?

            //Logger.info

        }catch (JMSException e){
            //Logge Error
        }
    }

    public void sendQueueMessage(String destinationName, String sendMesage, Map<String,String> properties,
                                 long jmsTimeToLive, boolean async, boolean isPersistent) throws JMSException {

        this.sendMessage(destinationName, sendMesage, properties, jmsTimeToLive, async, true, isPersistent);
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