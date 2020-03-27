package GeneralElements;

import java.awt.*;

public class LiveItem extends Item {
    static Color defaultColor = Color.cyan;
    static Color infectedColor = Color.RED;
    static Color curedColor = Color.green;
    static double curingTime = 50; // in seconds

    boolean touched = false;
    boolean infected = false;
    boolean cured = false;
    int nContacts = 0;
    int nContactsWithInfected = 0;
    double willBeCuredAt = 0;


    public LiveItem(Window parent) {
        super(parent);
        itemType = ItemType.LIVE;
        color = defaultColor;
    }

    public LiveItem(String name, double mass, double dia, boolean bInfected, Window parent) {
        super(name, mass, dia, defaultColor, parent);
        itemType = ItemType.SPHERE;
        setRadioButtons();
        thisItem = this;
        if (bInfected)
            markInfected(0);
    }


    public boolean markInfected(double nowT) {
        boolean retVal = false;
        if (!infected && !cured) {
            infected = true;

            color = infectedColor;
            willBeCuredAt = nowT + curingTime;
            retVal = true;
        }
        return retVal;
    }

    void noteIfCured(double nowT) {
        if (infected) {
            if (nowT >= willBeCuredAt) {
                markCured();
            }
        }
    }

    void markCured() {
        cured = true;
        infected = false;
        color = curedColor;
    }

    public void touchedBy(double nowT, DarkMatter it) {
        if ((it instanceof LiveItem) && ((LiveItem)it).infected) {
            markInfected(nowT);
            nContactsWithInfected++;
        }
        touched = true;
        nContacts++;
    }

    public boolean updatePAndV(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception {
        noteIfCured(nowT);
        return super.updatePAndV(deltaT, nowT, updateStep);
    }

    public StringBuilder statusStringForCSV(double posFactor, double velFactor) {
        StringBuilder csvStr = new StringBuilder(name + "," + gmID + "," + gm + "\n");
        csvStr.append("Position," + status.positionStringForCSV(posFactor) + "\n");
        csvStr.append("Velocity,").append(status.velocityStringForCSV(velFactor)).append("\n");
        csvStr.append("AngVel,").append(status.angularVelocityStringForCSV(1)).append("\n");
        return csvStr;
    }

    public StringBuilder statusStringForHistory(double posFactor, double velFactor) {
        StringBuilder csvStr = super.statusStringForHistory(posFactor, velFactor);
        csvStr.append("," + ((infected) ? "1" : "0") + "," + ((cured) ? "1" : "0"));
        return csvStr;
    }


}
