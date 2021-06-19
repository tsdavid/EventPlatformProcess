package com.dk.platform.eventManager;

import com.dk.platform.ems.AppPro;
import com.dk.platform.eventManager.process.ReceiverProcess;
import com.dk.platform.eventManager.process.NewQueueReceiverProcess;
import com.dk.platform.eventTasker.process.InitializeProcess;

public class Application implements com.dk.platform.Application{

    static {
        try {
            System.out.println("Application Static Block");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InitializeProcess initializeProcess;
    @Override
    public void initialize() {
        initializeProcess = InitializeProcess.builder()
                .emsServerUrl(AppPro.EMS_URL.getValue())
                .emsUserName(AppPro.EMS_USR.getValue())
                .emsPassword(AppPro.EMS_PWD.getValue())
                .build();
    }

    public Application() {

        this.initialize();
        this.initializeProcess.setUpInstance();

        try{
            NewQueueReceiverProcess systemTopicReceiver = new NewQueueReceiverProcess("$sys.monitor.queue.create", 1,true);
            systemTopicReceiver.setUpInstance();
            systemTopicReceiver.setActive();
            Thread thread = new Thread(systemTopicReceiver);
            thread.start();
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            ReceiverProcess receiverProcess = new ReceiverProcess(AppPro.EMS_MNG_QUEUE_NAME.getValue(), 1, false);
            receiverProcess.setUpInstance();
            receiverProcess.setActive();
            Thread thread1 = new Thread(receiverProcess);
            thread1.start();
        }catch (Exception e){
            e.printStackTrace();
        }


        // Set up Instance 한 후에 execute 를 통해 Scan EMS 를 기동
        this.initializeProcess.execute();

    }

//    @Override
//    public void run() {
//        new Application();
//    }


    public static void main(String[] args) {
        new Application();

    }
}
