package com.dk.platform.common.confs;

import org.json.simple.JSONObject;

public class MessageConf{

    final String Key = "Key"; final String value = "Value";
    final String New_Queue_Create = "New_Queue_Create"; final String TSK_Health_Msg_From = "TSK_Health_Msg_From"; final String Message_Type = "Message_Type";
    String New_Queue_CreateVal; String TSK_Health_Msg_FromVal; String Message_TypeVal;

    final String TSK_Init_To_Manager = "TSK_Init_To_Manager"; final String TSK_Health_Message = "TSK_Health_Message"; final String TSK_RBL_Return_Message = "TSK_RBL_Return_Message";
    final String MNG_RBL_Count_To_Tasker = "MNG_RBL_Count_To_Tasker"; final String TSK_Complete_WRK_To_Manager = "TSK_Complete_WRK_To_Manager"; final String MNG_Assign_WRK_To_Tasker = "MNG_Assign_WRK_To_Tasker";
    String TSK_Init_To_ManagerVal; String TSK_Health_MessageVal; String TSK_RBL_Return_MessageVal;
    String MNG_RBL_Count_To_TaskerVal; String TSK_Complete_WRK_To_ManagerVal; String MNG_Assign_WRK_To_TaskerVal;

    public MessageConf(){}

    public MessageConf(JSONObject jsonObject){

        JSONObject keyObject = (JSONObject) jsonObject.get(Key);
        New_Queue_CreateVal = (String) keyObject.get(New_Queue_Create);

        TSK_Health_Msg_FromVal = (String) keyObject.get(TSK_Health_Msg_From);

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
