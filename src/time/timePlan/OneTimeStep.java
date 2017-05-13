package time.timePlan;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;
import mvUtils.display.SmartFormatter;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by M Viswanathan on 18 Mar 2017
 */
public class OneTimeStep {
    public enum StepAction {
        ALIGNTOVELOCITY("Align To Velocity"),
        ALIGNCOUNTERTOVELOCITY("Align Counter To Velocity"),
        FIREJET("Fire Jet");

        private final String typeName;

        StepAction(String actionName) {
            this.typeName = actionName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static StepAction getEnum(String text) {
            StepAction retVal = null;
            if (text != null) {
                for (StepAction b : StepAction.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

    static public enum TimeStepColType {
        SLNO("Step"),
        ACTION("Action"),
        STTIME("StartTime(s)"),
        DURATION("Duration(s)");

        private final String typeName;

        TimeStepColType(String typeName) {
            this.typeName = typeName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static TimeStepColType getEnum(String text) {
            TimeStepColType retVal = null;
            if (text != null) {
                for (TimeStepColType b : TimeStepColType.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

    public StepAction stepAction = StepAction.FIREJET;
    public double startTime;
    public double endTime;

    public OneTimeStep(String xmlStr) {
       takeFromXML(xmlStr);
    }

    public OneTimeStep(double startTime, double duration) {
        setValues(startTime, duration);
    }

    private void setValues(double startTime, double duration) {
        this.startTime = startTime;
        this.endTime = startTime + duration;
    }

    public static String[] getColHeader() {
        TimeStepColType[] values = TimeStepColType.values();
        String[] colHeader = new String[values.length];
        for (int i = 0; i < colHeader.length; i++)
            colHeader[i] = "" + values[i];
        return colHeader;
    }

    public static int[] getColumnWidths() {
        TimeStepColType[] values = TimeStepColType.values();
        int[] colWidths = new int[values.length];
        for (int i = 0; i < colWidths.length; i++)
            colWidths[i] = oneColWidth(values[i]);
        return colWidths;
    }

    static int oneColWidth(TimeStepColType colType) {
        switch (colType) {
            case SLNO:
                return 30;
            case ACTION:
                return 60;
            case STTIME:
                return 100;
            case DURATION:
                return 100;
        }
        return 0;
    }

    public Object[] getRowData(int slNo) {
        TimeStepColType[] values = TimeStepColType.values();
        Object[] rowData = new Object[values.length];
        rowData[0] = "" + slNo;
        for (int i = 1; i < rowData.length; i++)
            rowData[i] = getOneColData(values[i]);
        return rowData;
    }

    Object getOneColData(TimeStepColType colType) {
        SmartFormatter fmt = new SmartFormatter(6);
        switch (colType) {
            case ACTION:
                return "" + stepAction;
            case STTIME:
                return String.format("%10.3f", startTime);
            case DURATION:
                return String.format("%10.3f", endTime - startTime);
        }
        return "";
    }

    Item.EditResponse editStep(InputControl inpC, Component c) {
        StepDetails dlg = new StepDetails(inpC);
//        dlg.setSize(400, 400);
        if (c == null)
            dlg.setLocation(100, 100);
        else
            dlg.setLocationRelativeTo(c);
        dlg.setVisible(true);
        return dlg.getResponse();
    }

    Item.EditResponse editStep(InputControl inpC) {
        return editStep(inpC, null);
    }

    class StepDetails extends JDialog {
        InputControl inpC;
        double duration;
        Item.EditResponse response;
        JComboBox<StepAction> cbStepAction;
        NumberTextField ntStartTime;
        NumberTextField ntDuration;
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JButton delete = new JButton("Delete");

        StepDetails(InputControl inpC) {
            setModal(true);
            setResizable(false);
            this.inpC = inpC;
//            if (c == null)
//                setLocation(100, 100);
//            else
//                setLocationRelativeTo(c);
            dbInit();
        }

        void dbInit() {
            duration = endTime - startTime;
            cbStepAction = new JComboBox<>(StepAction.values());
            cbStepAction.setSelectedItem(stepAction);
            ntStartTime = new NumberTextField(inpC, startTime, 6, false, 0, Double.MAX_VALUE, "#,##0.000", "Start Time (s)");
            ntDuration = new NumberTextField(inpC, duration, 6, false, 0, 1e6, "#,##0.000", "Sep Duration (s)");
            JPanel outerP = new JPanel(new BorderLayout());
            MultiPairColPanel jpBasic = new MultiPairColPanel("Time Plan Step Details");
            jpBasic.addItemPair("Select Action", cbStepAction);
            jpBasic.addItemPair(ntStartTime);
            jpBasic.addItemPair(ntDuration);
            jpBasic.addBlank();
            JPanel buttPanel = new JPanel(new BorderLayout());
            buttPanel.add(delete, BorderLayout.WEST);
            buttPanel.add(cancel, BorderLayout.CENTER);
            buttPanel.add(ok, BorderLayout.EAST);
            jpBasic.addItem(buttPanel);
            outerP.add(jpBasic);
            ActionListener li = (e -> {
                Object src = e.getSource();
                if (src == ok) {
                    if (takeDataFromUI()) {
                        response = Item.EditResponse.CHANGED;
                        closeThisWindow();
                    }
                } else if (src == delete) {
                    if (ItemMovementsApp.decide("Deleting Time Step  ", "Do you want to DELETE this Step?")) {
                        response = Item.EditResponse.DELETE;
                        closeThisWindow();
                    }
                } else {
                    response = Item.EditResponse.NOTCHANGED;
                    closeThisWindow();
                }
            }
            );
            ok.addActionListener(li);
            cancel.addActionListener(li);
            delete.addActionListener(li);
            add(outerP);
            pack();
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }

        boolean takeDataFromUI() {
            boolean retVal = false;
            if (ntDuration.dataOK()) {
                duration = ntDuration.getData();
                if (duration > 0) {
                    if (ntStartTime.dataOK()) {
                        stepAction = (StepAction)cbStepAction.getSelectedItem();
                        startTime = ntStartTime.getData();
                        endTime = startTime + duration;
                        retVal = true;
                    }
                }
            }
            return retVal;
        }

        Item.EditResponse getResponse() {
            return response;
        }
    }

    public StringBuilder dataInXML() {
        return (new StringBuilder(XMLmv.putTag("stepAction", stepAction.toString())).
                append(XMLmv.putTag("startTime", startTime))).
                append(XMLmv.putTag("duration", (endTime - startTime)));
    }

    private boolean takeFromXML(String xmlStr) {
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "stepAction", 0);
        if (vp.val.length() > 0)
            stepAction = StepAction.getEnum(vp.val);
        vp = XMLmv.getTag(xmlStr, "startTime", vp.endPos);
        double stTime = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "duration", vp.endPos);
        double duration = Double.valueOf(vp.val);
        setValues(stTime, duration);
        return true;
    }
}
