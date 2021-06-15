package com.dk.platform.eventManager.rebalance;

import org.junit.Test;

public class RebalanceTest {

    @Test
    /**
     * TEST SCENARIO
     * 1. One or Two Tasker Work With WorkQueue.
     * 2. New Tasker is Active
     * 3. Manager Send request First and Second Tasker to distribute Work Queue.
     * 4. First and Second Tasker Return WorkQueue Back to Manager
     * 5. Manager Assign Work Queue to lowest Tasker.
     */
    public void DistributeWRK_Queue_Test(){

        // 1. Generate Work Queue with lots of Message.
        // 2. Run Tasker and Assign WRK to them.
    }
}
