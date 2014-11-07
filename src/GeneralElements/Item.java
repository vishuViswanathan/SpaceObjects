package GeneralElements;

import Applications.ItemMovementsApp;
import GeneralElements.Display.ItemGraphic;
import GeneralElements.Display.TuplePanel;
import GeneralElements.localActions.LocalAction;
import com.sun.j3d.utils.universe.ViewingPlatform;
import display.InputControl;
import display.MultiPairColPanel;
import display.NumberTextField;
import mvUtils.SmartFormatter;
import mvUtils.Vector3dMV;
import mvXML.ValAndPos;
import mvXML.XMLmv;
import sun.text.resources.CollationData_th;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

/**
 * Created by M Viswanathan on 31 Mar 2014
 */
public class Item extends DarkMatter {
    static public enum ColType {
        SLNO("SlNo."),
        NAME("Name"),
        MASS("Mass(kg)"),
        DIA("Dia(m)"),
        POS("x, y, z Position(m)"),
        STATICPOS("FixedPos"),
        VEL("x, y, z Velocity(m)"),
        DIRACCON("DirAccON");

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

    TuplePanel fixedAccVectPan;
    NumberTextField ntFixedAcc;
    JRadioButton rbFixedAccOn;
    double xMax, yMax, zMax;
    double xMin, yMin, zMin;
    public AxisAngle4d spinAxis; // absolute
    double spinPeriod; // in hours
    public String imageName;
    public boolean isLightSrc = false;

    JTextField tfName;
    NumberTextField ntMass, ntDia;

    TuplePanel posTuplePan;
    JRadioButton rbFixedPos;
    TuplePanel velTuplePan;
    JButton relButton = new JButton("Set Relative Data");

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
        dirOfFixedGravityAcc = new Vector3dMV(0, -1, 0);
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
            case DIRACCON:
                return 30;
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
            case DIRACCON:
                return (bFixedForceOn) ? "Y" : "N";
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

    public void setbFixedForceOn(boolean set) {
        bFixedForceOn = set;
        rbFixedAccOn.setSelected(set);
    }

    public void setRefreshInterval(double interval, double nextRefresh) {
        reportInterval = interval;
        nextReport = nextRefresh;
//        reportInterval = interval;
//        nextReport += reportInterval;
    }

    public JPanel dataPanelOLD(int objNum) {
        JPanel outerPan = new JPanel(new BorderLayout());
        MultiPairColPanel jp = new MultiPairColPanel("Data of Item " + objNum);
        tfName = new JTextField(name, 10);
        jp.addItemPair("Object Name", tfName);
        ntMass = new NumberTextField(this, mass, 8, false, 1e-30, 1e40, "##0.#####E00", "Mass in kg");
        jp.addItemPair(ntMass);
        ntDia = new NumberTextField(this, dia, 6, false, 1e-20, 1e20, "##0.#####E00", "Dia in m");
        jp.addItemPair(ntDia);
        relButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getRelativeData(relButton);
            }
        });
        jp.addItemPair("", relButton);

        JPanel jpPos = new JPanel(new BorderLayout());
        posTuplePan = new TuplePanel(this, status.pos, 8, -1e30, 1e20, "##0.#####E00", "Position in m");
        rbFixedPos.setSelected(bFixedLocation);
        rbFixedPos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkFixedPos();
            }
        });
        jpPos.add(posTuplePan, BorderLayout.CENTER);
        jp.addItemPair("Position in m", jpPos);
        jp.addItemPair("", rbFixedPos);
        JPanel jpVel = new JPanel(new BorderLayout());
        velTuplePan = new TuplePanel(this, status.velocity, 8, -1e20, 1e20, "##0.#####E00", "Velocity im m/s");
        jpVel.add(velTuplePan, BorderLayout.CENTER);
        jp.addItemPair("Velocity in m/s", jpVel);
        rbFixedAccOn.setSelected(bFixedForceOn);
        rbFixedAccOn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkFixedAcc();
            }
        });
        jp.addItemPair("", rbFixedAccOn);
        outerPan.add(jp, BorderLayout.WEST);
