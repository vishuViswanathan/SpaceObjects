package GeneralElements.link;

import GeneralElements.DarkMatter;
import SpaceElements.Constants;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 24 May 2014
 */
public class Gravity extends Influence  {

    public Gravity (DarkMatter item1, DarkMatter item2) {
        type = Type.GRAVITY;
        hasDetails = false;
        this.item1 = item1;
        this.item2 = item2;
    }

    @Override
    public boolean evalForce() {
        boolean retVal = true;
        Vector3d distVect = new Vector3d();
        distVect.sub(item2.status.pos, item1.status.pos);
        double r = distVect.length();
        double force  =  (item1.mass / r) * Constants.G * (item2.mass / r) ;
        double ratio = force / r;
        if (Double.isNaN(ratio) ) {
            retVal = false;
        }
        else {
            Vector3d nowForce = new Vector3d(distVect);
            nowForce.scale(ratio);
            item1.addToForce(nowForce);
            nowForce.negate();
            item2.addToForce(nowForce);
        }
        return retVal;
    }
}
