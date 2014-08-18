package GeneralElements;

import mvUtils.Point3dMV;
import mvUtils.Vector3dMV;
import mvXML.ValAndPos;
import mvXML.XMLmv;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 20 May 2014
 */
public class ItemStat {
    double time;
    double distFromPrimary;
    public Point3dMV pos;
    public Vector3dMV velocity;
    Vector3d acc;

    public ItemStat () {
        velocity = new Vector3dMV();
        pos = new Point3dMV();
        acc = new Vector3d();
    }

    public ItemStat(String xmlStr) throws NumberFormatException {
        this();
        takeFromXML(xmlStr);
    }

    public void initPos(Point3d pos, Vector3d velocity) {
        this.pos.set(pos);
        this.velocity.set(velocity);
        acc.set(0, 0, 0);
    }

    public void add(ItemStat withStat) {
        pos.add(withStat.pos);
        velocity.add(withStat.velocity);
        acc.add(withStat.acc);
        distFromPrimary = 0;
    }

    public StringBuilder positionStringForCSV(double factor) {
        StringBuilder csvStr = new StringBuilder();
        csvStr.append(factor * pos.x).append(", ").append(factor * pos.y).append(", ").append(factor * pos.z);
        return csvStr;
    }

    public StringBuilder velocityStringForCSV(double factor) {
        StringBuilder csvStr = new StringBuilder();
        csvStr.append("" + (factor * velocity.x));
        csvStr.append(", " + (factor * velocity.y));
        csvStr.append(", " + (factor * velocity.z));
        return csvStr;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("pos", pos.dataInCSV())).
                                    append(XMLmv.putTag("vel", velocity.dataInCSV()));
        return xmlStr;
    }

    public boolean takeFromXML(String xmlStr) throws NumberFormatException {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "pos", 0);
        pos.set(vp.val);
        vp = XMLmv.getTag(xmlStr, "vel", 0);
        velocity.set(vp.val);
        return retVal;
    }
}
