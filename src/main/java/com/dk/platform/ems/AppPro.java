package com.dk.platform.ems;

import lombok.Getter;

@Getter
public enum AppPro {

    PROCESS_TSK("TSK"),
    PACKAGE_TSK("eventTasker"),

    PROCESS_MNG("MNG"),
    PACKAGE_MNG("eventManager"),

    PROCESS_HND("HND"),
    PACKAGE_HND("eventHandler"),

    PACKAGE_COMMON("com.dk.platform."),

    EMS_URL("tcp://140.238.31.93:7222"),
    EMS_USR("admin"),
    EMS_PWD(""),
    EMS_WRK_PREFIX("F1.EEP.WRK."),   //   WRK => WORK, Contain Tasks.
    EMS_TSK_PREFIX("F1.EEP.MGR.TSK."),
    EMS_MNG_QUEUE_NAME("F1.EEP.MGR.MNG"),   // MANAGER PROCESS will Receive Only One Queue.

    NEW_QUEUE_CREATE_VAL("target_name"),    // Map Message Header get new created Target name.

    /**
     * Message Properties.
     */
    MSG_TYPE("MSG_TYPE"),                       // Message Type. EMS Header Key.. below properties will be key.
    TSK_INIT_MNG_VAL("TSK_INIT"),               // (Tasker ==> Manager) Tasker Send Message to Manager when Tasker initialize.
    // ABOUT HEALTH CHECK
    TSK_HLTH_MNG_VAL("TSK_WRK_STATUS"),         // (Tasker ==> Manager) Tasker Send Health Check Message contain work queue status to manager periodically.
    TSK_HLTH_FROM_PROP("TSK_WRK_FROM"),         // (Tasker ==> Manager) Tasker Name who send HealthCheck Message.
    // RE-BALANCE
    TSK_RBL_RTN_MNG_VAL("TSK_WRK_RETURN"),      // (Tasker ==> Manager) Tasker Send Returned Work Queue to Manager Because of Rebalancing Order from Manager.
    MNG_RBL_CNT_TSK_VAL("MNG_REBALANCE_COUNT"), // (Manager ==> Tasker) Manager ask Tasker to return count of Work Queue.


    // COMPLETE
    TSK_COM_WRK_MNG_VAL("TSK_WRK_COMPLETE"),    // (Tasker ==> Manager) Tasker Send Completed Work Queue to Manager.

    // Assign Task to TSK
    MNG_ASG_WRK_VAL("MNG_ASSIGN_WRK"),          // (Manager ==> Tasker) Manager Send Work Queue to TASKER.


    /**
     * TSK Subprocess Receive Timeout second
     */
    TSK_REC_TIM_OUT("100"),

    /**
     * TSK PERIODICALLY PROPERTY
     */
    TSK_POLLING_INTERVAL("3");

    private String value;

    AppPro(String value){
        this.value = value;
    }
}


/**
 * Received message: MapMessage=
 * {
 *  Header=
 *      { JMSMessageID={null}
 *        JMSDestination={Topic[$sys.monitor.queue.create]}
 *        JMSReplyTo={null}
 *        JMSDeliveryMode={22}
 *        JMSRedelivered={false}
 *        JMSCorrelationID={null}
 *        JMSType={null}
 *        JMSTimestamp={Wed Jun 02 16:18:55 KST 2021}
 *        JMSDeliveryTime={0}
 *        JMSExpiration={0}
 *        JMSPriority={4}
 *        }
 *  Properties=
 *      { conn_type={String:generic}
 *        server={String:EMS-SERVER}
 *        JMSXDeliveryCount={Integer:1}
 *        target_name={String:h4}
 *        target_object={String:queue}
 *        event_reason={String:producer}
 *        event_class={String:queue.create}
 *        conn_hostname={String:DESKTOP-SS74P4U}
 *        conn_connid={Long:27}
 *        conn_username={String:anonymous}
 *        }
 *  Fields={ } }
 */