package GeneralElements.Display;

import Applications.ItemMovementsApp;
import GeneralElements.DarkMatter;
import GeneralElements.Item;
import mvUtils.display.InputControl;
import mvUtils.display.NumberTextField;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

/**
 * Created by M Viswanathan on 04 Nov 2014
 */
public class ItemTable {
    JTable table;
    InputControl inpC;
    LinkedList<Item> allItems;
    JTextField jName;
    NumberTextField ntMass;
    NumberTextField ntDia;
    JTextField jtPos;
    JRadioButton jrStatic;
    JTextField jtVelocity;
    JRadioButton jrDirAccON;
    int slNo = 0;

    ItemTableModel tableModel;

    public ItemTable(InputControl inp, LinkedList<Item> allItems) {
        this.allItems = allItems;
        tableModel = new ItemTableModel();
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

    public void addOneRow(Item item) {
        slNo++;
        Object[] data = item.getRowData(slNo);
        tableModel.addRow(data);
        allItems.add(item);
    }

    public void updateUI() {
        tableModel.fillTable();
    }

    public void deleteRow(int row) {
        if (row >= 0 && row < allItems.size()) {
            allItems.remove(row);
            updateUI();
//            tableModel.fillTable();
        }
    }

    public void setOneRow(int row) {
        if (row >= 0 && row < table.getRowCount()) {
            Object[] data = allItems.get(row).getRowData(row + 1);
            int nCol = table.getColumnCount();
            for (int col = 0; col < nCol; col++)
                tableModel.setValueAt(data[col], row, col);
        }
    }

     class ItemTableModel extends DefaultTableModel {
         ItemTableModel() {
             super(Item.getColHeader(), 0);
             fillTable();
//             for (int i = 0; i < allItems.size(); i++)
//                 addRow(allItems.get(i).getRowData(slNo = (i + 1)));
         }

         void fillTable() {
             clearTable();
             for (int i = 0; i < allItems.size(); i++)
                 addRow(allItems.get(i).getRowData(slNo = (i + 1)));
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
                Item thisItem = allItems.get(row);
                switch (thisItem.editItem(inpC)) {
                    case CHANGED:
                        setOneRow(row);
                        break;
                    case NOTCHANGED:
                        ItemMovementsApp.showMessage("no Change");
                        break;
                    case DELETE:
                        ItemMovementsApp.showMessage("DELETE WHAT ABOUT LINKS?");
                        deleteRow(row);
                        break;
                }
            }
        }
    }
}
