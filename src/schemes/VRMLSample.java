package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import mvUtils.physics.ForceElement;
import mvUtils.physics.Vector3dMV;

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
        Vector3d f = new Vector3d(0, 0, 1000);
        Point3d actingPt = new Point3d(0.000, 0, -2.5);
        it.setMomentsOfInertia(50, 50, 50);
        it.addJet(f, actingPt);

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

