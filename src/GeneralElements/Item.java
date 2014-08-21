package GeneralElements;

import Applications.ItemMovementsApp;
import GeneralElements.Display.ItemGraphic;
import GeneralElements.Display.ItemSphere;
import GeneralElements.Display.PathShape;
import GeneralElements.Display.TuplePanel;
import GeneralElements.link.ItemLink;
import GeneralElements.localActions.LocalAction;
import SpaceElements.collection.PointArrayFIFO;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;
import display.InputControl;
import display.MultiPairColPanel;
import display.NumberLabel;
import display.NumberTextField;
import evaluations.EvalOnce;
import mvUtils.Vector3dMV;
import mvXML.ValAndPos;
import mvXML.XMLmv;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Arc2D;
import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Created by M Viswanathan on 31 Mar 2014
 */
public class Item implements InputControl, EvalOnce {
    Window parentW;
    Vector <ItemLink> links;
    ItemSpace space;
    public ItemStat status;
    boolean bFixedLocation = false;
    Vector3dMV dirOfFixedGravityAcc;  // direction of fixed Acceleration, a unit Vector
    TuplePanel fixedAccVectPan;
    NumberTextField ntFixedAcc;
    double fixedAcc = 9.81; // fixed acceleration value
    Vector3d forceOfFixedGravity;
    double surfaceArea;
    boolean bFixedForceOn = false;
    JRadioButton rbFixedAccOn;
    double xMax, yMax, zMax;
    double xMin, yMin, zMin;
    Vector3d force = new Vector3d();
    public String name;
    public double mass;
    public double dia;
    public AxisAngle4d spinAxis; // absolute
    double spinPeriod; // in hours
    public Color color;
    public String imageName;
    public boolean isLightSrc = false;
    Vector<LocalAction> localActions;

    JTextField tfName;
    NumberTextField ntMass, ntDia;
    TuplePanel posTuplePan;
    JRadioButton rbFixedPos;
    TuplePanel velTuplePan;
    JButton relButton = new JButton("Set Relative Data");

    public double reportInterval = 0; // sec?  144000;
    double nextReport; // sec
    public Item(Window parent) {
        this.parentW = parent;
        links = new Vector<ItemLink>();
        localActions = new Vector<LocalAction>();
    }

    public Item (String name, double mass, double dia, Color color, Window parent)  {
        this(parent);
        this.name = name;
        this.mass = mass;
        this.dia = dia;
        this.color = color;
        status = new ItemStat();
        dirOfFixedGravityAcc = new Vector3dMV(0, -1, 0);
        setRadioButtons();
//        rbFixedPos = new JRadioButton("Fixed Position");
//        rbFixedAccOn = new JRadioButton("Directional Acceleration ON");
    }

    public Item(String xmlStr, Window parent) {
        this(parent);
        setRadioButtons();
        takeFromXMl(xmlStr);
    }

    public void addLocalAction(LocalAction action) {
        localActions.add(action);
    }

    public double getSurfaceArea() {
        return 4 * Math.PI * Math.pow(dia / 2, 2);
    }

    void setRadioButtons() {
        rbFixedPos = new JRadioButton("Fixed Position");
        rbFixedAccOn = new JRadioButton("Directional Acceleration ON");

    }

    public void setbFixedLocation(boolean set) {
        bFixedLocation = set;
        rbFixedPos.setSelected(set);
    }

    public void setbFixedForceOn(boolean set) {
        bFixedForceOn = set;
        rbFixedAccOn.setSelected(set);
    }

    public void setRefreshInterval(double interval, double nextRefresh) {
        reportInterval = interval;
        nextReport = nextRefresh;
    }

