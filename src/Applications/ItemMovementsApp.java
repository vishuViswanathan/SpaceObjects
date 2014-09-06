package Applications;

import GeneralElements.Display.MotionDisplay;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import GeneralElements.link.*;
import GeneralElements.localActions.FixedAcceleration;
import GeneralElements.localActions.V2Resistance;
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
import schemes.*;

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
                        DefaultScheme  scheme = new PlanetsAndMoons();
                        if (scheme.getScheme(mainF, space)) {
                            dateAndJDN = new DateAndJDN(scheme.startJDN());
//                        if (getPlanetsFromDB()) {
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
                        refreshInterval = 20000 * calculationStep;
                        space.enableGlobalGravity(false);
                        if ((new BungeeJumping()).getScheme(mainF, space)) {
//                        if ((new ChainWithBall()).getScheme(mainF, space)) {
//                        if (getMultiPendulum())  {
//                            if ((new MultiPendulum()).getScheme(mainF, space))  {
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

//    BufferedOutputStream[] inputSummaryFiles;
//    String[] inputJDNs;

//    boolean addToInputFiles(String objName, String[] splitStr){
//        boolean allOk = true;
//        if (createInputSummary && splitStr.length == 33) {
//            if (inputJDNs == null) {
//                inputJDNs = new String[3];
//                inputSummaryFiles = new BufferedOutputStream[3];
//                for (int j = 0; j < 3; j++) {
//                    inputJDNs[j] = splitStr[j * 11].trim();
//                    String filePath = "results\\refVectorsAt" + inputJDNs[j] + ".csv";
//                    try {
//                        inputSummaryFiles[j] = new BufferedOutputStream(new FileOutputStream(filePath));
//                        String header = "JDN " + inputJDNs[j] + " Vector data from Horizons\n" +
//                                "Position Vector in AU - x, y, z\n" +
//                                "Velocity Vector in AU/day - Vx, vY, vZ\n\n";
//                        try {
//                            inputSummaryFiles[j].write(header.getBytes());
//                        } catch (IOException e) {
//                            showError("Ünable to write Header to file " + filePath + "\n" + e.getMessage());
//                            allOk = false;
//                            break;
//                        }
//
//                    } catch (FileNotFoundException e) {
//                        showError("Ünable to create file "+ filePath + "\n" + e.getMessage());
//                        allOk = false;
//                        break;
//                    }
//                }
//            }
//            if (allOk) {
//                try {
//                    BufferedOutputStream oS;
//                    for (int j = 0; j < 3; j++) {
//                        if (splitStr[j * 11].trim().equals(inputJDNs[j])) {
//                            oS = inputSummaryFiles[j];
//                            oS.write((objName + "\n").getBytes());
//                            oS.write(("Position , " + splitStr[j * 11 + 2] + ", " +
//                                    splitStr[j * 11 + 3] + ", " + splitStr[j * 11 + 4] + "\n").getBytes());
//                            oS.write(("Velocity , " + splitStr[j * 11 + 5] + ", " +
//                                    splitStr[j * 11 + 6] + ", " + splitStr[j * 11 + 7] + "\n").getBytes());
//                        }
//                    }
//                } catch (IOException e) {
//                    showError("Ünable to Write Vectors for "+ objName + "to reference Summary file\n" + e.getMessage());
//                    allOk = false;
//                }
//            }
//        }
//        return allOk;
//    }

//    void closeInputFiles() {
//        if (createInputSummary && (inputSummaryFiles != null)) {
//            try {
//                for (BufferedOutputStream s:inputSummaryFiles) {
//                    s.close();
//                }
//            } catch (IOException e) {
//                showError("Some Problem in closing Input Reference Summary Files");
//            }
//        }
//        inputSummaryFiles = null;
//        inputJDNs = null;
//    }

//    boolean getPlanetsFromDB() {
//        Color[] colors = {Color.blue, Color.cyan, Color.white, Color.gray, Color.green, Color.BLUE,
//                Color.CYAN, Color.GRAY, Color.GREEN, Color.yellow, Color.darkGray};
//        int nowColor = 0;
//        int nColors = colors.length;
//        boolean bRetVal = false;
//        File folder = new File("planetData");
//        String[] fileNames = folder.list();
////        String[] fileNames = {"sun.csv", "mercury.csv", "earth.csv", "saturn.csv"};
//        Item item;
//        Statement st = createDBConnection("jdbc:odbc:ODBCtoPlanetData");
//        String itemName;
//        if (st != null) {
//            for (String oneFile:fileNames) {
//                if (oneFile.indexOf(".csv") > 0) {
//                    itemName = oneFile.substring(0, oneFile.length() - 4);
//                    item = getObjectFromTextFile(st, "planetData\\" + oneFile, itemName, colors[nowColor] );
//                    if (item == null)  {
//                        showError("Unable to create object " + itemName);
//                    }
//                    else {
//                        space.addItem(item);
//                        nowColor++;
//                        // recycle colors
//                        if (nowColor >= nColors)
//                            nowColor = 0;
//                    }
//                }
//            }
//            closeDBconnection();
//            bRetVal = true;
//        }
//        if (createInputSummary)
//            closeInputFiles();
//        return bRetVal;
//    }

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
