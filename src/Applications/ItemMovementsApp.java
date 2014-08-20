package Applications;

import GeneralElements.Display.MotionDisplay;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.link.*;
import SpaceElements.Constants;
import SpaceElements.time.DateAndJDN;
import display.InputControl;
import display.NumberTextField;
import evaluations.EvalOnce;
import mvUtils.StringOps;
import mvXML.ValAndPos;
import mvXML.XMLmv;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import javax.swing.*;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.sql.*;
import java.util.Calendar;

/**
 * Created by M Viswanathan on 23 May 2014
 */
public class ItemMovementsApp extends JApplet implements InputControl {
    static public enum SpaceSize {
        ASTRONOMICAL("Astronomical (km)"),
        GLOBAL("Earth and Environment (m)"),
        DAILY("Daily objects (mm)"),
        MOLECULAR("Molecular (micrometer)");

        private final String proName;

        SpaceSize(String proName) {
            this.proName = proName;
        }

        public String getValue() {
            return name();
        }

        @Override
        public String toString() {
            return proName;
        }

        public static SpaceSize getEnum(String text) {
            SpaceSize retVal = null;
            if (text != null) {
                for (SpaceSize b : SpaceSize.values()) {
                    if (text.equalsIgnoreCase(b.proName)) {
                        retVal = b;
                        break;
                    }
                }
                if (retVal == null)
                    retVal = DAILY;
            }
            return retVal;
        }
    }
    public SpaceSize spSize;
    JComboBox cbSpaceSize = new JComboBox(SpaceSize.values());
    ItemSpace space;
    boolean asApp = false;
    JFrame mainF;
    DateAndJDN dateAndJDN = new DateAndJDN();
    JButton pbStart = new JButton("Start");
    JButton pbSaveData = new JButton("Save Data to File");
    JButton pbReadData = new JButton("Data From file");
    NumberTextField ntfDuration;  // , ntfStep;
    double duration = 2000; // in h
    double calculationStep =2; //in seconds
    public boolean bShowOrbit = false;
    public boolean bShowLinks = false;
    public static Logger log;

    public ItemMovementsApp() {
    }

    public ItemMovementsApp(boolean asApp) {
        this();
        this.asApp = asApp;
        init();
    }
    boolean createInputSummary = true;

