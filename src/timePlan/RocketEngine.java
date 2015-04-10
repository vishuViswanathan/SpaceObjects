package timePlan;

import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;

import javax.swing.*;

/**
 * Created by M Viswanathan on 28 Mar 2015
 */
public class RocketEngine implements ForceSource {
    double force; // magnitude
    double fuelExhaustRate; // kg/s of fuel exhaust

    public RocketEngine(double force, double fuelExhaustRate) {
        this.force = force;
        this.fuelExhaustRate = fuelExhaustRate;
    }

    public RocketEngine clone() {
        return new RocketEngine(force, fuelExhaustRate);
    }

    public double fuelLoss(double duration) {
        return duration * fuelExhaustRate;
    }

    @Override
    public boolean anyMassChange() {
        return true;
    }

    @Override
    public double effectiveForce() {
        return force;
    }

    @Override
    public double massChange(double duration) {
        return fuelExhaustRate * duration;
    }

    NumberTextField ntForce;
    NumberTextField ntFuelExhaustRate;
    boolean uiReady = false;

    @Override
    public JPanel fsDetails() {
        MultiPairColPanel outerP = new MultiPairColPanel("Details of " + this);
        ntForce = new NumberTextField(null, force, 6, false, 0.001, 1e10, "#,##0.000", "Force (N)");
        ntFuelExhaustRate = new NumberTextField(null, fuelExhaustRate, 6, false, 0.001, 10000, "#,##0.000", "Fuel Exhaust rate (kg/s)");
        outerP.addItemPair(ntForce);
        outerP.addItemPair(ntFuelExhaustRate);
        uiReady = true;
        return outerP;
    }

    public boolean fsTakeDataFromUI() {
        boolean retVal = true;
        if (uiReady) {
            force = ntForce.getData();
            fuelExhaustRate = ntFuelExhaustRate.getData();
        }
        return retVal;
    }

    @Override
    public boolean anyDetails() {
        return true;
    }

    @Override
    public String dataAsString() {
        return "Force " + force + "N" +
                ((fuelExhaustRate > 0) ? "Fuel Exhaust Rate " + fuelExhaustRate + " kg/s" : "");
    }

    public String toString() {
        return "Rocket Engine";
    }
}
