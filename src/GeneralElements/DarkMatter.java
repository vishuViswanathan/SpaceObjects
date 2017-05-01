package GeneralElements;

import Applications.ItemMovementsApp;
import GeneralElements.globalActions.GlobalAction;
import GeneralElements.link.ItemLink;
import GeneralElements.localActions.LocalAction;
import evaluations.EvalOnce;
import mvUtils.display.InputControl;
import mvUtils.physics.Torque;
import mvUtils.physics.Vector3dMV;
import time.timePlan.FlightPlan;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.Vector;

/**
 * Created by M Viswanathan on 23 Sep 2014
 */
public class DarkMatter implements InputControl, EvalOnce {
    public Window parentW;
    Vector<ItemLink> links;
    public ItemSpace space;
    public ItemStat status;
    boolean bFixedLocation = false;
    Vector<LocalAction> localActions;
    public Vector3d tempForce = new Vector3d();  // used if required instead of creating a new object each time
    Vector3d netForce = new Vector3d();
    public String name;
    public double mass;
    public double dia;
    double radius;
    double projectedArea;
    double surfaceArea;
    double eCompression; // for elastic material
    double stickingPressure = 0; // in N/m2 of the area of contact (of the flattened sphere for eg.
    double collisionLossFactor = 1;
    boolean canStick = false;
    public Color color;
    public boolean boundaryItem = false;
    FlightPlan flightPlan;
    boolean bFlightPlan = false;
    double rocketFuelLoss = 0;
    Vector3d rocketForce = new Vector3d(); // TODO this may be removed


    public DarkMatter(Window parent) {
        this.parentW = parent;
        links = new Vector<ItemLink>();
        localActions = new Vector<LocalAction>();
    }

    public DarkMatter(String name, double mass, double dia, Color color, Window parent) {
        this(parent);
        this.name = name;
        this.mass = mass;
        this.dia = dia;
        radius = dia / 2;
        this.color = color;
        status = new ItemStat();
        calculateAreas();
    }

    public boolean takeBasicFrom(DarkMatter fromMatter) {
        if (fromMatter.getClass().equals(getClass())){
            mass = fromMatter.mass;
            dia = fromMatter.dia;
            radius = dia / 2;
            eCompression = fromMatter.eCompression;
            return true;
        }
        else
            return false;
    }

    boolean isElastic() {
        return (eCompression > -2);
    }

    public void seteCompression(double eCompression) {
        this.eCompression = eCompression;
    }

    public Vector<LocalAction> getLocalActions() {
        return localActions;
    }

    public void addInfluence(ItemLink itemLink) {
        links.add(itemLink);
    }

    public double getPositionX() {
        return status.pos.getX();
    }

    public double getPositionY() {
        return status.pos.getY();
    }

    public double getPositionZ() {
        return status.pos.getZ();
    }

    void calculateAreas() {
        surfaceArea = 4 * Math.PI * Math.pow(dia / 2, 2);
        projectedArea =  Math.PI * Math.pow(dia, 2) / 4;
    }

    public void setSurfaceArea(double surfaceArea) {
        this.surfaceArea = surfaceArea;
    }

    public void setProjectedArea(double projectedArea) {
        this.projectedArea = projectedArea;
    }

    public void addLocalAction(LocalAction action) {
        action.setItem(this);
        localActions.add(action);
    }

     public void clearInfluence() {
        links.clear();
    }

    public double getSurfaceArea() {
        return surfaceArea;
    }

    public double getProjectedArea() {
        return projectedArea;
    }

    public boolean canStick() {
        return canStick;
    }

    public double getCollisionLossFactor() {
        return collisionLossFactor;
    }

    public void setStickingPressure(double stickingPressure) {
        this.stickingPressure = stickingPressure;
        canStick = (stickingPressure > 0);
    }

    /**
     * Contact area with a flat surface a flatFaceDistance from item center of sphere
     * @param flatFaceDistance
     * @return
     */
    public double getStickingArea(double flatFaceDistance) {
        double stickingArea = 0;
        if (flatFaceDistance < radius)
            if (flatFaceDistance > 0)
                stickingArea = 2 * Math.PI * (radius- flatFaceDistance) * (dia - flatFaceDistance);
        return stickingArea;
    }

