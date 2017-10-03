package GeneralElements;

import Applications.ItemMovementsApp;
import GeneralElements.globalActions.GlobalAction;
import GeneralElements.link.Gravity;
import GeneralElements.link.InterItem;
import GeneralElements.link.ItemLink;
import GeneralElements.localActions.LocalAction;
import evaluations.EvalOnce;
import mvUtils.display.InputControl;
import mvUtils.physics.Vector3dMV;
//import time.timePlan.FlightPlan;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
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
    Vector3d netForce = new Vector3d();
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
        localActions = new Vector<>();
    }

    public DarkMatter(String name, double mass, double dia, Color color, Window parent) {
        this(parent);
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

    public void addGravityLink(Gravity oneGravityLink) {
        gravityLinks.add(oneGravityLink);
        gravityON = true;
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
    Vector3d lastAcc = new Vector3d();
    Vector3dMV effectiveForce = new Vector3dMV();
    Vector3dMV effectiveAcc = new Vector3dMV();
    Vector3dMV nowAcc = new Vector3dMV();
    Vector3dMV deltaV = new Vector3dMV();
    Vector3dMV newVelocity = new Vector3dMV();
    Vector3dMV averageV = new Vector3dMV();
    Vector3dMV deltaPos = new Vector3dMV();
    Vector3dMV newPos = new Vector3dMV();

    public void initNetForce() {
        netForce.set(0, 0, 0); // this may not be correct
    }

    public void setMatterStartConditions(double duration, double nowT) {
        if (!bFixedLocation) {
            lastPosition.set(status.pos);
            lastVelocity.set(status.velocity);
            lastAcc.set(status.acc);
            lastForce.set(netForce);
        }
    }

    public void setMatterLocalForces() {
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

    public synchronized void addToTorque(Vector3d angularAcceleration)  {
    }

    public synchronized void addToAngularVel(Vector3d angularVel) {

    }

    public synchronized void subtractFromTorque(Vector3d angularAcceleratioon)  {
    }

    public synchronized void subtractFromVel(Vector3d angularVel) {

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
            updatePAndV(deltaT, nowT, bFinal);
        } catch (Exception e) {
            ItemMovementsApp.log.error("In Item evalOnce for " + name + ":" + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean updatePAndV(double deltaT, double nowT, boolean bFinal) throws Exception {
        boolean changed = true;
        if (bFixedLocation)
            changed = false;
        else {
            if (netForce.length() > 0) {
                effectiveForce.set(netForce);
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
                }
            }
            if (gravityON)
                considerGravityEffectEuModified(deltaT, nowT);
            status.time = nowT + deltaT; // 20170724
        }
        return changed;
    }

    boolean considerGravityEffect(Vector3d acc) {
        boolean changed = true;
        if (bFixedLocation)
            changed = false;
        else {

            for (Gravity oneG: gravityLinks) {
                acc.add(oneG.accDueToG());
            }
        }
        return changed;
    }

    public boolean considerGravityEffectEuModified(double deltaT, double nowT) {
        boolean changed = false;
        double deltaTBy2 = deltaT / 2;
        double deltaTBy6 = deltaT / 6;
        TwoVectors pvBase = new TwoVectors(status.pos, status.velocity);

        TwoVectors pv = new TwoVectors(pvBase);
        TwoVectors pv1 = new TwoVectors(pv, bFixedLocation);

        pv = new TwoVectors(pvBase, deltaT, pv1);
        TwoVectors pv2 = new TwoVectors(pv, bFixedLocation);

        pv1.makeMeanWith(pv2);
        pvBase.scaleAndAdd(deltaT, pv1);
        status.pos.set(pvBase.v1);
        status.velocity.set(pvBase.v2);

//            status.time = nowT + deltaT;
        changed = true;
        return changed;
    }

    public boolean considerGravityEffectRK4(double deltaT, double nowT) {
        boolean changed = false;
        double deltaTBy2 = deltaT / 2;
        double deltaTBy6 = deltaT / 6;
        TwoVectors pvBase = new TwoVectors(status.pos, status.velocity);

        TwoVectors pv = new TwoVectors(pvBase);
        TwoVectors pv1 = new TwoVectors(pv, bFixedLocation);

        pv = new TwoVectors(pvBase, deltaTBy2, pv1);
        TwoVectors pv2 = new TwoVectors(pv, bFixedLocation);

        pv = new TwoVectors(pvBase, deltaTBy2, pv2);
        TwoVectors pv3 = new TwoVectors(pv, bFixedLocation);

        pv = new TwoVectors(pvBase, deltaT, pv2);
        TwoVectors pv4 = new TwoVectors(pv, bFixedLocation);

//            // EulerFwd
//            pvBase.scaleAndAdd(deltaT, pv1);

        pv1.scaleAndAdd(2, pv2);
        pv1.scaleAndAdd(2, pv3);
        pv1.add(pv4);

        pvBase.scaleAndAdd(deltaTBy6, pv1);
        status.pos.set(pvBase.v1);
        status.velocity.set(pvBase.v2);

//            status.time = nowT + deltaT;
        changed = true;
        return changed;
    }

    class TwoVectors {
        Vector3dMV v1;
        Vector3dMV v2;
        TwoVectors(Tuple3d v1, Tuple3d v2) {
            this.v1 = new Vector3dMV(v1);
            this.v2 = new Vector3dMV(v2);
        }

        TwoVectors(TwoVectors va) {
            this(va.v1, va.v2);
        }

        TwoVectors(TwoVectors base, double scale, TwoVectors scaleThis) {
            this(base);
            scaleAndAdd(scale, scaleThis);
        }

        TwoVectors scaleAndAdd(double factor, TwoVectors scaleThis) {
            v1.scaleAndAdd(scaleThis.v1, factor);
            v2.scaleAndAdd(scaleThis.v2, factor);
            return this;
        }

        TwoVectors(TwoVectors pAndV, boolean bFixed) {
            v1 = pAndV.v2;
            if (!bFixed) {
                v2 = new Vector3dMV();
                for (Gravity oneG: gravityLinks) {
                    v2.add(oneG.accDueToG(pAndV.v1));
                }
            }
        }

        void makeMeanWith(TwoVectors withThis) {
            v1.scale(0.5);
            v1.scaleAndAdd(withThis.v1, 0.5);
            v2.scale(0.5);
            v2.scaleAndAdd(withThis.v2, 0.5);
        }

        void add(TwoVectors addThis) {
            v1.add(addThis.v1);
            v2.add(addThis.v2);
        }

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