    public JPanel dataPanel(int objNum) {
        JPanel outerPan = new JPanel(new BorderLayout());
        MultiPairColPanel jp = new MultiPairColPanel("Data of Item " + objNum);
        tfName = new JTextField(name, 8);
        jp.addItemPair("Object Name", tfName);
        ntMass = new NumberTextField(this, mass, 8, false, 1e-30, 1e40, "##0.#####E00", "Mass in kg") ;
        jp.addItemPair(ntMass);
        ntDia = new NumberTextField(this, dia, 6, false, 1e-20, 1e20, "##0.#####E00", "Dia in m");
        jp.addItemPair(ntDia);
        relButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getRelativeData(relButton);
            }
        });
        jp.addItemPair("", relButton);

        JPanel jpPos = new JPanel(new BorderLayout());
        posTuplePan = new TuplePanel(this, status.pos, 8, -1e30, 1e20, "##0.#####E00", "Position in m");
        rbFixedPos.setSelected(bFixedLocation);
        rbFixedPos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkFixedPos();
            }
        });
        jpPos.add(posTuplePan, BorderLayout.CENTER);
        jp.addItemPair("Position in m", jpPos);
        jp.addItemPair("", rbFixedPos);
        JPanel jpVel = new JPanel(new BorderLayout());
        velTuplePan = new TuplePanel(this, status.velocity, 8, -1e20, 1e20,"##0.#####E00", "Velocity im m/s" );
        jpVel.add(velTuplePan, BorderLayout.CENTER);
        jp.addItemPair("Velocity in m/s", jpVel);
        rbFixedAccOn.setSelected(bFixedForceOn);
        rbFixedAccOn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkFixedAcc();
            }
        });
        jp.addItemPair("", rbFixedAccOn);
        outerPan.add(jp, BorderLayout.WEST);
//        JPanel jpFixedAcc = new JPanel(new BorderLayout());
        fixedAccVectPan = new TuplePanel(this, dirOfFixedGravityAcc, 8, -100, 100,"##0.#####E00", "Direction of Acc Vector");
        ntFixedAcc = new NumberTextField(this, fixedAcc, 8, false, 0, 2000,"##0.#####E00", "Fixed Acc in m/s2");
        checkFixedAcc();
        jp.addItemPair(ntFixedAcc);
        jp.addItemPair("Direction of Acc", fixedAccVectPan);
        outerPan.add(jp, BorderLayout.SOUTH);
        return outerPan;
    }

    void checkFixedPos() {
        bFixedLocation = rbFixedPos.isSelected();
        velTuplePan.setEnabled(!bFixedLocation);
    }

    void checkFixedAcc() {
        bFixedForceOn = rbFixedAccOn.isSelected();
        fixedAccVectPan.setEnabled(bFixedForceOn);
        ntFixedAcc.setEnabled(bFixedForceOn);
    }

    RelativeDlg relDlg;

    void getRelativeData(JComponent butt) {
        space.noteInput();
        relDlg = new RelativeDlg(this);
        relDlg.setLocationRelativeTo(butt);
        relDlg.setVisible(true);
    }

    void updateUI() {
        posTuplePan.updateTuple(status.pos);
        rbFixedPos.setSelected(bFixedLocation);
        velTuplePan.updateTuple(status.velocity);
        fixedAccVectPan.updateTuple(dirOfFixedGravityAcc);
        ntFixedAcc.setData(fixedAcc);
        rbFixedAccOn.setSelected(bFixedForceOn);
        checkFixedPos();
        checkFixedAcc();
    }

    class RelativeDlg extends JDialog {
        Item parent;
        Vector3d tupRelPos, tupRelVel;
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        TuplePanel relPosPan, relVelPan;
        InputControl inpC;
        JComboBox<Object> othersCB;
        RelativeDlg(InputControl inpC) {
            this.inpC = inpC;
            dbInit();
        }

        void dbInit() {
            tupRelPos = new Vector3d();
            tupRelVel = new Vector3d();
            MultiPairColPanel jp = new MultiPairColPanel("Relative Data of SpaceObject");
            othersCB = new JComboBox<Object>(space.getAllItems().toArray());
            jp.addItemPair(new JLabel("Relative to "), othersCB);
            relPosPan = new TuplePanel(inpC, tupRelPos, 8, -1e20, 1e20,"##0.#####E00", "Relative position in m");
            jp.addItemPair("position in m", relPosPan);
            if (!bFixedLocation) {
                relVelPan = new TuplePanel(inpC, tupRelVel, 8, -1e20, 1e20,"##0.#####E00", "Relative Velocity in m");
                jp.addItemPair("Velocity in m/s", relVelPan);
            }
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == ok) {
                        takeValuesFromUI();
                        closeThisWindow();

                    } else  {
                        closeThisWindow();
                    }
                }
            };
            ok.addActionListener(li);
            cancel.addActionListener(li);
            jp.addItemPair(cancel, ok);
            add(jp);
            pack();
        }

        void takeValuesFromUI() {
            parent = space.getAllItems().get(othersCB.getSelectedIndex());
            tupRelPos.set(relPosPan.getTuple3d());
            tupRelPos.add(parent.status.pos);
            status.pos.set(tupRelPos);
            if (!bFixedLocation) {
                tupRelVel.set(relVelPan.getTuple3d());
                tupRelVel.add(parent.status.velocity);
                status.velocity.set(tupRelVel);
            }
            updateUI();
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }

    }

    void noteInput() {
        name = tfName.getText();
        dia = ntDia.getData();
        mass = ntMass.getData();
        bFixedLocation = rbFixedPos.isSelected();
        Tuple3d posTuple = posTuplePan.getTuple3d();
        if (posTuple != null)
            status.pos.set(posTuple);
        Tuple3d velTuple = velTuplePan.getTuple3d();
        if (velTuple != null)
            status.velocity.set(velTuple);
        bFixedForceOn = rbFixedAccOn.isSelected();
        if (bFixedForceOn) {
            Tuple3d accTuple = fixedAccVectPan.getTuple3d();
            if (accTuple != null)
               dirOfFixedGravityAcc.set(accTuple);
            double vecLen = dirOfFixedGravityAcc.length();
            if (vecLen > 0) {
                fixedAcc = ntFixedAcc.getData();
                dirOfFixedGravityAcc.scale(1/vecLen);
                forceOfFixedGravity = new Vector3d(dirOfFixedGravityAcc);
                forceOfFixedGravity.scale(mass * fixedAcc);
            }
            else {
                bFixedForceOn = false;
                ItemMovementsApp.log.error("Acc Vector Length is < 0 [" + vecLen);
            }
        }
        resetLimits();
        status.time = 0;
        nextReport = 0;
//        history.add(status);
    }

    void resetLimits() {
        xMax = Double.NEGATIVE_INFINITY;
        yMax = Double.NEGATIVE_INFINITY;
        zMax = Double.NEGATIVE_INFINITY;
        xMin = Double.POSITIVE_INFINITY;
        yMin = Double.POSITIVE_INFINITY;
        zMin = Double.POSITIVE_INFINITY;
    }

    void evalMaxMinPos() {
        xMax = Math.max(xMax, status.pos.x);
        yMax = Math.max(yMax, status.pos.y);
        zMax = Math.max(zMax, status.pos.z);
        xMin = Math.min(xMin, status.pos.x);
        yMin = Math.min(yMin, status.pos.y);
        zMin = Math.min(zMin, status.pos.z);
    }


    public void setImage(String imageName) {
        this.imageName = imageName;
    }

    public void addInfluence(ItemLink itemLink) {
        links.add(itemLink);
    }

    public boolean removeInfluence(ItemLink itemLink){
        return links.remove(itemLink);
    }

    public void clearInfluence() {
        links.clear();
    }

    public void setSpace(ItemSpace space) {
        this.space = space;
    }

     public void initPosEtc(Point3d pos, Vector3d velocity) {
        status.initPos(pos, velocity);
        nextReport = reportInterval;
    }

    public void setSpin(AxisAngle4d spinAxis, double spinPeriod) {
        this.spinAxis = spinAxis;
        this.spinPeriod = spinPeriod;
        if (spinPeriod > 0)
            radPerSec = Math.PI * 2 / spinPeriod;
    }

    public float getDiaFloat() {
        return new Float(dia);
    }

    //   ======================== Display codes ==================
