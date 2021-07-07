package com.dk.platform.eventManager.process;

import com.dk.platform.Process;
import com.dk.platform.ems.AppPro;
import com.dk.platform.ems.util.EmsUtil;
import com.dk.platform.eventManager.util.ManagerUtil;
import com.dk.platform.eventManager.util.MemoryStorage;
import com.tibco.tibjms.admin.TibjmsAdminException;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

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
@Slf4j
public class InitializeProcess implements Process {


    /*****************************************************************************************
     **************************************  Logger ******************************************
     ****************************************************************************************/



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

        /*
         ***********************************  Variables ******************************************
         ****************************************************************************************/
        MemoryStorage memoryStorage = MemoryStorage.getInstance();

        try {
            this.emsUtil = new EmsUtil(this.emsServerUrl, this.emsUserName, this.emsPassword);
            memoryStorage.setEmsUtil(this.emsUtil);

        } catch (TibjmsAdminException e) {

            log.error("[{}] Cannot Get EmsUtil Instance. Exit System. Error : {}/{}.","ManagerInitialize", e.getMessage(), e.toString());
            e.printStackTrace();
            System.exit(1);
        }

        this.managerUtil = new ManagerUtil();
        memoryStorage.setManagerUtil(this.managerUtil);


    }


    @Override
    public void execute() {

        // Scan Ems
        this.scanEmsServer();

    }


    /*
     ***********************************  Scan Logic *****************************************
     *****************************************************************************************/


    /**
     * Assign DeActive WRK Q to Active TSK.
     */
    private void scanEmsServer() {

        String[] deactiveWrkQs = this.scanDeActiveWorkQueue();
        log.info(" deactiveWorkQueue List : {}", Arrays.toString(deactiveWrkQs));

        // if tasker is in Map.
        String assignableTasker = managerUtil.findIdleTasker();

        //TODO THINK BETTER ==> Recevier 가 기동 전이라 Tasker가 항상 없을거 같다.
        //  처음에 뜰때, Tasker 정보도 얻고 바로 queue를 할당 해줘야 할거 같음. Tasker Health Check Message 기다리기엔..
        // ==> execute method를 Receiver 다 뜬 후에 설정하는 것으로 해결
        for(String Wq : deactiveWrkQs){
            if(assignableTasker != null) {
                managerUtil.assignWrkQtoTSK(Wq, assignableTasker); // Assign De-Active WRK Q to Tasker.
                log.info("Tasker is Already in Map.  Here is Assignable Tasker : {}", assignableTasker);


            }else{   // IF No Tasker in Map. Save in Tmp Set.
                managerUtil.saveWorkQueueToTMP(Wq);
                log.info("No Tasker in Map. Send to Temporary Work Queue : {}.", Wq);
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
