package Applications;

import GeneralElements.link.ItemLink;
import evaluations.EvalOnce;

import java.util.concurrent.Callable;

/**
 * Created by M Viswanathan on 14 Aug 2014
 */
public class LinkEvaluator implements EvalOnce {
    ItemLink itemLink;
    public LinkEvaluator(ItemLink itemLink) {
        this.itemLink = itemLink;
    }
    public void evalOnce() {
//        return true;
        itemLink.evalForce();
    }

    //dummy
    @Override
    public void evalOnce(double deltaT, double nowT) {

    }
}
