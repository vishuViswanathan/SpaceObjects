package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.LiveItem;
import GeneralElements.Surface;
import mvUtils.physics.Vector3dMV;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.Random;

/**
 * Created by M Viswanathan on 19 Feb 2015
 */
public class ContactSpread implements DefaultScheme {

    public ContactSpread() {
    }

    @Override
    public boolean getScheme(JFrame mainF, ItemSpace space) {
        double xmin = -5, xmax = 5;
        double ymin = -5, ymax = 5;
//        Take care of item dia and boundary
        double zmin = 0, zmax = 0;

        Point3d minCorner = new Point3d(xmin + 1, ymin + 1, zmin);
        Point3d maxCorner = new Point3d(xmax - 1, ymax - 1, zmax);
        double vel1 = 0;
        double vel2 = 2;
        Random rd = new Random();
        Vector3d axes = new Vector3d(1, 1, 0);
        addManyBallsAtRandom(mainF, space, 50, minCorner, maxCorner,
                vel1, vel2, axes, rd, "A", 0.05, 0.05, false, 200000);
        addManyBallsAtRandom(mainF, space, 1, minCorner, maxCorner,
                vel1,vel2, axes, rd, "C", 0.05, 0.05, true, 200000);
        Item it;
        it = new Surface("Floor", new Point3d( -5, ymin, -5),
                new Point3d( -5, ymin, 0), new Point3d( 0, ymin, 5),
                mainF );
        // it.setStickingPressure(10000);
        space.addItem(it);
        it = new Surface("EastWall", new Point3d( xmin, -5, -5),
                new Point3d( xmin, 5, 0), new Point3d( xmin, -5, 5), mainF );
        space.addItem(it);
        it = new Surface("WestWall", new Point3d( xmax, -5, -5),
                new Point3d( xmax, 5, 0), new Point3d( xmax, -5, 5), mainF );
        space.addItem(it);
        it = new Surface("Ceiling", new Point3d( 5, ymax, 5),
                new Point3d( 5, ymax, 0), new Point3d( -5, ymax, 5), mainF );
        space.addItem(it);

        return true;
    }


    void addOneBall(JFrame mainF, ItemSpace space, String name, double mass, double dia, boolean infected,
                    Point3d loc, Vector3d velocity, double compression) {
        Item it =  new LiveItem(name, mass, dia, infected, mainF);
        it.initPosEtc(new Point3d(loc), new Vector3d(velocity));
        it.seteCompression(compression);
        space.addItem(it);
    }

    void addManyBallsAtRandom(JFrame mainF, ItemSpace space, int qty, Point3d minCorner, Point3d maxCorner,
                              double vel1, double vel2, Tuple3d axes , Random rd,
                              String baseName, double mass, double dia,
                              boolean infected, double compression) {
        Point3d posRange = new Point3d(maxCorner);
        posRange.sub(minCorner);
        for (int n = 0; n < qty; n++) {
            String nowName = baseName + n;
            Point3d pos = new Point3d(minCorner.x + rd.nextDouble() * posRange.x,
                    minCorner.y + rd.nextDouble() * posRange.y,
                    minCorner.z + rd.nextDouble() * posRange.z);
            Vector3d vel = Vector3dMV.getRandomVector(vel1, vel2, axes, rd);
            addOneBall(mainF, space, nowName, mass, dia, infected, pos, vel, compression);
        }
    }


    public ItemMovementsApp.SpaceSize getSpaceSize() {
        return ItemMovementsApp.SpaceSize.DAILY;
    }


    @Override
    public double startJDN() {
        return 0;
    }

    public String toString() {
        return "Contact Spread";
    }
}
