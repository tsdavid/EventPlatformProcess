package com.dk.platform.eventManager.process;

import com.dk.platform.Process;
import com.dk.platform.Receiver;
import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.Consumer;
import com.dk.platform.eventManager.util.ManagerUtil;
import com.dk.platform.eventManager.util.MemoryStorage;
import com.dk.platform.eventManager.vo.TaskerVO;
import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.admin.TibjmsAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.sql.Timestamp;


/**
 * Main Job
 * 1. Receive Message
 */
public class ReceiverProcess implements Runnable, Consumer, Process, Receiver {

    /*****************************************************************************************
     **************************************  Logger ******************************************
     ****************************************************************************************/

    private static final Logger logger = LoggerFactory.getLogger(ReceiverProcess.class);


    /*****************************************************************************************
     ***********************************  Variables ******************************************
     ****************************************************************************************/

    private Connection connection;

    private Session session;

    private MessageConsumer msgConsumer;

    private Destination destination;


    private boolean active = false;

    private MemoryStorage memoryStorage;

    private ManagerUtil managerUtil;

    private EmsUtil emsUtil;

    private boolean isTopic;

    private String destName;

    private int ackMode;

    private String ThreadName;


    /*****************************************************************************************
     ********************************  Message Properties ************************************
     ****************************************************************************************/

    private final String TSK_INIT = AppPro.TSK_INIT_MNG_VAL.getValue();
    private final String TSK_WRK_STATUS = AppPro.TSK_HLTH_MNG_VAL.getValue();
    private final String TSK_COM_WRK = AppPro.TSK_COM_WRK_MNG_VAL.getValue();
    private final String TSK_RET_WRK = AppPro.TSK_RBL_RTN_MNG_VAL.getValue();


    /*
     ***********************************  Constructor ****************************************
     ****************************************************************************************/

    /**
     *
     * @param destName      :       Ems destination Name. Receive Message From.
     */
    public ReceiverProcess(String destName){
        this(destName, Session.AUTO_ACKNOWLEDGE);
    }


    /**
     *
     * @param destName      :       Ems destination Name. Receive Message From.
     * @param ackMode       :       Ems Acknowledge, Default = Auto Acknowledge {@link Session}
     */
    public ReceiverProcess(String destName, int ackMode){
        this(destName, ackMode, false);
    }


    /**
     *
     * @param destName      :       Ems destination Name. Receive Message From.
     * @param ackMode       :       Ems Acknowledge, Default = Auto Acknowledge {@link Session}
     * @param isTopic       :       Ems Destination Type. Default = false
     */
    public ReceiverProcess(String destName, int ackMode, boolean isTopic){

        this.destName = destName;

        this.ackMode = ackMode;

        this.isTopic = isTopic;

    }


    /**
     * Use Instantiate Essential Class.
     * 0. Thread Name
     * 1. MemoryStorage
     * 2. EmsUtil
     * 3. Util
     * 4. (Optional) ProcessName.
     * 5. Create Ems Connection from Receiver Interface.
     */
    @Override
    public void setUpInstance() throws TibjmsAdminException {

        // Set-Up Thread Name.
        this.ThreadName = this.setUpThreadName(NewQueueReceiverProcess.class.getSimpleName());
        Thread thread = Thread.currentThread();
        String orginName = thread.getName();
        thread.setName(this.ThreadName);
        String ChangedName = thread.getName();

        if(ChangedName.equals(this.ThreadName)){
            logger.info("Thread Name Has been changed.. Original : {}.  New : {}..",
                    orginName, ChangedName);
        }
        // TODO THINK BETTER ==> What if Error while Change Name?
//        else {
//        }

        // Set-Up MemoryStorage.
        this.memoryStorage = MemoryStorage.getInstance();

        // EMS Util
        if(this.memoryStorage.getEmsUtil() == null) {
            this.emsUtil = new EmsUtil();
            this.memoryStorage.setEmsUtil(this.emsUtil);
        }
        this.emsUtil = this.memoryStorage.getEmsUtil();

        // Manager Util.
        if(this.memoryStorage.getManagerUtil() == null) this.memoryStorage.setManagerUtil(new ManagerUtil(this.emsUtil));
        this.managerUtil = this.memoryStorage.getManagerUtil();

        // Create EMS Connection.
        try {
            this.createEmsConnection();

        } catch (JMSException e) {
            logger.error(" Error : {}/{}.", e.getMessage(), e.toString());
            e.printStackTrace();
        }

    }


