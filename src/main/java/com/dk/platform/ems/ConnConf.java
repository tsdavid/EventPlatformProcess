package com.dk.platform.ems;

import lombok.Getter;

@Getter
public enum ConnConf {

    EMS_URL("tcp://localhost:7222"),
    EMS_USR("admin"),
    EMS_PWD(""),
    EMS_WRK_PREFIX("F1.EEP.WRK."),   //   WRK => WORK, Contain Tasks.
    EMS_TSK_PREFIX("F1.EEP.MGR.TSK."),
    EMS_MNG_QUEUE_NAME("F1.EEP.MGR.MNG"),   // MANAGER PROCESS will Receive Only One Queue.
    /**
     * Message Properties.
     */
    MSG_TYPE("MSG_TYPE"),                       // Message Type. EMS Header Key.. below properties will be key.
    TSK_INIT_MNG_VAL("TSK_INIT"),              // Tasker Send Message to Manager when Tasker initialize.
    // ABOUT HEALTH CHECK
    TSK_HLTH_MNG_VAL("TSK_WRK_STATUS"),        // Tasker Send Health Check Message contain work queue status to manager periodically.
    TSK_HLTH_FROM_PROP("TSK_WRK_FROM"),         // Tasker Name who send HealthCheck Message.
    // RE-BALANCE
    TSK_RET_WRK_MNG_VAL("TSK_WRK_RETURN"),     // Tasker Send Returned Work Queue to Manager Because of Rebalancing Order from Manager.
    TSK_COM_WRK_MNG_VAL("TSK_WRK_COMPLETE"),   // Tasker Send Completed Work Queue to Manager.

    // Assign Task to TSK
    MNG_ASG_WRK_VAL("MNG_ASSIGN_WRK"),          // Manager Send Work Queue to TASKER.

    /**
     * TSK PERIODICALLY PROPERTY
     */
    TSK_POLLING_INTERVAL("3");

    private String value;

    ConnConf(String value){
        this.value = value;
    }
}
