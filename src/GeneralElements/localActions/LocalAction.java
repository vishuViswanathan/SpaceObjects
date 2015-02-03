package GeneralElements.localActions;

import GeneralElements.DarkMatter;
import GeneralElements.Item;
import mvUtils.display.InputControl;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Created by M Viswanathan on 13 Aug 2014
 */
public abstract class LocalAction implements Cloneable {
    static public enum Type {
        FLUIDFRICTION("Fluid Friction"),
        FLUIDRESISTANCE("Fluid Resistance"),
        ITEMELASTICITY("Item Elasticity"),
        FIXEDACCELERATION("Fixed Acceleration");

        private final String actionName;

        Type(String actionName) {
            this.actionName = actionName;
        }

        public String getValue() {
            return actionName;
        }

        @Override
        public String toString() {
            return actionName;
        }

        public static Type getEnum(String text) {
            Type retVal = null;
            if (text != null) {
                for (Type b : Type.values()) {
                    if (text.equalsIgnoreCase(b.actionName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

//    static public enum EditResponse{CHANGED, NOTCHANGED, DELETE};

    static public enum ColType {
        SLNO("SlNo."),
        ACTION("Action Type"),
        PARAMS("Parameters");

        private final String typeName;

        ColType(String typeName) {
            this.typeName = typeName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static ColType getEnum(String text) {
            ColType retVal = null;
            if (text != null) {
                for (ColType b : ColType.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

    public static String[] getColHeader() {
        ColType[] values = ColType.values();
        String[] colHeader = new String[values.length];
        for (int i = 0; i < colHeader.length; i++)
            colHeader[i] = "" + values[i];
        return colHeader;
    }

    public static int[] getColumnWidths() {
        ColType[] values = ColType.values();
        int[] colWidths = new int[values.length];
        for (int i = 0; i < colWidths.length; i++)
            colWidths[i] = oneColWidth(values[i]);
        return colWidths;
    }

    static int oneColWidth(ColType colType) {
        switch(colType) {
            case SLNO:
                return 30;
            case ACTION:
                return 100;
            case PARAMS:
                return 300;
        }
        return 0;
    }


    DarkMatter item;
    Type type;

    public LocalAction(Type type) {
        this.type = type;
    }

    public LocalAction(Type type, DarkMatter item) {
        this(type);
        this.item = item;
    }

    public void setItem(DarkMatter item) {
        this.item = item;
    }

    static public LocalAction getLocalAction(DarkMatter item, String xmlStr) throws Exception{
        LocalAction action = null;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "type", 0);
        Type nowType = Type.getEnum(vp.val);
        if (nowType != null) {
            switch (nowType) {
                case FLUIDRESISTANCE:
                    action = new V2Resistance(item, xmlStr);
                    break;
                case FLUIDFRICTION:
                    action = new VResistance(item, xmlStr);
                    break;
                case FIXEDACCELERATION:
                    action = new FixedAcceleration(item, xmlStr);
                    break;
            }
        }
        return action;
    }

    public Vector3d getForce() {
        return new Vector3d();
    }

    public StringBuilder dataInXML() {
        return new StringBuilder("");
    }

    public Object[] getRowData(int slNo) {
        ColType[] values = ColType.values();
        Object[] rowData = new Object[values.length];
        rowData[0] = "" + slNo;
        for (int i = 1; i < rowData.length; i++)
            rowData[i] = getOneColData(values[i]);
        return rowData;
    }

    Object getOneColData(ColType colType) {
        switch(colType) {
            case ACTION:
                return "" + type;
            case PARAMS:
                return getParamString();
 //            case DIRACCON:
//                return (bFixedForceOn) ? "Y" : "N";
        }
        return "";
    }

    abstract String getParamString();

    public abstract Item.EditResponse editAction(InputControl inpC, Component c);

    public Object clone() {
        LocalAction cloned = null;
        try {
            cloned = (LocalAction)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return cloned;
    }
}
