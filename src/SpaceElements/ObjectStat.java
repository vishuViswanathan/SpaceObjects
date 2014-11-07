package SpaceElements;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Created by IntelliJ IDEA.
 * User: M Viswanathan
 * Date: 11/30/13
 * Time: 6:03 PM
 * To change this template use File | Settings | File Templates.
 * Param of the space object
 */
public class ObjectStat {
    double time;
    double distFromPrimary;
    public Point3d pos;
    public Vector3d velocity;
    Vector3d acc;

    public ObjectStat () {
        velocity = new Vector3d();
        pos = new Point3d();
        acc = new Vector3d();
    }

    public ObjectStat(double time, Point3d pos, Vector3d velocity, Vector3d acc) {
        this.time = time;
        this.velocity = new Vector3d(velocity);
        this.pos = new Point3d(pos);
        this.acc = new Vector3d(acc);
    }

    public ObjectStat(double time, Point3d pos, Vector3d velocity, Vector3d acc, double distFromPrimary) {
        this.time = time;
        this.velocity = new Vector3d(velocity);
        this.pos = new Point3d(pos);
        this.acc = new Vector3d(acc);
        this.distFromPrimary = distFromPrimary;
    }

    public ObjectStat(ObjectStat fromStat) {
        this(fromStat.time, fromStat.pos, fromStat.velocity, fromStat.acc, fromStat.distFromPrimary);
    }
    public void initPos(Point3d pos, Vector3d velocity) {
        this.pos.set(pos);
        this.velocity.set(velocity);
        acc.set(0, 0, 0);
    }

    void distFromPrimary(SpaceObject primary) {
        distFromPrimary = pos.distance(primary.status.pos);
    }

    void initAcc() {
        acc.set(0, 0, 0);
    }

    void addToAcc(Vector3d addAcc) {
        acc.add(addAcc);
    }

    public void changeReference(ObjectStat toStat) {
        sub(toStat);
    }

    public void changeReference(ObjectStat fromStat, ObjectStat toStat) {
        add(toStat);
        sub(toStat);
    }

    public void add(ObjectStat withStat) {
        pos.add(withStat.pos);
        velocity.add(withStat.velocity);
        acc.add(withStat.acc);
        distFromPrimary = 0;
    }

    public void sub(ObjectStat withStat) {
        pos.sub(withStat.pos);
        velocity.sub(withStat.velocity);
        acc.sub(withStat.acc);
        distFromPrimary = 0;
    }

    public StringBuilder positionStringForCSV(double factor) {
        StringBuilder csvStr = new StringBuilder();
        csvStr.append("" + (factor * pos.x));
        csvStr.append(", " + (factor * pos.y));
        csvStr.append(", " + (factor * pos.z));
        return csvStr;
    }

    public StringBuilder velocityStringForCSV(double factor) {
        StringBuilder csvStr = new StringBuilder();
        csvStr.append("" + (factor * velocity.x));
        csvStr.append(", " + (factor * velocity.y));
        csvStr.append(", " + (factor * velocity.z));
        return csvStr;
    }
}
