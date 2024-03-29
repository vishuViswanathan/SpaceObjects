package time.timePlan;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemInterface;
import GeneralElements.ItemSpace;
import GeneralElements.accessories.Jet;
import GeneralElements.accessories.JetsAndSeekers;
import mvUtils.display.*;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by M Viswanathan on 17 Mar 2017
 */
public class JetTimeController extends JetController{
    ItemInterface item;
    Hashtable<JetsAndSeekers, OneJetPlan> jetAndPlan;
    public Vector<JetsAndSeekers> jets = new Vector<>();
    JetTimeController mainThis;

//    Vector<OneJetPlan> controlTable;

    public JetTimeController(ItemInterface item) {
        this.item = item;
        jetAndPlan = new Hashtable<JetsAndSeekers, OneJetPlan>();
        jets = new Vector<>();
        mainThis = this;
    }

    public JetTimeController(ItemInterface item, String xmlStr) {
        this(item);
        takeFromXML(xmlStr);
    }

    public boolean addOneJet(JetsAndSeekers jet) {
        boolean retVal = false;
        if (!jetAndPlan.containsKey(jet)) {
            OneJetPlan onePlan = jet.thePlan;
            jetAndPlan.put(jet, onePlan);
            onePlan.noteController(this);
            updateJetList();
            retVal = true;
        }
        return retVal;
    }

    public void removeOneJet(Jet jet) {
        if (jetAndPlan.containsKey(jet)) {
            jetAndPlan.remove(jet);
        }

    }

    public void initConnections(ItemSpace space) {
        for (JetsAndSeekers j: jets)
            j.initConnections(space);
    }

    public boolean addOneJetPlanStep(Jet jet, double startTime, double duration) {
        boolean retVal = false;
        if (jetAndPlan.containsKey(jet)) {
            try {
                jetAndPlan.get(jet).addOneStep(new OneTimeStep(jet, startTime, duration));
                retVal = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return retVal;
    }

    double lastT;
    double deltaT;
    @Override
    public void upDateAllJetStatus(double deltaT, double nowTime) {
        Collection<OneJetPlan> plans = jetAndPlan.values();
        this.lastT = nowTime;
        this.deltaT = deltaT;
        for (OneJetPlan onePlan: plans)
            onePlan.updateJetStatus(deltaT, nowTime);
    }

    void updateJetList() {
        jets.removeAllElements();
        for (JetsAndSeekers jet : jetAndPlan.keySet()) {
            jets.add(jet);
        }
    }

    public ItemInterface.EditResponse editJetController(InputControl inpC, Component c) {
        ItemInterface.EditResponse response = ItemInterface.EditResponse.CANCEL;
        JetListEditor dlg = new JetListEditor();
        if (c == null)
            dlg.setLocation(50, 50);
        else
            dlg.setLocationRelativeTo(c);
        dlg.setTitle("Preparing Jet List");
        dlg.setVisible(true);
        switch (dlg.getResponse()) {
            case CHANGED:
//                if (originalPlan.copyFrom(mcodifiedPlan))
                    response = ItemInterface.EditResponse.CHANGED;
        }
        return response;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("nJet", jets.size()));
        int iJ = 0;
        for (JetsAndSeekers oneJet: jets) {
            xmlStr.append(XMLmv.putTag("iJ" + ("" + iJ).trim(), oneJet.dataInXML()));
            xmlStr.append(XMLmv.putTag("iJplan" + ("" + iJ).trim(),
                    jetAndPlan.get(oneJet).dataInXML()));
            iJ++;
        }
        return xmlStr;
    }

    boolean takeFromXML(String xmlStr) {
        boolean retVal = true;
        jets.clear();
        jetAndPlan.clear();
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "nJet", 0);
        int nJet = Integer.valueOf(vp.val);
        for (int iJ = 0; iJ < nJet; iJ++) {
            vp = XMLmv.getTag(xmlStr, "iJ" + ("" + iJ).trim(), vp.endPos);
            DataWithStatus<JetsAndSeekers> resp = JetsAndSeekers.getJetsAndSeekers(item, vp.val);
            if (resp.getStatus() == DataStat.Status.OK) {
                JetsAndSeekers oneJet = resp.getValue();
                vp = XMLmv.getTag(xmlStr, "iJplan" + ("" + iJ).trim(), vp.endPos);
                try {
                    OneJetPlan onePlan = new OneJetPlan(oneJet, vp.val);
                    jets.add(oneJet);
                    jetAndPlan.put(oneJet, onePlan);
                } catch (Exception e) {
                    retVal = false;
                    showError("Error in JetPlan: " + e.getMessage());
                    break;
                }
            }
        }
        return retVal;
    }

