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

import static GeneralElements.accessories.Aligner.TimePos.*;

/**
 * Created by mviswanathan on 14-05-2017.
 */
public class Aligner extends JetsAndSeekers {
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

    enum TimePos {NOTSTARTED, FIRST, SECOND, THIRD, FOURTH}
    Direction direction = Direction.POSZ;
    Vector3dMV nowTorque = new Vector3dMV();
    double activeT; // active since
    double halfTime; // when acceleration/ deceleration switch is to take place
    double quarter1Time;
    double quarter3Time;
    boolean switched = false;
    TimePos nowTimePos = NOTSTARTED;
    double xTorque;
    double yTorque;
    double zTorque;

    AlignTo alignBaseZ;
    AlignTo alignBaseX;
    AlignTo alignBaseY;
    AlignTo myAlignBase;
    boolean initiated = false;

    public Aligner(Item item) {
        super(item, "UNKNOWN",  ElementType.ALIGNER);
    }

    public Aligner(Item item, String xmlStr) {
        super(item, ElementType.ALIGNER);
        takeFromXML(xmlStr);
    }

    private void prepareAlignToList() {
        if (!initiated) {
            alignBaseZ = new AlignTo(item.miAsVector.z);
            alignBaseX = new AlignTo(item.miAsVector.x);
            alignBaseY = new AlignTo(item.miAsVector.y);

            alignBaseZ.setNeighbours(alignBaseY, alignBaseX);
            alignBaseX.setNeighbours(alignBaseZ, alignBaseY);
            alignBaseY.setNeighbours(alignBaseX, alignBaseZ);
            switch (direction) {
                case POSZ:
                case NEGZ:
                    myAlignBase = alignBaseZ;
                    break;
                case POSX:
                case NEGX:
                    myAlignBase = alignBaseX;
                    break;
                case POSY:
                case NEGY:
                    myAlignBase = alignBaseY;
                    break;

            }
            initiated = true;
        }
    }

    public void setActive(boolean active, OneTimeStep theStep, double duration) {
        this.active = active;
        if (active) {
            if (this.theStep != theStep) { // a new step
                this.theStep = theStep;
                prepareAlignToList();
                basicsSet = prepareBasicsForTorques();
            }
            updateTorque(duration);
        }
        else {
            basicsSet = false;
            nowTimePos = NOTSTARTED;
        }
    }

    Vector3dMV globalAlignTo;
    double tRefSq;
    boolean basicsSet = false;

    private boolean prepareBasicsForTorques() {
        boolean retVal = false;
        globalAlignTo = new Vector3dMV(item.status.velocity);
        halfTime = theStep.duration() / 2;
        if (halfTime > 0) {
            quarter1Time = halfTime / 2;
            quarter3Time = quarter1Time + halfTime;
            tRefSq = quarter1Time * quarter1Time;
            nowTimePos = NOTSTARTED;
            activeT = 0;
            retVal = true;
        }
        return retVal;
    }

    class AlignTo  {
        double mI;
        AlignTo prev;
        AlignTo next;
        double torque;

        AlignTo (double mI) {
            this.mI = mI;
        }

        public void setNeighbours(AlignTo prev, AlignTo next) {
            this.prev = prev;
            this.next = next;
        }

        void setTorque(boolean second) {
            Vector3dMV alignTo = new Vector3dMV(globalAlignTo);
            item.globalToItem().transform(alignTo);
            double numerator = 0;
            double denominator = 0;
            torque = 0;
            prev.torque = 0;
            next.torque = 0;
            switch (direction) {
                case POSZ:
                    numerator = (second) ? alignTo.x : alignTo.y;
                    denominator = alignTo.z;
                    break;
                case POSX:
                    numerator = (second) ? alignTo.y : alignTo.z;
                    denominator = alignTo.x;
                    break;
                case POSY:
                    numerator = (second) ? alignTo.z : alignTo.x;
                    denominator = alignTo.y;
                    break;
            }
            setTorque(numerator, denominator, second);
        }

