package GeneralElements;

import Applications.ItemMovementsApp;
import GeneralElements.Display.ItemGraphic;
import GeneralElements.Display.TuplePanel;
import GeneralElements.accessories.JetsAndSeekers;
import GeneralElements.localActions.LocalAction;
import collection.RelOrbitGroup;
import com.sun.j3d.utils.universe.ViewingPlatform;
// import jdk.nashorn.internal.scripts.JD;
import mvUtils.display.*;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;
import mvUtils.physics.ForceElement;
import mvUtils.physics.Point3dMV;
import mvUtils.physics.Torque;
import mvUtils.physics.Vector3dMV;
import time.timePlan.JetTimeController;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Transform3D;
import javax.swing.*;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Created by M Viswanathan on 31 Mar 2014
 */
public class Item extends DarkMatter implements ItemInterface {
    protected ItemType itemType;
    private String vrmlFile;
    JRadioButton rbFixedAccOn;
    double xMax, yMax, zMax;
    double xMin, yMin, zMin;
    private AxisAngle4d spinAxis; // absolute
    double spinPeriod; // in hours
    String imageName = "";
    private boolean isLightSrc = false;
    JRadioButton rbFixedPos;
    Item thisItem;
    private double reportInterval = 0; // sec?  144000;
    double nextReport; // sec
    Vector<ForceElement> forceElements = new Vector<ForceElement>();
    Point3d centerOfMass = new Point3d(0, 0, 0);
    double[] mI = {1.0, 1.0, 1.0}; // about xx, yy and zz
    private Vector3d miAsVector = new Vector3d();
    private Vector3d oneByMI = new Vector3d();
    Vector3d netTorque = new Vector3d();
    Vector3d jetForce = new Vector3d();
    Torque jetTorque = new Torque();
    Vector3d additionalAngularVel = new Vector3d();
    JetTimeController jetController;


    protected Item(Window parent) {
        super(parent);
        itemType = ItemType.SPHERE;
        thisItem = this;
    }

    public Item(Window theParent, String name, ItemType type) {
        super(theParent);
        itemType = ItemType.SPHERE;
        thisItem = this;
    }

    public Item(Window theParent, String name) {
        this(name, 1, 1, Color.RED, theParent);
    }

    public Item(String name, double mass, double dia, Color color, Window parent) {
        super(name, mass, dia, color, parent);
        itemType = ItemType.SPHERE;
        setRadioButtons();
        thisItem = this;
    }

    public Item(String name, double mass, String vrmlFile, Window parent) {
        super(name, mass, 1, Color.green, parent);
        itemType = ItemType.VMRL;
        this.vrmlFile = vrmlFile;
        jetController = new JetTimeController(this);
        setRadioButtons();
        thisItem = this;
    }


    public Item(String xmlStr, Window parent) {
        this(parent);
        setRadioButtons();
        takeFromXML(xmlStr);
    }

    public ItemType getItemType() {
        return itemType;
    }

    public ItemSpace getSpace() {
        return space;
    }

    public String getName() {
        return name;
    }

    public void noteTotalGM(double totalGM) {
        this.totalGM = totalGM;
        balanceGM = totalGM - gm;
    }

    public double getGM() {
        return gm;
    }

    @Override
    public String getImageName() {
        return imageName;
    }

    public Vector3d getVelocity() {
        return status.velocity;
    }

    public void initConnections() {
        if (jetController != null)
            jetController.initConnections(space);
    }

    public Vector3d getVelocity(ItemInterface relativeTo) {
        Vector3d v = new Vector3d(status.velocity);
        v.sub(relativeTo.getVelocity());
        return v;
    }

    public Point3d getPos() {
        return status.pos;
    }

    public String getVrmlFile() {
        return vrmlFile;
    }

    public AxisAngle4d getSpinAxis() {
        return spinAxis;
    }

    public boolean isLightSrc() {
        return isLightSrc;
    }

    public Vector3d getMiAsVector() {
        return miAsVector;
    }

    public Vector3d getOneByMI() {
        return oneByMI;
    }

    public void setJetController(JetTimeController jetController) {
        this.jetController = jetController;
    }

    private void setMomentsOfInertia(double commonMI) throws Exception {
        setMomentsOfInertia(commonMI, commonMI, commonMI);
    }

    public void setMomentsOfInertia(double mIxx, double mIyy, double mIzz) throws Exception {
        if (mIxx > 0 && mIyy > 0 && mIzz > 0) {
            mI[Torque.AboutX] = mIxx;
            mI[Torque.AboutY] = mIyy;
            mI[Torque.AboutZ] = mIzz;
            miAsVector.set(mIxx, mIyy, mIzz);
            oneByMI.set(1 / mIxx, 1 / mIyy, 1 / mIzz);
        } else
            throw new Exception("Some Moment of Inertial parameters are not acceptable");
    }

