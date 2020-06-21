package GeneralElements.accessories;

import Applications.ItemMovementsApp;
import GeneralElements.Display.TuplePanel;
import GeneralElements.Item;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;
import mvUtils.physics.Point3dMV;
import mvUtils.physics.Vector3dMV;

import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by mviswanathan on 08-06-2017.
 * consists of two JetCouples on same axis, inopposte directions
 */
public class AlignerJetSet extends JetCouple {
    AboutAxis aboutAxis;

    public AlignerJetSet(AlignerWithJets aligner) {
        super(aligner.item);
    }

    public AlignerJetSet(AlignerWithJets aligner, AboutAxis aboutAxis) {
        super(aligner.item);
        this.aboutAxis = aboutAxis;
    }

    public AlignerJetSet(AlignerWithJets aligner, String xmlStr) {
        this(aligner);
        takeFromXML(xmlStr);
    }


    protected boolean takeFromXML(String xmlStr) {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "name", 0);
        name = vp.val.trim();
        vp = XMLmv.getTag(xmlStr, "forceSource", vp.endPos);
        forceSource = new RocketEngine(vp.val);
        vp = XMLmv.getTag(xmlStr, "location", vp.endPos);
        Point3dMV location = new Point3dMV(vp.val);
        vp = XMLmv.getTag(xmlStr, "aboutAxis", vp.endPos);
        aboutAxis = AboutAxis.getEnum(vp.val);
        setJetData(forceSource, positiveDirectionAtLocation(location), location);
        return retVal;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("name", name));
        xmlStr.append(XMLmv.putTag("forceSource", forceSource.dataInXML()));
        xmlStr.append(XMLmv.putTag("location", new Vector3dMV(jetData.getActingPoint()).dataInCSV()));
        xmlStr.append(XMLmv.putTag("aboutAxis", aboutAxis.toString()));
        return xmlStr;
    }

    public Item.EditResponse editData(InputControl inpC, Component c) {
        AlignerJetDetails dlg = new AlignerJetDetails(inpC, c);
        if (c == null)
            dlg.setLocation(100, 100);
        else
            dlg.setLocationRelativeTo(c);
        dlg.setVisible(true);
        return dlg.getResponse();
    }

    Vector3d positiveDirectionAtLocation(Tuple3d location) {
        Vector3d vDirection = new Vector3d();
        switch(aboutAxis) {
            case X:
                vDirection.set(0, -location.z, location.y);
                break;
            case Y:
                vDirection.set(location.z, 0, -location.x);
                break;
            case Z:
                vDirection.set(-location.y, location.x, 0);
                break;
        }
        if (vDirection.length() > 0)
            vDirection.normalize();
        return vDirection;
    }

    class AlignerJetDetails extends JDialog {
        Component caller;
        InputControl inpC;
        Item.EditResponse response;
        TextField tName;
        JComboBox<AboutAxis> cBAxis;
        TuplePanel locationP;
        Vector3d direction;
        Point3d location;
        JButton jbTimePlan;
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JButton delete = new JButton("Delete");
        JDialog thisDlg;

        AlignerJetDetails(InputControl inpC, Component caller) {
            setModal(true);
            this.caller = caller;
            setResizable(false);
            this.inpC = inpC;
            thisDlg = this;
            dbInit();
        }

        void dbInit() {
            tName = new TextField(name, 30);
            location = new Point3d(jetData.getActingPoint());
            cBAxis = new JComboBox<>(AboutAxis.values());
            cBAxis.setSelectedItem(aboutAxis);
            locationP = new TuplePanel(inpC, location, 6, -1000, 1000, "#,##0.000", "Jet Location");
            JPanel outerP = new JPanel(new BorderLayout());
            MultiPairColPanel jpBasic = new MultiPairColPanel("Aligner Jet Set Details");
            jpBasic.addItemPair("Jet ID", tName);
            jpBasic.addItemPair("Couple Axis", cBAxis);
            jpBasic.addItemPair("Jet Location Vector (m)", locationP);
            jpBasic.addItem("Direction and Location are in Item's local coordinates");
            jpBasic.addItem("There will be two jet Pairs, for +ve and -ve Torques");
            jpBasic.addItem("Pair locations will diametrically opposite about the Couple Axis");
            jpBasic.addItem(forceSource.fsDetails());
            jpBasic.addItem("The Jet Details for each Jet of Each pair");
            jpBasic.addBlank();
            JPanel buttPanel = new JPanel(new BorderLayout());
            buttPanel.add(delete, BorderLayout.WEST);
            buttPanel.add(cancel, BorderLayout.CENTER);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            cancel.setEnabled(false);

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
                        if (ItemMovementsApp.decide("Deleting Jet  ", "Do you want to DELETE this Jet Couple?\n" +
                                "  DELETE to be checked", caller)) {
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
            aboutAxis = (AboutAxis)cBAxis.getSelectedItem();
            location.set(locationP.getTuple3d());
            retVal = forceSource.fsTakeDataFromUI();
            if (retVal) {
                setJetData(forceSource, positiveDirectionAtLocation(location),
                        location);
            }
            return retVal;
        }

        Item.EditResponse getResponse() {
            return response;
        }
    }


    void showError(String msg) {
        ItemMovementsApp.showError("AlignerJetSet: " + msg);
    }

}
