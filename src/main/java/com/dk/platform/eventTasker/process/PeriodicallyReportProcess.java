package com.dk.platform.eventTasker.process;

import com.dk.platform.Process;
import com.dk.platform.ems.ConnConf;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventTasker.util.MemoryStorage;
import com.dk.platform.eventTasker.util.TaskerUtil;
import com.dk.platform.eventTasker.vo.QueueVO;

import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PeriodicallyReportProcess implements Runnable, Process {

    private MemoryStorage memoryStorage = MemoryStorage.getInstance();
    private EmsUtil emsUtil = null;
    private TaskerUtil taskerUtil = null;

    public PeriodicallyReportProcess(){
        this.emsUtil = memoryStorage.getEmsUtil();
        this.taskerUtil = memoryStorage.getTaskerUtil();

        this.PrepareForTest();
        System.out.println("Check Out Prepare For Test...  " + memoryStorage.getQueueMap().toString());

    }

    private void PrepareForTest() {
        // Generate Work Queue
        for(int i=1; i <  5; i++){
            this.PutTestWorkQueue("TEST".concat(String.valueOf(i)));
        }
    }

    private void PutTestWorkQueue(String name) {

        String testQueueName = ConnConf.EMS_WRK_PREFIX.getValue().concat(name);
        QueueVO queueVO = QueueVO.builder()
                                .queueName(testQueueName)
                                .created_Time(new Timestamp(System.currentTimeMillis()))
                                .status("Working")
                                .build();
        memoryStorage.getQueueMap().put(testQueueName, queueVO);
    }

    @Override
    public void run() {

//        System.out.println("Run RUN Method");

        while (true){

            int delay = Integer.parseInt(ConnConf.TSK_POLLING_INTERVAL.getValue());
            try {
                Thread.sleep(delay * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Send Health Check Message.
            taskerUtil.sendHealthCheckMessage();

        }


    }

    @Deprecated
    private void ExecuteTest() {

        // Create Schedule Thread
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        Runnable runnable = () -> {
            System.out.println("Perodically Tasker Health Check Message");
            taskerUtil.sendHealthCheckMessage();
        };
        int delay = Integer.parseInt(ConnConf.TSK_POLLING_INTERVAL.getValue());
        executorService.schedule(runnable, 1, TimeUnit.SECONDS);
    }
}