    private boolean setMomentsOfInertia(String csv) {
        boolean retVal = false;
        String[] mIset = csv.split(",");
        try {
            if (mIset.length == 3) {
                setMomentsOfInertia(Double.valueOf(mIset[0]), Double.valueOf(mIset[1]), Double.valueOf(mIset[2]));
                retVal = true;
            }
        } catch (Exception e) {
            retVal = false;
        }
        return retVal;
    }

    @Override
    public boolean takeBasicFrom(DarkMatter fromItem) {
        if (super.takeBasicFrom(fromItem)) {
            if (fromItem instanceof Item) {
                Item fromItem1 = (Item) fromItem;
                itemType = fromItem1.itemType;
                imageName = fromItem1.imageName;
                isLightSrc = fromItem1.isLightSrc;
            }
            return true;
        } else
            return false;
    }

    static public Item getNewItem(ItemSpace theSpace, String theName, Window theParent) {
        Item theItem = null;
        ItemBasic dlg = new ItemBasic(theSpace, theName);
        dlg.setLocationRelativeTo(theParent);
        dlg.setVisible(true);
        if (dlg.getResponse() == EditResponse.OK) {
            ItemType selectedType = dlg.getSelectedType();
            switch (selectedType) {
                case SURFACE:
                    theItem = new Surface(theParent, theName);
                    break;
                case VMRL:
                    theItem = new Item(theName, 10000, "VRML\\rocket.wrl", theParent);
                    break;
                case SPHERE:
                    theItem = new Item(theParent, theName);
                    break;
            }
        }
        return theItem;
    }

    static class ItemBasic extends JDialog {
        JComboBox jcItem = new JComboBox(ItemType.values());
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ItemSpace theSpace;
        String theName;
        EditResponse response = EditResponse.CANCEL;

        ItemBasic(ItemSpace theSpace, String theName) {
            setModal(true);
            this.theSpace = theSpace;
            this.theName = theName;
            jcItem.setSelectedItem(ItemType.SPHERE);
            setTitle("Selection Object Type");
            MultiPairColPanel jp = new MultiPairColPanel("Selection Object Type");
            jp.addItemPair("Selected Type", jcItem);
            jp.addBlank();
            jp.addItemPair(cancel, ok);
            add(jp);
            pack();
            ActionListener li = e -> {
                Object src = e.getSource();
                if (src == ok)
                    response = EditResponse.OK;
                closeThisWindow();
            };
            ok.addActionListener(li);
            cancel.addActionListener(li);
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }

        ItemType getSelectedType() {
            return (ItemType) jcItem.getItemAt(jcItem.getSelectedIndex());
        }

        EditResponse getResponse() {
            return response;
        }
    }

