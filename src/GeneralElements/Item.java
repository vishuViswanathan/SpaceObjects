package GeneralElements;

import Applications.ItemMovementsApp;
import GeneralElements.Display.ItemGraphic;
import GeneralElements.Display.TuplePanel;
import GeneralElements.localActions.LocalAction;
import com.sun.j3d.utils.universe.ViewingPlatform;
import display.InputControl;
import display.MultiPairColPanel;
import display.NumberTextField;
import mvUtils.Vector3dMV;
import mvXML.ValAndPos;
import mvXML.XMLmv;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

/**
 * Created by M Viswanathan on 31 Mar 2014
 */
public class Item extends DarkMatter {
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

    public Item(String xmlStr, Window parent) {
        this(parent);
        setRadioButtons();
        takeFromXMl(xmlStr);
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
        space.noteInput();
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
            updateUI();
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

 /*   public void setLocalForces() {
        super.setLocalForces();
//        if (bFixedForceOn)
//            force.set(forceOfFixedGravity);
//        else
//            force.set(0, 0, 0);
        for (LocalAction action : localActions)
            force.add(action.getForce());
    }
*/
    public void addToForce(Vector3d addForce) {
        force.add(addForce);
    }

    // dummy not used
    @Override
    public void evalOnce() {
    }

    @Override
    public void evalOnce(double deltaT, double nowT) {
        try {
            updatePosAndVel(deltaT, nowT, true);
        } catch (Exception e) {
            ItemMovementsApp.log.error("In ITem evalOnce for " + name + ":" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updatePosAndVel(double deltaT, double nowT, boolean bFinal) throws Exception {  // deltaT is time is seconds
        if (!bFixedLocation) {
            effectiveForce.set(force);
            effectiveForce.add(lastForce);
            effectiveForce.scale(0.5); // the average force
            Vector3d thisAcc = new Vector3d(effectiveForce);
            thisAcc.scale((1.0 / mass));
            // calculate from force
            Vector3d deltaV = new Vector3d(effectiveForce);
            deltaV.scale(deltaT);
            deltaV.scale(1.0 / mass);
            Vector3d averageV = new Vector3d(deltaV);
            averageV.scaleAdd(+0.5, lastVelocity); //
            Point3d newPos = new Point3d(averageV);
            newPos.scale(deltaT);
            newPos.add(lastPosition);
            status.pos.set(newPos); // only position is updated here
            Vector3d newVelocity = new Vector3d(lastVelocity);
            newVelocity.add(deltaV);
            status.velocity.set(newVelocity);
            if (bFinal) {
                status.acc.set(thisAcc);
                status.time = nowT;
                lastForce.set(force);  // note down the force for the last calculation
                evalMaxMinPos();
                if (nowT > nextReport) {
                    updateOrbitAndPos();
                    nextReport += reportInterval;
                }
            }
        }
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
}
