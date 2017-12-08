package latticesim;

import java.util.Scanner;

public class SimUtil {
    public static String toString(double [] ds) {
        StringBuilder buffer = new StringBuilder("{");

        if (ds.length > 0) {
            buffer.append(ds[0]);

            for (int i = 1; i < ds.length; i++) {
                buffer.append(", " + ds[i]);
            }
        }

        buffer.append("}");

        return buffer.toString();
    }

    // Read all of System.in into a string
    // From http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
    public static String getStdIn() {
        Scanner s = new Scanner(System.in).useDelimiter("\\A");

        return s.hasNext() ? s.next() : "";
    }
}
