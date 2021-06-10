package com.dk.platform.eventTasker.process;

import com.dk.platform.Process;
import com.dk.platform.ems.ConnConf;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventTasker.util.TaskerUtil;
import com.tibco.tibjms.admin.QueueInfo;

/**
 * Tasker Initialize.
 * When New Tasker Run. Initialize Action will be taken by This (Initialize.java) class.
 *
 * 1. Send JMS Queue Message to Manger to let know new Tasker Running.
 */
public class Initialize implements Process {

    public Initialize() {
        EmsUtil emsUtil = null;
        try{
            emsUtil = new EmsUtil(ConnConf.EMS_URL.getValue(), ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue());
        }catch (Exception e){

        }



        TaskerUtil taskerUtil = new TaskerUtil(ConnConf.EMS_URL.getValue());
//        taskerUtil.sendQueueMessage();
    }


}
