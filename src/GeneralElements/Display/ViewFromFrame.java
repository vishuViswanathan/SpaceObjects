package GeneralElements.Display;

import Applications.ItemMovementsApp;
import GeneralElements.DarkMatter;
import GeneralElements.Item;
import GeneralElements.ItemInterface;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;
import mvUtils.display.FramedPanel;
import mvUtils.display.NumberLabel;
import mvUtils.physics.Vector3dMV;

import javax.media.j3d.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by M Viswanathan on 11 November 2022
 */
public class ViewFromFrame  extends JFrame implements MouseListener, MouseMotionListener, MouseWheelListener {
    JPanel commonMenuPanel;
    MotionDisplay motionDisplay;
    ItemInterface itemViewFrom;
    ItemMovementsApp controller;
    ViewingPlatform mainViewPlatform;
    JPanel viewFromPanel;
    Canvas3D localViewCanvas;
    ViewingPlatform localVp;
    OrbitBehavior localVpOrbitBehavior;
    ItemInterface lastItemWithLocalPlatform = null;
    NumberLabel nlViewDistance;
    NumberLabel nlZoffset;
    NumberLabel nlFieldOfView;
    JPanel jpViewDistance;
    JLabel jlItemName;
    double viewPosFromPlanet;
    boolean bPlatformWasAttached = false;
    JCheckBox slowRevolveCB;  // slowing down MouseOrbit
    JButton jbControlPanel;
    JButton jbItemData;
    JButton jbItemEdit;
    JComboBox<Item> cbItems;
    JComboBox<Item> cbWhereToLook;
    View theView;
    double defaultFieldOfView = Math.PI / 4;
//    double nowFOV = defaultFieldOfView;
    double maxFieldOfView = defaultFieldOfView * 1.8;
    double minFieldOfView = 0.1 / 180 * Math.PI;
    double fieldOfViewFactor = 1.1;
    double viewFromZPos = 0;
    double viewFromZstepFactor = 0.02; // on dia of ViewFromItem
    double viewFromZstep = 500000;  // in m
    Item origin = new Item(this, "Origin");
    ItemInterface itemViewAt = origin;

    ViewFromFrame(JPanel commonMenuPanel, ViewingPlatform mainViewPlatform, String name, ItemMovementsApp controller,
                   MotionDisplay motionDisplay) {
        super();
        setLayout(new BorderLayout());
        this.mainViewPlatform = mainViewPlatform;
        this.controller = controller;
        this.commonMenuPanel = commonMenuPanel;
        this.motionDisplay = motionDisplay;
        jbInit();
    }

    JPanel commonMenuPanelHolder;

    void jbInit() {
        prepareViewFromPanel();
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

        add(viewFromPanel, BorderLayout.CENTER);
        JPanel menuP = new JPanel(new GridBagLayout());
        GridBagConstraints outerGbc = new GridBagConstraints();
        outerGbc.insets = new Insets(5, 0, 5, 0);
        outerGbc.gridx = 0;
        outerGbc.gridy = 0;
        menuP.add(viewTowardsCB(), outerGbc);
        outerGbc.gridy++;
        commonMenuPanelHolder = new JPanel();
        commonMenuPanelHolder.add(commonMenuPanel);
        menuP.add(commonMenuPanelHolder, outerGbc);
        outerGbc.gridy++;
        menuP.add(menuPanel(), outerGbc);
        add(menuP, BorderLayout.EAST);
        pack();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Dimension minSize = new Dimension(300, 200);
        setMinimumSize(minSize);
    }

    public void showCommonMenu(boolean show) {
        if (show)
            commonMenuPanelHolder.add(commonMenuPanel);
        else
            commonMenuPanelHolder.remove(commonMenuPanel);
        pack();
    }

