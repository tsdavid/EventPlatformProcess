package com.dk.platform.common.confs;

import org.json.simple.JSONObject;

public class ProcessConf{

    final String Common = "Common"; final String  EventHandler = "EventHandler"; final String EventManager = "EventManager";
    final String EventTasker = "EventTasker"; final String EventWatcher = "EventWatcher";

    // Common
    final String PackageCommon = "PackageCommon";
    final String PackageHND = "PackageHND"; final String ProcessHND = "ProcessHND";
    final String PackageMNG = "PackageMNG"; final String ProcessMNG = "ProcessMNG";
    final String PackageTSK = "PackageTSK"; final String ProcessTSK = "ProcessTSK";

    String PackageCommonVal;
    String PackageHNDVal; String ProcessHNDVal;
    String PackageMNGVal; String ProcessMNGVal;
    String PackageTSKVal; String ProcessTSKVal;

    // EventHandler

    // EventManager
    final String Monitor_Queue_Create = "Monitor_Queue_Create"; String Monitor_Queue_CreateVal;

    // EventTasker
    final String Sub_TSK_REC_Time_Out = "Sub_TSK_REC_Time_Out"; int Sub_TSK_REC_Time_OutVal;
    final String TSK_Polling_Interval = "TSK_Polling_Interval"; int TSK_Polling_IntervalVal;

    // EventWatcher


    public ProcessConf(){}

    public ProcessConf(JSONObject jsonObject){

        // Common
        JSONObject CommonObject = (JSONObject) jsonObject.get(Common);
        PackageCommonVal = (String) CommonObject.get(PackageCommon);

        PackageHNDVal = (String) CommonObject.get(PackageHND); ProcessHNDVal = (String) CommonObject.get(ProcessHND);

        PackageMNGVal = (String) CommonObject.get(PackageMNG); ProcessMNGVal = (String) CommonObject.get(ProcessMNG);

        PackageTSKVal = (String) CommonObject.get(PackageTSK); ProcessTSKVal = (String) CommonObject.get(ProcessTSK);

        // EventHandler
//            JSONObject HandlerObject = (JSONObject) jsonObject.get(EventHandler);

        // EventManger
        JSONObject ManagerObject = (JSONObject) jsonObject.get(EventManager);
        Monitor_Queue_CreateVal = (String) ManagerObject.get(Monitor_Queue_Create);

        // EventTasker
        JSONObject TaskerObject = (JSONObject) jsonObject.get(EventTasker);
        Sub_TSK_REC_Time_OutVal = Integer.parseInt(TaskerObject.get(Sub_TSK_REC_Time_Out).toString());

        TSK_Polling_IntervalVal = Integer.parseInt(TaskerObject.get(TSK_Polling_Interval).toString());

        // EventWatcher
//            JSONObject WatcherObject = (JSONObject) jsonObject.get(EventWatcher);
    }

    public String getPackageCommonVal() {
        return PackageCommonVal;
    }

    public String getPackageHNDVal() {
        return PackageHNDVal;
    }

    public String getProcessHNDVal() {
        return ProcessHNDVal;
    }

    public String getPackageMNGVal() {
        return PackageMNGVal;
    }

    public String getProcessMNGVal() {
        return ProcessMNGVal;
    }

    public String getPackageTSKVal() {
        return PackageTSKVal;
    }

    public String getProcessTSKVal() {
        return ProcessTSKVal;
    }

    public String getMonitor_Queue_CreateVal() {
        return Monitor_Queue_CreateVal;
    }

    public int getSub_TSK_REC_Time_OutVal() {
        return Sub_TSK_REC_Time_OutVal;
    }

    public int getTSK_Polling_IntervalVal() {
        return TSK_Polling_IntervalVal;
    }

    @Override
    public String toString() {
        return "Process{" +
                "PackageCommonVal='" + PackageCommonVal + '\'' +
                ", PackageHNDVal='" + PackageHNDVal + '\'' +
                ", ProcessHNDVal='" + ProcessHNDVal + '\'' +
                ", PackageMNGVal='" + PackageMNGVal + '\'' +
                ", ProcessMNGVal='" + ProcessMNGVal + '\'' +
                ", PackageTSKVal='" + PackageTSKVal + '\'' +
                ", ProcessTSKVal='" + ProcessTSKVal + '\'' +
                ", Monitor_Queue_CreateVal='" + Monitor_Queue_CreateVal + '\'' +
                ", Sub_TSK_REC_Time_OutVal=" + Sub_TSK_REC_Time_OutVal +
                ", TSK_Polling_IntervalVal=" + TSK_Polling_IntervalVal +
                '}';
    }
}
