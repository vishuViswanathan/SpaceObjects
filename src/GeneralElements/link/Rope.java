package GeneralElements.link;

import GeneralElements.DarkMatter;
import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;

import javax.swing.*;

/**
 * Created by M Viswanathan on 29 May 2014
 */
public class Rope  extends LinkWithMass  {
    double ropeDia;
    static double defRopeDia = 0.0157079632679489;

    public Rope(DarkMatter item1, DarkMatter item2, double freeLen, double massPerM, double eExpansion, int nElements) {
        super(item1, item2, freeLen, massPerM, eExpansion, nElements);
    }

    public Rope(DarkMatter item1, DarkMatter item2, double freeLen, double massPerM, double ropeDia,
                double eExpansion, int nElements) {
        super(item1, item2, freeLen, massPerM, eExpansion, nElements);
        this.ropeDia = ropeDia;
        setAreas();
    }

    public Rope(DarkMatter item1, DarkMatter item2, double initialLenFactor, double eExpansion) {
        super(item1, item2, initialLenFactor, eExpansion);
        this.ropeDia = defRopeDia;
        setAreas();
    }


    void setAreas() {
        surfAreaPerM = Math.PI * ropeDia;
        projectedAreaPerM = ropeDia;
    }

    NumberTextField nteCompression;
    NumberTextField ntLenFactor;
    NumberTextField ntRopeDia;
    NumberTextField ntnElements;
    NumberTextField ntMassPerM;
    boolean uiReady = false;

    @Override
    public JPanel detailsPanel() {
        MultiPairColPanel outerP = new MultiPairColPanel("Details of " + this);
        nteCompression = new NumberTextField(null, eCompression, 6, false, 1, 1e10, "#,##0", "Elasticity (Force in Newton for 100%)");
        ntLenFactor = new NumberTextField(null, initialLenFactor, 6, false, 02, 10000, "#,##0.000", "Free Length Factor (Free Length/ distance");
        ntnElements = new NumberTextField(null, nElements, 6, true, 1, 100, "#,##0", "Subdivided Rope elements");
        ntRopeDia = new NumberTextField(null, ropeDia, 6, false, 0.001, 5, "#,##0.000", "Rope Diameter (m)");
        ntMassPerM = new NumberTextField(null, massPerM, 6, false, 0.001, 1000, "#,##0.000", "Mass per Unit Length (kg/m)");
        outerP.addItemPair(ntLenFactor);
        outerP.addItemPair(ntnElements);
        outerP.addItemPair(nteCompression);
        outerP.addItemPair(ntRopeDia);
        outerP.addItemPair(ntMassPerM);
        uiReady = true;
        return outerP;
    }

    @Override
    public boolean takeDataFromUI() {
        boolean  retVal = true;
        if (uiReady) {
            initialLenFactor = ntLenFactor.getData();
            eCompression = nteCompression.getData();
            eExpansion = eCompression;
            nElements = (int)ntnElements.getData();
            ropeDia = ntRopeDia.getData();
            massPerM = ntMassPerM.getData();
            setFreeLenAndKvalues();
            setAreas();
            noteBasicData(massPerM, nElements);
            setAllElements();
        }
        else
            retVal = false;
        return retVal;
    }
}

