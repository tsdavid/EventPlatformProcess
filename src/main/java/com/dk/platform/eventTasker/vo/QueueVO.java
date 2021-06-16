package com.dk.platform.eventTasker.vo;

import com.dk.platform.eventTasker.subProcess.WorkQueueReceiverSubProcess;
import com.dk.platform.eventTasker.util.MemoryStorage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Main Job
 * 1. Create VO
 */
@Getter
@NoArgsConstructor
public class QueueVO {

    private String QueueName;
    private Timestamp Created_Time;
    private String Status;
    private WorkQueueReceiverSubProcess subprocess;


    /**
     *
     * @param queueName         :       Work Queue Name.
     * @param created_Time      :       QueueVO Created Time
     * @param status            :       Queue Working Status... // TODO THINK BETTER ==> Does Queue Status Need?.
     * @param subProcess        :       Receiver Process which Work Queue Name.  {@link WorkQueueReceiverSubProcess}
     */
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
                ", subprocess=" + subprocess.getClass().getSimpleName() +
                '}';
    }

    /*****************************************************************************************
     *************************************  Main *********************************************
     ****************************************************************************************/


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
