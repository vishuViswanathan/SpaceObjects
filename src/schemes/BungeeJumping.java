package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.Surface;
import GeneralElements.link.ItemLink;
import GeneralElements.link.Rod;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Created by M Viswanathan on 29 Aug 2014
 */
public class BungeeJumping implements DefaultScheme{
    double pitch = 1;
    double k = 200000; // similar to e but force (ie N/100%)
    int nHalfChain = 25;
    double mass1 = 0.0288;
    double mass2 = 100;
    double resistFactor = 1;
    double jumpXVel = 0.1;
    public BungeeJumping() {
    }

    public boolean getScheme(JFrame mainF, ItemSpace space) {
        Item it;
        Item lastItem = null;
        int  lastPos = 0;
        ItemLink link;
        int linkNum;
        double xPos = 0;
        for (linkNum = 0; linkNum < nHalfChain; linkNum++) {
            it =  new Item("I" + linkNum, mass1, 0.1, Color.yellow, mainF);
            space.addItem(it);
            it.initPosEtc(new Point3d(xPos, -linkNum * pitch, 0), new Vector3d(0, 0, 0));
            if (linkNum > 0) {
                link = new ItemLink(lastItem, it, new Rod(lastItem, it, 1, k) , space);
                space.addItemLink(link);
            }
            else
                it.setbFixedLocation(true);
            lastPos = linkNum;
            lastItem = it;
        }
        it =  new Item("I" + linkNum, mass1, 0.1, Color.yellow, mainF);
        space.addItem(it);
        it.initPosEtc(new Point3d(xPos, -(lastPos + 1) * pitch, 0), new Vector3d(0, 0, 0));
        link = new ItemLink(lastItem, it, new Rod(lastItem, it, 1, k) , space);
        space.addItemLink(link);
        lastItem = it;
        xPos += pitch;
        double yPos = -linkNum * pitch;
        linkNum++;
        for (; linkNum < 2 * nHalfChain + 1; linkNum++) {
            it =  new Item("I" + linkNum, mass1, 0.1, Color.yellow, mainF);
            space.addItem(it);
            it.initPosEtc(new Point3d(xPos, yPos, 0), new Vector3d(0, 0, 0));
            link = new ItemLink(lastItem, it, new Rod(lastItem, it, 1, k) , space);
            space.addItemLink(link);
            lastItem = it;
            yPos += pitch; // going up
        }

        it =  new Item("Ball", mass2, 1, Color.WHITE, mainF);
        it.seteCompression(20000);;
        space.addItem(it);
        it.initPosEtc(new Point3d(xPos, yPos, 0), new Vector3d(jumpXVel, 0, 0));
        it = new Surface("Floor", new Point3d( -5, -5, -5), new Point3d( -5, -5, 0), new Point3d( 0, -5, 5), mainF );
        space.addItem(it);
        link = new ItemLink(lastItem, it, new Rod(lastItem, it, 1, k) , space);
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
        return "Bungee Jumping";
    }
}
