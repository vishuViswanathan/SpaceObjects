package GeneralElements.link;

import GeneralElements.DarkMatter;
import GeneralElements.Item;
import GeneralElements.ItemSpace;

import javax.media.j3d.LineArray;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.Vector;

/**
 * Created by M Viswanathan on 29 May 2014
 */
public class Rope  extends InfluenceDef  {
    int nElements;
    double massPerM;
    Vector<DarkMatter> massElements;
    Vector<ItemLink> allLinks;
    Window parent;
    ItemSpace space;

    public Rope(DarkMatter item1, DarkMatter item2, double freeLen, double massPerM, double eExpansion, int nElements) {
        type = Type.ROPE;
        this.item1 = item1;
        this.item2 = item2;
        this.massPerM = massPerM;
        this.freeLen = freeLen;
        this.eExpansion = eExpansion;
        allLinks = new Vector<ItemLink>(nElements);
        this.nElements = (nElements / 2) * 2; // make it even
        this.parent = item1.parentW;
        this.space = item1.space;
        setAllElements();
    }

// FOLLOWING IS TO BE DONE YET
    public Rope(Item item1, Item item2, double initialLenFactor, double eExpansion) {
        super(item1, item2, initialLenFactor, eExpansion, eExpansion);
        type = Type.ROPE;
    }

    void setAllElements() {
        int pairs = nElements / 2;
        allLinks = new Vector<ItemLink>(nElements);
        nElements = (nElements / 2) * 2; // make it even
        massElements = new Vector<DarkMatter>(nElements - 1); // the massElements are 1 less
        double uMass = massPerM * freeLen / (nElements - 1);
        Vector3d distVect = new Vector3d();
        Vector3d item1Pos = new Vector3d(item1.status.pos);
        distVect.sub(item2.status.pos, item1Pos);
        double distance = distVect.length();
        double oneLinkLen = freeLen / nElements;
        if (distance <= freeLen) {
            double stPitch = distance / nElements;
            double stPitch2 = Math.pow(stPitch, 2);
            double oneLinkLen2 = Math.pow(oneLinkLen, 2);
            // create mass elements and position them uniformly between item1 and item2, but at an oneLinkLen
            // in zigzag fashion if required
            // mark along x axis in xy plane
            Vector <Point3d> points = new Vector<Point3d>();
            double x = 0;
            double y = Math.sqrt(oneLinkLen2 - stPitch2);
            double z = 0;
            x = 0;
             for (int i = 0; i < pairs - 1; i++) {
                // the first point is offset from x axis
                x += stPitch;
                points.add(new Point3d(x, y, z));
                 // the next point is on the x axis
                x += stPitch;
                points.add(new Point3d(x, 0, z));
            }
            // for the last pair, take only the offset point since the next point will be item2
            x += stPitch;
            points.add(new Point3d(x, y, z));
            // create a transform to the actual connecting line
            double xShift = item1Pos.x;
            double yShift = item1Pos.y;
            double zShift = item1Pos.z;
            double yRot = Math.atan(distVect.getZ()/ distVect.getX());
            double zRot = - Math.atan(distVect.getY()/ distVect.getX());
            Transform3D tr = new Transform3D();
            tr.setTranslation(item1Pos);
            tr.rotY(yRot);
            tr.rotZ(zRot);
            DarkMatter oneElem;
            int i = 0;
            for (Point3d point: points) {
                tr.transform(point);
                oneElem = new DarkMatter("pa" + i++, uMass, 0.001, Color.BLACK, parent);
                oneElem.initPosEtc(point, new Vector3d());
                massElements.add(oneElem);
            }
            // create rods between the points
            DarkMatter lastItem = item1;
            for (DarkMatter oneItem:massElements) {
                allLinks.add(new ItemLink(lastItem, oneItem, new Rod(lastItem, oneItem, oneLinkLen, eExpansion), space));
            }

        }
        else {

        }
    }

    @Override
    public boolean evalForce() {
        Vector3d distVect = new Vector3d();
        distVect.sub(item2.status.pos, item1.status.pos);
        double r = distVect.length();
        Vector3d nowForce;
        double diff = r - freeLen;
        double force;
        // attraction is positive
        if (diff > 0) {
            force  =   diff * kExpansion;
            double ratio = force / r;
            nowForce = new Vector3d(distVect);
            nowForce.scale(ratio);
            item1.addToForce(nowForce);
            nowForce.negate();
            item2.addToForce(nowForce);
        }
        for (ItemLink l: allLinks)
            l.evalForce();
        return true;
    }
}

