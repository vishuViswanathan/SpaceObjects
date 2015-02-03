package GeneralElements.Display;

import Applications.ItemMovementsApp;
import GeneralElements.DarkMatter;
import GeneralElements.Item;
import GeneralElements.localActions.FixedAcceleration;
import GeneralElements.localActions.LocalAction;
import GeneralElements.localActions.V2Resistance;
import GeneralElements.localActions.VResistance;
import mvUtils.display.FramedPanel;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;

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
 * Created by M Viswanathan on 23 Nov 2014
 */
public class LocalActionsTable {
    JTable table;
    InputControl inpC;
    Vector<LocalAction> localActions;
    int slNo = 0;
    public DarkMatter item;
    ActionTableModel tableModel;

    public LocalActionsTable(DarkMatter item, InputControl inpC) {
        this.item = item;
        this.localActions = item.getLocalActions();
        this.inpC = inpC;
        tableModel = new ActionTableModel();
        table = new JTable(tableModel);
        table.addMouseListener(new TableListener());
        TableColumnModel colModel = table.getColumnModel();
        int[] colWidth = LocalAction.getColumnWidths();
        for (int c = 0; c < colModel.getColumnCount(); c++)
            colModel.getColumn(c).setPreferredWidth(colWidth[c]);
    }

    JTable getTable() {
        return table;
    }

    boolean addNewAction(Component c) {
        ActionBasic dlg = new ActionBasic();
        dlg.setLocationRelativeTo(c);
        dlg.setVisible(true);
        LocalAction newAction;
        boolean retVal = false;
        if (dlg.selOk) {
            LocalAction.Type type = dlg.getActionType();
            switch (type) {
                case FIXEDACCELERATION:
                    newAction = new FixedAcceleration(item);
                    break;
                case FLUIDFRICTION:
                    newAction = new VResistance(item);
                    break;
                case FLUIDRESISTANCE:
                    newAction = new V2Resistance(item);
                    break;
                case ITEMELASTICITY:
                default:
                    newAction = null;
                    break;
            }
            editResponse = Item.EditResponse.CHANGED;
            if (newAction.editAction(inpC, c) == Item.EditResponse.CHANGED)  {
                item.addLocalAction(newAction);
                ItemMovementsApp.showMessage("new Action Added");
                retVal = true;
            }
        }
        return retVal;
    }

    public Component getLocalActionPanel() {
        FramedPanel fp = new FramedPanel(new BorderLayout());
        final JButton pbAddAction = new JButton("Add new Local Action");
        pbAddAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (addNewAction(pbAddAction))
                    updateUI();
            }
        });
        fp.add(pbAddAction, BorderLayout.NORTH);
        JScrollPane sP = new JScrollPane();
        sP.setPreferredSize(new Dimension(400, 100));
        sP.setViewportView(getTable());
        fp.add(sP, BorderLayout.CENTER);
        return fp;
    }

    public void addOneRow(LocalAction action) {
        slNo++;
        Object[] data = action.getRowData(slNo);
        tableModel.addRow(data);
        localActions.add(action);
    }

    public void updateUI() {
        tableModel.fillTable();
    }

    public void deleteRow(int row) {
        if (row >= 0 && row < localActions.size()) {
            localActions.remove(row);
            updateUI();
//            tableModel.fillTable();
        }
    }

    public void setOneRow(int row) {
        if (row >= 0 && row < table.getRowCount()) {
            Object[] data = localActions.get(row).getRowData(row + 1);
            int nCol = table.getColumnCount();
            for (int col = 0; col < nCol; col++)
                tableModel.setValueAt(data[col], row, col);
        }
    }

    class ActionTableModel extends DefaultTableModel {
        ActionTableModel() {
            super(LocalAction.getColHeader(), 0);
            fillTable();
        }

        void fillTable() {
            clearTable();
            for (int i = 0; i < localActions.size(); i++)
                addRow(localActions.get(i).getRowData(slNo = (i + 1)));
        }

        void clearTable() {
            int nRow = getRowCount();
            for (int r = nRow - 1;  r >= 0; r--)
                tableModel.removeRow(r);
        }
    }

    Item.EditResponse editResponse = Item.EditResponse.NOTCHANGED;

    public Item.EditResponse getEditResponse() {
        return editResponse;
    }

    class TableListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                int row = table.getSelectedRow();
                LocalAction thisAction = localActions.get(row);
                switch (thisAction.editAction(inpC, (Component)e.getSource())) {
                    case CHANGED:
                        setOneRow(row);
                        editResponse = Item.EditResponse.CHANGED;
                        break;
                    case NOTCHANGED:
//                        ItemMovementsApp.showMessage("no Change");
                        break;
                    case DELETE:
                        deleteRow(row);
                        editResponse = Item.EditResponse.CHANGED;
                        break;
                }
            }
        }
    }

    class ActionBasic extends JDialog {
        JButton proceed = new JButton("Proceed");
        JButton cancel = new JButton("Cancel");
        JComboBox<LocalAction.Type> cbType = new JComboBox<LocalAction.Type>(LocalAction.Type.values());
        boolean selOk = false;
        LocalAction.Type type = LocalAction.Type.FIXEDACCELERATION;
        ActionBasic() {
            setModal(true);
            setTitle("New Local Action");
            dbInit();
        }

        void dbInit() {
            MultiPairColPanel jp = new MultiPairColPanel("New Local Action");
            jp.addItemPair("Action Type ", cbType);
            jp.addBlank();
            jp.addItemPair(cancel, proceed);
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == proceed) {
                        if (takeValuesFromUI())
                            closeThisWindow();
                    } else  {
                        selOk = false;
                        closeThisWindow();
                    }
                }
            };
            proceed.addActionListener(li);
            cancel.addActionListener(li);
            add(jp);
            pack();
        }

        boolean takeValuesFromUI() {
            type = (LocalAction.Type)cbType.getSelectedItem();
            selOk = true;
            return selOk;
        }

        LocalAction.Type getActionType() {
            if (selOk)
                return type;
            else
                return null;
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }
    }

}
