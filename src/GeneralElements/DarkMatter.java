package GeneralElements;

import Applications.ItemMovementsApp;
import GeneralElements.globalActions.GlobalAction;
import GeneralElements.link.Gravity;
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
    Vector3d addVelocity = new Vector3d();
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

    public void touchedBy(double nowT, DarkMatter it) {};

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

    public Vector3d getNormal() {return null;}

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
    Vector3d effectiveLastVelocity = new Vector3d();
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
        addVelocity.set(0, 0, 0);
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

    public synchronized void addToAddVelocity(Vector3d addVel)  {
        addVelocity.add(addVel);
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

    public boolean updatePAndVOLD(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception {
        boolean changed = false;
        if (bFixedLocation)
            changed = false;
        else {
            switch (updateStep) {
                case INTERMEDIATE:
                    changed = considerNetForceEffect(deltaT,false);
                    break;
                case FINAL:
                case EuFwd:
                case EUMod:
                case RK2:
                case RK4:
                    if (gravityON)
                        considerGravityEffect(deltaT, updateStep);
                    changed = considerNetForceEffect(deltaT, true);
                    status.time = nowT + deltaT;
                    break;
                default:
                    if (gravityON)
                        changed = considerGravityEffect(deltaT, updateStep);
                    break;
            }
        }
        return changed;
    }

    public boolean updatePAndVforAllActions(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception {
        boolean changed = false;
        if (bFixedLocation)
            changed = false;
        else {
            switch (updateStep) {
                case INTERMEDIATE:
                    effectiveLastVelocity.add(lastVelocity, addVelocity);
                    changed = considerNetForceEffect(deltaT,false);
                    break;
                case FINAL:
                case EuFwd:
                case EUMod:
                case RK2:
                case RK4:
                    effectiveLastVelocity.add(lastVelocity, addVelocity);
                    if (gravityON)
                        considerGravityEffect(deltaT, updateStep);
                    changed = considerNetForceEffect(deltaT, true);
                    status.time = nowT + deltaT;
                    break;
                default:
                    break;
            }
        }
        return changed;
    }

    public boolean updatePAndVforGravityOnly(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception {
        boolean changed = false;
        if (bFixedLocation)
            changed = false;
        else {
            switch (updateStep) {
                case INTERMEDIATE:
                    break;
                case FINAL:
                case EuFwd:
                case EUMod:
                case RK2:
                case RK4:
                    effectiveLastVelocity.add(lastVelocity, addVelocity);
                    considerGravityEffect(deltaT, updateStep);
                    status.time = nowT + deltaT;
                    break;
                default:
                    break;
            }
        }
        return changed;
    }

    public boolean updatePAndVforNetForceOnly(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception {
        boolean changed = false;
        if (bFixedLocation)
            changed = false;
        else {
            switch (updateStep) {
                case INTERMEDIATE:
                    effectiveLastVelocity.add(lastVelocity, addVelocity);
                    changed = considerNetForceEffect(deltaT,false);
                    break;
                case FINAL:
                case EuFwd:
                case EUMod:
                case RK2:
                case RK4:
                    effectiveLastVelocity.add(lastVelocity, addVelocity);
                    changed = considerNetForceEffect(deltaT, true);
                    status.time = nowT + deltaT;
                    break;
                default:
                    break;
            }
        }
        return changed;
    }

    public boolean updatePAndVnoGravityNoNetForce(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception {
        boolean changed = false;
        if (bFixedLocation)
            changed = false;
        else {
            switch (updateStep) {
                case INTERMEDIATE:
                    effectiveLastVelocity.add(lastVelocity, addVelocity);
                    changed = considerAddVelocityEffect(deltaT,false);
                    break;
                case FINAL:
                case EuFwd:
                case EUMod:
                case RK2:
                case RK4:
                    effectiveLastVelocity.add(lastVelocity, addVelocity);
                    changed = considerAddVelocityEffect(deltaT, true);
                    status.time = nowT + deltaT;
                    break;
                default:
                    break;
            }
        }
        return changed;
    }


    boolean considerNetForceEffect(double deltaT, boolean bFinal) {
        boolean changed = false;
        if (netForce.length() > 0) {
//            System.out.println("DarkMatter.#334: Force > 0  with bFinal " +  bFinal);
            lastAcc = status.acc;
//            lastVelocity = status.velocity;
            effectiveForce.set(netForce);
            nowAcc.scale(oneByMass, effectiveForce);
//            System.out.println("DarkMatter.#328 nowAcc: " +  nowAcc.dataInCSV() + ", Vel :" + status.velocity.dataInCSV());
            effectiveAcc.setMean(nowAcc, lastAcc);
            deltaV.scale(deltaT, effectiveAcc);
            newVelocity.add(effectiveLastVelocity, deltaV);
//            newVelocity.add(addVelocity);
            averageV.setMean(lastVelocity, newVelocity);
            deltaPos.scale(deltaT, averageV);
            newPos.add(lastPosition, deltaPos);
            status.pos.set(newPos); // only position is updated here
            if (bFinal) {
                status.velocity.set(newVelocity);
                status.acc.set(nowAcc);
            }
            changed = true;
        }
        return changed;
    }

    boolean considerAddVelocityEffect(double deltaT, boolean bFinal) {
//        newVelocity.add(lastVelocity, addVelocity);
        if (bFinal) {
            deltaPos.scale(deltaT, effectiveLastVelocity);
            newPos.add(lastPosition, deltaPos);
            status.pos.set(newPos); // only position is updated here
            status.velocity.set(effectiveLastVelocity);
        }
        return true;
    }

    TwoVectors pvBase;
    TwoVectors pv1, pv2, pv3, pv4;
    TwoVectors k1, k2, k3, k4;

    public Tuple3d getPosition(ItemInterface.UpdateStep updateStep) {
        Tuple3d pos = status.pos;
        switch(updateStep) {
            case K2:
                pos = pv1.v1;
                break;
            case K3:
                pos = pv2.v1;
                break;
            case K4:
                pos = pv3.v1;
                break;
        }
        return pos;
    }

    boolean considerGravityEffect(double deltaT, ItemInterface.UpdateStep updateStep) {
        boolean changed = true;
        double deltaTBy2 = deltaT / 2;
        double deltaTBy6 = deltaT / 6;
        TwoVectors pv;
        // WARNING ONLY FINAL IS corrected
        switch (updateStep) {
            case K1:
                pvBase = new TwoVectors(status.pos, status.velocity);
                k1 = new TwoVectors(pvBase, updateStep);
                pv1 = new TwoVectors(pvBase, deltaTBy2, k1);
                break;
            case K2:
//                pv1 = new TwoVectors(pvBase, deltaTBy2, k1);
                k2 = new TwoVectors(pv1, updateStep);
                pv2 = new TwoVectors(pvBase, deltaTBy2, k2);
                break;
            case K3:
//                pv2 = new TwoVectors(pvBase, deltaTBy2, k2);
                k3 = new TwoVectors(pv2, updateStep);
                pv3 = new TwoVectors(pvBase, deltaT, k3);
                break;
            case K4:
//                pv3 = new TwoVectors(pvBase, deltaTBy2, k3);
                k4 = new TwoVectors(pv3, updateStep);
                break;
            case EuFwd:
                pvBase.scaleAndAdd(deltaT, k1);
                status.pos.set(pvBase.v1);
                status.velocity.set(pvBase.v2);
                break;
            case RK4:
                k1.scaleAndAdd(2, k2);
                k1.scaleAndAdd(2, k3);
                k1.add(k4);

                pvBase.scaleAndAdd(deltaTBy6, k1);
                status.pos.set(pvBase.v1);
                status.velocity.set(pvBase.v2);
                break;
            case FINAL:
                changed = considerGravityEffectEuModified(deltaT);
                break;
            default:
                changed = false;
                break;
        }
        return changed;
    }

    public boolean considerGravityEffectEuModified(double deltaT) {
        boolean changed = false;
        double deltaTBy2 = deltaT / 2;
        double deltaTBy6 = deltaT / 6;
        TwoVectors pvBase = new TwoVectors(status.pos, effectiveLastVelocity);

        TwoVectors pv = pvBase;
        TwoVectors pv1 = new TwoVectors(pv);
        // pv1 is original velocity  and net acc due to gravity

        pv = new TwoVectors(pvBase, deltaT, pv1);
        // pv is new pos(original pos +  velocity * deltaT) and
        // new delta Velocity (original velocity + acc * deltaT)

        TwoVectors pv2 = new TwoVectors(pv);
        // pv2 original velocity  and net acc due to gravity

        pv1.makeMeanWith(pv2);
        // return mean Velocity and acc
        pvBase.scaleAndAdd(deltaT, pv1);
        status.pos.set(pvBase.v1);
        status.velocity.set(pvBase.v2);

//            status.time = nowT + deltaT;
        changed = true;
        return changed;
    }

    class TwoVectors {
        Vector3dMV v1 = new Vector3dMV();
        Vector3dMV v2;
        TwoVectors(Tuple3d v1, Tuple3d v2) {
            this.v1 = new Vector3dMV(v1);
            this.v2 = new Vector3dMV(v2);
        }

        TwoVectors(TwoVectors base, double scale, TwoVectors scaleThis) {
            this(base.v1, base.v2);
            scaleAndAdd(scale, scaleThis);
        }

        TwoVectors scaleAndAdd(double factor, TwoVectors scaleThis) {
            v1.scaleAndAdd(scaleThis.v1, factor);
            v2.scaleAndAdd(scaleThis.v2, factor);
            return this;
        }

        TwoVectors(TwoVectors pAndV) {
            v1.set(pAndV.v2);
            v2 = new Vector3dMV();
            for (Gravity oneG : gravityLinks) {
                v2.add(oneG.accDueToG(pAndV.v1));
            }
        }

        TwoVectors(TwoVectors pAndV, ItemInterface.UpdateStep updateStep) {
//            this(pAndV);
            v1.set(pAndV.v2);
            v2 = new Vector3dMV();
            for (Gravity oneG : gravityLinks) {
                v2.add(oneG.accDueToG(pAndV.v1, updateStep));
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
