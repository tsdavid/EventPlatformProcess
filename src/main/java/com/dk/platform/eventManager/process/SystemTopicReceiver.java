package com.dk.platform.eventManager.process;

// Consumer System topic to get info about new created destination.
// topic receiver ==> $sys.monitor.queue.create


import com.dk.platform.Process;
import com.dk.platform.ems.ConnConf;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.Consumer;
import com.dk.platform.eventManager.util.ManagerUtil;
import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.TibjmsConnectionFactory;
import com.tibco.tibjms.TibjmsMapMessage;
import com.tibco.tibjms.admin.TibjmsAdminException;

import javax.jms.*;

public class SystemTopicReceiver implements Runnable, Consumer, Process {

    private final String PROPERTY_NAME = "target_name";
    private Connection connection;
    private Session session;
    private MessageConsumer msgConsumer;
    private Destination destination;
    private int ackMode;
    private boolean active = false;

    public SystemTopicReceiver(String name, int ackMode, boolean isTopic) throws JMSException {

        this.ackMode = ackMode;
        try {
            this.connection = new EmsUtil(ConnConf.EMS_URL.getValue(), ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue()).getEmsConnection();
            this.session = this.connection.createSession(ackMode);
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
        destination = (isTopic) ? session.createTopic(name) : session.createQueue(name);
        msgConsumer = session.createConsumer(destination);

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

        System.out.println("Runnalbe Run Cnt active status : " + active);


        while(active){
            System.out.println("Runnalbe Run While Start : ");


            TibjmsMapMessage message = null;
            try {
                message = (TibjmsMapMessage) msgConsumer.receive();

                // Get Queue Name.
                String CreatedNewQueueName = null;
                try{
                    CreatedNewQueueName = message.getStringProperty(PROPERTY_NAME);

                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println(message.getStringProperty(PROPERTY_NAME));


                // Check WRK Queue Prefix.
                if(CreatedNewQueueName != null){

                    if(CreatedNewQueueName.startsWith(ConnConf.EMS_WRK_PREFIX.getValue())){

                        // Check Receive Count is Null
//                        if(EmsUtil.getReceiverCount(CreatedNewQueueName) != null){
//
//                            // Check Receiver Count.
//                            if(EmsUtil.getReceiverCount(CreatedNewQueueName) == 0){
//                                // Assign Task.
//                                System.out.println("Start Assign Task");
//
//                            // Prefix, Not Null,  But Who is Receive This Queue?
//                            }else{
//                                System.out.println("Wrong Case.  Some Tasker didnt close resource.");
//                            }
//
//                        }
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
}
