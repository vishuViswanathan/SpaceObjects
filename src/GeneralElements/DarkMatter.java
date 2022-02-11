package GeneralElements;

import GeneralElements.globalActions.GlobalAction;
import GeneralElements.link.Gravity;
import GeneralElements.localActions.LocalAction;
import evaluations.EvalOnce;
import mvUtils.display.InputControl;
import mvUtils.physics.Point3dMV;
import mvUtils.physics.Vector3dMV;
//import time.timePlan.FlightPlan;

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
    //    Vector<ItemLink> links;
    Vector<Gravity> gravityLinks;
    boolean gravityON = false;
    public ItemSpace space;
    public ItemStat status;
    boolean bFixedLocation = false;
    Vector<LocalAction> localActions;
    public Vector3d tempForce = new Vector3d();  // used if required instead of creating a new object each time
    Vector3d gravityForce = new Vector3d();
    Vector3d localForce = new Vector3d();
    Vector3d addVelocity = new Vector3d();
    Vector3d jetForce = new Vector3d();
    Vector3d globalForce =new Vector3d();
    Vector3d lastGravityforce = new Vector3dMV();
    Vector3d lastForce = new Vector3dMV();
    Point3dMV lastPosition = new Point3dMV();
    Vector3dMV lastVelocity = new Vector3dMV();
    //    Vector3d effectiveLastVelocity = new Vector3d();
    Vector3d lastAcc = new Vector3d();
    Vector3dMV effectiveForce = new Vector3dMV();
    Vector3dMV thisAcc = new Vector3dMV();
    Vector3dMV effectiveAcc = new Vector3dMV();
    Vector3dMV nowAcc = new Vector3dMV();
    Vector3dMV deltaV = new Vector3dMV();
    Vector3dMV newVelocity = new Vector3dMV();
    Vector3dMV averageV = new Vector3dMV();
    Vector3dMV deltaPos = new Vector3dMV();
    Vector3dMV newPos = new Vector3dMV();
    public String name;
    public String gmID; // for Horizons
    public double mass;
    public double gm;
    protected double totalGM;
    protected double balanceGM;
    public double oneByMass;
    public double dia;
    public double radius;
    double projectedArea;
    double surfaceArea;
    double eCompression; // for elastic material
    double stickingPressure = 0; // in N/m2 of the area of contact (of the flattened sphere for eg.
    double collisionLossFactor = 1;
    boolean canStick = false;
    public Color color;
    public boolean boundaryItem = false;

    public DarkMatter(Window parent) {
        this.parentW = parent;
        gravityLinks = new Vector<>();
        gravityON = false;
//        localActions = new Vector<>();
    }

    public DarkMatter(String name, double mass, double dia, Color color, Window parent) {
        this(parent);
//        String[] split = name.split("-");
//        if (split.length > 1) {
//            this.name = split[1].trim() + "-" + split[0].trim();
//        }
//        else
        this.name = name;
        setMass(mass);
//        this.mass = mass;
        this.dia = dia;
        radius = dia / 2;
        this.color = color;
        status = new ItemStat();
        calculateAreas();
    }

    public void setMass(double mass) {
        this.mass = mass;
        gm = mass * Constants.G;
        oneByMass = 1 / mass;
    }

    public void setGM(double gm) {
        this.gm = gm;
        setMass(gm / Constants.G);
    }

    public void setGMid(String gmID) {
        this.gmID = gmID;
    }

    public boolean takeBasicFrom(DarkMatter fromMatter) {
        if (fromMatter.getClass().equals(getClass())){
            setMass(fromMatter.mass);
//            mass = fromMatter.mass;
            dia = fromMatter.dia;
            radius = dia / 2;
            eCompression = fromMatter.eCompression;
            return true;
        }
        else
            return false;
    }

    boolean isElastic() {
        return (eCompression > 0);
    }

    public void seteCompression(double eCompression) {
        this.eCompression = eCompression;
    }

    public void touchedBy(double nowT, DarkMatter it) {}

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

