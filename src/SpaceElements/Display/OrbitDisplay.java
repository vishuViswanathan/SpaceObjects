package SpaceElements.Display;

import Applications.ObjectsInOrbit;
import SpaceElements.*;
import SpaceElements.time.DateAndJDN;
import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;
import display.NumberLabel;
import display.NumberTextField;
import mvmath.DoubleMaxMin;
import mvmath.FramedPanel;

import javax.media.j3d.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: M Viswanathan
 * Date: 12/24/13
 * Time: 10:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class OrbitDisplay extends JFrame implements MouseListener, MouseMotionListener, MouseWheelListener {
    TheSpace space;
    Transform3D defVPFtransform = new Transform3D();
    Transform3D defTGMaintransform = new Transform3D();
    ViewingPlatform vPf;
    TransformGroup tgMain;
    double maxOnOneSide;
    double duration;
//    JTextField zPos = new JTextField(60);
    ObjectsInOrbit controller;
//    public OrbitDisplay(ObjectHistory history) {
//        this.history = history;
//        jbInit();
//    }

    public OrbitDisplay(TheSpace space, double duration, ObjectsInOrbit controller) {
        this.controller = controller;
        this.space = space;
        this.duration = duration;
        jbInit();
    }

    JPanel localViewP;
    SimpleUniverse univ;
    void jbInit() {
        this.setSize(1300, 700);
        setJMenuBar(menuBar());
        setLayout(new BorderLayout());
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);
        add(canvas, BorderLayout.CENTER);
        localViewP = new FramedPanel(new BorderLayout());
        localViewP.setPreferredSize(new Dimension(700, 700));
        add(localViewP, BorderLayout.EAST);
        DoubleMaxMin xMaxMin = space.xMaxMin();
        DoubleMaxMin yMaxMin = space.yMaxMin();
        maxOnOneSide = Math.max(Math.max(xMaxMin.max, -xMaxMin.min), Math.max(yMaxMin.max, -yMaxMin.min));
        vPf = new ViewingPlatform();
        vPf.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        vPf.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        vPf.setNominalViewingTransform();
        Transform3D t3 = new Transform3D();
        vPf.getViewPlatformTransform().getTransform(t3);
        double viewPosFromOrigin = 3 * maxOnOneSide;
        t3.setTranslation(new Vector3d(0, 0, viewPosFromOrigin));
        TransformGroup vTg = vPf.getViewPlatformTransform();
        vTg.setTransform(t3);
        vTg.getTransform(defVPFtransform);
        addMouseAction(vTg);

        Viewer viewer = new Viewer( canvas );
        viewer.getView().setBackClipDistance(2 * viewPosFromOrigin);
        univ = new SimpleUniverse(vPf, viewer );
//        createLocalViewPf(univ);
        addMouseAction(vPf, canvas);  //.getViewPlatformTransform());
//        Locale locale = new Locale(univ);
        BranchGroup scene = createSceneGraph();
        univ.addBranchGraph(scene);
        setPick(canvas, scene);

//        JPanel dummy = new JPanel();
//        dummy.setPreferredSize(new Dimension(20,400));
//        localViewP.add(dummy, BorderLayout.WEST);
//        sceneObjects.get(0).oneObject.addLocalViewingPlatform(univ, addOnPanel);
    }

    OrbitBehavior vpOrbitBehavior;
    MouseRotate vpRotateBehavior;
    MouseTranslate vpTransBehavior;


    void addMouseAction(ViewingPlatform vP, Canvas3D canvas) {
        BoundingSphere bounds =
                new BoundingSphere(new Point3d(0.0,0.0,0.0), 10* maxOnOneSide);
        // with left button pressed
        vpOrbitBehavior = new OrbitBehavior(canvas);
        vpOrbitBehavior.setSchedulingBounds(bounds);
        vP.setViewPlatformBehavior(vpOrbitBehavior);

    }

    void addMouseAction(TransformGroup tg) {
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        BoundingSphere bounds =
                new BoundingSphere(new Point3d(0.0,0.0,0.0), 10* maxOnOneSide);
        // with left button pressed
        vpRotateBehavior = new MouseRotate();
//        debug("addMouseAction: rotateXFactor =" + vpRotateBehavior.getXFactor());
        vpRotateBehavior.setFactor(0.01);
        vpRotateBehavior.setTransformGroup(tg);
        vpRotateBehavior.setSchedulingBounds(bounds);
        tg.addChild(vpRotateBehavior);
        // with right button pressed
        vpTransBehavior = new MouseTranslate(MouseBehavior.INVERT_INPUT);
        setDefaultPan();
        vpTransBehavior.setTransformGroup(tg);
        vpTransBehavior.setSchedulingBounds(bounds);
        tg.addChild(vpTransBehavior);
    }

    JLabel nowTime = new JLabel();
    String  pauseStr = "Pause Orbit";
    String resumeStr = "Resume";
    String continueStr = "Continue";
    String stopItStr = "Stop Orbit";
    JButton pauseRunB = new JButton(pauseStr);
    JButton resetViewB = new JButton("Reset View");
    JButton stopB = new JButton(stopItStr);
    JButton resultsB = new JButton("Save Vectors");
    JScrollBar sbUpdateSpeed;
    NumberLabel lUpdateSpeed;
