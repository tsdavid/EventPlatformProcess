package com.dk.platform.eventTasker;

import com.dk.platform.ems.AppPro;
import com.dk.platform.eventManager.process.NewQueueReceiverProcess;
import com.dk.platform.eventTasker.process.InitializeProcess;
import com.dk.platform.eventTasker.process.PeriodicallyReportProcess;
import com.dk.platform.eventTasker.process.ReceiverProcess;
import com.dk.platform.eventTasker.util.MemoryStorage;
import com.tibco.tibjms.admin.TibjmsAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * EMS Connection 관리를 위해
 * EMS Connection이 필요로 하는 EMS Util, Tasker or Manager Util을 Application 단위에서 init 한다.
 * 나머지 process는 application에서 설정된 util만을 사용한다.
 */
public class Application implements com.dk.platform.Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    static {
        try {
            logger.info("Application Static Block");
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

        String MyName = MemoryStorage.getInstance().getPROCESS_NAME();
        logger.info("New Event Tasker is running now.  Set ThreadName : {}", MyName);


        // Run Receiver Process.
        ReceiverProcess receiverProcess = new ReceiverProcess(MyName, Session.AUTO_ACKNOWLEDGE, false);
        try {

            receiverProcess.setUpInstance();
            receiverProcess.setActive();
            Thread thread = new Thread(receiverProcess);
            thread.start();
            logger.info("New Tasker Receiver Process is Start. Name : {}", MyName);

        } catch (TibjmsAdminException e) {
            logger.error("Error While Setup Instance for Tasker Receiver Process.  {}/{}", e.getMessage(), e.toString());
            e.printStackTrace();
        }



        // Run PeriodicallyReportProcess.
        PeriodicallyReportProcess periodicallyReportProcess = new PeriodicallyReportProcess();
        try {

            periodicallyReportProcess.setUpInstance();
            Thread thread1 = new Thread(periodicallyReportProcess);
            thread1.start();
            logger.info("New Tasker Periodically Report Process is Start. Name : {}", MyName);

        } catch (Exception e) {
            logger.error("Error While Setup Instance for Tasker Periodically Report Process.  {}/{}", e.getMessage(), e.toString());
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {


        logger.info("Tasker Application is Running");
        new Application();

    }
}
