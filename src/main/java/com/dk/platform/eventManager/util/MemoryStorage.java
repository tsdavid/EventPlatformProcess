package com.dk.platform.eventManager.util;

import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.vo.TaskerVO;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryStorage {

    private MemoryStorage(){}

    private static class SingletonHelper {
        private static final MemoryStorage INSTANCE = new MemoryStorage();
    }

    public static MemoryStorage getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private EmsUtil emsUtil;
    private ManagerUtil managerUtil;


    /**
     * TASKER MANAGE MAP.
     * Key : TASKER NAME
     * VALUE : TASKER VO.
     */
    private ConcurrentHashMap<String, TaskerVO> Tasker_Mng_Map;


    /**
     * Temporary Work Queue Container.
     * QueueName
     */
    private HashSet<String> Tmp_WRK_Queue;


    /**
     * LOGIC
     */


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
     * Getter
     */
    public ConcurrentHashMap<String, TaskerVO> getTasker_Mng_Map() {
        if(Tasker_Mng_Map == null){
            Tasker_Mng_Map = new ConcurrentHashMap<String, TaskerVO>();
        }
        return Tasker_Mng_Map;
    }

    public HashSet<String> getTmp_WRK_Queue() {
        if(Tmp_WRK_Queue == null){
            this.Tmp_WRK_Queue = new HashSet<>();
        }
        return Tmp_WRK_Queue;
    }

    public EmsUtil getEmsUtil() {
        return emsUtil;
    }

    public ManagerUtil getManagerUtil() {
        return managerUtil;
    }

    /**
     * Setter
     */
    public void setEmsUtil(EmsUtil emsUtil) {
        this.emsUtil = emsUtil;
    }

    public void setManagerUtil(ManagerUtil managerUtil) {
        this.managerUtil = managerUtil;
    }

    /**
     * SELECT COUNT("taskerName") FROM MAP.
     * @param taskerName
     */
//    public Integer getWorkQueueCount(String taskerName){
//        if(WorkQueue_Mng_Map == null){
//            return null;
//        }
//        int cnt = (int) WorkQueue_Mng_Map.entrySet().stream().filter(entry -> entry.getValue().equals(taskerName)).count();
//        return cnt;
//
//    }
}
