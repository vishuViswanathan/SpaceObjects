package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.Surface;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Created by M Viswanathan on 19 Feb 2015
 */
public class BallAndFloor implements DefaultScheme {
    double e = 20000; // similar to e but force (ie N/100%)
    double mass2 = 100;

    public BallAndFloor() {
    }

    @Override
    public boolean getScheme(JFrame mainF, ItemSpace space) {
        Item it;
         it =  new Item("Ball", mass2, 1, Color.RED, mainF);
        it.initPosEtc(new Point3d(0,0, 0), new Vector3d(0, 0, 0));
        it.seteCompression(200000);
        space.addItem(it);
//        it = new Surface("InclinedFloor", new Point3d( 0, -0.5, -5), new Point3d( 0, -0.5, 5), new Point3d( 5, -1, 5), mainF );
//        space.addItem(it);
        it = new Surface("Floor", new Point3d( -5, -5, -5), new Point3d( -5, -5, 0), new Point3d( 0, -5, 5), 0.9, mainF );
//        it = new Surface("Floor", new Point3d( -5, -5, -5), new Point3d( -5, -5, 0), new Point3d( 0, -5, 5), mainF );
        it.setStickingPressure(10000);
        space.addItem(it);
        it = new Surface("EastWall", new Point3d( 5, -5, -5), new Point3d( 5, 5, 0), new Point3d( 5, -5, 5), mainF );
        space.addItem(it);
        it = new Surface("WestWall", new Point3d( -5, -5, -5), new Point3d( -5, 5, 0), new Point3d( -5, -5, 5), mainF );
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
        return "Ball and Floor";
    }
}
