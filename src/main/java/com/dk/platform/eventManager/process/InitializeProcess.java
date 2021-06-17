package com.dk.platform.eventManager.process;

import com.dk.platform.Process;
import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.util.ManagerUtil;
import com.dk.platform.eventManager.util.MemoryStorage;
import com.tibco.tibjms.admin.TibjmsAdminException;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Job
 * 1. Manager Application Initialize.
 *
 *
 * Manager Initialize
 * Search Work Queue and TSK on EMS Server. => put WRK.Q to Q Map, TSK.Q to Q Map
 * If> WRK.Q and TSK is exist, assign to TSK
 * IF> WRK.Q exist, just store it. store to tmp~~
 *
 *
 */
@NoArgsConstructor
public class InitializeProcess implements Process {


    /*****************************************************************************************
     **************************************  Logger ******************************************
     ****************************************************************************************/

    private static final Logger logger = LoggerFactory.getLogger(InitializeProcess.class);


    /*****************************************************************************************
     ***********************************  Variables ******************************************
     ****************************************************************************************/

    private MemoryStorage memoryStorage;

    private EmsUtil emsUtil;

    private ManagerUtil managerUtil;

    private String emsServerUrl;

    private String emsUserName;

    private String emsPassword;


    /*****************************************************************************************
     ***********************************  Constructor ****************************************
     ****************************************************************************************/


    @Builder
    public InitializeProcess(String emsServerUrl, String emsUserName, String emsPassword){

        this.emsServerUrl = emsServerUrl;

        this.emsUserName = emsUserName;

        this.emsPassword = emsPassword;

    }


    @Override
    public void setUpInstance() {

        this.memoryStorage = MemoryStorage.getInstance();

        try {
            this.emsUtil = new EmsUtil(this.emsServerUrl, this.emsUserName, this.emsPassword);
            this.memoryStorage.setEmsUtil(this.emsUtil);

        } catch (TibjmsAdminException e) {

            logger.error("[{}] Cannot Get EmsUtil Instance. Exit System. Error : {}/{}.","ManagerInitialize", e.getMessage(), e.toString());
            e.printStackTrace();
            System.exit(1);
        }

        this.managerUtil = new ManagerUtil();
        this.memoryStorage.setManagerUtil(this.managerUtil);


    }


    @Override
    public void execute() {

        // Scan Ems
        this.scanEmsServer();

    }


    /*****************************************************************************************
     ***********************************  Scan Logic *****************************************
     *****************************************************************************************/


    /**
     * Assign DeActive WRK Q to Active TSK.
     */
    private void scanEmsServer() {

        String[] deactiveWrkQs = this.scanDeActiveWorkQueue();

        // if tasker is in Map.
        String assignableTasker = managerUtil.findIdleTasker();
        for(String Wq : deactiveWrkQs){
            if(assignableTasker != null) managerUtil.assignWrkQtoTSK(Wq, assignableTasker); // Assign De-Active WRK Q to Tasker.
            else{   // IF No Tasker in Map. Save in Tmp Set.
                managerUtil.saveWorkQueueToTMP(Wq);
            }
        }
    }


    private String[] scanDeActiveWorkQueue() {
        return emsUtil.getAct_or_DeAct_QueueNames(AppPro.EMS_WRK_PREFIX.getValue(), 0);
    }



    public static void main(String[] args) {
        try {

            EmsUtil emsUtil = new EmsUtil(AppPro.EMS_URL.getValue(), AppPro.EMS_USR.getValue(), AppPro.EMS_PWD.getValue());
//            String[] arr = emsUtil.getActivewithPending(ConnConf.EMS_WRK_PREFIX.getValue(), 5);

//            System.out.println(
//                    Arrays.toString(arr)
//            );
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }

    }


}
