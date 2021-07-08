package com.dk.platform.eventTasker.process;

import com.dk.platform.Process;
import com.dk.platform.Receiver;
import com.dk.platform.common.EppConf;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.Consumer;
import com.dk.platform.eventManager.process.NewQueueReceiverProcess;
import com.dk.platform.eventTasker.subProcess.WorkQueueReceiverSubProcess;
import com.dk.platform.eventTasker.util.MemoryStorage;
import com.dk.platform.eventTasker.util.TaskerUtil;
import com.dk.platform.eventTasker.vo.QueueVO;
import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.admin.TibjmsAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Map;

public class ReceiverProcess implements Runnable, Consumer, Process, Receiver {

    /*
     **************************************  Logger ******************************************
     ****************************************************************************************/

    private static final Logger logger = LoggerFactory.getLogger(ReceiverProcess.class);



    /*
     ***********************************  Variables ******************************************
     ****************************************************************************************/

    private Connection connection;

    private Session session;

    private MessageConsumer msgConsumer;

    private Destination destination;

    private int ackMode;

    private String destName;

    private boolean isTopic;

    private boolean active = true;

    private MemoryStorage memoryStorage;

    private TaskerUtil taskerUtil;

    private EmsUtil emsUtil;

    private EppConf eppConf;

    private String ThreadName;

    private String MYNAME;


    /*
     ********************************  Message Properties ************************************
     ****************************************************************************************/

    /* PROP */
//    private final String MSG_TYPE_PROP = AppPro.MSG_TYPE.getValue();
    private final String MSG_TYPE_PROP = MemoryStorage.getInstance().getEppConf().message.getMessage_TypeVal();

    /* VAL */
//    private final String MNG_RBL_CNT_VAL = AppPro.MNG_RBL_CNT_TSK_VAL.getValue();
    private final String MNG_RBL_CNT_VAL = MemoryStorage.getInstance().getEppConf().message.getMNG_RBL_Count_To_TaskerVal();

//    private final String MNG_ASSIGN_TSK_VAL = AppPro.MNG_ASG_WRK_VAL.getValue();
    private final String MNG_ASSIGN_TSK_VAL = MemoryStorage.getInstance().getEppConf().message.getMNG_Assign_WRK_To_TaskerVal();


    /*
     ***********************************  Constructor ****************************************
     ****************************************************************************************/

