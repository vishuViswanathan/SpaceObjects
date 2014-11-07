package mvUtils;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.text.DecimalFormat;

/**
 * Created by M Viswanathan on 15 Jun 2014
 */
public class Vector3dMV extends Vector3d {

    public Vector3dMV() {
        super();
    }

    public Vector3dMV(double x, double y, double z) {
        super(x, y, z);
    }

    public Vector3dMV(String strCSV) throws NumberFormatException {
        super();
        set(strCSV);
    }

    public Vector3dMV(Tuple3d tuple) {
        super(tuple);
    }

    public Vector3dMV(double scale, Tuple3d vecRef) {
        super();
        scale(scale, vecRef); // suoer call
    }

    public static Vector3dMV meanVector3dMV(Tuple3d vec1, Tuple3d vec2) {
        Vector3dMV vec = new Vector3dMV();
        vec.setMean(vec1, vec2);
        return vec;
    }

    public Vector3dMV setMean(Tuple3d vec1, Tuple3d vec2) {
        set(vec1);
        add(vec2);
        scale(0.5);
        return this;
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
