package evaluations;

import GeneralElements.ItemInterface;

/**
 * Created by M Viswanathan on 18 Aug 2014
 */
public class CallableItemGroup extends CallableGroup {
    double deltaT;
    double nowT;
    ItemInterface.UpdateStep updateStep;
    public CallableItemGroup(int id) {
        super(id);
    }

    public void setTimes(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) {
        this.deltaT = deltaT;
        this.nowT = nowT;
        this.updateStep = updateStep;
    }

/*
    @Override
    public Boolean call() {
//        debug("just called - id =" + id);
        while(run) {
            try {
//                run = false;
//                debug("" + id + ": before start");
                startBarrier.await();
//                System.out.println("" + id + ": after start");
                for (EvalOnce oneEval:group)
                    oneEval.evalOnce(deltaT, nowT, bFinal);
                finishBarrier.await();
//                debug("c = " + c++);
//                System.out.println("" + id + ": after finish Barrier");
            } catch (InterruptedException e) {
                ItemMovementsApp.log.error("In CallableItemGroup call - Interruption: " + e.getMessage());
                run = false;
            } catch (BrokenBarrierException e) {
                if (run)
                    ItemMovementsApp.log.error("In CallableItemGroup call - Broken Barrier: " + e.getMessage());
                run = false;
            }
        }
        debug("call completed");
        return true;
    }
*/

    void evalOnce() {
        for (EvalOnce oneEval:group)
            oneEval.evalOnce(deltaT, nowT, updateStep);
    }


    void debug(String msg) {
        System.out.println("CallableItemGroup #" + id + ": " + msg);
    }
}
