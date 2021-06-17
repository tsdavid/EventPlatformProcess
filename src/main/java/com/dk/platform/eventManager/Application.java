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

    private InitializeProcess initialize;

    @Override
    public void initialize() {
        this.initialize = InitializeProcess.builder()
                            .emsServerUrl(AppPro.EMS_URL.getValue())
                            .emsUserName(AppPro.EMS_USR.getValue())
                            .emsPassword(AppPro.EMS_PWD.getValue())
                            .build();
    }

    public Application() {

        this.initialize();

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

    }

//    @Override
//    public void run() {
//        new Application();
//    }


    public static void main(String[] args) {
        new Application();

    }
}
