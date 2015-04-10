package timePlan;

import Applications.ItemMovementsApp;
import GeneralElements.Display.TuplePanel;
import GeneralElements.Item;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;
import mvUtils.display.SmartFormatter;
import mvUtils.math.Vector3dMV;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by M Viswanathan on 28 Mar 2015
 */
public class OneStep implements Cloneable {
    static InputControl inpC;
    static TuplePanel directionP;
    static JRadioButton rbRelativeToVelocity;
    static Vector3d direction;
    static boolean staticDataSet = false;
    FlightPlan flightPlan;
    double duration;
    Vector3dMV forceDirection = new Vector3dMV(); // a unit Vector;
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

    public OneStep(InputControl inpC, double duration, Vector3d forceDirection, ForceSource forceSource, boolean bRelativeToVelocity) {
        initStatics(inpC);
        this.duration = duration;
        this.forceSource = forceSource;
        this.bRelativeToVelocity = bRelativeToVelocity;
        if (!bRelativeToVelocity)
            effectiveForce.scale(forceSource.effectiveForce(), forceDirection);
    }

    public OneStep(InputControl inpC, double duration, Vector3d forceDirection, ForceSource forceSource) {
        this(inpC, duration, forceDirection, forceSource, false);
    }

    public OneStep(InputControl inpC, double duration) {
        this(inpC, duration, new Vector3d(), new RocketEngine(440, 0));
    }

    public OneStep clone() {
        return new OneStep(inpC, duration, new Vector3d(forceDirection), forceSource.clone(), bRelativeToVelocity);
    }

    public OneStep(OneStep cloneFrom) {
        this.duration = cloneFrom.duration;
        this.forceDirection = new Vector3dMV(cloneFrom.forceDirection);
        this.forceSource = cloneFrom.forceSource;
        this.bRelativeToVelocity = cloneFrom.bRelativeToVelocity;
        this.effectiveForce = new Vector3d(cloneFrom.effectiveForce);

    }

    void setEffectiveForce() {
        if (!bRelativeToVelocity && (forceDirection.length() > 0)) {
            this.forceDirection = new Vector3dMV(1.0 / forceDirection.length(), forceDirection);
            effectiveForce.scale(forceSource.effectiveForce(), forceDirection);
        }
    }

    static void initStatics(InputControl inpControl) {
        if (!staticDataSet) {
            inpC = inpControl;
            direction = new Vector3d();
            directionP = new TuplePanel(inpC, -1000, 1000, "#,##0.000", "Force Direction");
            rbRelativeToVelocity = new JRadioButton("Relative to Velocity", true);
            staticDataSet = true;
        }
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
//        dlg.setSize(400, 400);
        dlg.setVisible(true);
        return dlg.getResponse();
    }

    Item.EditResponse editStep(InputControl inpC) {
        return editStep(inpC, null);
    }

    class StepDetails extends JDialog {
        InputControl inpC;
        Item.EditResponse response;
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JButton delete = new JButton("Delete");
        NumberTextField ntDuration;

        StepDetails(InputControl inpC, Component c) {
            setModal(true);
            setResizable(false);
            this.inpC = inpC;
            if (c == null)
                setLocation(100, 100);
            else
                setLocationRelativeTo(c);
            dbInit();
        }

        void dbInit() {
            ntDuration = new NumberTextField(inpC, duration, 6, false, 0, 1e6, "#,##0.000", "Sep Duration (s)");
            directionP.updateTuple(forceDirection);
            rbRelativeToVelocity.setSelected(bRelativeToVelocity);
            JPanel outerP = new JPanel(new BorderLayout());
            MultiPairColPanel jpBasic = new MultiPairColPanel("Flight Plan Step Details");
            jpBasic.addItemPair(ntDuration);
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
                        if (takeDataFromUI()) {
                            response = Item.EditResponse.CHANGED;
                            closeThisWindow();
                        }
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
            if (ntDuration.dataOK()) {
                duration = ntDuration.getData();
                forceDirection.set(directionP.getTuple3d());
                bRelativeToVelocity = rbRelativeToVelocity.isSelected();
                retVal = forceSource.fsTakeDataFromUI();
                setEffectiveForce();
            }
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
                return "Direction " + forceDirection +
                        ", (" + ((bRelativeToVelocity) ? "Relative to Velocity" : "Absolute") + ")" +
                        ", " + forceSource.dataAsString();
        }
        return "";
    }


}
