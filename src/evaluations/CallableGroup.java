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

    public void start() {
        run = true;
    }

//    public void setRunOn() {
//        run = true;
//    }

    public int add(EvalOnce oneEval) {
        group.add(oneEval);
        return group.size();
    }

    int c = 0;

/*
    public Boolean call() {
//        debug("just called - id =" + id);
        while(run) {
            try {
//                run = false;
//                debug("" + id + ": before start");
                startBarrier.await();
//                System.out.println("" + id + ": after start");
                for (EvalOnce oneEval : group)
                    oneEval.evalOnce();
                finishBarrier.await();
//                debug("c = " + c++);
//                System.out.println("" + id + ": after finish Barrier");
            } catch (InterruptedException e) {
                ItemMovementsApp.log.error("In CallableGroup call - Interruption: " + e.getMessage());
                run = false;
            } catch (BrokenBarrierException e) {
                if (run)
                    ItemMovementsApp.log.error("In CallableGroup call - Broken Barrier: " + e.getMessage());
                run = false;
            }
        }
        debug("call completed");
        return true;
    }
*/

    public Boolean call() {
        try {
            startBarrier.await();
        } catch (InterruptedException e) {
            ItemMovementsApp.log.info("In CallableGroup call - before run Loop: " + e.getMessage());
        } catch (BrokenBarrierException e) {
            ItemMovementsApp.log.info("In CallableGroup call - before run Loop: " + e.getMessage());
        }
        while(run) {
            try {
                evalOnce();
                finishBarrier.await();
                startBarrier.await();
            } catch (InterruptedException e) {
                ItemMovementsApp.log.error("In CallableGroup call - Interruption: " + e.getMessage());
                run = false;
            } catch (BrokenBarrierException e) {
                if (run)
                    ItemMovementsApp.log.error("In CallableGroup call - Broken Barrier: " + e.getMessage());
                // else the barrier was broken with intentional reset
                run = false;
            }
        }
        debug("call completed");
        return true;
    }

    void evalOnce() {
        for (EvalOnce oneEval : group)
            oneEval.evalOnce();
    }

    void debug(String msg) {
        System.out.println("CallableGroup #" + id + ": " + msg);
    }
}
