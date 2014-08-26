package GeneralElements.localActions;

import GeneralElements.Item;
import mvXML.ValAndPos;
import mvXML.XMLmv;

import javax.vecmath.Vector3d;
import java.util.HashMap;

/**
 * Created by M Viswanathan on 13 Aug 2014
 */
public class V2Resistance extends LocalAction {
    double factor;
    double frictionArea;


    public V2Resistance(Item item, double factor) {
        super(item);
        this.factor = factor;
        type = Type.FLUIDRESISTANCE;
        evalAreas();
    }

    public V2Resistance(Item item, String xmlStr) throws Exception {
        super(item);
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "factor", 0);
        try {
            factor = Double.valueOf(vp.val);
            evalAreas();
        } catch (NumberFormatException e) {
            throw new Exception("in V2Resistance: " + e.getMessage());
        }
    }

    private void evalAreas() {
        frictionArea = item.getProjectedArea();
    }
    public Vector3d getForce() {
        Vector3d force;
        double vel = item.status.velocity.length();
        if (vel > 0) {
            double forceMagnitude = -Math.pow(vel, 2) * factor * frictionArea; // opposing the velocity
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
