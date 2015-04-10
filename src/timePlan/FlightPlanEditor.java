package timePlan;

import GeneralElements.Item;
import GeneralElements.ItemSpace;
import mvUtils.display.FramedPanel;
import mvUtils.display.InputControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by M Viswanathan on 05 Apr 2015
 */
public class FlightPlanEditor {
    ItemSpace space;
    FlightPlan originalPlan;
    FlightPlan modifiedPlan;
    PlanStepsTable stepsTable;

    public FlightPlanEditor(ItemSpace space) {
        this.space = space;
    }

    public Item.EditResponse editPlan(FlightPlan flightPlan) {
        Item.EditResponse response = Item.EditResponse.CANCEL;
        this.originalPlan = flightPlan;
        modifiedPlan = originalPlan.clone();
        stepsTable = new PlanStepsTable(modifiedPlan.inpC, modifiedPlan);
        DlgFlightPlanEditor dlg = new DlgFlightPlanEditor();
        dlg.setLocation(50, 50);
        dlg.setTitle("Preparing Flight Plan for " + modifiedPlan.item);
        dlg.setVisible(true);
        switch (dlg.getResponse()) {
            case CHANGED:
                if (originalPlan.copyFrom(modifiedPlan))
                    response = Item.EditResponse.CHANGED;
        }
        return response;
    }

    class DlgFlightPlanEditor extends JDialog {
        InputControl inpC;
        JButton jbSave = new JButton("Save Plan");
        JButton jbCancel = new JButton("Cancel");
        JButton jbAddStep = new JButton("Add new Step");
        Item.EditResponse response = Item.EditResponse.CANCEL;

        DlgFlightPlanEditor() {
            setModal(true);
            inpC = space.getInputControl();
            init();
        }

        void init() {
            FramedPanel fP = new FramedPanel(new BorderLayout());
            JScrollPane sP = new JScrollPane();
            sP.setPreferredSize(new Dimension(800, 550));
            sP.setViewportView(stepsTable.getTable());
            fP.add(sP, BorderLayout.CENTER);
            ActionListener li = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == jbCancel) {
                        response = Item.EditResponse.CANCEL;
                        closeThisDlg();
                    }
                    else if (src == jbAddStep)
                        addNewStep();
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

        boolean addNewStep() {
            OneStep oneStep = new OneStep(inpC, 1);
            if (oneStep.editStep(inpC) == Item.EditResponse.CHANGED) {
                stepsTable.addOneRow(oneStep);
                return true;
            }
            return false;
        }

        Item.EditResponse getResponse() {
            return response;
        }
    }
}
