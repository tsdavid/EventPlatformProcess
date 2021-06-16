package com.dk.platform.eventTasker.vo;

import com.dk.platform.eventTasker.subProcess.WorkQueueReceiverSubProcess;
import com.dk.platform.eventTasker.util.MemoryStorage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@NoArgsConstructor
public class QueueVO {

    private String QueueName;
    private Timestamp Created_Time;
    private String Status;
    private WorkQueueReceiverSubProcess subprocess;

    @Builder
    public QueueVO(String queueName, Timestamp created_Time, String status, WorkQueueReceiverSubProcess subProcess) {
        QueueName = queueName;
        Created_Time = created_Time;
        Status = status;
        subprocess = subProcess;
    }

    @Override
    public String toString() {
        return "QueueVO{" +
                "QueueName='" + QueueName + '\'' +
                ", Created_Time=" + Created_Time +
                ", Status='" + Status + '\'' +
                '}';
    }

    public static void main(String[] args) {
        QueueVO queueVO = QueueVO.builder()
                            .queueName("test")
                            .created_Time(new Timestamp(System.currentTimeMillis()))
                            .status("status")
                            .build();
        System.out.println(
                queueVO.toString()
        );

        MemoryStorage.getInstance().getQueueMap().put("test", queueVO);
        System.out.println(
                MemoryStorage.getInstance().getQueueMap().toString()
        );
    }
}
