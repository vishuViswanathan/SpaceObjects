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
import java.util.LinkedList;

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

    AlignTo alignToBase;
    AlignTo alignToNext;
    AlignTo alignToPrev;
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
            switch (direction) {
                case POSZ:
                case NEGZ:
                    alignToBase = new AlignTo(item.miAsVector.z);
                    alignToNext = new AlignTo(item.miAsVector.x);
                    alignToPrev = new AlignTo(item.miAsVector.y);

            }
            alignToBase.setNeighbours(alignToPrev, alignToNext);
            alignToNext.setNeighbours(alignToBase, alignToPrev);
            alignToPrev.setNeighbours(alignToNext, alignToBase);
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
//                initiateTorque();
            }
            updateTorque(duration);
        }
        else {
            basicsSet = false;
            nowTimePos = NOTSTARTED;
        }
    }

    private void initiateTorque() {
        Vector3dMV vel = new Vector3dMV(item.status.velocity);
        item.globalToItem().transform(vel);
        halfTime = theStep.duration() / 2;
        quarter1Time = halfTime / 2;
        quarter3Time = quarter1Time + halfTime;
        double tSqr = quarter1Time * quarter1Time;
        if (halfTime > 0) {
            switch (direction) {
                case POSZ:
                case NEGZ:
                    double thetaY = 0;
                    double thetaX = 0;
                    if (vel.z > 0) {
                        thetaY = Math.atan(vel.x / vel.z);
                        thetaX = - Math.atan(vel.y / vel.z); // TODO is -ve OK???
                    }
                    else {
                        thetaY = ((vel.x == 0) ? 0 : Math.PI / 2);
                        thetaX = ((vel.y == 0) ? 0 : - Math.PI / 2); // TODO is -ve OK???
                    }
                    if (direction == Direction.NEGZ) {
                        thetaX = Math.PI + thetaX;
                        thetaY = Math.PI + thetaY;
                    }
                    xTorque = thetaX / tSqr * item.miAsVector.x;
                    yTorque = thetaY / tSqr * item.miAsVector.y;
                    break;
            }
        }
//        switched = false;
        nowTimePos = NOTSTARTED;
        activeT = 0;
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

    private void setTorque(boolean theSecond) {
        boolean opposite = false;
        if (basicsSet) {
            switch (direction) {
                case NEGZ:
                    opposite = true;
                case POSZ:
                    if (theSecond)
                        setYTorque(opposite);
                    else
                        setXTorque(opposite);
                    break;

            }
        }
    }

    private void setXTorque(boolean opposite) {
        Vector3dMV alignTo = new Vector3dMV(globalAlignTo);
        item.globalToItem().transform(alignTo);
        double thetaX;
        if (alignTo.z == 0)
            thetaX = ((alignTo.y == 0) ? 0 : - Math.PI / 2); // TODO is -ve OK???
        else
            thetaX = - Math.atan(alignTo.y / alignTo.z); // TODO is -ve OK???
        if (alignTo.z < 0)
            thetaX -= Math.PI;
        if (thetaX < -Math.PI)
            thetaX += (2 * Math.PI);

        xTorque = thetaX / tRefSq * item.miAsVector.x;
    }

    private void setYTorque(boolean opposite) {
        Vector3dMV alignTo = new Vector3dMV(globalAlignTo);
        item.globalToItem().transform(alignTo);
        double thetaY;
        if (alignTo.z == 0)
            thetaY = ((alignTo.x == 0) ? 0 :  Math.PI / 2);
        else
            thetaY =  Math.atan(alignTo.x / alignTo.z);
        if (alignTo.z < 0)
            thetaY += Math.PI;
        if (thetaY > Math.PI)
            thetaY -= (2 * Math.PI);

        yTorque = thetaY / tRefSq * item.miAsVector.y;
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
            this.next = next;
            this.prev = prev;
        }

        void getTorque(boolean second) {
            Vector3dMV alignTo = new Vector3dMV(globalAlignTo);
            item.globalToItem().transform(alignTo);
            double numerator = 0;
            double denominator = 0;
            switch (direction) {
                case POSZ:
                    numerator = (second) ? alignTo.y : alignTo.x;
                    denominator = alignTo.z;
            }
            torque = getTorque(numerator, denominator, second);
        }

        double getTorque(double numerator, double denominator, boolean second) {
            double theta;
            double nowTorque;
            if (second) {
                if (denominator == 0)
                    theta = ((numerator == 0) ? 0 : Math.PI / 2);
                else
                    theta = Math.atan(numerator / denominator);
                if (denominator < 0)
                    theta += Math.PI;
                if (theta > Math.PI)
                    theta -= (2 * Math.PI);

                nowTorque = theta / tRefSq * prev.mI;

            }
            else {
                if (denominator == 0)
                    theta = ((numerator == 0) ? 0 : -Math.PI / 2);
                else
                    theta = -Math.atan(numerator / denominator);
                if (denominator < 0)
                    theta -= Math.PI;
                if (theta < -Math.PI)
                    theta += (2 * Math.PI);

                nowTorque = theta / tRefSq * next.mI;
            }
            return nowTorque;
        }
    }

    private void setXTorqueOLD(boolean opposite) {
        Vector3dMV alignTo = new Vector3dMV(globalAlignTo);
        item.globalToItem().transform(alignTo);
        double thetaX;
        if (alignTo.z == 0)
            thetaX = ((alignTo.y == 0) ? 0 : - Math.PI / 2); // TODO is -ve OK???
        else
            thetaX = - Math.atan(alignTo.y / alignTo.z); // TODO is -ve OK???
        if (opposite)
            thetaX = Math.PI + thetaX;
        xTorque = thetaX / tRefSq * item.miAsVector.x;
    }

    private void setYTorqueOLD(boolean opposite) {
        Vector3dMV alignTo = new Vector3dMV(globalAlignTo);
        item.globalToItem().transform(alignTo);
        double thetaY;
        if (alignTo.z == 0)
            thetaY = ((alignTo.x == 0) ? 0 :  Math.PI / 2);
        else
            thetaY =  Math.atan(alignTo.x / alignTo.z);

        if (opposite)
            thetaY = Math.PI + thetaY;

        yTorque = thetaY / tRefSq * item.miAsVector.y;
    }

    private void updateTorque(double duration) {
        switch (direction) {
            case POSZ:
            case NEGZ:
                switch (nowTimePos) {
                    case NOTSTARTED:
                        setTorque(false);
                        nowTorque.set(xTorque, 0, 0);
                        nowTimePos = FIRST;
                        break;
                    case FIRST:
                        if (activeT >= quarter1Time) {
                            nowTorque.set(-xTorque, 0, 0);
                            nowTimePos = SECOND;
                        }
                        break;
                    case SECOND:
                        if (activeT >= halfTime) {
                            setTorque(true);
                            nowTorque.set(0, yTorque, 0);
                            nowTimePos = THIRD;
                        }
                        break;
                    case THIRD:
                        if (activeT >= quarter3Time) {
                            nowTorque.set(0, -yTorque, 0);
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

    private void alignToVelocity() {
        Vector3dMV vel = new Vector3dMV(item.status.velocity);
        item.globalToItem().transform(vel);
        switch (direction) {
            case POSZ:
                if (vel.z > 0) {
                    double thetaY = Math.atan(vel.x / vel.z);
                    double thetaX = Math.atan(vel.y / vel.z);
                }

        }
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
