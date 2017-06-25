package GeneralElements.Display;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;
import mvUtils.display.FramedPanel;
import mvUtils.display.NumberLabel;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Transform3D;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by M Viswanathan on 04 Oct 2014
 */
public class LocalViewFrame  extends JFrame implements MouseListener, MouseMotionListener, MouseWheelListener {
    JPanel commonMenuPanel;
    MotionDisplay motionDisplay;
    Item itemInView;
    ItemMovementsApp controller;
    ViewingPlatform mainViewPlatform;
    JPanel localViewPanel;
    Canvas3D localViewCanvas;
    ViewingPlatform localVp;
    OrbitBehavior localVpOrbitBehavior;
    Item lastItemWithLocalPlatform = null;
    NumberLabel nlViewDistance;
    JPanel jpViewDistance;
    JLabel jlItemName;
    double viewPosFromPlanet;
    boolean bPlatformWasAttached = false;
    JCheckBox slowRevolveCB;  // slowing down MouseOrbit
    JButton jbControlPanel;
    JButton jbItemData;
    JButton jbItemEdit;

    LocalViewFrame(JPanel commonMenuPanel, ViewingPlatform mainViewPlatform, String name, ItemMovementsApp controller,
                   MotionDisplay motionDisplay) {
        super(name);
        setLayout(new BorderLayout());
        this.mainViewPlatform = mainViewPlatform;
        this.controller = controller;
        this.commonMenuPanel = commonMenuPanel;
        this.motionDisplay = motionDisplay;
        jbInit();
    }

    JPanel commonMenuPanelHolder;

    void jbInit() {
        prepareLocalViewPanel();
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

        add(localViewPanel, BorderLayout.CENTER);
        JPanel menuP = new JPanel(new GridBagLayout());
        GridBagConstraints outerGbc = new GridBagConstraints();
        outerGbc.insets = new Insets(5, 0, 5, 0);
        outerGbc.gridx = 0;
        outerGbc.gridy = 0;
        commonMenuPanelHolder = new JPanel();
        commonMenuPanelHolder.add(commonMenuPanel);
        menuP.add(commonMenuPanelHolder, outerGbc);
        outerGbc.gridy++;
        menuP.add(menuPanel(), outerGbc);
        add(menuP, BorderLayout.EAST);
        pack();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (controlPanel != null)
                    controlPanel.setVisible(false);
            }
        });

//        this.setSize(1100, 600);
    }

    public void showCommonMenu(boolean show) {
        if (show)
            commonMenuPanelHolder.add(commonMenuPanel);
        else
            commonMenuPanelHolder.remove(commonMenuPanel);
        pack();
    }

    public void addLocalViewingPlatform() {
        // create a Viewer and attach to its canvas
        // a Canvas3D can only be attached to a single Viewer
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        localViewCanvas = new Canvas3D(config);
        localViewCanvas.addMouseWheelListener(this);
        localViewCanvas.addMouseListener(this);
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
        localVpOrbitBehavior = new OrbitBehavior(localViewCanvas, OrbitBehavior.REVERSE_ROTATE);
        localVpOrbitBehavior.setSchedulingBounds(bounds);
        localVp.setViewPlatformBehavior(localVpOrbitBehavior);

        localVpOrbitBehavior.setRotationCenter(new Point3d(0, 0, 0));  //-viewPosFromPlanet));
        nlViewDistance = new NumberLabel(0, 150, "#,###");
        jpViewDistance = new JPanel();
        jpViewDistance.add(new JLabel("View Distance (km):"));
        jpViewDistance.add(nlViewDistance);
        jlItemName = new JLabel("Selected Item");
        localViewPanel.add(jlItemName, BorderLayout.NORTH);
        localViewPanel.add(localViewCanvas, BorderLayout.CENTER);
        localViewPanel.add(jpViewDistance, BorderLayout.SOUTH);
        localViewPanel.updateUI();
    }

    void prepareLocalViewPanel() {
        localViewPanel = new FramedPanel(new BorderLayout());
        localViewPanel.setPreferredSize(new Dimension(700, 600));
        addLocalViewingPlatform();
    }

    public void showLocalView(Item item) {
        jlItemName.setText(item.name);
        attachPlatformToItem(item);
        viewPosFromPlanet = 4 * item.dia;
        localVp.setNominalViewingTransform();
        Transform3D defaultTr = new Transform3D();
        localVp.getViewPlatformTransform().getTransform(defaultTr);
        defaultTr.setTranslation(new Vector3d(0, 0, viewPosFromPlanet));
        localVp.getViewPlatformTransform().setTransform(defaultTr);

        updateViewDistanceUI(1.0);
        setTitle(item.name);
//        showLocalViewFrame(item.name);
    }


    public void showLocalView(Item item, int atX, int atY) {
        jlItemName.setText(item.name);
        attachPlatformToItem(item);
        viewPosFromPlanet = 4 * item.dia;
        Transform3D mainVTr = new Transform3D();
        mainViewPlatform.getViewPlatformTransform().getTransform(mainVTr);

        Point3d eyePosINViewPlate= new Point3d();
        Viewer[] viewers = mainViewPlatform.getViewers();
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
        setTitle(item.name);
//        showLocalViewFrame(item.name);
    }

    private void attachPlatformToItem(Item item) {
        if (bPlatformWasAttached)
            lastItemWithLocalPlatform.detachPlatform();
        item.attachPlatform(localVp);
        lastItemWithLocalPlatform = item;
        bPlatformWasAttached = true;
        this.itemInView = item;
        jbControlPanel.setEnabled(itemInView.hasAnyAccessories());
    }

    void updateViewDistanceUI(double factor) {
        viewPosFromPlanet *= factor;
        nlViewDistance.setData(viewPosFromPlanet / 1000);
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
            controlPanel = itemInView.showControlPanel(controller, jbControlPanel);
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
                    itemInView.showItem(((iPassedIt) ?  "Paused (" + motionDisplay.nowTime() + ")" :""), controller, jbItemData);
                    if (iPassedIt)
                        motionDisplay.unPauseIt();
                }
            }
        });
        return jbItemData;
    }

    JComponent createItemEditButton() {
        jbItemEdit = new JButton("Edit Data");
        jbItemEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (bPlatformWasAttached) {
                    boolean iPassedIt = motionDisplay.pauseIfRunning();
                    itemInView.editItemKeepingPosition(((iPassedIt) ? "Paused (" + motionDisplay.nowTime() + ")" :""),
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
