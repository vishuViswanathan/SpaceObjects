package GeneralElements.accessories;

import Applications.ItemMovementsApp;
import GeneralElements.Display.TuplePanel;
import GeneralElements.Item;
import GeneralElements.ItemInterface;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;
import mvUtils.physics.ForceElement;
import time.timePlan.*;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by M Viswanathan on 12 Mar 2017
 */
public class Jet extends JetsAndSeekers {
//    String name;
    ForceSource forceSource;
//    public OneJetPlan thePlan;
    ForceElement jetData;
//    boolean active;

    public Jet(ItemInterface item) {
        this(item, "??Jet", new ForceElement(1, new Vector3d(), new Point3d()));
    }

    protected Jet(ItemInterface item, ElementType type) {
        super(item, type);
    }

    public Jet(ItemInterface item, String xmlStr) {
        this(item, ElementType.JET);
        takeFromXML(xmlStr);
    }

    public Jet(ItemInterface item, String name, ForceElement jetData) {
        super(item, name, ElementType.JET);
        this.jetData = jetData;
        forceSource = new RocketEngine("Test Jet", jetData.getForce().length() / 1000, 10);
//        thePlan = new OneJetPlan(this);
    }

    public Jet(ItemInterface item, String name, ForceSource forceSource, Vector3d direction, Point3d location) {
        super(item, name, ElementType.JET);
        setJetData(forceSource, direction, location);
//        thePlan = new OneJetPlan(this);
    }

    private void setJetData(ForceSource forceSource, Vector3d direction, Point3d location) {
        jetData = new ForceElement(forceSource.effectiveForce(), direction, location);
    }

    public void noteTimePlan(OneJetPlan thePlan) {
        this.thePlan = thePlan;
    }

    public boolean isActive() {
        return active;
    }

    public void addEffect() {
        if (active) {
            item.addTojetForce(jetData.getForce());
            item.addToJetTorque(jetData.getTorque());
        }
    }

    protected String detailsString() {
        return forceSource.dataAsString();
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = super.dataInXML();
        xmlStr.append(XMLmv.putTag("name", name));
        xmlStr.append(XMLmv.putTag("forceSource", forceSource.dataInXML()));
        xmlStr.append(XMLmv.putTag("jetData", jetData.dataInXML()));
        xmlStr.append(XMLmv.putTag("thePlan", thePlan.dataInXML()));
//        xmlStr.append(XMLmv.putTag("active", active));
        return xmlStr;
    }

    protected boolean takeFromXML(String xmlStr) {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "name", 0);
        name = vp.val.trim();
        vp = XMLmv.getTag(xmlStr, "forceSource", vp.endPos);
        forceSource = new RocketEngine(vp.val);
        vp = XMLmv.getTag(xmlStr, "jetData", vp.endPos);
        jetData = new ForceElement(vp.val);
        try {
            vp = XMLmv.getTag(xmlStr, "thePlan", vp.endPos);
            thePlan = new OneJetPlan(this, vp.val);
            active = false;
        } catch (Exception e) {
            showError(".153 takeFromXML: Some mess in Time Plan");
            retVal = false;
            retVal = false;
        }
        return retVal;
    }

    public OneTimeStep.StepAction[] actions() {
        return new OneTimeStep.StepAction[] {OneTimeStep.StepAction.FIREJET};
    }

    public Item.EditResponse editData(InputControl inpC, Component c) {
        JetDetails dlg = new JetDetails(inpC, c);
        if (c == null)
            dlg.setLocation(100, 100);
        else
            dlg.setLocationRelativeTo(c);
        dlg.setVisible(true);
        return dlg.getResponse();
    }

    class JetDetails extends JDialog {
        Component caller;
        InputControl inpC;
        Item.EditResponse response;
        TextField tName;
        TuplePanel directionP;
        TuplePanel locationP;
        Vector3d direction;
        Point3d location;
        JButton jbTimePlan;
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JButton delete = new JButton("Delete");
        JDialog thisDlg;

        JetDetails(InputControl inpC, Component caller) {
            setModal(true);
            this.caller = caller;
            setResizable(false);
            this.inpC = inpC;
            thisDlg = this;
            response = Item.EditResponse.NOTCHANGED;
            dbInit();
        }

        void dbInit() {
            tName = new TextField(name, 30);
            direction = jetData.getForce();
            double len = direction.length();
            if (len > 0)
                direction.normalize();
            location = new Point3d(jetData.getActingPoint());
            directionP = new TuplePanel(inpC, direction, 6, -1000, 1000, "#,##0.000", "Jet Direction");
            locationP = new TuplePanel(inpC, location, 6, -1000, 1000, "#,##0.000", "Jet Location");
            JPanel outerP = new JPanel(new BorderLayout());
            MultiPairColPanel jpBasic = new MultiPairColPanel("Jet Details");
//            jpBasic.addItemPair(ntDuration);
            jpBasic.addItemPair("Jet ID", tName);
            jpBasic.addItemPair("Jet Direction Vector (m)", directionP);
            jpBasic.addItemPair("Jet Location Vector (m)", locationP);
            jpBasic.addItem("Direction and Location are in Item's local coordinates");
            jpBasic.addItem(forceSource.fsDetails());
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
                        if (ItemMovementsApp.decide("Deleting Jet  ", "Do you want to DELETE this Jet?" +
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

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }

        boolean takeDataFromUI() {
            boolean retVal = false;
            name = tName.getText();
            direction.set(directionP.getTuple3d());
            if (direction.length() > 0) {
                location.set(locationP.getTuple3d());
                retVal = forceSource.fsTakeDataFromUI();
                if (retVal)
                    setJetData(forceSource, direction, location);
            }
            return retVal;
        }

        Item.EditResponse getResponse() {
            return response;
        }
    }

    void showError(String msg) {
        ItemMovementsApp.showError("Jet: " + msg);
    }
}

