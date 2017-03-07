package time.timePlan;

import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.swing.*;

/**
 * Created by M Viswanathan on 28 Mar 2015
 */
public class RocketEngine implements ForceSource {
    // SaturnV First stage thrustInkN = 7000 kN
    String engineName = "Engine";
    double effExhVelocity;  // effective exhaust velocity of gases
    double specificFuelConsumption = 300; // in g/kN.s
    double thrustInkN;
    double force; // in N
    double fuelExhaustRate; // kg/s of fuel exhaust
    boolean bValid = true;

    /**
     *
     * @param thrustInkN in kN
     * @param specificFuelConsumption in g/kN.s
     */
    public RocketEngine(String engineName, double thrustInkN, double specificFuelConsumption) {
        this.engineName = new String(engineName);
        this.thrustInkN = thrustInkN;
        this.specificFuelConsumption = specificFuelConsumption;
    }

    public RocketEngine(String xmlStr) {
        bValid = takeFromXML(xmlStr);
    }

    public RocketEngine clone(){
        return new RocketEngine(engineName, thrustInkN, specificFuelConsumption);
    }

    @Override
    public double effectiveForce() {
        return force;
    }

    @Override
    public double massChange(double duration) {
        return -fuelExhaustRate * duration;
    }

    void evalForceAndFuelExhRate() {
        force = thrustInkN * 1000; // converted to N
        fuelExhaustRate = thrustInkN * specificFuelConsumption / 1000; // converted to kg/s
    }

    JTextField tfName;
    NumberTextField ntForce;
    NumberTextField ntSpFuelConsumption;
    boolean uiReady = false;

    @Override
    public JPanel fsDetails() {
        MultiPairColPanel outerP = new MultiPairColPanel("Details of " + this);
        tfName = new JTextField(engineName, 20);
        ntForce = new NumberTextField(null, thrustInkN, 6, false, 0.001, 1e10, "#,##0.000", "Force (kN)");
        ntSpFuelConsumption = new NumberTextField(null, specificFuelConsumption, 6, false, 0, 10000, "#,##0.000",
                "Specific Fuel Consumption(g/kN.s)");
        outerP.addItemPair("Engine Name", tfName);
        outerP.addItemPair(ntForce);
        outerP.addItemPair(ntSpFuelConsumption);
        uiReady = true;
        return outerP;
    }

    public boolean fsTakeDataFromUI() {
        boolean retVal = true;
        if (uiReady) {
            engineName = tfName.getText();
            thrustInkN = ntForce.getData();
            specificFuelConsumption = ntSpFuelConsumption.getData();
            evalForceAndFuelExhRate();
        }
        return retVal;
    }

    @Override
    public String dataAsString() {
        return engineName + " with thrust "+ thrustInkN + "kN" +
                ((specificFuelConsumption > 0) ? ", specificFuelConsumption " + specificFuelConsumption + " g/kN.s" : "");
    }

    @Override
    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("engineName", engineName));
        xmlStr.append(XMLmv.putTag("thrustInkN", thrustInkN));
        xmlStr.append(XMLmv.putTag("specificFuelConsumption",  specificFuelConsumption));
        return xmlStr;
    }

    @Override
    public boolean takeFromXML(String xmlStr) throws NumberFormatException {
        boolean retVal = false;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "engineName", 0);
        engineName = vp.val;
        vp = XMLmv.getTag(xmlStr, "thrustInkN", 0);
        thrustInkN = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "specificFuelConsumption", 0);
        specificFuelConsumption = Double.valueOf(vp.val);
        evalForceAndFuelExhRate();
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
