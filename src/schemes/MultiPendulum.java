package schemes;

import Applications.ItemMovementsApp;
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
public class MultiPendulum implements DefaultScheme {
    double z = 0, y;
    double x = -0.2;
    double zStep = 0.05;
    double len = 0.5;
    double lenStep = 0.05;

    double e = 20000;
    int nPendulums = 15;
    double mass1 = 0.1;
    double mass2 = 1;
    double resistance = 0;

    public MultiPendulum() {
    }

    public boolean getScheme(JFrame mainF, ItemSpace space) {
        Item itHook, itBall;
        ItemLink link;
        for (int i = 0; i < nPendulums; i++) {
            itHook =  new Item("Support" + i, mass1, 0.01, Color.blue, mainF);
            space.addItem(itHook);
            itHook.initPosEtc(new Point3d(0, 0, z), new Vector3d(0, 0, 0));
            itHook.setbFixedLocation(true);

            itBall =  new Item("Ball" + i, mass2, 0.05, Color.yellow, mainF);
            space.addItem(itBall);
            y = - Math.sqrt(Math.pow(len, 2) - Math.pow(x, 2));
            itBall.initPosEtc(new Point3d(x, y, z), new Vector3d(0, 0, 0));
            itBall.addLocalAction(new FixedAcceleration(new Vector3d(0, -1, 0), 9.81));
            itBall.addLocalAction(new V2Resistance(resistance));

//            link = new ItemLink(itHook, itBall, new Rod(itHook, itBall, len, k, true) , space);
            link = new ItemLink(itHook, itBall, new Rod(itHook, itBall, 1, e) , space);
            space.addItemLink(link);

            len += lenStep;
            z -= zStep;
        }
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
        return "Multi Pendulums";
    }
}
