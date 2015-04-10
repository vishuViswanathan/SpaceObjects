package timePlan;

import GeneralElements.DarkMatter;
import mvUtils.display.InputControl;

import javax.vecmath.Vector3d;
import java.util.Vector;

/**
 * Created by M Viswanathan on 28 Mar 2015
 */
public class FlightPlan implements Cloneable{
    InputControl inpC;
    DarkMatter item;
    Vector<OneStep> theSteps;
    int planSize;
    // while running the flightPlan
    double lastStepTime = 0;
    int lastStepPos = 0;
    OneStep theLastStep;


    public FlightPlan(DarkMatter item, InputControl inpC) {
        this.item = item;
        this.inpC = inpC;
        theSteps = new Vector<OneStep>();
        planSize = 0;
    }

    public FlightPlan(FlightPlan cloneFrom) {
        this(cloneFrom.item, cloneFrom.inpC);
        for (OneStep oneStep: cloneFrom.theSteps)
            addStep(oneStep.clone());
    }

    public FlightPlan clone() {
        FlightPlan newPlan = new FlightPlan(item, inpC);
        for (OneStep oneStep: theSteps)
            newPlan.addStep(oneStep.clone());
        return newPlan;
    }

    public boolean copyFrom(FlightPlan copyFrom) {
        boolean retVal = false;
        if (copyFrom.item == item) {
            theSteps.clear();
            for (OneStep oneStep: copyFrom.theSteps)
                addStep(oneStep.clone());
            retVal = true;
        }
        return retVal;
    }

    /**
     * @param oneStep
     * @return the total steps of the plan
     */
    public int addStep(OneStep oneStep) {
        oneStep.setFlightPlan(this);
        oneStep.setStartTime(isValid() ? getTotalTime() : 0);
        if (theSteps.size() == 0)
            theLastStep = oneStep;
        theSteps.add(oneStep);
        planSize = theSteps.size();
        return theSteps.size();
    }

    public double getTotalTime() {
        if (theSteps.size() > 0)
            return theSteps.lastElement().endTime;
        else
            return 0;
    }

    protected Vector<OneStep> getTheSteps() {
        return theSteps;
    }

    public boolean isValid() {
        return (theSteps.size() > 0);
    }

    public Vector3d getForce(double duration) {
        Vector3d force = null;
        if (lastStepPos < planSize) {
            double nowStepTime = lastStepTime + duration;
            if (theLastStep.endTime >= nowStepTime) {
                force = theLastStep.getEffectiveForce();
            } else {
                if (++lastStepPos > planSize) {
                    theLastStep = theSteps.get(lastStepPos);
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
                    theLastStep = theSteps.get(lastStepPos);
                    getForce(force, duration); // recursive
                }
            }
            if (force != null) {
                lastStepTime = nowStepTime;
            }
        }
        return force;
    }

    protected int getPlanSize() {
        return planSize;
    }

    protected OneStep getOneStep(int stepNum) {
        if (stepNum < planSize)
            return theSteps.get(stepNum);
        else
            return null;
    }

    protected void removeOneStep(int stepNum) {
        if (stepNum < theSteps.size())
            theSteps.remove(stepNum);
        planSize = theSteps.size();
    }

    public boolean isActive() {
        return (lastStepPos < planSize);
    }

}