package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.link.ItemLink;
import GeneralElements.link.LinkWithMass;
import GeneralElements.link.Rope;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Created by M Viswanathan on 25 Sep 2014
 */
public class BungeeJumpingWithRope implements DefaultScheme{
    double e = 20000; // similar to e but force (ie N/100%)
    double massPerM = 0.1;
    double mass1 = 0.02;
    double mass2 = 100;
    double resistFactor = 1;
    double gAcc = 9.81;
    double ropeDia = 0.0157079632679489;

    public BungeeJumpingWithRope() {
    }

    @Override
    public boolean getScheme(JFrame mainF, ItemSpace space) {
        Item it;
        Item lastItem;
        it =  new Item("Support", mass1, 0.1, Color.yellow, mainF);
        it.setbFixedLocation(true);
        it.initPosEtc(new Point3d(0, 0, 0), new Vector3d(0, 0, 0));
        space.addItem(it);

        lastItem = it;
        it =  new Item("Ball", mass2, 1, Color.WHITE, mainF);
        it.initPosEtc(new Point3d(4,0, 0), new Vector3d(0, 0, 0));
        it.seteCompression(20000);
        space.addItem(it);
        LinkWithMass rope = new Rope(lastItem, it, (25.0/4), e);
        ItemLink link = new ItemLink(lastItem, it, rope, space);
        space.addItemLink(link);
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
        return "Bungee Jumping with Rope";
    }
}
