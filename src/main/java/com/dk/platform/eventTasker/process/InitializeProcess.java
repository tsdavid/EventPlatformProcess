package com.dk.platform.eventTasker.process;

import com.dk.platform.Process;
import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventTasker.util.MemoryStorage;
import com.dk.platform.eventTasker.util.TaskerUtil;
import com.tibco.tibjms.admin.QueueInfo;
import com.tibco.tibjms.admin.TibjmsAdminException;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.util.*;

/**
 * Main Job
 * 1. Tasker Application Initialize.
 *
 * Tasker Initialize.
 * When New Tasker Run. Initialize Action will be taken by This (Initialize.java) class.
 *
 * 1. Send JMS Queue Message to Manger to let know new Tasker Running.
 *  1-1. check EMS find Queue with TASKER prefix.
 *  1-2. if> tasker is on EMS, Set Name
 */
@NoArgsConstructor
public class InitializeProcess implements Process {


    /*****************************************************************************************
     **************************************  Logger ******************************************
     ****************************************************************************************/

    private static final Logger logger = LoggerFactory.getLogger(InitializeProcess.class);


    private EmsUtil emsUtil;

    private String MYNAME;

    private String emsServerUrl;

    private String emsUserName;

    private String emsPassword;


    /*****************************************************************************************
     ***********************************  Constructor ****************************************
     ****************************************************************************************/

    @Builder
    public InitializeProcess(String emsServerUrl, String emsUserName, String emsPassword) {

        this.emsServerUrl = emsServerUrl;

        this.emsUserName = emsUserName;

        this.emsPassword = emsPassword;

    }


    /**
     * Use Instantiate Essential Class.
     */
    @Override
    public void setUpInstance() {

        /*
         ***********************************  Variables ******************************************
         ****************************************************************************************/
        MemoryStorage memoryStorage = MemoryStorage.getInstance();

        try {
            this.emsUtil = new EmsUtil(this.emsServerUrl, this.emsUserName, this.emsPassword);
            memoryStorage.setEmsUtil(this.emsUtil);

        } catch (TibjmsAdminException e) {

            logger.error("[{}] Cannot Get EmsUtil Instance. Exit System. Error : {}/{}.","TaskerInitialize", e.getMessage(), e.toString());
            e.printStackTrace();
            System.exit(1);
        }


        TaskerUtil taskerUtil = new TaskerUtil();
        memoryStorage.setTaskerUtil(taskerUtil);

        this.MYNAME = taskerUtil.setTaskerName();
        memoryStorage.setPROCESS_NAME(this.MYNAME);
        logger.info("Process Name is ready to Set-up : {}.", MYNAME);

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

        // Let Manager TSK run.
        try {
            this.sendInitMsgToManager();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /*
     *******************************  Send Init Message Logic ********************************
     *****************************************************************************************/

    /**
     * Send Queue Message to Manager to let know new Tasker is run.
     */
    private void sendInitMsgToManager() {

        // Set-up Message Properties.
        Map<String, String> init_properties = new HashMap<>();
        init_properties.put(AppPro.MSG_TYPE.getValue(), AppPro.TSK_INIT_MNG_VAL.getValue());

        try {

            emsUtil.sendQueueMessage(AppPro.EMS_MNG_QUEUE_NAME.getValue(), this.MYNAME,init_properties,
                    0, false, true);

        } catch (JMSException e) {

            logger.error("[{}] Error : {}/{}.","SendInitMsg", e.getMessage(), e.toString());
            e.printStackTrace();

            // TODO THINK BETTER ==> Fail to Send Message.
        }
    }


    /*
     *************************************  Deprecated ***************************************
     ****************************************************************************************/

    /**
     * Set-Up For Tasker Name.
     * @return          :       New Tasker Name.
     */
    @Deprecated
    private String setTaskerName() {

        for(int i=1;; i++){
            String possibleName = AppPro.EMS_TSK_PREFIX.getValue().concat(String.valueOf(i));
            QueueInfo queueInfo = emsUtil.getQueueInfo(possibleName);
            // Does QueueName is not exist in Server, then use it !.
            if(queueInfo == null) return possibleName;
                // If Exist, then Does it have receiver?, if not => use it!.
            else if(queueInfo.getReceiverCount() == 0) return possibleName;
        }

    }


    /*****************************************************************************************
     *************************************  Main *********************************************
     ****************************************************************************************/

    public static void main(String[] args) throws TibjmsAdminException {

//        EmsUtil emsUtil = new EmsUtil(ConnConf.EMS_URL.getValue(), ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue());
//        System.out.println(Arrays.toString(emsUtil.getQueueNames(ConnConf.EMS_TSK_PREFIX.getValue())));
//        for(String name : emsUtil.getQueueNames(ConnConf.EMS_TSK_PREFIX.getValue())){
//            System.out.println(
//                    name.split(ConnConf.EMS_TSK_PREFIX.getValue())[1]
//            );
//        }
    }

}