//    public void addLocalAction(LocalAction action) {
//        action.setItem(this);
//        localActions.add(action);
//    }

    public void clearGravityLinks() {
        gravityLinks.clear();
        gravityON = false;
    }

    public double getSurfaceArea() {
        return surfaceArea;
    }

    public double getProjectedArea() {
        return projectedArea;
    }

    public Vector3d getNormal() {return null;}

    public void setStickingPressure(double stickingPressure) {
        this.stickingPressure = stickingPressure;
        canStick = (stickingPressure > 0);
    }

    public void setSpace(ItemSpace space) {
        this.space = space;
    }

    public void initPosEtc(Point3d pos, Vector3d velocity) {
        status.initPos(pos, velocity);
    }

    public float getDiaFloat() {
        return (float)dia;
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


    public void initNetForce() {
        gravityForce.set(0, 0,0);
        localForce.set(0, 0, 0); // this may not be correct
        addVelocity.set(0, 0, 0);
    }

    public void setMatterStartConditions(double duration, double nowT) {
        if (!bFixedLocation) {
            lastPosition.set(status.pos);
            lastVelocity.set(status.velocity);
            lastAcc.set(status.acc);
            lastForce.set(localForce);
            lastGravityforce.set(gravityForce);
        }
    }

    public void setMatterLocalForces() {
        localForce.set(0, 0, 0);
        globalForce.set(0, 0, 0);
//        for (LocalAction action : localActions)
//            localForce.add(action.getForce());
        for (GlobalAction gAction : space.getActiveGlobalActions())
            globalForce.add(gAction.getForce(this));
    }

    public synchronized void addToLocalForce(Vector3d addForce)  {
        localForce.add(addForce);
    }

    public synchronized void addToGraviryForce(Vector3d addForce)  {
        gravityForce.add(addForce);
    }

    public synchronized void addToAddVelocity(Vector3d addVel)  {
        addVelocity.add(addVel);
    }

    public synchronized void subtractFromLocalForce(Vector3d subtractForce) {
        localForce.sub(subtractForce);
    }

    public synchronized void subtractFromGravityForce(Vector3d subtractForce) {
        gravityForce.sub(subtractForce);
    }

    public synchronized void addToTorque(Vector3d angularAcceleration)  {
    }

    public synchronized void addToAngularVel(Vector3d angularVel) {
    }

    public synchronized void subtractFromTorque(Vector3d angularAcceleratioon)  {
    }

    public synchronized void subtractFromVel(Vector3d angularVel) {
    }

    public Vector3d distanceVector(Point3d fromPoint) {
        Vector3d distance = new Vector3d(status.pos);
        distance.sub(fromPoint);
        return distance;
    }

    // dummy not used
    @Override
    public synchronized void evalOnce() {
    }

    public void evalOnce(double nowT, double deltaT, boolean bFInal) {
    }

    @Override
    public void evalOnce(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) {
//        try {
//            updatePAndV(deltaT, nowT, updateStep);
//        } catch (Exception e) {
//            ItemMovementsApp.log.error("In Item evalOnce for " + name + ":" + e.getMessage());
//            e.printStackTrace();
//        }
    }

    public boolean updatePAndVforContactJetGlobal(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception {
        boolean changed = true;
        if (bFixedLocation)
            changed = false;
        else {
            switch (updateStep) {
                case INTERMEDIATE:
                    considerContactJetGlobal(nowT, deltaT, false);
                    break;
                case FINAL:
                case EuFwd:
                case EUMod:
                case RK2:
                case RK4:
                    considerContactJetGlobal(nowT, deltaT, true);
                    break;
                default:
                    changed = false;
                    break;
            }
        }
        return changed;
    }

    boolean considerContactJetGlobal(double nowT, double deltaT, boolean bFinal) {
        lastAcc = status.acc;
        effectiveForce.add(localForce, globalForce);
        effectiveForce.add(jetForce);
        nowAcc.scale(oneByMass, effectiveForce);
        effectiveAcc.setMean(nowAcc, lastAcc);
        deltaV.scale(deltaT, effectiveAcc);
        newVelocity.add(lastVelocity, deltaV);
        averageV.setMean(lastVelocity, newVelocity);
        deltaPos.scale(deltaT, averageV);
        newPos.add(lastPosition, deltaPos);
        status.pos.set(newPos); // only position is updated here
        if (bFinal) {
            status.velocity.set(newVelocity);
            status.acc.set(nowAcc);
            status.time = nowT + deltaT;
        }
        return true;
    }

    public boolean updatePAndVforGravityJetGlobal(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception {
        boolean changed = true;
        if (bFixedLocation)
            changed = false;
        else {
            switch (updateStep) {
                case INTERMEDIATE:
                    considerGravityJetGlobal(nowT, deltaT, false);
                    break;
                case FINAL:
                case EuFwd:
                case EUMod:
                case RK2:
                case RK4:
                    considerGravityJetGlobal(nowT, deltaT, true);
                    break;
                default:
                    changed = false;
                    break;
            }
        }
        return changed;
    }

    boolean considerGravityJetGlobal(double nowT, double deltaT, boolean bFinal) {
        boolean changed = true;
        if (bFixedLocation)
            changed = false;
        else {
            // add mean gravity force, jet force and global force
            effectiveForce.setMean(gravityForce, lastGravityforce);
            effectiveForce.add(jetForce);
            effectiveForce.add(globalForce);
            thisAcc.scale(oneByMass, effectiveForce);
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
                status.time = nowT + deltaT;
//                if (bFlightPlan)
//                    mass += flightPlan.massChange(deltaT);
            }
        }
        return changed;
    }

    // CHECK 202020402 for Bounce Jet and Global, all are without repeat
//    public boolean updatePAndVforBounceJetGlobal(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception {
//        boolean changed = true;
//        if (bFixedLocation)
//            changed = false;
//        else {
//            switch (updateStep) {
//                case INTERMEDIATE:
//                    considerBounceJetGlobal(nowT, deltaT, false);
//                    break;
//                case FINAL:
//                case EuFwd:
//                case EUMod:
//                case RK2:
//                case RK4:
//                    considerBounceJetGlobal(nowT, deltaT, true);
//                    break;
//                default:
//                    changed = false;
//                    break;
//            }
//        }
//        return changed;
//    }
//
//    // CHECK 202020402 for Bounce Jet and Global, all are without repeat
//    boolean considerBounceJetGlobal(double nowT, double deltaT, boolean bFinal) {
//        if (bFinal){
//            lastAcc = status.acc;
////            lastVelocity = status.velocity;
////            lastPosition = status.pos;
//            effectiveForce.add(globalForce, jetForce);
//            nowAcc.scale(oneByMass, effectiveForce);
//            effectiveAcc.setMean(nowAcc, lastAcc);
//            deltaV.scale(deltaT, effectiveAcc);
//            lastVelocity.add(addVelocity);
//            newVelocity.add(lastVelocity, deltaV);
//            averageV.setMean(lastVelocity, newVelocity);
//            deltaPos.scale(deltaT, averageV);
//            newPos.add(lastPosition, deltaPos);
//            status.pos.set(newPos);
//            status.velocity.set(newVelocity);
//            status.acc.set(nowAcc);
//            status.time = nowT + deltaT;
//        }
//        return true;
//    }
//
//    public boolean updatePAndVforBounce(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception {
//        boolean changed = true;
//        if (bFixedLocation)
//            changed = false;
//        else {
//            switch (updateStep) {
//                case INTERMEDIATE:
//                    considerAddVelocityOnly(nowT, deltaT,false);
//                    break;
//                case FINAL:
//                case EuFwd:
//                case EUMod:
//                case RK2:
//                case RK4:
//                    considerAddVelocityOnly(nowT, deltaT,true);
//                    break;
//                default:
//                    changed = false;
//                    break;
//            }
//        }
//        return changed;
//    }
//
//    boolean considerAddVelocityOnly(double nowT, double deltaT, boolean bFinal) {
//        if (bFinal) {
//            lastPosition = status.pos;
//            status.velocity.add(addVelocity);
//            deltaPos.scale(deltaT, status.velocity);
//            newPos.add(lastPosition, deltaPos);
//            status.pos.set(newPos);
//            status.time = nowT + deltaT;
//        }
//        return true;
//    }

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
