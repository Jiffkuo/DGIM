import javax.xml.crypto.Data;
import java.io.*;
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println(net[0] + ":" + Integer.valueOf(net[1]));
                    Socket socket = new Socket(net[0], Integer.valueOf(net[1]));
                    DataInputStream in = new DataInputStream(socket.getInputStream());
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
                    byte[] buf = new byte[5];
                    while (in.read(buf) > 0) {
                        String s = new String(buf);
                        System.out.println(s);
                        datastreams.setData(s);
                        buf = new byte[5];
                    }
                } catch (IOException e) {
                    System.out.println("[Error] socket cannot adopt " + inputlists.get(0) + "");
                }
            }
        }).start();

        // 4. DGIM algorithm
        DGIM[] dgims = new DGIM[bitLen];
        // 4.1 create and start 16-bit streams with threads
        for (int i = 0; i < bitLen; i++) {
            dgims[i] = new DGIM(datastreams.getData(i), i);
            dgims[i].start();
        }
        // 4.2 execute query
        // 5. end and stop program
    }
}
