package GeneralElements.Display;

import GeneralElements.Item;
import SpaceElements.collection.PointArrayFIFO;
import com.sun.j3d.utils.universe.ViewingPlatform;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 08 Aug 2014
 * All the variables for Graphic display for an Item
 * Created as a separate class and a WeakReference to the Item
 */
public class ItemGraphic {
    TransformGroup positionTrGrp;
    TransformGroup tgPlanet;
    TransformGroup trgAxis;
    TransformGroup trgRotation;
    PathShape[] orbitShapes;
    PointArrayFIFO ptArr;
    int nShapeSets = 4;
    int nPos = 2000; // number of positions
    Color3f color3f;
    double viewScale = 1;
    Item item;

    public ItemGraphic(Item item) {
        this.item = item;
    }

     public void addObjectAndOrbit(Group grp, RenderingAttributes orbitAtrib) throws Exception{
        createSphereAndOrbitPath(orbitAtrib);
        for (PathShape os: orbitShapes)
            grp.addChild(os);
        grp.addChild(positionTrGrp);
    }


    public void setScale (double scale) {
        Transform3D tr = new Transform3D();
        tgPlanet.getTransform(tr);
        tr.setScale(scale);
        tgPlanet.setTransform(tr);
        viewScale = scale;
    }

    public void attachPlatform(ViewingPlatform platform) {
        attachedPlatform = platform;
        positionTrGrp.addChild(attachedPlatform);
        bPlatformAttached = true;
    }

    ViewingPlatform attachedPlatform;
    boolean bPlatformAttached = false;

    public void detachPlatform() {
        if (bPlatformAttached)
            positionTrGrp.removeChild(attachedPlatform);
        bPlatformAttached = false;
    }

    private void createSphereAndOrbitPath(RenderingAttributes orbitAtrib) throws Exception {
        positionTrGrp = new TransformGroup();
        positionTrGrp.setCapability(Group.ALLOW_CHILDREN_WRITE);
        positionTrGrp.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        trgAxis  = new TransformGroup();
        tgPlanet = new TransformGroup();
        tgPlanet.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tgPlanet.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Transform3D alignPlanet = new Transform3D();
        tgPlanet.getTransform(alignPlanet);
        Transform3D turnNinetyX = new Transform3D();
        turnNinetyX.rotX(Math.PI / 2);
        alignPlanet.mul(turnNinetyX);

        tgPlanet.setTransform(alignPlanet);
        if (item.spinAxis != null) {
            Transform3D axisT = new Transform3D();
            trgAxis.getTransform(axisT);
            axisT.set(item.spinAxis);
            trgAxis.setTransform(axisT);
        }
        trgRotation = new TransformGroup();
        trgRotation.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        trgRotation.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        positionTrGrp.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        ItemSphere planet = new ItemSphere(item);
        tgPlanet.addChild(planet);
        trgRotation.addChild(tgPlanet);
        Light l = getLightIfEnabled();
        if (l != null)
            trgRotation.addChild(l);

        trgAxis.addChild(trgRotation);
        positionTrGrp.addChild(trgAxis);
        color3f = new Color3f(item.color);
        PointArrayFIFO onePtArr, lastOne = null;
        orbitShapes = new PathShape[nShapeSets];
        for (int os = 0; os < orbitShapes.length; os++) {
            onePtArr = onePointArray(nPos, ((os == (orbitShapes.length - 1) ) ? 1: 4), GeometryArray.COORDINATES|GeometryArray.COLOR_3, color3f);
            onePtArr.noteNextArray(lastOne);
            orbitShapes[os] = new PathShape(planet, onePtArr, orbitAtrib);
            lastOne = onePtArr;
            if (os == (orbitShapes.length - 1))
                ptArr = onePtArr;
        }
        updateOrbitAndPos(0);
    }

    PointArrayFIFO onePointArray(int vertexCount, int onceIn, int vertexFormat, Color3f color) {
        PointArrayFIFO onePtArr = new PointArrayFIFO(vertexCount, onceIn, vertexFormat, color);
        onePtArr.setCapability(PointArray.ALLOW_COORDINATE_READ);
        onePtArr.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
        onePtArr.setCapability(PointArray.ALLOW_COLOR_WRITE);
        return onePtArr;
    }

    public PointLight getLightIfEnabled() {
        PointLight light = null;
        if (item.isLightSrc) {
            light = new PointLight(true, new Color3f(1.0f, 1.0f, 1.0f), new Point3f(0, 0, 0), new Point3f(1, 0, 0));
            light.setCapability(PointLight.ALLOW_POSITION_WRITE);
//            light.setAttenuation(1f, 1e-15f, 0f);
            BoundingSphere bounds =
                    new BoundingSphere(new Point3d(), 1e22);
            light.setInfluencingBounds(bounds);
        }
        return light;
    }

    public void updateOrbitAndPos(double spinIncrement) throws Exception{
        ptArr.addCoordinate(item.status.pos);
        updateObjectPosition();
        updateSpin(spinIncrement);
    }

    private void updateObjectPosition() throws Exception {
        // position the planet
        Transform3D tr = new Transform3D();
        positionTrGrp.getTransform(tr);
        Vector3d posVector = new Vector3d(item.status.pos);
        tr.setTranslation(posVector);
//        posVector = null;
        try {
            positionTrGrp.setTransform(tr);
        } catch (Exception e) {
            String msg = e.getMessage();
            item.showError(e.getMessage());
            throw (new Exception(item.name + ": " + msg));
        }
    }

    void updateSpin(double spinIncrement) {
        if (spinIncrement != 0) {
            Transform3D rotTr = new Transform3D();
            trgRotation.getTransform(rotTr);
            Transform3D rotZ = new Transform3D();
            rotZ.rotZ(spinIncrement);
            rotTr.mul(rotZ);
            trgRotation.setTransform(rotTr);
        }
    }

}
