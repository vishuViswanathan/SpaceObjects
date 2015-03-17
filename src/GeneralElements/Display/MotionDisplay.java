package GeneralElements.Display;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
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
import mvUtils.display.FramedPanel;
import mvUtils.display.NumberLabel;
import mvUtils.display.NumberTextField;
import mvUtils.math.DoubleMaxMin;

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
    Transform3D defVPFTransform = new Transform3D();
    Transform3D defTGMainTransform = new Transform3D();
    ViewingPlatform mainViewPlatform;
    TransformGroup tgMain;
    double maxOnOneSide;
    double duration;
    ItemMovementsApp controller;
    Vector<ItemGraphic> itemGraphics;

    public MotionDisplay(ItemSpace space, double interval, double duration, ItemMovementsApp controller) throws Exception {
        super("Objects in Motion");
        this.controller = controller;
        this.space = space;
        this.duration = duration;
        nowInterval = interval;
        controller.setRefreshInterval(interval);
        jbInit();
    }

     SimpleUniverse univ;

    void jbInit() throws Exception {
        itemGraphics = new Vector<ItemGraphic>();
        this.setSize(1300, 700);
        addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                localViewFrame.setVisible(false);
                controller.stopIt();
            }
         });
        setLayout(new BorderLayout());
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        mainCanvas = new Canvas3D(config);
        add(mainCanvas, BorderLayout.CENTER);
        add(menuPanel(), BorderLayout.EAST);
        DoubleMaxMin xMaxMin = space.xMaxMin();
        DoubleMaxMin yMaxMin = space.yMaxMin();
        maxOnOneSide = Math.max(Math.max(xMaxMin.max, -xMaxMin.min), Math.max(yMaxMin.max, -yMaxMin.min));
        mainViewPlatform = new ViewingPlatform();
        mainViewPlatform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        mainViewPlatform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        mainViewPlatform.setNominalViewingTransform();
        Transform3D t3 = new Transform3D();
        mainViewPlatform.getViewPlatformTransform().getTransform(t3);
        double viewPosFromOrigin = 3 * maxOnOneSide;
        t3.setTranslation(new Vector3d(0, 0, viewPosFromOrigin));
        TransformGroup vTg = mainViewPlatform.getViewPlatformTransform();
        vTg.setTransform(t3);
        vTg.getTransform(defVPFTransform);
        addMouseAction(vTg);
        Viewer viewer = new Viewer( mainCanvas );
        viewer.getView().setBackClipDistance(2 * viewPosFromOrigin);
        if (controller.spSize != ItemMovementsApp.SpaceSize.ASTRONOMICAL)
            viewer.getView().setFrontClipDistance(0.00001);
        univ = new SimpleUniverse(mainViewPlatform, viewer );
        addMouseAction(mainViewPlatform, mainCanvas);  //.getViewPlatformTransform());
        BranchGroup scene;
        scene = createSceneGraph();
        localViewFrame = new LocalViewFrame(mainViewPlatform, "Local View", controller);
