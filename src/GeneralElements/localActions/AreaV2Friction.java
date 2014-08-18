package GeneralElements.localActions;

import GeneralElements.Item;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 13 Aug 2014
 */
public class AreaV2Friction extends LocalAction {
    double factor;
    double frictionArea;

    public AreaV2Friction(Item item, double factor) {
        super(item);
        this.factor = factor;
        frictionArea = item.getSurfaceArea();
    }

    public Vector3d getForce() {
        Vector3d force;
        double vel = item.status.velocity.length();
        if (vel > 0) {
            double forceMagnitude = -Math.pow(vel, 2) * factor * frictionArea; // opposing the velocity
            double scaleFactor = forceMagnitude / vel;
            force = new Vector3d(item.status.velocity);
            force.scale(scaleFactor);
        }
        else
            force = new Vector3d();
        return force;
    }

}
