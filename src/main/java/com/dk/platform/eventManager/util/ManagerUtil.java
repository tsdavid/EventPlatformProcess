package com.dk.platform.eventManager.util;


import com.dk.platform.ems.ConnConf;
import com.dk.platform.eventManager.vo.TaskerVO;
import com.tibco.tibjms.TibjmsConnectionFactory;
import com.tibco.tibjms.admin.QueueInfo;
import com.tibco.tibjms.admin.TibjmsAdmin;
import com.tibco.tibjms.admin.TibjmsAdminException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.*;

public class ManagerUtil {

    private static class SingletonHelper {
        private static final ManagerUtil INSTANCE = new ManagerUtil();
    }

    public static ManagerUtil getInstance() {
        return ManagerUtil.SingletonHelper.INSTANCE;
    }


    static ConnectionFactory factory;
    static Connection connection;
    static TibjmsAdmin tibjmsAdmin;

    /**
     * TASKER MANAGE MAP.
     * Key : TASKER NAME
     * VALUE : TASKER VO.
     */
    private ConcurrentHashMap<String, TaskerVO> Tasker_Mng_Map;


    /**
     * Work Queue Manage Map.
     * Key : QueueName
     * Value :  TaskerName/
     */
    private ConcurrentHashMap<String, String> WorkQueue_Mng_Map;


    public ConcurrentHashMap<String, TaskerVO> getTasker_Mng_Map() {
        if(Tasker_Mng_Map == null){
            Tasker_Mng_Map = new ConcurrentHashMap<String, TaskerVO>();
        }
        return Tasker_Mng_Map;
    }

    public ConcurrentHashMap<String, String> getWorkQueue_Mng_Map(){
        if(WorkQueue_Mng_Map == null){
            WorkQueue_Mng_Map = new ConcurrentHashMap<>();
        }
        return WorkQueue_Mng_Map;
    }

    /**
     * when Receive Health Check Message.
     * Tasker VO  update Time is updated.
     * @param name
     */
    public void updateTaskerHealthCheckTime(String name, Timestamp timestamp){
        if(Tasker_Mng_Map == null){
            return;
        }
        TaskerVO vo = Tasker_Mng_Map.get(name);
        vo.setUpdated_Time(timestamp);
    }

    /**
     * SELECT COUNT("taskerName") FROM MAP.
     * @param taskerName
     */
    public Integer getWorkQueueCount(String taskerName){
        if(WorkQueue_Mng_Map == null){
            return null;
        }
        int cnt = (int) WorkQueue_Mng_Map.entrySet().stream().filter(entry -> entry.getValue().equals(taskerName)).count();
        return cnt;

    }







    public static void main(String[] args) {

    }

}
