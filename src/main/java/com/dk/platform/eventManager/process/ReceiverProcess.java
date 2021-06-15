package com.dk.platform.eventManager.process;

import com.dk.platform.Process;
import com.dk.platform.ems.ConnConf;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.Consumer;
import com.dk.platform.eventManager.util.ManagerUtil;
import com.dk.platform.eventManager.util.MemoryStorage;
import com.dk.platform.eventManager.vo.TaskerVO;
import com.tibco.tibjms.Tibjms;
import com.tibco.tibjms.admin.TibjmsAdminException;

import javax.jms.*;
import java.sql.Timestamp;
import java.util.HashSet;

public class ReceiverProcess implements Runnable, Consumer, Process {

    private Connection connection;
    private Session session;
    private MessageConsumer msgConsumer;
    private Destination destination;
    private int ackMode;
    private boolean active = false;

    private MemoryStorage memoryStorage;
    private ManagerUtil managerUtil;
    private EmsUtil emsUtil;

    // Properties
    private final String TSK_INIT = ConnConf.TSK_INIT_MNG_VAL.getValue();
    private final String TSK_WRK_STATUS = ConnConf.TSK_HLTH_MNG_VAL.getValue();
    private final String TSK_RET_WRK = ConnConf.TSK_RET_WRK_MNG_VAL.getValue();
    private final String TSK_COM_WRK = ConnConf.TSK_COM_WRK_MNG_VAL.getValue();

    public ReceiverProcess(String name, int ackMode, boolean isTopic) throws JMSException {

        this.ackMode = ackMode;
        try {
            this.connection = new EmsUtil(ConnConf.EMS_URL.getValue(), ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue()).getEmsConnection();
            this.session = this.connection.createSession(ackMode);
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
        destination = (isTopic) ? session.createTopic(name) : session.createQueue(name);
        msgConsumer = session.createConsumer(destination);

        // Set-Up MemoryStorage.
        memoryStorage = MemoryStorage.getInstance();
        this.managerUtil = this.memoryStorage.getManagerUtil();
        this.emsUtil = this.memoryStorage.getEmsUtil();

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

            // Parsing Message.
            try {
                String MessageProperty = message.getStringProperty(ConnConf.MSG_TYPE.getValue());
                System.out.println(MessageProperty + " And Message Contents : " + message.getText());

                // if statement
                if(MessageProperty.equals(TSK_INIT)) this.taskerInitProcess(message.getText());
                else if(MessageProperty.equals(TSK_WRK_STATUS)){
                    String messageFrom = message.getStringProperty(ConnConf.TSK_HLTH_FROM_PROP.getValue());
                    this.taskerReportProcess(messageFrom, message.getText());
                }

//                else if(MessageProperty.equals(TSK_RET_WRK)) this.taskerReturnedWrok(message.getText());
//                else if(MessageProperty.equals(TSK_COM_WRK)) this.taskerCompletedWork(message.getText());

            } catch (JMSException e) {
                e.printStackTrace();
            }


            // Do Something.

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

    private void taskerInitProcess(String taskerName){

        // Put TaskerVO in Tasker Map.
        TaskerVO taskerVO = TaskerVO.builder()
                                    .created_Time(new Timestamp(System.currentTimeMillis()))
                                    .updated_Time(new Timestamp(System.currentTimeMillis()))
                                    .taskerName(taskerName)
                                    .build();
        memoryStorage.getTasker_Mng_Map().put(taskerName, taskerVO);
        System.out.println(memoryStorage.getTasker_Mng_Map().toString());

        // Check Whether WRK Q is int TMP WRK Q.
        if(memoryStorage.getTmp_WRK_Queue().size() > 0){
            this.managerUtil.executeTmpQueue(taskerName);
        }


    }

    private void taskerReportProcess(String fromDestination, String workQueues){
        System.out.println("taskerReportProcess" + "  ..MEssage From : . " + fromDestination + " queuessss...L " + workQueues);

        int cnt = workQueues.split(",").length -1;
        this.managerUtil.updateTaskerHealthCheck(fromDestination, workQueues, cnt);

    }


    public static void main(String[] args) throws JMSException {
        String str = "F1.EEP.WRK.TEST2,F1.EEP.WRK.TEST1,F1.EEP.WRK.TEST4,F1.EEP.WRK.TEST3,";
        System.out.println(str.split(",").length -1);
    }
}