    /**
     *
     * @param destName          :           EMS Destination. Receive Message From
     */
    public ReceiverProcess(String destName){
        this(destName, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     *
     * * @param destName          :           EMS Destination. Receive Message From
     * @param ackMode       :       Ems Acknowledge, Default = Auto Acknowledge {@link Session}
     */
    public ReceiverProcess(String destName, int ackMode) {
        this(destName, ackMode, false);
    }


    /**
     *
     * @param destName      :       Ems destination Name. Receive Message From.
     * @param ackMode       :       Ems Acknowledge, Default = Auto Acknowledge {@link Session}
     * @param isTopic       :       Ems Destination Type. Default = false
     */
    public ReceiverProcess(String destName, int ackMode, boolean isTopic) {

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
        ThreadName = this.setUpThreadName(NewQueueReceiverProcess.class.getSimpleName());
        Thread thread = Thread.currentThread();
        String orginName = thread.getName();
        thread.setName(ThreadName);
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

        this.MYNAME = this.memoryStorage.getPROCESS_NAME();

        // EMS Util
        if(this.memoryStorage.getEmsUtil() == null) {
            this.emsUtil = new EmsUtil();
            this.memoryStorage.setEmsUtil(this.emsUtil);
        }
        this.emsUtil = this.memoryStorage.getEmsUtil();

        // Tasker Util.
        if(this.memoryStorage.getTaskerUtil() == null) this.taskerUtil = new TaskerUtil();
        this.taskerUtil = this.memoryStorage.getTaskerUtil();


        // EppConf
        this.eppConf = memoryStorage.getEppConf();

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
//        if(fullPackName.contains(AppPro.PACKAGE_TSK.getValue())) packageName = AppPro.PROCESS_TSK.getValue();
        if(fullPackName.contains(eppConf.process.getPackageTSKVal())) packageName = eppConf.process.getProcessTSKVal();
        // Manager Case.
//        if(fullPackName.contains(AppPro.PACKAGE_MNG.getValue())) packageName = AppPro.PROCESS_MNG.getValue();
        if(fullPackName.contains(eppConf.process.getPackageMNGVal())) packageName = eppConf.process.getProcessMNGVal();
        // Handler Case.
//        if(fullPackName.contains(AppPro.PACKAGE_HND.getValue())) packageName = AppPro.PROCESS_HND.getValue();
        if(fullPackName.contains(eppConf.process.getPackageHNDVal())) packageName = eppConf.process.getProcessHNDVal();

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
//            this.connection = new EmsUtil(AppPro.EMS_URL.getValue(), AppPro.EMS_USR.getValue(), AppPro.EMS_PWD.getValue()).getEmsConnection();
            this.connection = new EmsUtil(eppConf.ems.getUrlVal(), eppConf.ems.getUsrVal(), eppConf.ems.getPwdVal()).getEmsConnection();
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

            TextMessage message = null;
            try {

                message = (TextMessage) msgConsumer.receive();

                // Do Something
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

        // Parsing Message
        try{
            String header = textMessage.getStringProperty(MSG_TYPE_PROP);
            logger.info("Parsing Message Header : {} and  Body : {}.", header, textMessage.getText());

            // Assign Task Message.
            // Assign New Work Queue Process.
            // TODO TEST REQUIRED.
            if(header.equals(MNG_ASSIGN_TSK_VAL)) {

                String newAssignWrkQ = textMessage.getText();

                // Assignable Check
                // if> queue is already in queue manage map. => error.
                if(!memoryStorage.getQueueMap().containsKey(newAssignWrkQ)){

                    try{
                        //TODO THINK BETTER => Assign Thread Number.
                        int numOfThread = memoryStorage.getQueueMap().size() + 1;
                        this.invokeReceiverSubProcess(newAssignWrkQ, numOfThread);
                        logger.info("Tasker : {} is Start to Receive Work Queue : {}.",this.MYNAME, newAssignWrkQ );

                    }catch (Exception e){
                        logger.error("[{}] Error : {}/{}.","InvokeError", e.getMessage(), e.toString());
                        e.printStackTrace();
                    }
                }else {

                    // TODO Re-Assigned Queue. Error Logic.
                    logger.error("New Work Queue is Re Assigned. Check this Out... queue : {}., print All Queue Map : {}",
                            newAssignWrkQ, memoryStorage.getQueueMap().toString());
                }

            }

            // Re-balance Message
            // TODO Re-Balance Process
            if(header.equals(MNG_RBL_CNT_VAL)) {

                int RebalanceCount = Integer.parseInt(textMessage.getText());
                logger.info("Receive Re-balance Order.  Target Threshold : {}. ", RebalanceCount);

                // do re-balance process.
                this.rebalanceExecuteProcess(RebalanceCount);
            }

        }catch (JMSException e){
            logger.error("[{}] Error : {}/{}.","HandleError", e.getMessage(), e.toString());
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
     * Invoke New Thread.
     * @param assignedWorkQueue         :       Ready to Assign Queue Name.
     * @throws JMSException             :       {@link JMSException}
     */
    private void invokeReceiverSubProcess(String assignedWorkQueue, int num) throws JMSException {

        String name = "WorkReceiverThread-" + num;


        // Invoke WorkQueueReceiverSubProcess
        WorkQueueReceiverSubProcess subProcess = new WorkQueueReceiverSubProcess(name, assignedWorkQueue);
        try {
            subProcess.setUpInstance();
            subProcess.setActive();

        } catch (TibjmsAdminException e) {
            logger.error("Error : {}/{}.", e.getMessage(), e.toString());
            e.printStackTrace();
        }

        // Invoke New Thread.
        Thread thread = new Thread(subProcess);
        thread.start();
        // TODO THINK BETTER ==> Need to Manage theses sub-thread?.

        logger.info("New Sub Receiver is Invoked.. Thread Name : {}. Working Queue : {}. ", name, assignedWorkQueue);
    }


    /**
     *
     * @param cnt           :       Threshold
     */
    private void rebalanceExecuteProcess(int cnt){

        int i = 0;
        for(Map.Entry<String, QueueVO> voEntry : memoryStorage.getQueueMap().entrySet()){
            if(i == cnt) break;
            voEntry.getValue().getSubprocess().setRebalanceOrder();
            i++;
            memoryStorage.getQueueMap().remove(voEntry.getKey());

            logger.info(" Return Work Queue Because of Re-Balance Order : {} th Queue, Target : {}. ", i, cnt);
        }

    }



    /*****************************************************************************************
     *************************************  Main *********************************************
     ****************************************************************************************/

    public static void main(String[] args) throws JMSException {
        new ReceiverProcess("h", Session.AUTO_ACKNOWLEDGE, false).run();

    }
}
