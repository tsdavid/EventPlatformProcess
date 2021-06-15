package com.dk.platform.eventTasker.util;

import com.dk.platform.backup.SingleTone;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventTasker.vo.QueueVO;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton
 */
public class MemoryStorage {

    private MemoryStorage(){}

    private static class SingletonHelper {
        private static final MemoryStorage INSTANCE = new MemoryStorage();
    }

    public static MemoryStorage getInstance() {
        return MemoryStorage.SingletonHelper.INSTANCE;
    }


    private String PROCESS_NAME;

    private EmsUtil emsUtil;

    private TaskerUtil taskerUtil;

    private ConcurrentHashMap<String, QueueVO> QueueMap = null;


    /**
     * Getter
     */
    public String getPROCESS_NAME(){
        return PROCESS_NAME;
    }

    public ConcurrentHashMap<String, QueueVO> getQueueMap() {
        if(QueueMap == null){
            QueueMap = new ConcurrentHashMap<String, QueueVO>();
        }
        return QueueMap;
    }

    public EmsUtil getEmsUtil() {
        return emsUtil;
    }

    public TaskerUtil getTaskerUtil() {
        return taskerUtil;
    }


    /**
     * Setter
     */
    public void setPROCESS_NAME(String name){
        this.PROCESS_NAME = name;
    }

    public void setTaskerUtil(TaskerUtil taskerUtil) {
        this.taskerUtil = taskerUtil;
    }

    public void setEmsUtil(EmsUtil emsUtil) {
        this.emsUtil = emsUtil;
    }
}