//    NumberLabel lCalculStep;
    JCheckBox chkBshowOrbit;

    JMenuBar menuBar() {
        JMenuBar bar = new JMenuBar();
        ActionListener l = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object src = e.getSource();
                if (src == resetViewB)
                    resetView();
                if (src == pauseRunB) {
                    if (isPauseInResume()) {
                        controller.continueOrbit(true);
                        tunePauseButt(false);
                    }
                    else {
                        controller.continueOrbit(false);
                        tunePauseButt(true);
                    }
                }
                if (src == stopB) {
                    if (isStopButtInContinue()) {
                        controller.oneMoreTime();
                        tuneStopButt(false);
                    }
                    else {
                        controller.stopIt();
                        tuneStopButt(true);
                    }
                }
                if (src == resultsB)
                    controller.writeCurrentVectorsToFile();
            }
        };
        resetViewB.addActionListener(l);
        bar.add(resetViewB);
        JPanel nowTp = new JPanel();
        nowTp.add(new JLabel("Time of Display "));
        nowTp.add(nowTime);
        bar.add(nowTp);
//        bar.add(zPos);
        bar.add(getPlanetSizeBar());
        bar.add(getSpeedSelector());
        bar.add(showOrbitCB());
        pauseRunB.addActionListener(l);
        bar.add(pauseRunB);
        stopB.addActionListener(l);
        bar.add(stopB);
        resultsB.setEnabled(false);
        resultsB.addActionListener(l);
        bar.add(resultsB);
        return bar;
    }

    void tuneStopButt(boolean toContinue) {
        if (toContinue) {
            controller.stopIt();
            resultsB.setEnabled(true);
            stopB.setText(continueStr);
            pauseRunB.setEnabled(false);
        }
        else {
            resultsB.setEnabled(false);
            stopB.setText(stopItStr);
            tunePauseButt(false);
            pauseRunB.setEnabled(true);
        }

    }

    boolean isStopButtInContinue() {
        return (stopB.getText().equals(continueStr));
    }

    void tunePauseButt(boolean toResume) {
        if (toResume) {
            pauseRunB.setText(resumeStr);
            resultsB.setEnabled(true);
        }
        else {
            pauseRunB.setText(pauseStr);
            resultsB.setEnabled(false);
        }
    }

    boolean isPauseInResume() {
        return (pauseRunB.getText().equals(resumeStr));
    }

    JPanel getSpeedSelector() {
        sbUpdateSpeed = new JScrollBar(JScrollBar.HORIZONTAL, 60, 20, 1, 3600);
        sbUpdateSpeed.setPreferredSize(new Dimension(100, 25));
        lUpdateSpeed = new NumberLabel(1, 60, "#,###.000 h/s");
//        lCalculStep = new NumberLabel(1, 60, "step #,###");
        sbUpdateSpeed.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                double speed = sbUpdateSpeed.getValue();
                controller.setRefreshInterval(speed);
            }
        });
        JPanel jp = new FramedPanel();
        jp.add(sbUpdateSpeed);
        jp.add(lUpdateSpeed);
//        jp.add(lCalculStep);
        return jp;
    }

    public void resultsReady() {
        tuneStopButt(true);
//        resultsB.setEnabled(true);
//        stopB.setText(continueStr);
//        pauseRunB.setEnabled(false);
    }