    /**
     * Set Up Custom Thread Name.
     * @param clazzName         :           This Class Name.
     * @return                  :           formatted Thread Name( Package Name - Class Name - Thread)
     */
    // TODO THINK BETTER ==> Make Common Util?
    public String setUpThreadName(String clazzName) {

        String fullPackName = this.getClass().getPackage().getName();
        String packageName = null;
        // Tasker Case.
        if(fullPackName.contains(AppPro.PACKAGE_TSK.getValue())) packageName = AppPro.PROCESS_TSK.getValue();
        // Manager Case.
        if(fullPackName.contains(AppPro.PACKAGE_MNG.getValue())) packageName = AppPro.PROCESS_MNG.getValue();
        // Handler Case.
        if(fullPackName.contains(AppPro.PACKAGE_HND.getValue())) packageName = AppPro.PROCESS_HND.getValue();

        return packageName + "-" + clazzName+"or";
    }


    /**
     * Create Connection with EMS.
     * Receiver has to make connection with EMS, to Receive Some staff from there.
     *
     * @throws TibjmsAdminException :       Exception when Initiate {@link EmsUtil}
     * @throws JMSException         :       Exception when Create Session {@link Session}
     */
    @Override
    public void createEmsConnection() throws TibjmsAdminException, JMSException {

        if(this.emsUtil == null){
            this.connection = new EmsUtil(AppPro.EMS_URL.getValue(), AppPro.EMS_USR.getValue(), AppPro.EMS_PWD.getValue()).getEmsConnection();
        }
        this.connection = this.emsUtil.getEmsConnection();
        this.session = this.connection.createSession(ackMode);

        destination = (this.isTopic) ? session.createTopic(this.destName) : session.createQueue(this.destName);
        msgConsumer = session.createConsumer(destination);


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


        logger.info(" execute Method, Run the While Loop, Check Active : {}", active);


        while(active){


            Message message = null;
            try {
                message =  msgConsumer.receive();

                // Do Something.
                this.handleMessage(message);

            } catch (JMSException e) {
                logger.error("[{}] Error : {}/{}.","ParsingMsg", e.getMessage(), e.toString());
                e.printStackTrace();
            }



            // Ack
            if (ackMode == Session.CLIENT_ACKNOWLEDGE ||
                    ackMode == Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE ||
                    ackMode == Tibjms.EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE)
            {
                try {

                    // TODO THINKK BETTER ==> Prevent Quick Net Work Disable Case.
                    message.acknowledge();

                } catch (JMSException e) {
                    logger.error("[{}] Error : {}/{}.","AckMsg", e.getMessage(), e.toString());
                    e.printStackTrace();


                } catch (Exception e) {
                    logger.error("[{}] Error : {}/{}.","AckMsg", e.getMessage(), e.toString());
                    e.printStackTrace();
                }
            }
        }
        logger.info(" execute Method, Break the While Loop");

    }


    /**
     * Message Handle Method.
     * When Receiver Receive Message, run handleMessage method.
     *
     * @param message :           Message.
     */
    @Override
    public void handleMessage(Message message) {

        TextMessage textMessage = (TextMessage) message;

        // Parsing Message.
        try {
            String header = textMessage.getStringProperty(AppPro.MSG_TYPE.getValue());
            System.out.println(header + " And Message Contents : " + textMessage.getText());

            // New Tasker Arrive Case.
            if(header.equals(TSK_INIT)) this.taskerInitProcess(textMessage.getText());

            // Receive Tasker's Health Check Message Case.
            else if(header.equals(TSK_WRK_STATUS)){
                String messageFrom = textMessage.getStringProperty(AppPro.TSK_HLTH_FROM_PROP.getValue());
                this.taskerReportProcess(messageFrom, textMessage.getText());
            }
            // Receive Tasker's Complete Message Case.
            else if(header.equals(TSK_COM_WRK)) {
                String messageFrom = textMessage.getStringProperty(AppPro.TSK_HLTH_FROM_PROP.getValue());
                this.taskerCompletedWork(messageFrom, textMessage.getText());
            }
            // Receive Tasker's returned work queue because of Re-balance case.
            else if(header.equals(TSK_RET_WRK)) {

                this.taskerReturnedWrok(textMessage.getText());
            }

        } catch (JMSException e) {
            logger.error("[{}] Error : {}/{}.","handleMessage", e.getMessage(), e.toString());
            e.printStackTrace();
        }
    }


    @Override
    public void setActive(){
        logger.info(" Set Active Method, This will Start in sec.");
        active = true;
    }


