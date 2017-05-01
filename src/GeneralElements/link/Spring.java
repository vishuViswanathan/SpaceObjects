package GeneralElements.link;

import GeneralElements.DarkMatter;
import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Created by M Viswanathan on 24 May 2014
 */
public class Spring extends InfluenceDef  {
    LineArray linkLine;
    public Spring(DarkMatter item1, DarkMatter item2, double freeLen, double kCompression, double kExpansion, boolean bOldDef) {
        type = Type.SPRING;
        this.item1 = item1;
        this.item2 = item2;
        this.freeLen = freeLen;
        this.kCompression = kCompression;
        this.kExpansion = kExpansion;
    }

    public Spring(DarkMatter item1, DarkMatter item2, double freeLen, double kCommon, boolean bOldDef) {
        this(item1, item2, freeLen, kCommon, kCommon, bOldDef);
        type = Type.SPRING;
    }

    public Spring(DarkMatter item1, DarkMatter item2, double initialLenFactor, double eCommon) {
        super(item1, item2, initialLenFactor, eCommon, eCommon);
        type = Type.SPRING;
    }

    public Spring(DarkMatter item1, DarkMatter item2, double initialLenFactor, double eCompression, double eExpansion) {
        super(item1, item2, initialLenFactor, eCompression, eExpansion);
        type = Type.SPRING;
    }

    @Override
    public boolean evalForce(double deltaT, boolean bFinal) {
        boolean retVal = true;
        Vector3d distVect = new Vector3d();
        distVect.sub(item2.status.pos, item1.status.pos);
        double r = distVect.length();
        double diff = r - freeLen;
        double force;
        // attraction is positive
        if (diff > 0)
            force =  diff * kExpansion;
        else
            force =  diff * kCompression;
        double ratio = force / r;
        if (Double.isNaN(ratio) ) {
            retVal = false;
        }
        else {
            Vector3d nowForce = new Vector3d(distVect);
            nowForce.scale(ratio);
            item1.addToForce(nowForce);
//            nowForce.negate();
//            item2.addToForce(nowForce);
            item2.subtractFromForce(nowForce);
        }
        return retVal;
    }

    public boolean addLinksDisplay(Group grp, RenderingAttributes linkAtrib) {
        linkLine = new LineArray(2, GeometryArray.COORDINATES|GeometryArray.COLOR_3);
        linkLine.setCoordinates(0, new Point3d[]{item1.status.pos, item2.status.pos});
        Color3f color =  new Color3f(Color.darkGray);
        for (int c = 0; c < linkLine.getValidVertexCount(); c++)
            linkLine.setColor(c, color);
        linkLine.setCapability(PointArray.ALLOW_COORDINATE_READ);
        linkLine.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
        Shape3D shape = new Shape3D(linkLine);
        Appearance app = new Appearance();
        app.setRenderingAttributes(linkAtrib);
        shape.setAppearance(app);
        grp.addChild(shape);
        return true;
    }

    public void updateDisplay() {
        linkLine.setCoordinates(0, new Point3d[]{item1.status.pos, item2.status.pos});
    }

    NumberTextField nteCompression;
    NumberTextField nteExpansion;
    NumberTextField ntLenFactor;

    @Override
    public JPanel detailsPanel() {
        MultiPairColPanel outerP = new MultiPairColPanel("Details of " + this);
        nteCompression = new NumberTextField(null, eCompression, 6, false, 1, 1e10, "#,##0", "Elasticity - Compression (Force in Newton for 100%)");
        nteExpansion = new NumberTextField(null, eExpansion, 6, false, 1, 1e10, "#,##0", "Elasticity - Expansion (Force in Newton for 100%)");
        ntLenFactor = new NumberTextField(null, initialLenFactor, 6, false, 0.2, 10000, "#,##0.000", "Free Length Factor (Free Length/ distance");
        outerP.addItemPair(ntLenFactor);
        outerP.addItemPair(nteCompression);
        outerP.addItemPair(nteExpansion);
        return outerP;
    }

    @Override
    public boolean takeDataFromUI() {
        boolean  retVal = true;
        if ((ntLenFactor != null) && (nteCompression != null) && (nteExpansion != null)) {
            initialLenFactor = ntLenFactor.getData();
            eCompression = nteCompression.getData();
            eExpansion = nteExpansion.getData();
            setFreeLenAndKvalues();
        }
        else
            retVal = false;
        return retVal;
    }
}
