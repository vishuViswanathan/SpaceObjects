package SpaceElements;

import SpaceElements.collection.PointArrayFIFO;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberLabel;
import mvUtils.display.NumberTextField;
import mvUtils.math.DoublePoint;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Created by IntelliJ IDEA.
 * User: M Viswanathan
 * Date: 11/24/13
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpaceObject implements InputControl{
    public String name;
    public double mass, dia;
    AxisAngle4d spinAxis; // absolute
    double spinPeriod; // in hours
    ObjectStat status;
    public SpaceObject primary; // primary center-object of revolution
    double xMax, yMax, zMax;
    double xMin, yMin, zMin;
    JTextField tfName;
    NumberTextField ntMass, ntDia;
    NumberTextField ntX, ntY, ntZ;
    NumberTextField ntVx, ntVy, ntVz;
//    NumberTextField ntfReportInterval;
    public double reportInterval = 7200; // 144000;
    double nextReport;
    long count = 0;
    ObjectHistory history;
    JPanel posHistoryPan;
    Component lastHistory;
    public Color color;
    boolean isLightSrc = false;
    public String imageName;
    TheSpace space;
//    OrbitShape orbitShape;
    OrbitShape[] orbitShapes = new OrbitShape[4];
    //        PointArray ptArr;
    PointArrayFIFO ptArr;
    int nPos = 3000; // number of positions
    Color3f color3f;
    TransformGroup orbitTrGrp;
    TransformGroup trgAxis;
    TransformGroup trgRotation;
//    TransformGroup trgView;
    OrbitBehavior vpOrbitBehavior;

    public SpaceObject (String name, double mass, double dia, Color color)  {
        this.name = name;
        this.mass = mass;
        this.dia = dia;
        this.color = color;
        status = new ObjectStat();
        history = new ObjectHistory(this);
    }

    public void setImage(String imageName) {
        this.imageName = imageName;
    }

    public void setPrimaryObject(SpaceObject primary) {
        this.primary = primary;
    }

    void setSpace(TheSpace space) {
        this.space = space;
    }

    void resetLimits() {
        xMax = Double.NEGATIVE_INFINITY;
        yMax = Double.NEGATIVE_INFINITY;
        zMax = Double.NEGATIVE_INFINITY;
        xMin = Double.POSITIVE_INFINITY;
        yMin = Double.POSITIVE_INFINITY;
        zMin = Double.POSITIVE_INFINITY;
    }

    public void initPosEtc(Point3d pos, Vector3d velocity) {
        resetLimits();
        status.initPos(pos, velocity);
//        this.pos.set(pos);
//        this.velocity.set(velocity);
        count = 0;
        nextReport = reportInterval;
        history.clear();
    }

    public void enableLightSrc(boolean ena) {
        isLightSrc = ena;
    }

    public PointLight getLightIfEnabled() {
        PointLight light = null;
        if (isLightSrc) {
            light = new PointLight(true, new Color3f(1.0f, 1.0f, 1.0f), new Point3f(0, 0, 0), new Point3f(1, 0, 0));
            light.setCapability(PointLight.ALLOW_POSITION_WRITE);
            light.setAttenuation(1f, 1e-15f, 0f);
            BoundingSphere bounds =
                    new BoundingSphere(new Point3d(), 1e22);
            light.setInfluencingBounds(bounds);
        }
        return light;
    }

    public void setSpin(AxisAngle4d spinAxis, double spinPeriod) {
        this.spinAxis = spinAxis;
        this.spinPeriod = spinPeriod;
        if (spinPeriod > 0)
            radPerSec = Math.PI * 2 / spinPeriod;
    }

    public ObjectStat getStatus() {
        return status;
    }
    void initAcc() {
        status.initAcc();
    }

    void initForce() {
        force.set(0, 0, 0);
    }

    public float getDiaFloat() {
         return new Float(dia);
    }

    void addToAcc(Vector3d addAcc) {
        status.addToAcc(addAcc);
//        acc.add(addAcc);
    }

    Vector3d force = new Vector3d();

    void addToForce(Vector3d addForce) {
        force.add(addForce);
    }

    double errDiff;
    double errDiff2;
    double errDiff3;

    void evalForce(SpaceObject duetoObject) {
        Vector3d distVect = new Vector3d();
        distVect.sub(duetoObject.status.pos, status.pos);    // pos - withObject.pos
        double r = distVect.length();
//        double force2  =  Constants.G * mass * duetoObject.mass / Math.pow(r, 2);
//        double force1  =  Constants.G * mass /  Math.pow(r, 2) * duetoObject.mass ;
        double force  =  (mass / r) * Constants.G * (duetoObject.mass / r) ;
//        double force3  =  mass / r * Constants.G * duetoObject.mass / r ;
//        errDiff = force - force1;
//        errDiff2 = force - force2;
//        errDiff3 = force - force3;
        double ratio = force / r;
        Vector3d nowForce = new Vector3d(distVect);
        nowForce.scale(ratio);
        addToForce(nowForce);
        nowForce.negate();
        duetoObject.addToForce(nowForce);
    }

    void evalAcceleration(SpaceObject duetoObject) {
        Vector3d distVect = new Vector3d();
        distVect.sub(duetoObject.status.pos, status.pos);    // pos - withObject.pos
        double r = distVect.length();
        double force  =  Constants.G * mass * duetoObject.mass / Math.pow(r, 2);
        double a = force / mass;
//       Vector3d addAcc;
        double ratio = a / r;
        Vector3d thisAcc = new Vector3d(distVect);
//        thisAcc.scale(ratio);
        thisAcc.scale(a);
        thisAcc.scale(1.0/r);
        addToAcc(thisAcc);
        ratio = mass / duetoObject.mass;
        thisAcc.scale(ratio);
        thisAcc.negate();
        duetoObject.addToAcc(thisAcc);
    }

    void  updatePosAndVel(double deltaT, double nowT)  {  // deltaT is time is seconds
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
            updateHistory();
            nextReport += reportInterval;
        }
    }

    void  updatePosAndVelOLD(double deltaT, double nowT)  {  // deltaT is time is seconds
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
            updateHistory();
            nextReport += reportInterval;
        }
    }

    void evalMaxMinPos() {
        xMax = Math.max(xMax, status.pos.x);
        yMax = Math.max(yMax, status.pos.y);
        zMax = Math.max(zMax, status.pos.z);
        xMin = Math.min(xMin, status.pos.x);
        yMin = Math.min(yMin, status.pos.y);
        zMin = Math.min(zMin, status.pos.z);
        count++;
//        tfCount.setText("" + count);
    }

    void updateHistory() {
        if (primary != null)
            status.distFromPrimary(primary);
        updateOrbitAndPos();
    }


