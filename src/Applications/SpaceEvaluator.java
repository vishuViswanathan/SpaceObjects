package Applications;

import GeneralElements.ItemInterface;
import evaluations.CallableGroup;
import evaluations.CallableItemGroup;

import java.util.Vector;
import java.util.concurrent.*;

/**
 * Created by M Viswanathan on 10 Aug 2014
 */
public class SpaceEvaluator implements Runnable {
    boolean useAllCPUs = true;
    int numberOfCPUs;
    int numberOfCPUsToUse;
    private volatile static SpaceEvaluator theEvaluator;
    private volatile Thread t;
    private String threadName = "Calculation Thread";
    boolean fresh = true;
    volatile boolean execute;
    ItemMovementsApp callerApp;
    static ExecutorService pool;
    static CompletionService<Boolean> completionService;
    Vector<CallableGroup> linkCallables;
    Vector<CallableItemGroup> itemCallables;

    private SpaceEvaluator(ItemMovementsApp callerApp, boolean fresh) {
        super();
        numberOfCPUs = Runtime.getRuntime().availableProcessors();
        numberOfCPUsToUse = numberOfCPUs;
        this.callerApp = callerApp;
        useAllCPUs = callerApp.useAllCPUs();
        initiateService();
        linkCallables = new Vector<CallableGroup>();
        itemCallables = new Vector<CallableItemGroup>();
        this.fresh = fresh;
    }

    private void initiateService() {
        pool = Executors.newCachedThreadPool(); //newFixedThreadPool(numberOfCPUsToUse);
        completionService = new ExecutorCompletionService<Boolean>(pool);
    }

    static public SpaceEvaluator getSpaceEvaluator(ItemMovementsApp callerApp, boolean fresh) {
        synchronized (SpaceEvaluator.class) {
            theEvaluator = new SpaceEvaluator(callerApp, fresh);
        }
        theEvaluator.fresh = fresh;
        theEvaluator.execute = true;
        return theEvaluator;
    }

    CyclicBarrier startLinkCalculations;
    CyclicBarrier forcesReady;
    CyclicBarrier startUpdatePositions;
    CyclicBarrier positionsReady;

    private void prepareLinkCallables() {
        linkCallables.clear();
        int count = 0;
        int id = 0;
        int nLinks = callerApp.nItemLinks();
        int eachCallableLen = nLinks / numberOfCPUsToUse + 1;
        CallableGroup group = new CallableGroup(id++);
        boolean bFirstTime = true;
        for (int l = 0 ; l < nLinks; l++) {
            if (count >= eachCallableLen) {
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
        int taskCount = linkCallables.size();
        startLinkCalculations = new CyclicBarrier(taskCount + 1);
        forcesReady = new CyclicBarrier(taskCount + 1); // one for the 'this' thread
        for (CallableGroup gr: linkCallables)
            gr.setBarriers(startLinkCalculations, forcesReady);
        submitLinkTasks();
    }

    public void resetStartLinkBarrier() {
        startLinkCalculations.reset();
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

    public int awaitPositionsReady()  {
        int retVal;
        try {
            retVal = positionsReady.await();
        } catch (InterruptedException e) {
            ItemMovementsApp.log.error("In awaitPositionsReady: " + e.getMessage());
            retVal = -2;
        } catch (BrokenBarrierException e) {
            ItemMovementsApp.log.error("In awaitPositionsReady: " + e.getMessage());
            retVal = -1;
        }
        return retVal;
    }


    public int awaitStartLinkCalculations()  {
        int retVal;
        try {
            retVal = startLinkCalculations.await();
        } catch (InterruptedException e) {
            ItemMovementsApp.log.error("InterruptedException StartLinkCalculations Barrier in SpaceEvaluator: " + e.getMessage());
            retVal = -2;
        } catch (BrokenBarrierException e) {
            ItemMovementsApp.log.error("BrokenBarrierException StartLinkCalculations Barrier in SpaceEvaluator: " + e.getMessage());
            retVal = -1;
        }
        return retVal;
    }

    public int awaitStartUpdatePositions()  {
        int retVal;
        try {
            retVal = startUpdatePositions.await();
        } catch (InterruptedException e) {
            ItemMovementsApp.log.error("InterruptedException awaitStartUpdatePositions Barrier in SpaceEvaluator: " + e.getMessage());
            retVal = -2;
        } catch (BrokenBarrierException e) {
            ItemMovementsApp.log.error("BrokenBarrierException awaitStartUpdatePositions Barrier in SpaceEvaluator: " + e.getMessage());
            retVal = -1;
        }
        return retVal;
    }

    private void prepareItemCallables() {
        itemCallables.clear();
        int count = 0;
        int id = 0;
        int nItems = callerApp.nItems();
        int eachCallableLen = nItems / numberOfCPUsToUse + 1;
        CallableItemGroup group = new CallableItemGroup(id++);
        boolean bFirstTime = true;
        for (int l = 0 ; l < nItems; l++) {
            if (count >= eachCallableLen) {
                if (!bFirstTime)
                    itemCallables.add(group);
                group = new CallableItemGroup(id++);
                count = 0;
            }
            group.add(callerApp.getItemEvalOnce(l));
            bFirstTime = false;
            count++;
        }
        itemCallables.add(group);
        int taskCount = itemCallables.size();
        startUpdatePositions = new CyclicBarrier(taskCount + 1);
        positionsReady = new CyclicBarrier(taskCount + 1); // one for the 'this' thread
        for (CallableGroup gr: itemCallables)
            gr.setBarriers(startUpdatePositions, positionsReady);
        submitItemTasks();
    }


    public void stopTasks() {
        for (CallableGroup group: linkCallables)
            group.stop();
        startLinkCalculations.reset();
        forcesReady.reset();
        for (CallableGroup group:itemCallables)
            group.stop();
        startUpdatePositions.reset();
        positionsReady.reset();
    }

    public void startTasks() {
//        prepareLinkCallables();
//        for (CallableGroup group: linkCallables)
//            group.start();
        prepareItemCallables();
        for (CallableGroup group: itemCallables)
            group.start();
        prepareLinkCallables();
        for (CallableGroup group: linkCallables)
            group.start();
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

    private void submit(CallableGroup callableGroup) {
        completionService.submit(callableGroup);
    }

    public void submitLinkTasks() {
        for (CallableGroup  group: linkCallables)
            submit(group);

    }

    public void submitItemTasks() {
        for (CallableItemGroup  group: itemCallables) {
            submit(group);
        }
    }

    double nowT = 0;

    public double getNowT() {
        return nowT;
    }

    public void setTimes(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) {
        this.nowT = nowT;
        for (CallableItemGroup  group: itemCallables) {
            group.setTimes(deltaT, nowT, updateStep);
        }
    }

    public void run() {
        callerApp.debug("SpaceEvaluator.#257: started" +
                ((useAllCPUs) ? " with " + numberOfCPUsToUse + " CPUs" : " One CPU"));
        if (useAllCPUs)
            callerApp.doCalculationPARELLEL(fresh);
        else
            callerApp.doCalculationSERIAL(fresh);
    }

    public void start() {
//        System.out.println("Starting " +  threadName );
        t = new Thread (this, threadName);
        t.start ();
    }
}
