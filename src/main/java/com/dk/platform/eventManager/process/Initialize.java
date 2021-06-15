package com.dk.platform.eventManager.process;

import com.dk.platform.ems.ConnConf;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.util.ManagerUtil;
import com.dk.platform.eventManager.util.MemoryStorage;
import com.tibco.tibjms.admin.TibjmsAdminException;

import java.util.Arrays;

/**
 * Manager Initialize
 * Search Work Queue and TSK on EMS Server. => put WRK.Q to Q Map, TSK.Q to Q Map
 * If> WRK.Q and TSK is exist, assign to TSK
 * IF> WRK.Q exist, just store it. store to tmp~~
 *
 *
 */
public class Initialize {

    private EmsUtil emsUtil;
    private ManagerUtil managerUtil;

    public Initialize() {

        // Set-Up Essential Instance.
        try{
            this.emsUtil = new EmsUtil(ConnConf.EMS_URL.getValue(), ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue());
            this.managerUtil = new ManagerUtil();

        }catch (Exception e){
            e.printStackTrace();
        }

        // Store to Memory Storage
        MemoryStorage.getInstance().setEmsUtil(this.emsUtil);
        MemoryStorage.getInstance().setManagerUtil(this.managerUtil);

        // Scan Ems
        this.scanEmsServer();
    }

    /**
     * Assign DeActive WRK Q to Active TSK.
     */
    private void scanEmsServer() {

        String[] deactiveWrkQs = this.scanDeActiveWorkQueue();

        // if tasker is in Map.
        boolean assignable = MemoryStorage.getInstance().getTasker_Mng_Map().size() > 0;
        for(String Wq : deactiveWrkQs){
            if(assignable) managerUtil.assignWrkQtoTSK(Wq); // // Assign De-Active WRK Q to Tasker.
            else{   // IF No Tasker in Map. Save in Tmp Set.
                MemoryStorage.getInstance().getTmp_WRK_Queue().add(Wq);
            }
        }
    }


    private String[] scanDeActiveWorkQueue() {
        return emsUtil.getAct_or_DeAct_QueueNames(ConnConf.EMS_WRK_PREFIX.getValue(), 0);
    }



    public static void main(String[] args) {
        try {

            EmsUtil emsUtil = new EmsUtil(ConnConf.EMS_URL.getValue(), ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue());
//            String[] arr = emsUtil.getActivewithPending(ConnConf.EMS_WRK_PREFIX.getValue(), 5);

//            System.out.println(
//                    Arrays.toString(arr)
//            );
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }

    }


}
