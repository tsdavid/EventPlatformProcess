package com.dk.platform.eventTasker.util;

import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.util.ManagerUtil;
import com.dk.platform.eventTasker.process.ReceiverProcess;
import com.tibco.tibjms.admin.QueueInfo;
import com.tibco.tibjms.admin.TibjmsAdminException;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskerUtil_Test {

    private EmsUtil emsUtil;
    private ManagerUtil managerUtil;
    private TaskerUtil taskerUtil;

    void setEmsUtil() {
        try {
            this.emsUtil = new EmsUtil(AppPro.EMS_URL.getValue(), AppPro.EMS_USR.getValue(), AppPro.EMS_PWD.getValue());
            this.managerUtil = new ManagerUtil(emsUtil);
            this.taskerUtil = new TaskerUtil(emsUtil);

            MemoryStorage.getInstance().setEmsUtil(emsUtil);
            MemoryStorage.getInstance().setTaskerUtil(taskerUtil);

        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
    }

    @After
    public void cleanUp() {
        emsUtil.destroyQueues(AppPro.EMS_TSK_PREFIX.getValue());
    }

    @Test
    public void setTaskerName_Test(){

        this.setEmsUtil();

        // given
        int cntOfTasker = 5;
        int answer = 3;

        for( int i=1; i <cntOfTasker+1; i++){
            if(i == answer) continue;
            String taskername = AppPro.EMS_TSK_PREFIX.getValue().concat(String.valueOf(i));
            // Set Up Receiver
            ReceiverProcess receiverProcess = new ReceiverProcess(taskername, 1, false);
            receiverProcess.setActive();
            Thread thread = new Thread(receiverProcess);
            thread.start();

        }

        String[] currentTskNames = emsUtil.getAct_or_DeAct_QueueNames(AppPro.EMS_TSK_PREFIX.getValue(), 1);
        System.out.println(
                Arrays.toString(currentTskNames)
        );


        // then
        String newTaskername = taskerUtil.setTaskerName();
        QueueInfo queueInfo = emsUtil.getQueueInfo(newTaskername);
        System.out.println("new Tasker name : " + newTaskername + " is null? " + queueInfo == null);

        int num = Integer.parseInt(newTaskername.split(AppPro.EMS_TSK_PREFIX.getValue())[1]);

        assertThat(answer).isEqualTo(num);

    }


}
