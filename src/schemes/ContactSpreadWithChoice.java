package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.*;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;
import mvUtils.physics.Vector3dMV;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * Created by M Viswanathan on 19 Feb 2015
 */
public class ContactSpreadWithChoice implements DefaultScheme {
    int boxXmin = -5, boxXmax = 5;
    int boxYmin = -5 , boxYmax = 5;
    int boxZmin = -2, boxZmax = 2;
    int nMoving = 50, nNotMoving = 0, nAffected = 10;
    double movingSpeedMin = 1, movingSpeedMax = 5;
    double affectedSpeedMin = 2, affectedSpeedMax = 2;
    double mass = 0.05;
    double diameter = 0.1;

    public ContactSpreadWithChoice() {
    }

    @Override
    public boolean getScheme(JFrame mainF, ItemSpace space) {
        ItemDialog dlg = new ItemDialog("Member DEtails", null, mainF);
        dlg.setVisible(true);

        Point3d minCorner = new Point3d(boxXmin + diameter, boxYmin + diameter, boxZmin);
        Point3d maxCorner = new Point3d(boxXmax - diameter, boxYmax - diameter, boxZmax);
        Random rd = new Random();
        Vector3d axes = new Vector3d(1, 1, 0);
        // moving items
        addManyBallsAtRandom(mainF, space, nMoving, minCorner, maxCorner,
                movingSpeedMin, movingSpeedMax, axes, rd, "A", mass, diameter, false, 200000);
        // non-moving items
        addManyBallsAtRandom(mainF, space, nNotMoving, minCorner, maxCorner,
                0, 0, axes, rd, "B", mass, diameter, false, 200000);

        // affected items
        addManyBallsAtRandom(mainF, space, nAffected, minCorner, maxCorner,
                affectedSpeedMin,affectedSpeedMax, axes, rd, "C", mass, diameter, true, 200000);
        Item it;
        it = new Surface("Floor", new Point3d( -5, boxXmin, -5),
                new Point3d( -5, boxXmin, 0), new Point3d( 0, boxXmin, 5),
                mainF );
        // it.setStickingPressure(10000);
        space.addItem(it);
        it = new Surface("EastWall", new Point3d( boxXmin, -5, -5),
                new Point3d( boxXmin, 5, 0), new Point3d( boxXmin, -5, 5), mainF );
        space.addItem(it);
        it = new Surface("WestWall", new Point3d( boxXmax, -5, -5),
                new Point3d( boxXmax, 5, 0), new Point3d( boxXmax, -5, 5), mainF );
        space.addItem(it);
        it = new Surface("Ceiling", new Point3d( 5, boxYmax, 5),
                new Point3d( 5, boxYmax, 0), new Point3d( -5, boxYmax, 5), mainF );
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

    class ItemDialog extends JDialog {
        Component parent;
        NumberTextField ntboxXmin, ntboxXmax;
        NumberTextField ntboxYmin, ntboxYmax;
        NumberTextField ntboxZmin, ntboxZmax;
        NumberTextField ntnMoving, ntnNotMoving, ntnAffected;
        NumberTextField ntmovingSpeedMin, ntmovingSpeedMax;
        NumberTextField ntaffectedSpeedMin, ntaffectedSpeedMax;
        NumberTextField ntmass, ntdiameter;

        JButton pbSave = new JButton("Save");
        JButton pbCancel = new JButton("Cancel");
        InputControl inpC;

        ItemDialog(String title, InputControl inpC, Component c) {
            setModal(true);
            setResizable(false);
            this.inpC = inpC;
            this.parent = c;
            setTitle(title);
            dbInit();
            if (c == null)
                setLocation(100, 100);
            else
                setLocationRelativeTo(c);
        }

        void dbInit() {
            JPanel outerPan = new JPanel(new BorderLayout());
            MultiPairColPanel jp = new MultiPairColPanel("Collections of Contacts");
            ntboxXmin = new NumberTextField(inpC, boxXmin, 4, true,
                    -100, 100, "####", "Xmin");
            ntboxXmax = new NumberTextField(inpC, boxXmax, 4, true,
                    -100, 100, "####", "Xmax");
            ntboxYmin = new NumberTextField(inpC, boxYmin, 4, true,
                    -100, 100, "####", "Ymin");
            ntboxYmax = new NumberTextField(inpC, boxYmax, 4, true,
                    -100, 100, "####", "Ymax");
            ntboxZmin = new NumberTextField(inpC, boxZmin, 4, true,
                    -100, 100, "####", "Zmin");
            ntboxZmax = new NumberTextField(inpC, boxZmax, 4, true,
                    -100, 100, "####", "Zmax");
            JPanel jpX = new JPanel();
            jpX.add(ntboxXmin);
            jpX.add(ntboxXmax);
            jp.addItemPair("Limiting Box X Limits(m)", jpX);

            JPanel jpY = new JPanel();
            jpY.add(ntboxYmin);
            jpY.add(ntboxYmax);
            jp.addItemPair("Limiting Box Y Limits(m)", jpY);

            JPanel jpZ = new JPanel();
            jpZ.add(ntboxZmin);
            jpZ.add(ntboxZmax);
            jp.addItemPair("Limiting Box Z Limits(m)", jpZ);

            jp.addBlank();
            ntnMoving = new NumberTextField(inpC, nMoving, 6, true,
                    1, 1000, "####", "Quantity in motion");
            ntnNotMoving = new NumberTextField(inpC, nNotMoving, 6, true,
                    0, 1000, "####", "Quantity static");
            ntnAffected = new NumberTextField(inpC, nAffected, 6, true,
                    1, 1000, "####", "Quantity Affected");
            jp.addItemPair(ntnMoving);
            jp.addItemPair(ntnNotMoving);
            jp.addItemPair(ntnAffected);
            jp.addBlank();

            ntmovingSpeedMin = new NumberTextField(inpC, movingSpeedMin, 4, false,
                    0, 10, "####.##", "Min Moving Speed");
            ntmovingSpeedMax = new NumberTextField(inpC, movingSpeedMax, 4, false,
                    0, 10, "####.##", "Max Moving Speed");
            JPanel jpS1 = new JPanel();
            jpS1.add(ntmovingSpeedMin);
            jpS1.add(ntmovingSpeedMax);
            jp.addItemPair("Moving Speed Limits(m/s)", jpS1);

            ntaffectedSpeedMin = new NumberTextField(inpC, affectedSpeedMin, 4, false,
                    0, 10, "####.##", "Min Affected Speed");
            ntaffectedSpeedMax = new NumberTextField(inpC, affectedSpeedMax, 4, false,
                    0, 10, "####.##", "Max Affected Speed");
            JPanel jpAS1 = new JPanel();
            jpAS1.add(ntaffectedSpeedMin);
            jpAS1.add(ntaffectedSpeedMax);
            jp.addItemPair("Affected Limits(m/s)", jpAS1);
            jp.addBlank();

            ntmass = new NumberTextField(inpC, mass, 6, false,
                    0.001, 1e5, "####.###", "Each item Mass(kg)");
            ntdiameter = new NumberTextField(inpC, diameter, 6, false,
                    0.001, 10, "####.###", "Each item Dia(m)");
            outerPan.add(jp, BorderLayout.CENTER);
            jp.addItemPair(ntmass);
            jp.addItemPair(ntdiameter);

            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == pbSave) {
                        if (takeValuesFromUI())
                            closeThisWindow();
                    } else if (src == pbCancel) {
                            closeThisWindow();
                    }
                }
            };
            pbSave.addActionListener(li);
            pbCancel.addActionListener(li);
            JPanel buttPanel = new JPanel(new BorderLayout());
            buttPanel.add(pbCancel, BorderLayout.WEST);
            buttPanel.add(pbSave, BorderLayout.EAST);
            outerPan.add(buttPanel, BorderLayout.SOUTH);
            add(outerPan);
            pack();
        }

/*
    double mass = 0.05;
    double diameter = 0.05;

 */
        boolean takeValuesFromUI() {
            boxXmin = (int)ntboxXmin.getData();
            boxXmax = (int)ntboxXmax.getData();
            boxYmin = (int)ntboxYmin.getData();
            boxYmax = (int)ntboxYmax.getData();
            boxZmin = (int)ntboxZmin.getData();
            boxZmax = (int)ntboxZmax.getData();

            nMoving = (int)ntnMoving.getData();
            nNotMoving = (int)ntnNotMoving.getData();
            nAffected = (int)ntnAffected.getData();

            movingSpeedMin = ntmovingSpeedMin.getData();
            movingSpeedMax = ntmovingSpeedMax.getData();

            affectedSpeedMin = ntaffectedSpeedMin.getData();
            affectedSpeedMax = ntaffectedSpeedMax.getData();

            mass = ntmass.getData();
            diameter = ntdiameter.getData();
            return true;
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
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
        return "Contact Spread With Choice";
    }
}
