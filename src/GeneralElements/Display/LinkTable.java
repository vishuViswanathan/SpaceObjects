package GeneralElements.Display;

import Applications.ItemMovementsApp;
import GeneralElements.DarkMatter;
import GeneralElements.link.ItemLink;
import mvUtils.display.InputControl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

/**
 * Created by M Viswanathan on 10 Jan 2015
 */
public class LinkTable {
    JTable table;
    InputControl inpC;
    LinkedList<ItemLink> linkList;
    int slNo = 0;

    LinkTableModel tableModel;

    public LinkTable(InputControl inpC, LinkedList<ItemLink> linkList) {
        this.inpC = inpC;
        this.linkList = linkList;
        tableModel = new LinkTableModel();
        table = new JTable(tableModel);
        table.addMouseListener(new TableListener());
        TableColumnModel colModel = table.getColumnModel();
        int[] colWidth = ItemLink.getColumnWidths();
        for (int c = 0; c < colModel.getColumnCount(); c++)
            colModel.getColumn(c).setPreferredWidth(colWidth[c]);
    }

    public JTable getTable() {
        return table;
    }

    public void addOneRow(ItemLink itemLink) {
        slNo++;
        Object[] data = itemLink.getRowData(slNo);
        tableModel.addRow(data);
        linkList.add(itemLink);
    }

    public void updateUI() {
        tableModel.fillTable();
    }

    public void deleteRow(int row) {
        if (row >= 0 && row < linkList.size()) {
            linkList.remove(row);
            updateUI();
        }
    }

    public boolean deleteLinksOfItem(DarkMatter item) {
        boolean retVal = false;
        for (int l = 0; l < linkList.size(); l++) {
            if (linkList.get(l).linksItem(item)) {
                linkList.remove(l);
                retVal = true;
                l--;
            }
        }
        updateUI();
        return retVal;
    }

    public void setOneRow(int row) {
        if (row >= 0 && row < table.getRowCount()) {
            Object[] data = linkList.get(row).getRowData(row + 1);
            int nCol = table.getColumnCount();
            for (int col = 0; col < nCol; col++)
                tableModel.setValueAt(data[col], row, col);
        }
    }

    class LinkTableModel extends DefaultTableModel {
        LinkTableModel() {
            super(ItemLink.getColHeader(), 0);
            fillTable();
        }

        void fillTable() {
            clearTable();
            for (int i = 0; i < linkList.size(); i++)
                addRow(linkList.get(i).getRowData(slNo = (i + 1)));
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
                ItemLink thisLink = linkList.get(row);
                switch (thisLink.editLink(inpC, (Component)e.getSource())) {
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
