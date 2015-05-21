package evaluations;

import GeneralElements.link.ItemLink;

/**
 * Created by M Viswanathan on 17 Aug 2014
 */
public class EvaluateLink implements EvalOnce {
    ItemLink link;

    public EvaluateLink(ItemLink link) {
        this.link = link;
    }

    public synchronized void evalOnce() {
        link.evalForce();
    }

    @Override
    public void evalOnce(double deltaT, double nowT, boolean bFinal) {

    }
}
