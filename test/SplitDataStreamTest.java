import org.junit.Test;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;

import static org.junit.Assert.*;

/**
 * Created by user on 2017/4/24.
 */
public class SplitDataStreamTest {
    int size = 16;
    SplitDataStream sol = new SplitDataStream(size);
    @Test
    public void setData() throws Exception {
        sol.setData("1");
        sol.setData("100");
        sol.setData("1000");
        sol.setData("10000");
        for (int i = 0; i < size; i++) {
            //SynchronousQueue<Boolean> ans = sol.getData(i);
            Queue<Boolean> ans = sol.getData(i);
            System.out.print("[Bit" + i + "]\t");
            Iterator<Boolean> iter = ans.iterator();
            while(iter.hasNext()) {
                if (ans.poll()) {
                    System.out.print("1 ");
                } else {
                    System.out.print("0 ");
                }
            }
            System.out.println();
        }
    }

    @Test
    public void getData() throws Exception {
    }

}