//    JComboBox<UpdateSpeed> getSpeedSelector() {
//        cbUpdateSpeed = new JComboBox<UpdateSpeed>();
//        speedChoice = new Vector<UpdateSpeed>();
//        speedChoice.add(new UpdateSpeed("Update s/s", 1.0));
//        speedChoice.add(new UpdateSpeed("Update min/s", 60));
//        speedChoice.add(new UpdateSpeed("Update h/s", 3600));
//        speedChoice.add(new UpdateSpeed("Update day/s", 3600 * 24));
//        speedChoice.add(new UpdateSpeed("Update month/s", 3600 * 24 * 30));
//        speedChoice.add(new UpdateSpeed("Update year/s", 3600 * 24 * 365));
//        cbUpdateSpeed = new JComboBox(speedChoice);
//        cbUpdateSpeed.addActionListener( new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                UpdateSpeed speed = (UpdateSpeed)(cbUpdateSpeed.getSelectedItem());
//                controller.setRefreshInterval(speed.secPerSec);
//            }
//        });
//        return cbUpdateSpeed;
//    }

    JCheckBox showOrbitCB() {
        chkBshowOrbit = new JCheckBox("Show Orbit", true);
        chkBshowOrbit.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                orbitAttrib.setVisible(chkBshowOrbit.isSelected());
            }
        });
        return chkBshowOrbit;
    }

    JScrollBar planetSizeBar;
    NumberTextField nlPlanetScale;

    JPanel getPlanetSizeBar() {
        planetSizeBar = new JScrollBar(JScrollBar.HORIZONTAL, 1, 20, 1, 500);
        planetSizeBar.setPreferredSize(new Dimension(200, 25));
        planetSizeBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                int val = planetSizeBar.getValue();
                nlPlanetScale.setData(val);
                if (val > 1)
                    nlPlanetScale.setBackground(Color.red);
                else
                    nlPlanetScale.setBackground(Color.cyan);
                for (int o = 0; o < space.nObjects(); o++)
                    space.getOneObject(o).setScale(val);
            }
        });
        JPanel jp = new FramedPanel();
        nlPlanetScale = new NumberTextField(null, 1, 4, false, 1, 1000, "####", "");
        nlPlanetScale.setBackground(Color.cyan);
        nlPlanetScale.setEditable(false);
        jp.add(new JLabel("Object Magnification:"));
        jp.add(planetSizeBar);
        jp.add(nlPlanetScale);
        return jp;
    }


    void resetView() {
        vPf.getViewPlatformTransform().setTransform(defVPFtransform);
        setDefaultPan();
    }

    void setDefaultPan() {
        vpTransBehavior.setFactor(maxOnOneSide / 100);
    }

    RenderingAttributes orbitAttrib;

    BranchGroup createSceneGraph() {
        BranchGroup brGrpMain = new BranchGroup();
        tgMain = new TransformGroup();
        orbitAttrib = new RenderingAttributes();
        orbitAttrib.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        orbitAttrib.setVisible(true);
        for (int o = 0; o < space.nObjects(); o++)
            space.getOneObject(o).addObjectAndOrbit(tgMain, orbitAttrib);
        tgMain.addChild(oneAxis(1, 1e13, Color.red));
        tgMain.addChild(oneAxis(2, 1e13, Color.blue));
        tgMain.addChild(oneAxis(3, -1e13, Color.lightGray));
        tgMain.getTransform(defTGMaintransform);
        brGrpMain.addChild(tgMain);
        return brGrpMain;
    }