    @Override
    public void setDeActive() {
        logger.info(" Set DeActive Method, This will be Stopped in sec.");
        active = false;

    }

    @Override
    public void closeAllResources() {

        if(session != null){
            try {
                session.close();
            } catch (JMSException e) {
                logger.error("[{}] Error : {}/{}.","CloseSession", e.getMessage(), e.toString());
                e.printStackTrace();
            }
        }

        if(msgConsumer != null){
            try {
                msgConsumer.close();
            } catch (JMSException e) {
                logger.error("[{}] Error : {}/{}.","CloseConsumer", e.getMessage(), e.toString());
                e.printStackTrace();
            }
        }

    }


    /*
     ***********************************  Case Logic *****************************************
     *****************************************************************************************/



    /**
     * 1. Set-Up TaskerVO in Map.
     * 2. Check Re-Balance.
     * @param taskerName            :           New Running Tasker Name
     */
    private void taskerInitProcess(String taskerName){

        // Put TaskerVO in Tasker Map.
        TaskerVO taskerVO = TaskerVO.builder()
                                    .created_Time(new Timestamp(System.currentTimeMillis()))
                                    .updated_Time(new Timestamp(System.currentTimeMillis()))
                                    .taskerName(taskerName)
                                    .cnt(0).workQueueList("")
                                    .build();
        memoryStorage.getTasker_Mng_Map().put(taskerName, taskerVO);
        logger.info(" New Tasker Set-Up name : {}.  size of tasker map : {}", taskerName, memoryStorage.getTasker_Mng_Map().size());
        logger.info(" New Tasker Set-Up name : {}. tasker map : {}", taskerName, memoryStorage.getTasker_Mng_Map().toString());

        // check Re-Balance
        this.checkRebalancable(taskerName);

        // Check Whether WRK Q is int TMP WRK Q.
        if(memoryStorage.getTmp_WRK_Queue().size() > 0){
            this.managerUtil.executeTmpQueue(taskerName);
        }

    }


    /**
     *
     * @param newTaskerName            :           New Running Tasker Name
     */
    private void checkRebalancable(String newTaskerName){


        // Check Re-Balance Condition.
        if(managerUtil.isRebalanceCase(newTaskerName)){
            // Re-Balance Case. => Send Re-balance Message to O-Tasker to get Returned WRK Q.  Target : tobeThreshold
            logger.info("Tasker Name : {}.  It is a Re-Balance Case. Do Re-balance Task.", newTaskerName);
            try {
                // execute Re-balance task.
                managerUtil.doRebalanceCase(newTaskerName);
            } catch (JMSException e) {
                logger.error("[{}] Error : {}/{}.","FailRe-balance", e.getMessage(), e.toString());
                e.printStackTrace();
            }

        }

    }


    /**
     *
     * @param fromDestination       :       Tasker Name who send this Report.
     * @param workQueues            :       Queue List owned by reporter(tasker).
     */
    private void taskerReportProcess(String fromDestination, String workQueues){

        logger.info(" Receive Message From : {}, Work Queue : {}", fromDestination, workQueues);
        int cnt = workQueues.split(",").length -1;
//        this.managerUtil.updateTaskerHealthCheck(fromDestination, workQueues, cnt);

        try{
            this.managerUtil.updateTaskerHealthCheck(fromDestination, workQueues, cnt);
        }catch (Exception e){
            logger.error("Error : {}/{}", e.getMessage(), e.toString());
            e.printStackTrace();
        }

    }


    /**
     *
     * @param fromDestination       :       Tasker Name who send this Report.
     * @param completeQueue         :       Completed Queue Name.
     */
    // TODO COMPLETE WORK QUEUE LOGIC
    private void taskerCompletedWork(String fromDestination, String completeQueue){

        logger.info(" Tasker : {}, Complete Work Queue : {}", fromDestination, completeQueue);
    }

    /**
     *
     * @param returedWorkQueue      :       Returned Queue Name by Re-Balance Order.
     */
    private void taskerReturnedWrok(String returedWorkQueue){

        String newAssignedTasker = managerUtil.findIdleTasker();
        managerUtil.assignWrkQtoTSK(returedWorkQueue, newAssignedTasker);

    }


    /*****************************************************************************************
     *************************************  Main *********************************************
     ****************************************************************************************/

    public static void main(String[] args) throws JMSException {
        String str = "F1.EEP.WRK.TEST2,F1.EEP.WRK.TEST1,F1.EEP.WRK.TEST4,F1.EEP.WRK.TEST3,";
        System.out.println(str.split(",").length -1);
    }
}