//        JPanel jpFixedAcc = new JPanel(new BorderLayout());
        fixedAccVectPan = new TuplePanel(this, dirOfFixedGravityAcc, 8, -100, 100, "##0.#####E00", "Direction of Acc Vector");
        ntFixedAcc = new NumberTextField(this, fixedAcc, 8, false, 0, 2000, "##0.#####E00", "Fixed Acc in m/s2");
        checkFixedAcc();
        jp.addItemPair(ntFixedAcc);
        jp.addItemPair("Direction of Acc", fixedAccVectPan);
        outerPan.add(jp, BorderLayout.SOUTH);
        return outerPan;
    }

    public JPanel dataPanel(int objNum) {
        JPanel outerPan = new JPanel(new BorderLayout());
        MultiPairColPanel jp = new MultiPairColPanel("Data of Item " + objNum);
        tfName = new JTextField(name, 10);
        jp.addItemPair("Object Name", tfName);
        ntMass = new NumberTextField(this, mass, 8, false, 1e-30, 1e40, "##0.#####E00", "Mass in kg");
        jp.addItemPair(ntMass);
        ntDia = new NumberTextField(this, dia, 6, false, 1e-20, 1e20, "##0.#####E00", "Dia in m");
        jp.addItemPair(ntDia);
        relButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getRelativeData(relButton);
            }
        });
        jp.addItemPair("", relButton);

        JPanel jpPos = new JPanel(new BorderLayout());
        posTuplePan = new TuplePanel(this, status.pos, 8, -1e30, 1e20, "##0.#####E00", "Position in m");
        rbFixedPos.setSelected(bFixedLocation);
        rbFixedPos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkFixedPos();
            }
        });
        jpPos.add(posTuplePan, BorderLayout.CENTER);
        jp.addItemPair("Position in m", jpPos);
        jp.addItemPair("", rbFixedPos);
        JPanel jpVel = new JPanel(new BorderLayout());
        velTuplePan = new TuplePanel(this, status.velocity, 8, -1e20, 1e20, "##0.#####E00", "Velocity im m/s");
        jpVel.add(velTuplePan, BorderLayout.CENTER);
        jp.addItemPair("Velocity in m/s", jpVel);
        rbFixedAccOn.setSelected(bFixedForceOn);
        rbFixedAccOn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkFixedAcc();
            }
        });
        jp.addItemPair("", rbFixedAccOn);
        outerPan.add(jp, BorderLayout.WEST);
//        JPanel jpFixedAcc = new JPanel(new BorderLayout());
        fixedAccVectPan = new TuplePanel(this, dirOfFixedGravityAcc, 8, -100, 100, "##0.#####E00", "Direction of Acc Vector");
        ntFixedAcc = new NumberTextField(this, fixedAcc, 8, false, 0, 2000, "##0.#####E00", "Fixed Acc in m/s2");
        checkFixedAcc();
        jp.addItemPair(ntFixedAcc);
        jp.addItemPair("Direction of Acc", fixedAccVectPan);
        outerPan.add(jp, BorderLayout.SOUTH);
        return outerPan;
    }


    void checkFixedPos() {
        bFixedLocation = rbFixedPos.isSelected();
        velTuplePan.setEnabled(!bFixedLocation);
    }

    void checkFixedAcc() {
        bFixedForceOn = rbFixedAccOn.isSelected();
        fixedAccVectPan.setEnabled(bFixedForceOn);
        ntFixedAcc.setEnabled(bFixedForceOn);
    }

    RelativeDlg relDlg;

    void getRelativeData(JComponent butt) {
//        space.noteItemData();
        relDlg = new RelativeDlg(this);
        relDlg.setLocationRelativeTo(butt);
        relDlg.setVisible(true);
    }

    void updateUI() {
        posTuplePan.updateTuple(status.pos);
        rbFixedPos.setSelected(bFixedLocation);
        velTuplePan.updateTuple(status.velocity);
        fixedAccVectPan.updateTuple(dirOfFixedGravityAcc);
        ntFixedAcc.setData(fixedAcc);
        rbFixedAccOn.setSelected(bFixedForceOn);
        checkFixedPos();
        checkFixedAcc();
    }

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
            if (!bFixedLocation) {
                tupRelVel.set(relVelPan.getTuple3d());
                tupRelVel.add(parent.status.velocity);
                status.velocity.set(tupRelVel);
            }
//            updateUI();
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }
    }

    public void editItem(InputControl inpC) {
        ItemDialog dlg = new ItemDialog(inpC);
        dlg.setVisible(true);
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
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        TuplePanel relPosPan, relVelPan;
        InputControl inpC;
        JComboBox<Object> othersCB;

        ItemDialog(InputControl inpC) {
            setModal(true);
            this.inpC = inpC;
            dbInit();
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
            itemRelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    relDlg = new RelativeDlg(inpC);
                    relDlg.setLocationRelativeTo(itemRelButton);
                    relDlg.setVisible(true);
                }
            });
            jp.addItemPair("", itemRelButton);

            JPanel jpPos = new JPanel(new BorderLayout());
            itemPosTuplePan = new TuplePanel(inpC, status.pos, 8, -1e30, 1e20, "##0.#####E00", "Position in m");
            rbItemFixedPos = new JRadioButton("Fixed Position");
            rbItemFixedPos.setSelected(bFixedLocation);
            rbItemFixedPos.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkFixedPos();
                }
            });
            jpPos.add(itemPosTuplePan, BorderLayout.CENTER);
            jp.addItemPair("Position in m", jpPos);
            jp.addItemPair("", rbItemFixedPos);
            JPanel jpVel = new JPanel(new BorderLayout());
            itemVelTuplePan = new TuplePanel(inpC, status.velocity, 8, -1e20, 1e20, "##0.#####E00", "Velocity im m/s");
            jpVel.add(itemVelTuplePan, BorderLayout.CENTER);
            jp.addItemPair("Velocity in m/s", jpVel);
            rbItemFixedAccOn = new JRadioButton("Directional Acceleration ON");
            rbItemFixedAccOn.setSelected(bFixedForceOn);
            rbItemFixedAccOn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkFixedAcc();
                }
            });
            jp.addItemPair("", rbItemFixedAccOn);
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == ok) {
                        if (takeValuesFromUI())
                            closeThisWindow();
                    }
                }
            };
            ok.addActionListener(li);
            cancel.addActionListener(li);
            jp.addItemPair(cancel, ok);
            outerPan.add(jp, BorderLayout.WEST);
