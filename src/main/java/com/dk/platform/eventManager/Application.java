package com.dk.platform.eventManager;

import com.dk.platform.ems.AppPro;
import com.dk.platform.eventManager.process.ReceiverProcess;
import com.dk.platform.eventManager.process.NewQueueReceiverProcess;
import com.dk.platform.eventManager.process.InitializeProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application implements com.dk.platform.Application{

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    static {
        try {
            logger.info("Application Static Block");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InitializeProcess initializeProcess;

    public Application() {}

    public Application(String filePath) {

        this.initialize(filePath);
        this.initializeProcess.setUpInstance();


        // Run New Queue Receiver Process.
        NewQueueReceiverProcess systemTopicReceiver = new NewQueueReceiverProcess("$sys.monitor.queue.create", 1,true);
        try{

            systemTopicReceiver.setUpInstance();
            systemTopicReceiver.setActive();
            Thread thread = new Thread(systemTopicReceiver);
            thread.start();
            logger.info("New Queue Receiver Process is Start.");

        }catch (Exception e){
            logger.error("Error While Setup Instance for New Queue Receiver Process.  {}/{}", e.getMessage(), e.toString());
            e.printStackTrace();
        }


        // Run Receiver Process.
        ReceiverProcess receiverProcess = new ReceiverProcess(AppPro.EMS_MNG_QUEUE_NAME.getValue(), 1, false);
        try{

            receiverProcess.setUpInstance();
            receiverProcess.setActive();
            Thread thread1 = new Thread(receiverProcess);
            thread1.start();
            logger.info("New Manager Receiver Process is Start.");

        }catch (Exception e){
            logger.error("Error While Setup Instance for Manager Receiver Process.  {}/{}", e.getMessage(), e.toString());
            e.printStackTrace();
        }


        // Set up Instance ??? ?????? execute ??? ?????? Scan EMS ??? ??????
        this.initializeProcess.execute();

    }

    @Override
    public void initialize(String filePath) {
        initializeProcess = InitializeProcess.builder()
                .emsServerUrl(AppPro.EMS_URL.getValue())
                .emsUserName(AppPro.EMS_USR.getValue())
                .emsPassword(AppPro.EMS_PWD.getValue())
                .build();
    }



    public static void main(String[] args) {

        // TODO EppConf File Path
        logger.info("Manager Application is Running");
        new Application("");

    }
}
