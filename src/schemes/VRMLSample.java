package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.Jet;
import mvUtils.physics.Vector3dMV;
import time.timePlan.JetTimeController;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 09 Mar 2017
 */
public class VRMLSample implements DefaultScheme{
    String vrmlFile = "C:\\Java Programs\\SpaceObjects\\VRML\\rocket.wrl"; // similar to e but force (ie N/100%)
    double mass2 = 100;

    public VRMLSample() {
    }

    @Override
    public boolean getScheme(JFrame mainF, ItemSpace space) {
        Item it;
        it =  new Item("Rocket", mass2, vrmlFile, mainF);
        it.initPosEtc(new Point3d(0,0, 0), new Vector3d(0, 0, 0));
        it.setbFixedLocation(true);
        try {
            it.setMomentsOfInertia(50, 50, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JetTimeController jetController = new JetTimeController(it);
        it.setJetController(jetController);
        it.addOneJet("Main", new Vector3d(0, 0, 100), new Point3d(0.000, 0.000, -2.5), 2.0, 100000);
        it.addJetCouple("ctrl", new Vector3d(0, 100, 0), new Point3d(0.00, 0.000, -0.002),
                Vector3dMV.Axis.Z, 5, 4);
        it.addJetCouple("ctrl2", new Vector3d(0, -100, 0), new Point3d(0.00, 0.000, -0.002),
                Vector3dMV.Axis.Z, 10, 4);
//        it.addOneJet("Main", new Vector3d(0, 0, 100), new Point3d(0.000, 0.000, -2.5), 2.0, 100000);
//        Vector3d f1 = new Vector3d(0, 0, 100);
//        Point3d actingPt1 = new Point3d(0.000, 0.000, -2.5);
//        Jet jet1 = it.addOneJet("Main", f1, actingPt1);
//        it.addOneJetPlanStep(jet1, 2.0, 100000);

//        Vector3d f2 = new Vector3d(0, 100, 0);
//        Point3d actingPt2 = new Point3d(0.02, 0, 0);
//        Jet jet2 = it.addOneJet("Aux", f2, actingPt2);
//        it.addOneJetPlanStep(jet2, 10.0, 5);

//        Vector3d f3 = new Vector3d(0, -1000, 0);
//        Point3d actingPt3 = new Point3d(0.002, 0, 0);
//        Jet jet3 = it.addOneJet("Aux", f3, actingPt3);
//        it.addOneJetPlanStep(jet3, 10.0, 5);
        space.addItem(it);

        return true;
    }

    public ItemMovementsApp.SpaceSize getSpaceSize() {
        return ItemMovementsApp.SpaceSize.DAILY;
    }


    @Override
    public double startJDN() {
        return 0;
    }

    public String toString() {
        return "The Rocket";
    }
}

