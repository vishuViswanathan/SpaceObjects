package GeneralElements.link;

import GeneralElements.DarkMatter;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 20 Sep 2014
 */
public class InfluenceDef extends Influence {
    double initialLenFactor; // factor (freeLength/Item Distance) at start of run
    double eCompression; // Force for 100% compression (similar to modulus of elasticity definition, but Force instead of pressure)
    double eExpansion; //

    public InfluenceDef() {

    }

    public InfluenceDef(DarkMatter item1, DarkMatter item2, double initialLenFactor, double eCompression, double eExpansion) {
        this.item1 = item1;
        this.item2 = item2;
        this.initialLenFactor = initialLenFactor;
        this.eCompression = eCompression;
        this.eExpansion = eExpansion;
        setFreeLenAndKvalues();
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("initialLenFactor", initialLenFactor));
        xmlStr.append(XMLmv.putTag("eCompression", eCompression)).append(XMLmv.putTag("eExpansion", eExpansion));
        return xmlStr;
    }

    public boolean set(String xmlStr) throws NumberFormatException {
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "initialLenFactor", 0);
        initialLenFactor = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "eCompression", 0);
        eCompression = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "eExpansion", 0);
        eExpansion = Double.valueOf(vp.val);
        setFreeLenAndKvalues();
        return true;
    }


    void setFreeLenAndKvalues() {
        Vector3d distVect = new Vector3d();
        distVect.sub(item2.status.pos, item1.status.pos);
        double distance = distVect.length();
        freeLen = distance * initialLenFactor;
        kCompression = eCompression / freeLen;
        kExpansion = eExpansion / freeLen;
    }

}

