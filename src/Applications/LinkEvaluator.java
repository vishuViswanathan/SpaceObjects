package Applications;

import GeneralElements.ItemInterface;
import GeneralElements.link.ItemLink;
import evaluations.EvalOnce;

/**
 * Created by M Viswanathan on 14 Aug 2014
 */
public class LinkEvaluator implements EvalOnce {
    ItemLink itemLink;
    public LinkEvaluator(ItemLink itemLink) {
        this.itemLink = itemLink;
    }
    public synchronized void evalOnce() {
//        return true;
        itemLink.evalForce();
    }

    @Override
    public void evalOnce(double deltaT, boolean bFinal) {
        itemLink.evalForce(deltaT, bFinal);
    }

    //dummy
    @Override
    public void evalOnce(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) {

    }
}
