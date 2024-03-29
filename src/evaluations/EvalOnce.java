package evaluations;

import GeneralElements.ItemInterface;

/**
 * Created by M Viswanathan on 17 Aug 2014
 */
public interface EvalOnce {
    public void evalOnce();
    public void evalOnce(double nowT, double deltaT, boolean bFinal);
    public void evalOnce(double deltaT, double nowT, ItemInterface.UpdateStep updateStep);
}
