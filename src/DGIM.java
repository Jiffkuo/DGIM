import java.util.Iterator;
import java.util.Queue;

/**
 * Created by Tzu-Chi Kuo on 2017/4/24.
 */
public class DGIM implements Runnable {
    private Queue<Boolean> dataStream;
    private String tName;
    private int tID;
    private Thread thread;

    public DGIM (Queue<Boolean> stream, int id) {
        dataStream = stream;
        tID = id;
        tName = String.valueOf(tID);
    }
    @Override
    public void run() {
        System.out.print("[Bit" + tID + "]\t");
        Iterator<Boolean> iter = dataStream.iterator();
        while(iter.hasNext()) {
            if (dataStream.poll()) {
                System.out.print("1 ");
            } else {
                System.out.print("0 ");
            }
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this, tName);
            thread.start();
        }
    }
}
