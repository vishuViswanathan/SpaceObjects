package GeneralElements.link;

import GeneralElements.Item;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Created by M Viswanathan on 08 Jun 2014
 */
public class Rod extends InfluenceDef  {
    LineArray linkLine;
    public Rod(Item item1, Item item2, double freeLen, double kCompression, boolean bOldDef) {
        type = Type.ROD;
        this.item1 = item1;
        this.item2 = item2;
        this.freeLen = freeLen;
        this.kCompression = kCompression;
    }

    public Rod(Item item1, Item item2, double initialLenFactor, double eCompression) {
        super(item1, item2, initialLenFactor, eCompression, eCompression);
        type = Type.ROD;
    }

    @Override
    public boolean evalForce() {
        Vector3d distVect = new Vector3d();
        distVect.sub(item2.status.pos, item1.status.pos);
        double r = distVect.length();
        Vector3d nowForce;
        double diff = r - freeLen;
        double force;
        // attraction is positive
        force  =   diff * kCompression;
        double ratio = force / r;
        nowForce = new Vector3d(distVect);
        nowForce.scale(ratio);
        item1.addToForce(nowForce);
        nowForce.negate();
        item2.addToForce(nowForce);
        return true;
    }

    public boolean addLinksDisplay(Group grp, RenderingAttributes linkAtrib) {
        linkLine = new LineArray(2, GeometryArray.COORDINATES|GeometryArray.COLOR_3);
        linkLine.setCoordinates(0, new Point3d[]{item1.status.pos, item2.status.pos});
        Color3f color =  new Color3f(Color.gray);
        for (int c = 0; c < linkLine.getValidVertexCount(); c++)
            linkLine.setColor(c, color);
        linkLine.setCapability(LineArray.ALLOW_COORDINATE_READ);
        linkLine.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
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
}
