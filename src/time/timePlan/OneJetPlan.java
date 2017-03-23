package time.timePlan;

import GeneralElements.Jet;

import java.util.Vector;

/**
 * Created by M Viswanathan on 18 Mar 2017
 */
public class OneJetPlan {
    Jet theJet;
    JetController theController;
    Vector<OneTimeStep> theSteps;
    int planSize;
    // while running the flightPlan
    double nowStepTime = 0;
    int stepPos = 0;
    OneTimeStep nowStep;
    boolean bValid = true;

    public OneJetPlan(Jet theJet) {
        this.theJet = theJet;
        theSteps = new Vector<OneTimeStep>();
        theJet.noteFlighPlan(this);
    }

    public void noteController(JetController theController) {
        this.theController = theController;
    }

    public boolean isValid() {
        return (theSteps.size() > 0) && bValid;
    }

    public void updateJetStatus(double duration, double nowT) {
        if (bValid) {
            boolean makeActive = false;
            nowStepTime += duration;
            if (nowStepTime >= nowStep.startTime) {
                if (nowStepTime < nowStep.endTime)
                    makeActive = true;
                else {
                    stepPos++;
                    if (stepPos < planSize)
                        nowStep = theSteps.get(stepPos);
                    else
                        bValid = false;
                }
            }
            theJet.setActive(makeActive);
        }
    }

    /**
     *
     * @param oneStep
     * @return the endTime of the included step
     */
    public double addOneStep(OneTimeStep oneStep) throws Exception {
        if (planSize > 0) {
            if (theSteps.get(planSize -1).endTime > oneStep.startTime)
                throw new Exception("The start time is earlier than the end time of last step");
        }
        theSteps.add(oneStep);
        planSize = theSteps.size();
        nowStep = theSteps.get(stepPos);
        return planSize;
    }
}
