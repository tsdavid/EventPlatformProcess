package com.dk.platform.eventTasker.process;

import com.dk.platform.Process;
import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.Consumer;
import com.dk.platform.eventTasker.subProcess.WorkQueueReceiverSubProcess;
import com.dk.platform.eventTasker.util.MemoryStorage;
import com.dk.platform.eventTasker.util.TaskerUtil;
import com.dk.platform.eventTasker.vo.QueueVO;
import com.tibco.tibjms.Tibjms;

import javax.jms.*;
import java.util.Map;

public class ReceiverProcess implements Runnable, Consumer, Process {

    private Connection connection;
    private Session session;
    private MessageConsumer msgConsumer;
    private Destination destination;
    private int ackMode;
    private boolean active = true;

    private MemoryStorage memoryStorage = MemoryStorage.getInstance();
    private TaskerUtil taskerUtil = null;
    private EmsUtil emsUtil = null;

    /****************************************************************************************/
    /****************************************************************************************/
    /*************************         Message Properties    ********************************/
    /****************************************************************************************/
    /****************************************************************************************/
    /* PROP */
    private final String MSG_TYPE_PROP = AppPro.MSG_TYPE.getValue();

    /* VAL */
    private final String MNG_RBL_CNT_VAL = AppPro.MNG_RBL_CNT_TSK_VAL.getValue();
    private final String MNG_ASSIGN_TSK_VAL = AppPro.MNG_ASG_WRK_VAL.getValue();

    public ReceiverProcess(String name, int ackMode, boolean isTopic) throws JMSException {

        this.taskerUtil = memoryStorage.getTaskerUtil();
        this.emsUtil = memoryStorage.getEmsUtil();

        this.ackMode = ackMode;
        try {
            this.connection = emsUtil.getEmsConnection();
            this.session = this.connection.createSession(ackMode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        destination = (isTopic) ? session.createTopic(name) : session.createQueue(name);
        msgConsumer = session.createConsumer(destination);

    }


    @Override
    public void setActive(){
        active = true;
    }

    @Override
    public void setDeActive() {
        active = false;

    }

    @Override
    public void closeAllResources() {
        if(session != null){
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        if(msgConsumer != null){
            try {
                msgConsumer.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void run() {

//        System.out.println("Runnalbe Run Cnt active status : " + active);


        while(active){
//            System.out.println("Runnalbe Run While Start : ");


            TextMessage message = null;
            try {

                message = (TextMessage) msgConsumer.receive();

            } catch (JMSException e) {
                e.printStackTrace();
            }

            // Parsing Message
            try{
                String MessageProperty = message.getStringProperty(MSG_TYPE_PROP);
                System.out.println(MessageProperty + " And Message Contents : " + message.getText());

                // Assign Task Message.
                // Assign New Work Queue Process.
                // TODO TEST REQUIRED.
                if(MessageProperty.equals(MNG_ASSIGN_TSK_VAL)) {


                    String newAssignWrkQ = message.getText();
                    System.out.println("New Assigned Task Queue : " + newAssignWrkQ);

                    // Assignable Check
                    // if> queue is already in queue manage map. => error.
                    if(!memoryStorage.getQueueMap().containsKey(newAssignWrkQ)){

                        try{
                            this.invokeReceiverSubProcess(newAssignWrkQ);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else {

                        // TODO Re-Assigned Queue. Error Logic.
                        System.out.println("Assigned Work Queue is Re Assigned. Check this Out... taskQueueName : "
                                + newAssignWrkQ + " print queue Map : " + memoryStorage.getQueueMap().toString());
                    }


                }

                // Re-balance Message
                // TODO Re-Balance Process
                if(MessageProperty.equals(MNG_RBL_CNT_VAL)) {

                    int RebalanceCount = Integer.parseInt(message.getText());
                    System.out.println("Rebalance Count : " + RebalanceCount);
                    // do re-balance process.
                    this.rebalanceExecuteProcess(RebalanceCount);
                }




            }catch (JMSException e){
                e.printStackTrace();
            }


            // Ack
            if (ackMode == Session.CLIENT_ACKNOWLEDGE ||
                    ackMode == Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE ||
                    ackMode == Tibjms.EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE)
            {
                try {
                    message.acknowledge();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }


        }
        System.out.println("Runnalbe Run Break While");

    }


    /**
     * Invoke New Thread.
     * @param assignedWorkQueue
     * @throws JMSException
     */
    private void invokeReceiverSubProcess(String assignedWorkQueue) throws JMSException {

        // Invoke WorkQueueReceiverSubProcess
        WorkQueueReceiverSubProcess subProcess = WorkQueueReceiverSubProcess.builder()
                                                                .name(assignedWorkQueue)
                                                                .ackMode(Session.AUTO_ACKNOWLEDGE)
                                                                .build();
        subProcess.setActive();

        // Invoke New Thread.
        Thread thread = new Thread(subProcess);
        thread.start();
    }

    /**
     *
     * @param cnt
     */
    private void rebalanceExecuteProcess(int cnt){

        int i = 0;
        for(Map.Entry<String, QueueVO> voEntry : memoryStorage.getQueueMap().entrySet()){
            if(i == cnt) break;
            voEntry.getValue().getSubprocess().setRebalanceOrder();
            i++;
            memoryStorage.getQueueMap().remove(voEntry.getKey());
        }

    }


    public static void main(String[] args) throws JMSException {
        new ReceiverProcess("h", Session.AUTO_ACKNOWLEDGE, false).run();

    }

    @Override
    public void setUpInstance() {

    }
}
