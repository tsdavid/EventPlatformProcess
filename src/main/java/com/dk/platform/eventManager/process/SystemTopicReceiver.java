package com.dk.platform.eventManager.process;

// Consumer System topic to get info about new created destination.
// topic receiver ==> $sys.monitor.queue.create


import com.dk.platform.Process;
import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.Consumer;
import com.dk.platform.eventManager.util.ManagerUtil;
import com.dk.platform.eventManager.util.MemoryStorage;
import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.TibjmsMapMessage;
import com.tibco.tibjms.admin.TibjmsAdminException;

import javax.jms.*;

/**
 * Receive New Queue -> Assign or Save in Set
 */
public class SystemTopicReceiver implements Runnable, Consumer, Process {

    private final String PROPERTY_NAME = "target_name";
    private Connection connection;
    private Session session;
    private MessageConsumer msgConsumer;
    private Destination destination;
    private int ackMode;
    private boolean active = false;

    private MemoryStorage memoryStorage;

    private EmsUtil emsUtil;

    private ManagerUtil managerUtil;

    public SystemTopicReceiver(String name, int ackMode, boolean isTopic) throws JMSException {

        this.ackMode = ackMode;
        try {
            this.connection = new EmsUtil(AppPro.EMS_URL.getValue(), AppPro.EMS_USR.getValue(), AppPro.EMS_PWD.getValue()).getEmsConnection();
            this.session = this.connection.createSession(ackMode);
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
        destination = (isTopic) ? session.createTopic(name) : session.createQueue(name);
        msgConsumer = session.createConsumer(destination);


        // Set-Up Instance
        this.memoryStorage = MemoryStorage.getInstance();
        this.emsUtil = this.memoryStorage.getEmsUtil();
        this.managerUtil = this.memoryStorage.getManagerUtil();

    }

    @Override
    public void setActive(){
        active = true;
    }

    @Override
    public void setDeActive() {
        System.out.println(" Set DeActive Method is Run");
        active = false;

    }

    @Override
    public void closeAllResources() {
        if(session != null){
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        if(msgConsumer != null){
            try {
                msgConsumer.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void run() {

//        System.out.println("Runnalbe Run Cnt active status : " + active);


        while(active){
//            System.out.println("Runnalbe Run While Start : ");


            TibjmsMapMessage message = null;
            try {
                message = (TibjmsMapMessage) msgConsumer.receive();

                // Get Queue Name.
                String newQueue = null;
                try{
                    newQueue = message.getStringProperty(PROPERTY_NAME);

                }catch (Exception e){
                    e.printStackTrace();
                }
//                System.out.println(message.getStringProperty(PROPERTY_NAME));


                // Check WRK Queue Prefix.
                if(newQueue != null && newQueue.startsWith(AppPro.EMS_WRK_PREFIX.getValue())){

                    int receiverCount = emsUtil.getQueueInfo(newQueue).getReceiverCount();
                    // Check Receiver Count is 0 ==> De-Active Queue
                    if(receiverCount == 0){
                        String assignableTasker = managerUtil.findIdleTasker();
                        managerUtil.assignWrkQtoTSK(newQueue, assignableTasker);

                        System.out.println("New Queue : " + newQueue + " has been assigned to " + assignableTasker);
                    }

                }

            } catch (JMSException e) {
                e.printStackTrace();
            }

            // Ack
            if (ackMode == Session.CLIENT_ACKNOWLEDGE ||
                ackMode == Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE ||
                ackMode == Tibjms.EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE)
            {
                try {
                    assert message != null;
                    message.acknowledge();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }

            /**
             * Received message: MapMessage=
             * {
             *  Header=
             *      { JMSMessageID={null}
             *        JMSDestination={Topic[$sys.monitor.queue.create]}
             *        JMSReplyTo={null}
             *        JMSDeliveryMode={22}
             *        JMSRedelivered={false}
             *        JMSCorrelationID={null}
             *        JMSType={null}
             *        JMSTimestamp={Wed Jun 02 16:18:55 KST 2021}
             *        JMSDeliveryTime={0}
             *        JMSExpiration={0}
             *        JMSPriority={4}
             *        }
             *  Properties=
             *      { conn_type={String:generic}
             *        server={String:EMS-SERVER}
             *        JMSXDeliveryCount={Integer:1}
             *        target_name={String:h4}
             *        target_object={String:queue}
             *        event_reason={String:producer}
             *        event_class={String:queue.create}
             *        conn_hostname={String:DESKTOP-SS74P4U}
             *        conn_connid={Long:27}
             *        conn_username={String:anonymous}
             *        }
             *  Fields={ } }
             */

        }
        System.out.println("Runnalbe Run Break While");

    }

    public static void main(String[] args) throws JMSException {

    }

    @Override
    public void setUpInstance() {

    }
}
