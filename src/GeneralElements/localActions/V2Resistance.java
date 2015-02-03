package GeneralElements.localActions;

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
 * Created by M Viswanathan on 13 Aug 2014
 */
public class V2Resistance extends LocalAction {
    double factor = 1;
    double frictionArea;

    public V2Resistance(DarkMatter item) {
        this();
        this.item = item;
//        this(item, 1e4);
    }

    public V2Resistance(DarkMatter item, double factor) {
        this(factor);
        setItem(item);
    }

    public V2Resistance(double factor) {
        super(Type.FLUIDRESISTANCE);
        this.factor = factor;
    }

    public V2Resistance() {
        super(Type.FLUIDRESISTANCE);
    }

    public V2Resistance(DarkMatter item, String xmlStr) throws Exception {
        super(Type.FLUIDRESISTANCE, item);
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "factor", 0);
        try {
            factor = Double.valueOf(vp.val);
//            evalAreas();
        } catch (NumberFormatException e) {
            throw new Exception("in V2Resistance: " + e.getMessage());
        }
    }

    public Vector3d getForce() {
        Vector3d force;
        double vel = item.status.velocity.length();
        if (vel > 0) {
            double forceMagnitude = -Math.pow(vel, 2) * factor * item.getProjectedArea(); // opposing the velocity
            double scaleFactor = forceMagnitude / vel;
            force = new Vector3d(item.status.velocity);
            force.scale(scaleFactor);
        } else
            force = new Vector3d();
        return force;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("type", type.toString()));
        xmlStr.append(XMLmv.putTag("factor", factor));
        return xmlStr;
    }

    public Object clone() {
        V2Resistance cloned = (V2Resistance)super.clone();
        return cloned;
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
