import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Tzu-Chi Kuo on 2017/4/23.
 * ID: W1279858
 * PurcurrPose:
 *   1. read redirect file and open a client socket
 *   2. receive input data stream and feed into DGIM algorithm
 *   3. Base on query and display the answer
 *   4. 'End' to stop the program
 */

public class P2 {
    public static String hostName = null;
    public static int portNum = 0;
    public static Object lock = new Object();
    public static Object sync = new Object();
    public static Object forward = new Object();
    public static Object backward = new Object();
    public static long queryNum = 0;
    public static long startPos = 1;
    public static long currPos = 1; // record number of input data

    // main entry
    public static void main(String[] args) {
        // 0. initialize parameters
        int bitLen = 16;
        SplitDataStream datastreams = new SplitDataStream(bitLen);
        DGIM[] dgims = new DGIM[bitLen];

        // 1. read stdin with thread
        Thread stdin = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
                    String line = "";
                    String prefix = "What is the sum for last";
                    int cmdCnt = 0;
                    // wait 1 ms in order to invoke all threads
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e){
                        System.out.println("[Error] cannot make stdin thread sleep");
                    }
                    while((line = bReader.readLine()) != null) {
                        if (cmdCnt == 0) {
                            if (!line.contains(":")) {
                                System.out.println("[Error] host:port must be first command");
                                System.exit(0);
                            } else {
                                String[] cmd = line.trim().split(":");
                                String port = cmd[1];
                                for (int i = 0; i < port.length(); i++) {
                                    if (!Character.isDigit(port.charAt(i))) {
                                        System.out.println("[Error] host:port must be first command");
                                        System.exit(0);
                                    }
                                }
                                hostName = cmd[0];
                                portNum = Integer.valueOf(port);
                                synchronized (lock) {
                                    lock.notifyAll();
                                }
                                cmdCnt++;
                            }
                        } else {
                            if (line.contains("end")) {
                                //System.out.println("Exit P2 program. Thank you!");
                                System.exit(0);
                            }
                            // check query statement format
                            if (!line.startsWith(prefix) && !line.contains("integers")) {
                                System.out.print("[Error] query format error: " + line);
                                System.out.println(" (should be: " + prefix + " k integers)");
                                System.exit(0);
                            } else {
                                String[] query = line.substring(prefix.length()).trim().split(" ");
                                if (query != null) {
                                    queryNum = Long.valueOf(query[0]);
                                    System.out.println(line);
                                    startPos = currPos;
                                    // unitl current query execution is finished
                                    synchronized (backward) {
                                        backward.wait();
                                    }
                                }
                            }
                        }
                    }
                    bReader.close();
                    //System.out.println("[Info] End of file");
                    //System.exit(0);
                } catch (IOException e) {
                    System.out.println("[Error]: No stdin redirect input file");
                    System.exit(0);
                } catch (InterruptedException e) {
                    System.out.println("[Error]: Cannot wait backward object");
                }
            }
        });
        stdin.start();

        // 2. initialize client socket and receive data
        Thread receiver = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        lock.wait();
                        //System.out.println("Connect to " + hostName + ":" + portNum);
                        Socket socket = new Socket(hostName, portNum);
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String s;
                        while ((s = in.readLine()) != null) {
                            // 2.1 data split 16-bit to queue
                            //System.out.println("startPos = " + startPos + " currPos = " + currPos);
                            datastreams.setData(s);
                            System.out.println(s);
                            // start to execute query if the number of input data is satisfied
                            if ((startPos >= queryNum && queryNum != 0) || (currPos == queryNum) || (currPos - startPos + 1) == queryNum) {
                                synchronized (forward) {
                                    forward.notify();
                                }
                                synchronized (backward) {
                                    backward.wait();
                                }
                                startPos = 1;
                            }
                            currPos++;
                        }
                        //System.out.println("[Info] no more input data");
                    }
                } catch (IOException e) {
                    System.out.println("[Error] socket cannot adopt " + hostName + ":" + portNum);
                    System.exit(0);
                } catch (InterruptedException e) {
                    System.out.println("[Error] cannot wait for lock object");
                }
            }
        });
        //receiver.setPriority(Thread.MAX_PRIORITY);
        receiver.start();

        // 3. DGIM algorithm : create and start 16-bit streams with threads
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (sync) {
                        for (int i = 0; i < bitLen; i++) {
                            dgims[i] = new DGIM(datastreams.getData(i), i);
                        }
                        synchronized (forward) {
                            while(true) {
                                forward.wait();
                                for (int i = 0; i < bitLen; i++) {
                                    dgims[i].setTarget(queryNum);
                                    dgims[i].execute(sync);
                                }
                                // generate result
                                long sum = 0;
                                for (int i = 0; i < bitLen; i++) {
                                    sum += dgims[i].getBucketCnt(queryNum) * Math.pow(2, i);
                                    //System.out.println("P2 position = " + currPos + " buckekStream position = " + dgims[i].getCurrentPos());
                                }
                                System.out.println("The sum of last " + queryNum + " integers is " + sum);
                                synchronized (backward) {
                                    backward.notifyAll();
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("[Error] cannot wait for forward object");
                }
            }
        }).start();
    }
}
