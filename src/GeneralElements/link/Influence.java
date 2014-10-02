package GeneralElements.link;

import GeneralElements.DarkMatter;
import mvXML.ValAndPos;
import mvXML.XMLmv;

import javax.media.j3d.Group;
import javax.media.j3d.RenderingAttributes;

/**
 * Created by M Viswanathan on 24 May 2014
 */
public class Influence {
    DarkMatter item1;
    DarkMatter item2;
    Type type;
    double freeLen = 0;
    double kExpansion = 0;
    double kCompression = 0;

    static public enum Type {
        GRAVITY("Gravity"),
        SPRING("Spring"),
        ROPE("Rope"),
        ROD("Rod");

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
    public void updatePosAndVel(double deltaT, double nowT, boolean bFinal) throws Exception {
    }

    public Type getType() {return type;}

    public double getFreeLength(){return freeLen;}
    public double getKCompression(){return kCompression;}
    public double getKExpansion(){return kExpansion;}
    public boolean addLinksDisplay(Group grp, RenderingAttributes linkAtrib) {
        return false;
    }
    public void setStartConditions() {}
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
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "freeLen", 0);
        freeLen = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "kCompression", 0);
        kCompression = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "kExpansion", 0);
        kExpansion = Double.valueOf(vp.val);
        return true;
    }

    public String toString() {
        return "Influence type " + type + " ";
    }
}
