package time.timePlan;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.accessories.JetsAndSeekers;
import mvUtils.display.FramedPanel;
import mvUtils.display.InputControl;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * Created by M Viswanathan on 18 Mar 2017
 */
public class OneJetPlan {
    JetsAndSeekers theJet;
    JetController theController;
    Vector<OneTimeStep> theSteps;
    OneTimeStep manualStep;
    boolean manualStepOn = false;

    int planSize;
    // while running the flightPlan
    double nowStepTime = 0;
    int stepPos = 0;
    OneTimeStep nowStep;
    boolean bValid = false;

    public OneJetPlan(JetsAndSeekers theJet, String xmlStr) throws Exception {
        this(theJet);
        takeFromXML(xmlStr);
    }

    public OneJetPlan(JetsAndSeekers theJet) {
        this.theJet = theJet;
        theSteps = new Vector<OneTimeStep>();
        theJet.noteTimePlan(this);
        manualStep = new OneTimeStep(theJet, 0, 5);
        manualStepOn = false;
    }

    public void noteController(JetController theController) {
        this.theController = theController;
    }

    public boolean isValid() {
        return (theSteps.size() > 0) && bValid;
    }

    public boolean activateManual(double nowT, double deltaT) {
        boolean retVal = false;
        if (!manualStepOn && manualStep != null)   {
            ItemMovementsApp.debug("Present active Step is not checked and will be interrupted, if ON");
            manualStep.startTime = nowT;
            manualStep.endTime = nowT + manualStep.duration;
            nowStep = manualStep;
            manualStepOn = true;
            nowStepTime = nowT - deltaT;
            retVal = true;
        }
        return retVal;
    }

    public void updateJetStatus(double deltaT, double nowT) {
        if (manualStepOn) {
            boolean makeActive = false;
            nowStepTime += deltaT;
            if (nowStepTime >= nowStep.startTime) {
                if (nowStepTime < nowStep.endTime)
                    makeActive = true;
                else {
                    manualStepOn = false;
                    nowStepTime = nowT;
                }
            }
            theJet.setActive(makeActive, nowStep, deltaT);
            if (!makeActive) {
                if (bValid)
                    nowStep = theSteps.get(stepPos);
            }
        }
        else {
            if (bValid) {
                boolean makeActive = false;
                nowStepTime += deltaT;
                if (nowStepTime >= nowStep.startTime) {
                    if (nowStepTime < nowStep.endTime)
                        makeActive = true;
                    else {
                        stepPos++;
                        if (stepPos < planSize)
                            nowStep = theSteps.get(stepPos);
                        else
                            bValid = false;
                    }
                }
                theJet.setActive(makeActive, nowStep, deltaT);
            }
        }
    }

    public void updateJetStatusOLD(double deltaT, double nowT) {
        if (bValid) {
            boolean makeActive = false;
            nowStepTime += deltaT;
            if (nowStepTime >= nowStep.startTime) {
                if (nowStepTime < nowStep.endTime)
                    makeActive = true;
                else {
                    stepPos++;
                    if (stepPos < planSize)
                        nowStep = theSteps.get(stepPos);
                    else
                        bValid = false;
                }
            }
            theJet.setActive(makeActive, nowStep, deltaT);
        }
    }

    /**
     * @param oneStep
     * @return the endTime of the included step
     */
    public double addOneStep(OneTimeStep oneStep) throws Exception {
        if (planSize > 0) {
            if (theSteps.get(planSize - 1).endTime > oneStep.startTime)
                throw new Exception("OneJetPlan.73: The start time is earlier than the end time of last step");
        }
        theSteps.add(oneStep);
        planSize = theSteps.size();
        nowStep = theSteps.get(stepPos);
        bValid = true;
        return planSize;
    }

    public void removeOneStep(OneTimeStep oneStep) {
        theSteps.removeElement(oneStep);
        bValid = theSteps.size() > 0;
    }

    public void removeOneStep(int stepN) {
        theSteps.removeElement(stepN);
    }

    public Item.EditResponse editPlan(InputControl inpC, Component c) {
        Item.EditResponse response = Item.EditResponse.CANCEL;
        DlgTimePlanEditor dlg = new DlgTimePlanEditor(inpC, c);
        if (c == null)
            dlg.setLocation(50, 50);
        else
            dlg.setLocationRelativeTo(c);
        dlg.setVisible(true);
        switch (dlg.getResponse()) {
            case CHANGED:
                response = Item.EditResponse.CHANGED;
        }
        return response;
    }

    OneTimeStep getOneStep(int stepN) {
        return theSteps.get(stepN);
    }

    int getPlanSize() {
        return theSteps.size();
    }

    class DlgTimePlanEditor extends JDialog {
        Component caller;
        InputControl inpC;
        JButton jbSave = new JButton("Save Time Steps");
        JButton jbCancel = new JButton("Cancel");
        JButton jbAddStep = new JButton("Add new Time Step");
        Item.EditResponse response = Item.EditResponse.CANCEL;
        TimePlanTable timePlanTable;
        JDialog thisDlg;
        DlgTimePlanEditor(InputControl inpC, Component caller) {
            setModal(true);
            this.caller = caller;
            this.inpC = inpC;
            thisDlg = this;
            init();
        }

