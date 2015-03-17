package GeneralElements.localActions;

import Applications.ItemMovementsApp;
import GeneralElements.DarkMatter;
import GeneralElements.Display.TuplePanel;
import GeneralElements.Item;
import mvUtils.Vector3dMV;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.swing.*;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by M Viswanathan on 27 Aug 2014
 */

// TODO FixedAcceleration is not required since handled by GlobalActions
public class FixedAcceleration extends LocalAction {
    static Vector3dMV unitDirection = new Vector3dMV(0, -1, 0);  // direction of fixed Acceleration, a unit Vector
    static double fixedAcc = 9.81; // fixed acceleration value
    boolean bFirstTime = true; // for the first time no edit is allowed
    public FixedAcceleration() {
        super(Type.FIXEDACCELERATION);
    }

    public FixedAcceleration(DarkMatter item) {
        super(Type.FIXEDACCELERATION, item);
    }

    public FixedAcceleration(Vector3d direction, double fixedAcc) {
        super(Type.FIXEDACCELERATION);
        setValues(direction, fixedAcc);
    }

    private void setValues(Vector3d direction, double fixedAcc) {
        unitDirection.set(direction);
        unitDirection.scale(1 / direction.length());
        this.fixedAcc = fixedAcc;
    }

    private void setValues(String xmlStr) throws Exception {
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "direction", 0);
        unitDirection = new Vector3dMV(vp.val);
        vp = XMLmv.getTag(xmlStr, "fixedAcc", 0);
        fixedAcc = Double.valueOf(vp.val);
    }

    public FixedAcceleration(DarkMatter item, String xmlStr) throws Exception {
        super(Type.FIXEDACCELERATION, item);
        setValues(xmlStr);
    }

    public Vector3d getForce() {
        Vector3d force = new Vector3d(unitDirection);
        force.scale(fixedAcc * item.mass);
        return force;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("type", type.toString()));
        xmlStr.append(XMLmv.putTag("direction", unitDirection.dataInCSV()));
        xmlStr.append((XMLmv.putTag("fixedAcc", fixedAcc)));
        return xmlStr;
    }

    String getParamString() {
        return "Acc " + fixedAcc + ", Direction :" + unitDirection;
    }

    public Object clone() {
        FixedAcceleration cloned = (FixedAcceleration)super.clone();
        cloned.unitDirection = new Vector3dMV(unitDirection);
        return cloned;
    }

    @Override
    public Item.EditResponse editAction(InputControl inpC, Component c) {
        if (bFirstTime) {
            bFirstTime = false;
            return Item.EditResponse.CHANGED;
        }
        else { // the next time edit is allowed which changes the static values
            ActionDialog dlg = new ActionDialog(inpC, c);
            dlg.setVisible(true);
            return dlg.getResponse();
        }
    }

    class ActionDialog extends JDialog {
        TuplePanel fixedAccVectPan;
        NumberTextField ntFixedAcc;
        JButton delete = new JButton("DELETE");
        JButton ok = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        InputControl inpC;
        Item.EditResponse response = Item.EditResponse.CHANGED;

        ActionDialog(InputControl inpC, Component c) {
            setModal(true);
            setResizable(false);
            this.inpC = inpC;
            dbInit();
            setLocationRelativeTo(c);
        }

        void dbInit() {
            JPanel outerPan = new JPanel(new BorderLayout());
            MultiPairColPanel jp = new MultiPairColPanel("Fixed Acceleration (eg. Due to Earth's gravity)");
            fixedAccVectPan = new TuplePanel(inpC, unitDirection, 8, -100, 100, "##0.#####E00", "Direction of Acc Vector");
            ntFixedAcc = new NumberTextField(inpC, fixedAcc, 8, false, 0, 2000, "##0.#####E00", "Fixed Acc in m/s2");
            jp.addItemPair(ntFixedAcc);
            jp.addItemPair("Direction of Acc", fixedAccVectPan);
            outerPan.add(jp, BorderLayout.CENTER);
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == ok) {
                        if (takeValuesFromUI())
                            closeThisWindow();
                    }
                    else if (src == delete) {
                        if (ItemMovementsApp.decide("Deleting Action ", "Do you want to DELETE this Action?")) {
                            response = Item.EditResponse.DELETE;
                            closeThisWindow();
                        }
                    }
                    else {
                        response = Item.EditResponse.NOTCHANGED;
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
            add(outerPan);
            pack();
        }

        Item.EditResponse getResponse() {
            return response;
        }

        boolean takeValuesFromUI() {
            boolean retVal = false;
            if (!ntFixedAcc.isInError()) {
                if (ItemMovementsApp.decide("Change in Global Fixed Acceleration", "Any change here will affect all invocation of Fixed Acceleration")) {
                    Tuple3d accTuple = fixedAccVectPan.getTuple3d();
                    if (accTuple != null)
                        unitDirection.set(accTuple);
                    double vecLen = unitDirection.length();
                    if (vecLen > 0) {
                        fixedAcc = ntFixedAcc.getData();
                        unitDirection.scale(1 / vecLen);
                        response = Item.EditResponse.CHANGED;
                        retVal = true;
                    } else {
                        ItemMovementsApp.log.error("FixedAcceleration: Acc Vector Length is < 0 [" + vecLen);
                    }
                }
            }
            return retVal;
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }
    }

}