// to shift to ItemGraphic --------------------------------------
//    TransformGroup positionTrGrp;
//    TransformGroup tgPlanet;
//    TransformGroup trgAxis;
//    TransformGroup trgRotation;
//    OrbitBehavior vpOrbitBehavior;
//    PathShape[] orbitShapes;
//    PointArrayFIFO ptArr;
//    int nShapeSets = 4;
//    int nPos = 2000; // number of positions
//    Color3f color3f;
//    Canvas3D localVewCanvas;
//    ViewingPlatform localVp;
//    NumberLabel nlViewDistance;
//    JPanel jpViewDistance;
//    double viewPosFromPlanet = 0;
//    double viewScale = 1;

//    public void addLocalViewingPlatform() {
//        // create a Viewer and attach to its canvas
//        // a Canvas3D can only be attached to a single Viewer
//        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
//        localVewCanvas = new Canvas3D(config);
//        localVewCanvas.addMouseWheelListener(new WheelListener());
//        viewPosFromPlanet = 4 * dia * viewScale;
//
//        Viewer viewer = new Viewer(localVewCanvas);
//        viewer.getView().setBackClipDistance(1e22); //100 * viewPosFromPlanet);
//        viewer.getView().setFrontClipDistance(0.00001);
//
//        // create a ViewingPlatform with 1 TransformGroups above the ViewPlatform
//        localVp = new ViewingPlatform();
//        localVp.setNominalViewingTransform();
//        Transform3D t3 = new Transform3D();
//        viewer.setViewingPlatform(localVp);
//
//        BoundingSphere bounds =
//                new BoundingSphere(new Point3d(), 1e22);
//        // with left button pressed
//        vpOrbitBehavior = new OrbitBehavior(localVewCanvas, OrbitBehavior.REVERSE_ROTATE);
//        vpOrbitBehavior.setSchedulingBounds(bounds);
//        localVp.setViewPlatformBehavior(vpOrbitBehavior);
//
//        vpOrbitBehavior.setRotationCenter(new Point3d(0, 0, 0));  //-viewPosFromPlanet));
//        localVp.getViewPlatformTransform().getTransform(t3);
//        t3.setTranslation(new Vector3d(0, 0, viewPosFromPlanet));
//        localVp.getViewPlatformTransform().setTransform(t3);
//
//        positionTrGrp.addChild(localVp);
//        nlViewDistance = new NumberLabel(0, 150, "#,###");
//        jpViewDistance = new JPanel();
//        jpViewDistance.add(new JLabel("View Distance (km):"));
//        jpViewDistance.add(nlViewDistance);
//        updateViewDistanceUI(1.0);
//    }