/*
    void showStatus() {
        updateHistory();   // for the last time
        if (lastHistory != null)
            posHistoryPan.remove(lastHistory);
        lastHistory =  posHistoryPanel();
        posHistoryPan.add(lastHistory, BorderLayout.WEST);
    }
*/

    JButton relButton = new JButton("Set Relative Data");

    public JPanel dataPanel(int objNum) {
        JPanel outerPan = new JPanel(new BorderLayout());
        MultiPairColPanel jp = new MultiPairColPanel("Data of SpaceObject " + objNum);
        tfName = new JTextField(name, 8);
        jp.addItemPair("Object Name", tfName);
        ntMass = new NumberTextField(this, mass, 8, false, 1, 1e40, "##0.#####E00", "Mass in kg") ;
        jp.addItemPair(ntMass);
        ntDia = new NumberTextField(this, dia, 6, false, 1, 1e20, "##0.#####E00", "Dia inm");
        jp.addItemPair(ntDia);
        relButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getRelativeData(relButton);
            }
        });
        jp.addItemPair("", relButton);

        JPanel jpPos = new JPanel(new BorderLayout());
        ntX = new NumberTextField(this, status.pos.x, 8, false, -1e20, 1e20,"##0.#####E00", "X position im m" ) ;
        ntY = new NumberTextField(this, status.pos.y, 8, false, -1e20, 1e20,"##0.#####E00", "Y position im m" ) ;
        ntZ = new NumberTextField(this, status.pos.z, 8, false, -1e20, 1e20,"##0.#####E00", "Z position im m" ) ;
        jpPos.add(ntX, BorderLayout.WEST);
        jpPos.add(ntY, BorderLayout.CENTER);
        jpPos.add(ntZ, BorderLayout.EAST);
        jp.addItemPair("Position in m", jpPos);

        JPanel jpVel = new JPanel(new BorderLayout());
        ntVx = new NumberTextField(this, status.velocity.x, 8, false, -1e20, 1e20,"##0.#####E00", "X Velocity im m/s" ) ;
        ntVy = new NumberTextField(this, status.velocity.y, 8, false, -1e20, 1e20,"##0.#####E00", "Y Velocity im m/s" ) ;
        ntVz = new NumberTextField(this, status.velocity.z, 8, false, -1e20, 1e20,"##0.#####E00", "Z Velocity im m/s" ) ;
        jpVel.add(ntVx, BorderLayout.WEST);
        jpVel.add(ntVy, BorderLayout.CENTER);
        jpVel.add(ntVz, BorderLayout.EAST);
        jp.addItemPair("Velocity in m/s", jpVel);
