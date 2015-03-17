package GeneralElements.globalActions;
import Applications.ItemMovementsApp;
import GeneralElements.DarkMatter;
import GeneralElements.Item;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by M Viswanathan on 25 Jan 2015
 */
public class V2ResistanceG extends GlobalAction {
    static double factor = 1;
    double frictionArea;

    public V2ResistanceG(double factor) {
        this();
        this.factor = factor;
    }

    public V2ResistanceG() {
        super(GlobalAction.Type.FLUIDRESISTANCEg);
    }

    public V2ResistanceG(String xmlStr) throws Exception {
        super(GlobalAction.Type.FLUIDRESISTANCEg);
        setValues(xmlStr);
    }

    @Override
    protected boolean setValues(String xmlStr) throws Exception {
        boolean retVal = false;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "factor", 0);
        try {
            factor = Double.valueOf(vp.val);
            super.setValues(xmlStr);
            retVal = true;
        } catch (NumberFormatException e) {
            throw new Exception("in V2ResistanceG: " + e.getMessage());
        }
        return retVal;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("type", type.toString()));
        xmlStr.append(XMLmv.putTag("factor", factor));
        xmlStr.append(super.dataInXML());
        return xmlStr;
    }

    public Vector3d getForce(DarkMatter item) {
//        Vector3d force;
        Vector3d force = item.tempForce;
        double vel = item.status.velocity.length();
        if (vel > 0) {
            double forceMagnitude = -Math.pow(vel, 2) * factor * item.getProjectedArea(); // opposing the velocity
            double scaleFactor = forceMagnitude / vel;
//            force = new Vector3d(item.status.velocity);
            force.set(item.status.velocity);
            force.scale(scaleFactor);
        } else
            force = new Vector3d();
        return force;
    }

    String getParamString() {
        return "" + type + ", Resistance " + factor + " N.sec2/m4";
    }


    @Override
    public Item.EditResponse editAction(InputControl inpC, Component c) {
        ActionDialog dlg = new ActionDialog(inpC, c);
        dlg.setVisible(true);
        return dlg.getResponse();
    }

    class ActionDialog extends JDialog {
        NumberTextField ntFactor;
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
            MultiPairColPanel jp = new MultiPairColPanel("Resistance on face area proportional to Velocity squared");
            ntFactor = new NumberTextField(inpC, factor, 8, false, 0, 2000, "##0.##E00", "Factor(N.sec2/m4");
            jp.addItemPair(ntFactor);
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
            if (!ntFactor.isInError()) {
                factor = ntFactor.getData();
                retVal = true;
            }
            return retVal;
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }
    }

}

