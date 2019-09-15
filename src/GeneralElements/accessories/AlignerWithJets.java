package GeneralElements.accessories;

import Applications.ItemMovementsApp;
import GeneralElements.Display.TuplePanel;
import GeneralElements.Item;
import GeneralElements.ItemInterface;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;
import mvUtils.physics.Vector3dMV;
import time.timePlan.OneJetPlan;
import time.timePlan.OneTimeStep;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by mviswanathan on 14-05-2017.
 */
public class AlignerWithJets extends JetsAndSeekers {
    public enum Direction {
        POSZ("Positive Z"),
        NEGZ("Negative Z"),
        POSX("Positive X"),
        NEGX("Negative X"),
        POSY("Positive Y"),
        NEGY("Negative Y");

        private final String typeName;

        Direction(String actionName) {
            this.typeName = actionName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static Direction getEnum(String text) {
            Direction retVal = null;
            if (text != null) {
                for (Direction b : Direction.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

    enum AxisStep {XFOR, XREV, YFOR, YREV, ZFOR, ZREV, FINISH, DONE}
    double thisActionTime; // duration of the current action
    Direction direction = Direction.POSZ;
    Vector3dMV vDirection;
    Vector3dMV nowTorque = new Vector3dMV();
    double activeT; // active since
    AxisStep nowAxisStep = AxisStep.ZFOR;
    AlignTo alignBaseZ;
    AlignTo alignBaseX;
    AlignTo alignBaseY;

    AlignerJetSet alignerJetSetX;
    AlignerJetSet alignerJetSetY;
    AlignerJetSet alignerJetSetZ;
    Queue<AxisStep> axisQueue;

    boolean initiated = false;

    public AlignerWithJets(ItemInterface item) {
        super(item, "UNKNOWN",  ElementType.ALIGNERWITHJETS);
        vDirection = new Vector3dMV(0, 0, 1);
        alignerJetSetX = new AlignerJetSet(this, AboutAxis.X);
        alignerJetSetY = new AlignerJetSet(this, AboutAxis.Y);
        alignerJetSetZ = new AlignerJetSet(this, AboutAxis.Z);
        axisQueue = new LinkedList<>();
    }

    void prepareAxisSequence() {
        axisQueue.clear();
        axisQueue.add(AxisStep.ZFOR);
        axisQueue.add(AxisStep.XFOR);
        axisQueue.add(AxisStep.YFOR);
        if (!justTurn) {
            axisQueue.add(AxisStep.ZFOR);
            axisQueue.add(AxisStep.XFOR);
            axisQueue.add(AxisStep.YFOR);
        }
        thisActionTime = 0;
    }

    public AlignerWithJets(ItemInterface item, String xmlStr) {
        this(item);
        takeFromXML(xmlStr);
    }

    private void prepareAlignToList() throws Exception{
        if (!initiated) {
            Vector3d mi = item.getMiAsVector();
            alignBaseZ = new AlignTo(mi.z, alignerJetSetZ, AboutAxis.Z);
            alignBaseX = new AlignTo(mi.x, alignerJetSetX, AboutAxis.X);
            alignBaseY = new AlignTo(mi.y, alignerJetSetY, AboutAxis.Y);
            initiated = true;
        }
    }

    public boolean completeThisStep(OneTimeStep theStep, double nowT, double deltaT) {
        boolean retVal = false;
        if (nowT >= theStep.startTime) {
            try {
                ActivateIt(theStep, deltaT);
            } catch (Exception e) {
                showError(".120:" + e.getMessage());
                nowAxisStep = AxisStep.DONE;
            }
        }
        if (nowAxisStep == AxisStep.DONE) {
            resetStatus();
            retVal = true;
        }
        return retVal;
    }

    void resetStatus() {
        this.theStep = null;
        basicsSet = false;
        prepareAxisSequence();
        nowAxisStep = axisQueue.remove();
    }

    public void ActivateIt(OneTimeStep theStep, double deltaT) throws Exception{
        if (nowAxisStep != AxisStep.DONE) {
            if (!theStep.isON) { // a new step
                this.theStep = theStep;
                theStep.markItOn(true);
                prepareAlignToList();
                basicsSet = prepareBasicsForTorques();
                active = true;
            }
            updateTorque(deltaT);
        }
     }

    Vector3dMV globalAlignTo;
    Vector3dMV turnByAngle;
    boolean justTurn = false;
    boolean basicsSet = false;

    private boolean prepareBasicsForTorques() {
        justTurn = false;
        switch (theStep.stepAction) {
            case ALIGNTOANOBJECT:
                globalAlignTo = objectDirection(theStep.alignToObject);
                break;
            case ALIGNTOVELOCITY:
                globalAlignTo = new Vector3dMV(item.getVelocity(theStep.alignToObject));
                break;
            case ALIGNCOUNTERTOVELOCITY:
                globalAlignTo = new Vector3dMV(item.getVelocity(theStep.alignToObject));
                globalAlignTo.negate();
                break;
            case TURNBYANGLE:
                turnByAngle = theStep.turnByAngle;
                justTurn = true;
                break;
        }
        prepareAxisSequence();
        nowAxisStep = axisQueue.remove();
        activeT = 0;
        return true;
    }

    Vector3dMV objectDirection(ItemInterface object) {
        Vector3dMV retVal = new Vector3dMV(object.getStatus().pos);
        retVal.sub(item.getPos());
        return retVal;
    }

    class AlignTo  {
        boolean rotate = true;
        AboutAxis axis;
        double mI;
        AlignerJetSet alignerJetSet;
        double angleAlignAxis;
        double torqueMagnitude;
        double torque;
        double alpha; // angular acceleration

        AlignTo (double mI, AlignerJetSet alignerJetSet, AboutAxis axis) throws Exception {
            this.mI = mI;
            this.alignerJetSet = alignerJetSet;
            this.axis = axis;
            torqueMagnitude = alignerJetSet.getTorque().length();
            alpha = torqueMagnitude / mI;
            if (alpha == 0)
                throw new Exception("Torque capability is 0");
            switch(axis) {
                case X:
                    if (vDirection.z == 0 && vDirection.y == 0)
                        rotate = false;
                    else
                        angleAlignAxis = Math.atan2(vDirection.z, vDirection.y);
                    break;
                case Y:
                    if (vDirection.x == 0 && vDirection.z == 0)
                        rotate = false;
                    else
                        angleAlignAxis = Math.atan2(vDirection.x, vDirection.z);
                    break;
                case Z:
                    if (vDirection.y == 0 && vDirection.x == 0)
                        rotate = false;
                    else
                        angleAlignAxis = Math.atan2(vDirection.y, vDirection.x);
                    break;
            }
        }

        double actionTime(double theta) {
            if (theta == 0) {
                torque = 0;
                return 0;
            }
            else {
                double tBy2Square = Math.abs(theta / alpha);
                if (theta < 0)
                    torque = -torqueMagnitude;
                else
                    torque = torqueMagnitude;
                return Math.sqrt(tBy2Square);
            }
        }

        void setTorque() {
            if (rotate) {
                if (justTurn) {
                    double theta = 0;
                    switch(axis) {
                        case X:
                            theta = turnByAngle.getX();
                            break;
                        case Y:
                            theta = turnByAngle.getY();
                            break;
                        case Z:
                            theta = turnByAngle.getZ();
                            break;
                    }
                    thisActionTime = actionTime(theta);
                }
                else {
                    double theta;
                    Vector3dMV alignTo = new Vector3dMV(globalAlignTo);
                    item.globalToItem().transform(alignTo);
                    double angleV = 0;
                    boolean tryLater = false;
                    switch (axis) {
                        case X:
                            tryLater = alignTo.z == 0 && alignTo.y == 0;
                            if (tryLater)
                                axisQueue.add(AxisStep.XFOR);
                            else
                                angleV = Math.atan2(alignTo.z, alignTo.y);
                            break;
                        case Y:
                            tryLater = alignTo.x == 0 && alignTo.z == 0;
                            if (tryLater)
                                axisQueue.add(AxisStep.YFOR);
                            else
                                angleV = Math.atan2(alignTo.x, alignTo.z);
                            break;
                        case Z:
                            tryLater = alignTo.y == 0 && alignTo.x == 0;
                            if (tryLater)
                                axisQueue.add(AxisStep.ZFOR);
                            else
                                angleV = Math.atan2(alignTo.y, alignTo.x);
                            break;
                    }
                    if (tryLater) {
                        ItemMovementsApp.debug("AlignerWithJets, tryLater is on");
                        thisActionTime = 0;
                        torque = 0;
                    } else {
                        theta = angleV - angleAlignAxis;
                        if (theta < -Math.PI)
                            theta += (2 * Math.PI);
                        else if (theta > Math.PI)
                            theta -= (2 * Math.PI);
                        thisActionTime = actionTime(theta);
                    }
                }
            }
            else {
                torque = 0;
                thisActionTime = 0;
            }
        }

        void setTorqueOLD() {
            if (rotate) {
                Vector3dMV alignTo = new Vector3dMV(globalAlignTo);
                item.globalToItem().transform(alignTo);
                double angleV = 0;
                double theta;
                boolean tryLater = false;
                switch (axis) {
                    case X:
                        tryLater = alignTo.z == 0 && alignTo.y == 0;
                        if (tryLater)
                            axisQueue.add(AxisStep.XFOR);
                        else
                            angleV = Math.atan2(alignTo.z, alignTo.y);
                        break;
                    case Y:
                        tryLater = alignTo.x == 0 && alignTo.z == 0;
                        if (tryLater)
                            axisQueue.add(AxisStep.YFOR);
                        else
                            angleV = Math.atan2(alignTo.x, alignTo.z);
                        break;
                    case Z:
                        tryLater = alignTo.y == 0 && alignTo.x == 0;
                        if (tryLater)
                            axisQueue.add(AxisStep.ZFOR);
                        else
                            angleV = Math.atan2(alignTo.y, alignTo.x);
                        break;
                }
                if (tryLater) {
                    ItemMovementsApp.debug("AlignerWithJets, tryLater is on");
                    thisActionTime = 0;
                    torque = 0;
                 }
                else {
                    theta = angleV - angleAlignAxis;
                    if (theta < -Math.PI)
                        theta += (2 * Math.PI);
                    else if (theta > Math.PI)
                        theta -= (2 * Math.PI);
                    thisActionTime = actionTime(theta);
                }
            }
            else {
                torque = 0;
                thisActionTime = 0;
            }
        }
    }

    private void updateTorque(double duration) {
        switch (nowAxisStep) {
            case XFOR:
                if (activeT > thisActionTime) {
                    alignBaseX.setTorque();
                    activeT = 0;
                    nowTorque.set(alignBaseX.torque, 0, 0);
                    nowAxisStep = AxisStep.XREV;
                }
                break;
            case XREV:
                if (activeT > thisActionTime) {
                    activeT = 0;
                    nowTorque.set(-alignBaseX.torque, 0, 0);
                    nowAxisStep = axisQueue.poll();
                    if (nowAxisStep == null)
                        nowAxisStep = AxisStep.FINISH;
                }
                break;
            case YFOR:
                if (activeT > thisActionTime) {
                    alignBaseY.setTorque();
                    activeT = 0;
                    nowTorque.set(0, alignBaseY.torque, 0);
                    nowAxisStep = AxisStep.YREV;
                }
                break;
            case YREV:
                if (activeT > thisActionTime) {
                    activeT = 0;
                    nowTorque.set(0, -alignBaseY.torque, 0);
                    nowAxisStep = axisQueue.poll();
                    if (nowAxisStep == null)
                        nowAxisStep = AxisStep.FINISH;
                }
                break;
            case ZFOR:
                if (activeT > thisActionTime) {
                    alignBaseZ.setTorque();
                    activeT = 0;
                    nowTorque.set(0, 0, alignBaseZ.torque);
                    nowAxisStep = AxisStep.ZREV;
                }
                break;
            case ZREV:
                if (activeT > thisActionTime) {
                    activeT = 0;
                    nowTorque.set(0, 0, -alignBaseZ.torque);
                    nowAxisStep = axisQueue.poll();
                    if (nowAxisStep == null)
                        nowAxisStep = AxisStep.FINISH;
                }
                break;
            case FINISH:
                if (activeT >= thisActionTime) {
                    nowTorque.set(0, 0, 0);
                    nowAxisStep = AxisStep.DONE;
                    theStep.markItOn(false);
                }
                break;
        }
        activeT += duration;
    }

    public void addEffect() {
        if (active)
            item.addToJetTorque(nowTorque);
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = super.dataInXML();
        xmlStr.append(XMLmv.putTag("name", name));
        xmlStr.append(XMLmv.putTag("vDirection", vDirection.dataInCSV()));
        xmlStr.append(XMLmv.putTag("alignerJetSetX", alignerJetSetX.dataInXML()));
        xmlStr.append(XMLmv.putTag("alignerJetSetY", alignerJetSetY.dataInXML()));
        xmlStr.append(XMLmv.putTag("alignerJetSetZ", alignerJetSetZ.dataInXML()));
        xmlStr.append(XMLmv.putTag("thePlan", thePlan.dataInXML()));
        return xmlStr;
    }

    private boolean takeFromXML(String xmlStr) {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "name", 0);
        name = vp.val.trim();
        vp = XMLmv.getTag(xmlStr, "direction", vp.endPos);
        if (vp.val.length() > 0) {
            direction = Direction.getEnum(vp.val);
            switch(direction) {
                case POSX:
                    vDirection.set(1, 0, 0);
                    break;
                case POSY:
                    vDirection.set(0, 1, 0);
                    break;
                case POSZ:
                    vDirection.set(0, 0, 1);
                    break;
                default:
                    showError("Unknown Direction: " + direction);
                    break;
            }
        }
        vp = XMLmv.getTag(xmlStr, "vDirection", vp.endPos);
        if (vp.val.length() > 0)
            vDirection.set(vp.val);
        int nowPos = vp.endPos;
        vp = XMLmv.getTag(xmlStr, "alignerJetSetX", vp.endPos);
        if (vp.val.length() > 0) {
            alignerJetSetX = new AlignerJetSet(this, vp.val);
            vp = XMLmv.getTag(xmlStr, "alignerJetSetY", vp.endPos);
            alignerJetSetY = new AlignerJetSet(this, vp.val);
            vp = XMLmv.getTag(xmlStr, "alignerJetSetZ", vp.endPos);
            alignerJetSetZ = new AlignerJetSet(this, vp.val);
        }
        else
            vp.endPos = nowPos;

        try {
            vp = XMLmv.getTag(xmlStr, "thePlan", vp.endPos);
            thePlan = new OneJetPlan(this, vp.val);
            active = false;
        } catch (Exception e) {
            showError(".153 takeFromXML: Some mess in Time Plan");
            retVal = false;
        }
        return retVal;
    }

    public OneTimeStep.StepAction[] actions() {
        return new OneTimeStep.StepAction[] {OneTimeStep.StepAction.ALIGNTOVELOCITY,
                OneTimeStep.StepAction.ALIGNCOUNTERTOVELOCITY,
                OneTimeStep.StepAction.ALIGNTOANOBJECT, OneTimeStep.StepAction.TURNBYANGLE};
    }

    public Item.EditResponse editData(InputControl inpC, Component c) {
        AlignerDetails dlg = new AlignerDetails(inpC, c);
        if (c == null)
            dlg.setLocation(100, 100);
        else
            dlg.setLocationRelativeTo(c);
        dlg.setVisible(true);
        return dlg.getResponse();
    }

    JButton pbAlignerJetX = new JButton("X-AlignerJetSet");
    JButton pbAlignerJetY = new JButton("Y-AlignerJetSet");
    JButton pbAlignerJetZ = new JButton("Z-AlignerJetSet");

    class AlignerDetails extends JDialog {
        Component caller;
        InputControl inpC;
        Item.EditResponse response;
        TextField tName;
        TuplePanel directionP;

//        JComboBox<Direction> cbDirection;
        JButton jbTimePlan;
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JButton delete = new JButton("Delete");
        JDialog thisDlg;

        AlignerDetails(InputControl inpC, Component caller) {
            setModal(true);
            this.caller = caller;
            setResizable(false);
            this.inpC = inpC;
            thisDlg = this;
            response = Item.EditResponse.NOTCHANGED;
            dbInit();
        }

        void dbInit() {
            initiated = false; // of the main class
            tName = new TextField(name, 30);
            directionP = new TuplePanel(inpC, vDirection, 6, -100, +100, "###.###", "Axis Direction");
            JPanel outerP = new JPanel(new BorderLayout());
            MultiPairColPanel jpBasic = new MultiPairColPanel("Aligner Details");
            jpBasic.addItemPair("ID", tName);
            jpBasic.addItemPair("Aligner Direction Vector", directionP);
            jpBasic.addItem("Direction is in Item's local coordinates");

            ActionOnAlignerJet l = new ActionOnAlignerJet();
            pbAlignerJetX.addActionListener(l);
            pbAlignerJetY.addActionListener(l);
            pbAlignerJetZ.addActionListener(l);

            jpBasic.addItem(pbAlignerJetX);
            jpBasic.addItem(pbAlignerJetY);
            jpBasic.addItem(pbAlignerJetZ);

            jbTimePlan = new JButton("Edit Time Plan");
            jbTimePlan.addActionListener(e ->
            {
                Item.EditResponse response = thePlan.editPlan(inpC, thisDlg);
                System.out.println("response from TimePlan is " + response);
            });
            JPanel p = new JPanel();
            p.add(jbTimePlan);
            jpBasic.addItem(p);
            jpBasic.addBlank();
            JPanel buttPanel = new JPanel(new BorderLayout());
            buttPanel.add(delete, BorderLayout.WEST);
            buttPanel.add(cancel, BorderLayout.CENTER);
            buttPanel.add(ok, BorderLayout.EAST);
            jpBasic.addItem(buttPanel);
            outerP.add(jpBasic);
            ActionListener li =  (e ->{
                    Object src = e.getSource();
                    if (src == ok) {
                        if (takeDataFromUI()) {
                            response = Item.EditResponse.CHANGED;
                            closeThisWindow();
                        }
                    } else if (src == delete) {
                        if (ItemMovementsApp.decide("Deleting Aligner ", "Do you want to DELETE this Aligner?" +
                                "DELETE to be checked", caller)) {
                            response = Item.EditResponse.DELETE;
                            closeThisWindow();
                        }
                    } else {
                        response = Item.EditResponse.NOTCHANGED;
                        closeThisWindow();
                    }
                });
            ok.addActionListener(li);
            cancel.addActionListener(li);
            delete.addActionListener(li);
            add(outerP);
            pack();
        }

        class ActionOnAlignerJet implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object src = e.getSource();
                if (src == pbAlignerJetX) {
                    alignerJetSetX.editData(inpC, pbAlignerJetX);
                }
                if (src == pbAlignerJetY) {
                    alignerJetSetY.editData(inpC, pbAlignerJetY);
                }
                if (src == pbAlignerJetZ) {
                    alignerJetSetZ.editData(inpC, pbAlignerJetZ);
                }
            }
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }

        boolean takeDataFromUI() {
            name = tName.getText();
            vDirection.set(directionP.getTuple3d());
            if (vDirection.lengthSquared() > 0)
                vDirection.normalize();
            return true;
        }

        Item.EditResponse getResponse() {
            return response;
        }
    }

    void showError(String msg) {
        ItemMovementsApp.showError("AlignerWithJets: " + msg);
    }

}
