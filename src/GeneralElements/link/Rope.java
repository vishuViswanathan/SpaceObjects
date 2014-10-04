package GeneralElements.link;

import GeneralElements.DarkMatter;

/**
 * Created by M Viswanathan on 29 May 2014
 */
public class Rope  extends LinkWithMass  {
    double ropeDia;

    public Rope(DarkMatter item1, DarkMatter item2, double freeLen, double massPerM, double eExpansion, int nElements) {
        super(item1, item2, freeLen, massPerM, eExpansion, nElements);
    }

    public Rope(DarkMatter item1, DarkMatter item2, double freeLen, double massPerM, double ropeDia,
                double eExpansion, int nElements) {
        super(item1, item2, freeLen, massPerM, eExpansion, nElements);
        this.ropeDia = ropeDia;
        setAreas();
    }

    void setAreas() {
        surfAreaPerM = Math.PI * ropeDia;
        projectedAreaPerM = ropeDia;
    }
}

