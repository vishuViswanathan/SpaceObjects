package schemes;

import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.link.ItemLink;
import GeneralElements.link.Rod;
import GeneralElements.localActions.FixedAcceleration;
import GeneralElements.localActions.V2Resistance;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Created by M Viswanathan on 29 Aug 2014
 */
public class BungeeJumping implements DefaultScheme{
    double pitch = 2;
    double k = 10000;
    int nHalfChain = 10;
    double mass1 = 0.1;
    double mass2 = 100;
    double resistFactor = 5;
    double jumpXVel = 0.1;
    public BungeeJumping() {
    }

    @Override
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
                link = new ItemLink(lastItem, it, new Rod(lastItem, it, pitch, k) , space);
                space.addItemLink(link);
                it.addLocalAction(new FixedAcceleration(it, new Vector3d(0, -1, 0), 9.81));
                it.addLocalAction(new V2Resistance(it, resistFactor));            }
            else
                it.setbFixedLocation(true);
            lastPos = linkNum;
            lastItem = it;
        }
        it =  new Item("I" + linkNum, mass1, 0.1, Color.yellow, mainF);
        space.addItem(it);
        it.initPosEtc(new Point3d(xPos, -(lastPos + 1) * pitch, 0), new Vector3d(0, 0, 0));
        it.addLocalAction(new FixedAcceleration(it, new Vector3d(0, -1, 0), 9.81));
        it.addLocalAction(new V2Resistance(it, resistFactor));
        link = new ItemLink(lastItem, it, new Rod(lastItem, it, pitch, k) , space);
        space.addItemLink(link);
        lastItem = it;
        xPos += pitch;
        double yPos = -linkNum * pitch;
        linkNum++;
        for (; linkNum < 2 * nHalfChain + 1; linkNum++) {
            it =  new Item("I" + linkNum, mass1, 0.1, Color.yellow, mainF);
            space.addItem(it);
            it.initPosEtc(new Point3d(xPos, yPos, 0), new Vector3d(0, 0, 0));
            link = new ItemLink(lastItem, it, new Rod(lastItem, it, pitch, k) , space);
            space.addItemLink(link);
            it.addLocalAction(new FixedAcceleration(it, new Vector3d(0, -1, 0), 9.81));
            it.addLocalAction(new V2Resistance(it, resistFactor));
//            lastPos = linkNum;
            lastItem = it;
            yPos += pitch; // going up
        }

        it =  new Item("Ball", mass2, 1, Color.WHITE, mainF);
        space.addItem(it);
        it.initPosEtc(new Point3d(xPos, yPos, 0), new Vector3d(jumpXVel, 0, 0));
        link = new ItemLink(lastItem, it, new Rod(lastItem, it, pitch, k) , space);
        it.addLocalAction(new FixedAcceleration(it, new Vector3d(0, -1, 0), 9.81));
        it.addLocalAction(new V2Resistance(it, resistFactor));
        space.addItemLink(link);
        return true;
    }

    @Override
    public double startJDN() {
        return 0;
    }

    public String toString() {
        return "Bungee Jumping";
    }
}