    public void init() {
        if (log == null)
            log = Logger.getLogger(ItemMovementsApp.class);
        modifyJTextEdit();
        space = new ItemSpace(this);
        mainF = new JFrame("Items in Motion") ;
        mainF.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                mainF.dispose();
                System.exit(0);
            }
        });
        mainF.add(cbSpaceSize);
        cbSpaceSize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spSize = (SpaceSize) cbSpaceSize.getSelectedItem();
                switch (spSize) {
                    case ASTRONOMICAL:
                        duration = 200000;
                        calculationStep = 2; // s
                        refreshInterval = 100 * calculationStep;
                        if (getPlanetsFromDB()) {
//                            space.setGravityLinks();
//                            space.enableGlobalGravity(true);
                            proceedToItemList(false);
                        }
                        bShowOrbit = true;
                        bShowLinks = false;
                        break;
                    case DAILY:
                        duration = 200;
                        calculationStep = 0.000002;
                        refreshInterval = 10000 * calculationStep;
                        space.enableGlobalGravity(false);
//                        if (getTestData()) {
                        if (getMultiPendulum())  {
//                            log.info(space.getOneItem(0).dataInXML());
//                            log.info(space.getOneItem(1).dataInXML());
//                           space.setGravityLinks();
                            proceedToItemList(false);
                        }
                        bShowOrbit = false;
                        bShowLinks = true;
                        break;
                }
            }
        });
        mainF.pack();
        mainF.setVisible(true);
    }

    JComponent jcItemList;
    JComponent jcInfluenceList;
    JComponent jcAstroDate;

    void proceedToItemList(boolean reDo) {
        if (reDo) {
            if (jcItemList != null) mainF.remove(jcItemList);
            if (jcInfluenceList != null) mainF.remove(jcInfluenceList);
            if (jcAstroDate != null) mainF.remove(jcAstroDate);
        }
        else
            mainF.remove(cbSpaceSize);
        if (spSize == SpaceSize.ASTRONOMICAL) {
            JPanel dateP = new JPanel(new BorderLayout());
            dateP.add(dateAndJDN.panWithJDN(), BorderLayout.WEST);
            dateP.add(dateAndJDN.panWithDateTime(), BorderLayout.EAST);
            jcAstroDate = dateP;
            mainF.add(jcAstroDate, BorderLayout.NORTH);
        }
        jcItemList = space.itemListPanel();
        mainF.add(jcItemList, BorderLayout.WEST);
        jcInfluenceList = space.influenceListPanel();
        mainF.add(jcInfluenceList, BorderLayout.EAST);
        if (!reDo) {
            mainF.add(buttonPanel(), BorderLayout.SOUTH);
        }
        mainF.pack();
        mainF.setVisible(true);
    }

    BufferedOutputStream[] inputSummaryFiles;
    String[] inputJDNs;

    boolean addToInputFiles(String objName, String[] splitStr){
        boolean allOk = true;
        if (createInputSummary && splitStr.length == 33) {
            if (inputJDNs == null) {
                inputJDNs = new String[3];
                inputSummaryFiles = new BufferedOutputStream[3];
                for (int j = 0; j < 3; j++) {
                    inputJDNs[j] = splitStr[j * 11].trim();
                    String filePath = "results\\refVectorsAt" + inputJDNs[j] + ".csv";
                    try {
                        inputSummaryFiles[j] = new BufferedOutputStream(new FileOutputStream(filePath));
                        String header = "JDN " + inputJDNs[j] + " Vector data from Horizons\n" +
                                "Position Vector in AU - x, y, z\n" +
                                "Velocity Vector in AU/day - Vx, vY, vZ\n\n";
                        try {
                            inputSummaryFiles[j].write(header.getBytes());
                        } catch (IOException e) {
                            showError("Ünable to write Header to file " + filePath + "\n" + e.getMessage());
                            allOk = false;
                            break;
                        }

                    } catch (FileNotFoundException e) {
                        showError("Ünable to create file "+ filePath + "\n" + e.getMessage());
                        allOk = false;
                        break;
                    }
                }
            }
            if (allOk) {
                try {
                    BufferedOutputStream oS;
                    for (int j = 0; j < 3; j++) {
                        if (splitStr[j * 11].trim().equals(inputJDNs[j])) {
                            oS = inputSummaryFiles[j];
                            oS.write((objName + "\n").getBytes());
                            oS.write(("Position , " + splitStr[j * 11 + 2] + ", " +
                                    splitStr[j * 11 + 3] + ", " + splitStr[j * 11 + 4] + "\n").getBytes());
                            oS.write(("Velocity , " + splitStr[j * 11 + 5] + ", " +
                                    splitStr[j * 11 + 6] + ", " + splitStr[j * 11 + 7] + "\n").getBytes());
                        }
                    }
                } catch (IOException e) {
                    showError("Ünable to Write Vectors for "+ objName + "to reference Summary file\n" + e.getMessage());
                    allOk = false;
                }
            }
        }
        return allOk;
    }

    void closeInputFiles() {
        if (createInputSummary && (inputSummaryFiles != null)) {
            try {
                for (BufferedOutputStream s:inputSummaryFiles) {
                    s.close();
                }
            } catch (IOException e) {
                showError("Some Problem in closing Input Reference Summary Files");
            }
        }
        inputSummaryFiles = null;
        inputJDNs = null;
    }

    boolean getMultiPendulum() {
        Item itHook, itBall;
        ItemLink link;
        double z = 0, y;
        double x = -0.2;
        double zStep = 0.2;
        double len = 0.5;
        double lenStep = 0.05;

        double k = 10000;
        int nPendulums = 15;
        double mass1 = 0.1;
        double mass2 = 1;
        for (int i = 0; i < nPendulums; i++) {
            itHook =  new Item("Support" + i, mass1, 0.01, Color.blue, mainF);
            space.addItem(itHook);
            itHook.initPosEtc(new Point3d(0, 0, z), new Vector3d(0, 0, 0));
            itHook.setbFixedLocation(true);

            itBall =  new Item("Ball" + i, mass2, 0.05, Color.yellow, mainF);
            space.addItem(itBall);
            y = - Math.sqrt(Math.pow(len, 2) - Math.pow(x, 2));
            itBall.initPosEtc(new Point3d(x, y, z), new Vector3d(0, 0, 0));
            itBall.setbFixedForceOn(true);
//            itBall.addLocalAction(new AreaV2Friction(itBall, 10));

            link = new ItemLink(itHook, itBall, new Rod(itHook, itBall, len, k) , space);
            space.addItemLink(link);

            len += lenStep;
            z -= zStep;
        }
        return true;
    }

    boolean getTestData() {
        Item it;
        Item lastItem = null;
        int  lastPos = 0;
        ItemLink link;
        double pitch = 1;
        double k = 100;
        int nChain = 10;
        double mass1 = 1;
        double mass2 = 5;
        for (int i = 0; i < nChain; i++) {
            it =  new Item("I" + i, mass1, 0.1, Color.yellow, mainF);
            space.addItem(it);
            it.initPosEtc(new Point3d(0, -i * pitch, 0), new Vector3d(0, 0, 0));
            if (i > 0) {
                link = new ItemLink(lastItem, it, new Rod(lastItem, it, pitch, k) , space);
                space.addItemLink(link);
                it.setbFixedForceOn(true);
            }
            else
                it.setbFixedLocation(true);
            lastPos = i;
            lastItem = it;
        }
        it =  new Item("Ball", mass2, 0.5, Color.WHITE, mainF);
        space.addItem(it);
        it.initPosEtc(new Point3d(0, -(lastPos + 1) * pitch, 0), new Vector3d(2, 0, 0));
        link = new ItemLink(lastItem, it, new Rod(lastItem, it, pitch, k) , space);
        it.setbFixedForceOn(true);
        space.addItemLink(link);
        return true;
    }

    boolean getPlanetsFromDB() {
        Color[] colors = {Color.blue, Color.cyan, Color.white, Color.gray, Color.green, Color.BLUE,
                Color.CYAN, Color.GRAY, Color.GREEN, Color.yellow, Color.darkGray};
        int nowColor = 0;
        int nColors = colors.length;
        boolean bRetVal = false;
        File folder = new File("planetData");
        String[] fileNames = folder.list();
//        String[] fileNames = {"sun.csv", "mercury.csv", "earth.csv", "saturn.csv"};
        Item item;
        Statement st = createDBConnection("jdbc:odbc:ODBCtoPlanetData");
        String itemName;
        if (st != null) {
            for (String oneFile:fileNames) {
                if (oneFile.indexOf(".csv") > 0) {
                    itemName = oneFile.substring(0, oneFile.length() - 4);
                    item = getObjectFromTextFile(st, "planetData\\" + oneFile, itemName, colors[nowColor] );
                    if (item == null)  {
                        showError("Unable to create object " + itemName);
                    }
                    else {
                        space.addItem(item);
                        nowColor++;
                        // recycle colors
                        if (nowColor >= nColors)
                            nowColor = 0;
                    }
                }
            }
            closeDBconnection();
            bRetVal = true;
        }
        if (createInputSummary)
            closeInputFiles();
        return bRetVal;
    }

    JPanel buttonPanel() {
        JPanel jp = new JPanel();
        ntfDuration = new NumberTextField(this, duration, 8, false, 0.001, 1e10, "#,###.###", "Duration");
        MyListener listener = new MyListener();
        pbReadData.addActionListener(listener);
        pbSaveData.addActionListener(listener);
        pbStart.addActionListener(listener);
        JPanel durPanel = new JPanel(new BorderLayout());
        durPanel.add(new JLabel("Duration in h"), BorderLayout.WEST);
        durPanel.add(ntfDuration, BorderLayout.EAST);
        jp.add(pbReadData);
        jp.add(pbSaveData);
        jp.add(durPanel);
        jp.add(pbStart);
        return jp;
    }

    public int nItemLinks() {
        return space.nItemLinks();
    }

    public EvalOnce getLinkEvalOnce(int l) {
        return space.getOneLink(l);
    }

    public int nItems() {
        return space.nItems();
    }

    public EvalOnce getItemEvalOnce(int i) {
        return space.getOneItem(i);
    }

    //    Thread calTh;
    SpaceEvaluator evaluator;

    void startRunThread() {
        space.noteInput();
        if (showOrbitMap()) {
            runIt = true;
            SpaceEvaluator.closePool();
            evaluator = SpaceEvaluator.getSpaceEvaluator(this, true);
            evaluator.start();
        }

    }

    public void oneMoreTime() {
        evaluator = SpaceEvaluator.getSpaceEvaluator(this, false); // new SpaceEvaluator(this, false);
        evaluator.start();
    }

    boolean continueIt = true;
    boolean runIt = true;

    public void continueOrbit(boolean bContinue) {
        continueIt = bContinue;
    }

    public void stopIt() {
        runIt = false;
    }

    double refreshInterval = 60; // sec

    public void setRefreshInterval(double interval) {
//        log.info("refreshInterval changed from "  + refreshInterval + " to " + interval);
        refreshInterval = interval;
        nextRefresh = nowT + refreshInterval;
        for (int i = 0; i < space.nItems(); i++)
            space.getOneItem(i).setRefreshInterval(refreshInterval, nextRefresh);
    }

    void enableButtons(boolean ena) {
        pbStart.setEnabled(ena);
        space.enableButtons(ena);
    }

    DateAndJDN nowDate; // = new DateAndJDN(dateAndJDN);
    boolean showNow = false;
    //    Timer timer;
    double nowT = 0; // sec
    double nextRefresh = 0; // sec
    double lastRefresh = 0; // sec
    long lastTnano;
