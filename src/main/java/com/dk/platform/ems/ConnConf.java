package com.dk.platform.ems;

import lombok.Getter;

@Getter
public enum ConnConf {

    EMS_URL("tcp://localhost:7222"),
    EMS_USR("admin"),
    EMS_PWD(""),
    EMS_WRK_PREFIX("F1_EEP_WRK"),   //   WRK => WORK, Contain Tasks.
    EMS_TSK_PREFIX("F1_EEP_MGR_TSK"),
    EMS_MNG_PREFIX("F1_EEP_MGR_MNG");

    private String value;

    ConnConf(String value){
        this.value = value;
    }
}
