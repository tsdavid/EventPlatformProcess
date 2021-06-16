package com.dk.platform.eventTasker.subProcess;

import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.Consumer;
import com.dk.platform.eventTasker.util.MemoryStorage;
import com.dk.platform.eventTasker.util.TaskerUtil;
import com.dk.platform.eventTasker.vo.QueueVO;
import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.admin.TibjmsAdminException;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.jms.*;
import java.sql.Timestamp;

@NoArgsConstructor
public class WorkQueueReceiverSubProcess implements Runnable, Consumer {

    private Connection connection;
    private Session session;
    private MessageConsumer msgConsumer;
    private Destination destination;
    private int ackMode;
    private boolean active = false;

    private MemoryStorage memoryStorage = MemoryStorage.getInstance();
    private TaskerUtil taskerUtil = null;
    private EmsUtil emsUtil = null;

    private int ReceiveTimeOut = 0;
    private String MyNAME = null;
    private boolean rebalanceOrder = false;

    @Builder
    public WorkQueueReceiverSubProcess(String name, int ackMode) throws JMSException {

        this.setUpInstance();

        this.MyNAME = name;

        // Put Work Queue in Map.
        this.updateQueueMap(name);

        this.ackMode = ackMode;
        this.connection = this.emsUtil.getEmsConnection();
        this.session = this.connection.createSession(ackMode);

//        destination = (isTopic) ? session.createTopic(name) : session.createQueue(name);
        destination = session.createQueue(name);
        msgConsumer = session.createConsumer(destination);

        this.ReceiveTimeOut = Integer.parseInt(AppPro.TSK_REC_TIM_OUT.getValue()) * 1000;

    }

    public WorkQueueReceiverSubProcess(String name, int ackMode, EmsUtil emsUtil) throws JMSException {

        this.setUpInstance();

        this.MyNAME = name;

        // Put Work Queue in Map.
        this.updateQueueMap(name);

        this.ackMode = ackMode;
        this.connection = emsUtil.getEmsConnection();
        this.session = this.connection.createSession(ackMode);

//        destination = (isTopic) ? session.createTopic(name) : session.createQueue(name);
        destination = session.createQueue(name);
        msgConsumer = session.createConsumer(destination);

        this.ReceiveTimeOut = Integer.parseInt(AppPro.TSK_REC_TIM_OUT.getValue()) * 1000;

    }


    private void setUpInstance() {
        if(memoryStorage == null){
            memoryStorage = MemoryStorage.getInstance();
        }
        this.taskerUtil = memoryStorage.getTaskerUtil();
        this.emsUtil = memoryStorage.getEmsUtil();
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
        if(!memoryStorage.getQueueMap().containsKey(WorkQueueName)){
            memoryStorage.getQueueMap().put(WorkQueueName, queueVO);
        }

    }

    @Override
    public void setActive(){
        active = true;
    }

    @Override
    public void setDeActive() {
        active = false;

    }

    /**
     * Set for Re-balance Case.
     */
    public void setRebalanceOrder() {
        rebalanceOrder = true;
        // Stop Receving Other Event.
        this.setDeActive();
    }


    @Override
    public void closeAllResources() {

        // Remove Queue
        if(MyNAME != null && memoryStorage.getQueueMap().containsKey(MyNAME)){
            memoryStorage.getQueueMap().remove(MyNAME);
        }

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

        // check Re-balance
        if(rebalanceOrder) { this.setRebalanceWorkProcess();}

        while(active){
//            System.out.println("Runnalbe Run While Start : ");


            TextMessage message = null;
            try {

                message = (TextMessage) msgConsumer.receive(ReceiveTimeOut);

            } catch (JMSException e) {
                e.printStackTrace();

            }catch (Exception e){
                System.out.println("Timeout?");
                e.printStackTrace();
            }


            // Get Message Body
            // TODO TimeOut Logic ==> TimeOut -> NullPointerException?
            try {
                String msgBody = message.getText();
                System.out.println(msgBody);

            } catch (NullPointerException e) {

                System.out.println("Timeout?22222");
                this.setCompleteWorkProcess(this.MyNAME);

            }catch (Exception e){

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
     *
     */
    private void setRebalanceWorkProcess(){

        // check rebalance order is true? and active is false.
        if(rebalanceOrder && !active){
            // send to Manager to return work queue.
            taskerUtil.sendRebalanceReturnMessage(this.MyNAME);

            // Close EMS Resource
            this.closeAllResources();
        }

    }

    private void setCompleteWorkProcess(String workQueueName){

        // 0. Send Manager to Complete Queue
        taskerUtil.sendMessageToManager(false, false, true, workQueueName);

        // 1. Set De-Active
        this.setDeActive();

        // 2. Close EMS Resource
        this.closeAllResources();

    }

    public static void main(String[] args) throws JMSException {

        EmsUtil emsUtil = null;
        try {
            emsUtil = new EmsUtil(AppPro.EMS_URL.getValue(), AppPro.EMS_USR.getValue(), AppPro.EMS_PWD.getValue());
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }

        WorkQueueReceiverSubProcess subProcess = new WorkQueueReceiverSubProcess("testQueue", Session.AUTO_ACKNOWLEDGE, emsUtil);
        subProcess.setActive();
        subProcess.run();

    }
}
