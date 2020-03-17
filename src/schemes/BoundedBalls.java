package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.Surface;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.Random;

/**
 * Created by M Viswanathan on 19 Feb 2015
 */
public class BoundedBalls implements DefaultScheme {
    double e = 20000; // similar to e but force (ie N/100%)
    double mass2 = 100;

    public BoundedBalls() {
    }

    @Override
    public boolean getScheme(JFrame mainF, ItemSpace space) {
        double xmin = -10, xmax = 10;
        double ymin = -10, ymax = 10;
        double zmin = 0, zmax = 0;

        Point3d minCorner = new Point3d(xmin, ymin, zmin);
        Point3d maxCorner = new Point3d(xmax, ymax, zmax);
        Vector3d vel1 = new Vector3d(-5, -5, 0);
        Vector3d vel2 = new Vector3d(5, 5, 0);
        addManyBallsAtRandom(mainF, space, 20, minCorner, maxCorner,
                vel1,vel2, "B", 0.05, 0.1, Color.RED, 200000);
        addManyBallsAtRandom(mainF, space, 20, minCorner, maxCorner,
                vel1,vel2, "B", 0.05, 0.1, Color.YELLOW, 200000);
        addManyBallsAtRandom(mainF, space, 10, minCorner, maxCorner,
                vel1,vel2, "B", 0.05, 0.1, Color.CYAN, 200000);
        Item it;
//        it =  new Item("Ball", mass2, 1, Color.RED, mainF);
//        it.initPosEtc(new Point3d(0,0, 0), new Vector3d(5, 5, 0));
//        it.seteCompression(200000);
//        space.addItem(it);
//        it = new Surface("InclinedFloor", new Point3d( 0, -0.5, -5), new Point3d( 0, -0.5, 5), new Point3d( 5, -1, 5), mainF );
//        space.addItem(it);
        it = new Surface("Floor", new Point3d( -5, ymin, -5),
                new Point3d( -5, ymin, 0), new Point3d( 0, ymin, 5),
                0.9, mainF );
        it.setStickingPressure(10000);
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


    void addOneBall(JFrame mainF, ItemSpace space, String name, double mass, double dia, Color color,
                    Point3d loc, Vector3d velocity, double compression) {
        Item it =  new Item(name, mass, dia, color, mainF);
        it.initPosEtc(new Point3d(loc), new Vector3d(velocity));
        it.seteCompression(compression);
        space.addItem(it);
    }

    void addManyBallsAtRandom(JFrame mainF, ItemSpace space, int qty, Point3d minCorner, Point3d maxCorner,
                              Vector3d vel1, Vector3d vel2, String baseName, double mass, double dia,
                              Color color, double compression ) {
        Random rd = new Random();
        Point3d posRange = new Point3d(maxCorner);
        posRange.sub(minCorner);
        Vector3d velRange = new Vector3d(vel2);
        velRange.sub(vel1);
        for (int n = 0; n < qty; n++) {
            String nowName = baseName + n;
            Point3d pos = new Point3d(minCorner.x + rd.nextDouble() * posRange.x,
                    minCorner.y + rd.nextDouble() * posRange.y,
                    minCorner.z + rd.nextDouble() * posRange.z);
            Vector3d vel = new Vector3d(vel1.x + rd.nextDouble() * velRange.x,
                    vel1.y + rd.nextDouble() * velRange.y,
                    vel1.z + rd.nextDouble() * velRange.z);
            addOneBall(mainF, space, nowName, mass, dia, color, pos, vel, compression);
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
        return "Bounded Balls";
    }
}
