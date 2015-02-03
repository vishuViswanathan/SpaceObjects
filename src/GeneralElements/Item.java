package GeneralElements;

import Applications.ItemMovementsApp;
import GeneralElements.Display.ItemGraphic;
import GeneralElements.Display.LocalActionsTable;
import GeneralElements.Display.TuplePanel;
import GeneralElements.localActions.LocalAction;
import com.sun.j3d.utils.universe.ViewingPlatform;
import mvUtils.SmartFormatter;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.media.j3d.Group;
import javax.media.j3d.RenderingAttributes;
import javax.swing.*;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

/**
 * Created by M Viswanathan on 31 Mar 2014
 */
public class Item extends DarkMatter {
    static public enum EditResponse{CHANGED, NOTCHANGED, DELETE, CANCEL, OK}

    static public enum ColType {
        SLNO("SlNo."),
        NAME("Name"),
        MASS("Mass(kg)"),
        DIA("Dia(m)"),
        POS("x, y, z Position(m)"),
        STATICPOS("FixedPos"),
        VEL("x, y, z Velocity(m)");

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

    NumberTextField ntFixedAcc;
    JRadioButton rbFixedAccOn;
    double xMax, yMax, zMax;
    double xMin, yMin, zMin;
    public AxisAngle4d spinAxis; // absolute
    double spinPeriod; // in hours
    public String imageName;
    public boolean isLightSrc = false;

    JRadioButton rbFixedPos;

    public double reportInterval = 0; // sec?  144000;
    double nextReport; // sec

    public Item(Window parent) {
        super(parent);
//        localActions = new Vector<LocalAction>();
    }

    public Item(String name, double mass, double dia, Color color, Window parent) {
        this(parent);
        this.name = name;
        this.mass = mass;
        this.dia = dia;
        this.color = color;
        status = new ItemStat();
//        dirOfFixedGravityAcc = new Vector3dMV(0, -1, 0);
        setRadioButtons();
//        rbFixedPos = new JRadioButton("Fixed Position");
//        rbFixedAccOn = new JRadioButton("Directional Acceleration ON");
    }

    public Item(ItemSpace space, String name, double mass, double dia, Color color, Window parent) {
        this(parent);
        this.space = space;
        this.name = name;
        this.mass = mass;
        this.dia = dia;
        this.color = color;
        status = new ItemStat();
//        dirOfFixedGravityAcc = new Vector3dMV(0, -1, 0);
//        setRadioButtons();
//        rbFixedPos = new JRadioButton("Fixed Position");
//        rbFixedAccOn = new JRadioButton("Directional Acceleration ON");
    }

