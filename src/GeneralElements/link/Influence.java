package GeneralElements.link;

import GeneralElements.DarkMatter;
import GeneralElements.ItemInterface;
import mvUtils.mvXML.XMLmv;

import javax.media.j3d.Group;
import javax.media.j3d.RenderingAttributes;
import javax.swing.*;

/**
 * Created by M Viswanathan on 24 May 2014
 */
public class Influence {
    public DarkMatter item1;
    public DarkMatter item2;
    Type type;
    double freeLen = 0;
    double kExpansion = 0;
    double kCompression = 0;
    public boolean hasDetails = true;
    boolean valid = true;
    boolean collisionOn = false;

    static public enum Type {
        INTERITEM("InterItem"),
//        ITEMCONTACT("Contact"),
//        GRAVITY("Gravity"),
        SPRING("Spring"),
        ROPE("Rope"),
        ROD("Rod"),
        GRAVITY("Gravity");

        private final String inflName;

        Type(String inflName) {
            this.inflName = inflName;
        }

        public String getValue() {
            return inflName;
        }

        @Override
        public String toString() {
            return inflName;
        }

        static public Type[] getValidTypes() {
            Type[] values = values();
            int len = values.length;
            Type[] validTypes = new Type[len - 1];
            int vLoc = 0;
            for (Type t : values)
//                if (t != GRAVITY)
                if (t != INTERITEM)
                    validTypes[vLoc++] = t;
            return validTypes;
        }

        public static Type getEnum(String text) {
            Type retVal = null;
            if (text != null) {
                for (Type b : Type.values()) {
                    if (text.equalsIgnoreCase(b.inflName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }
//    enum Types {GRAVITY, SPRING, ATTRACTION, REPULSION, CSPRING, ESPRING};
    public boolean evalForce() {return false;}

    public boolean evalForce(double deltaT, boolean bFinal) {return false;}

    public void updatePosAndVel(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception {
    }

    public boolean isValid() {
        return valid;
    }

    public Type getType() {return type;}

    public double getFreeLength(){return freeLen;}
    public double getKCompression(){return kCompression;}
    public double getKExpansion(){return kExpansion;}
    public boolean addLinksDisplay(Group grp, RenderingAttributes linkAtrib) {
        return false;
    }
    public void setStartConditions(double duration, double nowT) {}
    public void initStartForces(){}
    public void setGravityLinks(boolean bSet) {}
    public void setLocalForces() {}
    public void updateDisplay() {}

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("freeLen", freeLen));
        xmlStr.append(XMLmv.putTag("kCompression", kCompression)).append(XMLmv.putTag("kExpansion", kExpansion));
        return xmlStr;
    }

    public boolean set(String xmlStr) throws NumberFormatException {
//        ValAndPos vp;
//        vp = XMLmv.getTag(xmlStr, "freeLen", 0);
//        freeLen = Double.valueOf(vp.val);
//        vp = XMLmv.getTag(xmlStr, "kCompression", 0);
//        kCompression = Double.valueOf(vp.val);
//        vp = XMLmv.getTag(xmlStr, "kExpansion", 0);
//        kExpansion = Double.valueOf(vp.val);
        return true;
    }

    public static Influence createInfluence(DarkMatter dm1, DarkMatter dm2, Type ofType) {
        Influence inf = null;
        switch (ofType) {
//            case GRAVITY:
//                inf = new Gravity(dm1, dm2);
//                break;
            case INTERITEM:
                inf = new InterItem(dm1, dm2);
                break;
            case ROD:
                inf = new Rod(dm1, dm2, 1, 20000);
                break;
            case SPRING:
                inf = new Spring(dm1, dm2, 1, 1000);
                break;
            case ROPE:
                Rope rope = new Rope(dm1, dm2);
//                rope.addLocalAction(new FixedAcceleration());
//                rope.addLocalAction(new V2Resistance());
                inf = rope;
                break;
        }
        return inf;
    }

    public boolean takeDataFromUI() {
        return false;
    }

    public JPanel detailsPanel() {
        return null;
    }

    public String toString() {
        return "Influence type " + type + " ";
    }
}
