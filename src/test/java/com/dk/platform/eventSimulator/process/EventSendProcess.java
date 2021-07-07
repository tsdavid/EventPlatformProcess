package com.dk.platform.eventSimulator.process;

import com.dk.platform.ems.util.EmsUtil;
import com.tibco.tibjms.admin.TibjmsAdminException;
import lombok.extern.slf4j.Slf4j;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;

@Slf4j
public class EventSendProcess implements Runnable{

    static {
        try {
            log.info("" +
                    "" +
                    "=========================================================\n" +
                    "=========================================================\n" +
                    "======         ==========================================\n" +
                    "=========================================================\n" +
                    "=========================================================\n" +
                    "=========================================================\n" +
                    "Application Event Simulator Static Block\n"+
                    "=========================================================\n" +
                    "=========================================================\n" +
                    "======         ==========================================\n" +
                    "=========================================================\n" +
                    "=========================================================\n" +
                    "=========================================================\n" +
                    "");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String queueName = "";
    private int count = 10000;          //  Message Count
    private int delivery = DeliveryMode.PERSISTENT;
    private String type = "";
    private boolean random = false;

    private EmsUtil emsUtil;


    public EventSendProcess(){

    }

    public EventSendProcess(String queueName, int count, int deliveryMode,
                            String type, boolean NameRandom) throws TibjmsAdminException {

        this.emsUtil = new EmsUtil();

        this.queueName = queueName;

        this.count = count;

        this.delivery = deliveryMode;

        this.type = type;

        this.random = NameRandom;

    }


    /**
     *
     * @param type
     * @return
     */
    private String getMessage(String type, int number){

        // TODO Message Generator.
        return "";
    }



    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        try {
            emsUtil.sendSyncQueueMessage(queueName, getMessage(type, 1), null);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

    }
}
