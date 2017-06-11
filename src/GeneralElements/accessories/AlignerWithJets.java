package GeneralElements.accessories;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;
import mvUtils.physics.Vector3dMV;
import time.timePlan.OneJetPlan;
import time.timePlan.OneTimeStep;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
 * Created by mviswanathan on 14-05-2017.
 */
public class AlignerWithJets extends JetsAndSeekers {
    public enum Direction {
        POSX("Positive X"),
        NEGX("Negative X"),
        POSY("Positive Y"),
        NEGY("Negative Y"),
        POSZ("Positive Z"),
        NEGZ("Negative Z");

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

    enum ActionPos {NOTSTARTED, FIRST, SECOND, THIRD, FOURTH, DONE}
    double thisActionTime; // duration of the current action
    double actionEndTime; // updated for each action pos
    Direction direction = Direction.POSZ;
    Vector3dMV nowTorque = new Vector3dMV();
    double activeT; // active since
    double halfTime; // when acceleration/ deceleration switch is to take place
    double quarter1Time;
    double quarter3Time;
    boolean opposite = false; // to take care NEGX, NEGY and NEGZ
    ActionPos nowActionPos = ActionPos.NOTSTARTED;

    AlignTo alignBaseZ;
    AlignTo alignBaseX;
    AlignTo alignBaseY;
    AlignTo myAlignBase;

    AlignerJetSet alignerJetSetX;
    AlignerJetSet alignerJetSetY;
    AlignerJetSet alignerJetSetZ;

    boolean initiated = false;

    public AlignerWithJets(Item item) {
        super(item, "UNKNOWN",  ElementType.ALIGNERWITHJETS);
        alignerJetSetX = new AlignerJetSet(this, AboutAxis.X);
        alignerJetSetY = new AlignerJetSet(this, AboutAxis.Y);
        alignerJetSetZ = new AlignerJetSet(this, AboutAxis.Z);
    }

    public AlignerWithJets(Item item, String xmlStr) {
        super(item, ElementType.ALIGNERWITHJETS);
        takeFromXML(xmlStr);
    }

    private void prepareAlignToList() throws Exception{
        if (!initiated) {
            alignBaseZ = new AlignTo(item.miAsVector.z, alignerJetSetZ);
            alignBaseX = new AlignTo(item.miAsVector.x, alignerJetSetX);
            alignBaseY = new AlignTo(item.miAsVector.y, alignerJetSetY);

            alignBaseZ.setNeighbours(alignBaseY, alignBaseX);
            alignBaseX.setNeighbours(alignBaseZ, alignBaseY);
            alignBaseY.setNeighbours(alignBaseX, alignBaseZ);

            opposite = false;
            switch (direction) {
                case NEGZ:
                    opposite = true;
                case POSZ:
                    myAlignBase = alignBaseZ;
                    break;
                case NEGX:
                    opposite = true;
                case POSX:
                    myAlignBase = alignBaseX;
                    break;
                case NEGY:
                    opposite = true;
                case POSY:
                    myAlignBase = alignBaseY;
                    break;

            }
            initiated = true;
        }
    }

    public boolean completeThisStep(OneTimeStep theStep, double nowT, double deltaT) {
        boolean retVal = false;
        if (nowT >= theStep.startTime) {
            try {
                ActivateIt(theStep, deltaT);
            } catch (Exception e) {
                showError(e.getMessage());
                nowActionPos = ActionPos.DONE;
            }
        }
        if (nowActionPos == ActionPos.DONE) {
            resetStatus();
            retVal = true;
        }
        return retVal;
    }

    void resetStatus() {
        this.theStep = null;
        basicsSet = false;
        nowActionPos = ActionPos.NOTSTARTED;
    }

    public void ActivateIt(OneTimeStep theStep, double deltaT) throws Exception{
        if (nowActionPos != ActionPos.DONE) {
            if (this.theStep != theStep) { // a new step
                this.theStep = theStep;
                prepareAlignToList();
                basicsSet = prepareBasicsForTorques();
                active = true;
            }
            updateTorque(deltaT);
        }
     }

