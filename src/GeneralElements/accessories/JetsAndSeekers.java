package GeneralElements.accessories;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.Surface;
import mvUtils.display.DataWithStatus;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;
import time.timePlan.OneJetPlan;
import time.timePlan.OneTimeStep;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by mviswanathan on 13-05-2017.
 */
public abstract class JetsAndSeekers {
    public enum JetTableColType {
        SLNO("SlNo."),
        ELEMENT("Element Type"),
        DETAILS("Details");

        private final String typeName;

        JetTableColType(String typeName) {
            this.typeName = typeName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static JetTableColType getEnum(String text) {
            JetTableColType retVal = null;
            if (text != null) {
                for (JetTableColType b : JetTableColType.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

    public enum ElementType {
        JET("Jet"),
        SEEKER("Seeker"),
        ALIGNER("Aligner");

        private final String typeName;

        ElementType(String typeName) {
            this.typeName = typeName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static ElementType getEnum(String text) {
            ElementType retVal = null;
            if (text != null) {
                for (ElementType b : ElementType.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }
    Item item;
    ElementType elementType;
    String name;
    boolean active = false;
    OneTimeStep theStep;
    public OneJetPlan thePlan;

    protected JetsAndSeekers(Item item, ElementType elementType) {
        this(item, "UNKNOWN", elementType);
    }

    protected JetsAndSeekers(Item item, String name, ElementType elementType) {
        this.name = name;
        this.item = item;
        this.elementType = elementType;
        thePlan = new OneJetPlan(this);
    }

    public static DataWithStatus<JetsAndSeekers> getJetsAndSeekers(Item item, String xmlStr) {
        DataWithStatus<JetsAndSeekers> retVal = new DataWithStatus<>();
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "elementType", 0);
        ElementType type  = ElementType.getEnum(vp.val);
        type = (type == null) ? ElementType.JET : type;
        switch (type) {
            case JET:
                retVal.setValue(new Jet(item, xmlStr));
                break;
            case SEEKER:
                ItemMovementsApp.showError("JetsAndSeekers.111: Not ready for " + ElementType.ALIGNER );
                break;
            case ALIGNER:
                retVal.setValue(new Aligner(item, xmlStr));
                break;
        }
        return retVal;
    }

    public static String[] getColHeader() {
        JetTableColType[] values = JetTableColType.values();
        String[] colHeader = new String[values.length];
        for (int i = 0; i < colHeader.length; i++)
            colHeader[i] = "" + values[i];
        return colHeader;
    }

    public static int[] getColumnWidths() {
        JetTableColType[] values = JetTableColType.values();
        int[] colWidths = new int[values.length];
        for (int i = 0; i < colWidths.length; i++)
            colWidths[i] = oneColWidth(values[i]);
        return colWidths;
    }

    static int oneColWidth(JetTableColType colType) {
        switch (colType) {
            case SLNO:
                return 30;
            case ELEMENT:
                return 60;
            case DETAILS:
                return 630;
        }
        return 0;
    }

    public void noteTimePlan(OneJetPlan thePlan) {
        this.thePlan = thePlan;
    }

    public void setActive(boolean active, OneTimeStep theStep, double duration) {
        this.active = active;
        this.theStep = theStep;
    }

    public boolean isActive() {
        return active;
    }

    public void addEffect() {
    }

    public Object[] getRowData(int slNo) {
        JetTableColType[] values = JetTableColType.values();
        Object[] rowData = new Object[values.length];
        rowData[0] = "" + slNo;
        for (int i = 1; i < rowData.length; i++)
            rowData[i] = getOneColData(values[i]);
        return rowData;
    }

    public Object getOneColData(JetTableColType colType) {
        switch(colType) {
            case ELEMENT:
                return elementType.toString();
            case DETAILS:
                return name + " : " + detailsString();
        }
        return "";
    }

    public static Item.EditResponse editData(JetsAndSeekers theJet, InputControl inpC, Component c) {
        Item.EditResponse retVal = Item.EditResponse.CANCEL;
        switch (theJet.elementType) {
            case JET:
                retVal = ((Jet)theJet).editData(inpC, c);
                break;
            case SEEKER:
                ItemMovementsApp.showError("JetsAndSeekers.190: Not ready for " + ElementType.SEEKER, c );
                break;
            case ALIGNER:
                retVal = ((Aligner)theJet).editData(inpC, c);
                break;
        }
        return retVal;
    }

    static public JetsAndSeekers getNewAccessory(Item item, Component theParent) {
        JetsAndSeekers theAccessory = null;
        AccessoryBasic dlg = new AccessoryBasic();
        dlg.setLocationRelativeTo(theParent);
        dlg.setVisible(true);
        if (dlg.getResponse() == Item.EditResponse.OK) {
            ElementType selectedType = dlg.getSelectedType();
            switch (selectedType) {
                case JET:
                    theAccessory = new Jet(item);
                    break;
                case SEEKER:
                    ItemMovementsApp.showError("JetsAndSeekers.216: Not ready for " + ElementType.SEEKER);
                    break;
                case ALIGNER:
                    theAccessory = new Aligner(item);
                    break;
            }
        }
        return theAccessory;
    }

    static class AccessoryBasic extends JDialog {
        JComboBox jcItem = new JComboBox(ElementType.values());
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ItemSpace theSpace;
        String theName;
        Item.EditResponse response = Item.EditResponse.CANCEL;

        AccessoryBasic() {
            setModal(true);
            this.theSpace = theSpace;
            this.theName = theName;
            jcItem.setSelectedItem(Item.ItemType.SPHERE);
            setTitle("Selection Object Type");
            MultiPairColPanel jp = new MultiPairColPanel("Selection Object Type");
            jp.addItemPair("Selected Type", jcItem);
            jp.addBlank();
            jp.addItemPair(cancel, ok);
            add(jp);
            pack();
            ActionListener li = e -> {
                Object src = e.getSource();
                if (src == ok)
                    response = Item.EditResponse.OK;
                closeThisWindow();
            };
            ok.addActionListener(li);
            cancel.addActionListener(li);
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }

        ElementType getSelectedType() {
            return (ElementType) jcItem.getItemAt(jcItem.getSelectedIndex());
        }

        Item.EditResponse getResponse() {
            return response;
        }
    }

    protected String detailsString() {
        return "";
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("elementType", elementType.toString()));
        return xmlStr;
    }

}
