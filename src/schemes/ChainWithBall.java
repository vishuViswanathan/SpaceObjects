package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.link.ItemLink;
import GeneralElements.link.Rod;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Created by M Viswanathan on 29 Aug 2014
 */
public class ChainWithBall implements DefaultScheme {
    double pitch = 1;
    double k = 10000;
    int nChain = 10;
    double mass1 = 1;
    double mass2 = 5;

    public ChainWithBall() {
    }

    public boolean getScheme(JFrame mainF, ItemSpace space) {
        Item it;
        Item lastItem = null;
        int  lastPos = 0;
        ItemLink link;
        for (int i = 0; i < nChain; i++) {
            it =  new Item("I" + i, mass1, 0.1, Color.yellow, mainF);
            space.addItem(it);
            it.initPosEtc(new Point3d(0, -i * pitch, 0), new Vector3d(0, 0, 0));
            if (i > 0) {
                link = new ItemLink(lastItem, it, new Rod(lastItem, it, pitch, k, true) , space);
                space.addItemLink(link);
//                it.setbFixedForceOn(true);
            }
            else
                it.setbFixedLocation(true);
            lastPos = i;
            lastItem = it;
        }
        it =  new Item("Ball", mass2, 0.5, Color.WHITE, mainF);
        space.addItem(it);
        it.initPosEtc(new Point3d(0, -(lastPos + 1) * pitch, 0), new Vector3d(2, 0, 0));
        link = new ItemLink(lastItem, it, new Rod(lastItem, it, pitch, k, true) , space);
//        it.setbFixedForceOn(true);
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

    @Override
    public String toString() {
        return "Chain with Ball";
    }
}
