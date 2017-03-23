package GeneralElements;

import mvUtils.physics.ForceElement;
import time.timePlan.FlightPlan;
import time.timePlan.ForceSource;
import time.timePlan.JetController;
import time.timePlan.OneJetPlan;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 12 Mar 2017
 */
public class Jet {
    String name;
    OneJetPlan thePlan;
    ForceElement jetData;
    boolean active;

    public Jet(String name, ForceElement jetData) {
        this.name = name;
        this.jetData = jetData;
    }

    public void noteFlighPlan(OneJetPlan thePlan) {
        this.thePlan = thePlan;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    Vector3d getForce() {
//        System.out.println("active " + active);
        if (active)
            return jetData.getForce();
        else
            return new Vector3d();
    }

    Vector3d getTorque() {
        if (active)
            return jetData.getTorque();
        else
            return new Vector3d();
    }

}
