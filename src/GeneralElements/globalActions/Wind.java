package GeneralElements.globalActions;

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
 * Created by M Viswanathan on 26 Jan 2015
 */
public class Wind extends GlobalAction {
    static Vector3dMV unitDirection = new Vector3dMV(0, -1, 0);  // direction of fixed Acceleration, a unit Vector
    static double speed = 1; // fixed acceleration value
    static Vector3d pressure = new Vector3d();
    static double factor = 1.293 /2; // for air at NTP

    public Wind() {
        super(Type.WIND);
    }

    public Wind(Vector3d direction, double fixedAcc) {
        this();
        setValues(direction, fixedAcc);
    }

    private void setValues(Tuple3d direction, double speed) {
        unitDirection.set(direction);
        unitDirection.scale(1 / unitDirection.length());
        this.speed = speed;
        pressure.set(unitDirection);
        pressure.scale(factor * speed * speed); // factor * velocity^2
    }

    protected boolean setValues(String xmlStr) throws Exception {
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "windDirection", 0);
        unitDirection = new Vector3dMV(vp.val);
        vp = XMLmv.getTag(xmlStr, "windSpeed", 0);
        speed = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "factor", 0);
        factor = Double.valueOf(vp.val);
        setValues(unitDirection, speed);
        super.setValues(xmlStr);
        return true;
    }

    public Wind(String xmlStr) throws Exception {
        this();
        setValues(xmlStr);
    }

    public Vector3d getForce(DarkMatter item) {
        Vector3d force = new Vector3d(pressure);
        force.scale(item.getProjectedArea());
        return force;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("type", type.toString()));
        xmlStr.append(XMLmv.putTag("windDirection", unitDirection.dataInCSV()));
        xmlStr.append((XMLmv.putTag("windSpeed", speed)));
        xmlStr.append((XMLmv.putTag("factor", factor)));
        xmlStr.append(super.dataInXML());
        return xmlStr;
    }

    String getParamString() {
        return "Speed " + speed + ", Direction :" + unitDirection;
    }

    @Override
    public Item.EditResponse editAction(InputControl inpC, Component c) {
//        if (bFirstTime) {
//            bFirstTime = false;
//            return Item.EditResponse.CHANGED;
//        }
//        else { // the next time edit is allowed which changes the static values
        ActionDialog dlg = new ActionDialog(inpC, c);
        dlg.setVisible(true);
        return dlg.getResponse();
//        }
    }

    class ActionDialog extends JDialog {
        TuplePanel dirVectorPan;
        NumberTextField ntSpeed;
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
            MultiPairColPanel jp = new MultiPairColPanel("Wind");
            dirVectorPan = new TuplePanel(inpC, unitDirection, 8, -100, 100, "##0.#####E00", "Direction of Acc Vector");
            ntSpeed = new NumberTextField(inpC, speed, 8, false, 0, 20000, "##0.#####E00", "Speed in m/s");
            jp.addItemPair(ntSpeed);
            jp.addItemPair("Direction of Wind", dirVectorPan);
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
            if (!ntSpeed.isInError()) {
                Tuple3d dirTuple = dirVectorPan.getTuple3d();
                if (dirTuple != null) {
                    double nowSpeed = ntSpeed.getData();
                    setValues(dirTuple, nowSpeed);
                    response = Item.EditResponse.CHANGED;
                    retVal = true;
                } else {
                    ItemMovementsApp.log.error("Wind: Director vector");
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
