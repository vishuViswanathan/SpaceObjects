package GeneralElements.utils;

import mvUtils.math.DoubleMaxMin;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 05 Jul 2015
 * ThreeDSize has the ranges of x, y and z positions
 */
public class ThreeDSize {
    DoubleMaxMin xMaxMin;
    DoubleMaxMin yMaxMin;
    DoubleMaxMin zMaxMin;

    public ThreeDSize(DoubleMaxMin xMaxMin, DoubleMaxMin yMaxMin, DoubleMaxMin zMaxMi) {
        this.xMaxMin = xMaxMin;
        this.yMaxMin = yMaxMin;
        this.zMaxMin = zMaxMi;
    }

    public Point3d midPoint() {
        return new Point3d(xMaxMin.mean(), yMaxMin.mean(), zMaxMin.mean());
    }

    public Vector3d range() {
        return new Vector3d(xMaxMin.range(), yMaxMin.range(), zMaxMin.range());
    }
}
