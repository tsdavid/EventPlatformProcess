package com.dk.platform.eventTasker.util;

import com.dk.platform.common.EppConf;
import com.dk.platform.ems.util.EmsUtil;

import javax.jms.*;

import com.dk.platform.ems.common.tibjmsPerfCommon;
import com.tibco.tibjms.admin.QueueInfo;
import com.tibco.tibjms.admin.TibjmsAdmin;
import com.tibco.tibjms.admin.TibjmsAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * Main Job
 * 1. Report Manager Logic
 * 2. ETC Logic.
 */
public class TaskerUtil extends tibjmsPerfCommon{

    /*
     **************************************  Logger ******************************************
     ****************************************************************************************/


    private static final Logger logger = LoggerFactory.getLogger(TaskerUtil.class);


    /*
     ***********************************  Variables ******************************************
     *****************************************************************************************/


    private Connection connection;

    private Session session;

    private MemoryStorage memoryStorage;

    private EmsUtil emsUtil = null;

    private EppConf eppConf;

    private String ManagerName;

    /*
     ********************************  Message Properties ************************************
     ****************************************************************************************/


    /**
     * Pre Set-up EMS Message Header Properties about Health Check Message.
     */
    private Map<String, String> HealthCheckProperties;

    /**
     * Pre Set-up EMS Message Header Properties about Complete Queue Message.
     */
    private Map<String, String> CompleteQueueProperties;


    /**
     * Pre Set-up EMS Message Header Properties about Returned Queue Message by Re-balance.
     */
    private Map<String, String> RebalanceReturnedQueueProperties;


    /* PROP */
    private final String FROM_TASKER_PROP = MemoryStorage.getInstance().getEppConf().message.getTSK_Health_Msg_FromVal();





    /*
     ***********************************  Constructor ****************************************
     *****************************************************************************************/