//    long lastStepTms;
    long nowTnano;
    public boolean bRealTime = false;
    double stepDeltaT;
//    boolean updateDisplayNow = false;

    void doCalculation(boolean fresh)   {
        enableButtons(false);
        double step = calculationStep;
        double hrsPerSec = 0;
        continueIt = true;
        double endT;
        if (fresh) {
            space.setGravityLinks();
            nowT = 0;
            setRefreshInterval(refreshInterval);
            nextRefresh = 0;
            endT = ntfDuration.getData() * 3600;
            lastTnano = System.nanoTime(); // new Date()).getTime();
            nowDate = new DateAndJDN(dateAndJDN);
            continueIt = true;
        }
        else {
            endT = nowT + ntfDuration.getData() * 3600;
        }
        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec); //.format(nowDate.getTime()));

        runIt = true;
        long lastStepNano = System.nanoTime();
        long nowStepNano, diffStepNano;
        while (runIt && nowT < endT) {
            if (continueIt) {
                try {
                    doOneStep(step, nowT);
                    if (bRealTime) {
                        nowStepNano = System.nanoTime();
                        diffStepNano = (nowStepNano - lastStepNano);
                        stepDeltaT = (double)(diffStepNano) / 1e9;
                        if (stepDeltaT <= calculationStep) {
                            step = stepDeltaT;
//                            debug("YES : stepDeltaT = " + stepDeltaT + ", calculationStep = " + calculationStep);
                        }
                        else {
                            step = calculationStep;
//                            debug("NO : stepDeltaT = " + stepDeltaT + ", calculationStep = " + calculationStep);
                        }
                    }
                    else {
                        step = calculationStep;
                    }

//                    if (++count > 1000) {
//                        nowStepNano = System.nanoTime();
//                        diffStepNano = (nowStepNano - lastStepNano);
//                        System.out.println(diffStepNano);
//                        count = 0;
//                    }

                    lastStepNano = System.nanoTime();
                    nowT += step;
                    if (nowT > nextRefresh) {
                        space.updateLinkDisplay();
                        showNow = false;
                        nowTnano = System.nanoTime(); //new Date().getTime();
                        double deltaT = ((double)(nowTnano - lastTnano))/ 1e9;
                        hrsPerSec = (refreshInterval / 3600) / deltaT;
                        nowDate.add(Calendar.SECOND, (int) (nowT - lastRefresh));
                        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec);
                        lastRefresh = nowT;
                        nextRefresh += refreshInterval;
                        lastTnano = nowTnano;
                    }
                } catch (Exception e) {
                    showError("Aborting in 'doCalculation' at nowT = " + nowT + " due to :" + e.getMessage());
                    runIt = false;
                }
            }
        }
        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec);
        orbitDisplay.resultsReady();
        enableButtons(true);
    }

    void doOneStep(double deltaT, double nowT) throws Exception {
        space.doCalculation(deltaT, nowT);
    }

    void doCalculationFast(boolean fresh)   {
//        space.prepareLinkEvaluator();
        enableButtons(false);
        double step = calculationStep;
        double hrsPerSec = 0;
        continueIt = true;
        double endT;
        if (fresh) {
            space.setGravityLinks();
            nowT = 0;
            setRefreshInterval(refreshInterval);
            nextRefresh = 0;
            endT = ntfDuration.getData() * 3600;
            lastTnano = System.nanoTime(); // new Date()).getTime();
            nowDate = new DateAndJDN(dateAndJDN);
            continueIt = true;
        }
        else {
            endT = nowT + ntfDuration.getData() * 3600;
        }
        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec); //.format(nowDate.getTime()));

        runIt = true;
        long lastStepNano = System.nanoTime();
        long nowStepNano, diffStepNano;
        while (runIt && nowT < endT) {
            if (continueIt) {
                try {
                    doOneStepFast(step, nowT);
//                    doOneStep(step, nowT);
                    if (bRealTime) {
                        nowStepNano = System.nanoTime();
                        diffStepNano = (nowStepNano - lastStepNano);
                        stepDeltaT = (double)(diffStepNano) / 1e9;
                        if (stepDeltaT <= calculationStep) {
                            step = stepDeltaT;
//                            debug("YES : stepDeltaT = " + stepDeltaT + ", calculationStep = " + calculationStep);
                        }
                        else {
                            step = calculationStep;
//                            debug("NO : stepDeltaT = " + stepDeltaT + ", calculationStep = " + calculationStep);
                        }
                    }
                    else {
                        step = calculationStep;
                    }
                    lastStepNano = System.nanoTime();
                    nowT += step;
                    if (nowT > nextRefresh) {
                        space.updateLinkDisplay();
                        showNow = false;
                        nowTnano = System.nanoTime(); //new Date().getTime();
                        double deltaT = ((double)(nowTnano - lastTnano))/ 1e9;
                        hrsPerSec = (refreshInterval / 3600) / deltaT;
                        nowDate.add(Calendar.SECOND, (int) (nowT - lastRefresh));
                        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec);
                        lastRefresh = nowT;
                        nextRefresh += refreshInterval;
                        lastTnano = nowTnano;
                    }
                } catch (Exception e) {
                    showError("Aborting in 'doCalculationFast()' at nowT = " + nowT + " due to :" + e.getMessage());
                    runIt = false;
                }
            }
        }
        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec);
        orbitDisplay.resultsReady();
        evaluator.stopTasks();
        enableButtons(true);
    }

    void doOneStepFast(double deltaT, double nowT) throws Exception {
        space.doCalculation(evaluator, deltaT, nowT);
    }

    MotionDisplay orbitDisplay;

    boolean showOrbitMap() {
        if (orbitDisplay != null) {
            orbitDisplay.cleanup();
            orbitDisplay.dispose();
        }
        try {
            orbitDisplay = new MotionDisplay(space, refreshInterval, duration, this);
            orbitDisplay.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosed(e);
                    pbStart.setEnabled(true);
                }
            });
            orbitDisplay.setVisible(true);
            return true;
        } catch (Exception e) {
            showError("In showOrbitMap : " + e.getMessage());
            return false;
        }
    }

    double dataJD = 0;
    double oneAuInM  = Constants.oneAuInkm * 1000;
    double secsPerDay = Constants.secsPerDay;

    boolean getFromXL() {
        boolean loaded = false;
        String filePath = "Default Planet Data from DE405.xls";
        Color[] colors = {Color.blue, Color.cyan, Color.white, Color.gray, Color.green, Color.BLUE,
                Color.CYAN, Color.GRAY, Color.GREEN, Color.yellow, Color.darkGray};
        try {
            FileInputStream xlInput = new FileInputStream(filePath);
            POIFSFileSystem xlFileSystem = new POIFSFileSystem(xlInput);

            /** Create a workbook using the File System**/
            HSSFWorkbook wB = new HSSFWorkbook(xlFileSystem);

            /** Get the first sheet from workbook**/
            HSSFSheet sh = wB.getSheet("PlanetData");
            if (sh != null) {
                dataJD = sh.getRow(0).getCell(1).getNumericCellValue();
                dateAndJDN = new DateAndJDN(dataJD);
                oneAuInM = sh.getRow(2).getCell(1).getNumericCellValue() * 1000;
                int gmRow = 6;
                int posRow = 20;

                for (int pl = 0; pl < 2; pl++ ) // Mercury anc Venus
                    space.addItem(getObjectFromXL(sh, gmRow++, posRow++, colors[pl]));
                Item tempEarth = getObjectFromXL(sh, gmRow, posRow, colors[2]);
                // gm ok for earth but the pos and velocity data are for EM-bary
                gmRow = 9;
                posRow = 23;
                for (int pl = 3; pl < 10; pl++ ) {// Mars, Jupiter, Saturn, Uranus, Neptune, Pluto, sun{
                    space.addItem(getObjectFromXL(sh, gmRow++, posRow++, colors[pl]));
                }
                Item tempMoon = getObjectFromXL(sh, gmRow, posRow, colors[9]); //yellow for moon
                // gm ok for moon but the pos and velocity data are for wrt earth
                // calculate positions wrt solar-bary
                Point3d mWRTe = new Point3d(tempMoon.getStatus().pos);
                Point3d emWRTe = new Point3d(mWRTe);
                emWRTe.scale(tempMoon.mass/ (tempMoon.mass + tempEarth.mass));
                Point3d eWRTsb = new Point3d(tempEarth.getStatus().pos); // the position of em-bary
                eWRTsb.sub(emWRTe);
                Point3d mWRTsb = new Point3d(mWRTe);
                mWRTsb.add(eWRTsb);
                // calculate velocities wrt solar-bary
                Vector3d vmWRTe = new Vector3d(tempMoon.getStatus().velocity);
                Vector3d veWRTem = new Vector3d(vmWRTe);
                veWRTem.scale(-tempMoon.mass/ (tempMoon.mass + tempEarth.mass));
                Vector3d veWRTsb = new Vector3d(tempEarth.getStatus().velocity); // the velocity of em-bary
                eWRTsb.add(veWRTem);
                Vector3d vmWRTsb = new Vector3d(vmWRTe);
                vmWRTsb.add(veWRTsb);
                tempEarth.initPosEtc(eWRTsb, veWRTsb);
//                tempEarth.setSpin(new AxisAngle4d(0, 1, 0, Math.PI / 180 * 23.5), (24 * 3600));
//                tempEarth.setSpin(null, (24 * 3600));
                space.addItem(tempEarth);
                tempMoon.initPosEtc(mWRTsb, vmWRTsb);
                space.addItem(tempMoon);

                sh = wB.getSheet("ESatelites");
                if (sh != null) {
                    double nSats = sh.getRow(0).getCell(1).getNumericCellValue();
                    gmRow = 6;
                    posRow = 20;
                    Vector3d vsatWRTsb;
                    Point3d satWRTsb;
                    for (double s = 0; s < nSats; s++) {
                        Item tempSat = getObjectFromXL(sh, gmRow++, posRow++, Color.RED);
                        satWRTsb = new Point3d(tempSat.getStatus().pos);
                        satWRTsb.add(eWRTsb);
                        vsatWRTsb = new Vector3d(tempSat.getStatus().velocity);
                        vsatWRTsb.add(veWRTsb);
                        tempSat.initPosEtc(satWRTsb, vsatWRTsb);
                        space.addItem(tempSat);
                    }
                }
                loaded = true;
            }
            xlInput.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return loaded;
    }

    double baseJDN = -1;

    Connection dbConnection;

    Statement createDBConnection(String connectTo) {
        Statement st = null;
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            dbConnection = DriverManager.getConnection(connectTo);
            st = dbConnection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  st;
    }

    void closeDBconnection() {
        if (dbConnection != null)
            try {
                dbConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        dbConnection = null;
    }


    Item baseObjectFromDBdata(Statement st, String objName, Color color) {
        Item obj = null;
        String name;
        String moonOf = "";
        String[] nameSplit = objName.split("-");
        int nItems = nameSplit.length;
        if (nItems > 0) {
            name = nameSplit[0].trim();
            if (nItems > 1) { // it is a moon of
                moonOf = nameSplit[1].trim();
            }
            double radius;
            double mass;
            double gm;
            double rotationPeriod;
            double axisInclination;
            boolean isLightSrc;
            String imageName;
            ResultSet res;
            String queryStr1 = "SELECT Radius, Mass, GM, imageName, rotationPeriodH, axisInclinationDeg, isLightSrc FROM PhycialData WHERE ObjName = '" + name + "' AND ";
            String queryStr2;
            try {
                if (nItems > 1) {
                    queryStr2 = "MoonOf = '" + moonOf + "'";
                }
                else
                    queryStr2 = " (NOT isMoon)";
                res = st.executeQuery(queryStr1 + queryStr2);
                if (res.next()) {
                    radius = res.getDouble("Radius");
                    try {
                        gm = res.getDouble("GM");
                        if (gm > 0) {
                            mass = gm / Constants.G;
//                            debug("got from GM for " + objName);
                        }
                        else
                            mass = res.getDouble("Mass");
                    } catch (SQLException e) {
                        mass = res.getDouble("Mass");
                    }
                    if (radius > 0 && mass > 0) {
                        obj = new Item(objName, mass, radius * 2 * 1000, color, mainF);
                        imageName = res.getString("imageName");
                        if (imageName != null && imageName.length() > 3) {
                            obj.setImage(imageName);
                            rotationPeriod = res.getDouble("rotationPeriodH");
                            axisInclination = res.getDouble("axisInclinationDeg");
                            isLightSrc = res.getBoolean("isLightSrc");
                            obj.setSpin(new AxisAngle4d(0, 1, 0, Math.PI / 180 * axisInclination ), rotationPeriod * 3600);
                            obj.enableLightSrc(isLightSrc);
                        }
                    }
                    else {
                        showError("There is some problem is Mass or radius of " + objName + ", taking as 1kg and 1m");
                        obj = new Item(objName, 1, 1 * 2, color, mainF);
                    }

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    Item getObjectFromTextFile(Statement st, String filePath, String objName, Color color) {
        Item obj;
        obj = baseObjectFromDBdata(st, objName, color);
        if (obj == null) {
            showError("There is some problem is Mass or dia of " + objName);
        }
        else {
            // now get the position and velocity params
            try {
                BufferedInputStream iStream = new BufferedInputStream(new FileInputStream(filePath));
                File f = new File(filePath);
                long len = f.length();
                if (len > 1000 && len < 15000) {
                    int iLen = (int) len;
                    byte[] data = new byte[iLen + 100];
                    try {
                        iStream.read(data);
                        String strDat = new String(data);
                        String vectors = StringOps.substring(strDat, "$$SOE", "$$EOE");
                        if (vectors.length() > 50) {
                            String[] split = vectors.split(",", 33);
                            try {
                                double nowJDN = Double.valueOf(split[0]);
                                boolean jdnOk = true;
                                if (baseJDN > 0)
                                    jdnOk = (nowJDN == baseJDN);
                                else
                                    dateAndJDN = new DateAndJDN(nowJDN); // the date of data
                                if (jdnOk) {
                                    if (createInputSummary)
                                        addToInputFiles(objName, split);
                                    baseJDN = nowJDN;
                                    double x = Double.valueOf(split[2]) * oneAuInM;
                                    double y = Double.valueOf(split[3]) * oneAuInM;
                                    double z = Double.valueOf(split[4]) * oneAuInM;
                                    double vx = Double.valueOf(split[5]) * oneAuInM / secsPerDay;
                                    double vy = Double.valueOf(split[6])* oneAuInM / secsPerDay;
                                    double vz= Double.valueOf(split[7])* oneAuInM / secsPerDay;
                                    obj.initPosEtc(new Point3d(x, y, z), new Vector3d(vx, vy, vz));
                                }
                                else {
                                    showError("Mismatch in JDN of data for " + objName + ", skipping it");
                                    obj = null;
                                }
                            } catch (NumberFormatException e) {
                                showError("Some problem in getting " + filePath + "\n"+ e.getMessage());
                                obj = null;
                            }
                        }
                        iStream.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                    showError("Length of File " + filePath + " is Out of range [" + len + "]");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    public boolean writeCurrentVectorsToFile() {
        boolean bRetVal = false;
        String filePath = "results\\statusVectors.csv";
        double posFactor = 1 / Constants.oneAuInkm / 1000;
        double velFactor = posFactor * Constants.secsPerDay;
        try {
            FileOutputStream stream = new FileOutputStream(filePath);
            BufferedOutputStream oStream = new BufferedOutputStream(stream);
            int nObj = space.nItems();

            try {
                String header = "JDN " + nowDate.getJdN() + "\n" +
                        "Position Vector in AU - x, y, z\n" +
                        "Velocity Vector in AU/day - Vx, vY, vZ\n\n";
                oStream.write(header.getBytes());
                for (int o = 0; o < nObj; o++) {
                    oStream.write(space.getOneItem(o).statusStringForCSV(posFactor, velFactor).toString().getBytes());
                }
                oStream.close();
                bRetVal = true;
            } catch (IOException e) {
                showError("Some problem wring Vectors to file " + filePath + "\n" + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            showError("Some problem saving Vectors to file " + filePath + "\n" + e.getMessage());
        }
        return bRetVal;
    }

    /*
    Get number after the dataName followed by '='
    */
    double getNumberAfter(String datStr, String dataName) {
        String src;
        double val = Double.NaN;
        int locGM = datStr.indexOf(dataName);
        if (locGM > 0) {
            locGM += dataName.length() + 1;
            src = datStr.substring(locGM);
            src = StringOps.ltrim(src);
            src = src.substring(0, 20);
            src = src.replace("=", " "); // removed '=' if exists
            src = StringOps.ltrim(src);
            int spaceLoc = src.indexOf(" ");
            if (spaceLoc > 1)
                src = src.substring(0, spaceLoc);
            try {
                val = Double.valueOf(src);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return val;
    }

    Item getObjectFromXL(Sheet sheet, int gmRow, int posRow,Color color ) {
        Row r = sheet.getRow(gmRow);
        String name = r.getCell(0).getStringCellValue();
        double gm = r.getCell(1).getNumericCellValue(); // au3/day2
        double Gau3day2 = Constants.G * secsPerDay / oneAuInM * secsPerDay / oneAuInM / oneAuInM;
        double mass = gm / Gau3day2;
        double dia = r.getCell(2).getNumericCellValue() * 1000; // in m
        double axisTilt = r.getCell(5).getNumericCellValue();
        double spinPeriod = r.getCell(6).getNumericCellValue();
        boolean isLightSrc = (r.getCell(4).getNumericCellValue() == 1.0);
        String imageName = r.getCell(3).getStringCellValue().trim();

        r = sheet.getRow(posRow);

        double x = r.getCell(1).getNumericCellValue() * oneAuInM; // im m
        double y = r.getCell(2).getNumericCellValue() * oneAuInM; // im m
        double z = r.getCell(3).getNumericCellValue() * oneAuInM; // im m

        double vx = r.getCell(4).getNumericCellValue() * oneAuInM / secsPerDay; // im m/s
        double vy = r.getCell(5).getNumericCellValue() * oneAuInM / secsPerDay; // im m/s
        double vz = r.getCell(6).getNumericCellValue() * oneAuInM / secsPerDay; // im m/s

        Item spObj = new Item(name, mass, dia, color, mainF);
        spObj.setImage(imageName);
        spObj.initPosEtc(new Point3d(x, y, z), new Vector3d(vx, vy, vz));
        spObj.setSpin(new AxisAngle4d(0, 1, 0, Math.PI / 180 * axisTilt ), spinPeriod * 3600);
        spObj.enableLightSrc(isLightSrc);
        return spObj;
    }

    void saveDataToFile() {
        boolean proceed = true;
        space.noteInput();
        if (space.anyUnsavedLink()) {
            proceed = decide("Unsaved Item/link", "Do you want to proceed abandoning the changes made?");
            if (proceed)
                space.resetLinkList();
        }
        if (proceed) {
            String xmlData = baseDatainXML() +
                            space.dataInXML().toString();

            String title = "Data for ItemMovementsApp";
            FileDialog fileDlg =
                    new FileDialog(mainF, title,
                            FileDialog.SAVE);
            fileDlg.setFile("*.imDat");
            fileDlg.setVisible(true);

            String bareFile = fileDlg.getFile();
            if (!(bareFile == null)) {
                int len = bareFile.length();
                if ((len < 8) || !(bareFile.substring(len - 6).equalsIgnoreCase(".imDat"))) {
                    showMessage("Adding '.imDat' to file name");
                    bareFile = bareFile + ".imDat";
                }
                String fileName = fileDlg.getDirectory() + bareFile;
                debug("Save Data file name :" + fileName);
                boolean goAhead = true;
                if (goAhead) {
                    try {
                        BufferedOutputStream oStream = new BufferedOutputStream(new FileOutputStream(fileName));
                        oStream.write(xmlData.getBytes());
                        oStream.close();
                    } catch (FileNotFoundException e) {
                        showError("File " + fileName + " NOT found!");
                    } catch (IOException e) {
                        showError("Some IO Error in writing to file " + fileName + "!");
                    }
                }
            }
            parent().toFront();
        }
    }

    boolean getDataFromFile() {
        boolean bRetVal = false;
        FileDialog fileDlg =
                new FileDialog(mainF, "Read Data",
                        FileDialog.LOAD);
        fileDlg.setFile("*.imDat");
        fileDlg.setVisible(true);
        String fileName = fileDlg.getFile();
        if (fileName != null) {
            String filePath = fileDlg.getDirectory() + fileName;
            if (!filePath.equals("nullnull")) {
                debug("Data file name :" + filePath);
                try {
                    BufferedInputStream iStream = new BufferedInputStream(new FileInputStream(filePath));
                    //           FileInputStream iStream = new FileInputStream(fileName);
                    File f = new File(filePath);
                    long len = f.length();
                    if (len > 100 && len < 50000) {
                        int iLen = (int) len;
                        byte[] data = new byte[iLen + 100];
                            iStream.read(data);
                        String xmlStr = new String(data);
                        if (getBaseDataFromXML(xmlStr) && space.takeFromXML(xmlStr)) {
                                bRetVal = true;
                                parent().toFront();
                             } else
                                showError("Unable to take data from file");
                    } else
                        showError("File size " + len + " for " + filePath);
                } catch (Exception e) {
                    showError("Some Problem in getting file!");
                }
             }
        }
        return bRetVal;
    }

    String baseDatainXML() {
        return XMLmv.putTag("baseData" , XMLmv.putTag("calculationStep", calculationStep) +
                XMLmv.putTag("refreshInterval", refreshInterval) +
                XMLmv.putTag("duration", duration) +
                XMLmv.putTag("bShowOrbit", bShowOrbit) +
                XMLmv.putTag("bShowLinks", bShowLinks) +
                XMLmv.putTag("spSize", spSize.toString()));
    }

    boolean getBaseDataFromXML(String xmlStr) {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "baseData", 0);
        String basicData = vp.val;
        if (basicData.length() > 20) {
//            space = new ItemSpace(this);
//            orbitDisplay = null;
            try {
                vp = XMLmv.getTag(basicData, "calculationStep", 0);
                calculationStep = Double.valueOf(vp.val);
                vp = XMLmv.getTag(basicData, "refreshInterval", 0);
                refreshInterval = Double.valueOf(vp.val) ;
                vp = XMLmv.getTag(basicData, "duration", 0);
                duration = Double.valueOf(vp.val) ;
                vp = XMLmv.getTag(basicData, "bShowOrbit", 0);
                bShowOrbit = vp.val.equals("1");
                vp = XMLmv.getTag(basicData, "bShowLinks", 0);
                bShowLinks = vp.val.equals("1");
                vp = XMLmv.getTag(basicData, "spSize", 0);
                spSize = SpaceSize.getEnum(vp.val);
            } catch (NumberFormatException e) {
                showError("Some problem in reading Basic Data :" + e.getMessage());
                retVal = false;
            }
        }
        else
            retVal = false;
        return retVal;
    }

    @Override
    public boolean canNotify() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void enableNotify(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Window parent() {
        return mainF;  //To change body of implemented methods use File | Settings | File Templates.
    }

    void showError(String msg) {
        log.info(msg);
        JOptionPane.showMessageDialog(parent(), msg, "ERROR", JOptionPane.ERROR_MESSAGE);
        parent().toFront();
    }

    void showMessage(String msg) {
        JOptionPane.showMessageDialog(parent(), msg, "FOR INFORMATION", JOptionPane.INFORMATION_MESSAGE);
        parent().toFront();
    }

    boolean decide(String title, String msg) {
        int resp = JOptionPane.showConfirmDialog(parent(), msg, title, JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION)
            return true;
        else
            return false;
    }


    class MyListener implements ActionListener {
        MyListener() {

        }
        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            aBlock: {
                if (src == pbStart) {
                    boolean proceed = true;
                    if (space.anyUnsavedLink()) {
                        proceed = decide("Unsaved Item/link", "Do you want to proceed abandoning the changes made?");
                        if (proceed)
                            space.resetLinkList();
                    }
                    if (proceed) {
                        duration = ntfDuration.getData();
                        if (duration > 0)
                            startRunThread();
                        pbStart.setEnabled(false);
                    }
                    break aBlock;
                }

                if (src == pbSaveData) {
                    saveDataToFile();
                    break aBlock;
                }

                if (src == pbReadData) {
                    if (getDataFromFile())
                        proceedToItemList(true);
                    break aBlock;
                }

            }
        }
    }

    void modifyJTextEdit() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addPropertyChangeListener("permanentFocusOwner", new PropertyChangeListener() {

                    public void propertyChange(final PropertyChangeEvent e) {

                        if (e.getOldValue() instanceof JTextField) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    JTextField oldTextField = (JTextField) e.getOldValue();
                                    oldTextField.setSelectionStart(0);
                                    oldTextField.setSelectionEnd(0);
                                }
                            });

                        }

                        if (e.getNewValue() instanceof JTextField) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    JTextField textField = (JTextField) e.getNewValue();
                                    textField.selectAll();
                                }
                            });

                        }
                    }
                });
    }

    void debug(String msg) {
        log.debug(msg);
    }

    public static void main(String[] args) {
        final ItemMovementsApp orbit = new ItemMovementsApp(true);
        orbit.setVisible(true);
    }

}
