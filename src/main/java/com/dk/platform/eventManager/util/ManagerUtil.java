package com.dk.platform.eventManager.util;


import com.dk.platform.ems.ConnConf;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.vo.TaskerVO;

import javax.jms.JMSException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.*;

public class ManagerUtil {

    private MemoryStorage memoryStorage;
    private EmsUtil emsUtil;

    private Map<String, String> AssignTskProperties;

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

    public ManagerUtil(){
        this.memoryStorage = MemoryStorage.getInstance();
        this.Tasker_Mng_Map = this.memoryStorage.getTasker_Mng_Map();
        this.Tmp_WRK_Queue = this.memoryStorage.getTmp_WRK_Queue();

        this.emsUtil = this.memoryStorage.getEmsUtil();

        // Set-Up Assign Tsk Properties.
        this.AssignTskProperties.put(ConnConf.MSG_TYPE.getValue(), ConnConf.MNG_ASG_WRK_VAL.getValue());


    }

    /**
     * TASKER VO Updated when Receive Health Check Message.
     * @param TaskerName
     * @param workQueueList
     * @param count
     */
    public void updateTaskerHealthCheck(String TaskerName, String workQueueList, int count){
        if(Tasker_Mng_Map == null){
            Tasker_Mng_Map = memoryStorage.getTasker_Mng_Map();
        }


        TaskerVO vo = Tasker_Mng_Map.get(TaskerName);
        if(vo != null){
            vo.setUpdated_Time(new Timestamp(System.currentTimeMillis()));
            vo.setCount(count);
            vo.setWorkQueueList(workQueueList);
            System.out.println(vo.toString());
        }else{
            this.setUpTasker(TaskerName, workQueueList, count);
        }

    }


    /**
     * Set-Up Tasker if tasker absent on map when receive Health Check Message.
     * @param TaskerName
     * @param workQueueList
     * @param count
     */
    private void setUpTasker(String TaskerName, String workQueueList, int count){

        try{
            TaskerVO taskerVO = TaskerVO.builder().taskerName(TaskerName)
                    .created_Time(new Timestamp(System.currentTimeMillis()))
                    .updated_Time(new Timestamp(System.currentTimeMillis()))
                    .workQueueList(workQueueList)
                    .cnt(count)
                    .build();

            memoryStorage.getTasker_Mng_Map().put(TaskerName, taskerVO);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * assign Work Queue to Tasker Queue.
     * @param WrkQueueName
     */
    public void assignWrkQtoTSK(String WrkQueueName){

        String tasker = this.findIdleTasker();
        try {
            this.sendAssignWrkMessage(tasker, WrkQueueName);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    private String findIdleTasker(){

        int min = 32768;
        String tasker = null;
        for(Map.Entry<String, TaskerVO> vo : memoryStorage.getTasker_Mng_Map().entrySet()){

            if(vo.getValue().getCount() < min){
                min = vo.getValue().getCount();
                tasker = vo.getValue().getTaskerName();
            }
        }
        return tasker;
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
     * Send to Tasker. to assign WRK queue in TMP
     * @param TskName
     */
    public void executeTmpQueue(String TskName){


        if(Tmp_WRK_Queue.size() > 0){
            Tmp_WRK_Queue.stream().forEach(wrkQ -> {
                try {
                    this.sendAssignWrkMessage(TskName, wrkQ);

                } catch (JMSException e) {
                    e.printStackTrace();
                }
            });
        }

        // Clean Up TMP Set
        Tmp_WRK_Queue.clear();
        System.out.println("TMP WRK has been cleaned.  length : " + Tmp_WRK_Queue.size());
    }

    private void sendAssignWrkMessage(String TskName, String WrkQueue) throws JMSException {
        emsUtil.sendSafeQueueMessage(TskName, WrkQueue, this.AssignTskProperties);

    }








    public static void main(String[] args) {

    }

}
