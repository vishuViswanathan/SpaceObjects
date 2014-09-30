package schemes;

import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.link.*;
import GeneralElements.localActions.FixedAcceleration;
import GeneralElements.localActions.V2Resistance;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Created by M Viswanathan on 25 Sep 2014
 */
public class BungeeJumpingWithRope implements DefaultScheme{
    double e = 200000; // similar to e but force (ie N/100%)
    double massPerM = 0.01526;
    double mass1 = 0.02;
    double mass2 = 100;
    double resistFactor = 1;

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
//        it.setbFixedLocation(true);
        it.addLocalAction(new FixedAcceleration(it, new Vector3d(0, -1, 0), 9.81));
        it.addLocalAction(new V2Resistance(it, resistFactor));
        it.initPosEtc(new Point3d(4,0, 0), new Vector3d(0, 0, 0));
        space.addItem(it);

        LinkWithMass rope = new Rope(lastItem, it, 30, massPerM, e, 30);
        if (rope.setAllElements()) {
            rope.addLocalAction("<type>FixedAccel</type><direction>0, -1, 0</direction><fixedAcc>9.81</fixedAcc>");
            rope.addLocalAction("<type>FluidResistance</type><factor>" + resistFactor + "</factor>");
            ItemLink link = new ItemLink(lastItem, it, rope, space);
            space.addItemLink(link);
        }
        return true;
    }

    @Override
    public double startJDN() {
        return 0;
    }

    public String toString() {
        return "Bungee Jumping with Rope";
    }
}