package collection;

import GeneralElements.ItemInterface;

import javax.media.j3d.PointArray;
import javax.vecmath.Point3d;

/**
 * Created by mviswanathan on 15-09-2019.
 */
public class RelativePointArrayFIFO extends PointArrayFIFO {
//    RelativePointArrayFIFO nextArray = null;
    ItemInterface relativeTo;

    public RelativePointArrayFIFO(PointArrayFIFO reference, ItemInterface relativeTo, PointArrayFIFO fifoRelativeTo) {
        super(reference.getVertexCount(), reference.getVertexFormat(), reference.color);
        this.relativeTo = relativeTo;

        int nextPos1 = reference.nextPos;
        boolean full1 = reference.full;
        int nowCount1 = reference.nowCount;
        int onceIn1 = reference.onceIn;

        Point3d point = new Point3d();
        Point3d relPoint = new Point3d();
        for (int p = 0; p < size; p++) {
            reference.getCoordinate(p, point);
            fifoRelativeTo.getCoordinate(p, relPoint);
            point.sub(relPoint);
            setCoordinate(p, point);
// some problem here, the methods change reference.nextpos !!! ifoRelativeTo.getCoordinate, setCoordinate(p, point);
        }
//        nextPos = reference.nextPos;
//        full = reference.full;
//        nowCount = reference.nowCount;
//        onceIn = reference.onceIn;
        if (reference.nextArray != null) {
            nextArray = new RelativePointArrayFIFO(reference.nextArray, relativeTo, fifoRelativeTo.nextArray);
            nextArray.setCapability(PointArray.ALLOW_COORDINATE_READ);
            nextArray.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
            nextArray.setCapability(PointArray.ALLOW_COLOR_WRITE);
        }
        nextPos = nextPos1;
        full = full1;
        nowCount = nowCount1;
        onceIn = onceIn1;
    }

//    public RelativePointArrayFIFO getNextArray() {
//        return nextArray;
//    }

    public void addRelativeCoordinate(Point3d point) {
        Point3d relPt = new Point3d(point);
        relPt.sub(relativeTo.getPos());
/*
        nowCount++;
        if (nowCount >= onceIn) {
            nowCount = 0;
            if (full && nextArray != null) {
//                ItemMovementsApp.log.trace("going to nextArray " + nextArray);
                int lastLoc = nextPos;
                Point3d oldPoint = new Point3d();
                getCoordinate(lastLoc, oldPoint);
                nextArray.addCoordinate(oldPoint);
            }
            setCoordinate(nextPos, point);
            nextPos++;
            if (nextPos >= size){
//                if (!full)
//                    ItemMovementsApp.log.trace("just got full " + this);
                nextPos = 0;
                full = true;
            }
        }
*/

        super.addCoordinate(relPt);
//        System.out.println("rel to " + relativeTo.getName());
    }
}
