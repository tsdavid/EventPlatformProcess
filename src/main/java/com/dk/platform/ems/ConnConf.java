package com.dk.platform.ems;

import lombok.Getter;

@Getter
public enum ConnConf {

    EMS_URL("tcp://localhost:7222"),
    EMS_USR("admin"),
    EMS_PWD(""),
    EMS_WRK_PREFIX("F1.EEP.WRK"),   //   WRK => WORK, Contain Tasks.
    EMS_TSK_PREFIX("F1.EEP.MGR.TSK."),
    EMS_MNG_QUEUE_NAME("F1.EEP.MGR.MNG"),   // MANAGER PROCESS will Receive Only One Queue.
    /**
     * Message Properties.
     */
    MSG_TYPE("MSG_TYPE"),                       // Message Type. EMS Header Key.. below properties will be key.
    TSK_INIT_MNG_PROP("TSK_INIT"),              // Tasker Send Message to Manager when Tasker initialize.
    TSK_HLTH_MNG_PROP("TSK_WRK_STATUS"),        // Tasker Send Health Check Message contain work queue status to manager periodically.
    TSK_RET_WRK_MNG_PROP("TSK_WRK_RETURN"),     // Tasker Send Returned Work Queue to Manager Because of Rebalancing Order from Manager.
    TSK_COM_WRK_MNG_PROP("TSK_WRK_COMPLETE");   // Tasker Send Completed Work Queue to Manager.

    private String value;

    ConnConf(String value){
        this.value = value;
    }
}
