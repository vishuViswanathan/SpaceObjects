package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.LiveItem;
import GeneralElements.Surface;
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

public class TwoTypesOfBallsInContainer implements DefaultScheme {
    int boxXmin = -5, boxXmax = 5;
    int boxYmin = -5 , boxYmax = 5;
    int boxZmin = -5, boxZmax = 5;
    int nTypeA = 30, nNotMoving = 0, nTypeB = 2;
    double minSpeedA = 1, maxSpeedA = 5;
    double minSpeedB = 2, maxSpeedB = 2;
    double massA = 0.05;
    double diaA = 0.1;
    double massB = 0.05;
    double diaB = 0.1;
    Color colorA = Color.RED;
    Color colorB = Color.CYAN;
    Color colorC = Color.blue;

    public TwoTypesOfBallsInContainer() {
    }

    @Override
    public boolean getScheme(JFrame mainF, ItemSpace space) {
        ItemDialog dlg = new ItemDialog("Member Details", null, mainF);
        dlg.setVisible(true);

        Point3d minCorner = new Point3d(boxXmin + diaA, boxYmin + diaA, boxZmin);
        Point3d maxCorner = new Point3d(boxXmax - diaA, boxYmax - diaA, boxZmax);
        Random rd = new Random();
        Vector3d axes = new Vector3d(1, 1, 0);
        // Type A
        addManyBallsAtRandom(mainF, space, nTypeA, minCorner, maxCorner,
                minSpeedA, maxSpeedA, axes, rd, "A", massA, diaA, colorA, 200000);
         // Type B
        addManyBallsAtRandom(mainF, space, nTypeB, minCorner, maxCorner,
                minSpeedB, maxSpeedB, axes, rd, "B", massB, diaB, colorB, 200000);
        // Type C
        addManyBallsAtRandom(mainF, space, nNotMoving, minCorner, maxCorner,
                0, 0, axes, rd, "C", massA, diaA, colorC, 200000);

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


    void addOneBall(JFrame mainF, ItemSpace space, String name, double mass, double dia, Color color,
                    Point3d loc, Vector3d velocity, double compression) {
        Item it =  new Item(name, mass, dia, color, mainF);
        it.initPosEtc(new Point3d(loc), new Vector3d(velocity));
        it.seteCompression(compression);
        space.addItem(it);
    }

    void addManyBallsAtRandom(JFrame mainF, ItemSpace space, int qty, Point3d minCorner, Point3d maxCorner,
                              double vel1, double vel2, Tuple3d axes , Random rd,
                              String baseName, double mass, double dia, Color color,
                              double compression) {
        Point3d posRange = new Point3d(maxCorner);
        posRange.sub(minCorner);
        for (int n = 0; n < qty; n++) {
            String nowName = baseName + n;
            Point3d pos = new Point3d(minCorner.x + rd.nextDouble() * posRange.x,
                    minCorner.y + rd.nextDouble() * posRange.y,
                    minCorner.z + rd.nextDouble() * posRange.z);
            Vector3d vel = Vector3dMV.getRandomVector(vel1, vel2, axes, rd);
            addOneBall(mainF, space, nowName, mass, dia, color, pos, vel, compression);
        }
    }

    class ItemDialog extends JDialog {
        Component parent;
        NumberTextField ntboxXmin, ntboxXmax;
        NumberTextField ntboxYmin, ntboxYmax;
        NumberTextField ntboxZmin, ntboxZmax;
        NumberTextField ntnTypeA, ntnNotMoving, ntnTypeB;
        NumberTextField ntMinSpeedA, ntMaxSpeedA;
        NumberTextField ntMInSpeedB, ntMaxSPeedB;
        NumberTextField ntMassA, ntDiaA;
        NumberTextField ntMassB, ntDiaB;

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
            ntnTypeA = new NumberTextField(inpC, nTypeA, 6, true,
                    1, 1000, "####", "Quantity Type A");
            ntnNotMoving = new NumberTextField(inpC, nNotMoving, 6, true,
                    0, 1000, "####", "Quantity static");
            ntnTypeB = new NumberTextField(inpC, nTypeB, 6, true,
                    1, 1000, "####", "Quantity Type B");
            jp.addItemPair(ntnTypeA);
            jp.addItemPair(ntnNotMoving);
            jp.addItemPair(ntnTypeB);
            jp.addBlank();

            ntMinSpeedA = new NumberTextField(inpC, minSpeedA, 4, false,
                    0, 10, "####.##", "Min Moving Speed");
            ntMaxSpeedA = new NumberTextField(inpC, maxSpeedA, 4, false,
                    0, 10, "####.##", "Max Moving Speed");
            JPanel jpS1 = new JPanel();
            jpS1.add(ntMinSpeedA);
            jpS1.add(ntMaxSpeedA);
            jp.addItemPair("Moving Speed Limits(m/s)", jpS1);

            ntMInSpeedB = new NumberTextField(inpC, minSpeedB, 4, false,
                    0, 10, "####.##", "Min Affected Speed");
            ntMaxSPeedB = new NumberTextField(inpC, maxSpeedB, 4, false,
                    0, 10, "####.##", "Max Affected Speed");
            JPanel jpAS1 = new JPanel();
            jpAS1.add(ntMInSpeedB);
            jpAS1.add(ntMaxSPeedB);
            jp.addItemPair("Affected Limits(m/s)", jpAS1);
            jp.addBlank();

            ntMassA = new NumberTextField(inpC, massA, 6, false,
                    0.001, 1e5, "####.###", "Type A unit mass(kg)");
            ntDiaA = new NumberTextField(inpC, diaA, 6, false,
                    0.001, 10, "####.###", "Type A Dia(m)");
            ntMassB = new NumberTextField(inpC, massB, 6, false,
                    0.001, 1e5, "####.###", "Type B unit mass(kg))");
            ntDiaB = new NumberTextField(inpC, diaB, 6, false,
                    0.001, 10, "####.###", "Type B Dia(m)");
            outerPan.add(jp, BorderLayout.CENTER);
            jp.addItemPair(ntMassA);
            jp.addItemPair(ntDiaA);

            jp.addItemPair(ntMassB);
            jp.addItemPair(ntDiaB);

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

            nTypeA = (int) ntnTypeA.getData();
            nNotMoving = (int)ntnNotMoving.getData();
            nTypeB = (int) ntnTypeB.getData();

            minSpeedA = ntMinSpeedA.getData();
            maxSpeedA = ntMaxSpeedA.getData();

            minSpeedB = ntMInSpeedB.getData();
            maxSpeedB = ntMaxSPeedB.getData();

            massA = ntMassA.getData();
            diaA = ntDiaA.getData();
            massB = ntMassB.getData();
            diaB = ntDiaB.getData();
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
        return "Many Ball types in a box";
    }
}

