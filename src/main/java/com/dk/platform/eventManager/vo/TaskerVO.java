package com.dk.platform.eventManager.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Main Job
 * 1. Create Tasker VO.
 */
@Getter
@NoArgsConstructor
public class TaskerVO {

    private Timestamp Created_Time;
    private Timestamp Updated_Time;
    private String TaskerName;
    private String WorkQueueList;       // queuename concat with ","
    private int count;                  // count of managing work queue length.


    /**
     *
     * @param created_Time          :       TaskerVO Created Time.
     * @param updated_Time          :       recently Update Time when Tasker's Health Check.
     * @param taskerName            :       Tasker Name.
     * @param workQueueList         :       Work Queue List owned by Tasker.
     * @param cnt                   :       Count of Work Queues
     */
    @Builder
    public TaskerVO(Timestamp created_Time, Timestamp updated_Time, String taskerName, String workQueueList, int cnt) {
        Created_Time = created_Time;
        Updated_Time = updated_Time;
        TaskerName = taskerName;
        WorkQueueList = workQueueList;
        count = cnt;
    }


    @Override
    public String toString() {
        return "TaskerVO{" +
                "Created_Time=" + Created_Time +
                ", Updated_Time=" + Updated_Time +
                ", TaskerName='" + TaskerName + '\'' +
                ", WorkQueueList='" + WorkQueueList + '\'' +
                ", count=" + count +
                '}';
    }


    /**
     *
     * @param updated_Time          :       recently Update Time when Tasker's Health Check.
     */
    public void setUpdated_Time(Timestamp updated_Time) {
        Updated_Time = updated_Time;
    }


    /**
     *
     * @param count                   :       Count of Work Queues
     */
    public void setCount(int count){
        this.count = count;
    }


    /**
     *
     * @param workQueueList         :       Work Queue List owned by Tasker.
     */
    public void setWorkQueueList(String workQueueList) {
        WorkQueueList = workQueueList;
    }


    /*****************************************************************************************
     *************************************  Main *********************************************
     ****************************************************************************************/


    public static void main(String[] args){
        TaskerVO taskerVO = TaskerVO.builder().created_Time(new Timestamp(System.currentTimeMillis()))
                .updated_Time(new Timestamp(System.currentTimeMillis()))
                .taskerName("TaskerName")
                .build();

        System.out.println(
                taskerVO.toString()
        );
    }
}
