package GeneralElements.Display;

import Applications.ItemMovementsApp;
import GeneralElements.DarkMatter;
import GeneralElements.ItemInterface;
import collection.PointArrayFIFO;
import collection.RelOrbitGroup;
import collection.RelativePointArrayFIFO;
import com.sun.j3d.utils.universe.ViewingPlatform;
import mvUtils.physics.Vector3dMV;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.LinkedList;

/**
 * Created by M Viswanathan on 08 Aug 2014
 * All the variables for Graphic display for an Item
 * Created as a separate class and a WeakReference to the Item
 */
public class ItemGraphic {
    RenderingAttributes itemAttrib;
    RenderingAttributes itemRelOrbitAttrib;
    RenderingAttributes itemPathAttrib;

    TransformGroup positionTrGrp;
    Transform3D positionTransform = new Transform3D();
    TransformGroup tgPlanet;
    Transform3D scaleTransform = new Transform3D();
    TransformGroup trgAxis;
    Transform3D axisTransform = new Transform3D();
    TransformGroup trgRotation;
    Transform3D rotTransform = new Transform3D();
    PathShape[] orbitShapes;
    PathShape[] relOrbitShapes;
    PointArrayFIFO ptArr;
    // for relative orbit points
    RelativePointArrayFIFO relPtArr;
    int nShapeSets = 4;
    int nPos = 1000; // number of positions
    int onceIn = 4; // each historystep takes one data inthis many of theprevious
    Color3f color3f;
    double viewScale = 1;
    private ItemInterface item;

    public ItemGraphic(ItemInterface item) {
        this.item = item;
    }

    public void setVisible(boolean visible) {
        itemAttrib.setVisible(visible);
        setRelOrbitVisible(visible);
        setPathVisible(visible);
    }

    public void setRelOrbitVisible(boolean visible) {
        itemRelOrbitAttrib.setVisible(visible  & ItemMovementsApp.bShowRelOrbits &
                itemAttrib.getVisible());
    }

    public void setPathVisible(boolean visible) {
        itemPathAttrib.setVisible(visible  & ItemMovementsApp.bShowPaths &
                itemAttrib.getVisible());
    }

    public boolean addObjectAndOrbit(Group grp) throws Exception{
        boolean retVal = false;
        if (createSphereAndOrbitPath()) {
            for (PathShape os : orbitShapes)
                grp.addChild(os);
            grp.addChild(positionTrGrp);
            retVal = true;
        }
        return retVal;
    }

    public PointArrayFIFO getPtArray() {
        return ptArr;
    }

    public void setScale (double scale) {
        tgPlanet.getTransform(scaleTransform);
        scaleTransform.setScale(scale);
        tgPlanet.setTransform(scaleTransform);
        viewScale = scale;
    }

    public void attachPlatform(ViewingPlatform platform,
                               boolean bShowRelOrbits, RelOrbitGroup relOrbitGroup) {
        attachedPlatform = platform;
        positionTrGrp.addChild(attachedPlatform);
        bPlatformAttached = true;
        if (bShowRelOrbits)
            prepareAllRelativeOrbits(relOrbitGroup);
    }


    ViewingPlatform attachedPlatform;
    boolean bPlatformAttached = false;

    public void detachPlatform() {
        if (bPlatformAttached)
            positionTrGrp.removeChild(attachedPlatform);
        bPlatformAttached = false;
    }

    ItemDisplay planet;

