package com.dk.platform.eventTasker;

import lombok.Getter;

@Getter
public enum TaskerStatus {

    Stop(0), Active(1), Work(2), Stun(9);
    /**
     * Represent Tasker Status...
     * Stop : Not Active.w
     * Active : Tasker is Running
     * Stun :  The Middle of  Stop. Stopping
     */

    private final Integer status;

    private Long time;

    private TaskerStatus(Integer status) {
        this.status = status;
        this.time  = System.currentTimeMillis();
    }

    public static void main(String[] args) {

        System.out.println(TaskerStatus.Stun.getTime());
        System.out.println(TaskerStatus.Stun.getStatus());

    }
}

