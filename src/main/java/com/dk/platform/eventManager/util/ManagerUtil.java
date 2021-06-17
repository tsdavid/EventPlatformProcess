package com.dk.platform.eventManager.util;


import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.vo.TaskerVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Main Job
 * 1. TASKER HEALTH CHECK Logic
 * 2. Assign Work Logic
 * 3. Re-Balance Logic
 */
public class ManagerUtil {

    /*****************************************************************************************
     **************************************  Logger ******************************************
     ****************************************************************************************/


    private static final Logger logger = LoggerFactory.getLogger(ManagerUtil.class);


    /*****************************************************************************************
     ***********************************  Variables ******************************************
     *****************************************************************************************/


    /**
     * SingleTone Process Shared Storage.
     */
    private MemoryStorage memoryStorage;


    /**
     * EMS Util Instance.
     */
    private EmsUtil emsUtil;


    /**
     * Data Structure contain TaskerVO
     * TASKER MANAGE MAP.
     * Key : TASKER NAME
     * VALUE : TASKER VO.
     */
    private ConcurrentHashMap<String, TaskerVO> Tasker_Mng_Map;


    /**
     * Temporary Work Queue Container.
     * QueueName
     */
    private HashSet<String> Tmp_WRK_Queue;


    /*****************************************************************************************
     ********************************  Message Properties ************************************
     ****************************************************************************************/


    /**
     * Pre Set-up EMS Message Header Properties about Rebalance Message.
     */
    private Map<String, String> RebalanceProperties;


    /**
     * Pre Set-up EMS Message Header Properties about Assign Task.
     */
    private Map<String, String> AssignTskProperties;



    /*****************************************************************************************
     ***********************************  Constructor ****************************************
     *****************************************************************************************/


    /**
     * Constructor.
     */
    public ManagerUtil(){

        this.setUpInstance();

        // Set-Up Assign Tsk Properties.
        this.setUpMessageProperties();

    }


    /**
     * Test Version.
     * Constructor.
     */
    public ManagerUtil(EmsUtil emsUtil){

        this.setUpInstance(emsUtil);

        // Set-Up Assign Tsk Properties.
        this.setUpMessageProperties();

    }


    /**
     * Essential Instance
     */
    private void setUpInstance(){

        this.memoryStorage = MemoryStorage.getInstance();
        this.Tasker_Mng_Map = this.memoryStorage.getTasker_Mng_Map();
        this.Tmp_WRK_Queue = this.memoryStorage.getTmp_WRK_Queue();

        this.emsUtil = memoryStorage.getEmsUtil();
    }


    /**
     * Test Version.
     * Essential Instance
     */
    private void setUpInstance(EmsUtil emsUtil){

        this.memoryStorage = MemoryStorage.getInstance();
        this.Tasker_Mng_Map = this.memoryStorage.getTasker_Mng_Map();
        this.Tmp_WRK_Queue = this.memoryStorage.getTmp_WRK_Queue();

        this.emsUtil = emsUtil;
    }


    /**
     * Set-Up Main Message Properties.
     */
    private void setUpMessageProperties() {

        // Set-Up Assign Tsk Properties.
        this.AssignTskProperties = new HashMap<>(); this.RebalanceProperties = new HashMap<>();
        this.AssignTskProperties.put(AppPro.MSG_TYPE.getValue(), AppPro.MNG_ASG_WRK_VAL.getValue());
        this.RebalanceProperties.put(AppPro.MSG_TYPE.getValue(), AppPro.MNG_RBL_CNT_TSK_VAL.getValue());

    }


    /*****************************************************************************************
     ********************************  TASKER HEALTH CHECK Logic *****************************
     *****************************************************************************************/


    /**
     * TASKER VO Updated when Receive Health Check Message.
     * Manager Receive Tasker's Health Check Message body is consist of Working Queues.
     * @param TaskerName            :       Tasker who send this Message.
     * @param workQueueList         :       Queue List the tasker own.
     * @param count                 :       Count of Work Queue List.
     */
    // TODO TEST REQUIRED.
    public void updateTaskerHealthCheck(String TaskerName, String workQueueList, int count){

        if(Tasker_Mng_Map == null){
            Tasker_Mng_Map = memoryStorage.getTasker_Mng_Map();
        }

        TaskerVO vo = Tasker_Mng_Map.get(TaskerName);
        if(vo != null){
            vo.setUpdated_Time(new Timestamp(System.currentTimeMillis()));
            vo.setCount(count);
            vo.setWorkQueueList(workQueueList);

        }else{

            this.setUpTasker(TaskerName, workQueueList, count);
        }

        logger.debug("[{}] Tasker Name : {}, Count : {}, Print TaskerVo Map : {}",
                "HLT_CHCK",TaskerName, count, vo.toString());

    }


    /*****************************************************************************************
     ********************************  Assign Work Logic *************************************
     *****************************************************************************************/