//    class WheelListener implements MouseWheelListener {
//        @Override
//        public void mouseWheelMoved(MouseWheelEvent e) {
//            int movement = e.getUnitsToScroll();
//            double factor = (movement > 0) ? 1.1: 1/1.1;
//            Transform3D vpTr = new Transform3D();
//            localVp.getViewPlatformTransform().getTransform(vpTr);
//            Vector3d trans = new Vector3d();
//            vpTr.get(trans);
//            trans.scale(factor);
//            vpTr.setTranslation(trans);
//            localVp.getViewPlatformTransform().setTransform(vpTr);
//            updateViewDistanceUI(factor);
//        }
//    }

//    void updateViewDistanceUI(double factor) {
//        viewPosFromPlanet *= factor;
//        nlViewDistance.setData(viewPosFromPlanet / 1000);

//        Transform3D vpTr = new Transform3D();
//        localVp.getViewPlatformTransform().getTransform(vpTr);
//        Vector3d trans = new Vector3d();
//        vpTr.get(trans);
//        trans.scale(factor);
//        vpTr.setTranslation(trans);
//        localVp.getViewPlatformTransform().setTransform(vpTr);
//    }

//    public void showLocalViewOLD(JPanel jp) {
//        putItOnPanel(jp);
//    }

//    public void showLocalViewOLD(ViewingPlatform mainView, int atX, int atY, JPanel jp) {
//        viewPosFromPlanet = 4 * dia * viewScale;
//        Transform3D mainVTr = new Transform3D();
//        mainView.getViewPlatformTransform().getTransform(mainVTr);
//
//        Point3d eyePosINViewPlate= new Point3d();
//        Viewer[] viewers = mainView.getViewers();
//        Canvas3D canvas = viewers[0].getCanvas3D();
//        canvas.getCenterEyeInImagePlate(eyePosINViewPlate);
//        double midX = eyePosINViewPlate.x;
//        double midY = eyePosINViewPlate.y;
//
//        Point3d planetPosOnPlate = new Point3d();
//        canvas.getPixelLocationInImagePlate(atX, atY, planetPosOnPlate);
//
//        double angleY = Math.atan2((midX - planetPosOnPlate.x), eyePosINViewPlate.z);
//        double angleX = Math.atan2((midY - planetPosOnPlate.y), eyePosINViewPlate.z);
//        Transform3D rotX = new Transform3D();
//        rotX.rotX(-angleX);
//        Transform3D rotY = new Transform3D();
//        rotY.rotY(angleY);
//        mainVTr.mul(rotY);
//        mainVTr.mul(rotX);
//
//        Vector3d eye = new Vector3d();
//        mainVTr.get(eye);
//
//        Vector3d diff = new Vector3d(eye);
//        diff.sub(status.pos);
//        double planetFromEye = diff.length();
//        double factor = viewPosFromPlanet / planetFromEye;
//        diff.scale(factor);
//        Transform3D localVpt = new Transform3D(mainVTr);
//        localVpt.setTranslation(diff);
//        localVp.getViewPlatformTransform().setTransform(localVpt);
//        updateViewDistanceUI(1.0);
//        putItOnPanel(jp);
//    }

