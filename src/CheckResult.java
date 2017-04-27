import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Tzu-Chi Kuo on 2017/4/25.
 */
public class CheckResult {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("please add your example file in argument");
            System.exit(0);
        }
        try {
            FileReader fReader = new FileReader(args[0]);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            long result = 0;
            while ((line = bReader.readLine()) != null) {
                if (line.contains("The sum of last")) {
                    String[] tmp = line.split(" ");
                    long estimate = Long.valueOf(tmp[7]);
                    double error = ((double)(result - estimate)/estimate) * 100;
                    System.out.println(line);
                    System.out.println("[Parser] : " + result + " error = " + error + "%");
                }
                if (!line.contains("sum")) {
                    result += Integer.valueOf(line);
                }
            }
            bReader.close();
            fReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