        void setTorque(double numerator, double denominator, boolean second) {
            double theta;
            if (second) {
                if (denominator == 0)
                    theta = ((numerator == 0) ? 0 : Math.PI / 2);
                else
                    theta = Math.atan(numerator / denominator);
                if (denominator < 0)
                    theta += Math.PI;
                if (theta > Math.PI)
                    theta -= (2 * Math.PI);

                prev.torque = theta / tRefSq * prev.mI;

            } else {
                if (denominator == 0)
                    theta = ((numerator == 0) ? 0 : -Math.PI / 2);
                else
                    theta = -Math.atan(numerator / denominator);
                if (denominator < 0)
                    theta -= Math.PI;
                if (theta < -Math.PI)
                    theta += (2 * Math.PI);

                next.torque = theta / tRefSq * next.mI;
            }
        }
    }

    private void updateTorque(double duration) {
        switch (direction) {
            case POSZ:
            case NEGZ:
                switch (nowTimePos) {
                    case NOTSTARTED:
                        myAlignBase.setTorque(false);
                        nowTorque.set(alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                        nowTimePos = FIRST;
                        break;
                    case FIRST:
                        if (activeT >= quarter1Time) {
                            nowTorque.set(-alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                            nowTimePos = SECOND;
                        }
                        break;
                    case SECOND:
                        if (activeT >= halfTime) {
                            myAlignBase.setTorque(true);
                            nowTorque.set(alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                            nowTimePos = THIRD;
                        }
                        break;
                    case THIRD:
                        if (activeT >= quarter3Time) {
                            nowTorque.set(alignBaseX.torque, -alignBaseY.torque, alignBaseZ.torque);
                            nowTimePos = FOURTH;
                        }
                        break;
                }
                break;
            case POSX:
            case NEGX:
                switch (nowTimePos) {
                    case NOTSTARTED:
                        myAlignBase.setTorque(false);
                        nowTorque.set(alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                        nowTimePos = FIRST;
                        break;
                    case FIRST:
                        if (activeT >= quarter1Time) {
                            nowTorque.set(alignBaseX.torque, -alignBaseY.torque, alignBaseZ.torque);
                            nowTimePos = SECOND;
                        }
                        break;
                    case SECOND:
                        if (activeT >= halfTime) {
                            myAlignBase.setTorque(true);
                            nowTorque.set(alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                            nowTimePos = THIRD;
                        }
                        break;
                    case THIRD:
                        if (activeT >= quarter3Time) {
                            nowTorque.set(alignBaseX.torque, alignBaseY.torque, -alignBaseZ.torque);
                            nowTimePos = FOURTH;
                        }
                        break;
                }
                break;
            case POSY:
            case NEGY:
                switch (nowTimePos) {
                    case NOTSTARTED:
                        myAlignBase.setTorque(false);
                        nowTorque.set(alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                        nowTimePos = FIRST;
                        break;
                    case FIRST:
                        if (activeT >= quarter1Time) {
                            nowTorque.set(alignBaseX.torque, alignBaseY.torque, -alignBaseZ.torque);
                            nowTimePos = SECOND;
                        }
                        break;
                    case SECOND:
                        if (activeT >= halfTime) {
                            myAlignBase.setTorque(true);
                            nowTorque.set(alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                            nowTimePos = THIRD;
                        }
                        break;
                    case THIRD:
                        if (activeT >= quarter3Time) {
                            nowTorque.set(-alignBaseX.torque, alignBaseY.torque, alignBaseZ.torque);
                            nowTimePos = FOURTH;
                        }
                        break;
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

    public Item.EditResponse editData(InputControl inpC, Component c) {
        AlignerDetails dlg = new AlignerDetails(inpC, c);
        if (c == null)
            dlg.setLocation(100, 100);
        else
            dlg.setLocationRelativeTo(c);
        dlg.setVisible(true);
        return dlg.getResponse();
    }

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
                        if (ItemMovementsApp.decide("Deleting Aligner ", "Do you want to DELETE this Aligner?", caller)) {
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
