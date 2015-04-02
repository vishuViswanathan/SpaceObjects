package timePlan;

import GeneralElements.DarkMatter;
import mvUtils.display.InputControl;

import javax.vecmath.Vector3d;
import java.util.Vector;

/**
 * Created by M Viswanathan on 28 Mar 2015
 */
public class FlightPlan {
    InputControl inpC;
    DarkMatter item;
    Vector<OneStep> thePlan;
    int planSize;
    // while running the flightPlan
    double lastStepTime = 0;
    int lastStepPos = 0;
    OneStep theLastStep;


    public FlightPlan(DarkMatter item, InputControl inpC) {
        this.item = item;
        this.inpC = inpC;
    }

    /**
     * @param oneStep
     * @return the total steps of the plan
     */
    public int addStep(OneStep oneStep) {
        oneStep.setFlightPlan(this);
        oneStep.setStartTime((thePlan.size() > 0) ? getTotalTime() : 0);
        if (thePlan.size() == 0)
            theLastStep = oneStep;
        thePlan.add(oneStep);
        planSize = thePlan.size();
        return thePlan.size();
    }

    public double getTotalTime() {
        if (thePlan.size() > 0)
            return thePlan.lastElement().endTime;
        else
            return 0;
    }

    protected Vector<OneStep> getThePlan() {
        return thePlan;
    }

    public Vector3d getForce(double duration) {
        Vector3d force = null;
        if (lastStepPos < planSize) {
            double nowStepTime = lastStepTime + duration;
            if (theLastStep.endTime >= nowStepTime) {
                force = theLastStep.getEffectiveForce();
            } else {
                if (++lastStepPos > planSize) {
                    theLastStep = thePlan.get(lastStepPos);
                    force = getForce(duration);
                }
            }
            if (force != null) {
                lastStepTime = nowStepTime;
            }
        }
        return force;
    }

    public Vector3d getForce(Vector3d force, double duration) {
        if (lastStepPos < planSize) {
            double nowStepTime = lastStepTime + duration;
            if (theLastStep.endTime >= nowStepTime) {
               theLastStep.getEffectiveForce(force);
            } else {
                if (++lastStepPos > planSize) {
                    theLastStep = thePlan.get(lastStepPos);
                    getForce(force, duration); // recursive
                }
            }
            if (force != null) {
                lastStepTime = nowStepTime;
            }
        }
        return force;
    }



    public boolean isActive() {
        return (lastStepPos < planSize);
    }

}