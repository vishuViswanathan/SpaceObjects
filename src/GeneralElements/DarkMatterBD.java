package GeneralElements;

import GeneralElements.globalActions.GlobalAction;
import GeneralElements.link.ItemLink;
import GeneralElements.localActions.LocalAction;
import mvUtils.physics.VectorBD;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.Vector;

/**
 * Created by mviswanathan on 30-07-2017.
 */
public class DarkMatterBD extends DarkMatter {

    public DarkMatterBD(Window parent) {
        super(parent);
        netForce = new VectorBD();
    }

    public DarkMatterBD(String name, double mass, double dia, Color color, Window parent) {
        super(name, mass, dia, color, parent);
        netForce = new VectorBD();
    }

    public void initNetForce() {
        ((VectorBD)netForce).setTuple(0, 0, 0); // this may not be correct
    }

    public void setMatterLocalForces() {
        ((VectorBD)netForce).setTuple(0, 0, 0);
        for (LocalAction action : localActions)
            ((VectorBD)netForce).addTuple(action.getForce());
        for (GlobalAction gAction : space.getActiveGlobalActions())
            ((VectorBD)netForce).addTuple(gAction.getForce(this));
    }

    public synchronized void addToForce(Vector3d addForce)  {
        ((VectorBD)netForce).addTuple(addForce);
    }

    public synchronized void subtractFromForce(Vector3d subtractForce) {
        ((VectorBD)netForce).subtractTuple(subtractForce);
    }

    public boolean updatePAndV(double deltaT, double nowT, boolean bFinal) throws Exception {
        ((VectorBD)netForce).update3dValues();
        return super.updatePAndV(deltaT, nowT, bFinal);
    }

    }