//    void putItOnPanel(JPanel jp) {
//        jp.removeAll();
//        jp.add(new JLabel(name), BorderLayout.NORTH);
//        jp.add(localVewCanvas, BorderLayout.CENTER);
//        jp.add(jpViewDistance, BorderLayout.SOUTH);
//        jp.updateUI();
//
//    }

//    public void addObjectAndOrbit(Group grp, RenderingAttributes orbitAtrib) throws Exception{
//        createSphereAndOrbitPath(orbitAtrib);
//        for (PathShape os: orbitShapes)
//            grp.addChild(os);
//        grp.addChild(positionTrGrp);
//        addLocalViewingPlatform();
//    }

//    public void setScaleOLD (double scale) {
//        Transform3D tr = new Transform3D();
//        tgPlanet.getTransform(tr);
//        tr.setScale(scale);
//        tgPlanet.setTransform(tr);
//        viewScale = scale;
//    }

//    private void createSphereAndOrbitPath(RenderingAttributes orbitAtrib) throws Exception {
//        positionTrGrp = new TransformGroup();
//        trgAxis  = new TransformGroup();
//        tgPlanet = new TransformGroup();
//        tgPlanet.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
//        tgPlanet.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//        Transform3D alignPlanet = new Transform3D();
//        tgPlanet.getTransform(alignPlanet);
//        Transform3D turnNinetyX = new Transform3D();
//        turnNinetyX.rotX(Math.PI / 2);
//        alignPlanet.mul(turnNinetyX);
//
//        tgPlanet.setTransform(alignPlanet);
//        if (spinAxis != null) {
//            Transform3D axisT = new Transform3D();
//            trgAxis.getTransform(axisT);
//            axisT.set(spinAxis);
//            trgAxis.setTransform(axisT);
//        }
//        trgRotation = new TransformGroup();
//        trgRotation.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
//        trgRotation.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//        positionTrGrp.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//        ItemSphere planet = new ItemSphere(this);
//        tgPlanet.addChild(planet);
//        trgRotation.addChild(tgPlanet);
//        Light l = getLightIfEnabled();
//        if (l != null)
//            trgRotation.addChild(l);
//
//        trgAxis.addChild(trgRotation);
//        positionTrGrp.addChild(trgAxis);
//        color3f = new Color3f(color);
//        PointArrayFIFO onePtArr, lastOne = null;
//        orbitShapes = new PathShape[nShapeSets];
//        for (int os = 0; os < orbitShapes.length; os++) {
//            onePtArr = onePointArray(nPos, ((os == (orbitShapes.length - 1) ) ? 1: 4), GeometryArray.COORDINATES|GeometryArray.COLOR_3, color3f);
//            onePtArr.noteNextArray(lastOne);
//            orbitShapes[os] = new PathShape(planet, onePtArr, orbitAtrib);
//            lastOne = onePtArr;
//            if (os == (orbitShapes.length - 1))
//                ptArr = onePtArr;
//        }
//        updateOrbitAndPos();
//    }

//    PointArrayFIFO onePointArray(int vertexCount, int onceIn, int vertexFormat, Color3f color) {
//        PointArrayFIFO onePtArr = new PointArrayFIFO(vertexCount, onceIn, vertexFormat, color);
//        onePtArr.setCapability(PointArray.ALLOW_COORDINATE_READ);
//        onePtArr.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
//        onePtArr.setCapability(PointArray.ALLOW_COLOR_WRITE);
//        return onePtArr;
//    }

//    public PointLight getLightIfEnabled() {
//        PointLight light = null;
//        if (isLightSrc) {
//            light = new PointLight(true, new Color3f(1.0f, 1.0f, 1.0f), new Point3f(0, 0, 0), new Point3f(1, 0, 0));
//            light.setCapability(PointLight.ALLOW_POSITION_WRITE);
////            light.setAttenuation(1f, 1e-15f, 0f);
//            BoundingSphere bounds =
//                    new BoundingSphere(new Point3d(), 1e22);
//            light.setInfluencingBounds(bounds);
//        }
//        return light;
//    }

//    public void updateOrbitAndPosOLD() throws Exception{
//        ptArr.addCoordinate(status.pos);
//        updateObjectPosition();
//    }

