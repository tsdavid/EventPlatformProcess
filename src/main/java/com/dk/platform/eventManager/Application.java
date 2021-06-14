package com.dk.platform.eventManager;

import com.dk.platform.eventManager.process.SystemTopicReceiver;
import com.dk.platform.eventManager.util.ManagerUtil;

import javax.jms.JMSException;

public class Application {

    static {
        try {
            System.out.println("Application Static Block");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Application() throws JMSException {
        SystemTopicReceiver systemTopicReceiver = new SystemTopicReceiver("$sys.monitor.queue.create", 1,true);
        systemTopicReceiver.run();

        try {
            Thread.sleep(5000);

            System.out.println(" After 5s..., Set DeActive");

            systemTopicReceiver.setDeActive();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws JMSException {
        new Application();

    }
}
