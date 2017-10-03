package GeneralElements.link;

import GeneralElements.Constants;
import GeneralElements.DarkMatter;
import GeneralElements.ItemSpace;
import GeneralElements.ItemStat;
import mvUtils.physics.Vector3dMV;

import javax.vecmath.Vector3d;

/**
 * Created by mviswanathan on 26-09-2017.
 */
public class Gravity extends Influence {
    double totalMass;
    ItemSpace space;

    public Gravity(DarkMatter itemOne, DarkMatter itemTwo, ItemSpace space) {
        this.space = space;
        type = Type.GRAVITY;
        hasDetails = false;
        item1 = itemOne;
        item2 = itemTwo;
    }

    public Vector3d accDueToG() {
        boolean retVal = true;
        Vector3d distVect = new Vector3d();
        distVect.sub(item2.status.pos, item1.status.pos); // vector item1 towards item2
        double distance = distVect.length();
        Vector3dMV accVector = new Vector3dMV();
        double acc = item2.gm / distance / distance; // attraction force
        double ratio = acc / distance;
        if (!Double.isNaN(ratio)) {
            accVector.set(distVect);
            accVector.scale(ratio, distVect);
        }
        return accVector;
    }

    public Vector3d accDueToG(Vector3d nowPos) {
        boolean retVal = true;
        Vector3d distVect = new Vector3d();
        distVect.sub(item2.status.pos, nowPos); // vector item1 towards item2
        double distance = distVect.length();
        Vector3dMV accVector = new Vector3dMV();
        double acc = item2.gm / distance / distance; // attraction force
        double ratio = acc / distance;
        if (!Double.isNaN(ratio)) {
            accVector.set(distVect);
            accVector.scale(ratio, distVect);
        }
        return accVector;
    }

    void trace(String msg) {
        System.out.println("InterItem:" + msg);
    }
}
