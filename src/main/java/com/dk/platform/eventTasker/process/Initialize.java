package com.dk.platform.eventTasker.process;

import com.dk.platform.Process;
import com.dk.platform.ems.ConnConf;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventTasker.util.TaskerUtil;
import com.tibco.tibjms.admin.QueueInfo;
import com.tibco.tibjms.admin.TibjmsAdminException;

import javax.jms.JMSException;
import java.util.*;

/**
 * Tasker Initialize.
 * When New Tasker Run. Initialize Action will be taken by This (Initialize.java) class.
 *
 * 1. Send JMS Queue Message to Manger to let know new Tasker Running.
 *  1-1. check EMS find Queue with TASKER prefix.
 *  1-2. if> tasker is on EMS, Set Name
 */
public class Initialize implements Process {

    private EmsUtil emsUtil = null;
    private TaskerUtil taskerUtil = null;
    private String MYNAME = null;


    public Initialize() {

        try{
            emsUtil = new EmsUtil(ConnConf.EMS_URL.getValue(), ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue());
            taskerUtil = new TaskerUtil();
//            this.MYNAME = this.setTaskerProcessName();
            this.MYNAME = this.setTaskerProcessNameWithCheckReceiverCount();

        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            this.sendInitMsgToManager();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendInitMsgToManager() {
        // Send Queue Message.
        // Set-Up for name


        // Set-up Message Properties.
        Map<String, String> init_properties = new HashMap<>();
        init_properties.put(ConnConf.MSG_TYPE.getValue(), ConnConf.TSK_INIT_MNG_PROP.getValue());
        try {
            taskerUtil.sendQueueMessage(ConnConf.EMS_MNG_QUEUE_NAME.getValue(), this.MYNAME,init_properties,
                    0, false, true);
        } catch (JMSException e) {
            System.out.println("Error WIth Send Message.");
            e.printStackTrace();
        }
    }


    /**
     * Simple Version.
     * @return
     */
    private String setTaskerProcessName() {

        String[] activeTaskersName = emsUtil.getQueueNames(ConnConf.EMS_TSK_PREFIX.getValue());

        // if no tasker is in server.
        // set first tasker.
        if(activeTaskersName == null) return ConnConf.EMS_TSK_PREFIX.getValue().concat("1");
        // if  not numbering with name.
        else return ConnConf.EMS_TSK_PREFIX.getValue().concat(String.valueOf(activeTaskersName.length + 1));
    }

    /**
     * Set Process Name with Checking Receiver Count.
     * @return
     */
    private String setTaskerProcessNameWithCheckReceiverCount() {

        QueueInfo[] queueInfos = emsUtil.getQueueInfos(ConnConf.EMS_TSK_PREFIX.getValue());

        // if no tasker is in server.
        // set first tasker.
        if(queueInfos == null) return ConnConf.EMS_TSK_PREFIX.getValue().concat("1");
            // if  not numbering with name.
        else {
            int min = 999;
            for(QueueInfo queueInfo : queueInfos){
                if(queueInfo.getReceiverCount() != 0) continue;
                int num = Integer.parseInt(queueInfo.getName().split(ConnConf.EMS_TSK_PREFIX.getValue())[1]);
                if(num < min) min = num;
            }
            if (min != 999) return ConnConf.EMS_TSK_PREFIX.getValue().concat(String.valueOf(min));
            else return ConnConf.EMS_TSK_PREFIX.getValue().concat(String.valueOf(queueInfos.length + 1));
        }
    }

    public String getProcessName() {
        if(MYNAME == null) {
            this.setTaskerProcessName();
        }
        return MYNAME;
    }




    public static void main(String[] args) throws TibjmsAdminException {

        EmsUtil emsUtil = new EmsUtil(ConnConf.EMS_URL.getValue(), ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue());
        System.out.println(Arrays.toString(emsUtil.getQueueNames(ConnConf.EMS_TSK_PREFIX.getValue())));
        for(String name : emsUtil.getQueueNames(ConnConf.EMS_TSK_PREFIX.getValue())){
            System.out.println(
                    name.split(ConnConf.EMS_TSK_PREFIX.getValue())[1]
            );
        }
    }

}
