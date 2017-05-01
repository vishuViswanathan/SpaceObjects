package GeneralElements;

import Applications.ItemMovementsApp;
import GeneralElements.Display.ItemGraphic;
import GeneralElements.Display.TuplePanel;
import GeneralElements.localActions.LocalAction;
import com.sun.j3d.utils.universe.ViewingPlatform;
import mvUtils.display.*;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;
import mvUtils.physics.ForceElement;
import mvUtils.physics.Point3dMV;
import mvUtils.physics.Torque;
import mvUtils.physics.Vector3dMV;
import time.timePlan.FlightPlan;
import time.timePlan.FlightPlanEditor;
import time.timePlan.JetTimeController;

import javax.media.j3d.Group;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Transform3D;
import javax.swing.*;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Created by M Viswanathan on 31 Mar 2014
 */
public class Item extends DarkMatter {
     public enum ItemType {
        SPHERE("Sphere"), // default spherical object
        SURFACE("Surface"),
        VMRL("from VMRL file");

        private final String typeName;

        ItemType(String actionName) {
            this.typeName = actionName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static ItemType getEnum(String text) {
            ItemType retVal = null;
            if (text != null) {
                for (ItemType b : ItemType.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

    public enum EditResponse{CHANGED, NOTCHANGED, DELETE, CANCEL, OK}

    public enum ColType {
        SLNO("SlNo."),
        NAME("Name"),
        DETAILS("Details");

        private final String typeName;

        ColType(String typeName) {
            this.typeName = typeName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static ColType getEnum(String text) {
            ColType retVal = null;
            if (text != null) {
                for (ColType b : ColType.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }
    public ItemType itemType;
    public String vrmlFile;
    JRadioButton rbFixedAccOn;
    double xMax, yMax, zMax;
    double xMin, yMin, zMin;
    public AxisAngle4d spinAxis; // absolute
    double spinPeriod; // in hours
    public String imageName = "";
    public boolean isLightSrc = false;
    JRadioButton rbFixedPos;
    Item thisItem;
    public double reportInterval = 0; // sec?  144000;
    double nextReport; // sec
    Vector<ForceElement> forceElements = new Vector<ForceElement>();
    Point3d centerOfMass = new Point3d(0, 0, 0);
    double[] mI = new double[3]; // about xx, yy and zz
    Vector3d oneByMI = new Vector3d();
    Vector3d jetForce = new Vector3d();
    Torque jetTorque = new Torque();

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

    public void setJetController(JetTimeController jetController) {
        this.jetController = jetController;
    }

    public void setMomentsOfInertia(double mIxx, double mIyy, double mIzz) throws Exception{
        if (mIxx > 0 && mIyy > 0 && mIzz > 0) {
            mI[Torque.AboutX] = mIxx;
            mI[Torque.AboutY] = mIyy;
            mI[Torque.AboutZ] = mIzz;
            oneByMI.set(1 / mIxx, 1 / mIyy, 1 / mIzz);
        }
        else
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
        }
        else
            return false;
    }

    public Jet addOneJet(Jet jet) {
        jetController.addOneJet(jet);
        return jet;
    }

    public void removeJet(Jet jet) {
        jetController.removeOneJet(jet);

    }

    public Jet addOneJet(String name, Vector3d force, Point3d actingPt) {
        Jet jet = new Jet(name, new ForceElement(force, actingPt, new Point3d(), null));
        jetController.addOneJet(jet);
        return jet;
    }

    public boolean addOneJetPlanStep(Jet jet, double startTime, double duration) {
        return jetController.addOneJetPlanStep(jet, startTime, duration);
    }

    public DataWithStatus<Jet> addOneJet(String jetName, Vector3d force, Point3d actingPoint, double startTime, double duration) {
        DataWithStatus<Jet> response = new DataWithStatus<Jet>();
        Jet j = addOneJet(name,force, actingPoint);
        if (addOneJetPlanStep(j, startTime, duration)) {
            response.setValue(j);
        }
        else {
            removeJet(j);
            response.setErrorMsg("Some error in Jet time definition");
        }
        return response;
    }

    public DataWithStatus<Boolean> addJetCouple(String baseName, Vector3d force, Point3d actingPoint,
                                                Vector3dMV.Axis aboutAxis, double startTime, double duration) {
        DataWithStatus<Boolean> response = new DataWithStatus<Boolean>();
        String j1Name = baseName + "1";
        Jet j1 = addOneJet(j1Name, force, actingPoint);
        if (addOneJetPlanStep(j1, startTime, duration)) {
            String j2Name = baseName + "2";
            Vector3d oppForce = new Vector3d(force);
            oppForce.negate();
            Point3dMV oppPoint = new Point3dMV(actingPoint);
            oppPoint.negateOneAxis(aboutAxis);
            Jet j2 = addOneJet(baseName + "2", oppForce, oppPoint);
            if (!addOneJetPlanStep(j2, startTime, duration))
                response.setErrorMsg("Some Error in timing of " + j2Name);
        }
        else
            response.setErrorMsg("Some Error in timing of " + j1Name);
        return response;
    }

//    public int addForceElements(Vector3d force, Point3d actingPoint) { // TODO-remove
//        ForceElement fe = new ForceElement(force, actingPoint, centerOfMass, null);
//        forceElements.add(fe);
//        return forceElements.size();
//    }
//
//    public void setFlightPlan(FlightPlan flightPlan) { // TODO-remove
//        this.flightPlan = flightPlan;
//        bFlightPlan = true;
//    }

    boolean anyFlightPlan() {
        bFlightPlan = (flightPlan != null) && (flightPlan.isValid());
        return bFlightPlan;
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
//                    theItem = new Item(theName, 10000, "C:\\Java Programs\\SpaceObjects\\VRML\\rocket.wrl", theParent);
                    theItem = new Item(theName, 10000, "VRML\\rocket.wrl", theParent);
                    break;
                case SPHERE:
                    theItem = new Item(theParent, theName);
                    break;
            }
        }
        return theItem;
    }

    static class  ItemBasic extends JDialog {
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
            ActionListener li = e-> {
                Object src = e.getSource();
                if (src == ok)
                    response = EditResponse.OK;
                closeThisWindow();};
            ok.addActionListener(li);
            cancel.addActionListener(li);
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }

        ItemType getSelectedType() {
            return (ItemType)jcItem.getItemAt(jcItem.getSelectedIndex());
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
                switch(nowType) {
                    case SURFACE:
                        theItem = new Surface(xmlStr, parent);
                        done = true;
                        break;
                    case VMRL:
//                        th
//                        ItemMovementsApp.showError("Item.338: getItemFromXML:Not Ready for VRML ");
//                        break;
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
        switch(colType) {
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
        switch(colType) {
            case NAME:
                return name;
            case DETAILS:
                return "Sphere with Mass:" + fmt.format(mass) +
                        ",    Dia:" + fmt.format(dia) +
                        ",    Pos:" + status.dataInCSV(ItemStat.Param.POSITION, 4) +
                        ((bFixedLocation) ?
                                " Static"  : ",    Vel:" + status.dataInCSV(ItemStat.Param.VELOCITY, 4)) +
                        ((status.angularAcceleration.isNonZero())?
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
        Item parent;
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
            tupRelPos.add(parent.status.pos);
            status.pos.set(tupRelPos);
            tupRelVel.set(relVelPan.getTuple3d());
            tupRelVel.add(parent.status.velocity);
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
        anyFlightPlan();
        return dlg.getResponse();
    }

    public void showItem(String title, InputControl inpC, Component c) {
        ItemDialog dlg = new ItemDialog(title, false, inpC, c);
        dlg.setVisible(true);
        anyFlightPlan();
    }

    public void editItemKeepingPosition(String title, InputControl inpC, Component c) {
        ItemDialog dlg = new ItemDialog(title, true, true, inpC, c);
        dlg.setVisible(true);
        anyFlightPlan();
    }

    public EditResponse editItem(InputControl inpC, Component c) {
        return editItem("", inpC, c);
    }

    class ItemDialog extends JDialog {
        JTextField tfItemName;
        JTextField tfVRMLflePath;
        JTextField tfImageFilePath;
        JButton colorButton = new JButton("Object Color");
        JLabel banner = new JLabel("");
        NumberTextField ntItemMass, ntItemDia;
        TuplePanel tpMI;
        NumberTextField ntElasticity;
        TuplePanel itemPosTuplePan;
        TuplePanel angularPosTuplePan;
        JRadioButton rbItemFixedPos;
        TuplePanel itemVelTuplePan;
        JButton itemRelButton = new JButton("Set Relative Data");
        boolean bFlightPlanCopy;
        FlightPlan flightPlanCopy;
        JButton jbManageJets = new JButton("Manage Jets");
        JButton delete = new JButton("DELETE");
        JButton ok = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        InputControl inpC;
        EditResponse response = EditResponse.CHANGED;
        boolean allowEdit = false;
        boolean freezePosition = false;
        JDialog thisDlg;
//        LocalActionsTable localActionTable;

        ItemDialog(String title, boolean allowEdit, boolean freezePosition, InputControl inpC, Component c) {
            setModal(true);
            setResizable(false);
            thisDlg = this;
            this.inpC = inpC;
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
            bFlightPlanCopy = bFlightPlan;
            if (bFlightPlanCopy)
                flightPlanCopy = flightPlan.clone();
            else
                flightPlanCopy = new FlightPlan(thisItem);
//            setFlightPlanButton();
            JPanel outerPan = new JPanel(new BorderLayout());
            MultiPairColPanel jp = new MultiPairColPanel("Data of Item");
            tfItemName = new JTextField(name, 10);
            jp.addItemPair("Object Name", tfItemName);
            if (itemType == ItemType.VMRL) {
                tfVRMLflePath = new JTextField(vrmlFile, 30);
                jp.addItemPair("VRML File Path", tfVRMLflePath);
            }
            else {
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
                tpMI = new TuplePanel(inpC, new Vector3d(mI), 6, 0, 1e20, "#,###.000", "Mass Moment of Inertia (kg.m2)");
                jp.addItemPair(tpMI.getTitle(), tpMI);
            }
            ntElasticity = new NumberTextField(inpC, eCompression, 6, false, -1, 1e20, "##0.####E00", "Elasticity N/100% ('-1' is sticky)");
            jp.addItemPair(ntElasticity);
            JPanel jpAngularPos = new JPanel(new BorderLayout());
            angularPosTuplePan = new TuplePanel(inpC, status.angularPos, 8, -10, +10, "##0.####", "Angular Orientation (on local axis) rad");
            jpAngularPos.add(angularPosTuplePan, BorderLayout.CENTER);
            jp.addItemPair("Angular Position in rad(local)", jpAngularPos);
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
                }
            });
            outerPan.add(jp, BorderLayout.CENTER);
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == jbManageJets) {
                        editJetList(thisDlg);
                    }
                    else if (src == ok) {
                        if (takeValuesFromUI())
                            closeThisWindow();
                    }
                    else if (src == delete) {
                        if (ItemMovementsApp.decide("Deleting Object ", "Do you want to DELETE this Object?")) {
                            response = EditResponse.DELETE;
                            closeThisWindow();
                        }
                    }
                    else {
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

        void getFlightPlan() {
            FlightPlanEditor flightPlanEditor = new FlightPlanEditor(space);
            flightPlanEditor.editPlan(inpC, flightPlanCopy);
        }

        void setFlightPlanButton() {
            jbManageJets.setText((flightPlanCopy.isValid()) ? "Edit Flight Plan" : "Add Flight Plan");
        }

        EditResponse getResponse() {
            return response;
        }

        boolean takeValuesFromUI() {
            boolean retVal = false;
            name = tfItemName.getText();
            if (name.length() > 1 && (!name.substring(0,2).equals("##"))) {
                try {
                    mass = ntItemMass.getData();
                    if (itemType == ItemType.SPHERE)
                        imageName = tfImageFilePath.getText().trim();
                    dia = ntItemDia.getData();
                    if (itemType == ItemType.VMRL) {
                        vrmlFile = tfVRMLflePath.getText().trim();
                        Vector3d  mIxyz = new Vector3d(tpMI.getTuple3d());
                        setMomentsOfInertia(mIxyz.x, mIxyz.y, mIxyz.z);
                    }
                    eCompression = ntElasticity.getData();
                    status.pos.set(itemPosTuplePan.getTuple3d());
                    status.angularPos.set(angularPosTuplePan.getTuple3d());
                    status.velocity.set(itemVelTuplePan.getTuple3d());
                    bFixedLocation = rbItemFixedPos.isSelected();
                    flightPlan = flightPlanCopy;
                    retVal = true;
                } catch (Exception e) {
                    showError("Some parameter is not acceptable");
                    retVal = false;
                }
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

    void noteInput() {
        calculateAreas();
        resetLimits();
        status.time = 0;
        nextReport = 0;
        initStartForce();
        if (bFlightPlan)
            flightPlan.initFlightPlan();
//        history.add(status);
    }

    void resetLimits() {
        xMax = Double.NEGATIVE_INFINITY;
        yMax = Double.NEGATIVE_INFINITY;
        zMax = Double.NEGATIVE_INFINITY;
        xMin = Double.POSITIVE_INFINITY;
        yMin = Double.POSITIVE_INFINITY;
        zMin = Double.POSITIVE_INFINITY;
    }

    public void evalForceFromBuiltInSource(double duration, double nowT) { // TODO
        if (bFlightPlan) {
            if (flightPlan.isActive()) // TODO this must be done only once in each cycle
                flightPlan.getForce(rocketForce, duration);
            else
                rocketForce.set(0, 0, 0);
        }
        // get Jet Force and torque
        jetForce.set(0, 0, 0);
        jetTorque.set(0, 0, 0);
        if (jetController != null) {
            jetController.upDateAllJetStatus(duration, nowT);
            for (Jet oneJet : jetController.jets) {
                jetForce.add(oneJet.getForce());
                jetTorque.add(oneJet.getTorque());
            }
        }
        Transform3D tr = new Transform3D();
        itemGraphic.get().getTotalTransform(tr);
        tr.transform(jetForce);
    }

    public void setLocalForces() {
        super.setLocalForces();
        netForce.add(rocketForce);
        netForce.add(jetForce);
    }

        @Override
    public void setStartConditions(double duration, double nowT) {
        lastTorque.set(jetTorque);
//        lastAngularVelocity.set(newAngularVelocity);
        lastAngularVelocity.set(status.angularVelocity);
        lastAngle.set(status.angularPos);
        super.setStartConditions(duration, nowT);
        evalForceFromBuiltInSource(duration, nowT);
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

    public void initAngularPosEtc(Vector3d angularPos, Vector3d angularVelocity) {
        status.intAngularPos(angularPos, angularVelocity);
    }

    public void setSpin(AxisAngle4d spinAxis, double spinPeriod) {
        this.spinAxis = spinAxis;
        this.spinPeriod = spinPeriod;
        if (spinPeriod > 0)
            radPerSec = Math.PI * 2 / spinPeriod;
    }

      // The new methods with ItemGraphic

    WeakReference<ItemGraphic> itemGraphic;

    public ItemGraphic createItemGraphic(Group grp, RenderingAttributes orbitAtrib) throws Exception {
        ItemGraphic itemG = new ItemGraphic(this);
        if (itemG.addObjectAndOrbit(grp, orbitAtrib)) {
            itemGraphic = new WeakReference<ItemGraphic>(itemG);
            itemGraphic.get().updateAngularPosition(status.angularPos);
        }
        else
            itemG = null;
        return itemG;
    }

    public void setItemDisplayAttribute(RenderingAttributes itemAttribute) {
        itemGraphic.get().setItemDisplayAttribute(itemAttribute);
    }

    public void attachPlatform(ViewingPlatform platform) {
        try {
            itemGraphic.get().attachPlatform(platform);
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

    public boolean updatePosAndVel(double deltaT, double nowT, boolean bFinal) throws Exception {

        updateAngularPosAndVelocity(deltaT, nowT, bFinal);
        super.updatePosAndVel(deltaT, nowT, bFinal);

//        if (super.updatePosAndVel(deltaT, nowT, bFinal)) {
            evalMaxMinPos();
            if (nowT > nextReport) {
//                updateAngularPosData();
                updateOrbitAndPos();
                nextReport += reportInterval;
            }

//        }
        return true;
    }

    void noteAngularStatus() {
        status.angularPos.set(newAngle);
        status.angularVelocity.set(newAngularVelocity);
        status.angularAcceleration.set(thisAngularAcc);
    }


    Vector3d lastTorque = new Torque();
    Vector3d lastAngularVelocity = new Vector3d();
    Vector3dMV effectiveTorque = new Vector3dMV();
    Vector3dMV thisAngularAcc = new Vector3dMV();
    Vector3dMV deltaAngularV = new Vector3dMV();
    Vector3dMV newAngularVelocity = new Vector3dMV();
    Vector3dMV averageAngularV = new Vector3dMV();
    Vector3dMV deltaAngle= new Vector3dMV();
    Vector3d lastAngle = new Vector3d();
    Vector3dMV newAngle = new Vector3dMV();

    boolean updateAngularPosAndVelocity(double deltaT, double nowT, boolean bFinal) {
        boolean changed = false;
        if (bFinal) {
            effectiveTorque.setMean(jetTorque, lastTorque);
            thisAngularAcc.scale(oneByMI, effectiveTorque);
            deltaAngularV.scale(deltaT, thisAngularAcc);
            newAngularVelocity.add(lastAngularVelocity, deltaAngularV);
            averageAngularV.setMean(lastAngularVelocity, newAngularVelocity);
            deltaAngle.scale(deltaT, averageAngularV);
            newAngle.add(lastAngle, deltaAngle);
            itemGraphic.get().updateAngularPosition(deltaAngle);
            noteAngularStatus();
            changed = true;
        }
        return changed;
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
        StringBuilder csvStr = new StringBuilder(name + "\n");
        csvStr.append("Position , " + status.positionStringForCSV(posFactor) + "\n");
        csvStr.append("Velocity , ").append(status.velocityStringForCSV(velFactor)).append("\n");
//        if (status.angularVelocity.isNonZero())
            csvStr.append("AngVel , ").append(status.angularVelocityStringForCSV(1)).append("\n");
        return csvStr;
    }

    protected StringBuilder defaultDataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("name", name));
        xmlStr.append(XMLmv.putTag("itemType", ("" + itemType)));
        return xmlStr;
    }

    public StringBuilder dataInXML() {
//        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("name", name));
        StringBuilder xmlStr = defaultDataInXML();
        if (itemType == ItemType.VMRL)
            xmlStr.append(XMLmv.putTag("vrmlFile", vrmlFile));
        xmlStr.append(XMLmv.putTag("mass", mass)).append(XMLmv.putTag("dia", dia));
        if (itemType == ItemType.VMRL)
            xmlStr.append(XMLmv.putTag("mI", Vector3dMV.dataInCSV(mI)));
        xmlStr.append(XMLmv.putTag("eCompression", eCompression));
        xmlStr.append(XMLmv.putTag("color", ("" + color.getRGB())));
        xmlStr.append(XMLmv.putTag("status", ("" + status.dataInXML())));
        xmlStr.append(XMLmv.putTag("bFixedLocation", bFixedLocation));
        xmlStr.append(XMLmv.putTag("nLocalActions", localActions.size()));
        int a = 0;
        for (LocalAction action: localActions) {
            xmlStr.append(XMLmv.putTag("a#" + ("" + a).trim(), action.dataInXML().toString()));
            a++;
        }
        if (jetController != null)
            xmlStr.append(XMLmv.putTag("jetController", jetController.dataInXML()));
//        xmlStr.append(XMLmv.putTag("bFlightPlan", bFlightPlan));
//        if (bFlightPlan)
//            xmlStr.append(XMLmv.putTag("flightPlan", flightPlan.dataInXML()));
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
        vp = XMLmv.getTag(xmlStr, "mass", 0);
        mass = Double.valueOf(vp.val);
        if (itemType == ItemType.VMRL) {
            vp = XMLmv.getTag(xmlStr, "mI", vp.endPos);
            setMomentsOfInertia(vp.val);
        }
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
