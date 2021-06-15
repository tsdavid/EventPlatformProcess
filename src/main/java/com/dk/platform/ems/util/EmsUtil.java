package com.dk.platform.ems.util;

import com.dk.platform.ems.ConnConf;
import com.dk.platform.eventTasker.util.TaskerUtil;
import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.TibjmsConnectionFactory;
import com.tibco.tibjms.admin.QueueInfo;
import com.tibco.tibjms.admin.TibjmsAdmin;
import com.tibco.tibjms.admin.TibjmsAdminException;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static java.lang.System.out;
// Connection Manage
public class EmsUtil extends tibjmsPerfCommon{

    private ConnectionFactory factory;
    private Connection connection;
    private TibjmsAdmin tibjmsAdmin;
    private TibCompletionListener completionListener;
    private Session session;

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


        try {
            this.session = this.connection.createSession(Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            e.printStackTrace();
        }

        this.completionListener = new TibCompletionListener();
    }

    /**
     * Connection Setting
     */

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

//            out.println(" Complete to Create EMS Connection.. Connection ID: " + connection.getClientID());
//            out.println(connection.getMetaData().toString());
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



    /**
     * Search
     */

    public QueueInfo getQueueInfo(String queueName){
        try {
            tibjmsAdmin.getQueue(queueName);
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
        return null;
    }





    /**
     *
     * @param prefix
     * @param activeOfnot       :       if find Active => 1, De-Active = 0
     * @return
     */
    public String[] getAct_or_DeAct_QueueNames(String prefix, int activeOfnot) {
        try {
            return this.findQueueNamesWithCondition(prefix, activeOfnot, -1, true);

        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getActivewithPending(String prefix, int threshold){

        try {
            return this.findQueueNamesWithCondition(prefix, 1, threshold, true);

        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getDeActivewithPending(String prefix,int threshold){
        try {
            return this.findQueueNamesWithCondition(prefix, 0, threshold, true);

        } catch (TibjmsAdminException e) {
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
    private String[] findQueueNamesWithCondition(String prefix, int ReceiverCnt, int PendingCountThreshold, boolean pendingAbove) throws TibjmsAdminException {

        boolean checkReceiverCount = ReceiverCnt != -1;             // Active or De-Active.
        boolean checkPendingCount = PendingCountThreshold != -1;    // Pending or No-Pending.
        out.println(checkPendingCount + " " + checkPendingCount);

        QueueInfo[] queueInfos = tibjmsAdmin.getQueues(prefix.concat("*"));
        System.out.println(queueInfos.length);

        // Check with Condition.
        if(checkReceiverCount || checkPendingCount){
            ArrayList<String> arrayList = new ArrayList<>();

            // Check Receiver Count Only ==> Active or De-Active.
            if(checkReceiverCount && !checkPendingCount){

                // Get Active
                if(ReceiverCnt == 1){
                    for (QueueInfo queueInfo : queueInfos){
                        if (queueInfo.getReceiverCount() > 0) arrayList.add(queueInfo.getName());
                    }
                    return arrayList.toArray(new String[arrayList.size()]);

                // Get De-Active.
                }else if(ReceiverCnt == 0){
                    for (QueueInfo queueInfo : queueInfos){
                        if (queueInfo.getReceiverCount() == 0) arrayList.add(queueInfo.getName());
                    }
                    return arrayList.toArray(new String[arrayList.size()]);
                }

            // Check Pending Message Count Only ==> Pending or No-Pending
            }else if(checkPendingCount && !checkReceiverCount) {

                // Get Pending
                if(pendingAbove){
                    for (QueueInfo queueInfo : queueInfos){
                        if (queueInfo.getPendingMessageCount() >= PendingCountThreshold) arrayList.add(queueInfo.getName());
                    }
                    return arrayList.toArray(new String[arrayList.size()]);
                // Get De-Pending
                }else {
                    for (QueueInfo queueInfo : queueInfos){
                        if (queueInfo.getPendingMessageCount() < PendingCountThreshold) arrayList.add(queueInfo.getName());
                    }
                    return arrayList.toArray(new String[arrayList.size()]);
                }

            // check Both Condition.
            }else if(checkReceiverCount && checkPendingCount){

//                System.out.println("check Both Condition.");

                if(ReceiverCnt == 1){

                    // Get Active && Pending
                    if(pendingAbove){
//                        System.out.println("Get Active && Pending.");
                        for (QueueInfo queueInfo : queueInfos){
                            if (queueInfo.getPendingMessageCount() >= PendingCountThreshold && queueInfo.getReceiverCount() > 0)
                                arrayList.add(queueInfo.getName());
                        }
                        return arrayList.toArray(new String[arrayList.size()]);
                    // Get Active && No-Pending
                    }else{
                        for (QueueInfo queueInfo : queueInfos){
                            if (queueInfo.getPendingMessageCount() < PendingCountThreshold && queueInfo.getReceiverCount() > 0)
                                arrayList.add(queueInfo.getName());
                        }
                        return arrayList.toArray(new String[arrayList.size()]);
                    }

                }else if(ReceiverCnt == 0){

                    // Get De-Active && Pending
                    if(pendingAbove){
                        for (QueueInfo queueInfo : queueInfos){
                            if (queueInfo.getPendingMessageCount() >= PendingCountThreshold && queueInfo.getReceiverCount() == 0)
                                arrayList.add(queueInfo.getName());
                        }
                        return arrayList.toArray(new String[arrayList.size()]);

                    // Get De-Active && No-Pending
                    }else{
                        for (QueueInfo queueInfo : queueInfos){
                            if (queueInfo.getPendingMessageCount() < PendingCountThreshold && queueInfo.getReceiverCount() == 0)
                                arrayList.add(queueInfo.getName());
                        }
                        return arrayList.toArray(new String[arrayList.size()]);
                    }
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


    /**
     *
     * @param prefix
     * @param checkReceiverCount
     * @param checkPendingCount
     * @param threshold
     * @return
     * @throws TibjmsAdminException
     */
    private String[] findQueueNames(String prefix, boolean checkReceiverCount, boolean checkPendingCount,
                                    int threshold) throws TibjmsAdminException {

        QueueInfo[] queueInfos = tibjmsAdmin.getQueues(prefix.concat("*"));
        if(queueInfos == null) return null;

        // Check Receiver Count.
        if(checkReceiverCount || checkPendingCount){
            ArrayList<String> arrayList = new ArrayList<>();

            // CheckReceiver Count Only
            if(checkReceiverCount && !checkPendingCount){
                for (QueueInfo queueInfo : queueInfos){
                    if (queueInfo.getReceiverCount() > 0) arrayList.add(queueInfo.getName());
                }
                return arrayList.toArray(new String[arrayList.size()]);

            // checkPendingCount Only
            }else if(!checkReceiverCount && checkPendingCount) {
                for (QueueInfo queueInfo : queueInfos){
                    if (queueInfo.getPendingMessageCount() > threshold) arrayList.add(queueInfo.getName());
                }
                return arrayList.toArray(new String[arrayList.size()]);

            // check Both
            }else if(checkReceiverCount && checkPendingCount){
                for (QueueInfo queueInfo : queueInfos){
                    if (queueInfo.getPendingMessageCount() > threshold && queueInfo.getReceiverCount() > 0)
                        arrayList.add(queueInfo.getName());
                }
                return arrayList.toArray(new String[arrayList.size()]);
            }

        // No Need to Check.
        }else{
            String[] arr = new String[queueInfos.length];
            for(int i=0; i< queueInfos.length; i++){
                arr[i] = queueInfos[i].getName();
            }
            return arr;
        }
        return null;
    }

    /**
     * Message Send
     */

    public void sendAsyncQueueMessage(String destinationName, String sendMesage, Map<String,String> properties) throws JMSException {

        this.sendMessage(destinationName, sendMesage, properties, 0, true, true, false);
    }

    public void sendSyncQueueMessage(String destinationName, String sendMesage, Map<String,String> properties) throws JMSException {

        this.sendMessage(destinationName, sendMesage, properties, 0, false, true, false);
    }

    public void sendSafeQueueMessage(String destinationName, String sendMesage, Map<String,String> properties) throws JMSException {

        this.sendMessage(destinationName, sendMesage, properties, 0, false, true, true);
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



    /**
     * Destroy Queue
     */


    public void destroyQueue(String queueName){
        try {
            tibjmsAdmin.destroyQueue(queueName);
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
    }

    public void destroyQueues(String prefix){
        try {
            tibjmsAdmin.destroyQueues(prefix.concat("*"));
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
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

}