    public double getStickingForce(double onArea) {
        return stickingPressure * onArea;
    }

    public void setSpace(ItemSpace space) {
        this.space = space;
    }

    public void initPosEtc(Point3d pos, Vector3d velocity) {
        status.initPos(pos, velocity);
    }

    public float getDiaFloat() {
        return new Float(dia);
    }

    public double getDia() {
        return dia;
    }

    public double getECompression() {
        return eCompression;
    }

    //    =========================== calculations ======================
    public ItemStat getStatus() {
        return status;
    }

    Vector3d lastForce = new Vector3dMV();
    Vector3d lastPosition = new Vector3d();
    Vector3d lastVelocity = new Vector3d();
    Vector3dMV effectiveForce = new Vector3dMV();
    Vector3dMV thisAcc = new Vector3dMV();
    Vector3dMV deltaV = new Vector3dMV();
    Vector3dMV newVelocity = new Vector3dMV();
    Vector3dMV averageV = new Vector3dMV();
    Vector3dMV deltaPos = new Vector3dMV();
    Vector3dMV newPos = new Vector3dMV();

    public void initStartForce() {
        netForce.set(0, 0, 0); // this may not be correct
    }

    public void setStartConditions(double duration, double nowT) {
        if (!bFixedLocation) {
            lastPosition.set(status.pos);
            lastVelocity.set(status.velocity);
            lastForce.set(netForce);
        }
    }

    public void setLocalForces() {
        netForce.set(0, 0, 0);
        for (LocalAction action : localActions)
            netForce.add(action.getForce());
        for (GlobalAction gAction : space.getActiveGlobalActions())
            netForce.add(gAction.getForce(this));
    }

    public synchronized void addToForce(Vector3d addForce)  {
        netForce.add(addForce);
    }

    public synchronized void subtractFromForce(Vector3d subtractForce) {
        netForce.sub(subtractForce);
    }

    /**
     * This is generally overridden in the subclass
     * @param fromPoint
     * @return
     */
    public Vector3d distanceVector(Point3d fromPoint) {
        Vector3d distance = new Vector3d(status.pos);
        distance.sub(fromPoint);
        return distance;
    }

        // dummy not used
    @Override
    public synchronized void evalOnce() {
    }

    public void evalOnce(double deltaT, boolean bFInal) {
    }

    @Override
    public void evalOnce(double deltaT, double nowT, boolean bFinal) {
        try {
            updatePosAndVel(deltaT, nowT, bFinal);
        } catch (Exception e) {
            ItemMovementsApp.log.error("In Item evalOnce for " + name + ":" + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean updatePosAndVel(double deltaT, double nowT, boolean bFinal) throws Exception {
        boolean changed = true;
        if (bFixedLocation)
            changed = false;
        else {
            effectiveForce.setMean(netForce, lastForce);
            thisAcc.scale((1.0/ mass), effectiveForce);
            // calculate from netForce
            deltaV.scale(deltaT, thisAcc);
            newVelocity.add(lastVelocity, deltaV);
            averageV.setMean(lastVelocity, newVelocity);
            deltaPos.scale(deltaT, averageV);
//            if (deltaPos.length() > 0.25)
//                ItemMovementsApp.log.info("deltaPos for " + name + "at " + nowT + " = " + deltaPos );
            newPos.add(lastPosition, deltaPos);
            status.pos.set(newPos); // only position is updated here
            if (bFinal) {
                status.velocity.set(newVelocity);
                status.acc.set(thisAcc);
                status.time = nowT;
                if (bFlightPlan)
                    mass += flightPlan.massChange(deltaT);
            }
        }
        return changed;
    }

     public void showError(String msg) {
        JOptionPane.showMessageDialog(parentW, name + " has some problem at " + status.time + " \n " + msg, "ERROR", JOptionPane.ERROR_MESSAGE);
        parentW.toFront();
    }

    void showMessage(String msg) {
        JOptionPane.showMessageDialog(parentW, msg, "FOR INFORMATION", JOptionPane.INFORMATION_MESSAGE);
        parentW.toFront();
    }

    @Override
    public boolean canNotify() {
        return true;
    }

    @Override
    public void enableNotify(boolean b) {

    }

    @Override
    public Window parent() {
        return null;
    }

    public String toString() {
        return name;
    }
}
