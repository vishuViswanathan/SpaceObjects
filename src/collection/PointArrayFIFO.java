package collection;

import javax.media.j3d.PointArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

/**
 * Created by M Viswanathan on 28 Jan 2014
 */
public class PointArrayFIFO extends PointArray {
    int nextPos = 0;
    int size = 0;
    boolean full = false;
    PointArrayFIFO nextArray = null;
    int onceIn = 1; // add data to it once in so may inputs
    int nowCount = 0;
    Color3f color;
    public PointArrayFIFO(int vertexCount, int vertexFormat, Color3f color) {
        this(vertexCount, 1, vertexFormat, color);
    }

    public PointArrayFIFO(int vertexCount, int onceIn, int vertexFormat, Color3f color) {
        super(vertexCount, vertexFormat);
        this.color = color;
        for (int i = 0; i < vertexCount; i++)
            setColor(i, color);
        size = vertexCount;
        this.onceIn = onceIn;
    }

    public void noteNextArray(PointArrayFIFO nextArray) {
        this.nextArray = nextArray;
    }

    public PointArrayFIFO getNextArray() {
        return nextArray;
    }

    public void addCoordinate(Point3d point) {
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
    }

  }
