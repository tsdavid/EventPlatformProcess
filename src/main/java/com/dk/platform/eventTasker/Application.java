package com.dk.platform.eventTasker;

import com.dk.platform.eventTasker.process.InitializeProcess;
import com.dk.platform.eventTasker.process.PeriodicallyReportProcess;
import com.dk.platform.eventTasker.process.ReceiverProcess;
import com.dk.platform.eventTasker.util.MemoryStorage;
import com.tibco.tibjms.admin.TibjmsAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Session;
import java.util.concurrent.ConcurrentLinkedDeque;

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

    public Application() {}

    public Application(String filePath) {

        this.initialize(filePath);
        this.initializeProcess.setUpInstance();
        this.initializeProcess.execute();

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

    @Override
    public void initialize(String filePath) {


        initializeProcess = new InitializeProcess(filePath);

    }



    public static void main(String[] args) {

        // TODO EppConf File Path
//        logger.info("Tasker Application is Running");
//        new Application("");

        ConcurrentLinkedDeque deque = new ConcurrentLinkedDeque();
        deque.add("h"); deque.add("h1"); deque.add("h2");
        logger.info(deque.toString());

        logger.info((String) deque.poll());
        logger.info(deque.toString());

        Object obj = deque.peek();
        logger.info((String) obj);
        logger.info(deque.toString());

        deque.add("h3");
        logger.info(deque.toString());









    }
}
