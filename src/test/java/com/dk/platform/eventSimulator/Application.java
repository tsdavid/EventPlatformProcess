package com.dk.platform.eventSimulator;


import com.dk.platform.ems.util.EmsUtil;
import com.tibco.tibjms.admin.TibjmsAdminException;
import lombok.extern.slf4j.Slf4j;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import java.util.Arrays;
import java.util.Vector;

/**
 * This is a Message Generator(Producer) for test Event Manager and Event Tasker.
 *
 * Usage: java Application [options]
 *
 *      -qcount         <num queue>             Number of Queue. Default is 1.
 *      -count          <num msgs>              Number of Messages to send per queue. Default is 10k.
 *      -time           <seconds>               Delay Time to Send Message. Default is 0. Send Message immediately.
 *      -delivery       <mode>                  Delivery mode. Default is NON_PERSISTENT.  Other values : PERSISTENT and RELIABLE.
 *      -thread         <num threads>           Number of message producer threads. Default is 1.
 *      -type           <msg type>              Message Type will be written in message body.
 *      -random         <queue name Randomly>   Enable Create Queue with Random Name.
 */
@Slf4j
public class Application implements Runnable{

    static {
        try {
            log.info("" +
                    "" +
                    "=========================================================\n" +
                    "=========================================================\n" +
                    "======         ==========================================\n" +
                    "=========================================================\n" +
                    "=========================================================\n" +
                    "=========================================================\n" +
                    "Application Event Simulator Static Block\n"+
                    "=========================================================\n" +
                    "=========================================================\n" +
                    "======         ==========================================\n" +
                    "=========================================================\n" +
                    "=========================================================\n" +
                    "=========================================================\n" +
                    "");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private int qcount = 1;
    private int count = 10000;
    private int time = 0;
    private int delivery = DeliveryMode.PERSISTENT;
    private int thread = 1;
    private String type = "";
    private boolean random = false;

    private Vector<Thread> threadVector = null;
    private EmsUtil  emsUtil;

    public Application(String[] args){

        try {
            this.emsUtil = new EmsUtil();
        } catch (TibjmsAdminException e) {
            e.printStackTrace();
        }

        getArgs(args);

        // Create Thread run Send Process.
        // Application -> getArgs -> Run Producer -> Get Message


    }



    /**
     * Run Producer Thread.
     * @param qcount
     * @param count
     * @param deliveryMode
     * @param thread
     * @param MessageType
     * @param NameRandom
     */
    private void runProducer(int qcount, int count, int deliveryMode,
                             int thread, String MessageType, boolean NameRandom){

        // Create Vector for threads.
        threadVector = new Vector<>(thread);


        // Run Producer Class.
        // TODO Run Process
        // Thread => Class

    }

    /**
     *
     * @param type
     * @return
     */
    private String getMessage(String type, int number){

        // TODO Message Generator.
        return "";
    }







    /*-----------------------------------------------------------------------
     * getArgs
     *----------------------------------------------------------------------*/

    /**
     * Thread, thr producer run method.
     *
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {


    }

    /**
     *
     * @param QueueName
     * @param Message
     * @throws JMSException
     */
    private void sendEmsMessage(String QueueName, String Message) throws JMSException {

        String queueName = QueueName;
        String message = Message;
        this.emsUtil.sendSyncQueueMessage(QueueName, Message, null);
    }

    private void getArgs(String[] args){
        int i=0;

        log.info(Arrays.toString(args));
        log.info( args[i]);

        while (i < args.length){
            if (args[i].compareTo("-qcount")==0){

                if ((i+1) >= args.length) usage();
                qcount = Integer.parseInt(args[i+1]);
                i += 2;
            }
            else if (args[i].compareTo("-count")==0){
                if ((i+1) >= args.length) usage();
                count = Integer.parseInt(args[i+1]);
                i += 2;
            }
            else if (args[i].compareTo("-time") == 0){
                if ((i+1) >= args.length) usage();
                time = Integer.parseInt(args[i+1]);
                i += 2;
            }
            else if (args[i].compareTo("-delivery") == 0){
                if ((i+1) >= args.length) usage();
                delivery = Integer.parseInt(args[i+1]);
                i += 2;
            }
            else if (args[i].compareTo("-thread") == 0){
                if ((i+1) >= args.length) usage();
                thread = Integer.parseInt(args[i+1]);
                i += 2;
            }
            else if (args[i].compareTo("-random")==0){
                i += 1;
                random = true;
            }else {
                System.err.println("Unrecognized parameter: "+args[i]);
                usage();
            }
        }

    }

    private void usage(){
        System.err.println("\nUsage: java Application [options]");
        System.err.println("\nUsage: java Application qcount 1 count 1 ... ");
        System.err.println("\n");
        System.err.println("   where options are:");
        System.err.println("");
        System.err.println("   -qcount         <num queue>             Number of Queue. Default is 1.");
        System.err.println("   -count          <num msgs>              Number of Messages to send per queue. Default is 10k.");
        System.err.println("   -time           <seconds>               Delay Time to Send Message. Default is 0. Send Message immediately.");
        System.err.println("   -delivery       <mode>                  Delivery mode. Default is NON_PERSISTENT.  Other values : PERSISTENT and RELIABLE.");
        System.err.println("   -type           <msg type>              Message Type will be written in message body.");
        System.err.println("   -thread         <num threads>           Number of message producer threads. Default is 1.");
        System.err.println("   -random         <queue name Randomly>   Enable Create Queue with Random Name.");

        System.exit(0);
    }

    @Override
    public String toString() {
        return "Application{" +
                "qcount=" + qcount +
                ", count=" + count +
                ", time=" + time +
                ", delivery=" + delivery +
                ", thread=" + thread +
                ", type='" + type + '\'' +
                ", random=" + random +
                '}';
    }

    public static void main(String[] args) {

        Application application = new Application(args);
        System.out.println(
                application.toString()
        );

    }
}
