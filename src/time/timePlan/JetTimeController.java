package time.timePlan;

import GeneralElements.Item;
import GeneralElements.Jet;

import java.util.Collection;
import java.util.Hashtable;

/**
 * Created by M Viswanathan on 17 Mar 2017
 */
public class JetTimeController extends JetController{
    Item item;
    Hashtable<Jet, OneJetPlan> jetAndPlan;

//    Vector<OneJetPlan> controlTable;

    public JetTimeController(Item item) {
        this.item = item;
        jetAndPlan = new Hashtable<Jet, OneJetPlan>();
    }

    public boolean addOneJet(Jet jet) {
        boolean retVal = false;
        if (!jetAndPlan.containsKey(jet)) {
            OneJetPlan onePlan = new OneJetPlan(jet);
            jetAndPlan.put(jet, onePlan);
            onePlan.noteController(this);
            retVal = true;
        }
        return retVal;
    }

    public void removeOneJet(Jet jet) {
        if (jetAndPlan.containsKey(jet)) {
            jetAndPlan.remove(jet);
        }

    }

    public boolean addOneJetPlanStep(Jet jet, double startTime, double duration) {
        boolean retVal = false;
        if (jetAndPlan.containsKey(jet)) {
            try {
                jetAndPlan.get(jet).addOneStep(new OneTimeStep(startTime, duration));
                retVal = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return retVal;
    }

    @Override
    public void upDateAllJetStatus(double duration, double nowTime) {
        Collection<OneJetPlan> plans = jetAndPlan.values();
        for (OneJetPlan j: plans)
            j.updateJetStatus(duration, nowTime);
    }
}
