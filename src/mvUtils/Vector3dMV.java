package mvUtils;

import javax.vecmath.Vector3d;

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
}
