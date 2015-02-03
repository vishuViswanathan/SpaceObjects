package GeneralElements;

import Applications.ItemMovementsApp;
import GeneralElements.globalActions.GlobalAction;
import GeneralElements.link.ItemLink;
import GeneralElements.localActions.LocalAction;
import evaluations.EvalOnce;
import mvUtils.Vector3dMV;
import mvUtils.display.InputControl;

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
//    Vector3dMV dirOfFixedGravityAcc;  // direction of fixed Acceleration, a unit Vector
    double fixedAcc = 9.81; // fixed acceleration value
    Vector<LocalAction> localActions;
//    Vector3d forceOfFixedGravity;
//    boolean bFixedForceOn = false;
    Vector3d force = new Vector3d();
    public String name;
    public double mass;
    public double dia;
    double projectedArea;
    double surfaceArea;
    public Color color;

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
        this.color = color;
        status = new ItemStat();
        calculateAreas();
//        dirOfFixedGravityAcc = new Vector3dMV(0, -1, 0);
    }

    public Vector<LocalAction> getLocalActions() {
        return localActions;
    }

    public void addInfluence(ItemLink itemLink) {
        links.add(itemLink);
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

//    public void setbFixedForceOn(boolean set) {
//        bFixedForceOn = set;
//    }

    public void setSpace(ItemSpace space) {
        this.space = space;
    }

    public void initPosEtc(Point3d pos, Vector3d velocity) {
        status.initPos(pos, velocity);
    }

    public float getDiaFloat() {
        return new Float(dia);
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
        force.set(0, 0, 0); // this may not be correct
    }

    public void setStartConditions() {
        lastPosition.set(status.pos);
        lastVelocity.set(status.velocity);
        lastForce.set(force);
    }

    public void setLocalForces() {
//        if (bFixedForceOn)
//            force.set(forceOfFixedGravity);
//        else
            force.set(0, 0, 0);
        for (LocalAction action : localActions)
            force.add(action.getForce());
        for (GlobalAction gAction : space.getActiveGlobalActions())
            force.add(gAction.getForce(this));
    }

    public void addToForce(Vector3d addForce) {
        force.add(addForce);
    }

    // dummy not used
    @Override
    public void evalOnce() {
    }

    @Override
    public void evalOnce(double deltaT, double nowT) {
        try {
            updatePosAndVel(deltaT, nowT, true);
        } catch (Exception e) {
            ItemMovementsApp.log.error("In ITem evalOnce for " + name + ":" + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean updatePosAndVel(double deltaT, double nowT, boolean bFinal) throws Exception {
        boolean changed = true;
        if (bFixedLocation)
            changed = false;
        else {
            effectiveForce.setMean(force, lastForce);
            thisAcc.scale((1.0/ mass), effectiveForce);
            // calculate from force
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
            }
        }
        return changed;
    }

    public boolean updatePosAndVelOLD(double deltaT, double nowT, boolean bFinal) throws Exception {
        boolean changed = true;
        if (bFixedLocation)
            changed = false;
        else {
            effectiveForce.set(force);
            effectiveForce.add(lastForce);
            effectiveForce.scale(0.5); // the average force

            Vector3d thisAcc = new Vector3d(effectiveForce);
            thisAcc.scale((1.0 / mass));
            // calculate from force
            Vector3d deltaV = new Vector3d(effectiveForce);
            deltaV.scale(deltaT);
            deltaV.scale(1.0 / mass);
            Vector3d averageV = new Vector3d(deltaV);
            averageV.scaleAdd(+0.5, lastVelocity); //
            Point3d newPos = new Point3d(averageV);
            newPos.scale(deltaT);
            newPos.add(lastPosition);
            status.pos.set(newPos); // only position is updated here
            Vector3d newVelocity = new Vector3d(lastVelocity);
            newVelocity.add(deltaV);
            status.velocity.set(newVelocity);
            if (bFinal) {
                status.acc.set(thisAcc);
                status.time = nowT;
                lastForce.set(force);  // note down the force for the last calculation
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
