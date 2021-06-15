package com.dk.platform.eventManager.util;

import com.dk.platform.ems.ConnConf;
import com.dk.platform.ems.util.EmsUtil;
import com.tibco.tibjms.admin.TibjmsAdminException;
import org.junit.After;
import org.junit.Test;

import javax.jms.JMSException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchTest {

    private EmsUtil emsUtil;

    void setEmsUtil() {
        try {
            this.emsUtil = new EmsUtil(ConnConf.EMS_URL.getValue(), ConnConf.EMS_USR.getValue(), ConnConf.EMS_PWD.getValue());
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }
    }
    @After
    public void DestroyQueue(){
        emsUtil.destroyQueues(ConnConf.EMS_WRK_PREFIX.getValue());
    }


    @Test
    public void GetDeActiveWrkQueue(){
        // prepare Queue.
        this.setEmsUtil();
        int q_count = 10;
        for(int i=0; i < q_count; i++){

            String destName = ConnConf.EMS_WRK_PREFIX.getValue().concat(String.valueOf(i));
            try {
                emsUtil.sendAsyncQueueMessage(destName, "", null);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }


        // then
        String[] arr = emsUtil.getAct_or_DeAct_QueueNames(ConnConf.EMS_WRK_PREFIX.getValue(), 0);
        System.out.println(Arrays.toString(arr));

        assertThat(q_count).isEqualTo(arr.length);
    }


    @Test
    public void GetWRKQueueDEACTIVE_PENDINGCase() {

        // prepare Queue.
        this.setEmsUtil();

        int threshold = 5;
        int q_count = 10;
        int[] pendingIndex = {3, 5, 7};

        for(int i=0; i < q_count; i++){

            String destName = ConnConf.EMS_WRK_PREFIX.getValue().concat(String.valueOf(i));
            // check pending index.
            if(Arrays.binarySearch(pendingIndex, i) >= 0){
                for(int j=0; j < threshold+1; j++){
                    try {
                        emsUtil.sendAsyncQueueMessage(destName, "", null);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }

            }else{
                try {
                    emsUtil.sendAsyncQueueMessage(destName, "", null);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }

        // Then
        String[] arr = emsUtil.getDeActivewithPending(ConnConf.EMS_WRK_PREFIX.getValue(), threshold);
        System.out.println(Arrays.toString(arr));

        // reference https://cornswrold.tistory.com/300
        // all Match ....
        boolean result = Arrays.stream(arr).allMatch(a -> Arrays.binarySearch(pendingIndex, Integer.parseInt(a.split(ConnConf.EMS_WRK_PREFIX.getValue())[1])) >= 0);
        System.out.println("Test Result : " + result);
        assertThat(true).isEqualTo(result);

    }
}
