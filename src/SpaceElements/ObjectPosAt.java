package SpaceElements;

import javax.vecmath.Point3d;

/**
 * Created by mviswanathan on 1/23/14.
 */
public class ObjectPosAt {
    double time;
    public Point3d pos;

    public ObjectPosAt(double time, Point3d thePos) {
        this.time = time;
        pos = new Point3d(thePos);
    }
}
