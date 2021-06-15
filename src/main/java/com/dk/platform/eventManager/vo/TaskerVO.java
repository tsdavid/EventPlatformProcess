package com.dk.platform.eventManager.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 *  + Work Queue List.
 *  + Tasker Status.
 */

@Getter
@NoArgsConstructor
public class TaskerVO {

    private Timestamp Created_Time;
    private Timestamp Updated_Time;
    private String TaskerName;
    private String WorkQueueList;       // queuename concat with ","
    private int count;                  // count of managing work queue length.

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

    public void setUpdated_Time(Timestamp updated_Time) {
        Updated_Time = updated_Time;
    }

    public void setCount(int count){
        this.count = count;
    }

    public void setWorkQueueList(String workQueueList) {
        WorkQueueList = workQueueList;
    }

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