    public void addViewFromPlatform() {
        // create a Viewer and attach to its canvas
        // a Canvas3D can only be attached to a single Viewer
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        localViewCanvas = new Canvas3D(config);
        localViewCanvas.addMouseWheelListener(this);
//        localViewCanvas.addMouseListener(this);
//        localViewCanvas.addKeyListener(new KeyboardListener());
        Viewer viewer = new Viewer(localViewCanvas);
        if (controller.spSize != ItemMovementsApp.SpaceSize.ASTRONOMICAL) {
            viewer.getView().setBackClipDistance(10000);
            viewer.getView().setFrontClipDistance(0.00001);
        }
        else {
            viewer.getView().setBackClipDistance(1e22); //100 * viewPosFromPlanet);
            viewer.getView().setFrontClipDistance(0.00001);

        }

        // create a ViewingPlatform with 1 TransformGroups above the ViewPlatform
        localVp = new ViewingPlatform();
        localVp.setNominalViewingTransform();
        viewer.setViewingPlatform(localVp);

        BoundingSphere bounds =
                new BoundingSphere(new Point3d(), 1e22);
        // with left button pressed
        localVpOrbitBehavior = new OrbitBehavior(localViewCanvas); //, OrbitBehavior.REVERSE_ROTATE);
        double rotXFactor = localVpOrbitBehavior.getRotXFactor();
        double rotYFactor = localVpOrbitBehavior.getRotYFactor();
        debug("rotXFactor = " + rotXFactor + "  rotYFactor = " + rotYFactor);
        localVpOrbitBehavior.setSchedulingBounds(bounds);
        localVp.setViewPlatformBehavior(localVpOrbitBehavior);

        localVpOrbitBehavior.setRotationCenter(new Point3d(0, 0, 0));  //-viewPosFromPlanet));
        nlViewDistance = new NumberLabel(0, 130, "#,### km");
        nlFieldOfView = new NumberLabel(defaultFieldOfView * 180 / Math.PI, 70, "#.000 deg");
        nlZoffset = new NumberLabel(viewFromZPos / 1000, 70, "#,### km");
        jpViewDistance = new JPanel();
//        jpViewDistance.add(new JLabel("View Distance:"));
//        jpViewDistance.add(nlViewDistance);
        jpViewDistance.add(new JLabel("             Field Of View:"));
        jpViewDistance.add(nlFieldOfView);
        jpViewDistance.add(new JLabel("             Z offset :"));
        jpViewDistance.add(nlZoffset);
        jlItemName = new JLabel("Selected Item");
        viewFromPanel.add(jlItemName, BorderLayout.NORTH);
        viewFromPanel.add(localViewCanvas, BorderLayout.CENTER);
        viewFromPanel.add(jpViewDistance, BorderLayout.SOUTH);
        theView = localViewCanvas.getView();
        viewFromPanel.updateUI();
    }

    void prepareViewFromPanel() {
        viewFromPanel = new FramedPanel(new BorderLayout());
        viewFromPanel.setPreferredSize(new Dimension(700, 600));
        addViewFromPlatform();
    }

    public void setViewFrom(ItemInterface item, boolean bShowRelOrbits) {
//        if (getState() == Frame.ICONIFIED ) {
//            System.out.println("was ICONIFIED");
//            setState(Frame.NORMAL);
//        }
        jlItemName.setText(item.getName());
        attachPlatformToItem(item, bShowRelOrbits);
        viewPosFromPlanet = 4 * ((DarkMatter)item).dia;
        localVp.setNominalViewingTransform();
//        Transform3D defaultTr = new Transform3D();
//        localVp.getViewPlatformTransform().getTransform(defaultTr);
//        defaultTr.setTranslation(new Vector3d(0, 0, viewPosFromPlanet));
//        localVp.getViewPlatformTransform().setTransform(defaultTr);
        itemViewAt = origin;
        setViewDirection(itemViewAt); // setViewAtDirection(new Point3d(0, 0, 0));
        setFOV(defaultFieldOfView);
//        theView.setFieldOfView(nowFOV);
        updateViewDistanceUI(1.0);
//        updateFOVUI(nowFOV);
        setTitle(item.getName());
        viewFromZstep = ((DarkMatter) item).dia * viewFromZstepFactor;
    }

