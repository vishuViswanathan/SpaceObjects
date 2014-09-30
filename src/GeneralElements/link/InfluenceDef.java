package GeneralElements.link;

import GeneralElements.DarkMatter;
import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 20 Sep 2014
 */
public class InfluenceDef extends Influence {
    double initialLenFactor; // factor (itemsDistance/freeLength) at start of run
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

    void setFreeLenAndKvalues() {
        Vector3d distVect = new Vector3d();
        distVect.sub(item2.status.pos, item1.status.pos);
        double distance = distVect.length();
        freeLen = distance / initialLenFactor;
        kCompression = eCompression / freeLen;
        kExpansion = eExpansion / freeLen;
    }

}