    Vector3dMV globalAlignTo;
    boolean basicsSet = false;

    private boolean prepareBasicsForTorques() {
        boolean retVal = true;
        globalAlignTo = new Vector3dMV(item.status.velocity);
        if (theStep.stepAction == OneTimeStep.StepAction.ALIGNCOUNTERTOVELOCITY)
            globalAlignTo.negate();
        nowActionPos = ActionPos.NOTSTARTED;
        activeT = 0;
        return retVal;
    }

    class AlignTo  {
        double mI;
        AlignerJetSet alignerJetSet;
        AlignTo prev;
        AlignTo next;
        double torqueMagnitude;
        double torque;
        double alpha; // angular acceleration

        AlignTo (double mI, AlignerJetSet alignerJetSet) throws Exception {
            this.mI = mI;
            this.alignerJetSet = alignerJetSet;
            torqueMagnitude = alignerJetSet.getTorque().length();
            alpha = torqueMagnitude / mI;
            if (alpha == 0)
                throw new Exception("Torque capability is 0");
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

        public void setNeighbours(AlignTo prev, AlignTo next) {
            this.prev = prev;
            this.next = next;
        }

        void setTorque(boolean secondTime) {
            Vector3dMV alignTo = new Vector3dMV(globalAlignTo);
            item.globalToItem().transform(alignTo);
            double numerator = 0;
            double denominator = 0;
            torque = 0;
            prev.torque = 0;
            next.torque = 0;
            switch (direction) {
                case POSZ:
                case NEGZ:
                    numerator = (secondTime) ? alignTo.x : alignTo.y;
                    denominator = alignTo.z;
                    break;
                case POSX:
                case NEGX:
                    numerator = (secondTime) ? alignTo.y : alignTo.z;
                    denominator = alignTo.x;
                    break;
                case POSY:
                case NEGY:
                    numerator = (secondTime) ? alignTo.z : alignTo.x;
                    denominator = alignTo.y;
                    break;
            }
            setTorque(numerator, denominator, secondTime);
        }

        double setTorque(double numerator, double denominator, boolean secondTime) {
            double theta;
            if (secondTime) {
                if (denominator == 0)
                    theta = ((numerator == 0) ? 0 : Math.PI / 2);
                else
                    theta = Math.atan(numerator / denominator);
                if (denominator < 0)
                    theta += Math.PI;
                if (theta > Math.PI)
                    theta -= (2 * Math.PI);
                if (opposite) { // case of NEGX etc.
                    if (theta <= 0)
                        theta += Math.PI;
                    else
                        theta -= Math.PI;
                }
                thisActionTime = prev.actionTime(theta);

            } else {
                if (denominator == 0)
                    theta = ((numerator == 0) ? 0 : -Math.PI / 2);
                else
                    theta = -Math.atan(numerator / denominator);
                if (denominator < 0)
                    theta -= Math.PI;
                if (theta < -Math.PI)
                    theta += (2 * Math.PI);
                if (opposite) { // case of NEGX etc.
                    if (theta <= 0)
                        theta += Math.PI;
                    else
                        theta -= Math.PI;
                }
                thisActionTime = next.actionTime(theta);
            }
            return thisActionTime;
        }
    }

