package com.dk.platform;

import com.tibco.tibjms.admin.TibjmsAdminException;

public interface Process {


    /**
     * Use Instantiate Essential Class.
     * 0. Thread Name
     * 1. MemoryStorage
     * 2. EmsUtil
     * 3. Util
     * 4. (Optional) ProcessName.
     * 5. Create Ems Connection from Receiver Interface.
     */
    void setUpInstance() throws TibjmsAdminException;

    /**
     * run Logic
     * if runnable case, run() {
     *     execute() {
     *         Logic.
     *     }
     * }
     */
    void execute();

}