//        prepareLocalViewPanel();
        univ.addBranchGraph(scene);
        setPick(mainCanvas, scene);
        pauseRunB.doClick();
    }


    OrbitBehavior mainVpOrbitBehavior;
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
        mainVpOrbitBehavior = new OrbitBehavior(canvas);
        mainVpOrbitBehavior.setSchedulingBounds(bounds);
        vP.setViewPlatformBehavior(mainVpOrbitBehavior);
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
    JLabel timeLabel;
    String  pauseStr = "Pause Action";
    String resumeStr = "Resume";
    String continueStr = "Continue";
    String stopItStr = "Stop Action";
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
    JCheckBox chkBrealTime;

    JPanel menuPanel() {
        JPanel menuP = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
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

        menuP.add(resetViewB, gbc);
        gbc.gridy++;
        FramedPanel nowTp = new FramedPanel();
        nowTp.setLayout(new BoxLayout(nowTp, BoxLayout.Y_AXIS));
         if (controller.spSize == ItemMovementsApp.SpaceSize.ASTRONOMICAL)
            timeLabel = new JLabel("Time of Display ");
        else
            timeLabel = new JLabel("Elapsed Time (s) ");
        nowTp.add(timeLabel);
        nowTime.setSize(new Dimension(100, 20));
        nowTp.add(nowTime);
        menuP.add(nowTp, gbc);
        gbc.gridy++;
        menuP.add(getPlanetSizeBar(), gbc);
        gbc.gridy++;
        menuP.add(getSpeedSelector(), gbc);
        gbc.gridy++;
        if (controller.spSize != ItemMovementsApp.SpaceSize.ASTRONOMICAL) {
            menuP.add(showRealTimeCB(), gbc);
            gbc.gridy++;
        }
        menuP.add(showLinksCB(), gbc);
        gbc.gridy++;
        menuP.add(showOrbitCB(), gbc);
        gbc.gridy++;
        pauseRunB.addActionListener(l);
        menuP.add(pauseRunB, gbc);
        gbc.gridy++;
        stopB.addActionListener(l);
        menuP.add(stopB, gbc);
        gbc.gridy++;
        resultsB.setEnabled(false);
        resultsB.addActionListener(l);
        menuP.add(resultsB, gbc);
        return menuP;
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
        return Math.pow(10, (logMinInterval + intervalSrFactor*(nowPos - scrollBarMin)));
    }

    JPanel getSpeedSelector() {
        minInterval = controller.calculationStep;
        maxInterval = controller.refreshInterval * 10;
//        switch (controller.spSize) {
//            case ASTRONOMICAL:
//                minInterval = 10;
//                maxInterval = 14400;
//                break;
//            case DAILY:
//                minInterval = 0.0001;
//                maxInterval = 10;
//                break;
//            default:
//                minInterval = 1;
//                maxInterval = 3600;
//                break;
//        }

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
                    nowInterval = getIntervalFromScrBar(val); // h
                    controller.setRefreshInterval(nowInterval);
                    lSpeedSet.setData(nowInterval);
            }
        });
        JPanel jp = new FramedPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
        jp.add(new JLabel("Update Speed"));
        jp.add(lSpeedSet);
        jp.add(sbUpdateSpeed);
        jp.add(lUpdateSpeed);
        return jp;
    }

    public void resultsReady() {
        tuneStopButt(true);
    }

    JCheckBox showOrbitCB() {
        chkBshowOrbit = new JCheckBox("Show Path", true);
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

    JCheckBox showRealTimeCB() {
        chkBrealTime = new JCheckBox("Real Time", true);
        chkBrealTime.setSelected(controller.bRealTime);
        chkBrealTime.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                controller.bRealTime = chkBrealTime.isSelected();
            }
        });
        return chkBrealTime;
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
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
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
        mainViewPlatform.getViewPlatformTransform().setTransform(defVPFTransform);
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
        tgMain.getTransform(defTGMainTransform);
        brGrpMain.addChild(tgMain);
        return brGrpMain;
    }

    Canvas3D mainCanvas;

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
        mainViewPlatform.getViewPlatformTransform().getTransform(tr);

        Transform3D vpT = new Transform3D();
        mainViewPlatform.getViewPlatformTransform().getTransform(vpT);
        Vector3d eye = new Vector3d();
        vpT.get(eye);
        Point3d diff = new Point3d(eye);
        diff.sub(objPos);
        diff.scaleAdd(factor, objPos);
        tr.setTranslation(new Vector3d(diff));
        mainViewPlatform.getViewPlatformTransform().setTransform(tr);
        adjustPanScale(objPos);
    }

    Point3d mouseSelPt = null;
    Point2i selPtOnScreen = new Point2i();

    void noteMouseSelObject(MouseEvent e) {
        if (mouseSelPt == null) {
            pickCanvas.setShapeLocation(e);
            PickResult result = pickCanvas.pickClosest();
            if (result == null) {
                mainVpOrbitBehavior.setEnable(false);
                vpRotateBehavior.setEnable(true);
            }
            if (result != null)   {
//                debug("noteMouseSelObject: Number of intersections :" + result.numIntersections());
                PickIntersection pInter = result.getIntersection(0);
                mouseSelPt = pInter.getPointCoordinatesVW();
                selPtOnScreen.x = e.getXOnScreen();
                selPtOnScreen.y = e.getYOnScreen();
                mainVpOrbitBehavior.setRotationCenter(mouseSelPt);
                mainVpOrbitBehavior.setEnable(true);
                vpRotateBehavior.setEnable(false);

                adjustPanScale(mouseSelPt);
            }
        }
    }

    void adjustPanScale(Point3d thePointOfInterest) {
        Transform3D tr = new Transform3D();
        mainViewPlatform.getViewPlatformTransform().getTransform(tr);
        Vector3d eye = new Vector3d();
        tr.get(eye);
        Vector3d dist = new Vector3d(eye);
        dist.sub(thePointOfInterest);
        double distance = dist.length();
        vpTransBehavior.setFactor(distance / 100);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mainVpOrbitBehavior.setEnable(true);
        pickCanvas.setShapeLocation(e);
        PickResult result = pickCanvas.pickClosest();
        Point3d pt = new Point3d();
        if (result != null) {
            result.getClosestIntersection(pt);
            boolean done = false;
            Object s = result.getNode(PickResult.SHAPE3D);
            if (s != null) {
                if (s instanceof PathShape) {
                    showLocalView(((PathShape) s).planet.planet);
                    debug("Selected via Path " + ((PathShape) s).planet.planet.name);
                    done = true;
                }
            }
            if (!done) {
                Primitive p = (Primitive) result.getNode(PickResult.PRIMITIVE);
                if (p != null) {
                    if (p instanceof ItemSphere) {
                        showLocalView(((ItemSphere) p).planet, e.getX(), e.getY());
//                                    showPlanet((ItemSphere) p, e.getX(), e.getY());
                        debug("Selected " + ((ItemSphere) p).planet.name);
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mainVpOrbitBehavior.setEnable(true);
        int butt = e.getButton();
        if (butt == MouseEvent.BUTTON1)
            noteMouseSelObject(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseSelPt = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mainVpOrbitBehavior.setEnable(true);
        vpRotateBehavior.setEnable(true);
        vpTransBehavior.setEnable(true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mainVpOrbitBehavior.setEnable(false);
        vpRotateBehavior.setEnable(false);
        vpTransBehavior.setEnable(false);
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
        double factor = (movement > 0) ? 1.2 : 1 / 1.2;
        pickCanvas.setShapeLocation(e);
        PickResult result = pickCanvas.pickClosest();
        if (result != null) {
//            debug("pick result = "+ result);
            PickIntersection pInter = result.getIntersection(0);
            zoom(factor, pInter.getPointCoordinatesVW());
        }
    }

//    Transferred from ItemGraphics ========================

    LocalViewFrame localViewFrame;

    void showLocalViewFrame(String showWhat) {
        localViewFrame.setTitle("Local View of " + showWhat);
        localViewFrame.setVisible(true);
    }

    public void showLocalView(Item item) {
//        jlItemName.setText(item.name);
//        attachPlatformToItem(item);
//        viewPosFromPlanet = 4 * item.dia;
//        localVp.setNominalViewingTransform();
//        Transform3D defaultTr = new Transform3D();
//        localVp.getViewPlatformTransform().getTransform(defaultTr);
//        defaultTr.setTranslation(new Vector3d(0, 0, viewPosFromPlanet));
//        localVp.getViewPlatformTransform().setTransform(defaultTr);
//        updateViewDistanceUI(1.0);
//
        localViewFrame.showLocalView(item);
        showLocalViewFrame(item.name);
    }

    public void showLocalView(Item item, int atX, int atY) {
//        jlItemName.setText(item.name);
//        attachPlatformToItem(item);
//        viewPosFromPlanet = 4 * item.dia;
//        Transform3D mainVTr = new Transform3D();
//        mainViewPlatform.getViewPlatformTransform().getTransform(mainVTr);
//
//        Point3d eyePosINViewPlate= new Point3d();
//        Viewer[] viewers = mainViewPlatform.getViewers();
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
//        diff.sub(item.status.pos);
//        double planetFromEye = diff.length();
//        double factor = viewPosFromPlanet / planetFromEye;
//        diff.scale(factor);
//        Transform3D localVpt = new Transform3D(mainVTr);
//        localVpt.setTranslation(diff);
//        localVp.getViewPlatformTransform().setTransform(localVpt);
//        updateViewDistanceUI(1.0);
        localViewFrame.showLocalView(item, atX, atY);
        showLocalViewFrame(item.name);
    }

//  ===============================      Transferred from ItemGraphics

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
    DecimalFormat nowTfmt = new DecimalFormat("#,###.000000");
    Color colRealTime = Color.BLUE;
    Color colSimulation = Color.gray;
    public void updateDisplay(double nowT, DateAndJDN viewTime, double hrsPerSec, boolean bLive) {
        if (controller.spSize == ItemMovementsApp.SpaceSize.ASTRONOMICAL)
            nowTime.setText(sdf.format(viewTime.getTime()));
        else
            nowTime.setText(nowTfmt.format(nowT)); // / 3600));
        if (bLive) {
            nowTime.setForeground(colRealTime);
        }
        else
            nowTime.setForeground(colSimulation);
        lUpdateSpeed.setData(hrsPerSec);
    }

    void debug(String msg) {
        System.out.println("MotionDisplay: " + msg);
    }
}
