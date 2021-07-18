package com.dk.platform.eventTasker.subProcess;

import com.dk.platform.Process;
import com.dk.platform.Receiver;
import com.dk.platform.common.EppConf;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.Consumer;
import com.dk.platform.eventTasker.util.MemoryStorage;
import com.dk.platform.eventTasker.util.TaskerUtil;
import com.dk.platform.eventTasker.vo.QueueVO;
import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.admin.TibjmsAdminException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.sql.Timestamp;

@Slf4j
public class WorkQueueReceiverSubProcess implements Runnable, Consumer, Process, Receiver {

    /*
     **************************************  Logger ******************************************
     ****************************************************************************************/

    private static final Logger logger = LoggerFactory.getLogger(WorkQueueReceiverSubProcess.class);


    /*
     ***********************************  Variables ******************************************
     ****************************************************************************************/

    private Connection connection;

    private Session session;

    private MessageConsumer msgConsumer;

    private Destination destination;

    private String destName;

    private int ackMode;

    private boolean active = false;

    private MemoryStorage memoryStorage;

    private TaskerUtil taskerUtil;

    private EmsUtil emsUtil;

    private EppConf eppConf;

    private int ReceiveTimeOut;

    private boolean rebalanceOrder = false;

    private String ThreadName;


    /*
     ***********************************  Constructor ****************************************
     ****************************************************************************************/

    /**
     * @param ThreadName        :           Thread Name need to Set
     * @param destName          :           EMS Destination. Receive Message From
     */
    public WorkQueueReceiverSubProcess(String ThreadName, String destName){
        this(ThreadName, destName, Session.AUTO_ACKNOWLEDGE);
    }


    /**
     *
     * @param ThreadName        :           Thread Name need to Set
     * @param destName          :           EMS Destination. Receive Message From
     * @param ackMode       :       Ems Acknowledge, Default = Auto Acknowledge {@link Session}
     */
    public WorkQueueReceiverSubProcess(String ThreadName, String destName, int ackMode) {

        this.ThreadName = ThreadName;

        this.destName = destName;

        this.ackMode = ackMode;

    }

    /**
     * For Test
     * @param destName          :       EMS Destination Name
     * @param ackMode           :       EMS Acknowledge Mode.
     * @param emsUtil           :       EMS Util...
     * @throws JMSException     :       {@link JMSException}
     */
    public WorkQueueReceiverSubProcess(String ThreadName, String destName, int ackMode, EmsUtil emsUtil)  {


        this.ThreadName = ThreadName;
        logger.info("Thread Name will be assigned by :{},  And the Result Threa Name : {}", ThreadName, this.ThreadName);

        this.destName = destName;


        this.ackMode = ackMode;
        this.connection = emsUtil.getEmsConnection();

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

        logger.info(this.ThreadName);

        // Set-Up Thread Name.
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
        }
        this.emsUtil = this.memoryStorage.getEmsUtil();

        // Manager Util.
        if(this.memoryStorage.getTaskerUtil() == null) this.taskerUtil = new TaskerUtil(this.emsUtil);
        this.taskerUtil = this.memoryStorage.getTaskerUtil();

        // eppConf
        this.eppConf = memoryStorage.getEppConf();
        this.ReceiveTimeOut = eppConf.process.getSub_TSK_REC_Time_OutVal() * 1000;

        // Create EMS Connection.
        try {
            this.createEmsConnection();

        } catch (JMSException e) {
            logger.error(" Error : {}/{}.", e.getMessage(), e.toString());
            e.printStackTrace();
        }

        // Put Work Queue in Map.
        this.updateQueueMap(destName);

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

//        destination = (this.isTopic) ? session.createTopic(this.destName) : session.createQueue(this.destName);
        destination = session.createQueue(this.destName);
        msgConsumer = session.createConsumer(destination);

    }


    // TODO TEST REQUIRED
    private void updateQueueMap(String WorkQueueName) {

        QueueVO queueVO = QueueVO.builder()
                .queueName(WorkQueueName)
                .created_Time(new Timestamp(System.currentTimeMillis()))
                .status("Working")
                .subProcess(this)
                .build();
        // Put VO in Map.
        try{
            if(!memoryStorage.getQueueMap().containsKey(WorkQueueName)){
                memoryStorage.getQueueMap().put(WorkQueueName, queueVO);
            }
        }catch (Exception e){
            logger.error("[{}] Error : {}/{}.","FailUpdate", e.getMessage(), e.toString());
            e.printStackTrace();
        }

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

        // check Re-balance
        if(rebalanceOrder) { this.setRebalanceWorkProcess();}

        while(active){


            Message message = null;
            try {

                message = msgConsumer.receive(ReceiveTimeOut);

                // Do Something.
                this.handleMessage(message);

            } catch (JMSException e) {
                e.printStackTrace();

            }catch (Exception e){
                System.out.println("Timeout?");
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
        System.out.println("Runnalbe Run Break While");

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

        // Get Message Body
        // TODO TimeOut Logic ==> TimeOut -> NullPointerException?
        try {

            // Handling Work Queue Message.
            String msgBody = textMessage.getText();
            log.debug(msgBody);

        } catch (NullPointerException e) {

            System.out.println("Timeout?22222");
            this.setCompleteWorkProcess(this.destName);

        }catch (Exception e){

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
     * Set for Re-balance Case.
     */
    public void setRebalanceOrder() {
        rebalanceOrder = true;
        // Stop Receving Other Event.
        this.setDeActive();
    }


    /**
     *
     */
    private void setRebalanceWorkProcess(){

        // check rebalance order is true? and active is false.
        if(rebalanceOrder && !active){
            // send to Manager to return work queue.
            // TODO Deprecated Method.
            taskerUtil.sendRebalanceReturnMessage(this.destName);

            // Close EMS Resource
            this.closeAllResources();
        }

    }


    private void setCompleteWorkProcess(String workQueueName){

        logger.info("Work Queue is Completed.  Queue Name : {}.", workQueueName);

        // 0. Send Manager to Complete Queue
        taskerUtil.sendMessageToManager(false, false, true, workQueueName);

        // 1. Set De-Active
        this.setDeActive();

        // 2. Close EMS Resource
        this.closeAllResources();

    }


    /*****************************************************************************************
     *************************************  Main *********************************************
     ****************************************************************************************/

    public static void main(String[] args) throws JMSException {

//        MemoryStorage memoryStorage = MemoryStorage.getInstance();
//
//
//        EmsUtil emsUtil = null;
//        try {
//            emsUtil = new EmsUtil(AppPro.EMS_URL.getValue(), AppPro.EMS_USR.getValue(), AppPro.EMS_PWD.getValue());
//            memoryStorage.setEmsUtil(emsUtil);
//            memoryStorage.setTaskerUtil(new TaskerUtil(emsUtil));
//        } catch (TibjmsAdminException e) {
//            e.printStackTrace();
//        }
//
//        WorkQueueReceiverSubProcess subProcess = new WorkQueueReceiverSubProcess("Customemain","testQueue", Session.AUTO_ACKNOWLEDGE, emsUtil);
//        try {
//            subProcess.setUpInstance();
//        } catch (TibjmsAdminException e) {
//            e.printStackTrace();
//        }
//        subProcess.setActive();
//        subProcess.run();
//
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        subProcess.setCompleteWorkProcess("testQueue");

    }

}