//    private void updateObjectPositionOLD() throws Exception {
//        // position the planet
//        Transform3D tr = new Transform3D();
//        positionTrGrp.getTransform(tr);
//        Vector3d posVector = new Vector3d(status.pos);
//        tr.setTranslation(posVector);
//        posVector = null;
//        try {
//            positionTrGrp.setTransform(tr);
//        } catch (Exception e) {
//            String msg = name + " has some problem at " + status.time + " \n " + e.getMessage();
//            showError(msg);
//            throw(new Exception(msg));
//        }
//        updateSpin();
//    }

// ---------------------------------to shift to ItemGraphic

    // The new methods with ItemGraphic

    WeakReference<ItemGraphic> itemGraphic;

    public ItemGraphic createItemGraphic(Group grp, RenderingAttributes orbitAtrib) throws Exception{
        ItemGraphic itemG = new ItemGraphic(this);
        itemG.addObjectAndOrbit(grp, orbitAtrib);
        itemGraphic = null;
        itemGraphic = new WeakReference<ItemGraphic>(itemG);
        return itemG;
    }

    public void showLocalView(ViewingPlatform mainView, int atX, int atY, JPanel jp) {
        itemGraphic.get().showLocalView(mainView, atX, atY, jp);
    }

    public void showLocalView(JPanel jp) {
        itemGraphic.get().showLocalView(jp);
    }

    public void setScale (double scale) {
        itemGraphic.get().setScale(scale);
    }

    public void updateOrbitAndPos() throws Exception{
        itemGraphic.get().updateOrbitAndPos(getSpinIncrement());
     }

    public void enableLightSrc(boolean ena) {
        isLightSrc = ena;
    }

    //    =========================== calculations ======================
    public ItemStat getStatus() {
        return status;
    }

    void initForce() {
        if (bFixedForceOn)
            force.set(forceOfFixedGravity);
        else
            force.set(0, 0, 0);
        for (LocalAction action:localActions)
            force.add(action.getForce());
    }

    public void addToForce(Vector3d addForce) {
        force.add(addForce);
    }

    // dummy not used
    @Override
    public void evalOnce() {}

    @Override
    public void evalOnce(double deltaT, double nowT){
        try {
            updatePosAndVel(deltaT, nowT);
        } catch (Exception e) {
            ItemMovementsApp.log.error("In ITem evalOnce for " + name + ":" + e.getMessage());
            e.printStackTrace();
        }
    }

    void  updatePosAndVel(double deltaT, double nowT) throws Exception {  // deltaT is time is seconds
        if (!bFixedLocation) {
            Vector3d thisAcc = new Vector3d(force);
            thisAcc.scale((1.0 / mass));
            status.acc.set(thisAcc);
            // calculate from force
            Vector3d deltaV = new Vector3d(force);
            deltaV.scale(deltaT);
            deltaV.scale(1.0 / mass);
            Vector3d averageV = new Vector3d(deltaV);
            status.velocity.add(deltaV);
            averageV.scaleAdd(-0.5, status.velocity); // because the status.velocity is the new velocity
            Point3d deltaPos = new Point3d(averageV);
            deltaPos.scale(deltaT);
            status.pos.add(deltaPos);
            status.time = nowT;
            evalMaxMinPos();
            if (nowT > nextReport) {
                updateOrbitAndPos();
                nextReport += reportInterval;
            }
        }
    }

    void  updatePosAndVelOLD(double deltaT, double nowT) throws Exception {  // deltaT is time is seconds
        if (!bFixedLocation) {
            Vector3d thisAcc = new Vector3d(force);
            thisAcc.scale((1.0 / mass));
            status.acc.set(thisAcc);
            Vector3d deltaV = new Vector3d(status.acc);
            deltaV.scale(deltaT);
            Vector3d averageV = new Vector3d(deltaV);
            status.velocity.add(deltaV);
            averageV.scaleAdd(-0.5, status.velocity); // because the status.velocity is the new velocity
            Point3d deltaPos = new Point3d(averageV);
            deltaPos.scale(deltaT);
            status.pos.add(deltaPos);
            status.time = nowT;
            evalMaxMinPos();
            if (nowT > nextReport) {
                updateOrbitAndPos();
                nextReport += reportInterval;
            }
        }
    }

    double lastTime = 0;
    double radPerSec = 0;

    double getSpinIncrement() {
        double spinIncrement = 0;
        if (radPerSec > 0) {
            double nowTime = status.time;
            double interval = (nowTime - lastTime);
            spinIncrement = interval * radPerSec;
            lastTime = nowTime;
        }
        return spinIncrement;
    }

