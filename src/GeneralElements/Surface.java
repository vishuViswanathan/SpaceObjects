package GeneralElements;

import Applications.ItemMovementsApp;
import GeneralElements.Display.TuplePanel;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;
import mvUtils.display.SmartFormatter;
import mvUtils.physics.Point3dMV;
import mvUtils.physics.Vector3dMV;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by M Viswanathan on 17 Feb 2015
 */
public class Surface extends Item {
    Vector3dMV normal;
    Point3dMV point1;
    Point3dMV point2;
    Point3dMV point3;
    double coefficientA;
    double coefficientB;
    double coefficientC;
    double coefficientD;
    double sqrtA2B2C2;
    Vector3dMV distanceFromOrigin;

    private Surface(Window parent) {
        super(parent);
        itemType = ItemType.SURFACE;
        bFixedLocation = true;
        boundaryItem = true;
    }

    public Surface(Window parent, String name) {
        this(name, new Point3d(0, 0, 0), new Point3d(5, 0, 0), new Point3d(0, 0, -5), parent);
    }

    public Surface(String name, Point3d p1, Point3d p2, Point3d p3, double collisionLossFactor, Window parent) {
        super(name, 1, 0, Color.GREEN, parent);
        itemType = ItemType.SURFACE;
        bFixedLocation = true;
        boundaryItem = true;
        setPlane(p1, p2, p3);
        this.collisionLossFactor = collisionLossFactor;
    }

    public Surface(String name, Point3d p1, Point3d p2, Point3d p3, Window parent) {
        this(name, p1, p2, p3, 1, parent);
    }

    public Surface(String xmlStr, Window parent) {
        this(parent);
        takeFromXML(xmlStr);
    }

    public void setPlane(Point3d p1, Point3d p2, Point3d p3) {
        point1 = new Point3dMV(p1);
        point2 = new Point3dMV(p2);
        point3 = new Point3dMV(p3);
        setPlane();
    }

    public Vector3d getNormal() {
        return normal;
    }


    void setPlane() {
        Vector3d line1 = new Vector3d(point2);
        line1.sub(point1);
        Vector3d line2 = new Vector3d(point1);
        line2.sub(point3);
        normal = new Vector3dMV();
        normal.cross(line1, line2);
        normal.scale((1 / normal.length()));
        coefficientA = normal.x;
        coefficientB = normal.y;
        coefficientC = normal.z;
        coefficientD = coefficientA * point1.x + coefficientB * point1.y + coefficientC * point1.z;
        setSqrtA2B2C2();
    }

    void setSqrtA2B2C2() {
        sqrtA2B2C2 = Math.sqrt(coefficientA * coefficientA + coefficientB * coefficientB + coefficientC * coefficientC);
        distanceFromOrigin = new Vector3dMV(distanceVector(new Point3d()));
    }

    public void setVisible(boolean visible) {
        // no graphic
    }

    @Override
    public void setRelOrbitVisible(boolean visible) {
        // no relative orbit
    }

    @Override
    public void setPathVisible(boolean visible) {
        // no Path
    }

    RelativeDlg relDlg;

    class RelativeDlg extends JDialog {
        ItemInterface parent;
        Vector3d tupRelPos, tupRelVel;
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        TuplePanel relPosPan, relVelPan;
        InputControl inpC;
        JComboBox othersCB;

        RelativeDlg(InputControl inpC) {
            setModal(true);
            this.inpC = inpC;
            dbInit();
        }