    public Item(String xmlStr, Window parent) {
        this(parent);
        setRadioButtons();
        takeFromXMl(xmlStr);
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
            case MASS:
                return 100;
            case DIA:
                return 100;
            case POS:
                return 200;
            case STATICPOS:
                return 30;
            case VEL:
                return 200;
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
            case MASS:
                return "" + fmt.format(mass);
            case NAME:
                return name;
            case DIA:
                return "" + fmt.format(dia);
            case POS:
                return status.dataInCSV(ItemStat.Param.POSITION, 4);
            case STATICPOS:
                return (bFixedLocation) ? "Y" : "N";
            case VEL:
                return  status.dataInCSV(ItemStat.Param.VELOCITY, 4);
//            case DIRACCON:
//                return (bFixedForceOn) ? "Y" : "N";
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

//    public void setbFixedForceOn(boolean set) {
//        bFixedForceOn = set;
//        rbFixedAccOn.setSelected(set);
//    }

    public void setRefreshInterval(double interval, double nextRefresh) {
        reportInterval = interval;
        nextReport = nextRefresh;
//        reportInterval = interval;
//        nextReport += reportInterval;
    }

    void checkFixedPos() {
        bFixedLocation = rbFixedPos.isSelected();
//        velTuplePan.setEnabled(!bFixedLocation);
    }

    RelativeDlg relDlg;

    class RelativeDlg extends JDialog {
        Item parent;
        Vector3d tupRelPos, tupRelVel;
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        TuplePanel relPosPan, relVelPan;
        InputControl inpC;
        JComboBox<Object> othersCB;

        RelativeDlg(InputControl inpC) {
            setModal(true);
            this.inpC = inpC;
            dbInit();
        }

        void dbInit() {
            tupRelPos = new Vector3d();
            tupRelVel = new Vector3d();
            MultiPairColPanel jp = new MultiPairColPanel("Relative Data of SpaceObject");
            othersCB = new JComboBox<Object>(space.getAllItems().toArray());
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
//            if (!bFixedLocation) {
                tupRelVel.set(relVelPan.getTuple3d());
                tupRelVel.add(parent.status.velocity);
                status.velocity.set(tupRelVel);
//            }
//            updateUI();
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

    DarkMatter getThisItem() {
        return this;
    }

    class ItemDialog extends JDialog {
        JTextField tfItemName;
        NumberTextField ntItemMass, ntItemDia;

        TuplePanel itemPosTuplePan;
        JRadioButton rbItemFixedPos;
        JRadioButton rbItemFixedAccOn;
        TuplePanel itemVelTuplePan;
        JButton itemRelButton = new JButton("Set Relative Data");
        Vector3d tupPos, tupVel;
        JButton delete = new JButton("DELETE");
        JButton ok = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        InputControl inpC;
        JComboBox<Object> othersCB;
        EditResponse response = EditResponse.CHANGED;
        LocalActionsTable localActionTable;

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
            MultiPairColPanel jp = new MultiPairColPanel("Data of Item");
            tfItemName = new JTextField(name, 10);
            jp.addItemPair("Object Name", tfItemName);
            ntItemMass = new NumberTextField(inpC, mass, 8, false, 1e-30, 1e40, "##0.#####E00", "Mass in kg");
            jp.addItemPair(ntItemMass);
            ntItemDia = new NumberTextField(inpC, dia, 6, false, 1e-20, 1e20, "##0.#####E00", "Dia in m");
            jp.addItemPair(ntItemDia);
            jp.addItemPair("", itemRelButton);

            JPanel jpPos = new JPanel(new BorderLayout());
            itemPosTuplePan = new TuplePanel(inpC, status.pos, 8, -1e30, 1e20, "##0.#####E00", "Position in m");
            rbItemFixedPos = new JRadioButton("Fixed Position", bFixedLocation);
//            rbItemFixedPos.setSelected(bFixedLocation);
            rbItemFixedPos.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    bFixedLocation = rbItemFixedPos.isSelected();
//                    checkFixedPos();
                }
            });
            jpPos.add(itemPosTuplePan, BorderLayout.CENTER);
            jp.addItemPair("Position in m", jpPos);
            jp.addItemPair("", rbItemFixedPos);
            JPanel jpVel = new JPanel(new BorderLayout());
            itemVelTuplePan = new TuplePanel(inpC, status.velocity, 8, -1e20, 1e20, "##0.#####E00", "Velocity im m/s");
            jpVel.add(itemVelTuplePan, BorderLayout.CENTER);
            jp.addItemPair("Velocity in m/s", jpVel);
            itemRelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    relDlg = new RelativeDlg(inpC);
                    relDlg.setLocationRelativeTo(itemRelButton);
                    relDlg.setVisible(true);
                    itemPosTuplePan.updateTuple(status.pos);
                    itemVelTuplePan.updateTuple(status.velocity );
                }
            });
            outerPan.add(jp, BorderLayout.CENTER);
            localActionTable = new LocalActionsTable(getThisItem(), inpC);
            outerPan.add(localActionTable.getLocalActionPanel(), BorderLayout.EAST);
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == ok) {
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
                        if (localActionTable.getEditResponse() == EditResponse.CHANGED) {
                            ItemMovementsApp.showMessage("Some Local Responses have changed and Saved");
                            response = EditResponse.CHANGED;
                        }
                        else
                            response = EditResponse.NOTCHANGED;
                        closeThisWindow();
                     }
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
//        JPanel jpFixedAcc = new JPanel(new BorderLayout());
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
                mass = ntItemMass.getData();
                dia = ntItemDia.getData();
                status.pos.set(itemPosTuplePan.getTuple3d());
                status.velocity.set(itemVelTuplePan.getTuple3d());
                bFixedLocation = rbItemFixedPos.isSelected();
//                bFixedForceOn = rbItemFixedAccOn.isSelected();
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

    void noteInput() {
        calculateAreas();
        resetLimits();
        status.time = 0;
        nextReport = 0;
        initStartForce();
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
        itemG.addObjectAndOrbit(grp, orbitAtrib);
//        itemGraphic = null;
        itemGraphic = new WeakReference<ItemGraphic>(itemG);
        return itemG;
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
        if (super.updatePosAndVel(deltaT, nowT, bFinal)) {
            evalMaxMinPos();
            if (nowT > nextReport) {
                updateOrbitAndPos();
                nextReport += reportInterval;
            }
        }
        return true;
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
        return csvStr;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("name", name));
        xmlStr.append(XMLmv.putTag("mass", mass)).append(XMLmv.putTag("dia", dia));
//        xmlStr.append(XMLmv.putTag("bFixedLocation", bFixedLocation)).append(XMLmv.putTag("bFixedForceOn", bFixedForceOn));
        xmlStr.append(XMLmv.putTag("color", ("" + color.getRGB())));
        xmlStr.append(XMLmv.putTag("status", ("" + status.dataInXML())));
        xmlStr.append(XMLmv.putTag("bFixedLocation", bFixedLocation));
//        if (bFixedForceOn) {
//            xmlStr.append(XMLmv.putTag("dirOfFixedGravityAcc", dirOfFixedGravityAcc.dataInCSV())).
//                    append(XMLmv.putTag("fixedAcc", fixedAcc));
//        }
        xmlStr.append(XMLmv.putTag("nLocalActions", localActions.size()));
        int a = 0;
        for (LocalAction action: localActions) {
            xmlStr.append(XMLmv.putTag("a#" + ("" + a).trim(), action.dataInXML().toString()));
            a++;
        }
        return xmlStr;
    }

    public boolean takeFromXMl(String xmlStr) throws NumberFormatException {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "name", 0);
        name = vp.val;
        vp = XMLmv.getTag(xmlStr, "mass", 0);
        mass = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "dia", 0);
        dia = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "bFixedLocation", 0);
        bFixedLocation = (vp.val.equals("1"));
//        vp = XMLmv.getTag(xmlStr, "bFixedForceOn", 0);
//        bFixedForceOn = (vp.val.equals("1"));
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
        return retVal;
    }
}
