import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Tzu-Chi Kuo on 2017/4/23.
 * ID: W1279858
 * Purpose:
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
                    // wait 10 ms in order to invoke all threads
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e){
                        System.out.println("[Error] cannot make thread sleep");
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
                                    System.out.println(line);
                                    getResult(dgims, bitLen, Long.valueOf(query[0]));
                                }
                            }
                        }
                    }
                    //System.out.println("[Info] End of file");
                    bReader.close();
                    //System.exit(0);
                } catch (IOException e) {
                    System.out.println("[Error]: No redirect input file");
                    System.exit(0);
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
                            datastreams.setData(s);
                            System.out.println(s);
                        }
                        System.out.println("[Info] no more input data");
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
        synchronized (sync) {
            for (int i = 0; i < bitLen; i++) {
                dgims[i] = new DGIM(datastreams.getData(i), i);
                dgims[i].start(sync);
            }
        }
    }

    public static void getResult(DGIM[] dgims, int bitLen, long query) {
        long sum = 0;
        synchronized (dgims) {
            for (int j = 0; j < bitLen; j++) {
                while (true) {
                    if (dgims[j] == null) {
                        continue;
                    }
                    synchronized (sync) {
                        dgims[j].setTarget(query);
                        if (dgims[j].getCurrentPos() <= query) {
                            try {
                                sync.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }
                    /*
                    if (dgims[j].getCurrentPos() <= query) {
                        try {
                            TimeUnit.MICROSECONDS.sleep(30);
                        } catch (InterruptedException e) {
                            System.out.println("[Error] cannot make thread sleep");
                        }
                    } else {
                        break;
                    }
                    */
                }
                // sum (j=0, n) of ci * 2^j
                //dgims[j].displayBucketStream();
                sum += dgims[j].getBucketCnt(query) * Math.pow(2, j);
            }
            System.out.println("The sum of last " + query + " integers is " + sum);
        }
    }
}
