package com.dk.platform.eventTasker;

import com.dk.platform.eventTasker.process.Initialize;
import com.dk.platform.eventTasker.process.ReceiverProcess;
import com.dk.platform.eventTasker.util.TaskerUtil;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * EMS Connection 관리를 위해
 * EMS Connection이 필요로 하는 EMS Util, Tasker or Manager Util을 Application 단위에서 init 한다.
 * 나머지 process는 application에서 설정된 util만을 사용한다.
 */
public class Application implements com.dk.platform.Application {

    private Initialize initialize;

    @Override
    public void initialize() {
        this.initialize = new Initialize();

    }

    public Application() {

        this.initialize();
        if(this.initialize == null){
            this.initialize = new Initialize();
        }

        String MyName = this.initialize.getProcessName();
        System.out.println(MyName);

        // Run all Process.
        try {
            new ReceiverProcess(MyName, Session.AUTO_ACKNOWLEDGE, false).run();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new Application();

    }
}
