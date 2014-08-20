package evaluations;

import Applications.ItemMovementsApp;

import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by M Viswanathan on 17 Aug 2014
 */
public class CallableGroup implements Callable<Boolean> {
    Vector<EvalOnce> group;
    CyclicBarrier startBarrier;
    CyclicBarrier finishBarrier;
    boolean run = true;
    int id;
    public CallableGroup(int id) {
        group = new Vector<EvalOnce>();
        this.id = id;
    }

    public void setBarriers(CyclicBarrier startBarrier, CyclicBarrier finishBarrier) {
        this.finishBarrier = finishBarrier;
        this.startBarrier = startBarrier;
    }

    public void stop() {
        run = false;
    }

//    public void setRunOn() {
//        run = true;
//    }

    public int add(EvalOnce oneEval) {
        group.add(oneEval);
        return group.size();
    }

    public Boolean call() {
        while(run) {
            try {
//                run = false;
                startBarrier.await();
//                System.out.println("" + id + ": after start");
                for (EvalOnce oneEval : group)
                    oneEval.evalOnce();
                finishBarrier.await();
//                System.out.println("" + id + ": after finish Barrier");
            } catch (InterruptedException e) {
                ItemMovementsApp.log.error("In awaitForceComplete: " + e.getMessage());
                run = false;
            } catch (BrokenBarrierException e) {
                ItemMovementsApp.log.error("In awaitForceComplete: " + e.getMessage());
                run = false;
            }
        }
        return true;
    }
}
