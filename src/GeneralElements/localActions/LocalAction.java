package GeneralElements.localActions;

import GeneralElements.Item;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 13 Aug 2014
 */
public class LocalAction {
    Item item;
    public LocalAction(Item item) {
        this.item = item;
    }
    public Vector3d getForce() {
        return new Vector3d();
    }
}