//    void createLocalViewPf(SimpleUniverse u) {
//        for (int o = 0; o < space.nObjects(); o++)
//            space.getOneObject(o).addLocalViewingPlatform(u);
//    }

    PickCanvas pickCanvas;

    void setPick(Canvas3D canvas, BranchGroup scene) {
        pickCanvas = new PickCanvas(canvas, scene);
        pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
        pickCanvas.setTolerance(4.0f);
        canvas.addMouseListener(this);
//        canvas.addMouseMotionListener(this);
        canvas.addMouseWheelListener(this);
    }

    Shape3D oneAxis(int axis, double length, Color color) {
        LineArray line = new LineArray(2, GeometryArray.COORDINATES|GeometryArray.COLOR_3);
        Point3d point = new Point3d();
        switch (axis) {
            case 1: // x
                point = new Point3d(length, 0, 0);
                break;
            case 2: // y
                point = new Point3d(0, length, 0);
                break;
            case 3: // x
                point = new Point3d(0, 0, length);
                break;
        }
        line.setCoordinates(0, new Point3d[]{new Point3d(), point});
        Color3f[] colors = new Color3f[2];
        for (int c = 0; c < colors.length; c++)
            colors[c] = new Color3f(color);
        line.setColors(0, colors);
        return new Shape3D(line);
    }

    void zoom(double factor, Point3d objPos) {
//        debug("objPos =" + objPos);
        //get location of center of view screen
        Transform3D tr =  new Transform3D();
        vPf.getViewPlatformTransform().getTransform(tr);

        Transform3D vpT = new Transform3D();
        vPf.getViewPlatformTransform().getTransform(vpT);
        Vector3d eye = new Vector3d();
        vpT.get(eye);
        Point3d diff = new Point3d(eye);
        diff.sub(objPos);
        diff.scaleAdd(factor, objPos);
        tr.setTranslation(new Vector3d(diff));
        vPf.getViewPlatformTransform().setTransform(tr);
        adjustPanScale(objPos);
    }

    Point3d mouseSelPt = null;
    Point2i selPtOnScreen = new Point2i();

    void noteMouseSelObject(MouseEvent e) {
        if (mouseSelPt == null) {
            pickCanvas.setShapeLocation(e);
            PickResult result = pickCanvas.pickClosest();
            if (result == null) {
                vpOrbitBehavior.setEnable(false);
                vpRotateBehavior.setEnable(true);
            }
            if (result != null)   {
//                debug("noteMouseSelObject: Number of intersections :" + result.numIntersections());
                PickIntersection pInter = result.getIntersection(0);
                mouseSelPt = pInter.getPointCoordinatesVW();
                selPtOnScreen.x = e.getXOnScreen();
                selPtOnScreen.y = e.getYOnScreen();
                vpOrbitBehavior.setRotationCenter(mouseSelPt);
                vpOrbitBehavior.setEnable(true);
                vpRotateBehavior.setEnable(false);

                adjustPanScale(mouseSelPt);
            }
        }
    }

    void adjustPanScale(Point3d thePointOfInterest) {
        Transform3D tr = new Transform3D();
        vPf.getViewPlatformTransform().getTransform(tr);
        Vector3d eye = new Vector3d();
        tr.get(eye);
        Vector3d dist = new Vector3d(eye);
        dist.sub(thePointOfInterest);
        double distance = dist.length();
        vpTransBehavior.setFactor(distance / 100);
    }

    void showPlanet(Planet p) {
        p.planet.showLocalView(vPf, localViewP);
    }

    void showPlanet(Planet p, int atX, int atY) {
        p.planet.showLocalView(vPf, atX, atY, localViewP);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        pickCanvas.setShapeLocation(e);
        PickResult result = pickCanvas.pickClosest();
        Point3d pt = new Point3d();
        if (result != null)   {
            result.getClosestIntersection(pt);
            boolean done = false;
            Object  s = result.getNode(PickResult.SHAPE3D);
            if (s != null)  {
                if (s instanceof OrbitShape) {
                    showPlanet(((OrbitShape)s).planet);
                    done = true;
                }
            }
            if (!done) {
                Primitive p = (Primitive)result.getNode(PickResult.PRIMITIVE);
                if (p != null) {
                    if (p instanceof Planet) {
                        showPlanet((Planet)p, e.getX(), e.getY());
                        debug("Selected " + ((Planet)p).planet.name);
                     }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int butt = e.getButton();
        if (butt == MouseEvent.BUTTON1) {
            noteMouseSelObject(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseSelPt = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int movement = e.getUnitsToScroll();
        double factor = (movement > 0) ? 1.2: 1/1.2;
        pickCanvas.setShapeLocation(e);
        PickResult result = pickCanvas.pickClosest();
        if (result != null)   {
//            debug("pick result = "+ result);
            PickIntersection pInter = result.getIntersection(0);
            zoom(factor, pInter.getPointCoordinatesVW());
        }
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
double updateCount = 0;
    public void updateDisplay(DateAndJDN viewTime, double hrsPerSec) {
        nowTime.setText(sdf.format(viewTime.getTime()));
        lUpdateSpeed.setData(hrsPerSec);
//        lCalculStep.setData(updateCount++);
    }

    void debug(String msg) {
        System.out.println("OrbitDisplay: " + msg);
    }
}
