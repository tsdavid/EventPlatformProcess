package com.dk.platform.common.confs;

import org.json.simple.JSONObject;

public class EMS_Conf{

    final String URL = "URL"; final String USR = "USR"; final String PWD = "PWD";
    final String Prefix = "Prefix";
    final String WorkQueue = "WorkQueue"; final String TaskQueue = "TaskQueue"; final String ManagerQueue = "ManagerQueue";

    String UrlVal; String UsrVal; String PwdVal;
    String WorkQueueVal; String TaskQueueVal; String ManagerQueueVal;

    public EMS_Conf(){}

    public EMS_Conf(JSONObject jsonObject){

        UrlVal = (String) jsonObject.get(URL);

        UsrVal = (String) jsonObject.get(USR);

        PwdVal = (String) jsonObject.get(PWD);

        JSONObject PrefixObject = (JSONObject) jsonObject.get(Prefix);
        WorkQueueVal = (String) PrefixObject.get(WorkQueue);

        TaskQueueVal = (String) PrefixObject.get(TaskQueue);

        ManagerQueueVal = (String) PrefixObject.get(ManagerQueue);

    }

    public String getUrlVal() {
        return UrlVal;
    }

    public String getUsrVal() {
        return UsrVal;
    }

    public String getPwdVal() {
        return PwdVal;
    }

    public String getWorkQueueVal() {
        return WorkQueueVal;
    }

    public String getTaskQueueVal() {
        return TaskQueueVal;
    }

    public String getManagerQueueVal() {
        return ManagerQueueVal;
    }

    @Override
    public String toString() {
        return "EMS{" +
                "UrlVal='" + UrlVal + '\'' +
                ", UsrVal='" + UsrVal + '\'' +
                ", PwdVal='" + PwdVal + '\'' +
                ", WorkQueueVal='" + WorkQueueVal + '\'' +
                ", TaskQueueVal='" + TaskQueueVal + '\'' +
                ", ManagerQueueVal='" + ManagerQueueVal + '\'' +
                '}';
    }
}