    public void setViewDirection(ItemInterface viewAt) {
        setFOV(defaultFieldOfView);
//        nowFOV = defaultFieldOfView;
//        theView.setFieldOfView(nowFOV);
        itemViewAt = viewAt;
        setViewAtDirection(viewAt.getPos());
    }

    public void updateViewAt() {
        if (itemViewAt != origin)
            setViewAtDirection(itemViewAt.getPos());
    }

    public void setViewAtDirection(Point3d itemPos) {
        localVp.setNominalViewingTransform();
        Transform3D defaultTr = new Transform3D();
        localVp.getViewPlatformTransform().getTransform(defaultTr);

        Point3d eye = new Point3d(itemViewFrom.getPos());
        Vector3dMV vec = new Vector3dMV(itemPos);
        vec.sub(eye);
//        vec.normalize();
//        vec.scale(viewPosFromPlanet);
        Transform3D lookAt = new Transform3D();
        lookAt.lookAt(eye, itemPos, new Vector3d(0, 0, 1));
        lookAt.invert();
        lookAt.setTranslation(new Vector3d());
        localVp.getViewPlatformTransform().setTransform(lookAt);
        viewPosFromPlanet = vec.length();
        updateViewDistanceUI(1.0);
        updateViewDistanceUI(1.0);
        setTitle("View From" + itemViewFrom + " towards " + itemPos);
    }

    void moveZlocalVp(double deltaZ) {
        viewFromZPos += deltaZ;
        Transform3D tr = new Transform3D();
        localVp.getViewPlatformTransform().getTransform(tr);
        tr.setTranslation(new Vector3d(0, 0, viewFromZPos));
        localVp.getViewPlatformTransform().setTransform(tr);
        nlZoffset.setData(viewFromZPos / 1000);
    }

    private void attachPlatformToItem(ItemInterface item, boolean showRelOrbits) {
        if (bPlatformWasAttached) {
            lastItemWithLocalPlatform.detachPlatform();
            System.out.println("The earlier View-From Platform detached");
        }
        item.attachPlatform(localVp, showRelOrbits, motionDisplay.relOrbitGroup);
        lastItemWithLocalPlatform = item;
        bPlatformWasAttached = true;
        this.itemViewFrom = item;
        jbControlPanel.setEnabled(itemViewFrom.hasAnyAccessories());
    }

    void updateViewDistanceUI(double factor) {
        viewPosFromPlanet *= factor;
        nlViewDistance.setData(viewPosFromPlanet / 1000);
    }

    void updateFOVUI(double fov) {
        nlFieldOfView.setData(fov * 180 / Math.PI);
    }

    JPanel menuPanel() {
        JPanel menuP = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        menuP.add(createSlowRevolveCB(), gbc);
        gbc.gridy++;
        menuP.add(createControlPanelButton(), gbc);
        gbc.gridy++;
        menuP.add(createItemEditButton(), gbc);
        gbc.gridy++;
        menuP.add(createItemDataButton(), gbc);
        return menuP;
    }