    static public Item getItemFromXML(String xmlStr, Window parent) {
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "itemType", 0);
        boolean done = false;
        Item theItem = null;
        if (vp.val.length() > 2) {
            ItemType nowType = ItemType.getEnum(vp.val);
            if (nowType != null) {
                switch (nowType) {
                    case SURFACE:
                        theItem = new Surface(xmlStr, parent);
                        done = true;
                        break;
                    case VMRL:
                    case SPHERE:
                        theItem = new Item(xmlStr, parent);
                        done = true;
                        break;
                }
            }
        }
        if (!done)
            theItem = new Item(xmlStr, parent);
        return theItem;
    }

    public static String[] getColHeader() {
        ColType[] values = ColType.values();
        String[] colHeader = new String[values.length];
        for (int i = 0; i < colHeader.length; i++)
            colHeader[i] = "" + values[i];
        return colHeader;
    }

    public static int[] getColumnWidths() {
        ColType[] values = ColType.values();
        int[] colWidths = new int[values.length];
        for (int i = 0; i < colWidths.length; i++)
            colWidths[i] = oneColWidth(values[i]);
        return colWidths;
    }

    static int oneColWidth(ColType colType) {
        switch (colType) {
            case SLNO:
                return 30;
            case NAME:
                return 100;
            case DETAILS:
                return 630;
        }
        return 0;
    }

    public Object[] getRowData(int slNo) {
        ColType[] values = ColType.values();
        Object[] rowData = new Object[values.length];
        rowData[0] = "" + slNo;
        for (int i = 1; i < rowData.length; i++)
            rowData[i] = getOneColData(values[i]);
        return rowData;
    }

    Object getOneColData(ColType colType) {
        SmartFormatter fmt = new SmartFormatter(6);
        switch (colType) {
            case NAME:
                return name;
            case DETAILS:
                return "Sphere with Mass:" + fmt.format(mass) +
                        ",    Dia:" + fmt.format(dia) +
                        ",    Pos:" + status.dataInCSV(ItemStat.Param.POSITION, 4) +
                        ((bFixedLocation) ?
                                " Static" : ",    Vel:" + status.dataInCSV(ItemStat.Param.VELOCITY, 4)) +
                        ((status.angularAcceleration.isNonZero()) ?
                                ", angVel:" + status.dataInCSV(ItemStat.Param.OMEGA, 4) : "");
        }
        return "";
    }

    void setRadioButtons() {
        rbFixedPos = new JRadioButton("Fixed Position");
        rbFixedAccOn = new JRadioButton("Directional Acceleration ON");

    }

    public void setbFixedLocation(boolean set) {
        bFixedLocation = set;
        rbFixedPos.setSelected(set);
    }

    public void setRefreshInterval(double interval, double nextRefresh) {
        reportInterval = interval;
        nextReport = nextRefresh;
    }

    RelativeDlg relDlg;

    public class RelativeDlg extends JDialog {
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

    public EditResponse editItem(String title, InputControl inpC, Component c) {
        ItemDialog dlg = new ItemDialog(title, inpC, c);
        dlg.setVisible(true);
        return dlg.getResponse();
    }

    public void showItem(String title, InputControl inpC, Component c) {
        ItemDialog dlg = new ItemDialog(title, false, inpC, c);
        dlg.setVisible(true);
    }

    public void editItemKeepingPosition(String title, InputControl inpC, Component c) {
        ItemDialog dlg = new ItemDialog(title, true, true, inpC, c);
        dlg.setVisible(true);
    }

    public EditResponse editItem(InputControl inpC, Component c) {
        return editItem("", inpC, c);
    }

    class ItemDialog extends JDialog {
        Component parent;
        JTextField tfItemName;
        JTextField tfVRMLflePath;
        JTextField tfImageFilePath;
        JButton colorButton = new JButton("Object Color");
        JLabel banner = new JLabel("");
        NumberTextField ntItemMass, ntItemDia;
        TuplePanel tpMI;
        NumberTextField ntCommonMI;
        NumberTextField ntElasticity;
        TuplePanel itemPosTuplePan;
        TuplePanel angularPosTuplePan;
        TuplePanel angularVelTuplePan;
        JRadioButton rbItemFixedPos;
        TuplePanel itemVelTuplePan;
        JButton itemRelButton = new JButton("Set Relative Data");
        JButton jbManageJets = new JButton("Manage Jets");
        JButton delete = new JButton("DELETE");
        JButton ok = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        InputControl inpC;
        EditResponse response = EditResponse.CHANGED;
        boolean allowEdit = false;
        boolean freezePosition = false;
        JDialog thisDlg;
        double commonMI;

        ItemDialog(String title, boolean allowEdit, boolean freezePosition, InputControl inpC, Component c) {
            setModal(true);
            setResizable(false);
            thisDlg = this;
            this.inpC = inpC;
            this.parent = c;
            this.allowEdit = allowEdit;
            if (allowEdit)
                this.freezePosition = freezePosition;
            setTitle(title);
            dbInit();
            if (c == null)
                setLocation(100, 100);
            else
                setLocationRelativeTo(c);
        }

        ItemDialog(String title, boolean allowEdit, InputControl inpC, Component c) {
            this(title, allowEdit, false, inpC, c);
        }

        ItemDialog(String title, InputControl inpC, Component c) {
            this(title, true, false, inpC, c);
        }

        void dbInit() {
            JPanel outerPan = new JPanel(new BorderLayout());
            MultiPairColPanel jp = new MultiPairColPanel("Data of Item");
            tfItemName = new JTextField(name, 10);
            jp.addItemPair("Object Name", tfItemName);
            if (itemType == ItemType.VMRL) {
                tfVRMLflePath = new JTextField(vrmlFile, 30);
                jp.addItemPair("VRML File Path", tfVRMLflePath);
            } else {
                if (itemType == ItemType.SPHERE) {
                    tfImageFilePath = new JTextField(imageName, 30);
                    jp.addItemPair("Image File Path", tfImageFilePath);
                    jp.addItem("The base path 'images/'");
                }
                banner.setPreferredSize(new Dimension(100, 20));
                banner.setBackground(color);
                banner.setOpaque(true);
                colorButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Color newColor = JColorChooser.showDialog(
                                ItemDialog.this,
                                "Choose Item Color",
                                banner.getBackground());
                        if (newColor != null) {
                            color = newColor;
                            banner.setBackground(newColor);
                        }
                    }

                });
                jp.addItemPair(colorButton, banner);
            }
            ntItemDia = new NumberTextField(inpC, dia, 6, false, 1e-20, 1e20, "##0.#####E00", "Dia in m");
            jp.addItemPair(ntItemDia);
            if (itemType == ItemType.VMRL)
                jp.addItem("(Dia is for collision check)");
            ntItemMass = new NumberTextField(inpC, mass, 8, false, 1e-30, 1e40, "##0.#####E00", "Mass in kg");
            jp.addItemPair(ntItemMass);
            if (itemType == ItemType.VMRL) {
                tpMI = new TuplePanel(inpC, new Vector3d(mI), 6, 1e-20, 1e20, "#,###.000", "Mass Moment of Inertia (kg.m2)");
                jp.addItemPair(tpMI.getTitle(), tpMI);
            }
            else if (itemType == ItemType.SPHERE) {
                commonMI = mI[0];
                ntCommonMI = new NumberTextField(inpC, commonMI, 6, false, 1e-20, 1e20, "#,###.000", "Mass Moment of Inertia (kg.m2)");
                jp.addItemPair(ntCommonMI);
            }
            ntElasticity = new NumberTextField(inpC, eCompression, 6, false, -1, 1e20, "##0.####E00", "Elasticity N/100% ('-1' is sticky)");
            jp.addItemPair(ntElasticity);
            jp.addItem("<html><font color='red'>WARNING: <font color='black'> Sticky is still in trial stage</html>");
            JPanel jpAngularPos = new JPanel(new BorderLayout());
            angularPosTuplePan = new TuplePanel(inpC, status.angularPos, 8, -10, +10, "##0.####", "Angular Orientation (on local axis) rad");
            jpAngularPos.add(angularPosTuplePan, BorderLayout.CENTER);
            jp.addItemPair("Angular Position in rad(local)", jpAngularPos);
            JPanel jpAngularVel = new JPanel(new BorderLayout());
            angularVelTuplePan = new TuplePanel(inpC, status.angularVelocity, 8, -100, +100, "##0.####", "Angular Velocity (on local axis) rad/s");
            jpAngularVel.add(angularVelTuplePan, BorderLayout.CENTER);
            jp.addItemPair("Angular Velocity in rad/s(local)", jpAngularVel);
            jp.addBlank();
            jp.addItemPair("", itemRelButton);

            JPanel jpPos = new JPanel(new BorderLayout());
            itemPosTuplePan = new TuplePanel(inpC, status.pos, 8, -1e30, 1e20, "##0.#####E00", "Position in m");
            rbItemFixedPos = new JRadioButton("Fixed Position", bFixedLocation);
            rbItemFixedPos.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    bFixedLocation = rbItemFixedPos.isSelected();
                }
            });
            jpPos.add(itemPosTuplePan, BorderLayout.CENTER);
            jp.addItemPair("Position in m", jpPos);
            jp.addItemPair("", rbItemFixedPos);

            JPanel jpVel = new JPanel(new BorderLayout());
            itemVelTuplePan = new TuplePanel(inpC, status.velocity, 8, -1e20, 1e20, "##0.#####E00", "Velocity im m/s");
            jpVel.add(itemVelTuplePan, BorderLayout.CENTER);
            jp.addItemPair("Velocity in m/s", jpVel);
            jp.addBlank();
            jp.addItemPair("Jets", jbManageJets);
            itemRelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    relDlg = new RelativeDlg(inpC);
                    relDlg.setLocationRelativeTo(itemRelButton);
                    relDlg.setVisible(true);
                    itemPosTuplePan.updateTuple(status.pos);
                    angularPosTuplePan.updateTuple(status.angularPos);
                    itemVelTuplePan.updateTuple(status.velocity);
                    angularVelTuplePan.updateTuple(status.angularVelocity);
                }
            });
            outerPan.add(jp, BorderLayout.CENTER);
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == jbManageJets) {
                        editJetList(thisDlg);
                    } else if (src == ok) {
                        if (takeValuesFromUI())
                            closeThisWindow();
                    } else if (src == delete) {
                        if (ItemMovementsApp.decide("Deleting Object ", "Do you want to DELETE this Object?")) {
                            response = EditResponse.DELETE;
                            closeThisWindow();
                        }
                    } else {
                        response = EditResponse.NOTCHANGED;
                        closeThisWindow();
                    }
                }
            };
            delete.addActionListener(li);
            ok.addActionListener(li);
            cancel.addActionListener(li);
            jbManageJets.addActionListener(li);
            JPanel buttPanel = new JPanel(new BorderLayout());
            buttPanel.add(delete, BorderLayout.WEST);
            buttPanel.add(cancel, BorderLayout.CENTER);
            buttPanel.add(ok, BorderLayout.EAST);
            outerPan.add(buttPanel, BorderLayout.SOUTH);
            add(outerPan);
            setAllowEdit();
            pack();
        }

        void setAllowEdit() {
            if (!allowEdit) {
                tfItemName.setEditable(false);
                colorButton.setEnabled(false);
                ntItemMass.setEditable(false);
                ntItemDia.setEditable(false);
                ntElasticity.setEditable(false);
                itemPosTuplePan.setEditable(false);
                angularPosTuplePan.setEditable(false);
                angularVelTuplePan.setEnabled(false);
                rbItemFixedPos.setEnabled(false);
                itemVelTuplePan.setEditable(false);
                itemRelButton.setEnabled(false);
                jbManageJets.setEnabled(false);
                delete.setEnabled(false);
                ok.setEnabled(false);
            }
            if (freezePosition) {
                itemRelButton.setEnabled(false);
                itemPosTuplePan.setEditable(false);
                delete.setEnabled(false);
            }

        }

        void editJetList(Component c) {
            jetController.editJetController(inpC, c);
        }

        EditResponse getResponse() {
            return response;
        }

        boolean takeValuesFromUI() {
            boolean retVal = false;
            name = tfItemName.getText();
            if (name.length() > 1 && (!name.substring(0, 2).equals("##"))) {
//                if (space.noNameRepeat(name)) {
                if (space.getItem(name) != this) {
                    try {
                        setMass(ntItemMass.getData());
//                    mass = ntItemMass.getData();
                        if (itemType == ItemType.SPHERE) {
                            imageName = tfImageFilePath.getText().trim();
                            commonMI = ntCommonMI.getData();
                            setMomentsOfInertia(commonMI);
                        }
                        dia = ntItemDia.getData();
                        if (itemType == ItemType.VMRL) {
                            vrmlFile = tfVRMLflePath.getText().trim();
                            Vector3d mIxyz = new Vector3d(tpMI.getTuple3d());
                            setMomentsOfInertia(mIxyz.x, mIxyz.y, mIxyz.z);
                        }
                        eCompression = ntElasticity.getData();
                        status.pos.set(itemPosTuplePan.getTuple3d());
                        status.angularPos.set(angularPosTuplePan.getTuple3d());
                        status.velocity.set(itemVelTuplePan.getTuple3d());
                        status.angularVelocity.set(angularVelTuplePan.getTuple3d());
                        bFixedLocation = rbItemFixedPos.isSelected();
                        retVal = true;
                    } catch (Exception e) {
                        showError("Some parameter is not acceptable");
                        retVal = false;
                    }
                }
                else {
                    showError("Item name " + name + " already exisits in the list");
                    retVal = false;
                }
            } else
                showError("Enter Item Name");
            return retVal;
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }
    }

    public void noteInput() {
        calculateAreas();
        resetLimits();
        status.time = 0;
        nextReport = 0;
        initNetForce();
    }

    public ItemInterface[] getOtherItems() {
        return space.getOtherItems(this);
    }

    public boolean hasAnyAccessories() {
        return (jetController != null);
    }

    public boolean anyLocalAction() {
        return (localActions.size() > 0);
    }

    public Window showControlPanel(InputControl inpC, Component parent) {
        ControlPanelDialog dlg = null;
        if (jetController != null) {
            parent.setEnabled(false);
            dlg = new ControlPanelDialog(inpC, parent);
            dlg.setVisible(true);
        }
        return dlg;
    }

    class ControlPanelDialog extends JFrame {
        Component parent;
        InputControl inpC;
        JButton close = new JButton("Close");

        ControlPanelDialog(InputControl inpC, Component parent) {
//            setModal(true);
            setResizable(false);
            this.inpC = inpC;
            this.parent = parent;
            setTitle("Control Panel");
            dbInit();
            if (parent == null)
                setLocation(100, 100);
            else
                setLocationRelativeTo(parent);
        }

        void dbInit() {
            JPanel outerPan = new JPanel(new BorderLayout());
            LinkedList<ItemInterface> allItems = space.getAllItems();
            outerPan.add(jetController.controlPanel(parent, inpC, space.getOtherItems(Item.this)), BorderLayout.CENTER);
            JPanel buttonP = new JPanel();
            close.addActionListener(e -> {
                closeIt();
            });
            buttonP.add(close);
            outerPan.add(buttonP, BorderLayout.SOUTH);
            add(outerPan);
            pack();
        }

        void closeIt() {
            setVisible(false);
            parent.setEnabled(true);
        }
    }

    void resetLimits() {
        xMax = Double.NEGATIVE_INFINITY;
        yMax = Double.NEGATIVE_INFINITY;
        zMax = Double.NEGATIVE_INFINITY;
        xMin = Double.POSITIVE_INFINITY;
        yMin = Double.POSITIVE_INFINITY;
        zMin = Double.POSITIVE_INFINITY;
    }

    private void evalForceFromBuiltInSource(double duration, double nowT) { // TODO
        // get Jet Force and torque
        if (jetController != null) {
            jetForce.set(0, 0, 0);
            jetTorque.set(0, 0, 0);
            jetController.upDateAllJetStatus(duration, nowT);
            for (JetsAndSeekers oneJet : jetController.jets) {
                oneJet.addEffect();
            }
//            Transform3D tr = new Transform3D();
//            itemGraphic.get().getTotalTransform(tr);
            Transform3D tr = itemToGlobal();
            tr.transform(jetForce);
            tr.transform(jetTorque);// this has to be revered for angular pos calculations
        }
    }

    public void setLocalForces() {
        netTorque.set(0, 0, 0);
        addVelocity.set(0, 0, 0);
        setMatterLocalForces();
        netForce.add(jetForce);
        netTorque.add(jetTorque);
    }

    public void initStartForce() {
        super.initNetForce();
        additionalAngularVel.set(0, 0, 0);
        netTorque.set(0, 0, 0);
    }

    public void addTojetForce(Vector3d addForce) {
        jetForce.add(addForce);
    }

    public void addToJetTorque(Vector3d addTorque) {
        jetTorque.add(addTorque);
    }

    public synchronized void addToTorque(Vector3d angularAcceleration)  {
        Vector3dMV effectiveTorque = new Vector3dMV();
        effectiveTorque.scale(miAsVector, angularAcceleration);
        netTorque.add(effectiveTorque);
    }

    public synchronized void subtractFromTorque(Vector3d angularAcceleration) {
        Vector3dMV effectiveTorque = new Vector3dMV();
        effectiveTorque.scale(miAsVector, angularAcceleration);
        netTorque.sub(effectiveTorque);
    }

    @Override
    public void setStartConditions(double duration, double nowT) {
//        lastTorque.set(jetTorque);
        additionalAngularVel.set(0, 0, 0);
        lastTorque.set(netTorque);
        lastAngularVelocity.set(status.angularVelocity);
        lastAngle.set(status.angularPos);
        super.setMatterStartConditions(duration, nowT);
        evalForceFromBuiltInSource(duration, nowT);
    }

    public synchronized void addToAngularVel(Vector3d angularVel) {
        additionalAngularVel.add(angularVel);
    }

    public synchronized void subtractFromVel(Vector3d angularVel) {
        additionalAngularVel.sub(angularVel);
    }


    void evalMaxMinPos() {
        xMax = Math.max(xMax, status.pos.x);
        yMax = Math.max(yMax, status.pos.y);
        zMax = Math.max(zMax, status.pos.z);
        xMin = Math.min(xMin, status.pos.x);
        yMin = Math.min(yMin, status.pos.y);
        zMin = Math.min(zMin, status.pos.z);
    }


    public void setImage(String imageName) {
        this.imageName = imageName;
    }


    public void initPosEtc(Point3d pos, Vector3d velocity) {
        super.initPosEtc(pos, velocity);
        nextReport = reportInterval;
    }

    @Override
    public void setInitialAcceleration(Vector3d acc) {
        status.acc.set(acc);
    }

    public void initAngularPosEtc(Vector3d angularPos, Vector3d angularVelocity) {
        status.initAngularPos(angularPos, angularVelocity);
    }

    public void setSpin(AxisAngle4d spinAxis, double spinPeriod) {
        this.spinAxis = spinAxis;
        this.spinPeriod = spinPeriod;
        if (spinPeriod > 0)
            radPerSec = Math.PI * 2 / spinPeriod;
    }

    // The new methods with ItemGraphic

    WeakReference<ItemGraphic> itemGraphic;

    public ItemGraphic getItemGraphic() {
        return itemGraphic.get();
    }

    ItemGraphic itemG;

    public ItemGraphic createItemGraphic(Group grp, RenderingAttributes orbitAtrib) throws Exception {
//        ItemGraphic itemG = new ItemGraphic(this);
        itemG = new ItemGraphic(this);
        if (itemG.addObjectAndOrbit(grp, orbitAtrib)) {
            itemGraphic = new WeakReference<ItemGraphic>(itemG);
            itemGraphic.get().updateAngularPosition(status.angularPos);
        } else
            itemG = null;
        return itemG;
    }

    public void setItemDisplayAttribute(RenderingAttributes itemAttribute) {
        itemGraphic.get().setItemDisplayAttribute(itemAttribute);
    }

    public void attachPlatform(ViewingPlatform platform, boolean bShowRelOrbit,
                               RenderingAttributes relOrbitAtrib, RelOrbitGroup relOrbitGroup) {
        try {
            itemGraphic.get().attachPlatform(platform, bShowRelOrbit, relOrbitAtrib, relOrbitGroup);
        } catch (NullPointerException e) {
            showError("ERROR in Attaching a platform to an item " + name + ":" + e.getMessage());
        }
    }

    public void detachPlatform() {
        try {
            itemGraphic.get().detachPlatform();
        } catch (NullPointerException e) {
            showError("ERROR in Detaching a platform for an item " + name + ":" + e.getMessage());
        }
    }

    public void setScale(double scale) {
        try {
            ItemGraphic g = itemGraphic.get();
            if (g != null)
                g.setScale(scale);
        } catch (Exception e) {
            showError("ERROR in setScale an item " + name + ":" + e.getMessage());
        }
    }

    public void updateOrbitAndPos() throws Exception {
        try {
            itemGraphic.get().updateOrbitAndPos(getSpinIncrement());
        } catch (NullPointerException e) {
            showError("ERROR in updateOrbitAndPos an item " + name + ":" + e.getMessage());
        }
    }

    public void enableLightSrc(boolean ena) {
        isLightSrc = ena;
    }

    //    =========================== calculations ======================

    public boolean updatePosAndVelOLD(double deltaT, double nowT, UpdateStep updateStep) throws Exception {

        updateAngularPosAndVelocity(deltaT, nowT, updateStep);
        updatePAndVOLD(deltaT, nowT, updateStep);
        evalMaxMinPos();
        if (nowT > nextReport) {
            updateOrbitAndPos();
            nextReport += reportInterval;
        }

        return true;
    }

    public boolean updatePosAndVelAllActions(double deltaT, double nowT, UpdateStep updateStep) throws Exception {

        updateAngularPosAndVelocity(deltaT, nowT, updateStep);
        updatePAndVforAllActions(deltaT, nowT, updateStep);
        evalMaxMinPos();
        if (nowT > nextReport) {
            updateOrbitAndPos();
            nextReport += reportInterval;
        }

        return true;
    }

    public boolean updatePosAndVelGravityOnly(double deltaT, double nowT, UpdateStep updateStep) throws Exception {

        updateAngularPosAndVelocity(deltaT, nowT, updateStep);
        updatePAndVforGravityOnly(deltaT, nowT, updateStep);
        evalMaxMinPos();
        if (nowT > nextReport) {
            updateOrbitAndPos();
            nextReport += reportInterval;
        }

        return true;
    }

    public boolean updatePosAndVelforNetForceOnly(double deltaT, double nowT, UpdateStep updateStep) throws Exception {

        updateAngularPosAndVelocity(deltaT, nowT, updateStep);
        updatePAndVforNetForceOnly(deltaT, nowT, updateStep);
        evalMaxMinPos();
        if (nowT > nextReport) {
            updateOrbitAndPos();
            nextReport += reportInterval;
        }

        return true;
    }

    public boolean updatePosAndVelnoGravityNoNetForce(double deltaT, double nowT, UpdateStep updateStep) throws Exception {

        updateAngularPosAndVelocity(deltaT, nowT, updateStep);
        updatePAndVnoGravityNoNetForce(deltaT, nowT, updateStep);
        evalMaxMinPos();
        if (nowT > nextReport) {
            updateOrbitAndPos();
            nextReport += reportInterval;
        }

        return true;
    }


    void noteAngularStatus() {
        status.angularPos.set(newAngle);
        status.angularVelocity.set(newAngularVelocity);
        status.angularAcceleration.set(thisAngularAcc);
    }

    public Transform3D globalToItem() {
        Transform3D tr = new Transform3D();
        itemGraphic.get().getTotalTransform(tr);
        tr.invert();
        return tr;
    }

    public Transform3D itemToGlobal() {
        Transform3D tr = new Transform3D();
        itemGraphic.get().getTotalTransform(tr);
        return tr;
    }

    Vector3d lastTorque = new Torque();
    Vector3d lastAngularVelocity = new Vector3d();
    Vector3dMV effectiveTorque = new Vector3dMV();
    Vector3dMV thisAngularAcc = new Vector3dMV();
    Vector3dMV deltaAngularV = new Vector3dMV();
    Vector3dMV newAngularVelocity = new Vector3dMV();
    Vector3dMV averageAngularV = new Vector3dMV();
    Vector3dMV deltaAngle = new Vector3dMV();
    Vector3d lastAngle = new Vector3d();
    Vector3dMV newAngle = new Vector3dMV();

    boolean updateAngularPosAndVelocity(double deltaT, double nowT, UpdateStep updateStep) {
        boolean changed = false;
        switch(updateStep) {
            case FINAL:
            case EuFwd:
            case EUMod:
            case RK4:
                globalToItem().transform(netTorque);
                effectiveTorque.setMean(netTorque, lastTorque);
                thisAngularAcc.scale(oneByMI, effectiveTorque);
                deltaAngularV.scale(deltaT, thisAngularAcc);
                newAngularVelocity.add(lastAngularVelocity, deltaAngularV);
                averageAngularV.setMean(lastAngularVelocity, newAngularVelocity);
                deltaAngle.scale(deltaT, averageAngularV);
                newAngle.add(lastAngle, deltaAngle);
                itemGraphic.get().updateAngularPosition(deltaAngle);
                noteAngularStatus();
                changed = true;
                break;
        }
        return changed;
    }


    public void updateAngularPosition(Vector3dMV deltaAngle){
        itemGraphic.get().updateAngularPosition(deltaAngle);
    }

    double lastTime = 0;
    double radPerSec = 0;

    double getSpinIncrement() {
        double spinIncrement = 0;
        if (radPerSec > 0) {
            double nowTime = status.time;
            double interval = (nowTime - lastTime);
            spinIncrement = interval * radPerSec;
            lastTime = nowTime;
        }
        return spinIncrement;
    }

