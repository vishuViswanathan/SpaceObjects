package evaluations;

/**
 * Created by M Viswanathan on 18 Aug 2014
 */
public class CallableItemGroup extends CallableGroup {
    double deltaT;
    double nowT;
    boolean bFinal;
    public CallableItemGroup(int id) {
        super(id);
    }

    public void setTimes(double deltaT, double nowT, boolean bFinal) {
        this.deltaT = deltaT;
        this.nowT = nowT;
        this.bFinal = bFinal;
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
            oneEval.evalOnce(deltaT, nowT, bFinal);
    }


    void debug(String msg) {
        System.out.println("CallableItemGroup #" + id + ": " + msg);
    }
}
