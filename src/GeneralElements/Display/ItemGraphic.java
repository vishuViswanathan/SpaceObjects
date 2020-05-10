package GeneralElements.Display;

import Applications.ItemMovementsApp;
import GeneralElements.DarkMatter;
import GeneralElements.Item;
import GeneralElements.ItemInterface;
import collection.PointArrayFIFO;
import collection.RelOrbitGroup;
import collection.RelativePointArrayFIFO;
import com.sun.j3d.utils.universe.ViewingPlatform;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import java.util.LinkedList;

/**
 * Created by M Viswanathan on 08 Aug 2014
 * All the variables for Graphic display for an Item
 * Created as a separate class and a WeakReference to the Item
 */
public class ItemGraphic {
    RenderingAttributes itemAttrib;
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
    }

    public boolean addObjectAndOrbit(Group grp, RenderingAttributes orbitAtrib) throws Exception{
        boolean retVal = false;
        if (createSphereAndOrbitPath(orbitAtrib)) {
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
//        Transform3D scaleTransform = new Transform3D();
        tgPlanet.getTransform(scaleTransform);
        scaleTransform.setScale(scale);
        tgPlanet.setTransform(scaleTransform);
        viewScale = scale;
    }

    public void attachPlatform(ViewingPlatform platform,
                               boolean bShowRelOrbits, RenderingAttributes relOrbitAtrib, RelOrbitGroup relOrbitGroup) {
        attachedPlatform = platform;
        positionTrGrp.addChild(attachedPlatform);
        bPlatformAttached = true;
        if (bShowRelOrbits)
            prepareAllRelativeOrbits(relOrbitAtrib, relOrbitGroup);
    }


    ViewingPlatform attachedPlatform;
    boolean bPlatformAttached = false;

    public void detachPlatform() {
        if (bPlatformAttached)
            positionTrGrp.removeChild(attachedPlatform);
        bPlatformAttached = false;
    }

    ItemDisplay planet;

    private boolean createSphereAndOrbitPath(RenderingAttributes orbitAtrib) throws Exception {
        boolean retVal = false;
        planet = new ItemDisplay(item);
        if (planet.valid) {
            itemAttrib = new RenderingAttributes();
            itemAttrib.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
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
            positionTrGrp.addChild(trgAxis);
            color3f = new Color3f(((DarkMatter)item).color);
            PointArrayFIFO onePtArr, lastOne = null;
            orbitShapes = new PathShape[nShapeSets];
            for (int os = 0; os < orbitShapes.length; os++) {
                onePtArr = onePointArray(nPos, ((os == (orbitShapes.length - 1)) ? 1 : onceIn), GeometryArray.COORDINATES | GeometryArray.COLOR_3, color3f);
                onePtArr.noteNextArray(lastOne);
                orbitShapes[os] = new PathShape(planet, onePtArr, orbitAtrib);
                lastOne = onePtArr;
                if (os == (orbitShapes.length - 1))
                    ptArr = onePtArr;
            }
            updateOrbitAndPos(0);
            retVal = true;
        } else
            ItemMovementsApp.showError("ItemGraphic.212: Facing some problem in creating graphics for '" + item.getName() + "'");
        return retVal;
    }

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

    public PathShape[] prepareRelativeOrbit(ItemInterface relativeTo, RenderingAttributes orbitAtrib) {
        if (relPtArr == null) {
            relPtArr = relativeOnePointArray(ptArr, relativeTo);
            PointArrayFIFO onePtArr = relPtArr;
            relOrbitShapes = new PathShape[nShapeSets];
            for (int os = 0; os < orbitShapes.length; os++) {
                relOrbitShapes[os] = new PathShape(planet, onePtArr, orbitAtrib);
                onePtArr = onePtArr.getNextArray();
            }
        }
        return relOrbitShapes;
    }

    void prepareAllRelativeOrbits(RenderingAttributes orbitAtrib, RelOrbitGroup relOrbitGroup) {
        LinkedList<ItemInterface> itemList = item.getSpace().getAlItems();
        ItemGraphic ig;
        if (!relOrbitGroup.isInitiated()) {
            PathShape oneRelOrbitShapesArr[];
            for (ItemInterface i : itemList) {
                oneRelOrbitShapesArr = i.getItemGraphic().prepareRelativeOrbit(item, orbitAtrib);
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

    public void setItemDisplayAttribute(RenderingAttributes attribute) {
        planet.setRenderingAttribute(attribute);
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
        Point3d pos = item.getPos();
        ptArr.addCoordinate(pos);
        if (relPtArr != null) {
//            System.out.print(item.getName() + " ");
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
//            rotTransform = new Transform3D();
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
}
