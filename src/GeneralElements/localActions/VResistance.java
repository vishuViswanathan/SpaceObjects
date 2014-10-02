package GeneralElements.localActions;

import GeneralElements.DarkMatter;
import mvXML.ValAndPos;
import mvXML.XMLmv;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 26 Aug 2014
 */
public class VResistance extends LocalAction {
    double factor;
    double frictionArea;

    public VResistance(double factor) {
        super(Type.FLUIDFRICTION);
        this.factor = factor;
    }

    public VResistance(DarkMatter item, double factor) {
        this(factor);
        setItem(item);
   }

    public void setItem(DarkMatter item) {
        super.setItem(item);
        evalAreas();
    }

    public VResistance(DarkMatter item, String xmlStr) throws Exception {
        super(Type.FLUIDRESISTANCE, item);
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "factor", 0);
        try {
            factor = Double.valueOf(vp.val);
            evalAreas();
        } catch (NumberFormatException e) {
            throw new Exception("in VResistance: " + e.getMessage());
        }
    }

    private void evalAreas() {
        frictionArea = item.getSurfaceArea();
    }

    public Vector3d getForce() {
        Vector3d force;
        double vel = item.status.velocity.length();
        if (vel > 0) {
            double forceMagnitude = -vel * factor * frictionArea; // opposing the velocity
            double scaleFactor = forceMagnitude / vel;
            force = new Vector3d(item.status.velocity);
            force.scale(scaleFactor);
        } else
            force = new Vector3d();
        return force;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("type", type.toString()));
        xmlStr.append(XMLmv.putTag("factor", factor));
        return xmlStr;
    }

    public Object clone() {
        VResistance cloned = (VResistance)super.clone();
        return cloned;
    }
}

