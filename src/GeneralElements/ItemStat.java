package GeneralElements;

import mvUtils.physics.Point3dMV;
import mvUtils.physics.Vector3dMV;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 20 May 2014
 */
public class ItemStat {
    public enum Param {
        POSITION("Position"),
        VELOCITY("VELOCITY"),
        ACCELERATION("Acceleration"),
        OMEGA("Angular Velocity"),
        ALPHA("Angular Acceleration"),
        TIME("Time");

        private final String statusName;

        Param(String statusName) {
            this.statusName = statusName;
        }

        public String getValue() {
            return name();
        }

        @Override
        public String toString() {
            return statusName;
        }

        public static Param getEnum(String text) {
            Param retVal = null;
            if (text != null) {
                for (Param b : Param.values()) {
                    if (text.equalsIgnoreCase(b.statusName)) {
                        retVal = b;
                        break;
                    }
                }
                if (retVal == null)
                    retVal = POSITION;
            }
            return retVal;
        }
    }

    double time;
    double distFromPrimary; // TODO not used
    public Point3dMV pos;
    public Vector3dMV angularPos;  // on object's local coordinates
    public Vector3dMV velocity;
    public Vector3dMV angularVelocity; // on object's local coordinates
    Vector3dMV acc;
    public Vector3dMV angularAcceleration; // on object's local coordinates

    public ItemStat () {
        velocity = new Vector3dMV();
        pos = new Point3dMV();
        acc = new Vector3dMV();
        angularPos = new Vector3dMV();
        angularVelocity = new Vector3dMV();
        angularAcceleration = new Vector3dMV();
    }

    public ItemStat(String xmlStr) throws NumberFormatException {
        this();
        takeFromXML(xmlStr);
    }

    public void initPos(Point3d pos, Vector3d velocity) {
        initPos(pos, velocity, new Vector3d());
//        this.pos.set(pos);
//        this.velocity.set(velocity);
//        acc.set(0, 0, 0);
//        angularPos.set(0,0,0);
//        angularVelocity.set(0, 0, 0);
//        angularAcceleration.set(0, 0, 0);
    }

    public void initPos(Point3d pos, Vector3d velocity, Vector3d acc) {
        this.pos.set(pos);
        this.velocity.set(velocity);
        acc.set(acc);
        angularPos.set(0,0,0);
        angularVelocity.set(0, 0, 0);
        angularAcceleration.set(0, 0, 0);
    }

    public void initAngularPos(Vector3d angularPos, Vector3d angularVelocity) {
        this.angularPos.set(angularPos);
        this.angularVelocity.set(angularVelocity);
        angularAcceleration.set(0, 0, 0);
    }

    public void add(ItemStat withStat) {
        pos.add(withStat.pos);
        velocity.add(withStat.velocity);
        acc.add(withStat.acc);
        angularVelocity.add(withStat.angularVelocity);
        angularAcceleration.add(withStat.angularAcceleration);
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
        csvStr.append(", " + (factor * velocity.length()));
        return csvStr;
    }

    public StringBuilder accelerationStringForCSV(double factor) {
        StringBuilder csvStr = new StringBuilder();
        csvStr.append("" + (factor * acc.x));
        csvStr.append(", " + (factor * acc.y));
        csvStr.append(", " + (factor * acc.z));
        csvStr.append(", " + (factor * acc.length()));
        return csvStr;
    }

    public StringBuilder angularVelocityStringForCSV(double factor) {
        StringBuilder csvStr = new StringBuilder();
        csvStr.append("" + (factor * angularVelocity.x));
        csvStr.append(", " + (factor * angularVelocity.y));
        csvStr.append(", " + (factor * angularVelocity.z));
        return csvStr;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("pos", pos.dataInCSV())).
                                    append(XMLmv.putTag("vel", velocity.dataInCSV()));
        xmlStr.append(XMLmv.putTag("angularPos", angularPos.dataInCSV())).
                append(XMLmv.putTag("angularVel", angularVelocity.dataInCSV()));
//        if (angularVelocity.isNonZero())
//            xmlStr.append(XMLmv.putTag("angularVel", angularVelocity.dataInCSV()));
        return xmlStr;
    }

    public boolean takeFromXML(String xmlStr) throws NumberFormatException {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "pos", 0);
        pos.set(vp.val);
        vp = XMLmv.getTag(xmlStr, "vel", 0);
        velocity.set(vp.val);
        vp = XMLmv.getTag(xmlStr, "angularPos", 0);
        if (vp.val.length() > 0)
            angularPos.set(vp.val);
        vp = XMLmv.getTag(xmlStr, "angularVel", 0);
        if (vp.val.length() > 0)
            angularVelocity.set(vp.val);
        return retVal;
    }

    public Tuple3d getOneParam(Param param) {
        Tuple3d vector = null;
        switch(param) {
            case POSITION:
                vector = new Point3d(pos);
                break;
            case VELOCITY:
                vector = new Vector3d(velocity);
                break;
            case ACCELERATION:
                vector = new Vector3d(acc);
                break;
            case OMEGA:
                vector = new Vector3d(angularVelocity);
                break;
            case ALPHA:
                vector = new Vector3d(angularAcceleration);
                break;
        }
        return vector;
    }

    public void setParam(Tuple3d newVal, Param param) {
        switch(param) {
            case POSITION:
                pos.set(newVal);
                break;
            case VELOCITY:
                velocity.set(newVal);
                break;
            case ACCELERATION:
                acc.set(newVal);
                break;
            case OMEGA:
                angularVelocity.set(newVal);
                break;
            case ALPHA:
                angularAcceleration.set(newVal);
                break;
        }
    }

    String dataInCSV(Param param) {
        String csv = "";
        switch(param) {
            case POSITION:
                csv =  pos.dataInCSV();
                break;
            case VELOCITY:
                csv = velocity.dataInCSV();
                break;
            case ACCELERATION:
                csv = acc.dataInCSV();
                break;
            case OMEGA:
                csv = angularVelocity.dataInCSV();
                break;
            case ALPHA:
                csv = angularAcceleration.dataInCSV();
                break;
        }
        return csv;
    }

    String dataInCSV(Param param, String fmtStr) {
        String csv = "";
        switch(param) {
            case POSITION:
                csv =  pos.dataInCSV(fmtStr);
                break;
            case VELOCITY:
                csv = velocity.dataInCSV(fmtStr);
                break;
            case ACCELERATION:
                csv = acc.dataInCSV(fmtStr);
                break;
            case OMEGA:
                csv = angularVelocity.dataInCSV(fmtStr);
                break;
            case ALPHA:
                csv = angularAcceleration.dataInCSV(fmtStr);
                break;
        }
        return csv;
    }

    String dataInCSV(Param param, int significantDigits) {
        switch(param) {
            case POSITION:
                return pos.dataInCSV(significantDigits);
            case VELOCITY:
                return velocity.dataInCSV(significantDigits);
            case ACCELERATION:
                return acc.dataInCSV(significantDigits);
            case OMEGA:
                return angularVelocity.dataInCSV(significantDigits);
            case ALPHA:
                return angularAcceleration.dataInCSV(significantDigits);
         }
        return "";
    }
}