//    void updateSpin() {
//        if (radPerSec > 0) {
//            double nowTime = status.time;
//            double interval = (nowTime - lastTime);
//            double increment = interval * radPerSec;
//            Transform3D rotTr = new Transform3D();
//            trgRotation.getTransform(rotTr);
//            Transform3D rotZ = new Transform3D();
//            rotZ.rotZ(increment);
//            rotTr.mul(rotZ);
//            trgRotation.setTransform(rotTr);
//            lastTime = nowTime;
//        }
//    }

//    =============================================

    public StringBuilder statusStringForCSV(double posFactor, double velFactor) {
        StringBuilder csvStr = new StringBuilder(name + "\n");
        csvStr.append("Position , " + status.positionStringForCSV(posFactor) + "\n");
        csvStr.append("Velocity , ").append(status.velocityStringForCSV(velFactor)).append("\n");
        return csvStr;
    }


/*
    double fixedAcc = 9.81; // fixed acceleration value
    Vector3d forceOfFixedGravity;
    JRadioButton rbFixedAccOn;
    double xMax, yMax, zMax;
    double xMin, yMin, zMin;
    Vector3d force = new Vector3d();
    AxisAngle4d spinAxis; // absolute
    double spinPeriod; // in hours
    public Color color;
    public String imageName;

 */
    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("name", name));
        xmlStr.append(XMLmv.putTag("mass", mass)).append(XMLmv.putTag("dia", dia));
        xmlStr.append(XMLmv.putTag("bFixedLocation", bFixedLocation)).append(XMLmv.putTag("bFixedForceOn", bFixedForceOn));
        xmlStr.append(XMLmv.putTag("color", ("" + color.getRGB())));
        xmlStr.append(XMLmv.putTag("status", ("" + status.dataInXML())));
        if (bFixedForceOn) {

        xmlStr.append(XMLmv.putTag("dirOfFixedGravityAcc", dirOfFixedGravityAcc.dataInCSV())).
                append(XMLmv.putTag("fixedAcc", fixedAcc));
    }
        return xmlStr;
    }

    public boolean takeFromXMl(String xmlStr) throws NumberFormatException{
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "name", 0);
        name = vp.val;
        vp = XMLmv.getTag(xmlStr, "mass", 0);
        mass = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "dia", 0);
        dia = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "bFixedLocation", 0);
        bFixedLocation = (vp.val.equals("1"));
        vp = XMLmv.getTag(xmlStr, "bFixedForceOn", 0);
        bFixedForceOn = (vp.val.equals("1"));
        vp = XMLmv.getTag(xmlStr, "color", 0);
        color = new Color(Integer.valueOf(vp.val));
        vp = XMLmv.getTag(xmlStr, "status", 0);
        try {
            if (status == null)
                status = new ItemStat(vp.val);
            else
                status.takeFromXML(xmlStr);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Status data of " + name + " :" + e.getMessage());
        }
        if (dirOfFixedGravityAcc == null)
            dirOfFixedGravityAcc = new Vector3dMV(0, 0, 0);
        if (bFixedForceOn) {
            vp = XMLmv.getTag(xmlStr, "dirOfFixedGravityAcc", 0);
            try {
                if (dirOfFixedGravityAcc == null)
                    dirOfFixedGravityAcc = new Vector3dMV(vp.val);
                else
                    dirOfFixedGravityAcc.set(vp.val);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("dirOfFixedGravityAcc data of " + name + " :" + e.getMessage());
            }
            vp = XMLmv.getTag(xmlStr, "fixedAcc", 0);
            fixedAcc = Double.valueOf(vp.val);
        }
        return retVal;
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(parentW, name + " has some problem at " + status.time + " \n " + msg, "ERROR", JOptionPane.ERROR_MESSAGE);
        parentW.toFront();
    }

    void showMessage(String msg) {
        JOptionPane.showMessageDialog(parentW, msg, "FOR INFORMATION", JOptionPane.INFORMATION_MESSAGE);
        parentW.toFront();
    }

    @Override
    public boolean canNotify() {
        return true;
    }

    @Override
    public void enableNotify(boolean b) {

    }

    @Override
    public Window parent() {
        return null;
    }

    public String toString() {
        return name;
    }
}
