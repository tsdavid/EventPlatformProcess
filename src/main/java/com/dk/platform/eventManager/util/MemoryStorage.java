package com.dk.platform.eventManager.util;

import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.vo.TaskerVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main Job
 * 1. Singleton
 * 2. Data Structure
 */
public class MemoryStorage {


    /*****************************************************************************************
     **************************************  Singleton ***************************************
     ****************************************************************************************/


    private static class SingletonHelper {
        private static final MemoryStorage INSTANCE = new MemoryStorage();
    }


    public static MemoryStorage getInstance() {
        return SingletonHelper.INSTANCE;
    }


    /*****************************************************************************************
     **************************************  Logger ******************************************
     ****************************************************************************************/

    private static final Logger logger = LoggerFactory.getLogger(MemoryStorage.class);


    /*****************************************************************************************
     ***********************************  Variables ******************************************
     ****************************************************************************************/


    private EmsUtil emsUtil;


    private ManagerUtil managerUtil;


    /**
     * TASKER MANAGE MAP.
     * Key : TASKER NAME
     * VALUE : TASKER VO.
     */
    private ConcurrentHashMap<String, TaskerVO> Tasker_Mng_Map;


    /**
     * Temporary Work Queue Set.
     * QueueName
     */
    private HashSet<String> Tmp_WRK_Set;


    /*****************************************************************************************
     ***********************************  Constructor ****************************************
     ****************************************************************************************/


    private MemoryStorage(){}


    /*****************************************************************************************
     ***********************************  Logic **********************************************
     *****************************************************************************************/
    // TODO THINK BETTER  ==> Any Logic ?.




    /*****************************************************************************************
     ***********************************  Getter *********************************************
     ****************************************************************************************/


    /**
     *
     * @return          :       HashMap Managing Tasker VO.
     */
    public ConcurrentHashMap<String, TaskerVO> getTasker_Mng_Map() {

        if(Tasker_Mng_Map == null){
            Tasker_Mng_Map = new ConcurrentHashMap<String, TaskerVO>();
        }
        return Tasker_Mng_Map;
    }


    /**
     *
     * @return
     */
    public HashSet<String> getTmp_WRK_Queue() {

        if(Tmp_WRK_Set == null){
            this.Tmp_WRK_Set = new HashSet<>();
        }
        return Tmp_WRK_Set;
    }


    /**
     *
     * @return          :       {@link EmsUtil}
     */
    public EmsUtil getEmsUtil() {
        return emsUtil;
    }


    /**
     *
     * @return          :       {@link ManagerUtil}
     */
    public ManagerUtil getManagerUtil() {
        return managerUtil;
    }


    /*****************************************************************************************
     ***********************************  Setter *********************************************
     ****************************************************************************************/

    /**
     *
     * @param emsUtil           :       {@link EmsUtil}
     */
    public void setEmsUtil(EmsUtil emsUtil) {
        this.emsUtil = emsUtil;
    }


    /**
     *
     * @param managerUtil       :       {@link ManagerUtil}
     */
    public void setManagerUtil(ManagerUtil managerUtil) {
        this.managerUtil = managerUtil;
    }



    /*****************************************************************************************
     *************************************  Deprecated ***************************************
     ****************************************************************************************/


    /**
     * when Receive Health Check Message.
     * Tasker VO  update Time is updated.
     * @param name
     */
    @Deprecated
    public void updateTaskerHealthCheckTime(String name, Timestamp timestamp){
        if(Tasker_Mng_Map == null){
            return;
        }
        TaskerVO vo = Tasker_Mng_Map.get(name);
        vo.setUpdated_Time(timestamp);
    }


    /*****************************************************************************************
     *************************************  Main *********************************************
     ****************************************************************************************/


    public static void main(String[] args) {

    }

}
