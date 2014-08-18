package GeneralElements.Display;

import Applications.ItemMovementsApp;
import GeneralElements.ItemSpace;
import SpaceElements.time.DateAndJDN;
import com.sun.j3d.utils.behaviors.mouse.MouseBehavior;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
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
import javax.vecmath.Color3f;
import javax.vecmath.Point2i;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * Created by M Viswanathan on 23 May 2014
 */
public class MotionDisplay  extends JFrame implements MouseListener, MouseMotionListener, MouseWheelListener {
    ItemSpace space;
    Transform3D defVPFtransform = new Transform3D();
    Transform3D defTGMaintransform = new Transform3D();
    ViewingPlatform vPf;
    TransformGroup tgMain;
    double maxOnOneSide;
    double duration;
    ItemMovementsApp controller;
    Vector<ItemGraphic> itemGraphics;

    public MotionDisplay(ItemSpace space, double interval, double duration, ItemMovementsApp controller) throws Exception {
        super("Items in Motion");
        this.controller = controller;
        this.space = space;
        this.duration = duration;
        nowInterval = interval;
        controller.setRefreshInterval(interval);
        jbInit();
    }

    JPanel localViewP;
    SimpleUniverse univ;

//    public void clearAll() {
//        univ.removeAllLocales();
//        itemGraphics.clear();
//        itemGraphics = null;
//    }
    void jbInit() throws Exception {
        itemGraphics = new Vector<ItemGraphic>();
        this.setSize(1300, 700);
        addWindowListener(new WindowAdapter() {
            @Override
//            public void windowClosed(WindowEvent e) {
//                controller.stopIt();
//            }
            public void windowClosing(WindowEvent e) {
                controller.stopIt();
            }
         });
        setJMenuBar(menuBar());
        setLayout(new BorderLayout());
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);
        add(canvas, BorderLayout.CENTER);
        localViewP = new FramedPanel(new BorderLayout());
        localViewP.setPreferredSize(new Dimension(700, 700));
        add(localViewP, BorderLayout.EAST);
        //----
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
        if (controller.spSize != ItemMovementsApp.SpaceSize.ASTRONOMICAL)
            viewer.getView().setFrontClipDistance(0.00001);
        univ = new SimpleUniverse(vPf, viewer );
        addMouseAction(vPf, canvas);  //.getViewPlatformTransform());
        BranchGroup scene;
        scene = createSceneGraph();
        univ.addBranchGraph(scene);
        setPick(canvas, scene);
    }

    OrbitBehavior vpOrbitBehavior;
    MouseRotate vpRotateBehavior;
    MouseTranslate vpTransBehavior;

    public void cleanup() {
        if (univ != null)
            univ.cleanup();
        univ = null;
    }

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
    NumberLabel lSpeedSet;
    //    NumberLabel lCalculStep;
    JCheckBox chkBshowOrbit;
    JCheckBox chkBshowLinks;
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
        if (controller.spSize == ItemMovementsApp.SpaceSize.ASTRONOMICAL)
            nowTp.add(new JLabel("Time of Display "));
        else
            nowTp.add(new JLabel("Elapsed Time (s) "));
        nowTime.setSize(new Dimension(100, 20));
        nowTp.add(nowTime);
        bar.add(nowTp);
//        bar.add(zPos);
        bar.add(getPlanetSizeBar());
        bar.add(getSpeedSelector());
        bar.add(showOrbitCB());
        bar.add(showLinksCB());
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

    double minInterval, maxInterval, nowInterval; // h
    double logMinInterval, logMaxInterval, logSpan;
    int scrollBarMin = 0, scrollbarMax = 3600, scrollExt = 60, scrollBarNow = 1;
    double intervalSrFactor;

    int getIntervalScrPosOLD(double nowVal) {
        intervalSrFactor = (maxInterval - minInterval)/(scrollbarMax - scrollBarMin);
        return scrollBarMin + (int)((1 / intervalSrFactor) * (nowVal - minInterval));
    }

    double getIntervalFromScrBarOLD(int nowPos) {
        scrollBarNow = nowPos;
        return minInterval + (intervalSrFactor)*(nowPos - scrollBarMin);
    }

    int getIntervalScrPos(double nowVal) {
        logMinInterval = Math.log10(minInterval);
        logMaxInterval = Math.log10(maxInterval);
        logSpan = logMaxInterval - logMinInterval;
        intervalSrFactor = logSpan /(scrollbarMax - scrollBarMin);
        scrollBarNow = scrollBarMin + (int)((1 / intervalSrFactor) * (Math.log10(nowVal) - logMinInterval));
        return (scrollBarNow < scrollBarMin) ? scrollBarMin : scrollBarNow;
    }

    double getIntervalFromScrBar(int nowPos) {
        scrollBarNow = nowPos;
        double val = Math.pow(10, (logMinInterval + intervalSrFactor*(nowPos - scrollBarMin)));
        return val;
    }

    JPanel getSpeedSelector() {
        switch (controller.spSize) {
            case ASTRONOMICAL:
                minInterval = 1;
                maxInterval = 14400;
                break;
            case DAILY:
                minInterval = 0.0001;
                maxInterval = 10;
                break;
            default:
                minInterval = 1;
                maxInterval = 3600;
                break;
        }

        sbUpdateSpeed = new JScrollBar(JScrollBar.HORIZONTAL,
                getIntervalScrPos(nowInterval), scrollExt, scrollBarMin, scrollbarMax);
        sbUpdateSpeed.setPreferredSize(new Dimension(100, 25));
        lUpdateSpeed = new NumberLabel(1, 80, "#,###.000000 h/s");
        lUpdateSpeed.setSize(new Dimension(80, 20));
        lSpeedSet = new NumberLabel(nowInterval, 80, "#,###.00000000");
        lSpeedSet.setSize(new Dimension(80, 20));
        sbUpdateSpeed.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                int val = sbUpdateSpeed.getValue();
                if (val == scrollBarMin && controller.spSize != ItemMovementsApp.SpaceSize.ASTRONOMICAL) {
                    controller.bRealTime = true;
                    sbUpdateSpeed.setBackground(Color.RED);
                }
                else {
                    if (controller.bRealTime) {
                        controller.bRealTime = false;
                        sbUpdateSpeed.setBackground(Color.cyan);
                    }
                    nowInterval = getIntervalFromScrBar(val); // h
                    controller.setRefreshInterval(nowInterval);
                    lSpeedSet.setData(nowInterval);
                }
            }
        });
        JPanel jp = new FramedPanel();
        jp.add(lSpeedSet);
        jp.add(sbUpdateSpeed);
        jp.add(lUpdateSpeed);
