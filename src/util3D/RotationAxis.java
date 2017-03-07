package util3D;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 14 Apr 2016
 */
public class RotationAxis {
    Point3d thePoint;
    Vector3d axis;

    public RotationAxis(Point3d thePoint, Vector3d axis) {
        this.thePoint = thePoint;
        this.axis = axis;
    }
}
