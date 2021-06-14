package com.dk.platform.backup;

import com.dk.platform.ems.ConnConf;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.Consumer;
import com.dk.platform.eventManager.util.ManagerUtil;
import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.TibjmsMapMessage;
import com.tibco.tibjms.admin.TibjmsAdminException;

import javax.jms.*;

public class EmsReceiver_Backup implements Runnable, Consumer {

    private Connection connection;
    private Session session;
    private MessageConsumer msgConsumer;
    private Destination destination;
    private int ackMode;
    private boolean active = false;

    public EmsReceiver_Backup(String name, int ackMode, boolean isTopic) throws JMSException {

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

            } catch (JMSException e) {
                e.printStackTrace();
            }

            // Ack
            if (ackMode == Session.CLIENT_ACKNOWLEDGE ||
                ackMode == Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE ||
                ackMode == Tibjms.EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE)
            {
                try {
                    message.acknowledge();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }


        }
        System.out.println("Runnalbe Run Break While");

    }

    public static void main(String[] args) throws JMSException {

    }
}
