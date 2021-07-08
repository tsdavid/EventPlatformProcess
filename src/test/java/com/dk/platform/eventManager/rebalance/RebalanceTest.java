package com.dk.platform.eventManager.rebalance;

import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.util.ManagerUtil;
import com.dk.platform.eventManager.util.MemoryStorage;
import com.dk.platform.eventManager.vo.TaskerVO;
import com.dk.platform.eventTasker.process.ReceiverProcess;
import com.dk.platform.eventTasker.util.TaskerUtil;
import com.tibco.tibjms.admin.TibjmsAdminException;
import org.junit.After;
import org.junit.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

public class RebalanceTest {

    private EmsUtil emsUtil;
    private ManagerUtil managerUtil;
    private TaskerUtil taskerUtil;

    void setEmsUtil() {
        try {
            this.emsUtil = new EmsUtil(AppPro.EMS_URL.getValue(), AppPro.EMS_USR.getValue(), AppPro.EMS_PWD.getValue());
            this.managerUtil = new ManagerUtil(emsUtil);
            this.taskerUtil = new TaskerUtil(emsUtil);

        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
    }

    @Test
    /**
     * TEST SCENARIO
     * 1. One or Two Tasker Work With WorkQueue.
     * 2. New Tasker is Active
     * 3. Manager Send request First and Second Tasker to distribute Work Queue.
     * 4. First and Second Tasker Return WorkQueue Back to Manager
     * 5. Manager Assign Work Queue to lowest Tasker.
     */
//    public void DistributeWRK_Queue_Test(){
//
//        // 1. Generate Work Queue with lots of Message.
//        // 2. Run Tasker and Assign WRK to them.
//    }
    @After
    public void cleanEmsServer(){
        emsUtil.destroyQueues(AppPro.EMS_WRK_PREFIX.getValue());
        emsUtil.destroyQueues(AppPro.EMS_TSK_PREFIX.getValue());
    }


    @Test
    public void getAvgWrkQinTSK_TEST(){

        this.setEmsUtil();

        com.dk.platform.eventTasker.util.MemoryStorage.getInstance().setEmsUtil(emsUtil);
        com.dk.platform.eventTasker.util.MemoryStorage.getInstance().setTaskerUtil(taskerUtil);


        // given
        // WorkQueue => 10,,  TSK => 3
        final int cntWrkQ = 10; final int cntTskQ = 3;
        int answer = cntWrkQ / cntTskQ;

//        ArrayList<String> arrayList = new ArrayList<>();

        // Create WorkQueue
        for(int i=1; i < cntWrkQ+1; i++){
            String workQueueName = AppPro.EMS_WRK_PREFIX.getValue().concat(String.valueOf(i));
            // Create workQueue on EMS Server.
            try{
                emsUtil.createQueue(workQueueName);

            }catch (TibjmsAdminException e){
                e.printStackTrace();
            }

            // add arr
//            arrayList.add(workQueueName);
        }

        // Craete Tasker.
        for(int i=0; i < cntTskQ; i++){

            int wrkQ = 3;
            if(i == cntTskQ ) wrkQ = wrkQ +1;
            String taskerName = AppPro.EMS_TSK_PREFIX.getValue().concat(String.valueOf(i));
            TaskerVO vo = TaskerVO.builder()
                    .taskerName(taskerName)
                    .created_Time(new Timestamp(System.currentTimeMillis()))
                    .cnt(wrkQ).workQueueList("")
                    .build();
            MemoryStorage.getInstance().getTasker_Mng_Map().put(taskerName, vo);

            // Create Tsk Queue on EMS Server.
            try{
                emsUtil.createQueue(taskerName);

            }catch (TibjmsAdminException e){
                e.printStackTrace();
            }

            // Set Up Receiver
            ReceiverProcess receiverProcess = new ReceiverProcess(taskerName, 1, false);
            receiverProcess.setActive();
            Thread thread = new Thread(receiverProcess);
            thread.start();

        }
        System.out.println(MemoryStorage.getInstance().getTasker_Mng_Map().toString());


        // new Tasker Arrive.
        String newtaskerName = taskerUtil.setTaskerName();
        System.out.println("new Tasker Name : " + newtaskerName);
        TaskerVO vo = TaskerVO.builder()
                .taskerName(newtaskerName)
                .created_Time(new Timestamp(System.currentTimeMillis()))
                .cnt(0).workQueueList("")
                .build();
        MemoryStorage.getInstance().getTasker_Mng_Map().put(newtaskerName, vo);
        // Create Tsk Queue on EMS Server.
        try{
            emsUtil.createQueue(newtaskerName);

        }catch (TibjmsAdminException e){
            e.printStackTrace();
        }
        // Set Up Receiver
        ReceiverProcess receiverProcess = new ReceiverProcess(newtaskerName, 1, false);
        receiverProcess.setActive();
        Thread thread = new Thread(receiverProcess);
        thread.start();


        // then
        int o_taserCount = managerUtil.searchO_Tasker(newtaskerName).length;
        int searchWorkQueueCount = managerUtil.searchWorkQueue().length;
        int avgWorkQueue = managerUtil.getAvgWorkQueueOwnedByTSK(); // tobe_result

        System.out.println(MemoryStorage.getInstance().getTasker_Mng_Map().toString());
        System.out.println(MemoryStorage.getInstance().getTasker_Mng_Map().size());
        System.out.println("o_taserCount : " + o_taserCount + " searchWorkQueueCount : " + searchWorkQueueCount + " avgWorkQueueinTasker : "  + avgWorkQueue);

        int threshold_result = searchWorkQueueCount / o_taserCount; // => threshold

        System.out.println("threshold : " + threshold_result  + "  toBE : " + avgWorkQueue + " cntWrkQ / cntTskQ + 1 : " + (cntWrkQ / cntTskQ + 1));

        assertThat(answer).isEqualTo(threshold_result);
        assertThat(cntWrkQ / (cntTskQ + 1)).isEqualTo(avgWorkQueue);

    }
}
