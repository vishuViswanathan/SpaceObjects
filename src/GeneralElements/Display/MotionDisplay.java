package GeneralElements.Display;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemInterface;
import GeneralElements.ItemSpace;
import GeneralElements.utils.ThreeDSize;
import collection.RelOrbitGroup;
import mvUtils.display.*;
import mvUtils.time.DateAndJDN;
import com.sun.j3d.utils.behaviors.mouse.MouseBehavior;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point2i;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by M Viswanathan on 23 May 2014
 */
public class MotionDisplay  extends JFrame
        implements MouseListener, MouseMotionListener, MouseWheelListener {
    enum ViewDirection {XMinus, YMinus, ZMinus}
    public ItemSpace space;
    Transform3D defVPFTransform = new Transform3D();
    boolean bViewSaved = false;
    Transform3D defTGMainTransform = new Transform3D();
    ViewingPlatform mainViewPlatform;
    TransformGroup tgMain;
    double maxOnOneSide;
    double duration;
    ItemMovementsApp controller;
    Vector<ItemGraphic> itemGraphics;
    RelOrbitGroup relOrbitGroup;


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
        itemGraphics = new Vector<>();
        this.setSize(1100, 700);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                localViewFrame.setVisible(false);
                controller.stopIt();
            }
        });
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                super.windowGainedFocus(e);
                showCommonMenu(true);
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                super.windowLostFocus(e);
                showCommonMenu(false);
            }
        });
        setLayout(new BorderLayout());
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        mainCanvas = new Canvas3D(config);
        add(mainCanvas, BorderLayout.CENTER);
        add(menuPanel(), BorderLayout.EAST);
        mainViewPlatform = new ViewingPlatform();
        mainViewPlatform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        mainViewPlatform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        mainViewPlatform.setNominalViewingTransform();
        Viewer viewer = new Viewer( mainCanvas );
        TransformGroup vTg = mainViewPlatform.getViewPlatformTransform();
        addMouseAction(vTg);
        univ = new SimpleUniverse(mainViewPlatform, viewer );
        setViewAll(ViewDirection.ZMinus);
        addMouseAction(mainViewPlatform, mainCanvas);  //.getViewPlatformTransform());
        BranchGroup scene;
        scene = createSceneGraph();
        localViewFrame = new LocalViewFrame(commonMenuPanel, mainViewPlatform, "Local View", controller, this);
        univ.addBranchGraph(scene);
        setPick(mainCanvas, scene);
        pauseRunB.doClick();
        relOrbitGroup = new RelOrbitGroup();
        relOrbitGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
        relOrbitGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        relOrbitGroup.setCapability(BranchGroup.ALLOW_DETACH);
    }

    ViewDirection lastViewDirection = ViewDirection.ZMinus;

    void setViewAll(ViewDirection direction) {
        ThreeDSize spaceSize = space.getSize();
        Point3d volumeCenter = spaceSize.midPoint();
//        debug("MotionDisplay.123: Volume Center " + volumeCenter);
        Vector3d volumeRange = spaceSize.range();
        double xByY = 1.3;
        // assuming xByY is > 1

        double maxOnSide;
        double viewPosFromVolumeCenter;
        Vector3d translateBy;
        Transform3D rot = new Transform3D();
        Transform3D rot1 = new Transform3D();
        mainViewPlatform.setNominalViewingTransform();
        switch (direction) {
            case XMinus:
                maxOnSide = Math.max(volumeRange.z, volumeRange.y * xByY) / 2;
                viewPosFromVolumeCenter = 3 * maxOnSide;
                translateBy = new Vector3d(volumeCenter.x + viewPosFromVolumeCenter, volumeCenter.y, volumeCenter.z);
                rot.rotX(Math.PI / 2);
                rot1.rotY(Math.PI / 2);
                rot.mul(rot1);
                break;
            case YMinus:
                maxOnSide = Math.max(volumeRange.x, volumeRange.z * xByY) / 2;
                viewPosFromVolumeCenter = 3 * maxOnSide;
                translateBy = new Vector3d(volumeCenter.x, volumeCenter.y + viewPosFromVolumeCenter, volumeCenter.z);
                rot.rotY(-Math.PI / 2);
                rot1.rotX(-Math.PI / 2);
                rot.mul(rot1);
                break;
            default:
                maxOnSide = Math.max(volumeRange.x, volumeRange.y * xByY) / 2;
                viewPosFromVolumeCenter = 3 * maxOnSide;
                translateBy = new Vector3d(volumeCenter.x, volumeCenter.y, volumeCenter.z + viewPosFromVolumeCenter);
                break;
        }
        Transform3D t3 = new Transform3D();
        mainViewPlatform.getViewPlatformTransform().getTransform(t3);
        Viewer viewer = univ.getViewer();
        viewer.getView().setBackClipDistance(2 * viewPosFromVolumeCenter);
        if (controller.spSize != ItemMovementsApp.SpaceSize.ASTRONOMICAL)
            viewer.getView().setFrontClipDistance(0.00001);
        t3.mul(rot);
        t3.setTranslation(translateBy);
        TransformGroup vTg = mainViewPlatform.getViewPlatformTransform();
        vTg.setTransform(t3);
        // TODO to adjust bounds for Orbit behaviour and Rotate Behaviour
//        defVPFTransform.set(t3);
        lastViewDirection = direction;
    }

    void showCommonMenu(boolean show) {
        if (show)
            commonMenuPanelHolder.add(commonMenuPanel);
        else
            commonMenuPanelHolder.remove(commonMenuPanel);
        pack();
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
    JLabel nowDate = new JLabel();
    JLabel timeLabel;
    String  pauseStr = "Pause Action";
    String resumeStr = "Resume";
    String continueStr = "Continue";
    String stopItStr = "Stop Action";
    JButton pauseRunB = new JButton(pauseStr);
    JCheckBox cBSelStatus;
    boolean bSelStatus = false;
    JButton viewAllZX = new JButton("View All in ZX plane");
    JButton viewAllXY = new JButton("View All in XY plane");
    JButton viewAllYZ = new JButton("View All in YZ plane");
    JComboBox<Item> cbItems;
    JButton jbShowSelected;
    JButton stopB = new JButton(stopItStr);
    JButton resultsB = new JButton("Save Vectors");
    JScrollBar sbUpdateSpeed;
    NumberLabel lUpdateSpeed;
    NumberLabel lSpeedSet;
    JButton jbShowItems;
    JCheckBox chkBshowPaths;
    JCheckBox chkBshowLinks;
    JCheckBox chkBrealTime;
    JCheckBox chkShowRelOrbits;

    JPanel commonMenuPanel;
    JPanel commonMenuPanelHolder;

    JPanel menuPanel() {
        JPanel outerP = new JPanel(new GridBagLayout());
        GridBagConstraints outerGbc = new GridBagConstraints();
        outerGbc.insets = new Insets(5, 0, 5, 0);
        outerGbc.gridx = 0;
        outerGbc.gridy = 0;

        ArrayList itemList = new ArrayList(space.getAlItems());
//        Collections.sort(itemList, new Comparator<Object>(){
//            @Override
//            public int compare(Object o1, Object o2) {
//                return o1.toString().compareTo(o2.toString());
//            }
//        });
//        The above with lamda for Comparator as modified below

        Collections.sort(itemList,
                (o1, o2) -> o1.toString().compareTo(o2.toString()));

        cbItems = new JComboBox(itemList.toArray());
        jbShowSelected = new JButton("Local View");
        outerGbc.gridy++;
        cBSelStatus = new JCheckBox("Select Status", bSelStatus);
        cBSelStatus.addActionListener(e-> {
            bSelStatus = cBSelStatus.isSelected();
        });
        outerP.add(cBSelStatus, outerGbc);
        outerGbc.gridy++;

        ActionListener l = e -> {
            Object src = e.getSource();
           block:
           {
               if (src == viewAllXY) {
                   setViewAll(ViewDirection.ZMinus);
                   break block;
               }
               if (src == viewAllYZ) {
                   setViewAll(ViewDirection.XMinus);
                   break block;
               }
               if (src == viewAllZX) {
                   setViewAll(ViewDirection.YMinus);
                   break block;
               }

               if (src == jbShowSelected) {
                   showLocalView((ItemInterface) cbItems.getSelectedItem());
                   break block;
               }
               if (src == stopB) {
                   if (isStopButtInContinue()) {
                       controller.oneMoreTime();
                       tuneStopButt(false);
                   } else {
                       controller.stopIt();
                       tuneStopButt(true);
                   }
                   break block;
               }
               if (src == resultsB) {
                   controller.writeCurrentVectorsToFile();
                   break block;
               }
           }
        };
        jbShowSelected.addActionListener(l);
        JPanel selP = new JPanel();
        selP.add(cbItems);
        selP.add(jbShowSelected);
        outerP.add(selP, outerGbc);
        outerGbc.gridy++;
        viewAllXY.addActionListener(l);
        outerP.add(viewAllXY, outerGbc);
        outerGbc.gridy++;
        viewAllYZ.addActionListener(l);
        outerP.add(viewAllYZ, outerGbc);
        outerGbc.gridy++;
        viewAllZX.addActionListener(l);
        outerP.add(viewAllZX, outerGbc);
        outerGbc.gridy++;
        commonMenuPanelHolder = new JPanel();
        commonMenuPanelHolder.add(commonMenuPanel());
        outerP.add(commonMenuPanelHolder, outerGbc);
        outerGbc.gridy++;
        stopB.setEnabled(false);
        stopB.addActionListener(l);
        outerP.add(stopB, outerGbc);
        outerGbc.gridy++;
        resultsB.setEnabled(false);
        resultsB.addActionListener(l);
        outerP.add(resultsB, outerGbc);
        return outerP;
    }

    /**
     * returns true if this caused the pause
     */
    public boolean pauseIfRunning() {
        if (isPauseInResume())
            return false;  // nothing to do since already inpause
        else {
            pauseIt();
            return true; // yes the caller caused it
        }
    }

    void pauseIt() {
        controller.continueOrbit(false);
        tunePauseButt(true);
    }

    public void unPauseIt() {
        controller.continueOrbit(true);
        tunePauseButt(false);
    }

    public String nowTime() {
        return timeLabel.getText() + ":" + nowTime.getText();
    }

    JPanel commonMenuPanel() {
        JPanel menuP = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        ActionListener l = e -> {
            Object src = e.getSource();
            if (src == pauseRunB) {
                if (isPauseInResume())
                    unPauseIt();
                else
                    pauseIt();
            }
        };
        FramedPanel nowTp = new FramedPanel();
        nowTp.setLayout(new BoxLayout(nowTp, BoxLayout.Y_AXIS));
        menuP.add(showItemsCB(), gbc);
        gbc.gridy++;
        if (controller.spSize == ItemMovementsApp.SpaceSize.ASTRONOMICAL)
            timeLabel = new JLabel("Time of Display ");
        else
            timeLabel = new JLabel("Elapsed Time (s) ");
        nowTp.add(timeLabel);
        nowTime.setSize(new Dimension(100, 20));
        nowTp.add(nowTime);
        nowDate.setSize(new Dimension(150, 20));
        nowTp.add(nowDate);
        menuP.add(nowTp, gbc);
        gbc.gridy++;
        menuP.add(getPlanetSizeBar(), gbc);
        gbc.gridy++;
//        menuP.add(showItemsCB(), gbc);
//        gbc.gridy++;
        menuP.add(getSpeedSelector(), gbc);
        gbc.gridy++;
//        if (controller.spSize != ItemMovementsApp.SpaceSize.ASTRONOMICAL) {
            menuP.add(showRealTimeCB(), gbc);
            gbc.gridy++;
//        }
        menuP.add(showLinksCB(), gbc);
        menuP.add(showLinksCB(), gbc);
        gbc.gridy++;
        if (controller.spSize == ItemMovementsApp.SpaceSize.ASTRONOMICAL) {
            menuP.add(showRelOrbitCB(), gbc);
            gbc.gridy++;
        }
        menuP.add(showOrbitCB(), gbc);
        gbc.gridy++;
        pauseRunB.addActionListener(l);
        menuP.add(pauseRunB, gbc);
        gbc.gridy++;
        commonMenuPanel = menuP;
        return commonMenuPanel;
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
        sbUpdateSpeed = new JScrollBar(JScrollBar.HORIZONTAL,
                getIntervalScrPos(nowInterval), scrollExt, scrollBarMin, scrollbarMax);
        sbUpdateSpeed.setPreferredSize(new Dimension(100, 25));
        lUpdateSpeed = new NumberLabel(1, 80, "#,###.000000 h/s");
        lUpdateSpeed.setSize(new Dimension(80, 20));
        lSpeedSet = new NumberLabel(nowInterval, 80, "#,###.00000000");
        lSpeedSet.setSize(new Dimension(80, 20));
        sbUpdateSpeed.addAdjustmentListener(e -> {
            int val = sbUpdateSpeed.getValue();
                nowInterval = getIntervalFromScrBar(val); // h
                controller.setRefreshInterval(nowInterval);
                lSpeedSet.setData(nowInterval);
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
        chkBshowPaths = new JCheckBox("Show Path", ItemMovementsApp.bShowPaths);
//        chkBshowPaths.setSelected(controller.bShowPaths);
         chkBshowPaths.addChangeListener(e -> {
             ItemMovementsApp.bShowPaths = chkBshowPaths.isSelected();
             setPathsVisible(ItemMovementsApp.bShowPaths);
         });
        return chkBshowPaths;
    }

    JCheckBox showLinksCB() {
        chkBshowLinks = new JCheckBox("Show Links", true);
        chkBshowLinks.setSelected(ItemMovementsApp.bShowLinks);
        chkBshowLinks.addChangeListener(e -> {
            ItemMovementsApp.bShowLinks = chkBshowLinks.isSelected();
            linkAttrib.setVisible(ItemMovementsApp.bShowLinks);
        });
        return chkBshowLinks;
    }


    void SelectItemsToShow() {
        ListSelect ls =
                new ListSelect(space.getItemsArray(), jbShowItems, true);
        ls.showSelectionDlg();
        ls.takeAction();
    }

    JComponent showItemsCB() {
         jbShowItems = new JButton("Set Items to Display");
         jbShowItems.addActionListener(e -> SelectItemsToShow());
         return jbShowItems;
    }

    void setRelOrbitsVisible(boolean visible) {
         for (ItemInterface i: space.getAlItems())
             i.setRelOrbitVisible(visible);
    }

    void setPathsVisible(boolean visible) {
        for (ItemInterface i: space.getAlItems())
            i.setPathVisible(visible);
    }

    JCheckBox showRealTimeCB() {
        chkBrealTime = new JCheckBox("Real Time", true);
        chkBrealTime.setSelected(controller.bRealTime);
        chkBrealTime.addChangeListener(e -> controller.bRealTime = chkBrealTime.isSelected());
        return chkBrealTime;
    }

    JCheckBox showRelOrbitCB() {
        chkShowRelOrbits = new JCheckBox("Show Rel Orbits", ItemMovementsApp.bShowRelOrbits);
        chkShowRelOrbits.setSelected(ItemMovementsApp.bShowRelOrbits);
        chkShowRelOrbits.addChangeListener(e -> {
            ItemMovementsApp.bShowRelOrbits = chkShowRelOrbits.isSelected();
            setRelOrbitsVisible(ItemMovementsApp.bShowRelOrbits);
        });
        return chkShowRelOrbits;
    }

    JScrollBar planetSizeBar;
    NumberTextField nlPlanetScale;

    JPanel getPlanetSizeBar() {
        planetSizeBar = new JScrollBar(JScrollBar.HORIZONTAL, 1, 20, 1, 500);
        planetSizeBar.setPreferredSize(new Dimension(100, 25));
        planetSizeBar.addAdjustmentListener(e -> {
            int val = planetSizeBar.getValue();
            nlPlanetScale.setData(val);
            if (val > 1)
                nlPlanetScale.setBackground(Color.red);
            else
                nlPlanetScale.setBackground(Color.cyan);
            for (int i = 0; i < space.nItems(); i++)
                space.getOneItem(i).setScale(val);
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
//        setViewAll(lastViewDirection);
        if (bViewSaved) {
            mainViewPlatform.getViewPlatformTransform().setTransform(defVPFTransform);
            setDefaultPan();
        }
    }

    void setDefaultPan() {
        vpTransBehavior.setFactor(maxOnOneSide / 100);
    }

    RenderingAttributes linkAttrib;

    BranchGroup createSceneGraph() throws Exception{
        BranchGroup brGrpMain = new BranchGroup();
        tgMain = null;
        tgMain = new TransformGroup();
        linkAttrib = new RenderingAttributes();
        linkAttrib.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        linkAttrib.setVisible(ItemMovementsApp.bShowLinks);

        space.addObjectAndOrbit(itemGraphics, tgMain, linkAttrib);
        tgMain.addChild(oneAxis(1, 1e13, Color.red));
        tgMain.addChild(oneAxis(2, 1e13, Color.blue));
        tgMain.addChild(oneAxis(3, 1e13, Color.lightGray));
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
//        canvas.addMouseWheelListener(this);
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

    boolean inMouseClick = false;

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!inMouseClick) {
            inMouseClick = true;
            mainVpOrbitBehavior.setEnable(true);
            pickCanvas.setShapeLocation(e);
            PickResult result = pickCanvas.pickClosest();
            Point3d pt = new Point3d();
            PickIntersection intersection;
            int button = e.getButton();
            if (result != null) {
                intersection = result.getClosestIntersection(pt);
                boolean done = false;
                Object s = result.getNode(PickResult.SHAPE3D);
                if (s != null) {
                    if (s instanceof PathShape) {
                        ItemInterface ii = ((PathShape) s).planet.getItem();
                        if (button == MouseEvent.BUTTON1) {
                            if (bSelStatus) {
                                showPoint(ii.getName(), intersection.getClosestVertexCoordinatesVW());
                            }
                            else {
                                showLocalView(ii);
                                debug("Selected via Path " + ii.getName());
                            }
                        }
                        else
                            cbItems.setSelectedItem(ii);
                        done = true;
                    }
                }
                if (!done) {
                    Object p = result.getNode(PickResult.GROUP);
                    if (p != null) {
                        if (p instanceof AttributeSetter) {
                            ItemInterface ii = ((AttributeSetter) p).getItem();
                            if (button == MouseEvent.BUTTON1)
                                showLocalView(ii, e.getX(), e.getY());
                            else
                                cbItems.setSelectedItem(ii);
                        }
                    }
                }
            }
            inMouseClick = false;
        }
        else
            debug("already in inMouseClick");
    }

    DecimalFormat posFmt = new DecimalFormat("#,###");
    void showPoint(String name, Point3d pt) {
       String msg = "X: " + posFmt.format(pt.x) + ", Y: " +
               posFmt.format(pt.y) + ", Z: " + posFmt.format(pt.z);
       ItemMovementsApp.showMessage("Position of " + name, msg, this);
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

    public void showLocalView(ItemInterface item) {
        localViewFrame.showLocalView(item,
                (controller.spSize == ItemMovementsApp.SpaceSize.ASTRONOMICAL));
        showLocalViewFrame(item.getName());
    }

    public void showLocalView(ItemInterface item, int atX, int atY) {
        localViewFrame.showLocalView(item, atX, atY,
                (controller.spSize == ItemMovementsApp.SpaceSize.ASTRONOMICAL));
        showLocalViewFrame(item.getName());
    }

//  ===============================      Transferred from ItemGraphics

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
    DecimalFormat nowTfmt = new DecimalFormat("#,###.000");
    Color colRealTime = Color.BLUE;
    Color colSimulation = Color.gray;
    public void updateDisplay(double nowT, DateAndJDN viewTime, double hrsPerSec, boolean bLive) {
        nowDate.setText(sdf.format(viewTime.getTime()));
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
