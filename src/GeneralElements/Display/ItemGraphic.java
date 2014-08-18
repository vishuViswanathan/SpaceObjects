package GeneralElements.Display;

import GeneralElements.Item;
import SpaceElements.collection.PointArrayFIFO;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;
import display.NumberLabel;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

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
    OrbitBehavior vpOrbitBehavior;
    PathShape[] orbitShapes;
    PointArrayFIFO ptArr;
    int nShapeSets = 4;
    int nPos = 2000; // number of positions
    Color3f color3f;
    Canvas3D localVewCanvas;
    ViewingPlatform localVp;
    NumberLabel nlViewDistance;
    JPanel jpViewDistance;
    double viewPosFromPlanet = 0;
    double viewScale = 1;
    Item item;

    public ItemGraphic(Item item) {
        this.item = item;
    }

    public void addLocalViewingPlatform() {
        // create a Viewer and attach to its canvas
        // a Canvas3D can only be attached to a single Viewer
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        localVewCanvas = new Canvas3D(config);
        localVewCanvas.addMouseWheelListener(new WheelListener());
        viewPosFromPlanet = 4 * item.dia * viewScale;

        Viewer viewer = new Viewer(localVewCanvas);
        viewer.getView().setBackClipDistance(1e22); //100 * viewPosFromPlanet);
        viewer.getView().setFrontClipDistance(0.00001);

        // create a ViewingPlatform with 1 TransformGroups above the ViewPlatform
        localVp = new ViewingPlatform();
        localVp.setNominalViewingTransform();
        Transform3D t3 = new Transform3D();
        viewer.setViewingPlatform(localVp);

        BoundingSphere bounds =
                new BoundingSphere(new Point3d(), 1e22);
        // with left button pressed
        vpOrbitBehavior = new OrbitBehavior(localVewCanvas, OrbitBehavior.REVERSE_ROTATE);
        vpOrbitBehavior.setSchedulingBounds(bounds);
        localVp.setViewPlatformBehavior(vpOrbitBehavior);

        vpOrbitBehavior.setRotationCenter(new Point3d(0, 0, 0));  //-viewPosFromPlanet));
        localVp.getViewPlatformTransform().getTransform(t3);
        t3.setTranslation(new Vector3d(0, 0, viewPosFromPlanet));
        localVp.getViewPlatformTransform().setTransform(t3);

        positionTrGrp.addChild(localVp);
        nlViewDistance = new NumberLabel(0, 150, "#,###");
        jpViewDistance = new JPanel();
        jpViewDistance.add(new JLabel("View Distance (km):"));
        jpViewDistance.add(nlViewDistance);
        updateViewDistanceUI(1.0);
    }

    void updateViewDistanceUI(double factor) {
        viewPosFromPlanet *= factor;
        nlViewDistance.setData(viewPosFromPlanet / 1000);

//        Transform3D vpTr = new Transform3D();
//        localVp.getViewPlatformTransform().getTransform(vpTr);
//        Vector3d trans = new Vector3d();
//        vpTr.get(trans);
//        trans.scale(factor);
//        vpTr.setTranslation(trans);
//        localVp.getViewPlatformTransform().setTransform(vpTr);

    }

    public void showLocalView(JPanel jp) {
        putItOnPanel(jp);
    }

    public void showLocalView(ViewingPlatform mainView, int atX, int atY, JPanel jp) {
        viewPosFromPlanet = 4 * item.dia * viewScale;
        Transform3D mainVTr = new Transform3D();
        mainView.getViewPlatformTransform().getTransform(mainVTr);

        Point3d eyePosINViewPlate= new Point3d();
        Viewer[] viewers = mainView.getViewers();
        Canvas3D canvas = viewers[0].getCanvas3D();
        canvas.getCenterEyeInImagePlate(eyePosINViewPlate);
        double midX = eyePosINViewPlate.x;
        double midY = eyePosINViewPlate.y;

        Point3d planetPosOnPlate = new Point3d();
        canvas.getPixelLocationInImagePlate(atX, atY, planetPosOnPlate);

        double angleY = Math.atan2((midX - planetPosOnPlate.x), eyePosINViewPlate.z);
        double angleX = Math.atan2((midY - planetPosOnPlate.y), eyePosINViewPlate.z);
        Transform3D rotX = new Transform3D();
        rotX.rotX(-angleX);
        Transform3D rotY = new Transform3D();
        rotY.rotY(angleY);
        mainVTr.mul(rotY);
        mainVTr.mul(rotX);

        Vector3d eye = new Vector3d();
        mainVTr.get(eye);

        Vector3d diff = new Vector3d(eye);
        diff.sub(item.status.pos);
        double planetFromEye = diff.length();
        double factor = viewPosFromPlanet / planetFromEye;
        diff.scale(factor);
        Transform3D localVpt = new Transform3D(mainVTr);
        localVpt.setTranslation(diff);
        localVp.getViewPlatformTransform().setTransform(localVpt);
        updateViewDistanceUI(1.0);
        putItOnPanel(jp);
    }

    void putItOnPanel(JPanel jp) {
        jp.removeAll();
        jp.add(new JLabel(item.name), BorderLayout.NORTH);
        jp.add(localVewCanvas, BorderLayout.CENTER);
        jp.add(jpViewDistance, BorderLayout.SOUTH);
        jp.updateUI();

    }

     public void addObjectAndOrbit(Group grp, RenderingAttributes orbitAtrib) throws Exception{
        createSphereAndOrbitPath(orbitAtrib);
        for (PathShape os: orbitShapes)
            grp.addChild(os);
        grp.addChild(positionTrGrp);
        addLocalViewingPlatform();
    }


    public void setScale (double scale) {
        Transform3D tr = new Transform3D();
        tgPlanet.getTransform(tr);
        tr.setScale(scale);
        tgPlanet.setTransform(tr);
        viewScale = scale;
    }

    private void createSphereAndOrbitPath(RenderingAttributes orbitAtrib) throws Exception {
        positionTrGrp = new TransformGroup();
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
            throw(new Exception(item.name + ": " + msg));
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

    class WheelListener implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int movement = e.getUnitsToScroll();
            double factor = (movement > 0) ? 1.1: 1/1.1;
            Transform3D vpTr = new Transform3D();
            localVp.getViewPlatformTransform().getTransform(vpTr);
            Vector3d trans = new Vector3d();
            vpTr.get(trans);
            trans.scale(factor);
            vpTr.setTranslation(trans);
            localVp.getViewPlatformTransform().setTransform(vpTr);
            updateViewDistanceUI(factor);
        }
    }

}
