package mvUtils;

import java.util.regex.Pattern;

/**
 * Created by M Viswanathan on 12 Apr 2014
 */
public class StringOps {
    private final static Pattern LTRIM = Pattern.compile("^\\s+");
    private final static Pattern RTRIM = Pattern.compile("\\s+$");

    public static String ltrim(String s) {
        return LTRIM.matcher(s).replaceAll("");
    }

    public static String rtrim(String s) {
        return RTRIM.matcher(s).replaceAll("");
    }

    /**
     *
     * @param src
     * @param from
     * @param to
     * @return String between the two parameters, excluding the params
     */
    public static String substring(String src, String from, String to) {
        int startLoc = src.indexOf(from);
        int endLoc = src.indexOf(to);
        if (startLoc >= 0 && endLoc > (startLoc + from.length())) {
            return src.substring(startLoc + from.length() + 1, endLoc);
        }
        else
            return "";
    }
}
