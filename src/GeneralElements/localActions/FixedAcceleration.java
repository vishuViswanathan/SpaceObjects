package GeneralElements.localActions;

import GeneralElements.DarkMatter;
import mvUtils.Vector3dMV;
import mvXML.ValAndPos;
import mvXML.XMLmv;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 27 Aug 2014
 */
public class FixedAcceleration extends LocalAction {
    Vector3dMV unitDirection;  // direction of fixed Acceleration, a unit Vector
    double fixedAcc = 9.81; // fixed acceleration value

    public FixedAcceleration(DarkMatter item, Vector3d direction, double fixedAcc) {
        super(Type.FIXEDACCELERATION, item);
        setValues(direction, fixedAcc);
    }

    public FixedAcceleration(Vector3d direction, double fixedAcc) {
        super(Type.FIXEDACCELERATION);
        setValues(direction, fixedAcc);
    }

    private void setValues(Vector3d direction, double fixedAcc) {
        unitDirection = new Vector3dMV(direction);
        unitDirection.scale(1 / direction.length());
        this.fixedAcc = fixedAcc;
    }

    private void setValues(String xmlStr) {
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "direction", 0);
        unitDirection = new Vector3dMV(vp.val);
        vp = XMLmv.getTag(xmlStr, "fixedAcc", 0);
        fixedAcc = Double.valueOf(vp.val);
    }

    public FixedAcceleration(DarkMatter item, String xmlStr) {
        super(Type.FIXEDACCELERATION, item);
        setValues(xmlStr);
    }

    public Vector3d getForce() {
        Vector3d force = new Vector3d(unitDirection);
        force.scale(fixedAcc * item.mass);
        return force;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("type", type.toString()));
        xmlStr.append(XMLmv.putTag("direction", unitDirection.dataInCSV()));
        xmlStr.append((XMLmv.putTag("fixedAcc", fixedAcc)));
        return xmlStr;
    }
}