//        jp.add(lCalculStep);
        return jp;
    }

    public void resultsReady() {
        tuneStopButt(true);
    }

    JCheckBox showOrbitCB() {
        chkBshowOrbit = new JCheckBox("Show Orbit", true);
        chkBshowOrbit.setSelected(controller.bShowOrbit);
         chkBshowOrbit.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                orbitAttrib.setVisible(chkBshowOrbit.isSelected());
//                controller.log.debug("chkBshowOrbit.isSelected()" + chkBshowOrbit.isSelected());
            }
        });
        return chkBshowOrbit;
    }

    JCheckBox showLinksCB() {
        chkBshowLinks = new JCheckBox("Show Links", true);
        chkBshowLinks.setSelected(controller.bShowLinks);
        chkBshowLinks.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean bSel = chkBshowLinks.isSelected();
               linkAttrib.setVisible(bSel);
            }
        });
        return chkBshowLinks;
    }

    JScrollBar planetSizeBar;
    NumberTextField nlPlanetScale;

    JPanel getPlanetSizeBar() {
        planetSizeBar = new JScrollBar(JScrollBar.HORIZONTAL, 1, 20, 1, 500);
        planetSizeBar.setPreferredSize(new Dimension(100, 25));
        planetSizeBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                int val = planetSizeBar.getValue();
                nlPlanetScale.setData(val);
                if (val > 1)
                    nlPlanetScale.setBackground(Color.red);
                else
                    nlPlanetScale.setBackground(Color.cyan);
                for (int i = 0; i < space.nItems(); i++)
                    space.getOneItem(i).setScale(val);
            }
        });
        JPanel jp = new FramedPanel();
        nlPlanetScale = new NumberTextField(null, 1, 4, false, 1, 1000, "####", "");
        nlPlanetScale.setBackground(Color.cyan);
        nlPlanetScale.setEditable(false);
        nlPlanetScale.setSize(new Dimension(30, 20));
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
    RenderingAttributes linkAttrib;

    BranchGroup createSceneGraph() throws Exception{
        BranchGroup brGrpMain = new BranchGroup();
        tgMain = null;
        tgMain = new TransformGroup();
        orbitAttrib = new RenderingAttributes();
        orbitAttrib.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        orbitAttrib.setVisible(controller.bShowOrbit);
        linkAttrib = new RenderingAttributes();
        linkAttrib.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        linkAttrib.setVisible(controller.bShowLinks);
        space.addObjectAndOrbit(itemGraphics, tgMain, orbitAttrib, linkAttrib);
        tgMain.addChild(oneAxis(1, 1e13, Color.red));
        tgMain.addChild(oneAxis(2, 1e13, Color.blue));
        tgMain.addChild(oneAxis(3, -1e13, Color.lightGray));
        tgMain.getTransform(defTGMaintransform);
        brGrpMain.addChild(tgMain);
        return brGrpMain;
    }

    PickCanvas pickCanvas;

    void setPick(Canvas3D canvas, BranchGroup scene) {
        pickCanvas = new PickCanvas(canvas, scene);
        pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
        pickCanvas.setTolerance(4.0f);
        canvas.addMouseListener(this);
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

    void showPlanet(ItemSphere p) {
        p.planet.showLocalView(localViewP);
    }

    void showPlanet(ItemSphere p, int atX, int atY) {
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
                if (s instanceof PathShape) {
                    showPlanet(((PathShape)s).planet);
                    debug("Selected via Path " + ((PathShape)s).planet.planet.name);
                    done = true;
                }
            }
            if (!done) {
                Primitive p = (Primitive)result.getNode(PickResult.PRIMITIVE);
                if (p != null) {
                    if (p instanceof ItemSphere) {
                        showPlanet((ItemSphere)p, e.getX(), e.getY());
                        debug("Selected " + ((ItemSphere)p).planet.name);
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
    DecimalFormat nowTfmt = new DecimalFormat("#,###.000000");

    public void updateDisplay(double nowT, DateAndJDN viewTime, double hrsPerSec) {
        if (controller.spSize == ItemMovementsApp.SpaceSize.ASTRONOMICAL)
            nowTime.setText(sdf.format(viewTime.getTime()));
        else
            nowTime.setText(nowTfmt.format(nowT)); // / 3600));

        lUpdateSpeed.setData(hrsPerSec);
    }

    void debug(String msg) {
        System.out.println("OrbitDisplay: " + msg);
    }
}
