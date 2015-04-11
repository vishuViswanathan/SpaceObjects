package timePlan;

import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.swing.*;

/**
 * Created by M Viswanathan on 28 Mar 2015
 */
public class RocketEngine implements ForceSource {
    double force; // magnitude
    double fuelExhaustRate; // kg/s of fuel exhaust
    boolean bValid = true;

    public RocketEngine(double force, double fuelExhaustRate) {
        this.force = force;
        this.fuelExhaustRate = fuelExhaustRate;
    }

    public RocketEngine(String xmlStr) {
        bValid = takeFromXML(xmlStr);
    }

    public RocketEngine clone(){
        return new RocketEngine(force, fuelExhaustRate);
    }

    @Override
    public double effectiveForce() {
        return force;
    }

    @Override
    public double massChange(double duration) {
        return -fuelExhaustRate * duration;
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
    public String dataAsString() {
        return "Force " + force + "N" +
                ((fuelExhaustRate > 0) ? "Fuel Exhaust Rate " + fuelExhaustRate + " kg/s" : "");
    }

    @Override
    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("name", "" + this));
        xmlStr.append(XMLmv.putTag("force", force));
        xmlStr.append(XMLmv.putTag("fuelExhaustRate",  fuelExhaustRate));
        return xmlStr;
    }

    @Override
    public boolean takeFromXML(String xmlStr) throws NumberFormatException {
        boolean retVal = false;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "force", 0);
        force = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "fuelExhaustRate", 0);
        fuelExhaustRate = Double.valueOf(vp.val);
        retVal = true;
        return retVal;
    }

    @Override
    public boolean isValid() {
        return bValid;
    }

    public String toString() {
        return "Rocket Engine";
    }
}
