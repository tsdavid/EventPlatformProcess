package com.dk.platform.eventManager.structure;

import com.dk.platform.eventManager.util.ManagerUtil;
import com.dk.platform.eventManager.vo.TaskerVO;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class SingleToneTest {

    @Test
    public void integrity_Test(){

        // given
        ConcurrentHashMap<String, TaskerVO> stringTaskerVOConcurrentHashMap = ManagerUtil.getInstance().getTasker_Mng_Map();
        String name = "test_name";
        String conId = "test_id";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        TaskerVO taskerVO = TaskerVO.builder()
                .created_Time(timestamp)
                .updated_Time(timestamp)
                .taskerName(name)
                .ems_connection_id(conId)
                .build();

        // when
        stringTaskerVOConcurrentHashMap.put(name, taskerVO);


        // then
        assertThat(ManagerUtil.getInstance().getTasker_Mng_Map().get(name).getTaskerName()).isEqualTo(name);

    }


    @Test
    public void updateTimeChange_Test(){

        // given
        ConcurrentHashMap<String, TaskerVO> stringTaskerVOConcurrentHashMap = ManagerUtil.getInstance().getTasker_Mng_Map();
        String name = "test_name";
        String conId = "test_id";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        TaskerVO taskerVO = TaskerVO.builder()
                .created_Time(timestamp)
                .updated_Time(timestamp)
                .taskerName(name)
                .ems_connection_id(conId)
                .build();

        // when
        stringTaskerVOConcurrentHashMap.put(name, taskerVO);
        Timestamp update_timestamp = new Timestamp(System.currentTimeMillis()+100);
        ManagerUtil.getInstance().updateTaskerHealthCheckTime(name, update_timestamp);


        // then
        assertThat(ManagerUtil.getInstance().getTasker_Mng_Map().get(name).getUpdated_Time()).isEqualTo(update_timestamp);
    }

    @Test
    public void getWorkQueueCount_Test(){

        int cnt = 10;
        String tasker_name = "tasker";
        IntStream.rangeClosed(1, cnt).forEach(i -> {
            String queue_name = "queue"+i;
            ManagerUtil.getInstance().getWorkQueue_Mng_Map().put(queue_name, tasker_name);
        });

        assertThat(ManagerUtil.getInstance().getWorkQueueCount(tasker_name)).isEqualTo(cnt);


    }
}
