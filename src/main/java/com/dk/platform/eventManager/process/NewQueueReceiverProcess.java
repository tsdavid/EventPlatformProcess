package com.dk.platform.eventManager.process;

// Consumer System topic to get info about new created destination.
// topic receiver ==> $sys.monitor.queue.create


import com.dk.platform.Process;
import com.dk.platform.Receiver;
import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.Consumer;
import com.dk.platform.eventManager.util.ManagerUtil;
import com.dk.platform.eventManager.util.MemoryStorage;
import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.admin.TibjmsAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

/**
 * Main Job
 * 1. Receive New Queue
 * 2. Send to Next Class.
 * Receive New Queue -> Assign or Save in Set
 */
public class NewQueueReceiverProcess implements Runnable, Consumer, Process, Receiver {


    /*****************************************************************************************
     **************************************  Logger ******************************************
     ****************************************************************************************/

    private static final Logger logger = LoggerFactory.getLogger(NewQueueReceiverProcess.class);


    /*****************************************************************************************
     ***********************************  Variables ******************************************
     ****************************************************************************************/

    private final String PROPERTY_NAME = AppPro.NEW_QUEUE_CREATE_VAL.getValue();

    private Session session;

    private MessageConsumer msgConsumer;

    private int ackMode;

    private boolean active = false;

    private EmsUtil emsUtil;

    private ManagerUtil managerUtil;

    private String topicName;

    private boolean  isTopic;


    /*
     ***********************************  Constructor ****************************************
     ****************************************************************************************/

    /**
     *
     * @param name      :       Topic Name.
     */
    public NewQueueReceiverProcess(String name) {

        this(name, Session.AUTO_ACKNOWLEDGE);
    }


    /**
     *
     * @param name      :       Topic Name.
     * @param ackMode   :       Ems Acknowledge Mode, Default = Auto Acknowledge.
     */
    public NewQueueReceiverProcess(String name, int ackMode){
        this(name, ackMode, true);
    }


    /**
     *
     * @param name      :       Topic Name.
     * @param ackMode   :       Ems Acknowledge Mode, Default = Auto Acknowledge.
     * @param isTopic   :       Destination Type Topic or Queue, Default = Topic.
     */
    public NewQueueReceiverProcess(String name, int ackMode, boolean isTopic) {

        this.topicName = name;

        this.ackMode = ackMode;

        this.isTopic = isTopic;

    }


    /**
     * Use Instantiate Essential Class.
     * Included 1. setUPThreadName 2. createEmsConnection.
     * 1. MemoryStorage
     * 2. EmsUtil
     * 3. Util
     * 4. (Optional) ProcessName.
     */
    @Override
    public void setUpInstance() throws TibjmsAdminException{

        // Set-Up Thread Name.
        String threadName = this.setUpThreadName(NewQueueReceiverProcess.class.getSimpleName());
        Thread thread = Thread.currentThread();
        String orginName = thread.getName();
        thread.setName(threadName);
        String ChangedName = thread.getName();

        if(ChangedName.equals(threadName)){
            logger.info("Thread Name Has been changed.. Original : {}.  New : {}..",
                        orginName, ChangedName);
        }
//        else {
//            // TODO THINK BETTER ==> What if Error while Change Name?
//        }


        // MemoryStorage.
        MemoryStorage memoryStorage = MemoryStorage.getInstance();

        // EMS Util
        if(memoryStorage.getEmsUtil() == null) {
            this.emsUtil = new EmsUtil();
            memoryStorage.setEmsUtil(this.emsUtil);
        }
        this.emsUtil = memoryStorage.getEmsUtil();

        // Manager Util.
        if(memoryStorage.getManagerUtil() == null) this.managerUtil = new ManagerUtil();
        this.managerUtil = memoryStorage.getManagerUtil();

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

        Connection connection;
        if(this.emsUtil == null){
            connection = new EmsUtil(AppPro.EMS_URL.getValue(), AppPro.EMS_USR.getValue(), AppPro.EMS_PWD.getValue()).getEmsConnection();
        }
        connection = this.emsUtil.getEmsConnection();
        this.session = connection.createSession(ackMode);

        Destination destination = (this.isTopic) ? session.createTopic(this.topicName) : session.createQueue(this.topicName);
        msgConsumer = session.createConsumer(destination);

    }


    /*
     ***********************************  Process Logic **************************************
     *****************************************************************************************/

    /**
     * Button By Runnable.  Automation Play Button.
     */
    @Override
    public void run() {

        logger.info(" Run Method, Run Execute Method");

        this.execute();

    }


    /**
     * arbitrary play button.
     *
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
                message = msgConsumer.receive();

                this.handleMessage(message);


            } catch (JMSException e) {
                logger.info("[{}] Error : {}/{}.","ReceiveProcess", e.getMessage(), e.toString());
                logger.error("[{}] Error : {}/{}.","ReceiveProcess", e.getMessage(), e.toString());
                e.printStackTrace();
            }


            // Ack
            if (ackMode == Session.CLIENT_ACKNOWLEDGE ||
                    ackMode == Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE ||
                    ackMode == Tibjms.EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE)
            {
                try {
                    assert message != null;
                    message.acknowledge();
                } catch (JMSException e) {
                    logger.error("[{}] Error : {}/{}.","ReceiveProcess", e.getMessage(), e.toString());
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

        logger.debug("Received Message : {}", message.toString());


        // Get Queue Name.
        String newQueue = null;
        try{
            newQueue = message.getStringProperty(PROPERTY_NAME);
            logger.info("New Queue Created and It's Name is : {}.  Need to Check WORK QUEUE.", newQueue);

        }catch (Exception e){
            logger.error("[{}] Error : {}/{}.","AssignWorkQueue", e.getMessage(), e.toString());
            e.printStackTrace();
        }


        // Check WRK Queue Prefix.
        if(newQueue != null && newQueue.startsWith(AppPro.EMS_WRK_PREFIX.getValue())){

            logger.info("New Queue : {}. No one Take it.", newQueue);
            String assignableTasker = managerUtil.findIdleTasker();
            managerUtil.assignWrkQtoTSK(newQueue, assignableTasker);

            logger.info(" New Work Queue : {}  has been assign to Tasker : {}", newQueue, assignableTasker);
            // TODO THINK BETTER ==> Check Receiver logic Wrong....

//            try {
//                int receiverCount = emsUtil.getTibjmsAdmin().getQueue(newQueue).getReceiverCount();
//                // Check Receiver Count is 0 ==> De-Active Queue
//                if(receiverCount == 0){
//
//                    String assignableTasker = managerUtil.findIdleTasker();
//                    managerUtil.assignWrkQtoTSK(newQueue, assignableTasker);
//
//                    logger.info(" New Work Queue : {}  has been assign to Tasker : {}", newQueue, assignableTasker);
//                }
//            } catch (TibjmsAdminException e) {
//                logger.error("[{}] Error : {}/{}.","getReceiverCount", e.getMessage(), e.toString());
//                e.printStackTrace();
//            }

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


    /*****************************************************************************************
     *************************************  Main *********************************************
     ****************************************************************************************/

    public static void main(String[] args) throws JMSException, ClassNotFoundException, TibjmsAdminException {
        String str = "NewQueueReceiverProcess";
        NewQueueReceiverProcess newQueueReceiverProcess = new NewQueueReceiverProcess("$sys.monitor.queue.create");
        newQueueReceiverProcess.setUpInstance();
        newQueueReceiverProcess.setActive();
        newQueueReceiverProcess.run();

    }


}