    JCheckBox createSlowRevolveCB() {
        slowRevolveCB = new JCheckBox("Slow Revolve", false);
        slowRevolveCB.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean bSel = slowRevolveCB.isSelected();
                if (bSel) {
                    localVpOrbitBehavior.setRotXFactor(0.1);
                    localVpOrbitBehavior.setRotYFactor(0.1);
                }
                else {
                    localVpOrbitBehavior.setRotXFactor(1);
                    localVpOrbitBehavior.setRotYFactor(1);
                }
            }
        });
        return slowRevolveCB;
    }

    Window controlPanel;

    JComponent createControlPanelButton() {
        jbControlPanel = new JButton("Control Panel");
        jbControlPanel.addActionListener(e -> {
            controlPanel = itemViewFrom.showControlPanel(controller, jbControlPanel);
        });
        return jbControlPanel;
    }

    JComponent createItemDataButton() {
        jbItemData = new JButton("View Data");
        jbItemData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (bPlatformWasAttached) {
                    boolean iPassedIt = motionDisplay.pauseIfRunning();
                    itemViewFrom.showItem(((iPassedIt) ?  "Paused (" + motionDisplay.nowTime() + ")" :""), controller, jbItemData);
                    if (iPassedIt)
                        motionDisplay.unPauseIt();
                }
            }
        });
        return jbItemData;
    }

    JComponent viewTowardsCB() {
        FramedPanel p = new FramedPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel("View direction to"));

        ArrayList itemList = new ArrayList(motionDisplay.space.getAlItems());
        Collections.sort(itemList, new Comparator<Object>(){
            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

//        cbItems = new JComboBox(itemList.toArray());
//        Item origin = new Item(this, "Origin");
        cbWhereToLook = new JComboBox<>();
        cbWhereToLook.addItem(origin);
        for (Object i: itemList){
            cbWhereToLook.addItem((Item)i);
        }
        cbItems = cbWhereToLook;

//        cbItems = new JComboBox(motionDisplay.space.getAlItems().toArray());
        cbItems.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ItemInterface itemTowards = (ItemInterface)cbItems.getSelectedItem();
                if (itemTowards == itemViewFrom) {

                }
                else {
                    setViewDirection(itemTowards);
                }
            }
        });
        p.add(cbItems);
        return(p);
    }

    JComponent createItemEditButton() {
        jbItemEdit = new JButton("Edit Data");
        jbItemEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (bPlatformWasAttached) {
                    boolean iPassedIt = motionDisplay.pauseIfRunning();
                    itemViewFrom.editItemKeepingPosition(((iPassedIt) ? "Paused (" + motionDisplay.nowTime() + ")" :""),
                            controller, jbItemData);
                    if (iPassedIt)
                        motionDisplay.unPauseIt();
                }
            }
        });
        return jbItemEdit;
    }

    @Override
    public void mouseClicked(MouseEvent e) {


    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (controller.isItInMotion()) {
            debug("In Motion");
        } else {
            debug("Stopped");
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int movement = e.getUnitsToScroll();
        if (e.isShiftDown()) {
            double nowFOV = theView.getFieldOfView();
            if (movement > 0) {
                nowFOV *= fieldOfViewFactor;
                if (nowFOV > maxFieldOfView)
                    nowFOV = maxFieldOfView;
            }
            else {
                nowFOV /= fieldOfViewFactor;
                if (nowFOV < minFieldOfView)
                    nowFOV = minFieldOfView;
            }
//            theView.setFieldOfView(nowFOV);
            setFOV(nowFOV);
//            updateFOVUI(nowFOV);

//            debug("fieldOfView " + theView.getFieldOfView());
        }
        else if (e.isAltDown()) {
                if (movement > 0)
                    moveZlocalVp(-viewFromZstep);

                else
                    moveZlocalVp(viewFromZstep);
            }

//        else {
//            double factor = (movement > 0) ? 1.2 : 1 / 1.2;
//            Transform3D vpTr = new Transform3D();
//            localVp.getViewPlatformTransform().getTransform(vpTr);
//            Vector3d trans = new Vector3d();
//            vpTr.get(trans);
//            trans.scale(factor);
//            vpTr.setTranslation(trans);
//            localVp.getViewPlatformTransform().setTransform(vpTr);
//            updateViewDistanceUI(factor);
//        }

    }

    void setFOV(double fov) {
//        nowFOV = fov;
        theView.setFieldOfView(fov);
        updateFOVUI(fov);
        double factor = fov / defaultFieldOfView * 0.15;
        localVpOrbitBehavior.setRotFactors(factor, factor);
    }

    void debug(String msg) {
        ItemMovementsApp.debug("LocalViewFrame: " + msg);
    }
}

