package com.dk.platform.ems.util;

import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.common.tibjmsPerfCommon;
import com.tibco.tibjms.TibjmsConnectionFactory;
import com.tibco.tibjms.admin.QueueInfo;
import com.tibco.tibjms.admin.TibjmsAdmin;
import com.tibco.tibjms.admin.TibjmsAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


/**
 * Main Job
 * 1. Ems Connection Management.
 * 2. Search Logic
 * 3. Message Send Logic
 * 4. Destroy Queue Logic
 * 5. ETC Logic
 */
public class EmsUtil extends tibjmsPerfCommon {

    /*****************************************************************************************
     **************************************  Logger ******************************************
     ****************************************************************************************/
    private static final Logger logger = LoggerFactory.getLogger(EmsUtil.class);


    /*****************************************************************************************
     ***********************************  Variables ******************************************
     ****************************************************************************************/
    private ConnectionFactory factory;
    private Connection connection;
    private TibjmsAdmin tibjmsAdmin;
    private TibCompletionListener completionListener;
    private Session session;


    /*****************************************************************************************
     ***********************************  Constructor ****************************************
     ****************************************************************************************/

    /**
     * For Test
     * @throws TibjmsAdminException
     */
    public EmsUtil() throws TibjmsAdminException {

        this.setTibjmsAdminConnection();

        try {
            this.setEmsConnection();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {
            this.session = this.connection.createSession(Session.AUTO_ACKNOWLEDGE);

        } catch (JMSException e) {
            e.printStackTrace();
        }

        this.completionListener = new TibCompletionListener();
    }

    /**
     *
     * @param Url
     * @param user
     * @param pwd
     * @throws TibjmsAdminException
     */
    public EmsUtil(String Url, String user, String pwd) throws TibjmsAdminException {

        super.serverUrl = Url;
        super.username = user;
        super.password = pwd;
        logger.info("[Initialize] EmsUtil.. Ems Server : {}., User : {}", Url, user);

        this.setTibjmsAdminConnection(Url, user, pwd);

        try {

            this.setEmsConnection(Url, user, pwd);

        } catch (JMSException e) {
            logger.error("[{}] Error While Make Connection with EMS. Error : {}/{}.",
                    "setEmsConnection",e.getMessage(), e.toString());
            e.printStackTrace();
        }

        try {

            this.session = this.connection.createSession(Session.AUTO_ACKNOWLEDGE);

        } catch (JMSException e) {
            logger.error("[{}] Error While Make Session with EMS. Error : {}/{}.",
                    "createSession",e.getMessage(), e.toString());
            e.printStackTrace();
        }

        this.completionListener = new TibCompletionListener();
    }


    /**
     * For Test
     */
    private void setTibjmsAdminConnection(){

        if(tibjmsAdmin != null){ return;}
        try {

            tibjmsAdmin = new TibjmsAdmin(AppPro.EMS_URL.getValue(), AppPro.EMS_USR.getValue(), AppPro.EMS_PWD.getValue());

        } catch (TibjmsAdminException e) {
            logger.error("[{}] Error While Make tibjmsAdmin with EMS. Error : {}/{}.",
                    "tibjmsAdmin",e.getMessage(), e.toString());
            e.printStackTrace();
        }
    }


    /**
     *
     * @param url
     * @param user
     * @param pwd
     */
    private void setTibjmsAdminConnection(String url, String user, String pwd){

        if(tibjmsAdmin != null){ return;}
        try {

            tibjmsAdmin = new TibjmsAdmin(url, user, pwd);

        } catch (TibjmsAdminException e) {
            logger.error("[{}] Error While Make tibjmsAdmin with EMS. Error : {}/{}.",
                    "tibjmsAdmin",e.getMessage(), e.toString());
            e.printStackTrace();
        }
    }


    /**
     * For Test
     * @throws JMSException
     */
    private void setEmsConnection() throws JMSException {

        if(connection == null){

            if(factory == null){
                factory = new TibjmsConnectionFactory(AppPro.EMS_URL.getValue());
            }
            connection = factory.createConnection(AppPro.EMS_USR.getValue(), AppPro.EMS_PWD.getValue());
            connection.start();

        }
    }


    /**
     *
     * @param url
     * @param user
     * @param pwd
     * @throws JMSException
     */
    private void setEmsConnection(String url, String user, String pwd) throws JMSException {

        if(connection == null){

            if(factory == null){
                factory = new TibjmsConnectionFactory(url);
            }
            connection = factory.createConnection(user, pwd);
            connection.start();

        }
    }


    /*****************************************************************************************
     ***********************************  Getter *********************************************
     ****************************************************************************************/


    /**
     *
     * @return
     */
    public Connection getEmsConnection() {

        if(this.connection == null){
            try {
                this.setEmsConnection();

            } catch (JMSException e) {
                logger.error("Error : {}/{}.", e.getMessage(), e.toString());
                e.printStackTrace();
            }
        }
        return connection;
    }


    /**
     *
     * @return
     */
    public TibjmsAdmin getTibjmsAdmin() {

        if(this.tibjmsAdmin == null){
            try{
                this.setTibjmsAdminConnection();

            }catch (Exception e){
                logger.error("Error : {}/{}.", e.getMessage(), e.toString());
                e.printStackTrace();
            }
        }

        return tibjmsAdmin;
    }


    /*****************************************************************************************
     ********************************  Search Logic ******************************************
     ****************************************************************************************/


    /**
     *
     * @param prefix
     * @param activeOfnot       :       if find Active => 1, De-Active = 0, No-Use = -1;
     * @return
     */
    public String[] getAct_or_DeAct_QueueNames(String prefix, int activeOfnot) {
        try {
            return this.findQueueNamesWithCondition(prefix, activeOfnot, -1, true);

        } catch (TibjmsAdminException e) {
            logger.error("[{}] Error : {}/{}.","SearchLogic", e.getMessage(), e.toString());
            e.printStackTrace();
        }
        return null;
    }


    /**
     *
     * @param prefix
     * @param threshold
     * @return
     */
    public String[] getDeActivewithPending(String prefix,int threshold){
        try {
            return this.findQueueNamesWithCondition(prefix, 0, threshold, true);

        } catch (TibjmsAdminException e) {
            logger.error("[{}] Error : {}/{}.","SearchLogic", e.getMessage(), e.toString());
            e.printStackTrace();
        }
        return null;

    }


    /**
     *
     * @param prefix
     * @param ReceiverCnt           =       Filter with Receiver Count, No Use = -1. find Active = 1, De-Active = 0
     * @param PendingCountThreshold =       Filter with Pending MsgCount, No Use = -1.
     * @param pendingAbove          =       true : find above threshold => pending , false : below threshold => no-Pending
     * @return
     * @throws TibjmsAdminException
     */
    private String[] findQueueNamesWithCondition(String prefix, int ReceiverCnt, int PendingCountThreshold,
                                                 boolean pendingAbove) throws TibjmsAdminException {

        boolean checkReceiverCount = ReceiverCnt != -1;             // Active or De-Active.
        boolean checkPendingCount = PendingCountThreshold != -1;    // Pending or No-Pending.
//        out.println(checkPendingCount + " " + checkPendingCount);

        QueueInfo[] queueInfos = tibjmsAdmin.getQueues(prefix.concat("*"));
        System.out.println(queueInfos.length);

        // Check with Condition.
        if(checkReceiverCount || checkPendingCount){
            ArrayList<String> arrayList = new ArrayList<>();

            // Check Receiver Count
            if(checkReceiverCount){

                // Check Receiver Count Only ==> Active or De-Active.
                if(!checkPendingCount){
                    // Get Active
                    if(ReceiverCnt == 1){
                        for (QueueInfo queueInfo : queueInfos){
                            if (queueInfo.getReceiverCount() > 0) arrayList.add(queueInfo.getName());
                        }
                        return arrayList.toArray(new String[0]);

                    // Get De-Active.
                    }else if(ReceiverCnt == 0){
                        for (QueueInfo queueInfo : queueInfos){
                            if (queueInfo.getReceiverCount() == 0) arrayList.add(queueInfo.getName());
                        }
                        return arrayList.toArray(new String[0]);
                    }

                // Check Receiver Count and Pending Count.
                }else{

                    // When Receiver Active
                    if(ReceiverCnt == 1){

                        // Get Active && Pending
                        if(pendingAbove){
//                        System.out.println("Get Active && Pending.");
                            for (QueueInfo queueInfo : queueInfos){
                                if (queueInfo.getPendingMessageCount() >= PendingCountThreshold && queueInfo.getReceiverCount() > 0)
                                    arrayList.add(queueInfo.getName());
                            }
                            return arrayList.toArray(new String[0]);

                        // Get Active && No-Pending
                        }else{
                            for (QueueInfo queueInfo : queueInfos){
                                if (queueInfo.getPendingMessageCount() < PendingCountThreshold && queueInfo.getReceiverCount() > 0)
                                    arrayList.add(queueInfo.getName());
                            }
                            return arrayList.toArray(new String[0]);
                        }

                    // Receiver De-Active
                    }else if(ReceiverCnt == 0){

                        // Get De-Active && Pending
                        if(pendingAbove){
                            for (QueueInfo queueInfo : queueInfos){
                                if (queueInfo.getPendingMessageCount() >= PendingCountThreshold && queueInfo.getReceiverCount() == 0)
                                    arrayList.add(queueInfo.getName());
                            }
                            return arrayList.toArray(new String[0]);

                        // Get De-Active && No-Pending
                        }else{
                            for (QueueInfo queueInfo : queueInfos){
                                if (queueInfo.getPendingMessageCount() < PendingCountThreshold && queueInfo.getReceiverCount() == 0)
                                    arrayList.add(queueInfo.getName());
                            }
                            return arrayList.toArray(new String[0]);
                        }
                    }
                }


            // Check Pending Message Count Only ==> Pending or No-Pending
            }else {

                // Get Pending
                if(pendingAbove){
                    for (QueueInfo queueInfo : queueInfos){
                        if (queueInfo.getPendingMessageCount() >= PendingCountThreshold) arrayList.add(queueInfo.getName());
                    }
                    return arrayList.toArray(new String[0]);

                // Get De-Pending
                }else {
                    for (QueueInfo queueInfo : queueInfos){
                        if (queueInfo.getPendingMessageCount() < PendingCountThreshold) arrayList.add(queueInfo.getName());
                    }
                    return arrayList.toArray(new String[0]);
                }
            }

        // No Condition.
        }else{
            String[] arr = new String[queueInfos.length];
            for(int i=0; i< queueInfos.length; i++){
                arr[i] = queueInfos[i].getName();
            }
            return arr;
        }
        return null;
    }



    /*****************************************************************************************
     ******************************  Message Send Logic **************************************
     ****************************************************************************************/

    /**
     *
     * @param destinationName       :   Ems Destination.
     * @param sendMessage           :   Message Contents.
     * @param properties            :   Properties attached Message header.
     * @throws JMSException         :   Throw JMSException...
     * @throws JMSException
     */
    public void sendAsyncQueueMessage(String destinationName, String sendMessage, Map<String,String> properties) throws JMSException {

        this.sendMessage(destinationName, sendMessage, properties, 0, true, true, false);
    }


    /**
     *
     * @param destinationName       :   Ems Destination.
     * @param sendMessage           :   Message Contents.
     * @param properties            :   Properties attached Message header.
     * @throws JMSException         :   Throw JMSException...
     */
    public void sendSyncQueueMessage(String destinationName, String sendMessage, Map<String,String> properties) throws JMSException {

        this.sendMessage(destinationName, sendMessage, properties, 0, false, true, false);
    }


    /**
     *
     * @param destinationName       :   Ems Destination.
     * @param sendMessage           :   Message Contents.
     * @param properties            :   Properties attached Message header.
     * @throws JMSException         :   Throw JMSException...
     */
    public void sendSafeQueueMessage(String destinationName, String sendMessage, Map<String,String> properties) throws JMSException {

        this.sendMessage(destinationName, sendMessage, properties, 0, false, true, true);
    }


    /**
     * @param destinationName       :   Ems Destination.
     * @param sendMessage           :   Message Contents.
     * @param properties            :   Properties attached Message header.
     * @param jmsTimeToLive         :   Time To Live. Time out for Message, No User => 0.
     * @param async                 :   A-Synchronize Message Send.
     * @param isPersistent          :   Message Persistent Mode => true
     * @throws JMSException         :   Throw JMSException...
     */
    public void sendQueueMessage(String destinationName, String sendMessage, Map<String,String> properties,
                                 long jmsTimeToLive, boolean async, boolean isPersistent) throws JMSException {

        this.sendMessage(destinationName, sendMessage, properties, 0, async, true, isPersistent);
    }


    // TODO Make FailQueue Logic.
    public void sendFailQueueMessage(){

    }


    /**
     * @param destinationName       :   Ems Destination.
     * @param sendMessage           :   Message Contents.
     * @param properties            :   Properties attached Message header.
     * @param jmsTimeToLive         :   Time To Live. Time out for Message, No User => 0.
     * @param async                 :   A-Synchronize Message Send.
     * @param isQueue               :   Check Destination Type. if> Queue => true
     * @param isPersistent          :   Message Persistent Mode => true
     * @throws JMSException         :   Throw JMSException...
     */
    private void sendMessage(String destinationName, String sendMessage, Map<String,String> properties,
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
        msg.setText(sendMessage);

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

        msgProducer.close();
    }


    // TODO CompletionListener Logic
    static class TibCompletionListener implements javax.jms.CompletionListener{

        @Override
        public void onCompletion(Message message) {

        }

        @Override
        public void onException(Message message, Exception e) {

        }
    }



     /*****************************************************************************************
     ***********************************  Destroy Queue Logic *********************************
     *****************************************************************************************/



    public void destroyQueue(String queueName){
        try {
            tibjmsAdmin.destroyQueue(queueName);
        } catch (TibjmsAdminException e) {
            logger.error("[{}] Error : {}/{}.","DestroyLogic", e.getMessage(), e.toString());
            e.printStackTrace();
        }
    }

    public void destroyQueues(String prefix){
        try {
            tibjmsAdmin.destroyQueues(prefix.concat("*"));

        } catch (TibjmsAdminException e) {
            logger.error("[{}] Error : {}/{}.","DestroyLogic", e.getMessage(), e.toString());
            e.printStackTrace();
        }
    }


    /*****************************************************************************************
     ****************************************  ETC Logic *************************************
     *****************************************************************************************/


    /**
     *
     * @param queueName
     * @throws TibjmsAdminException
     */
    public void createQueue(String queueName) throws TibjmsAdminException {
        QueueInfo queueInfo = new QueueInfo(queueName);
        tibjmsAdmin.createQueue(queueInfo);
    }


    /*****************************************************************************************
     *************************************  Deprecated ***************************************
     ****************************************************************************************/


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
            return tibjmsAdmin.getQueue(AppPro.EMS_MNG_QUEUE_NAME.getValue()).getName();
        }catch (Exception e){

            return null;
        }
    }

    /**
     *
     * @param queueName
     * @return
     */
    @Deprecated
    public QueueInfo getQueueInfo(String queueName){
        try {
            tibjmsAdmin.getQueue(queueName);
        } catch (TibjmsAdminException e) {

            e.printStackTrace();
        }
        return null;
    }


    /*****************************************************************************************
     *************************************  Main *********************************************
     ****************************************************************************************/

    public static void main(String[] args) throws TibjmsAdminException {

        //TODO Functional Test
        // 1. SearchLogic Test
        // 2. Send Logic Test.
        // 3. Destroy Logic Test.
    }

}
