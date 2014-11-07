package GeneralElements.Display;

import GeneralElements.Item;
import display.InputControl;
import display.NumberTextField;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
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
    }

     class ItemTableModel extends DefaultTableModel {
         ItemTableModel() {
             super(Item.getColHeader(), 0);
             for (int i = 0; i < allItems.size(); i++)
                 addRow(allItems.get(i).getRowData(slNo = (i + 1)));
         }
    }
}
