package GeneralElements.link;

import GeneralElements.Item;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 29 May 2014
 */
public class Rope  extends Influence  {

    public Rope(Item item1, Item item2, double freeLen, double kExpansion) {
        type = Type.ROPE;
        this.item1 = item1;
        this.item2 = item2;
        this.freeLen = freeLen;
        this.kExpansion = kExpansion;
    }

    @Override
    public boolean evalForce() {
        Vector3d distVect = new Vector3d();
        distVect.sub(item2.status.pos, item1.status.pos);
        double r = distVect.length();
        Vector3d nowForce = new Vector3d();
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
        return true;
    }
}