//    =============================================

    public StringBuilder statusStringForCSV(double posFactor, double velFactor) {
        StringBuilder csvStr = new StringBuilder(name + "," + gmID + "," + gm + "\n");
        csvStr.append("Position," + status.positionStringForCSV(posFactor) + "\n");
        csvStr.append("Velocity,").append(status.velocityStringForCSV(velFactor)).append("\n");
        csvStr.append("AngVel,").append(status.angularVelocityStringForCSV(1)).append("\n");
        return csvStr;
    }

    public StringBuilder statusStringForHistory(double posFactor, double velFactor) {
        StringBuilder csvStr = new StringBuilder(name + "," + gmID + "," + mass + "," + gm + ",");
        csvStr.append(status.positionStringForCSV(posFactor) + ",");
        csvStr.append(status.velocityStringForCSV(velFactor) + ",");
        csvStr.append(status.accelerationStringForCSV(velFactor)); //.append("\n");
        return csvStr;
    }

     protected StringBuilder defaultDataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("name", name));
        xmlStr.append(XMLmv.putTag("itemType", ("" + itemType)));
        return xmlStr;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = defaultDataInXML();
        if (itemType == ItemType.VMRL)
            xmlStr.append(XMLmv.putTag("vrmlFile", vrmlFile));
        else if (itemType == ItemType.SPHERE)
            xmlStr.append(XMLmv.putTag("imageName", imageName));
        xmlStr.append(XMLmv.putTag("mass", mass)).append(XMLmv.putTag("dia", dia));
        xmlStr.append(XMLmv.putTag("mI", Vector3dMV.dataInCSV(mI)));
        xmlStr.append(XMLmv.putTag("eCompression", eCompression));
        xmlStr.append(XMLmv.putTag("color", ("" + color.getRGB())));
        xmlStr.append(XMLmv.putTag("status", ("" + status.dataInXML())));
        xmlStr.append(XMLmv.putTag("bFixedLocation", bFixedLocation));
        xmlStr.append(XMLmv.putTag("nLocalActions", localActions.size()));
        int a = 0;
        for (LocalAction action : localActions) {
            xmlStr.append(XMLmv.putTag("a#" + ("" + a).trim(), action.dataInXML().toString()));
            a++;
        }
        if (jetController != null)
            xmlStr.append(XMLmv.putTag("jetController", jetController.dataInXML()));
        return xmlStr;
    }

    public boolean takeFromXML(String xmlStr) throws NumberFormatException {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "name", 0);
        name = vp.val;

        vp = XMLmv.getTag(xmlStr, "itemType", vp.endPos);
        if (vp.val.length() > 0)
            itemType = ItemType.getEnum(vp.val);
        if (itemType == ItemType.VMRL) {
            vp = XMLmv.getTag(xmlStr, "vrmlFile", vp.endPos);
            vrmlFile = vp.val;
        }
        vp = XMLmv.getTag(xmlStr,"imageName", vp.endPos);
        imageName = vp.val;
        vp = XMLmv.getTag(xmlStr, "mass", 0);
        setMass(Double.valueOf(vp.val));