    private boolean createSphereAndOrbitPath() throws Exception {
        boolean retVal = false;
        planet = new ItemDisplay(item);
        if (planet.valid) {
            itemAttrib = new RenderingAttributes();
            itemAttrib.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
            itemRelOrbitAttrib = new RenderingAttributes();
            itemRelOrbitAttrib.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
            itemPathAttrib = new RenderingAttributes();
            itemPathAttrib.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
            setVisible(true);
            planet.setRenderingAttribute(itemAttrib);
            positionTrGrp = new TransformGroup();
            positionTrGrp.setCapability(Group.ALLOW_CHILDREN_WRITE);
            positionTrGrp.setCapability(Group.ALLOW_CHILDREN_EXTEND);
            positionTrGrp.setCapability(BranchGroup.ALLOW_DETACH);
            trgAxis = new TransformGroup();
            tgPlanet = new TransformGroup();
            tgPlanet.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            tgPlanet.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            Transform3D alignPlanet = new Transform3D();
            tgPlanet.getTransform(alignPlanet);
            Transform3D turnNinetyX = new Transform3D();
            turnNinetyX.rotX(Math.PI / 2);
            alignPlanet.mul(turnNinetyX);

            tgPlanet.setTransform(alignPlanet);
            if (item.getSpinAxis() != null) {
//            axisTransform = new Transform3D();
                trgAxis.getTransform(axisTransform);
                axisTransform.set(item.getSpinAxis());
                trgAxis.setTransform(axisTransform);
            }

//            setAxisAnd0e0N(trgAxis);

//            Vector3dMV defaultAxisVector = item.getDefaultAxisVector();
//            defaultAxisVector.normalize();
//            trgAxis.getTransform(axisTransform);
//            double thetaY = Math.asin(defaultAxisVector.x);
//            double thetaX = - Math.asin(defaultAxisVector.y / Math.cos(thetaY));
//            Transform3D rotX = new Transform3D();
//            rotX.rotX(thetaX);
//            axisTransform.mul(rotX);
//            Transform3D rotY = new Transform3D();
//            rotY.rotY(thetaY);
//            axisTransform.mul(rotY);
//            trgAxis.setTransform(axisTransform);

            trgRotation = new TransformGroup();
            trgRotation.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            trgRotation.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            positionTrGrp.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            tgPlanet.addChild(planet);
            trgRotation.addChild(tgPlanet);
            Light l = getLightIfEnabled();
            if (l != null)
                trgRotation.addChild(l);

            trgAxis.addChild(trgRotation);
            trgAxis.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            positionTrGrp.addChild(trgAxis);

            trgRotation.addChild(getZeroEZeroNpointTrg());

            color3f = new Color3f(((DarkMatter)item).color);
            PointArray pA = new PointArray(1, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
            pA.setColor(0, color3f);
            Appearance a = new Appearance();
            a.setRenderingAttributes(itemAttrib);
            Shape3D s = new Shape3D(pA, a);

            positionTrGrp.addChild(s);

            PointArrayFIFO onePtArr, lastOne = null;
            orbitShapes = new PathShape[nShapeSets];
            for (int os = 0; os < orbitShapes.length; os++) {
                onePtArr = onePointArray(nPos, ((os == (orbitShapes.length - 1)) ? 1 : onceIn),
                        GeometryArray.COORDINATES | GeometryArray.COLOR_3, color3f);
                onePtArr.noteNextArray(lastOne);
                orbitShapes[os] = new PathShape(planet, onePtArr, itemPathAttrib);
                lastOne = onePtArr;
                if (os == (orbitShapes.length - 1))
                    ptArr = onePtArr;
            }
            updateOrbitAndPos(0);
            retVal = true;
        } else
            ItemMovementsApp.showError("ItemGraphic.170: Facing some problem in creating graphics for '" + item.getName() + "'");
        return retVal;
    }

    public void setAxisAnd0e0N() {
        // see notes dated 20210114
        Vector3dMV defaultAxisVector = new Vector3dMV(item.getDefaultAxisVector());
        if (defaultAxisVector.lengthSquared() > 0) {
//            debug(item.getName() + " zeroEzeroNPointUnitVector-Before :" + zeroEzeroNPointUnitVector());
            defaultAxisVector.normalize();
            Transform3D nowAxisTransform = new Transform3D();
            trgAxis.getTransform(nowAxisTransform);
            double thetaY = Math.asin(defaultAxisVector.x);
            double thetaX = -Math.asin(defaultAxisVector.y / Math.cos(thetaY));
            Transform3D rotX = new Transform3D();
            rotX.rotX(thetaX);
            nowAxisTransform.mul(rotX);
            Transform3D rotY = new Transform3D();
            rotY.rotY(thetaY);
            nowAxisTransform.mul(rotY);
            trgAxis.setTransform(nowAxisTransform);

            // position zeroEZeroNPoint
            Vector3dMV v1 = defaultAxisVector;
            Vector3dMV v2 = new Vector3dMV(item.getZeroLongiVector());
            if (v2.lengthSquared() > 0) {
//                debug(item.getName() + " zeroEzeroNPointUnitVector-After1 :" + zeroEzeroNPointUnitVector());
                v2.normalize(); // v2
                Vector3dMV v3 = getGlobalCoordsOfZeroEzeroNPoint();
                v3.sub(item.getPos());
                v3.normalize();
                Vector3dMV v4 = new Vector3dMV();
                v4.cross(v1, v2);
                v4.normalize();
                if (v4.lengthSquared() > 0) {
//                    debug("len v4:" + v4.length());
                    double theta1 = Math.acos(v3.dot(v4));
                    Vector3d v3crossv4 = new Vector3d();
                    v3crossv4.cross(v3, v4);
                    double theta2 = 0;
                    if (v3crossv4.dot(v1) > 0)  // case I or II
                        theta2 = theta1 - Math.PI / 2;
                    else
                        theta2 = -(theta1 + Math.PI / 2);
//                    debug("theta1:" + theta1 + ", theta2:" + theta2);
                    // need to rotate by theta2 about z axis
                    Transform3D rotZ = new Transform3D();
                    rotZ.rotZ(theta2);
                    trgAxis.getTransform(nowAxisTransform);
                    nowAxisTransform.mul(rotZ);
                    trgAxis.setTransform(nowAxisTransform);
                }
//                debug(item.getName() +" - zeroEzeroNPointUnitVector-After2 :" + zeroEzeroNPointUnitVector());
            }
        }
    }

//    Vector3d zeroEzeroNPointUnitVector() {
//        Vector3d vect = getGlobalCoordsOfZeroEzeroNPoint();
//        vect.sub(item.getPos());
//        vect.normalize();
//        return vect;
//    }

    Shape3D zeroEzeroNPoint;

    TransformGroup getZeroEZeroNpointTrg() {
        TransformGroup trg = new TransformGroup();
        Transform3D transform = new Transform3D();
        Vector3d translation = new Vector3d(0, -item.getRadius(), 0);
        transform.setTranslation(translation);
        trg.setTransform(transform);
        PointArray pA = new PointArray(1, GeometryArray.COORDINATES | GeometryArray.COLOR_3);

        pA.setColor(0, new Color3f(Color.yellow));
        Appearance a = new Appearance();
        a.setRenderingAttributes(itemAttrib);
        zeroEzeroNPoint = new Shape3D(pA, a);
        trg.addChild(zeroEzeroNPoint);
        return trg;
    }

    public Vector3dMV getGlobalCoordsOfZeroEzeroNPoint() {
        Transform3D transform = new Transform3D();
        zeroEzeroNPoint.getLocalToVworld(transform);
        Vector3dMV pos = new Vector3dMV();
        transform.get(pos);
        return pos;
    }

//    public void showZeroEzeroNPointPosition(String msg) {
//        debug(msg + ": position of zeroEzeroNPoint: " +
//                getGlobalCoordsOfZeroEzeroNPoint());
//    }

    RelativePointArrayFIFO relativeOnePointArray(PointArrayFIFO onePointArray, ItemInterface itemRelativeTo) {
        PointArrayFIFO  fifoRelativeTo = itemRelativeTo.getItemGraphic().getPtArray();
        RelativePointArrayFIFO onePtArr = new RelativePointArrayFIFO(onePointArray, itemRelativeTo, fifoRelativeTo);
//        onePtArr.setCapability(PointArray.ALLOW_COORDINATE_READ);
//        onePtArr.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
//        onePtArr.setCapability(PointArray.ALLOW_COLOR_WRITE);
        return onePtArr;
    }

    boolean updateRelOrbitBase(ItemInterface itemRelativeTo) {
        PointArrayFIFO  fifoRelativeTo = itemRelativeTo.getItemGraphic().getPtArray();
        RelativePointArrayFIFO oneRelPtArr = relPtArr;
        PointArrayFIFO ptArrHist = ptArr;
        while (oneRelPtArr != null) {
            oneRelPtArr.takeData(ptArrHist, itemRelativeTo, fifoRelativeTo);
            oneRelPtArr = (RelativePointArrayFIFO)oneRelPtArr.getNextArray();
            ptArrHist = ptArrHist.getNextArray();
            fifoRelativeTo = fifoRelativeTo.getNextArray();
        }
        return true;
    }

    public PathShape[] prepareRelativeOrbit(ItemInterface relativeTo) {
        if (relPtArr == null) {
            relPtArr = relativeOnePointArray(ptArr, relativeTo);
            PointArrayFIFO onePtArr = relPtArr;
            relOrbitShapes = new PathShape[nShapeSets];
            for (int os = 0; os < orbitShapes.length; os++) {
                relOrbitShapes[os] = new PathShape(planet, onePtArr, itemRelOrbitAttrib);
                onePtArr = onePtArr.getNextArray();
            }
        }
        return relOrbitShapes;
    }

    void prepareAllRelativeOrbits(RelOrbitGroup relOrbitGroup) {
        LinkedList<ItemInterface> itemList = item.getSpace().getAlItems();
        ItemGraphic ig;
        if (!relOrbitGroup.isInitiated()) {
            PathShape oneRelOrbitShapesArr[];
            for (ItemInterface i : itemList) {
                oneRelOrbitShapesArr = i.getItemGraphic().prepareRelativeOrbit(item);
                for (PathShape p : oneRelOrbitShapesArr)
                    relOrbitGroup.addChild(p);
            }
            relOrbitGroup.setInitiated();
        }
        else {
            for (ItemInterface it : itemList) {
                ig = it.getItemGraphic();
                ig.detachRelOrbitGroup(relOrbitGroup);
                ig.updateRelOrbitBase(item);
            }
        }
        attachRelObitGroup(relOrbitGroup);
    }

    public void attachRelObitGroup(RelOrbitGroup relOrbitGroup) {
        positionTrGrp.addChild(relOrbitGroup);
    }

    public void detachRelOrbitGroup(RelOrbitGroup relOrbitGroup) {
        positionTrGrp.removeChild(relOrbitGroup);
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
        if (item.isLightSrc()) {
            light = new PointLight(true, new Color3f(0.5f, 0.5f, 0.5f), new Point3f(0, 0, 0), new Point3f(1, 0, 0));
            light.setCapability(PointLight.ALLOW_POSITION_WRITE);
//            light.setAttenuation(1f, 1e-15f, 0f);
            BoundingSphere bounds =
                    new BoundingSphere(new Point3d(), 1e22);
            light.setInfluencingBounds(bounds);
        }
        return light;
    }

    public void updateOrbitAndPos(double spinIncrement) throws Exception{
        Point3d pos = item.getPos();
        ptArr.addCoordinate(pos);
        if (relPtArr != null) {
            relPtArr.addRelativeCoordinate(pos);
        }
        updateObjectPosition();
        updateSpin(spinIncrement);
    }

    private void updateObjectPosition() throws Exception {
        // position the planet
//        Transform3D positionTransform = new Transform3D();
        positionTrGrp.getTransform(positionTransform);
        Vector3d posVector = new Vector3d(item.getPos());
        positionTransform.setTranslation(posVector);
//        posVector = null;
        try {
            positionTrGrp.setTransform(positionTransform);
        } catch (Exception e) {
            String msg = e.getMessage();
            ItemMovementsApp.showError("ItemGraphic.175:" + e.getMessage());
            throw (new Exception(item.getName() + ": " + msg));
        }
    }

    void updateSpin(double spinIncrement) {
        if (spinIncrement != 0) {
            trgRotation.getTransform(rotTransform);
            Transform3D rotZ = new Transform3D();
            rotZ.rotZ(spinIncrement);
            rotTransform.mul(rotZ);
            trgRotation.setTransform(rotTransform);
        }
    }

    public void updateAngularPosition(Vector3d angle ) {
        Transform3D tr = new Transform3D();
        trgRotation.getTransform(tr);
        if (angle.x != 0) {
            Transform3D rotTrX = new Transform3D();
            rotTrX.rotX(angle.x);
            tr.mul(rotTrX);
        }
        if (angle.y != 0) {
            Transform3D rotTrY = new Transform3D();
            rotTrY.rotY(angle.y);
            tr.mul(rotTrY);
        }
        if (angle.z != 0) {
            Transform3D rotTrZ = new Transform3D();
            rotTrZ.rotZ(angle.z);
            tr.mul(rotTrZ);
        }
        rotTransform.set(tr);
        trgRotation.setTransform(rotTransform);
    }

    public Transform3D getTotalTransform(Transform3D transform) {
        transform.set(rotTransform);
        transform.mul(axisTransform);
        transform.mul(scaleTransform);
        transform.mul(positionTransform);
        return transform;
    }

    public void updateColor() {
        planet.updateColor();
    }

    public void setEnableLight(boolean ena) {
        planet.setEnableLight(ena);
    }

    void trace(String msg) {
        ItemMovementsApp.log.trace(msg);
    }

    void debug(String msg) {
        ItemMovementsApp.debug(msg);
    }

    void error(String msg) {
        ItemMovementsApp.showError(msg);
    }
}
