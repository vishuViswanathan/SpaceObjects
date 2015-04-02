package timePlan;

import Applications.ItemMovementsApp;
import GeneralElements.Display.TuplePanel;
import GeneralElements.Item;
import mvUtils.SmartFormatter;
import mvUtils.Vector3dMV;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by M Viswanathan on 28 Mar 2015
 */
public class OneStep {
    static InputControl inpC;
    static TuplePanel directionP;
    static JRadioButton rbRelativeToVelocity;
    static Vector3d direction;
    boolean staticDataSet = false;
    FlightPlan flightPlan;
    double duration;
    Vector3dMV forceDirection; // a unit Vector;
    boolean bRelativeToVelocity = false;
    Vector3d effectiveForce = new Vector3d();
    ForceSource forceSource;
    double startTime; // these times are of the FlightPlan and not absolute
    double endTime;

    static public enum PlanColType {
        SLNO("SlNo."),
        DETAILS("Details");

        private final String typeName;

        PlanColType(String typeName) {
            this.typeName = typeName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static PlanColType getEnum(String text) {
            PlanColType retVal = null;
            if (text != null) {
                for (PlanColType b : PlanColType.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

    public OneStep(double duration, Vector3d forceDirection, ForceSource forceSource, boolean bRelativeToVelocity) {
        this.duration = duration;
        this.forceDirection = new Vector3dMV(1.0 / forceDirection.length(), forceDirection);
        this.forceSource = forceSource;
        this.bRelativeToVelocity = bRelativeToVelocity;
        if (!bRelativeToVelocity)
            effectiveForce.scale(forceSource.effectiveForce(), forceDirection);
    }

    public OneStep(double duration, Vector3d forceDirection, ForceSource forceSource) {
        this(duration, forceDirection, forceSource, false);
    }

    static void initStatics(InputControl inpControl) {
        inpC = inpControl;
        direction = new Vector3d();
        directionP = new TuplePanel(inpC, 10000, 1000, "#,##0.000", "Force Direction");
        rbRelativeToVelocity = new JRadioButton("Relative to Velocity", true);
    }

    public void setFlightPlan(FlightPlan flightPlan) {
        this.flightPlan = flightPlan;
    }

    void setStartTime(double startTime) {
        this.startTime = startTime;
        this.endTime = startTime + duration;
    }

    Vector3d getEffectiveForce() {
        if (bRelativeToVelocity) { // else effectiveForce is already set
            effectiveForce.scale(forceSource.effectiveForce(), flightPlan.item.status.velocity);
        }
        return effectiveForce;
    }

    Vector3d getEffectiveForce(Vector3d force) {
        force.set(getEffectiveForce());
        return force;
    }

    Item.EditResponse editStep(InputControl inpC, Component c) {
        StepDetails dlg = new StepDetails(inpC, c);
            dlg.setVisible(true);
            return dlg.getResponse();
    }

    class StepDetails extends JDialog {
        InputControl inpC;
        Item.EditResponse response;
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JButton delete = new JButton("Delete");

        StepDetails(InputControl inpC, Component c) {
            setModal(true);
            setResizable(false);
            this.inpC = inpC;
            dbInit();
            if (c == null)
                setLocation(100, 100);
            else
                setLocationRelativeTo(c);
            dbInit();
        }

        void dbInit() {
            directionP.updateTuple(direction);
            rbRelativeToVelocity.setSelected(bRelativeToVelocity);
            JPanel outerP = new JPanel(new BorderLayout());
            MultiPairColPanel jpBasic = new MultiPairColPanel("Flight Plan Step Details");
            jpBasic.addItemPair(directionP.getTitle(), directionP);
            jpBasic.addItemPair("", rbRelativeToVelocity);
            jpBasic.addItem(forceSource.fsDetails());
            jpBasic.addBlank();
            JPanel buttPanel = new JPanel(new BorderLayout());
            buttPanel.add(delete, BorderLayout.WEST);
            buttPanel.add(cancel, BorderLayout.CENTER);
            buttPanel.add(ok, BorderLayout.EAST);
            jpBasic.addItem(buttPanel);
            outerP.add(jpBasic);
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == ok) {
                        if (takeDataFromUI())
                            closeThisWindow();
                    } else if (src == delete) {
                        if (ItemMovementsApp.decide("Deleting Plan Step  ", "Do you want to DELETE this Step?")) {
                            response = Item.EditResponse.DELETE;
                            closeThisWindow();
                        }
                    } else {
                        response = Item.EditResponse.NOTCHANGED;
                        closeThisWindow();
                    }
                }
            };
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
            direction.set(directionP.getTuple3d());
            bRelativeToVelocity = rbRelativeToVelocity.isSelected();
            retVal = forceSource.fsTakeDataFromUI();
            return retVal;
        }
        Item.EditResponse getResponse() {
            return response;
        }
    }

    public static String[] getColHeader() {
        PlanColType[] values = PlanColType.values();
        String[] colHeader = new String[values.length];
        for (int i = 0; i < colHeader.length; i++)
            colHeader[i] = "" + values[i];
        return colHeader;
    }

    public static int[] getColumnWidths() {
        PlanColType[] values = PlanColType.values();
        int[] colWidths = new int[values.length];
        for (int i = 0; i < colWidths.length; i++)
            colWidths[i] = oneColWidth(values[i]);
        return colWidths;
    }

    static int oneColWidth(PlanColType colType) {
        switch(colType) {
            case SLNO:
                return 30;
            case DETAILS:
                return 630;
        }
        return 0;
    }

    public Object[] getRowData(int slNo) {
        PlanColType[] values = PlanColType.values();
        Object[] rowData = new Object[values.length];
        rowData[0] = "" + slNo;
        for (int i = 1; i < rowData.length; i++)
            rowData[i] = getOneColData(values[i]);
        return rowData;
    }

    Object getOneColData(PlanColType colType) {
        SmartFormatter fmt = new SmartFormatter(6);
        switch(colType) {
            case DETAILS:
                return "Direction " + direction +
                        ", (" + ((bRelativeToVelocity) ? "Relative to Velocity" : "Absolute") + ")" +
                        ", " + forceSource.dataAsString();
        }
        return "";
    }


}