    public JPanel controlPanel(Component parent, InputControl inpC, ItemInterface[] otherItems) {
        ManualControlListener li;
        MultiPairColPanel mp = new MultiPairColPanel(item.getName() + " Control Panel");
        for (JetsAndSeekers oneJet: jets) {
            OneJetPlan onePlan = jetAndPlan.get(oneJet);
            li = new ManualControlListener(onePlan);
            mp.addItem(jetAndPlan.get(oneJet).controlPanel(oneJet.name, parent, inpC, otherItems, li));
        }
        return mp;
    }

    class ManualControlListener implements ActionListener {
        OneJetPlan thePlan;
        ManualControlListener(OneJetPlan thePlan) {
            this.thePlan = thePlan;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            thePlan.activateManual(lastT, deltaT);
        }
    }
    class JetListEditor extends JDialog {
        JetTable jetTable;
        InputControl inpC;
        JButton jbSave = new JButton("Save Jet List");
        JButton jbCancel = new JButton("Cancel");
        JButton jbAddNewJet = new JButton("Add new Jet");
        ItemInterface.EditResponse response = ItemInterface.EditResponse.CANCEL;
        JDialog thisDlg;
        JetListEditor() {
            setModal(true);
            inpC = item.getSpace().getInputControl();
            thisDlg = this;
            init();
        }

        void init() {
            jetTable = new JetTable(inpC, mainThis);
            FramedPanel fP = new FramedPanel(new BorderLayout());
            JScrollPane sP = new JScrollPane();
            sP.setPreferredSize(new Dimension(800, 550));
            sP.setViewportView(jetTable.getTable());
            fP.add(sP, BorderLayout.CENTER);
            ActionListener li = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == jbCancel) {
                        response = ItemInterface.EditResponse.CANCEL;
                        closeThisDlg();
                    }
                    else if (src == jbAddNewJet)
                        addNewJet(thisDlg);
                    else if (src == jbSave) {
                        response = ItemInterface.EditResponse.CHANGED;
                        closeThisDlg();
                    }
                }
            };
            jbSave.addActionListener(li);
            jbCancel.addActionListener(li);
            jbAddNewJet.addActionListener(li);
            JPanel buttP = new JPanel();
            buttP.add(jbCancel);
            buttP.add(jbAddNewJet);
            buttP.add(jbSave);
            fP.add(buttP, BorderLayout.SOUTH);
            add(fP);
            pack();
        }

        void closeThisDlg() {
            setVisible(false);
        }

        boolean addNewJet(Component c) {
            JetsAndSeekers theAccessory = JetsAndSeekers.getNewAccessory(item, c);
            if (theAccessory != null) {
                if (JetsAndSeekers.editData(theAccessory, inpC, c) == ItemInterface.EditResponse.CHANGED) {
                    jetTable.addOneRow(theAccessory);
                    return true;
                }
            }
            return false;
        }

        ItemInterface.EditResponse getResponse() {
            return response;
        }
    }

    void showError(String msg) {
        ItemMovementsApp.showError("JetTimeController: " + msg);
    }


//    class JetEditor {
//        ItemSpace space;
//        FlightPlan originalPlan;
//        FlightPlan modifiedPlan;
//        PlanStepsTable stepsTable;
//
//        public JetEditor(ItemSpace space) {
//            this.space = space;
//        }
//
//        public ItemInterface.EditResponse editPlan(InputControl inpC, FlightPlan flightPlan) {
//        }
//
//    }
}