    private void updateTorque(double duration) {
        switch (direction) {
            case POSZ:
            case NEGZ:
                switch (nowActionPos) {
                    case NOTSTARTED:
                        myAlignBase.setTorque(false);
                        actionEndTime = thisActionTime;
                        nowTorque.set(alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                        nowActionPos = ActionPos.FIRST;
                        break;
                    case FIRST:
                        if (activeT >= actionEndTime) {
                            actionEndTime += thisActionTime;
                            nowTorque.set(-alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                            nowActionPos = ActionPos.SECOND;
                        }
                        break;
                    case SECOND:
                        if (activeT >= actionEndTime) {
                            myAlignBase.setTorque(true);
                            actionEndTime += thisActionTime;
                            nowTorque.set(alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                            nowActionPos = ActionPos.THIRD;
                        }
                        break;
                    case THIRD:
                        if (activeT >= actionEndTime) {
                            actionEndTime += thisActionTime;
                            nowTorque.set(alignBaseX.torque, -alignBaseY.torque, alignBaseZ.torque);
                            nowActionPos = ActionPos.FOURTH;
                        }
                        break;
                    case FOURTH:
                        if (activeT >= actionEndTime) {
                            nowTorque.set(0, 0, 0);
                            nowActionPos = ActionPos.DONE;
                        }
                }
                break;
            case POSX:
            case NEGX:
                switch (nowActionPos) {
                    case NOTSTARTED:
                        myAlignBase.setTorque(false);
                        actionEndTime = thisActionTime;
                        nowTorque.set(alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                        nowActionPos = ActionPos.FIRST;
                        break;
                    case FIRST:
                        if (activeT >= actionEndTime) {
                            actionEndTime += thisActionTime;
                            nowTorque.set(alignBaseX.torque, -alignBaseY.torque, alignBaseZ.torque);
                            nowActionPos = ActionPos.SECOND;
                        }
                        break;
                    case SECOND:
                        if (activeT >= actionEndTime) {
                            myAlignBase.setTorque(true);
                            actionEndTime += thisActionTime;
                            nowTorque.set(alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                            nowActionPos = ActionPos.THIRD;
                        }
                        break;
                    case THIRD:
                        if (activeT >= actionEndTime) {
                            actionEndTime += thisActionTime;
                            nowTorque.set(alignBaseX.torque, alignBaseY.torque, -alignBaseZ.torque);
                            nowActionPos = ActionPos.FOURTH;
                        }
                        break;
                    case FOURTH:
                        if (activeT >= actionEndTime) {
                            nowTorque.set(0, 0, 0);
                            nowActionPos = ActionPos.DONE;
                        }
                }
                break;
            case POSY:
            case NEGY:
                switch (nowActionPos) {
                    case NOTSTARTED:
                        myAlignBase.setTorque(false);
                        actionEndTime = thisActionTime;
                        nowTorque.set(alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                        nowActionPos = ActionPos.FIRST;
                        break;
                    case FIRST:
                        if (activeT >= actionEndTime) {
                            actionEndTime += thisActionTime;
                            nowTorque.set(alignBaseX.torque, alignBaseY.torque, -alignBaseZ.torque);
                            nowActionPos = ActionPos.SECOND;
                        }
                        break;
                    case SECOND:
                        if (activeT >= actionEndTime) {
                            myAlignBase.setTorque(true);
                            actionEndTime += thisActionTime;
                            nowTorque.set(alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                            nowActionPos = ActionPos.THIRD;
                        }
                        break;
                    case THIRD:
                        if (activeT >= actionEndTime) {
                            actionEndTime += thisActionTime;
                            nowTorque.set(-alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                            nowActionPos = ActionPos.FOURTH;
                        }
                        break;
                    case FOURTH:
                        if (activeT >= actionEndTime) {
                            nowTorque.set(0, 0, 0);
                            nowActionPos = ActionPos.DONE;
                        }
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
        xmlStr.append(XMLmv.putTag("direction", direction.toString()));
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
        direction = Direction.getEnum(vp.val);
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
        return new OneTimeStep.StepAction[] {OneTimeStep.StepAction.ALIGNTOVELOCITY, OneTimeStep.StepAction.ALIGNCOUNTERTOVELOCITY};
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
        JComboBox<Direction> cbDirection;
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
            cbDirection = new JComboBox<Direction>(Direction.values());
            cbDirection.setSelectedItem(direction);
            JPanel outerP = new JPanel(new BorderLayout());
            MultiPairColPanel jpBasic = new MultiPairColPanel("Aligner Details");
            jpBasic.addItemPair("ID", tName);
            jpBasic.addItemPair("Aligner Direction Vector", cbDirection);
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
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
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
                }
            };
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
            boolean retVal = true;
            name = tName.getText();
            direction = (Direction)cbDirection.getSelectedItem();
            return retVal;
        }

        Item.EditResponse getResponse() {
            return response;
        }
    }

    void showError(String msg) {
        ItemMovementsApp.showError("Aligner: " + msg);
    }

}