//        JPanel jpFixedAcc = new JPanel(new BorderLayout());
            add(outerPan);
            pack();
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
                bFixedForceOn = rbItemFixedAccOn.isSelected();
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
        name = tfName.getText();
        dia = ntDia.getData();
        mass = ntMass.getData();
        bFixedLocation = rbFixedPos.isSelected();
        Tuple3d posTuple = posTuplePan.getTuple3d();
        if (posTuple != null)
            status.pos.set(posTuple);
        Tuple3d velTuple = velTuplePan.getTuple3d();
        if (velTuple != null)
            status.velocity.set(velTuple);
        bFixedForceOn = rbFixedAccOn.isSelected();
        if (bFixedForceOn) {
            Tuple3d accTuple = fixedAccVectPan.getTuple3d();
            if (accTuple != null)
                dirOfFixedGravityAcc.set(accTuple);
            double vecLen = dirOfFixedGravityAcc.length();
            if (vecLen > 0) {
                fixedAcc = ntFixedAcc.getData();
                dirOfFixedGravityAcc.scale(1 / vecLen);
                forceOfFixedGravity = new Vector3d(dirOfFixedGravityAcc);
                forceOfFixedGravity.scale(mass * fixedAcc);
            } else {
                bFixedForceOn = false;
                ItemMovementsApp.log.error("Acc Vector Length is < 0 [" + vecLen);
            }
        }
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
        xmlStr.append(XMLmv.putTag("bFixedLocation", bFixedLocation)).append(XMLmv.putTag("bFixedForceOn", bFixedForceOn));
        xmlStr.append(XMLmv.putTag("color", ("" + color.getRGB())));
        xmlStr.append(XMLmv.putTag("status", ("" + status.dataInXML())));
        if (bFixedForceOn) {
            xmlStr.append(XMLmv.putTag("dirOfFixedGravityAcc", dirOfFixedGravityAcc.dataInCSV())).
                    append(XMLmv.putTag("fixedAcc", fixedAcc));
        }
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
        vp = XMLmv.getTag(xmlStr, "bFixedForceOn", 0);
        bFixedForceOn = (vp.val.equals("1"));
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
        if (dirOfFixedGravityAcc == null)
            dirOfFixedGravityAcc = new Vector3dMV(0, 0, 0);
        if (bFixedForceOn) {
            vp = XMLmv.getTag(xmlStr, "dirOfFixedGravityAcc", 0);
            try {
                if (dirOfFixedGravityAcc == null)
                    dirOfFixedGravityAcc = new Vector3dMV(vp.val);
                else
                    dirOfFixedGravityAcc.set(vp.val);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("dirOfFixedGravityAcc data of " + name + " :" + e.getMessage());
            }
            vp = XMLmv.getTag(xmlStr, "fixedAcc", 0);
            fixedAcc = Double.valueOf(vp.val);
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

    class VectorDataEntry extends JTextField{
        ItemStat.Param param;
        InputControl inpC;
        VectorDataEntry(InputControl inpC, ItemStat.Param param) {
            this.inpC = inpC;
            this.param = param;
            fillLabel();
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getValue();
                }
            });

        }

        void getValue() {
            VectorDialog dlg = new VectorDialog(inpC, param);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        }

        void fillLabel() {
            setText(status.dataInCSV(param));
        }
    }

    class VectorDialog extends JDialog {
        Item parent;
        ItemStat.Param param;
        Vector3d data;
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        TuplePanel dataPan;
        InputControl inpC;
        JComboBox<Object> othersCB;
        boolean bRelative = false;

        VectorDialog(InputControl inpC, ItemStat.Param param) {
            setModal(true);
            this.inpC = inpC;
            this.param = param;
            dbInit();
        }

        void dbInit() {
            data = new Vector3d(status.getOneParam(param));
            MultiPairColPanel jp = new MultiPairColPanel("" + param + " of Object");
            othersCB = new JComboBox<Object>(space.getAllItems().toArray());
            othersCB.setEditable(bRelative);
            final JRadioButton relChoice = new JRadioButton("Set Relative Values");
            relChoice.addActionListener( new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    bRelative = relChoice.isSelected();
                    othersCB.setEditable(bRelative);
                }
            });
            jp.addItemPair(new JLabel("Relative to "), othersCB);
            dataPan = new TuplePanel(inpC, data, 8, -1e20, 1e20, "##0.#####E00", "x, y, z Values in m");
            jp.addItemPair("x, y, z Values in m", dataPan);
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
            if (bRelative) {
                parent = space.getAllItems().get(othersCB.getSelectedIndex());
                Tuple3d parentValue = parent.getStatus().getOneParam(param);
                data.set(dataPan.getTuple3d());
                data.add(parentValue);
                status.setParam(data, param);
            }
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }
    }
}
