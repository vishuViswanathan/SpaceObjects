package evaluations;

import java.util.Vector;
import java.util.concurrent.Callable;

/**
 * Created by M Viswanathan on 17 Aug 2014
 */
public class CallableGroup implements Callable<Boolean> {
    Vector<EvalOnce> group;

    public CallableGroup() {
        group = new Vector<EvalOnce>();
    }

    public int add(EvalOnce oneEval) {
        group.add(oneEval);
        return group.size();
    }

    public Boolean call() {
        for (EvalOnce oneEval:group)
            oneEval.evalOnce();
        return true;
    }
}
