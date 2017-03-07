package util3D;

import mvUtils.physics.Point3dMV;
import mvUtils.physics.Vector3dMV;

/**
 * Created by M Viswanathan on 14 Apr 2016
 */
public class Yeti {
    Point3dMV massCenter;
    Vector3dMV xAxis;
    Vector3dMV yAxis;

    public Yeti(Point3dMV massCenter, Vector3dMV xAxis, Vector3dMV yAxis) {
        this.massCenter = massCenter;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }
}
