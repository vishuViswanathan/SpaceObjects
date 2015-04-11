package timePlan;

import GeneralElements.DarkMatter;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.vecmath.Vector3d;
import java.util.Vector;

/**
 * Created by M Viswanathan on 28 Mar 2015
 */
public class FlightPlan implements Cloneable{
//    InputControl inpC;
    DarkMatter item;
    Vector<OneStep> theSteps;
    int planSize;
    // while running the flightPlan
    double lastStepTime = 0;
    int lastStepPos = 0;
    OneStep theLastStep;
    boolean bValid = true;


    public FlightPlan(DarkMatter item) {
        this.item = item;
//        this.inpC = inpC;
        theSteps = new Vector<OneStep>();
        planSize = 0;
    }

    public FlightPlan(FlightPlan cloneFrom) {
        this(cloneFrom.item);
        for (OneStep oneStep: cloneFrom.theSteps)
            addStep(oneStep.clone());
    }

    public FlightPlan(DarkMatter item, String xmlStr) {
        this(item);
        bValid = takeFromXML(xmlStr);
    }

    public FlightPlan clone() {
        FlightPlan newPlan = new FlightPlan(item);
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

    public Vector3d getForce(Vector3d force, double duration) {
        if (lastStepPos < planSize) {
            double nowStepTime = lastStepTime + duration;
            if (theLastStep.endTime >= nowStepTime) {
               theLastStep.getEffectiveForce(force);
            } else {
                if (++lastStepPos < planSize) {
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

    public double massChange(double duration) {
        if (lastStepPos < planSize)
            return theLastStep.massChange(duration);
        else
            return 0;
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

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("item", item.name));
        xmlStr.append(XMLmv.putTag("planSize", "" + planSize));
        if (planSize > 0) {
            int s = 0;
            for (OneStep oneStep: theSteps)
                xmlStr.append(XMLmv.putTag("s" + ("" + (++s)).trim(), oneStep.dataInXML()));
        }
        return xmlStr;
    }

    boolean takeFromXML(String xmlStr) {
        boolean retVal = false;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "item", 0);
        if (vp.val.equals(item.name)) {
            vp = XMLmv.getTag(xmlStr, "planSize", 0);
            int newPlanSize = Integer.valueOf(vp.val);
            if (newPlanSize > 0) {
                theSteps = new Vector<OneStep>();
                OneStep oneStep;
                for (int s = 1; s <= newPlanSize; s++) {
                    vp = XMLmv.getTag(xmlStr, "s" + ("" + s).trim(), vp.endPos);
                    oneStep = new OneStep(vp.val);
                    if (oneStep.bValid)
                        addStep(oneStep);
                    else
                        break;
                }
            }
            retVal = true;
        }
        return retVal;
    }
}