    /**
     *
     */
    public TaskerUtil(){

        this.setUpInstance();

        this.setUpMessageProperties();

        if(connection == null){
            this.connection = emsUtil.getEmsConnection();
        }
        try {
            this.session = this.connection.createSession(Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param emsUtil       :       EmsUtil
     */
    public TaskerUtil(EmsUtil emsUtil) {

        this.setUpInstance(emsUtil);

        this.setUpMessageProperties();

        this.connection = emsUtil.getEmsConnection();
        try {
            this.session = this.connection.createSession(Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }


    /**
     * Essential Instance
     */
    private void setUpInstance(){

        this.memoryStorage = MemoryStorage.getInstance();

        this.emsUtil = memoryStorage.getEmsUtil();

        this.eppConf = memoryStorage.getEppConf();

        ManagerName = eppConf.ems.getManagerQueueVal();
    }


    /**
     * Essential Instance
     */
    private void setUpInstance(EmsUtil emsUtil){

        this.memoryStorage = MemoryStorage.getInstance();

        this.emsUtil = emsUtil;
    }


    /**
     * Set-Up Main Message Properties.
     */
    private void setUpMessageProperties() {

        // Set-Up Assign Tsk Properties.
        this.HealthCheckProperties = new HashMap<>(); this.CompleteQueueProperties = new HashMap<>();
        this.RebalanceReturnedQueueProperties = new HashMap<>();

//        this.HealthCheckProperties.put(AppPro.MSG_TYPE.getValue(), AppPro.TSK_HLTH_MNG_VAL.getValue());
        this.HealthCheckProperties.put(eppConf.message.getMessage_TypeVal(), eppConf.message.getTSK_Health_MessageVal());

//        this.CompleteQueueProperties.put(AppPro.MSG_TYPE.getValue(), AppPro.TSK_COM_WRK_MNG_VAL.getValue());
        this.CompleteQueueProperties.put(eppConf.message.getMessage_TypeVal(), eppConf.message.getTSK_Complete_WRK_To_ManagerVal());

//        this.RebalanceReturnedQueueProperties.put(AppPro.MSG_TYPE.getValue(), AppPro.TSK_RBL_RTN_MNG_VAL.getValue());
        this.RebalanceReturnedQueueProperties.put(eppConf.message.getMessage_TypeVal(), eppConf.message.getTSK_RBL_Return_MessageVal());

    }


    /*
     ********************************  Report Manager Logic **********************************
     *****************************************************************************************/


    /**
     * Message Send to Manager Method.
     * Select Case with each flags. Have to Choose Only one Case of Three flags.
     * @param ishealth                  :           true : Tasker Health Check Message Case. No Duplicate
     * @param isRebalanceReturn         :           true : Tasker Returned Work Queue by Re-balance Case. No Duplicate
     * @param isComMsg                  :           true : Complete Message Case. No Duplicate
     * @param workQueueName             :           Work Queue Name. when if use Re-balance or Complete Case
     */
    public void sendMessageToManager(boolean ishealth, boolean isRebalanceReturn,  boolean isComMsg, String workQueueName){

        // Destination Information.

        // Set-up For Properties.
        Map<String,String> properties = null;
        String msg = "";

        // Complete WorkQueue Case.
        if(isComMsg) {
            properties = this.CompleteQueueProperties;
            properties.put(FROM_TASKER_PROP,memoryStorage.getPROCESS_NAME());
            msg = workQueueName;
        }

        // Health Check Message Case.
        if(ishealth) {
            properties = this.HealthCheckProperties;
            properties.put(FROM_TASKER_PROP,memoryStorage.getPROCESS_NAME());
            for(String k : memoryStorage.getQueueMap().keySet()){
                msg = msg.concat(k+",");
            }
        }

        // Re-balance Returned Queue Message Case.
        if(isRebalanceReturn){
            properties = this.RebalanceReturnedQueueProperties;
            msg = workQueueName;
        }


        // Send Queue
        try {
            emsUtil.sendSafeQueueMessage(ManagerName, msg, properties);

        } catch (JMSException e) {
            logger.error("[{}] Error : {}/{}.","ReportManager", e.getMessage(), e.toString());
            e.printStackTrace();
        }

    }




    /*
     ************************************  ETC Logic *****************************************
     *****************************************************************************************/


    /**
     * Get Tasker Prefix. Concat with Number start from 1 to 999 and make possible name.
     * Check Possible name is in EMS Server. if not, make possible name to process name.
     * if exist move to next number util get process name.
     *
     * Move to EmsUtil
     * Verified
     * Set-Up For Tasker Name.
     * @return              :           Tasker Name
     */
    public String setTaskerName() {


        for(int i=1;; i++){
//            String possibleName = AppPro.EMS_TSK_PREFIX.getValue().concat(String.valueOf(i));
            String possibleName = eppConf.ems.getTaskQueueVal().concat(String.valueOf(i));

            logger.debug("Process Possible Name : {}.", possibleName);

            TibjmsAdmin tibjmsAdmin = emsUtil.getTibjmsAdmin();
            QueueInfo queueInfo = null;
            try {
                queueInfo = tibjmsAdmin.getQueue(possibleName);
            } catch (TibjmsAdminException e) {
                logger.error("[{}] Error : {}/{}.","EtcLogic", e.getMessage(), e.toString());
                e.printStackTrace();
            }

            // Does QueueName is not exist in Server, then use it !.
            if(queueInfo == null) {
                return possibleName;

            // If Exist, then Does it have receiver?, if not => use it!.
            }else{
                if(queueInfo.getReceiverCount() == 0) {
                    return possibleName;
                }
            }
        }
    }


    /*
     *************************************  Deprecated ***************************************
     ****************************************************************************************/


    /**
     * Send Re-Balance Return Message.
     * @param workQueueName         :           Work Queue Name.
     */
    @Deprecated
    public void sendRebalanceReturnMessage(String workQueueName) {


        // Set-up For Properties.
        Map<String,String> properties = this.RebalanceReturnedQueueProperties;

        try {
            emsUtil.sendSafeQueueMessage(ManagerName, workQueueName, properties);

        } catch (JMSException e) {
            logger.error("[{}] Error : {}/{}.","ReportManager", e.getMessage(), e.toString());
            e.printStackTrace();
        }
    }


    /**
     * Replace with sendMessageToManager
     */
    @Deprecated
    public void sendHealthCheckMessage() {

        // Send Message. Get All Work Queue.
        String msg = "";
        for(String k : memoryStorage.getQueueMap().keySet()){
            msg = msg.concat(k+",");
        }

        // Set-up For Properties.
        Map<String,String> properties = this.HealthCheckProperties;
        properties.put(FROM_TASKER_PROP,memoryStorage.getPROCESS_NAME());
        try {
            emsUtil.sendAsyncQueueMessage(ManagerName, msg, properties);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


    /**
     * Replace with sendMessageToManager
     * @param workQueueName         :           Completed Work Queue Name.
     */
    @Deprecated
    public void sendCompleteWorkQueueMessage(String workQueueName) {


        // Set-up For Properties.
        Map<String,String> properties = this.CompleteQueueProperties;
        properties.put(FROM_TASKER_PROP,memoryStorage.getPROCESS_NAME());
        try {
            emsUtil.sendSafeQueueMessage(ManagerName, workQueueName, properties);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


    /*****************************************************************************************
     *************************************  Main *********************************************
     ****************************************************************************************/


    public static void main(String[] args) throws TibjmsAdminException {

//        EmsUtil emsUtil = null;
//        try {
//            emsUtil = new EmsUtil(AppPro.EMS_URL.getValue(), AppPro.EMS_USR.getValue(), AppPro.EMS_PWD.getValue());
//
//        } catch (TibjmsAdminException e) {
//            e.printStackTrace();
//        }
//
//        TibjmsAdmin tibjmsAdmin  = emsUtil.getTibjmsAdmin();
//
//        QueueInfo[] queueInfo = tibjmsAdmin.getQueues(AppPro.EMS_TSK_PREFIX.getValue().concat("*"));
//        for(QueueInfo queueInfo1 : queueInfo){
//
//            System.out.println(queueInfo1 == null);
//            System.out.println(queueInfo1.getName());
//
//            System.out.println(tibjmsAdmin.getQueue(queueInfo1.getName()).getReceiverCount());
//        }
//
//        System.out.println("GetQueue");
//        QueueInfo queueInfo1 = tibjmsAdmin.getQueue("F1.EEP.MGR.TSK.1");
//        System.out.println(queueInfo1.getName());

    }

}