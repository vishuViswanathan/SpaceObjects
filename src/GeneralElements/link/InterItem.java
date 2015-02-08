package GeneralElements.link;

import GeneralElements.DarkMatter;
import SpaceElements.Constants;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 08 Feb 2015
 * For inter item Gravity, contact force etc.
 */
public class InterItem extends Influence {
    boolean gravityON = false;
    double limitDistance;
    double factorLbyR;

    public InterItem (DarkMatter item1, DarkMatter item2, boolean gravityON) {
        type = Type.INTERITEM;
        hasDetails = false;
        this.item1 = item1;
        this.item2 = item2;
        this.gravityON = gravityON;
        double r1 = item1.getDia() / 2;
        double r2 = item2.getDia() / 2;
        limitDistance = r1 + r2;
        double e1 = item1.getECompression();
        double e2 = item2.getECompression();
        if (e1 > 0 && e2 > 0)
            factorLbyR = 1 / (r1 / e1 + r2 / e2);
        else factorLbyR = 0;
    }

    @Override
    public boolean evalForce() {
        boolean retVal = true;
        Vector3d distVect = new Vector3d();
        distVect.sub(item2.status.pos, item1.status.pos);
        double r = distVect.length();
        double compression = r - limitDistance;
        Vector3d nowForce = new Vector3d();
        if (compression > 0 && (factorLbyR > 0)) {
            nowForce.set(distVect);
            double force = (item1.mass / r) * Constants.G * (item2.mass / r);
            double ratio = force / r;
            if (Double.isNaN(ratio)) {
                retVal = false;
            } else {
                nowForce.scale(ratio);
            }
        }
        if (gravityON) {
            double gForceValue = (item1.mass / r) * Constants.G * (item2.mass / r);
            double ratio = gForceValue / r;
            if (Double.isNaN(ratio)) {
                retVal = false;
            } else {
                Vector3d gForce = new Vector3d(distVect);
                gForce.scale(ratio);
                nowForce.add(gForce);
             }
        }
        if (retVal) {
            item1.addToForce(nowForce);
            nowForce.negate();
            item2.addToForce(nowForce);
        }
        return retVal;
    }
}
