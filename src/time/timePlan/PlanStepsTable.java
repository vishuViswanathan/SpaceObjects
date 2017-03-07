package time.timePlan;

import Applications.ItemMovementsApp;
import mvUtils.display.InputControl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by M Viswanathan on 02 Apr 2015
 */
public class PlanStepsTable {
    JTable table;
    InputControl inpC;
    FlightPlan flightPlan;
//    Vector<OneStep> thePlanSteps;
    int slNo = 0;
    PlanTableModel tableModel;

    public PlanStepsTable(InputControl inpC, FlightPlan flightPlan) {
        this.inpC = inpC;
        this.flightPlan = flightPlan;
//        thePlanSteps = flightPlan.getTheSteps();
        tableModel = new PlanTableModel();
        table = new JTable(tableModel);
        table.addMouseListener(new TableListener());
        TableColumnModel colModel = table.getColumnModel();
        int[] colWidth = OneStep.getColumnWidths();
        for (int c = 0; c < colModel.getColumnCount(); c++)
            colModel.getColumn(c).setPreferredWidth(colWidth[c]);
    }

    public JTable getTable() {
        return table;
    }

    public void addOneRow(OneStep step) {
        slNo++;
        Object[] data = step.getRowData(slNo);
        tableModel.addRow(data);
        flightPlan.addStep(step);
    }

    public void updateUI() {
        tableModel.fillTable();
    }

    public void deleteRow(int row) {
        flightPlan.removeOneStep(row);
        updateUI();
    }

    public void setOneRow(int row) {
        if (row >= 0 && row < table.getRowCount()) {
            Object[] data = flightPlan.getOneStep(row).getRowData(row + 1);
            int nCol = table.getColumnCount();
            for (int col = 0; col < nCol; col++)
                tableModel.setValueAt(data[col], row, col);
        }
    }

    class PlanTableModel extends DefaultTableModel {
        PlanTableModel() {
            super(OneStep.getColHeader(), 0);
            fillTable();
        }

        void fillTable() {
            clearTable();
            for (int i = 0; i < flightPlan.getPlanSize(); i++)
                addRow(flightPlan.getOneStep(i).getRowData(slNo = (i + 1)));
        }

        void clearTable() {
            int nRow = getRowCount();
            for (int r = nRow - 1;  r >= 0; r--)
                tableModel.removeRow(r);
        }
    }

    class TableListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                int row = table.getSelectedRow();
                OneStep step = flightPlan.getOneStep(row);
                switch (step.editStep(inpC, (Component) e.getSource())) {
                    case CHANGED:
                        setOneRow(row);
                        break;
                    case NOTCHANGED:
                        ItemMovementsApp.showMessage("no Change");
                        break;
                    case DELETE:
                        deleteRow(row);
                        break;
                }
            }
        }
    }
}
