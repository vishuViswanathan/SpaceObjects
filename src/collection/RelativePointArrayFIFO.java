package collection;

import GeneralElements.ItemInterface;

import javax.media.j3d.PointArray;
import javax.vecmath.Point3d;

/**
 * Created by mviswanathan on 15-09-2019.
 */
public class RelativePointArrayFIFO extends PointArrayFIFO {
    ItemInterface relativeTo;

    public RelativePointArrayFIFO(PointArrayFIFO reference, ItemInterface relativeTo, PointArrayFIFO fifoRelativeTo) {
        super(reference.getVertexCount(), reference.getVertexFormat(), reference.color);
        takeData(reference, relativeTo, fifoRelativeTo);
        setCapability(PointArray.ALLOW_COORDINATE_READ);
        setCapability(PointArray.ALLOW_COORDINATE_WRITE);
        setCapability(PointArray.ALLOW_COLOR_WRITE);
        if (reference.nextArray != null) {
            nextArray = new RelativePointArrayFIFO(reference.nextArray, relativeTo, fifoRelativeTo.nextArray);
        }
    }

    public boolean takeData(PointArrayFIFO reference, ItemInterface relativeTo, PointArrayFIFO fifoRelativeTo) {
        this.relativeTo = relativeTo;
        Point3d point = new Point3d();
        Point3d relPoint = new Point3d();
        for (int p = 0; p < size; p++) {
            reference.getCoordinate(p, point);
            fifoRelativeTo.getCoordinate(p, relPoint);
            point.sub(relPoint);
            setCoordinate(p, point);
        }
        nextPos = reference.nextPos;
        full = reference.full;
        nowCount = reference.nowCount;
        onceIn = reference.onceIn;
        return true;
    }

    public void addRelativeCoordinate(Point3d point) {
        Point3d relPt = new Point3d(point);
        relPt.sub(relativeTo.getPos());
        super.addCoordinate(relPt);
    }
}