    /**
     * Create TaskerVO and Put in Managing Map.
     * Set-Up Tasker if tasker absent on map when receive Health Check Message.
     * @param TaskerName            :       Tasker Name.
     * @param workQueueList         :       Queue List Owned by Tasker.
     * @param count                 :       Count of Work Queue List.
     */
    private void setUpTasker(String TaskerName, String workQueueList, int count){

        try{
            TaskerVO taskerVO = TaskerVO.builder().taskerName(TaskerName)
                    .created_Time(new Timestamp(System.currentTimeMillis()))
                    .updated_Time(new Timestamp(System.currentTimeMillis()))
                    .workQueueList(workQueueList)
                    .cnt(count)
                    .build();

            memoryStorage.getTasker_Mng_Map().put(TaskerName, taskerVO);

        }catch (Exception e){
            logger.error("[{}] Error : {}/{}.","AssignWRK", e.getMessage(), e.toString());
            e.printStackTrace();
        }

    }


    /**
     * assign Work Queue to Tasker Queue by Sending EMS Message.
     * @param WorkQueueName             :           Work Queue Name.
     */
    // TODO TEST REQUIRED.
    public void assignWrkQtoTSK(String WorkQueueName, String Tasker){

        if(Tasker != null){
            try {
                this.sendAssignWrkMessage(Tasker, WorkQueueName);

            } catch (JMSException e) {

                logger.error("[{}] Error : {}/{}.","AssignWRK", e.getMessage(), e.toString());
                e.printStackTrace();
            }

        // No Tasker to Assign
        }else {
            // Save Work Queue to TMP Queue Storage.
            this.saveWorkQueueToTMP(WorkQueueName);
        }

    }


    /**
     * Find Tasker who have the least queues.
     * Compare with Queue Count.
     * @return          :       Tasker who have the least queues.
     */
    // TODO TEST REQUIRED.
    public String findIdleTasker(){

        int min = 32768;    // maximum of int range.
        String tasker = null;
        // TODO THINK BETTER.
        for(Map.Entry<String, TaskerVO> vo : memoryStorage.getTasker_Mng_Map().entrySet()){

            if(vo.getValue().getCount() < min){         // find the least count.
                min = vo.getValue().getCount();         // update if the least one
                tasker = vo.getValue().getTaskerName(); // update the tasker name.
            }
        }
        return tasker;
    }


    /**
     * IF No Tasker to Assign, Save in TMP Set.
     * @param WorkQueueName     :       Work Queue has no Assignable tasker.
     */
    // TODO TEST REQUIRED.
    public void saveWorkQueueToTMP(String WorkQueueName){

        try{
            memoryStorage.getTmp_WRK_Queue().add(WorkQueueName);

        }catch (Exception e){
            logger.error("[{}] Error : {}/{}.","AssignWRK", e.getMessage(), e.toString());
            e.printStackTrace();
        }
    }



    /**
     * Send to Tasker. to assign Work queue in TMP Set
     * @param TskName       :       Tasker will be assigned Work Queue From TMP Set.
     */
    // TODO TEST REQUIRED.
    public void executeTmpQueue(String TskName){

        if(Tmp_WRK_Queue.size() > 0){
            Tmp_WRK_Queue.stream().forEach(wrkQ -> {
                try {

                    this.sendAssignWrkMessage(TskName, wrkQ);

                } catch (JMSException e) {
                    logger.error("[{}] Error : {}/{}.","AssignWRK", e.getMessage(), e.toString());
                    e.printStackTrace();
                }
            });
        }

        // Clean Up TMP Set
        Tmp_WRK_Queue.clear();

        logger.info("[{}] TMP Set has been cleaned. Send TMP Work to : {}", "AssignWRK", TskName);

    }


    /**
     * Send Assign Message to Tasker.
     * @param TskName       :       Tasker Name.
     * @param WrkQueue      :       Workr Queue.
     * @throws JMSException :       JMSException
     */
    private void sendAssignWrkMessage(String TskName, String WrkQueue) throws JMSException {

        emsUtil.sendSafeQueueMessage(TskName, WrkQueue, this.AssignTskProperties);
    }


    /*****************************************************************************************
     ***********************************  Re-Balance Logic ************************************
     *****************************************************************************************/


    /**
     * Send Message threshold to Origin-Tasker.
     * @param newTaskerName         :       New Created Tasker Name.
     */
    public void doRebalanceCase(String newTaskerName) throws JMSException {

        // get threshold
        int threshold = this.getAvgWorkQueueOwnedByTSK();

        // send Message to o-tasker.
        for(String tsk : this.searchO_Tasker(newTaskerName)){
            this.sendRebalanceMessage(tsk, threshold);
            logger.info("[{}] Manager Send Re-Balance Request to {}.  Request Threshold : {}", "ReBalance", tsk, threshold);
        }

    }


