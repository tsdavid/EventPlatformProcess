package com.dk.platform.eventTasker.util;

import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventTasker.vo.QueueVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Main Job
 * 1. Singleton
 * 2. Data Structure
 */
public class MemoryStorage {


    /*
     **************************************  Singleton ***************************************
     ****************************************************************************************/

    private static class SingletonHelper {
        private static final MemoryStorage INSTANCE = new MemoryStorage();
    }

    public static MemoryStorage getInstance() {
        return SingletonHelper.INSTANCE;
    }


    /*
     **************************************  Logger ******************************************
     ****************************************************************************************/


    private static final Logger logger = LoggerFactory.getLogger(MemoryStorage.class);


    /*
     ***********************************  Variables ******************************************
     ****************************************************************************************/

    private String PROCESS_NAME;

    private EmsUtil emsUtil;

    private TaskerUtil taskerUtil;

    /**
     * Managing Queue in Map.
     *
     * Key : Queue Name.
     * Value : QueueVO.
     */
    private ConcurrentHashMap<String, QueueVO> QueueMap = null;


    /*
     ***********************************  Constructor ****************************************
     ****************************************************************************************/


    private MemoryStorage(){

        logger.info("Memory Storage Instantiate.");
    }


    /*
     ***********************************  Logic **********************************************
     *****************************************************************************************/
    // TODO THINK BETTER  ==> Any Logic ?.


    /*
     ***********************************  Getter *********************************************
     ****************************************************************************************/


    /**
     *
     * @return          :       Process Name.
     */
    public String getPROCESS_NAME(){
        return PROCESS_NAME;
    }


    /**
     *
     * @return          :       ConcurrentHashMap managing QueueVo.
     */
    public ConcurrentHashMap<String, QueueVO> getQueueMap() {
        if(this.QueueMap == null){
            this.QueueMap = new ConcurrentHashMap<>();
        }

        return this.QueueMap;
    }


    /**
     *
     * @return      :       {@link EmsUtil}
     */
    public EmsUtil getEmsUtil() {
        return emsUtil;
    }


    /**
     *
     * @return      :       {@link TaskerUtil}
     */
    public TaskerUtil getTaskerUtil() {
        return taskerUtil;
    }


    /*
     ***********************************  Setter *********************************************
     ****************************************************************************************/

    /**
     *
     * @param name          :       Process Name.
     */
    public void setPROCESS_NAME(String name){
        this.PROCESS_NAME = name;
    }


    /**
     *
     * @param taskerUtil        :       {@link TaskerUtil}
     */
    public void setTaskerUtil(TaskerUtil taskerUtil) {
        this.taskerUtil = taskerUtil;
    }


    /**
     *
     * @param emsUtil           :       {@link EmsUtil}
     */
    public void setEmsUtil(EmsUtil emsUtil) {
        this.emsUtil = emsUtil;
    }


    /*
     *************************************  Main *********************************************
     ****************************************************************************************/
    public static void main(String[] args) {

    }
}
