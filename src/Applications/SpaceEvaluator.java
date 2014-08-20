package Applications;

import evaluations.CallableGroup;
import evaluations.CallableItemGroup;

import java.util.Vector;
import java.util.concurrent.*;


/**
 * Created by M Viswanathan on 10 Aug 2014
 */
public class SpaceEvaluator implements Runnable {
    private volatile static SpaceEvaluator theEvaluator;
    private volatile Thread t;
    private String threadName = "Calculation Thread";
    boolean fresh = true;
//    volatile boolean execute;
    ItemMovementsApp callerApp;
    static ExecutorService pool;
    static int nThreads = 3;
    static CompletionService<Boolean> completionService;
    Vector<CallableGroup> linkCallables;
    Vector<CallableItemGroup> itemCallables;
    private int eachCallableLen = 5;  // this many number of smaller tasks combined as one Callable task


    private SpaceEvaluator(ItemMovementsApp callerApp, boolean fresh) {
        super();
        this.callerApp = callerApp;
        pool = Executors.newFixedThreadPool(nThreads);
        completionService = new ExecutorCompletionService<Boolean>(pool);
        linkCallables = new Vector<CallableGroup>();
        itemCallables = new Vector<CallableItemGroup>();
//        execute = true;
        this.fresh = fresh;
    }

    static public SpaceEvaluator getSpaceEvaluator(ItemMovementsApp callerApp, boolean fresh) {
        if (theEvaluator == null)
            synchronized (SpaceEvaluator.class) {
                theEvaluator = new SpaceEvaluator(callerApp, fresh);
            }
        theEvaluator.fresh = fresh;
//        theEvaluator.execute = true;
        theEvaluator.prepareLinkCallables();
//        theEvaluator.prepareItemCallables();
        return theEvaluator;
    }

    CyclicBarrier startOneRound;
    CyclicBarrier forcesReady;
    CyclicBarrier roundComplete;

    private void prepareLinkCallables() {
        linkCallables.clear();
        int count = 0;
        int id = 0;
        CallableGroup group = new CallableGroup(id++);
        boolean bFirstTime = true;
        for (int l = 0 ; l < callerApp.nItemLinks(); l++) {
            if (count > eachCallableLen) {
                if (!bFirstTime)
                    linkCallables.add(group);
                group = new CallableGroup(id++);
                count = 0;
            }
            group.add(callerApp.getLinkEvalOnce(l));
            bFirstTime = false;
            count++;
        }
        linkCallables.add(group);
        taskCount = linkCallables.size();
        startOneRound = new CyclicBarrier(taskCount + 1);
        forcesReady = new CyclicBarrier(taskCount + 1); // one for the 'this' thread
        for (CallableGroup gr: linkCallables)
            gr.setBarriers(startOneRound, forcesReady);
        submitLinkTasks();
    }

    public void resetStartBarrier() {
        startOneRound.reset();
    }

    public void resetForceBarrier() {
        forcesReady.reset();
    }

    public int awaitForceComplete()  {
        int retVal;
        try {
            retVal = forcesReady.await();
        } catch (InterruptedException e) {
            ItemMovementsApp.log.error("In awaitForceComplete: " + e.getMessage());
            retVal = -2;
        } catch (BrokenBarrierException e) {
            ItemMovementsApp.log.error("In awaitForceComplete: " + e.getMessage());
            retVal = -1;
        }
        return retVal;
    }


    public int awaitStartBarrier()  {
        int retVal;
        try {
            retVal = startOneRound.await();
        } catch (InterruptedException e) {
            ItemMovementsApp.log.error("In awaitStartBarrier in SpaceEvaluator: " + e.getMessage());
            retVal = -2;
        } catch (BrokenBarrierException e) {
            ItemMovementsApp.log.error("In awaitStartBarrier in SpaceEvaluator: " + e.getMessage());
            retVal = -1;
        }
        return retVal;
    }


    private void prepareItemCallables() {
        itemCallables.clear();
        int count = 0;
        int id = 100;
        CallableItemGroup group = new CallableItemGroup(id++);
        boolean bFirstTime = true;
//        boolean yetToSave = false;
        for (int l = 0 ; l < callerApp.nItems(); l++) {
            if (count > eachCallableLen) {
                if (!bFirstTime)
                    itemCallables.add(group);
                group = new CallableItemGroup(id++);
                count = 0;
            }
            group.add(callerApp.getItemEvalOnce(l));
            count++;
            bFirstTime = false;
        }
//        if (yetToSave)
            itemCallables.add(group);
    }

    public void stopTasks() {
        for (CallableGroup group: linkCallables)
            group.stop();
        for (CallableGroup group:itemCallables)
            group.stop();
    }

    public static boolean closePool() {
        boolean retVal = true;
        if (pool != null) {
            pool.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    pool.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                        retVal = false;
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                pool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
        return retVal;
    }

    int taskCount = 0;

    private void submit(CallableGroup callableGroup) {
        completionService.submit(callableGroup);
        taskCount++;
    }

    public void submitLinkTasks() {
        for (CallableGroup  group: linkCallables)
            submit(group);

    }

    public void submitItemTasks(double deltaT, double nowT) {
        for (CallableItemGroup  group: itemCallables) {
            group.setTimes(deltaT, nowT);
            submit(group);
        }
    }

    public boolean isComplete() {
        try {
            for (int i = 0; i < taskCount; i++)
                completionService.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        taskCount = 0;
        return true;
    }

    public void run() {
        callerApp.debug("SpaceEvaluator started");
        callerApp.doCalculation(fresh);
//        callerApp.doCalculationFast(fresh);
    }

    public void start() {
        System.out.println("Starting " +  threadName );
        t = new Thread (this, threadName);
        t.start ();
    }
}
