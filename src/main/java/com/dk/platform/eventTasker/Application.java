package com.dk.platform.eventTasker;

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
            System.out.println("Application Static Block");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InitializeProcess initialize;

    @Override
    public void initialize() {
        this.initialize = new InitializeProcess();

    }

    public Application() {

        this.initialize();
        if(this.initialize == null){
            this.initialize = new InitializeProcess();
        }

        String MyName = MemoryStorage.getInstance().getPROCESS_NAME();
        System.out.println(MyName);

        // Run all Process.
        ReceiverProcess receiverProcess = new ReceiverProcess(MyName, Session.AUTO_ACKNOWLEDGE, false);
        try {
            receiverProcess.setUpInstance();
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
        receiverProcess.setActive();
        Thread thread = new Thread(receiverProcess);
        thread.start();

        try {
            PeriodicallyReportProcess periodicallyReportProcess = new PeriodicallyReportProcess();
            Thread thread1 = new Thread(periodicallyReportProcess);
            thread1.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        new Application();

        logger.info("hgello");
    }
}
