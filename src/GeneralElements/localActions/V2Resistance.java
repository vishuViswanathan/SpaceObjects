package GeneralElements.localActions;

import GeneralElements.DarkMatter;
import GeneralElements.Item;
import mvXML.ValAndPos;
import mvXML.XMLmv;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 13 Aug 2014
 */
public class V2Resistance extends LocalAction {
    double factor;
    double frictionArea;


    public V2Resistance(Item item, double factor) {
        this(factor);
        setItem(item);
    }

    public V2Resistance(double factor) {
        super(Type.FLUIDRESISTANCE);
        this.factor = factor;
    }

    public V2Resistance(DarkMatter item, String xmlStr) throws Exception {
        super(Type.FLUIDRESISTANCE, item);
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "factor", 0);
        try {
            factor = Double.valueOf(vp.val);
            evalAreas();
        } catch (NumberFormatException e) {
            throw new Exception("in V2Resistance: " + e.getMessage());
        }
    }

    public void setItem(DarkMatter item) {
        super.setItem(item);
        evalAreas();
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

    public Object clone() {
        V2Resistance cloned = (V2Resistance)super.clone();
        return cloned;
    }
}