//        ntfReportInterval = new NumberTextField(this, reportInterval / 3600, 8, false, 0.01, 1e20, "#,###.##", "Report Interval in h");
//        jp.addItemPair(ntfReportInterval);
        outerPan.add(jp, BorderLayout.WEST);
        posHistoryPan = new JPanel(new BorderLayout());
//        outerPan.add(posHistoryPan, BorderLayout.EAST);
        return outerPan;
    }

    RelativeDlg relDlg;

    void getRelativeData(JComponent butt) {
        space.noteInput();
        if (relDlg == null)
            relDlg = new RelativeDlg(this);
        relDlg.setLocationRelativeTo(butt);
        relDlg.setVisible(true);
    }

    void updateUI() {
        ntX.setData(status.pos.x);
        ntY.setData(status.pos.y);
        ntZ.setData(status.pos.z);

        ntVx.setData(status.velocity.x);
        ntVy.setData(status.velocity.y);
        ntVz.setData(status.velocity.z);
    }

    class RelativeDlg extends JDialog {
        SpaceObject parent;
        double rX, rY, rZ;
        double rvX, rvY, rvZ;
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        NumberTextField ntRelX, ntRelY, ntRelZ;
        NumberTextField ntRelVX, ntRelVY, ntRelVZ;
        InputControl inpC;
        JComboBox othersCB;
        RelativeDlg(InputControl inpC) {
            this.inpC = inpC;
            dbInit();
        }

        void dbInit() {
            MultiPairColPanel jp = new MultiPairColPanel("Relative Data of SpaceObject");
//            JPanel relP = new JPanel(new BorderLayout());
            othersCB = new JComboBox(space.getAllObjects().toArray());
            jp.addItemPair(new JLabel("Relative to "), othersCB);
            JPanel jpPos = new JPanel(new BorderLayout());
            ntRelX = new NumberTextField(inpC, rX, 8, false, -1e20, 1e20,"##0.#####E00", "X position im m" ) ;
            ntRelY = new NumberTextField(inpC, rY, 8, false, -1e20, 1e20,"##0.#####E00", "Y position im m" ) ;
            ntRelZ = new NumberTextField(inpC, rZ, 8, false, -1e20, 1e20,"##0.#####E00", "Z position im m" ) ;
            jpPos.add(ntRelX, BorderLayout.WEST);
            jpPos.add(ntRelY, BorderLayout.CENTER);
            jpPos.add(ntRelZ, BorderLayout.EAST);
            jp.addItemPair("Position in m", jpPos);

            JPanel jpVel = new JPanel(new BorderLayout());
            ntRelVX = new NumberTextField(inpC, rvX, 8, false, -1e20, 1e20,"##0.#####E00", "X Velocity im m/s" ) ;
            ntRelVY = new NumberTextField(inpC, rvY, 8, false, -1e20, 1e20,"##0.#####E00", "Y Velocity im m/s" ) ;
            ntRelVZ = new NumberTextField(inpC, rvZ, 8, false, -1e20, 1e20,"##0.#####E00", "Z Velocity im m/s" ) ;
            jpVel.add(ntRelVX, BorderLayout.WEST);
            jpVel.add(ntRelVY, BorderLayout.CENTER);
            jpVel.add(ntRelVZ, BorderLayout.EAST);
            jp.addItemPair("Velocity in m/s", jpVel);
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
            parent = space.getAllObjects().get(othersCB.getSelectedIndex());
            rX = ntRelX.getData();
            rY = ntRelY.getData();
            rZ = ntRelZ.getData();
            rvX = ntRelVX.getData();
            rvY = ntRelVY.getData();
            rvZ = ntRelVZ.getData();

            status.pos.x = parent.status.pos.x + rX;
            status.pos.y = parent.status.pos.y + rY;
            status.pos.z = parent.status.pos.z + rZ;
            status.velocity.x = parent.status.velocity.x + rvX;
            status.velocity.y = parent.status.velocity.y + rvY;
            status.velocity.z = parent.status.velocity.z + rvZ;
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
        status.pos.set(ntX.getData(), ntY.getData(), ntZ.getData());
        status.velocity.set(ntVx.getData(), ntVy.getData(), ntVz.getData());
//        reportInterval = ntfReportInterval.getData() *3600;
//        if (reportInterval <= 0) {
//            reportInterval = 3600; // 1h
//            ntfReportInterval.setData(reportInterval / 3600);
//        }
//        nextReport = reportInterval;
        resetLimits();
        history.clear();
        status.time = 0;
//        history.add(status);
     }

    public void setRefreshInterval(double interval, double nextRefresh) {
        reportInterval = interval;
        nextReport = nextRefresh;
    }

    JPanel posHistoryPanel() {
        JTable table = history.getTable();
        JPanel resultsPan = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        resultsPan.add(new JLabel("Position History of " + name) , gbc);
        gbc.gridy++;
        JScrollPane resultScroll = new JScrollPane(table);
        resultScroll.setBackground(SystemColor.lightGray);
        int w =  table.getPreferredSize().width;
        resultScroll.setPreferredSize(new Dimension(w, 200)); //table.getPreferredSize().width, 500));
        JPanel innerP = new JPanel();
        innerP.add(resultScroll);
        resultsPan.add(innerP, gbc);
        return resultsPan;
    }

    Canvas3D localVewCanvas;
    ViewingPlatform localVp;
    double viewPosFromPlanet = 0;
    double viewScale = 1;

    public void addLocalViewingPlatform() {
        // create a Viewer and attach to its canvas
        // a Canvas3D can only be attached to a single Viewer
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        localVewCanvas = new Canvas3D(config);
        localVewCanvas.addMouseWheelListener(new WheelListener());
        viewPosFromPlanet = 4 * dia * viewScale;

        Viewer viewer = new Viewer(localVewCanvas);
        viewer.getView().setBackClipDistance(1e22); //100 * viewPosFromPlanet);

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

        orbitTrGrp.addChild(localVp);
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

    public void showLocalView(ViewingPlatform mainView, JPanel jp) {
        putItOnPanel(jp);
    }

    public void showLocalView(ViewingPlatform mainView, int atX, int atY, JPanel jp) {
        viewPosFromPlanet = 4 * dia * viewScale;
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
        diff.sub(status.pos);
        double planetFromEye = diff.length();
        double factor = viewPosFromPlanet / planetFromEye;
        diff.scale(factor);
        Transform3D localVpt = new Transform3D(mainVTr);
        localVpt.setTranslation(diff);
        localVp.getViewPlatformTransform().setTransform(localVpt);
        updateViewDistanceUI(1.0);
        putItOnPanel(jp);
    }

    NumberLabel nlViewDistance;
    JPanel jpViewDistance;
    void putItOnPanel(JPanel jp) {
        jp.removeAll();
        jp.add(new JLabel(name), BorderLayout.NORTH);
        jp.add(localVewCanvas, BorderLayout.CENTER);
        jp.add(jpViewDistance, BorderLayout.SOUTH);
        jp.updateUI();

    }

    DoublePoint[] getXYHistory() {
        return history.getXYHistory();
    }

    public void addObjectAndOrbit(Group grp, RenderingAttributes orbitAtrib) {
        createSphereAndOrbitPath(orbitAtrib);
        for (OrbitShape os: orbitShapes)
            grp.addChild(os);
//        grp.addChild(orbitShape);
        grp.addChild(orbitTrGrp);
        addLocalViewingPlatform();
    }

    TransformGroup tgPlanet;

    public void setScale (double scale) {
        Transform3D tr = new Transform3D();
        tgPlanet.getTransform(tr);
        tr.setScale(scale);
        tgPlanet.setTransform(tr);
        viewScale = scale;
    }

    private void createSphereAndOrbitPath(RenderingAttributes orbitAtrib) {
        orbitTrGrp = new TransformGroup();
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
        if (spinAxis != null) {
            Transform3D axisT = new Transform3D();
            trgAxis.getTransform(axisT);
            axisT.set(spinAxis);
            trgAxis.setTransform(axisT);
        }
        trgRotation = new TransformGroup();
        trgRotation.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        trgRotation.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        orbitTrGrp.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Planet planet = new Planet(this);
        tgPlanet.addChild(planet);
        trgRotation.addChild(tgPlanet);
        Light l = getLightIfEnabled();
        if (l != null)
            trgRotation.addChild(l);

        trgAxis.addChild(trgRotation);
        orbitTrGrp.addChild(trgAxis);
        color3f = new Color3f(color);
        PointArrayFIFO onePtArr, lastOne = null;
        for (int os = 0; os < orbitShapes.length; os++) {
            onePtArr = onePointArray(nPos, ((os == (orbitShapes.length - 1) ) ? 1: 4), GeometryArray.COORDINATES|GeometryArray.COLOR_3, color3f);
            onePtArr.noteNextArray(lastOne);
            orbitShapes[os] = new OrbitShape(planet, onePtArr, orbitAtrib);
            lastOne = onePtArr;
            if (os == (orbitShapes.length - 1));
                ptArr = onePtArr;
        }
        updateOrbitAndPos();
    }

    PointArrayFIFO onePointArray(int vertexCount, int onceIn, int vertexFormat, Color3f color) {
        PointArrayFIFO onePtArr = new PointArrayFIFO(vertexCount, onceIn, vertexFormat, color);
        onePtArr.setCapability(PointArray.ALLOW_COORDINATE_READ);
        onePtArr.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
        onePtArr.setCapability(PointArray.ALLOW_COLOR_WRITE);
        return onePtArr;
    }



    double lastTime = 0;
    double radPerSec = 0;

    void updateSpin() {
        if (radPerSec > 0) {
            double nowTime = status.time;
            double interval = (nowTime - lastTime);
            double increment = interval * radPerSec;
            Transform3D rotTr = new Transform3D();
            trgRotation.getTransform(rotTr);
            Transform3D rotZ = new Transform3D();
            rotZ.rotZ(increment);
            rotTr.mul(rotZ);
            trgRotation.setTransform(rotTr);
            lastTime = nowTime;
        }
    }

    private void updateObjectPosition() {
        // position the planet
        Transform3D tr = new Transform3D();
        orbitTrGrp.getTransform(tr);
        tr.setTranslation(new Vector3d(status.pos));
        try {
            orbitTrGrp.setTransform(tr);
        } catch (Exception e) {
            showError(name + " has some problem\n " + e.getMessage());
        }
        updateSpin();
    }

    public void updateOrbitAndPos() {
        ptArr.addCoordinate(status.pos);
        updateObjectPosition();
    }

    public StringBuilder statusStringForCSV(double posFactor, double velFactor) {
        StringBuilder csvStr = new StringBuilder(name + "\n");
        csvStr.append("Position , " + status.positionStringForCSV(posFactor) + "\n");
        csvStr.append("Velocity , " + status.velocityStringForCSV(velFactor) + "\n");
        return csvStr;
    }

    void showError(String msg) {
        JOptionPane.showMessageDialog(parent(), msg, "ERROR", JOptionPane.ERROR_MESSAGE);
//        parent().toFront();
    }

    void showMessage(String msg) {
        JOptionPane.showMessageDialog(parent(), msg, "FOR INFORMATION", JOptionPane.INFORMATION_MESSAGE);
//        parent().toFront();
    }

    class WheelListener implements MouseWheelListener  {
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

    @Override
    public boolean canNotify() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void enableNotify(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Window parent() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String toString() {
        return name;
    }

}
