package GeneralElements.Display;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import mvUtils.display.InputControl;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

/**
 * Created by M Viswanathan on 04 Nov 2014
 */
public class ItemTable {
    JTable table;
    ItemMovementsApp mainApp;
    InputControl inpC;
    LinkedList<Item> allItems;
    int slNo = 0;
    ItemSpace space;
    ItemTableModel tableModel;
    int markedRow = -1;

    public ItemTable(ItemMovementsApp mainApp, ItemSpace space, LinkedList<Item> allItems) {
        this.mainApp = mainApp;
        this.inpC = inpC;
        this.space = space;
        this.allItems = allItems;
        tableModel = new ItemTableModel();
        table = new JTable(tableModel);
//        table.setDefaultRenderer(new ItemRenderer());
        table.addMouseListener(new TableListener());
        TableColumnModel colModel = table.getColumnModel();
        int[] colWidth = Item.getColumnWidths();
        TableColumn col;
        TableCellRenderer  renderer = new ItemRenderer();
        for (int c = 0; c < colModel.getColumnCount(); c++) {
            col = colModel.getColumn(c);
            col.setPreferredWidth(colWidth[c]);
            col.setCellRenderer(renderer);
        }
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

    Color defSelectedBgColor = Color.GRAY;
    Color markColor = Color.RED;
    Color normalBG = Color.WHITE;

    class ItemRenderer extends DefaultTableCellRenderer {
        public ItemRenderer() { super(); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                if (row == markedRow)
                    c.setBackground(markColor);
                else
                    c.setBackground(defSelectedBgColor);
            }
            else
                c.setBackground(normalBG);
            return c;
        }
    }

    class TableListener extends MouseAdapter {
        @Override
        public void mouseReleased(MouseEvent e) {
            int r = table.rowAtPoint(e.getPoint());
            if (r >= 0 && r < table.getRowCount()) {
                table.setRowSelectionInterval(r, r);
            } else {
                table.clearSelection();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int event = e.getButton();
            switch (event) {
                case MouseEvent.BUTTON3:
                    markedRow = table.getSelectedRow();
                    break;
                 case MouseEvent.BUTTON1:
                    int row = table.getSelectedRow();
                    Item thisItem = allItems.get(row);
                    if (markedRow >= 0){
                        if (markedRow == row)
                            mainApp.showError("Copy on to itself! " + markedRow);
                        else {
                            if (thisItem.takeBasicFrom(allItems.get(markedRow))) {
                                setOneRow(row);
                            }
                            else
                                mainApp.showError("They are not similar objects");
                            markedRow = -1;
                        }
                    }
                    else {
                        switch (thisItem.editItem("", inpC, (Component) e.getSource())) {
                            case CHANGED:
                                setOneRow(row);
                                break;
                            case NOTCHANGED:
//                                ItemMovementsApp.showMessage("no Change");
                                break;
                            case DELETE:
                                ItemMovementsApp.showMessage("DELETE WHAT ABOUT LINKS?");
                                space.removeLinksOf(thisItem);
                                deleteRow(row);
                                break;
                        }
                    }
                    markedRow = -1;
                    break;
            }
        }
    }
}
