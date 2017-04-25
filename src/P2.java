import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

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
    // main entry
    public static void main(String[] args) {
        // 0. initialize parameters
        int bitLen = 16;

        // 1. read redirect
        List<String> inputlists;
        OpenAndValidate openvalid = new OpenAndValidate();
        inputlists = openvalid.getInputs();

        // 2. initialize client socket and receive data
        // 2.1 data split 16-bit to queue
        /*
        System.out.println("[Debug] DAT file contains:");
        for (String s : inputLists) {
            System.out.println(s);
        }
        */
        SplitDataStream datastreams = new SplitDataStream(bitLen);
        String[] net = inputlists.get(0).split(":");
        Thread receiver = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println(net[0] + ":" + Integer.valueOf(net[1]));
                    Socket socket = new Socket(net[0], Integer.valueOf(net[1]));
                    //InputStream in = new DataInputStream(socket.getInputStream());
                    /*
                    int len;
                    len = in.available();
                    byte[] buf = new byte[len];
                    int read = in.read(buf);
                    System.out.println(read);
                    for (byte b : buf) {
                        char c = (char) b;
                        System.out.print(c);
                    }
                    */
                    /*
                    while (in.available() > 0) {
                        byte b = in.readByte();
                        System.out.print(b + " = ");
                        System.out.println((char)b);
                    }
                    */
                    /*
                    byte[] buf = new byte[5];
                    while (in.read(buf) > 0) {
                        String s = new String(buf);
                        System.out.println(s);
                        datastreams.setData(s);
                        buf = new byte[5];
                    }
                    */
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String s;
                    while((s = in.readLine()) != null) {
                        System.out.println(s);
                        datastreams.setData(s);
                    }
                } catch (IOException e) {
                    System.out.println("[Error] socket cannot adopt " + inputlists.get(0) + "");
                    System.exit(0);
                }
            }
        });
        receiver.setPriority(Thread.MAX_PRIORITY);
        receiver.start();

        // 4. DGIM algorithm
        DGIM[] dgims = new DGIM[bitLen];
        // 4.1 create and start 16-bit streams with threads
        for (int i = 0; i < bitLen; i++) {
            dgims[i] = new DGIM(datastreams.getData(i), i);
            dgims[i].start();
        }

        // 4.2 execute query with thread, can check query by dat or stdin by prompt
        Thread query = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i < inputlists.size(); i++) {
                    if (inputlists.get(i).equals("end")) {
                        // get command end to leave program
                        System.exit(0);
                    }
                    long query = Long.valueOf(inputlists.get(i));
                    long sum = 0;
                    System.out.println(openvalid.prefix + " " + query + " integers?");
                    for (int j = 0; j < bitLen; j++) {
                        synchronized (dgims) {
                            while (dgims[j].getCurrentPos() < query) {
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            // sum (j=0, n) of ci * 2^j
                            //dgims[j].displayBucketStream();
                            sum += dgims[j].getBucketCnt(query) * Math.pow(2, j);
                        }
                    }
                    System.out.println("The sum of last " + query + " integers is " + sum);
                }
            }
        });
        query.start();
    }
}