//        mass = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "mI", vp.endPos);
        if (vp.val.length() > 0)
            setMomentsOfInertia(vp.val);
        vp = XMLmv.getTag(xmlStr, "dia", 0);
        dia = Double.valueOf(vp.val);
        radius = dia / 2;
        vp = XMLmv.getTag(xmlStr, "eCompression", 0);
        if (vp.val.length() > 1)
            eCompression = Double.valueOf(vp.val);
        else
            eCompression = 0;
        vp = XMLmv.getTag(xmlStr, "bFixedLocation", 0);
        bFixedLocation = (vp.val.equals("1"));
        vp = XMLmv.getTag(xmlStr, "color", 0);
        color = new Color(Integer.valueOf(vp.val));
        vp = XMLmv.getTag(xmlStr, "status", 0);
        try {
            if (status == null)
                status = new ItemStat(vp.val);
            else
                status.takeFromXML(xmlStr);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Status data of " + name + " :" + e.getMessage());
        }
        vp = XMLmv.getTag(xmlStr, "nLocalActions", vp.endPos);
        if (vp.val.length() > 0) {
            int nActions = Integer.valueOf(vp.val);
            try {
                for (int a = 0; a < nActions; a++) {
                    vp = XMLmv.getTag(xmlStr, "a#" + ("" + a).trim(), vp.endPos);
                    addLocalAction(LocalAction.getLocalAction(this, vp.val));
                }
            } catch (Exception e) {
                retVal = false;
                e.printStackTrace();
            }
        }
        vp = XMLmv.getTag(xmlStr, "jetController", vp.endPos);
        if (vp.val.length() > 2)
            jetController = new JetTimeController(this, vp.val);
        return retVal;
    }

    void debug(String msg) {
        System.out.println("Item: " + msg);
    }

}