        void init() {
            timePlanTable = new TimePlanTable(inpC, caller);
            FramedPanel fP = new FramedPanel(new BorderLayout());
            JScrollPane sP = new JScrollPane();
            sP.setPreferredSize(new Dimension(800, 550));
            sP.setViewportView(timePlanTable.getTable());
            fP.add(sP, BorderLayout.CENTER);
            ActionListener li = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == jbCancel) {
                        response = Item.EditResponse.CANCEL;
                        closeThisDlg();
                    } else if (src == jbAddStep)
                        addNewStep(thisDlg);
                    else if (src == jbSave) {
                        response = Item.EditResponse.CHANGED;
                        closeThisDlg();
                    }
                }
            };
            jbSave.addActionListener(li);
            jbCancel.addActionListener(li);
            jbAddStep.addActionListener(li);
            JPanel buttP = new JPanel();
            buttP.add(jbCancel);
            buttP.add(jbAddStep);
            buttP.add(jbSave);
            fP.add(buttP, BorderLayout.SOUTH);
            add(fP);
            pack();
        }

        void closeThisDlg() {
            setVisible(false);
        }

        boolean addNewStep(Component c) {
            OneTimeStep oneStep = new OneTimeStep(theJet,0, 0);
            if (oneStep.editStep(inpC, c) == Item.EditResponse.CHANGED) {
                try {
//                    addOneStep(oneStep);
                    timePlanTable.addOneRow(oneStep);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        }

        Item.EditResponse getResponse() {
            return response;
        }
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("nSteps", theSteps.size()));
        int iStep = 0;
        for (OneTimeStep theStep : theSteps) {
            xmlStr.append(XMLmv.putTag("iStep" + ("" + iStep).trim(), theStep.dataInXML()));
            iStep++;
        }
        return xmlStr;
    }

    private boolean takeFromXML(String xmlStr) throws Exception {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "nSteps", 0);
        int nSteps = Integer.valueOf(vp.val);
        for (int step = 0; step < nSteps; step++) {
            vp = XMLmv.getTag(xmlStr, "iStep" + ("" + step).trim(), vp.endPos);
            addOneStep(new OneTimeStep(theJet, vp.val));
        }
        return retVal;
    }

    public JPanel controlPanel(String jetName, Component parent, InputControl inpC, ActionListener activationListener) {
        return manualStep.controlPanel(jetName, parent, inpC, activationListener);
    }

    class TimePlanTable {
        Component caller;
        JTable table;
        InputControl inpC;
        int slNo = 0;
        PlanTableModel tableModel;

        public TimePlanTable(InputControl inpC, Component caller) {
            this.inpC = inpC;
            this.caller = caller;
            tableModel = new PlanTableModel();
            table = new JTable(tableModel);
            table.addMouseListener(new TableListener());
            TableColumnModel colModel = table.getColumnModel();
            int[] colWidth = OneTimeStep.getColumnWidths();
            for (int c = 0; c < colModel.getColumnCount(); c++)
                colModel.getColumn(c).setPreferredWidth(colWidth[c]);
        }

        public JTable getTable() {
            return table;
        }

        public void addOneRow(OneTimeStep step) {
            slNo++;
            Object[] data = step.getRowData(slNo);
            tableModel.addRow(data);
            try {
                addOneStep(step);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void updateUI() {
            tableModel.fillTable();
        }

        public void deleteRow(int row) {
            removeOneStep(row);
            updateUI();
        }

        public void setOneRow(int row) {
            if (row >= 0 && row < table.getRowCount()) {
                Object[] data = getOneStep(row).getRowData(row + 1);
                int nCol = table.getColumnCount();
                for (int col = 0; col < nCol; col++)
                    tableModel.setValueAt(data[col], row, col);
            }
            updateUI();
        }

        class PlanTableModel extends DefaultTableModel {
            PlanTableModel() {
                super(OneTimeStep.getColHeader(), 0);
                fillTable();
            }

            void fillTable() {
                clearTable();
                for (int i = 0; i < getPlanSize(); i++)
                    addRow(getOneStep(i).getRowData(slNo = (i + 1)));
            }

            void clearTable() {
                int nRow = getRowCount();
                for (int r = nRow - 1; r >= 0; r--)
                    tableModel.removeRow(r);
            }
        }

        class TableListener extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int row = table.getSelectedRow();
                    OneTimeStep step = getOneStep(row);
                    switch (step.editStep(inpC, caller)) {
                        case CHANGED:
                            setOneRow(row);
                            break;
                        case NOTCHANGED:
                            ItemMovementsApp.showMessage("no Change", caller);
                            break;
                        case DELETE:
                            deleteRow(row);
                            break;
                    }
                }
            }
        }

    }
}