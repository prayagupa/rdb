package util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class Util {
    public static void writeToFile(String context,
                                   String operation,
                                   String file,
                                   Map<Integer, Long> data) throws IOException {
        var out = new FileOutputStream(file);
        out.write(("context,operation,iteration" + "," + "time taken in millis" + "\n").getBytes());
        data.entrySet().forEach(entry -> {
            try {
                out.write((context + "," + operation + "," + entry.getKey() + "," + entry.getValue() + "\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        out.flush();
        out.close();
    }
}
