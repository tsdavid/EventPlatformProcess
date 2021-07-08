package com.dk.platform.backup;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

@Slf4j
public class EppConf_Backup {

    final String PlatformName = "Name"; private String PlatformNameVal;
    final String Enviroment= "Env"; private String EnviromentVal;
    final String EMS = "EMS"; final String Message = "Message"; final String Process = "Process";

    public EMS ems; public Message message; public Process process;

    private JSONObject jsonObject;

    public EppConf_Backup(){

        // TODO TODO REMOVE.

    }


    public EppConf_Backup(String filePath){


        try {
            JSONParser jsonParser = new JSONParser();
            jsonObject = (JSONObject) jsonParser.parse(new FileReader(filePath));

            PlatformNameVal = (String) jsonObject.get(PlatformName);

            EnviromentVal = (String) jsonObject.get(Enviroment);

            JSONObject EnvObject = (JSONObject) jsonObject.get(EnviromentVal);
            ems = new EMS((JSONObject) EnvObject.get(EMS));

            message = new Message((JSONObject) EnvObject.get(Message));

            process = new Process((JSONObject) EnvObject.get(Process));

        } catch (IOException e) {
            e.printStackTrace();

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    class EMS{

        final String URL = "URL"; final String USR = "USR"; final String PWD = "PWD";
        final String Prefix = "Prefix";
        final String WorkQueue = "WorkQueue"; final String TaskQueue = "TaskQueue"; final String ManagerQueue = "ManagerQueue";

        String UrlVal; String UsrVal; String PwdVal;
        String WorkQueueVal; String TaskQueueVal; String ManagerQueueVal;

        EMS(){}

        EMS(JSONObject jsonObject){

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

    class Message{

        final String Key = "Key"; final String value = "Value";
        final String New_Queue_Create = "New_Queue_Create"; final String TSK_Health_Msg_From = "TSK_Health_Msg_From"; final String Message_Type = "Message_Type";
        String New_Queue_CreateVal; String TSK_Health_Msg_FromVal; String Message_TypeVal;

        final String TSK_Init_To_Manager = "TSK_Init_To_Manager"; final String TSK_Health_Message = "TSK_Health_Message"; final String TSK_RBL_Return_Message = "TSK_RBL_Return_Message";
        final String MNG_RBL_Count_To_Tasker = "MNG_RBL_Count_To_Tasker"; final String TSK_Complete_WRK_To_Manager = "TSK_Complete_WRK_To_Manager"; final String MNG_Assign_WRK_To_Tasker = "MNG_Assign_WRK_To_Tasker";
        String TSK_Init_To_ManagerVal; String TSK_Health_MessageVal; String TSK_RBL_Return_MessageVal;
        String MNG_RBL_Count_To_TaskerVal; String TSK_Complete_WRK_To_ManagerVal; String MNG_Assign_WRK_To_TaskerVal;

        Message(){}

        Message(JSONObject jsonObject){

            JSONObject keyObject = (JSONObject) jsonObject.get(Key);
            New_Queue_CreateVal = (String) keyObject.get(New_Queue_Create);

            TSK_Health_MessageVal = (String) keyObject.get(TSK_Health_Message);

            Message_TypeVal = (String) keyObject.get(Message_Type);

            JSONObject valueObject = (JSONObject) jsonObject.get(value);
            TSK_Init_To_ManagerVal = (String) valueObject.get(TSK_Init_To_Manager);

            TSK_Health_MessageVal = (String) valueObject.get(TSK_Health_Message);

            TSK_RBL_Return_MessageVal = (String) valueObject.get(TSK_RBL_Return_Message);

            MNG_RBL_Count_To_TaskerVal = (String) valueObject.get(MNG_RBL_Count_To_Tasker);

            TSK_Complete_WRK_To_ManagerVal = (String) valueObject.get(TSK_Complete_WRK_To_Manager);

            MNG_Assign_WRK_To_TaskerVal = (String) valueObject.get(MNG_Assign_WRK_To_Tasker);

        }

        public String getNew_Queue_CreateVal() {
            return New_Queue_CreateVal;
        }

        public String getTSK_Health_Msg_FromVal() {
            return TSK_Health_Msg_FromVal;
        }

        public String getMessage_TypeVal() {
            return Message_TypeVal;
        }

        public String getTSK_Init_To_ManagerVal() {
            return TSK_Init_To_ManagerVal;
        }

        public String getTSK_Health_MessageVal() {
            return TSK_Health_MessageVal;
        }

        public String getTSK_RBL_Return_MessageVal() {
            return TSK_RBL_Return_MessageVal;
        }

        public String getMNG_RBL_Count_To_TaskerVal() {
            return MNG_RBL_Count_To_TaskerVal;
        }

        public String getTSK_Complete_WRK_To_ManagerVal() {
            return TSK_Complete_WRK_To_ManagerVal;
        }

        public String getMNG_Assign_WRK_To_TaskerVal() {
            return MNG_Assign_WRK_To_TaskerVal;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "New_Queue_CreateVal='" + New_Queue_CreateVal + '\'' +
                    ", TSK_Health_Msg_FromVal='" + TSK_Health_Msg_FromVal + '\'' +
                    ", Message_TypeVal='" + Message_TypeVal + '\'' +
                    ", TSK_Init_To_ManagerVal='" + TSK_Init_To_ManagerVal + '\'' +
                    ", TSK_Health_MessageVal='" + TSK_Health_MessageVal + '\'' +
                    ", TSK_RBL_Return_MessageVal='" + TSK_RBL_Return_MessageVal + '\'' +
                    ", MNG_RBL_Count_To_TaskerVal='" + MNG_RBL_Count_To_TaskerVal + '\'' +
                    ", TSK_Complete_WRK_To_ManagerVal='" + TSK_Complete_WRK_To_ManagerVal + '\'' +
                    ", MNG_Assign_WRK_To_TaskerVal='" + MNG_Assign_WRK_To_TaskerVal + '\'' +
                    '}';
        }
    }

    class Process{

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


        Process(){}

        Process(JSONObject jsonObject){

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

    public static void main(String[] args) {

//        JSONParser jsonParser = new JSONParser();
//        JSONObject jsonObject = null;
//
//        try {
//            jsonObject = (JSONObject) jsonParser.parse(jsonString);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(jsonObject.get("Name"));
//        System.out.println(jsonObject.get("Env"));
//        System.out.println(jsonObject.get(jsonObject.get("Env")));
//        System.out.println(jsonObject.get("EMS"));

        System.out.println(new EppConf_Backup().ems.toString());
        System.out.println(new EppConf_Backup().message.toString());
        System.out.println(new EppConf_Backup().process.toString());
    }

}

