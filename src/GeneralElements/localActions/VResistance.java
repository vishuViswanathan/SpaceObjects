package GeneralElements.localActions;

import GeneralElements.Item;
import mvXML.ValAndPos;
import mvXML.XMLmv;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 26 Aug 2014
 */
public class VResistance extends LocalAction {
    double factor;
    double frictionArea;


    public VResistance(Item item, double factor) {
        super(item);
        this.factor = factor;
        type = Type.FLUIDRESISTANCE;
        evalAreas();
    }

    public VResistance(Item item, String xmlStr) throws Exception {
        super(item);
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
}

