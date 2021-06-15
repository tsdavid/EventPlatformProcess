package com.dk.platform.eventManager.vo;

import org.junit.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

public class VoTest {

    @Test
    public void TaskerVO_Test() {

        // given
        String name = "test_name";
        String conId = "test_id";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // when
        TaskerVO taskerVO = TaskerVO.builder()
                .created_Time(timestamp)
                .updated_Time(timestamp)
                .taskerName(name)
                .build();

        // then
        assertThat(taskerVO.getTaskerName()).isEqualTo(name);
        assertThat(taskerVO.getCreated_Time()).isEqualTo(timestamp);
    }

}
