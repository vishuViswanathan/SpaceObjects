package evaluations;

/**
 * Created by M Viswanathan on 18 Aug 2014
 */
public class CallableItemGroup extends CallableGroup {
    double deltaT;
    double nowT;

    public CallableItemGroup(int id) {
        super(id);
    }

    public void setTimes(double deltaT, double nowT) {
        this.deltaT = deltaT;
        this.nowT = nowT;
    }

    @Override
    public Boolean call() {
        for (EvalOnce oneEval:group)
            oneEval.evalOnce(deltaT, nowT);
        return super.call();
    }
}
