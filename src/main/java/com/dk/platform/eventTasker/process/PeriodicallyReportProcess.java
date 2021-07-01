package com.dk.platform.eventTasker.process;

import com.dk.platform.Process;
import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventTasker.util.MemoryStorage;
import com.dk.platform.eventTasker.util.TaskerUtil;
import com.dk.platform.eventTasker.vo.QueueVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Main Job
 * 1. Process Logic.
 */
public class PeriodicallyReportProcess implements Runnable, Process {


    /*****************************************************************************************
     **************************************  Logger ******************************************
     ****************************************************************************************/


    private static final Logger logger = LoggerFactory.getLogger(PeriodicallyReportProcess.class);


    /*****************************************************************************************
     ***********************************  Variables ******************************************
     ****************************************************************************************/

    private MemoryStorage memoryStorage;

    private EmsUtil emsUtil;

    private TaskerUtil taskerUtil;


    /*
     ***********************************  Constructor ****************************************
     ****************************************************************************************/


    /**
     *
     */
    public PeriodicallyReportProcess(){

    }


    /**
     * Essential Instance
     */
    public void setUpInstance(){

        this.memoryStorage = MemoryStorage.getInstance();

        this.emsUtil = memoryStorage.getEmsUtil();

        this.taskerUtil = memoryStorage.getTaskerUtil();
    }


    /*****************************************************************************************
     ***********************************  Process Logic **************************************
     *****************************************************************************************/

    @Override
    public void run() {

        logger.info(" Run Method, Run Execute Method");

        this.execute();


    }


    /**
     * run Logic
     * if runnable case, run() {
     * execute() {
     * Logic.
     * }
     * }
     */
    @Override
    public void execute() {

        logger.info("[{}] Process is Now Run ", "ProcessLogic");
        // TODO THINK BETTER ==> Don't Need to Set Thread Name??.

        // TODO THINK BETTER ==> while???, Thread Sleep????? ==> Busy Wait Case..
        // Only way to use while loop?
        while (true){

            int delay = Integer.parseInt(AppPro.TSK_POLLING_INTERVAL.getValue());
            try {
                // TODO THINK BETTER ==> Busy-Waiting...

                Thread.sleep(delay * 1000L);

            } catch (InterruptedException e) {
                logger.error("[{}] Error : {}/{}.","HealthCheck", e.getMessage(), e.toString());
                e.printStackTrace();
            }

            // Send Health Check Message.
            taskerUtil.sendMessageToManager(true, false, false, "");
            logger.info("Tasker : {}.  Send Health Check Message ", memoryStorage.getPROCESS_NAME());

        }

    }


    /*****************************************************************************************
     *************************************  Deprecated ***************************************
     ****************************************************************************************/


    @Deprecated
    private void ExecuteTest() {

        // Create Schedule Thread
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        Runnable runnable = () -> {
            System.out.println("Perodically Tasker Health Check Message");
            taskerUtil.sendHealthCheckMessage();
        };
        int delay = Integer.parseInt(AppPro.TSK_POLLING_INTERVAL.getValue());
        executorService.schedule(runnable, 1, TimeUnit.SECONDS);
    }


    @Deprecated
    private void PrepareForTest() {
        // Generate Work Queue
        for(int i=1; i <  5; i++){
            this.PutTestWorkQueue("TEST".concat(String.valueOf(i)));
        }
    }


    @Deprecated
    private void PutTestWorkQueue(String name) {

        String testQueueName = AppPro.EMS_WRK_PREFIX.getValue().concat(name);
        QueueVO queueVO = QueueVO.builder()
                .queueName(testQueueName)
                .created_Time(new Timestamp(System.currentTimeMillis()))
                .status("Working")
                .build();
        memoryStorage.getQueueMap().put(testQueueName, queueVO);
    }


    /*****************************************************************************************
     *************************************  Main *********************************************
     ****************************************************************************************/


    public static void main(String[] args) {

    }
}
