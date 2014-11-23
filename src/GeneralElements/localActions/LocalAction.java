package GeneralElements.localActions;

import GeneralElements.DarkMatter;
import mvUtils.mvXML.*;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 13 Aug 2014
 */
public class LocalAction implements Cloneable {
    static public enum Type {
        FLUIDFRICTION("FluidFriction"),
        FLUIDRESISTANCE("FluidResistance"),
        ITEMELASTICITY("ItemElasticity"),
        FIXEDACCELERATION("FixedAccel");

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
