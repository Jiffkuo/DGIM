import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tzu-Chi Kuo on 2017/4/23.
 */
public class OpenAndValidate {
    private List<String> inputLists;
    private String prefix = "What is the sum for last";

    public OpenAndValidate() {
        // open and store
        inputLists = new ArrayList<>();
        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
            String line = "";
            while((line = bReader.readLine()) != null) {
                inputLists.add(line);
            }
        } catch (Exception e) {
            System.out.println("[Error]: No redirect input file");
            System.exit(0);
        }
        // validate format
        if (inputLists.size() <= 0) {
            System.out.println("[Error] no any command in dat file");
            System.exit(0);
        }
        // first one must be host:port information
        String firstCmd = inputLists.get(0);
        if (!firstCmd.contains(":")) {
            System.out.println("[Error] host:port should be the first line - " + firstCmd);
            System.exit(0);
        }
        boolean hasEnd = false;
        for (int i = 1; i < inputLists.size(); i++) {
            String cmd = inputLists.get(i);
            // check query statement format
            if (!cmd.startsWith(prefix) && !cmd.contains("integers")) {
                // check end statement
                if (cmd.startsWith("end") || cmd.contains("end")) {
                    hasEnd = true;
                } else {
                    System.out.print("[Error] query format error: " + cmd);
                    System.out.println(" (should be:" + prefix + " k integers)");
                    System.exit(0);
                }
            } else {
                // get query number
                String[] query = cmd.substring(prefix.length()).trim().split(" ");
                inputLists.set(i, query[0]);
            }
        }
        if (!hasEnd) {
            System.out.print("[Error] missing \"end\" in the dat file.");
            System.out.println(" Please add \"end\" in order to exit P2 program");
            System.exit(0);
        }
    }

    public List<String> getInputs() {
        return inputLists;
    }
}