        void dbInit() {
            tupRelPos = new Vector3d();
            tupRelVel = new Vector3d();
            MultiPairColPanel jp = new MultiPairColPanel("Relative Data of SpaceObject");
            othersCB = new JComboBox(space.getAllItems().toArray());
            jp.addItemPair(new JLabel("Relative to "), othersCB);
            relPosPan = new TuplePanel(inpC, tupRelPos, 8, -1e20, 1e20, "##0.#####E00", "Relative position in m");
            jp.addItemPair("position in m", relPosPan);
            if (!bFixedLocation) {
                relVelPan = new TuplePanel(inpC, tupRelVel, 8, -1e20, 1e20, "##0.#####E00", "Relative Velocity in m");
                jp.addItemPair("Velocity in m/s", relVelPan);
            }
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == ok) {
                        takeValuesFromUI();
                        closeThisWindow();

                    } else {
                        closeThisWindow();
                    }
                }
            };
            ok.addActionListener(li);
            cancel.addActionListener(li);
            jp.addItemPair(cancel, ok);
            add(jp);
            pack();
        }

        void takeValuesFromUI() {
            parent = space.getAllItems().get(othersCB.getSelectedIndex());
            tupRelPos.set(relPosPan.getTuple3d());
            tupRelPos.add(parent.getPos());
            status.pos.set(tupRelPos);
            tupRelVel.set(relVelPan.getTuple3d());
            tupRelVel.add(parent.getVelocity());
            status.velocity.set(tupRelVel);
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }
    }

    public EditResponse editItem(InputControl inpC, Component c) {
        ItemDialog dlg = new ItemDialog(inpC, c);
        dlg.setVisible(true);
        return dlg.getResponse();
    }

    public EditResponse editItem(InputControl inpC) {
        return editItem(inpC, null);
    }

    class ItemDialog extends JDialog {
        JTextField tfItemName;
        JButton colorButton = new JButton("Object Color");
        JLabel banner = new JLabel("");
        TuplePanel tpPoint1, tpPoint2, tpPoint3;
        NumberTextField ntCollisionLossFactor;
        JButton itemRelButton = new JButton("Set Relative Data");
        JButton delete = new JButton("DELETE");
        JButton ok = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        InputControl inpC;
        EditResponse response = EditResponse.CANCEL;

        ItemDialog(InputControl inpC, Component c) {
            setModal(true);
            setResizable(false);
            this.inpC = inpC;
            dbInit();
            if (c == null)
                setLocation(100, 100);
            else
                setLocationRelativeTo(c);
        }
        void dbInit() {
            JPanel outerPan = new JPanel(new BorderLayout());
            MultiPairColPanel jp = new MultiPairColPanel("Data of the Surface");
            tfItemName = new JTextField(name, 10);
            jp.addItemPair("Surface Name", tfItemName);
            banner.setPreferredSize(new Dimension(100, 20));
            banner.setBackground(color);
            banner.setOpaque(true);
            colorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Color newColor = JColorChooser.showDialog(
                            ItemDialog.this,
                            "Choose Color",
                            banner.getBackground());
                    if (newColor != null) {
                        color = newColor;
                        banner.setBackground(newColor);
                    }
                }

            });
            jp.addItemPair(colorButton, banner);
            ntCollisionLossFactor = new NumberTextField(inpC, collisionLossFactor, 8, false,
                    0.0, 1.0, "0.000", "Collision Loss Factor");
            jp.addItemPair(ntCollisionLossFactor);

            jp.addItemPair("", itemRelButton);

            JPanel jpPos = new JPanel(new BorderLayout());
            tpPoint1 = new TuplePanel(inpC, point1, 8, -1e30, 1e20, "##0.#####E00", "Point1");
            tpPoint2 = new TuplePanel(inpC, point2, 8, -1e30, 1e20, "##0.#####E00", "Point2");
            tpPoint3 = new TuplePanel(inpC, point3, 8, -1e30, 1e20, "##0.#####E00", "Point3");
            jp.addItemPair("point1", tpPoint1);
            jp.addItemPair("point2", tpPoint2);
            jp.addItemPair("point3", tpPoint3);
            jp.addItem("Points in clockwise, looking at Surface of Interest");
            itemRelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    relDlg = new RelativeDlg(inpC);
                    relDlg.setLocationRelativeTo(itemRelButton);
                    relDlg.setVisible(true);
                }
            });
            outerPan.add(jp, BorderLayout.CENTER);
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == ok) {
                        if (takeValuesFromUI()) {
                            response = EditResponse.CHANGED;
                            closeThisWindow();
                        }
                    }
                    else if (src == delete) {
                        if (ItemMovementsApp.decide("Deleting Object ", "Do you want to DELETE this Object?")) {
                            response = EditResponse.DELETE;
                            closeThisWindow();
                        }
                    }
                    else
                        closeThisWindow();
                }
            };
            delete.addActionListener(li);
            ok.addActionListener(li);
            cancel.addActionListener(li);
            JPanel buttPanel = new JPanel(new BorderLayout());
            buttPanel.add(delete, BorderLayout.WEST);
            buttPanel.add(cancel, BorderLayout.CENTER);
            buttPanel.add(ok, BorderLayout.EAST);
            outerPan.add(buttPanel, BorderLayout.SOUTH);
            add(outerPan);
            pack();
        }

        EditResponse getResponse() {
            return response;
        }

        boolean takeValuesFromUI() {
            boolean retVal = false;
            name = tfItemName.getText();
            if (name.length() > 1 && (!name.substring(0,2).equals("##"))) {
                collisionLossFactor = ntCollisionLossFactor.getData();
                point1.set(tpPoint1.getTuple3d());
                point2.set(tpPoint2.getTuple3d());
                point3.set(tpPoint3.getTuple3d());
                setPlane();
                retVal = true;
            }
            else
                showError("Enter Item Name");
            return retVal;
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }
    }


    @Override
    Object getOneColData(ColType colType) {
        SmartFormatter fmt = new SmartFormatter(6);
        switch(colType) {
            case NAME:
                return name;
            case DETAILS:
                return "Surface with P1: " + point1.dataInCSV() +
                        ",    P2: " + point2.dataInCSV() +
                        ",    P3: " + point3.dataInCSV() +
                        ",  CollisionLossFactor: " + collisionLossFactor;
        }
        return "N/A";
    }

    @Override
    public double getPositionX() {
        return  distanceFromOrigin.getX();
    }

    @Override
    public double getPositionY() {
        return distanceFromOrigin.getY();
    }

    @Override
    public double getPositionZ() {
        return  distanceFromOrigin.getZ();
    }

    @Override
    public void noteInput() {
        space.debug("Surface has no input");
    }

    @Override
    public void setLocalForces() {
    }

    /**
     * Vector of shortest distance from point to the plane
     *
     * @param fromPoint
     * @return
     */
    public Vector3d distanceVector(Point3d fromPoint) {
        double len = (coefficientA * fromPoint.x + coefficientB * fromPoint.y + coefficientC * fromPoint.z - coefficientD) /
                sqrtA2B2C2;
        Vector3d distance = new Vector3d(normal);
        distance.scale(len);
        return distance;
    }

    public boolean updatePosAndVelOLD(double deltaT, double nowT, UpdateStep updateStep) throws Exception {
        return false;
    }

    public boolean updatePosAndelforGravityJetBounce(double deltaT, double nowT, UpdateStep updateStep) throws Exception {
        return false;
    }

    public boolean updatePosAndVelforLocalGlobalBounce(double deltaT, double nowT, UpdateStep updateStep) throws Exception {
        return false;
    }

    public boolean updatePosAndVelforBounceJetGlobal(double deltaT, double nowT, UpdateStep updateStep) throws Exception {
        return false;
    }

    public boolean updatePosAndVelforBounce(double deltaT, double nowT, UpdateStep updateStep) throws Exception {
        return false;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = defaultDataInXML();
        xmlStr.append(XMLmv.putTag("normal", normal.dataInCSV()));
        xmlStr.append(XMLmv.putTag("point1", point1.dataInCSV()));
        xmlStr.append(XMLmv.putTag("point2", point2.dataInCSV()));
        xmlStr.append(XMLmv.putTag("point3", point3.dataInCSV()));
//        xmlStr.append(XMLmv.putTag("coefficientD", coefficientD));
        xmlStr.append(XMLmv.putTag("collisionLossFactor", collisionLossFactor));
//        xmlStr.append(XMLmv.putTag("stickingPressure", stickingPressure));
        return xmlStr;
    }

    public boolean takeFromXML(String xmlStr) throws NumberFormatException {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "name", 0);
        name = vp.val;
        vp = XMLmv.getTag(xmlStr, "normal", vp.endPos);
        normal = new Vector3dMV(vp.val);
        vp = XMLmv.getTag(xmlStr, "point1", vp.endPos);
        point1 = new Point3dMV(vp.val);
        vp = XMLmv.getTag(xmlStr, "point2", vp.endPos);
        point2 = new Point3dMV(vp.val);
        vp = XMLmv.getTag(xmlStr, "point3", vp.endPos);
        point3 = new Point3dMV(vp.val);
        setPlane();
        vp = XMLmv.getTag(xmlStr, "collisionLossFactor", vp.endPos);
        if (vp.val.length() > 0)
            collisionLossFactor = Double.valueOf(vp.val);
        return true;
    }
}
