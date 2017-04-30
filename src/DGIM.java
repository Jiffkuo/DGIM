import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.Queue;

/**
 * Created by Tzu-Chi Kuo on 2017/4/24.
 * Purpose:
 *   main DGIM algorihtm: access 1-bit data stream with thread
 */

public class DGIM implements Runnable {
    private Queue<Boolean> dataStream;
    private Deque<bucket> bucketStream;
    private String tName;
    private Thread thread;
    private int tID;
    private long curPos;
    private long query;
    private Object syncLock;

    // inner class for bucket
    class bucket {
        int size;
        long pos;
        public bucket(int size, long pos) {
            this.size = size;
            this.pos = pos;
        }
        public void setPos(long pos) {
            this.pos = pos;
        }
        public long getPos() {
            return pos;
        }
        public void setSize(int size) {
            this.size = size;
        }
        public int getSize() {
            return size;
        }
    }

    // constructor
    public DGIM (Queue<Boolean> stream, int id) {
        dataStream = stream;
        tID = id;
        tName = String.valueOf(tID);
        bucketStream = new ConcurrentLinkedDeque<>();
        curPos = 1;
        query = 0;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (dataStream) {
                if (!dataStream.isEmpty() && (query > 0)) {
                    if (dataStream.poll()) {
                        addBucket(curPos++);
                        //System.out.print(tID + ":1(" + pos + ") ");
                    } else {
                        curPos++;
                        //System.out.print(tID + ":0 ");
                    }
                    synchronized (syncLock) {
                        if ((curPos > query) && (query > 0)) {
                            syncLock.notifyAll();
                        }
                    }
                }
            }
        }
    }

    // start thread
    public void start(Object sync) {
        if (thread == null) {
            syncLock = sync;
            thread = new Thread(this, tName);
            thread.start();
            //System.out.println("[Info] thread-" + tID + " starts!");
        }
    }

    public void execute(Object sync) {
        syncLock = sync;
        while (true) {
            synchronized (dataStream) {
                if (!dataStream.isEmpty() && (query >= 0)) {
                    if (dataStream.poll()) {
                        addBucket(curPos++);
                        //System.out.print(tID + ":1(" + curPos + ") ");
                    } else {
                        curPos++;
                        //System.out.print(tID + ":0 ");
                    }
                    synchronized (syncLock) {
                        if ((curPos > query) && (query >= 0)) {
                            syncLock.notifyAll();
                            break;
                        }
                    }
                }
            }
        }
    }

    // set target position
    public void setTarget(long target) {
        query = target;
    }

    // get current position
    public long getCurrentPos() {
        return curPos;
    }

    // get total count base on k
    public synchronized int getBucketCnt(long k) {
        int result = 0;
        int value = 0;
        if (bucketStream.isEmpty()) {
            return 0;
        }

        long distance = curPos - bucketStream.getFirst().getPos();
        if (distance > k) {
            return 0;
        } else {
            Iterator<bucket> iter = bucketStream.iterator();
            while (iter.hasNext()) {
                bucket cur = iter.next();
                if (cur.getPos() >= (curPos - k)) {
                    value = cur.getSize();
                    result += value;
                } else {
                    //result -= value / 2;
                    break;
                }
            }
        }
        result -= value / 2;
        return result;
    }

    // check if need to merge two buckets
    public boolean isMerge(Iterator<bucket> iter) {
        bucket head, next, nextnext;
        if (iter.hasNext()) {
            head = iter.next();
            if (iter.hasNext()) {
                next = iter.next();
                if (next.getSize() != head.getSize()) {
                    return false;
                } else {
                    if (iter.hasNext()) {
                        nextnext = iter.next();
                        if (next.getSize() == nextnext.getSize()) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // get iterator with specific bucket of size
    public Iterator<bucket> getNextIterator(int size) {
        Iterator<bucket> iter = bucketStream.iterator();
        while (iter.hasNext()) {
            bucket bucket = iter.next();
            if (bucket.getSize() == size) {
                return iter;
            }
        }
        return null;
    }

    // do merge bucket
    public void doMerge(Iterator<bucket> iter) {
        iter.next();
        bucket next = iter.next();
        bucket nextnext = iter.next();
        int bucketofsize = next.getSize();

        next.setSize(next.getSize() + nextnext.getSize());
        iter.remove();
        if(isMerge(getNextIterator(bucketofsize))) {
            doMerge(getNextIterator(bucketofsize));
        }
    }

    // add bucket
    public void addBucket(long pos) {
        bucketStream.addFirst(new bucket(1, pos));

        if (isMerge(bucketStream.iterator())) {
            Iterator<bucket> iter = bucketStream.iterator();
            doMerge(iter);
        }
    }

    // display bucketstream content
    public void displayBucketStream() {
        if (!bucketStream.isEmpty()) {
            Iterator<bucket> iter = bucketStream.iterator();
            System.out.println("tID = " + tID);
            while (iter.hasNext()) {
                bucket cur = iter.next();
                System.out.print(cur.getSize() + ":" + cur.getPos() + " ");
            }
            System.out.println();
        }
    }
}
