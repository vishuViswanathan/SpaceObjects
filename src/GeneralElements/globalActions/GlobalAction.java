package GeneralElements.globalActions;
import GeneralElements.DarkMatter;
import GeneralElements.Item;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by M Viswanathan on 25 Jan 2015
 */
public abstract class GlobalAction {
    static public enum Type {
        FIXEDACCELERATION("Fixed Acceleration"),
        FLUIDRESISTANCEg("Global V2Resistance"),
        FLUIDFRICTIONg("Global VResistance"),
        WIND("Wind");

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
    public boolean bUsed = false;
    //    DarkMatter item;
    Type type;

    public GlobalAction(Type type) {
        this.type = type;
    }

    public JComponent editPanel(final InputControl inpC) {

        MultiPairColPanel outerP = new MultiPairColPanel("" + type);
        final JRadioButton rbUsed = new JRadioButton("Enable", bUsed);
        rbUsed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bUsed = rbUsed.isSelected();
            }
        });
        final JButton pbEdit = new JButton("Edit");
        pbEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editAction(inpC, pbEdit);
            }
        });
        outerP.addItemPair(pbEdit, rbUsed);
        return outerP;

    }

    static public GlobalAction getGlobalAction(Type nowType){
        GlobalAction action = null;
        if (nowType != null) {
            switch (nowType) {
                case FLUIDRESISTANCEg:
                    action = new V2ResistanceG();
                    break;
                case FLUIDFRICTIONg:
                    action = new VResistanceG();
                    break;
                case FIXEDACCELERATION:
                    action = new FixedAccelerationG();
                    break;
                case WIND:
                    action = new Wind();
                    break;
            }
        }
        return action;
    }

    static public GlobalAction getGlobalAction(String xmlStr) throws Exception{
        GlobalAction action = null;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "type", 0);
        Type nowType = Type.getEnum(vp.val);
        if (nowType != null) {
            switch (nowType) {
                case FLUIDRESISTANCEg:
                    action = new V2ResistanceG(xmlStr);
                    break;
                case FLUIDFRICTIONg:
                    action = new VResistanceG(xmlStr);
                    break;
                case FIXEDACCELERATION:
                    action = new FixedAccelerationG(xmlStr);
                    break;
                case WIND:
                    action = new Wind(xmlStr);
                    break;
            }
        }
        return action;
    }

    protected boolean setValues(String xmlStr) throws Exception {
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "bUsed", 0);
        bUsed =(vp.val.equals("1"));
        return true;
    }

    public StringBuilder dataInXML() {
        return new StringBuilder(XMLmv.putTag("bUsed" , bUsed));
    }


    public Vector3d getForce(DarkMatter item) {
//        return new Vector3d();
        Vector3d force = item.tempForce;
        force.set(0, 0, 0);
        return force;
    }

    abstract String getParamString();

    public abstract Item.EditResponse editAction(InputControl inpC, Component c);
}
