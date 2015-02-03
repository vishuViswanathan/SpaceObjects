package schemes;

import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.link.ItemLink;
import GeneralElements.link.Spring;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Created by M Viswanathan on 24 Jan 2015
 */
public class Mesh implements DefaultScheme {
    int xPoints = 10;
    int zPoints = 10;
    double xLen = 10;
    double zLen = 10;
    double ptMass = 0.1;
    double midMass = ptMass * 10000;
    double ptDia = 0.01;
    double eX = 20000;
    double eZ = 20000;
    double resistance = 100;
    double initialLenFactor = 1.2;

    public Mesh() {
    }

    public boolean getScheme(JFrame mainF, ItemSpace space) {
        Item[][] allNodes = new Item[xPoints + 1][zPoints + 1];
        Item item;
        double locX = 0;
        double locZ = 0;
        double locY = 0;
        double xStep = xLen / xPoints;
        double zStep = zLen / zPoints;
        // create points
        int midX = xPoints / 2;
        int midZ = zPoints / 2;
        for (int x = 0; x <= xPoints; x++) {
            locZ = 0;
            for (int z = 0; z <= zPoints; z++) {
//                if (x == midX && z == midZ)
//                    item = new Item("P" + str(x) + "." + str(z), midMass, ptDia, Color.yellow, mainF);
//                else
                    item = new Item("P" + str(x) + "." + str(z), ptMass, ptDia, Color.yellow, mainF);
                item.initPosEtc(new Point3d(locX, locY, locZ ), new Vector3d());
                // fix the corners
//                if ((x == 0 && z == 0) || (x == 0 && z == zPoints) || (x == xPoints && z == 0))
//                if ((x == 0 && z == 0) || (x == 0 && z == zPoints) || (x == xPoints && z == 0) || (x == xPoints && z == zPoints))
//                if (x == 0 || z == 0 || z == zPoints || x == xPoints)
                if (x == 0)
                    item.setbFixedLocation(true);
//                item.addLocalAction(new FixedAcceleration());
//                item.addLocalAction(new V2Resistance(resistance));
                space.addItem(item);
                allNodes[x][z] = item;
                locZ += zStep;
            }
            locX += xStep;
        }
        ItemLink link;
        Item item1;
        Item item2;
        for (int x = 0; x <= xPoints; x++) {
            for (int z = 0; z <= zPoints; z++) {
                item1 = allNodes[x][z];
                if (x < xPoints) {
                    item2 = allNodes[x + 1][z];
                    // springs with free compression like a rope
                    link = new ItemLink(item1, item2, new Spring(item1, item2, initialLenFactor, 0, eX), space);
                    space.addItemLink(link);
                }
                if (z < zPoints) {
                    item2 = allNodes[x][z + 1];
                    link = new ItemLink(item1, item2, new Spring(item1, item2, initialLenFactor, 0, eZ), space);
                    space.addItemLink(link);
                }
            }
        }
        return true;
    }

    String str(int value) {
        return ("" + value).trim();
    }

    @Override
    public double startJDN() {
        return 0;
    }
}
