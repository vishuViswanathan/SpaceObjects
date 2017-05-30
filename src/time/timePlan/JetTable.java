package time.timePlan;

import Applications.ItemMovementsApp;
import GeneralElements.accessories.Jet;
import GeneralElements.accessories.JetsAndSeekers;
import mvUtils.display.InputControl;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * Created by M Viswanathan on 02 Apr 2017
 */
public class JetTable {
    JetTimeController jtController;
    JTable table;
    InputControl inpC;
    //    Vector<OneStep> thePlanSteps;
    int slNo = 0;
    JetTableModel tableModel;

    public JetTable(InputControl inpC, JetTimeController jtController) {
        this.jtController = jtController;
        this.inpC = inpC;
        tableModel = new JetTableModel();
        table = new JTable(tableModel);
        table.addMouseListener(new TableListener());
        JetItemRenderer renderer = new JetItemRenderer();
        TableColumnModel colModel = table.getColumnModel();
        int[] colWidth = JetsAndSeekers.getColumnWidths(); //OneTimeStep.getColumnWidths();
        TableColumn col;
        for (int c = 0; c < colModel.getColumnCount(); c++) {
            col = colModel.getColumn(c);
            col.setPreferredWidth(colWidth[c]);
            col.setCellRenderer(renderer);
        }
    }

    public JTable getTable() {
        return table;
    }

    public void addOneRow(JetsAndSeekers theJet) {
        slNo++;
        Object[] data = theJet.getRowData(slNo);
        tableModel.addRow(data);
        jtController.addOneJet(theJet);
    }

    public void updateUI() {
        tableModel.fillTable();
    }

    public void deleteRow(int row) {
        jtController.jets.remove(row);
        updateUI();
    }

    public void setOneRow(int row) {
        Vector<JetsAndSeekers> jets = jtController.jets;
        if (row >= 0 && row < table.getRowCount()) {
            Object[] data = jets.get(row).getRowData(row + 1);
            int nCol = table.getColumnCount();
            for (int col = 0; col < nCol; col++)
                tableModel.setValueAt(data[col], row, col);
        }
    }

    class JetTableModel extends DefaultTableModel {
        JetTableModel() {
            super(JetsAndSeekers.getColHeader(), 0);
            fillTable();
        }

        void fillTable() {
            clearTable();
            Vector<JetsAndSeekers> jets = jtController.jets;
            for (int i = 0; i < jets.size(); i++)
                addRow(jets.get(i).getRowData(slNo = (i + 1)));
        }

        void clearTable() {
            int nRow = getRowCount();
            for (int r = nRow - 1;  r >= 0; r--)
                tableModel.removeRow(r);
        }
    }

    class JetItemRenderer extends DefaultTableCellRenderer {
        public JetItemRenderer() { super(); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
//            if (isSelected) {
//                if (row == markedRow)
//                    c.setBackground(markColor);
//                else
//                    c.setBackground(defSelectedBgColor);
//            }
//            else
//                c.setBackground(normalBG);
            return c;
        }
    }


    class TableListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                int row = table.getSelectedRow();
                JetsAndSeekers theJet = jtController.jets.get(row);
                switch (JetsAndSeekers.editData(theJet, inpC, table)) {
                    case CHANGED:
                        setOneRow(row);
                        break;
                    case NOTCHANGED:
                    case CANCEL:
                        ItemMovementsApp.showMessage("no Change", table);
                        break;
                    case DELETE:
                        deleteRow(row);
                        break;
                }
            }
        }
    }
}