    /**
     * Re-Balance Case.
     * O-tasker count = tasker Count except new tasker
     * 1. O-tasker Count except new tasker more than >= 1   => isTskCntEnough?
     * 2. Total WRK Count more than > O-tasker * 2          => isWrkCntEnough?
     * 3. TmpQueue Size < Avg of Wrk in Tasker              => isTmpQueueLess?
     * @param newTaskerName       :       New Created Tasker Name.
     */
    public boolean isRebalanceCase(String newTaskerName) {

        int o_tasker_size = this.searchO_Tasker(newTaskerName).length;
        return isTskCntEnough(o_tasker_size) && isWrkCntEnough(o_tasker_size) && isTmpQueueLess(newTaskerName);
    }

    /**
     * O-tasker Count except new tasker more than >= 1   => isTskCntEnough?
     * @param o_tasker_size         :       Count of Tasker, Tasker not contained New Created Tasker.
     * @return                      :       True : O-Tasker more, equal than 1?
     */
    private boolean isTskCntEnough(int o_tasker_size) {

        return o_tasker_size >= 1;
    }


    /**
     * 2. Total WRK Count more than > O-tasker * 2          => isWrkCntEnough?
     * @param o_tasker_size         :       Count of Tasker, Tasker not contained New Created Tasker.
     * @return                      :       True : Total Work Queue Size more than 2 * O-Tasker Size
     */
    private boolean isWrkCntEnough(int o_tasker_size) {

        return this.searchWorkQueue().length > o_tasker_size * 2;
    }


    /**
     * 3. TmpQueue Size < Avg of Wrk in Tasker              => isTmpQueueLess?
     * @param newTaskerName       :       New Created Tasker Name.
     * @return                    :       True : Threshold is bigger than New Tasker Name's queue count.
     */
    private boolean isTmpQueueLess(String newTaskerName) {

        int assignedWrkCntToNewTasker = memoryStorage.getTasker_Mng_Map().get(newTaskerName).getCount();
        int toBeThreshold = this.getAvgWorkQueueOwnedByTSK();

        return assignedWrkCntToNewTasker < toBeThreshold;  // new tasker's job is smaller than threshold
    }


    /**
     * Get Active Taskers on EMS Server.
     * But  except New Tasker.
     * @param newTaskerName       :       New Created Tasker Name.
     * @return                    :       String array with O-Tasker Name.
     */
    //TODO Need to Test.
    public String[] searchO_Tasker(String newTaskerName){

        String[] currentActiveTaskers =  emsUtil.getAct_or_DeAct_QueueNames(AppPro.EMS_TSK_PREFIX.getValue(), 1);

        // if> currentActiveTaskers => only new Tasker, no o-tasker in server
        if(currentActiveTaskers.length <= 1){
            return null;

        }else{
            String[] o_taskers = new String[currentActiveTaskers.length -1];
            int i = 0;
            for (String tasker : currentActiveTaskers){
                if (tasker.equals(newTaskerName)) continue;

                // Check it is in Map.
                if(memoryStorage.getTasker_Mng_Map().containsKey(tasker)) o_taskers[i++] = tasker;
                // if not contain init in map.
                else this.setUpTasker(tasker, "", 0);
            }
            return o_taskers;
        }
    }


    /**
     * Get All Work Queue on EMS Server.
     * @return          :       String array with Work Queue.
     */
    //TODO Need to Test.
    public String[] searchWorkQueue(){

        return emsUtil.getAct_or_DeAct_QueueNames(AppPro.EMS_WRK_PREFIX.getValue(), -1);
    }


    /**
     * ToBe Threshold.
     * All Tasker get Average Work Queue
     * @return          :       Average Queue Count.
     */
    public int getAvgWorkQueueOwnedByTSK(){

        int taskerCnt  = memoryStorage.getTasker_Mng_Map().size();
        int sumCount = 0;
        for(Map.Entry<String, TaskerVO> tasker : memoryStorage.getTasker_Mng_Map().entrySet()){
            sumCount += tasker.getValue().getCount();
        }
        return sumCount / taskerCnt;
    }


    /**
     * Send EMS Message for Re-balance Message.
     * @param TskName           :       Tasker Name
     * @param cnt               :       Threshold. Requested Queue Count.
     * @throws JMSException     :       JMSException.
     */
    private void sendRebalanceMessage(String TskName, int cnt) throws JMSException {

        emsUtil.sendSafeQueueMessage(TskName, String.valueOf(cnt), this.RebalanceProperties);
    }


    /*****************************************************************************************
     *************************************  Deprecated ***************************************
     ****************************************************************************************/


    /**
     * when Receive Health Check Message.
     * Tasker VO  update Time is updated.
     * @param name          :           Takser Name.
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

        //TODO CASE TEST.
        // 1. Re-Balance Case.
        // 2. Health Check Case.
        // 3. Assign Queue Case.
    }



}
