package time.timePlan;

/**
 * Created by M Viswanathan on 18 Mar 2017
 */
public class OneTimeStep {
    public double startTime;
    public double endTime;

    public OneTimeStep(double startTime, double duration) {
        this.startTime = startTime;
        this.endTime = startTime + duration;
    }
}
