package com.dk.platform.eventManager;

import com.dk.platform.ems.ConnConf;
import com.dk.platform.eventManager.process.ReceiverProcess;
import com.dk.platform.eventManager.process.SystemTopicReceiver;
import com.dk.platform.eventManager.util.ManagerUtil;
import com.dk.platform.eventTasker.process.Initialize;

import javax.jms.JMSException;

public class Application implements com.dk.platform.Application{

    static {
        try {
            System.out.println("Application Static Block");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Initialize initialize;

    @Override
    public void initialize() {
//        this.initialize = new Initialize();
    }

    public Application() {

//        this.initialize();
//        if(this.initialize == null){
//            this.initialize = new Initialize();
//        }

        try{
            SystemTopicReceiver systemTopicReceiver = new SystemTopicReceiver("$sys.monitor.queue.create", 1,true);
            systemTopicReceiver.setActive();
            Thread thread = new Thread(systemTopicReceiver);
            thread.start();
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            ReceiverProcess receiverProcess = new ReceiverProcess(ConnConf.EMS_MNG_QUEUE_NAME.getValue(), 1, false);
            receiverProcess.setActive();
            Thread thread1 = new Thread(receiverProcess);
            thread1.start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

//    @Override
//    public void run() {
//        new Application();
//    }


    public static void main(String[] args) {

    }
}
