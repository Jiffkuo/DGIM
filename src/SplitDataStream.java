import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Tzu-Chi Kuo on 2017/4/24.
 * Purpose:
 *  1. Split integer to 16-bit boolean data
 *  2. offer and pull data
 */

public class SplitDataStream {
    public Queue<Boolean>[] dataStreams;
    private int dataLen;
    private int maxLen;

    @SuppressWarnings("unchecked")
    public SplitDataStream(int size) {
        dataLen = size;
        dataStreams = new Queue[size];
        String max = String.valueOf((int)Math.pow(2, dataLen));
        maxLen = max.length();
        for (int i = 0; i < dataLen; i++) {
            dataStreams[i] = new ConcurrentLinkedQueue<>();
        }
    }

    public synchronized void setData(String in) {
        if (in.length() > maxLen) {
            System.out.println("[Error] input data over 16-bit space");
            System.exit(0);
        }
        int data = Integer.valueOf(in.trim());
        int mask = 1;
        for (int i = 0; i < dataLen; i++) {
            boolean success;
            synchronized (dataStreams[i]) {
                if ((data & mask) == 0) {
                    success = dataStreams[i].offer(Boolean.FALSE);
                } else {
                    success = dataStreams[i].offer(Boolean.TRUE);
                }
            }
            if (!success) {
                System.out.println("[Error] cannot offer data to Queue");
            }
            mask<<=1;
        }
    }

    public synchronized Queue<Boolean> getData(int bit) {
        return dataStreams[bit];
    }
}
