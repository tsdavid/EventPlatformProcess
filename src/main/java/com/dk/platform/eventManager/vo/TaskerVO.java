package com.dk.platform.eventManager.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

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
    private String Ems_connection_id;

    @Builder
    public TaskerVO(Timestamp created_Time, Timestamp updated_Time, String taskerName, String ems_connection_id) {
        Created_Time = created_Time;
        Updated_Time = updated_Time;
        TaskerName = taskerName;
        Ems_connection_id = ems_connection_id;
    }

    @Override
    public String toString() {
        return "TaskerVO{" +
                "Created_Time=" + Created_Time +
                ", Updated_Time=" + Updated_Time +
                ", TaskerName='" + TaskerName + '\'' +
                ", Ems_connection_id='" + Ems_connection_id + '\'' +
                '}';
    }

    public void setUpdated_Time(Timestamp updated_Time) {
        Updated_Time = updated_Time;
    }

    public static void main(String[] args){
        TaskerVO taskerVO = TaskerVO.builder().created_Time(new Timestamp(System.currentTimeMillis()))
                .updated_Time(new Timestamp(System.currentTimeMillis()))
                .taskerName("TaskerName")
                .ems_connection_id("ems_Connection").build();

        System.out.println(
                taskerVO.toString()
        );
    }
}
