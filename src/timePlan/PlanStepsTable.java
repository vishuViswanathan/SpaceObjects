package timePlan;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import mvUtils.display.InputControl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * Created by M Viswanathan on 02 Apr 2015
 */
public class PlanStepsTable {
    JTable table;
    InputControl inpC;
    FlightPlan flightPlan;
    Vector<OneStep> thePlan;
    int slNo = 0;
    PlanTableModel tableModel;

    public PlanStepsTable(InputControl inpC, FlightPlan flightPlan) {
        this.inpC = inpC;
        this.flightPlan = flightPlan;
        thePlan = flightPlan.getThePlan();
        tableModel = new PlanTableModel();
        table = new JTable(tableModel);
        table.addMouseListener(new TableListener());
        TableColumnModel colModel = table.getColumnModel();
        int[] colWidth = Item.getColumnWidths();
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
        thePlan.add(step);
    }

    public void updateUI() {
        tableModel.fillTable();
    }

    public void deleteRow(int row) {
        if (row >= 0 && row < thePlan.size()) {
            thePlan.remove(row);
            updateUI();
        }
    }

    public void setOneRow(int row) {
        if (row >= 0 && row < table.getRowCount()) {
            Object[] data = thePlan.get(row).getRowData(row + 1);
            int nCol = table.getColumnCount();
            for (int col = 0; col < nCol; col++)
                tableModel.setValueAt(data[col], row, col);
        }
    }

    class PlanTableModel extends DefaultTableModel {
        PlanTableModel() {
            super(Item.getColHeader(), 0);
            fillTable();
        }

        void fillTable() {
            clearTable();
            for (int i = 0; i < thePlan.size(); i++)
                addRow(thePlan.get(i).getRowData(slNo = (i + 1)));
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
                OneStep step = thePlan.get(row);
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
