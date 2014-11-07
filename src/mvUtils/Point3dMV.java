package mvUtils;

import javax.vecmath.Point3d;
import java.text.DecimalFormat;

/**
 * Created by M Viswanathan on 15 Jun 2014
 */
public class Point3dMV extends Point3d {
    public Point3dMV() {
        super();
    }

    public Point3dMV(double x, double y, double z) {
        super(x, y, z);
    }

    public Point3dMV(String strCSV) throws NumberFormatException {
        super();
        set(strCSV);
     }

    public void set(String strCSV) throws NumberFormatException {
        String[] split = strCSV.split(",");
        if (split.length == 3)
            set(Double.valueOf(split[0]), Double.valueOf(split[1]), Double.valueOf(split[2]));
        else
            throw new NullPointerException("CSV elements are not 3");
    }

    public String dataInCSV() {
        return "" + x + ", " + y + ", " + z;
    }

    public String dataInCSV(String fmtStr) {
        DecimalFormat fmt = new DecimalFormat(fmtStr);
        return fmt.format(x)+ ", " + fmt.format(y) + ", " + fmt.format(z);
    }

    public String dataInCSV(int significantDigits) {
        SmartFormatter fmt = new SmartFormatter(significantDigits);
        return fmt.format(x)+ ", " + fmt.format(y) + ", " + fmt.format(z);
    }
}
