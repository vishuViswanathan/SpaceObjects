package Applications;

import SpaceElements.Constants;
import SpaceElements.Display.OrbitDisplay;
import SpaceElements.SpaceObject;
import SpaceElements.TheSpace;
import SpaceElements.time.DateAndJDN;
import display.InputControl;
import display.NumberTextField;
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
import java.util.*;
import java.util.Date;

import mvUtils.StringOps;

/**
 * Created by IntelliJ IDEA.
 * User: M Viswanathan
 * Date: 11/28/13
 * Time: 5:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectsInOrbit extends JApplet implements InputControl {
    TheSpace space;
    boolean asApp = false;
    JFrame mainF;
    DateAndJDN dateAndJDN = new DateAndJDN();
    JButton start = new JButton("Start");
//    JButton fromXL = new JButton("Load from XL");
//    JButton showOrbitMap = new JButton(("Orbit Map"));
    NumberTextField ntfDuration;  // , ntfStep;
    double duration = 200000; // in h
//    double stepH = 0.000278;
    public  ObjectsInOrbit() {

    }

    public  ObjectsInOrbit(boolean asApp) {
        this();
        this.asApp = asApp;
        init();
    }
    boolean createInputSummary = true;

    public void init() {
        modifyJTextEdit();
        space = new TheSpace();
        if (false && !getFromXL()) {
            SpaceObject sun =  new SpaceObject("Sun", 1.9891E+30, 1392000000, Color.yellow);
            space.addObject(sun);
            sun.initPosEtc(new Point3d(0, 0, 0), new Vector3d(0, -0.09055, 0));
            SpaceObject earth =  new SpaceObject("Earth", 5.9742E+24, 12756000, Color.blue);
            earth.setPrimaryObject(sun);
            earth.initPosEtc(new Point3d(149597870.0*1000-4670000, 0, 0), new Vector3d(0, 29785.25423 - 12.12978, 0));
            space.addObject(earth);
            SpaceObject moon = new SpaceObject("Moon", 7.3477E+22, 868600, Color.red);
            moon.setPrimaryObject(earth);
            moon.initPosEtc(new Point3d(1.499977599e11, 0, 0), new Vector3d(0, 29785.25423 + 986.2375, 0));
            space.addObject(moon);
        }
        getFromTextFiles();
        mainF = new JFrame("Objects in Orbit") ;
        mainF.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent we) {
              mainF.dispose();
              System.exit(0);
          }
        });
        JPanel dateP = new JPanel(new BorderLayout());
        dateP.add(dateAndJDN.panWithJDN(), BorderLayout.WEST);
        dateP.add(dateAndJDN.panWithDateTime(), BorderLayout.EAST);
        mainF.add(dateP, BorderLayout.NORTH);
        mainF.add(inputPanel(), BorderLayout.CENTER);
        mainF.add(buttonPanel(), BorderLayout.SOUTH);
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

     boolean getFromTextFiles() {
        Color[] colors = {Color.blue, Color.cyan, Color.white, Color.gray, Color.green, Color.BLUE,
                Color.CYAN, Color.GRAY, Color.GREEN, Color.yellow, Color.darkGray};
        int nowColor = 0;
        int nColors = colors.length;
        boolean bRetVal = false;
        File folder = new File("planetData");
        String[] fileNames = folder.list();
//        String[] fileNames = {"sun.csv", "mercury.csv", "earth.csv", "saturn.csv"};
        SpaceObject obj;
        Statement st = createDBConnection("jdbc:odbc:ODBCtoPlanetData");
        String objName;
        if (st != null) {
            for (String oneFile:fileNames) {
                if (oneFile.indexOf(".csv") > 0) {
                    objName = oneFile.substring(0, oneFile.length() - 4);
                    obj = getObjectFromTextFile(st, "planetData\\" + oneFile, objName, colors[nowColor] );
                    if (obj == null)  {
                        showError("Unable to create object " + objName);
                    }
                    else {
                        space.addObject(obj);
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
        start.addActionListener(listener);
//        fromXL.addActionListener(listener);
        JPanel durPanel = new JPanel(new BorderLayout());
        durPanel.add(new JLabel("Duration in h"), BorderLayout.WEST);
        durPanel.add(ntfDuration, BorderLayout.EAST);
        JPanel stepPanel = new JPanel(new BorderLayout());
        stepPanel.add(new JLabel("Calculation step in h"), BorderLayout.WEST);
        jp.add(durPanel);
        jp.add(stepPanel);
        jp.add(start);
//        jp.add(fromXL);
        return jp;
    }

    JComponent inputPanel() {
        JScrollPane sP = new JScrollPane();
        sP.setPreferredSize(new Dimension(500, 600));
        JPanel inputPart = new JPanel(new GridBagLayout()) ;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        for (int o = 0; o < space.nObjects(); o++) {
            JPanel p = space.dataPanel(o);
            inputPart.add(p, gbc);
            gbc.gridy++;
        }
        sP.setViewportView(inputPart);
        return sP;
    }

    Thread calTh;

    void startRunThread() {
        space.noteInput();
        showOrbitMap();
        calTh = new Thread(new CalculThread(true));
        calTh.start();
    }

    public void oneMoreTime() {
        calTh = new Thread(new CalculThread(false));
        calTh.start();
    }

    boolean continueIt = true;
    boolean runIt = true;

    public void continueOrbit(boolean bContinue) {
        continueIt = bContinue;
    }

    public void stopIt() {
        runIt = false;
    }

    double refreshInterval = 60;

    public void setRefreshInterval(double interval) {
        refreshInterval = interval;
        nextRefresh = nowT + refreshInterval;
        for (int o = 0; o < space.nObjects(); o++)
            space.getOneObject(o).setRefreshInterval(interval, nextRefresh);
    }

    DateAndJDN nowDate; // = new DateAndJDN(dateAndJDN);
    boolean showNow = false;
//    Timer timer;
    double nowT = 0;
    double nextRefresh = 0;
    long lastTms;
    long nowTms;

    void doCalculation(boolean fresh)   {
        double step = 2;
        double hrsPerSec = 0;
        continueIt = true;
        double endT;
        if (fresh) {
            nowT = 0;
            nextRefresh = 0;
            endT = ntfDuration.getData() * 3600;
//          stepH = ntfStep.getData();
            lastTms = (new Date()).getTime();
            nowDate = new DateAndJDN(dateAndJDN);
//            orbitDisplay.updateDisplay(nowDate, hrsPerSec); //.format(nowDate.getTime()));
            continueIt = true;
        }
        else {
            endT = nowT + ntfDuration.getData() * 3600;
        }
        orbitDisplay.updateDisplay(nowDate, hrsPerSec); //.format(nowDate.getTime()));

        runIt = true;
//        timer = new Timer();
//        DisplayTask display = new DisplayTask();
//        timer.schedule(display, 1000, 1000);
        while (runIt && nowT < endT) {
            if (continueIt) {
                doOneStep(step, nowT);
                nowT += step;
                nowDate.add(Calendar.SECOND, (int)step);
                if (nowT > nextRefresh) {
//                    while (!showNow) {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//                    }
                    showNow = false;
                    nowTms = (new Date()).getTime();
                    double deltaT = (double)(nowTms - lastTms);
                    hrsPerSec = (refreshInterval / 3600) / (deltaT / 1000);
                    orbitDisplay.updateDisplay(nowDate, hrsPerSec);
                    nextRefresh += refreshInterval;
                    lastTms = nowTms;
                }
            }
            Thread.yield();
        }
        orbitDisplay.updateDisplay(nowDate, hrsPerSec);
        orbitDisplay.resultsReady();
//        space.showStatus();
//        mainF.pack();
    }

    void doOneStep(double deltaT, double nowT)  {
        space.doCalculation(deltaT, nowT);
    }


    OrbitDisplay orbitDisplay;

    void showOrbitMap() {
        orbitDisplay = new OrbitDisplay(space, duration, this);
        orbitDisplay.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosed(e);
                start.setEnabled(true);
            }
        });
        orbitDisplay.setVisible(true);
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
                    space.addObject(getObjectFromXL(sh, gmRow++, posRow++, colors[pl]));
                SpaceObject tempEarth = getObjectFromXL(sh, gmRow, posRow, colors[2]);
                                        // gm ok for earth but the pos and velocity data are for EM-bary
                gmRow = 9;
                posRow = 23;
                for (int pl = 3; pl < 10; pl++ ) {// Mars, Jupiter, Saturn, Uranus, Neptune, Pluto, sun{
                    space.addObject(getObjectFromXL(sh, gmRow++, posRow++, colors[pl]));
                }
                SpaceObject tempMoon = getObjectFromXL(sh, gmRow, posRow, colors[9]); //yellow for moon
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
                space.addObject(tempEarth);
                tempMoon.initPosEtc(mWRTsb, vmWRTsb);
                space.addObject(tempMoon);

                sh = wB.getSheet("ESatelites");
                if (sh != null) {
                    double nSats = sh.getRow(0).getCell(1).getNumericCellValue();
                    gmRow = 6;
                    posRow = 20;
                    Vector3d vsatWRTsb;
                    Point3d satWRTsb;
                    for (double s = 0; s < nSats; s++) {
                        SpaceObject tempSat = getObjectFromXL(sh, gmRow++, posRow++, Color.RED);
                        satWRTsb = new Point3d(tempSat.getStatus().pos);
                        satWRTsb.add(eWRTsb);
                        vsatWRTsb = new Vector3d(tempSat.getStatus().velocity);
                        vsatWRTsb.add(veWRTsb);
                        tempSat.initPosEtc(satWRTsb, vsatWRTsb);
                        space.addObject(tempSat);
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


    SpaceObject baseObjectFromDBdata(Statement st, String objName, Color color) {
        SpaceObject obj = null;
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
            boolean isLightSrc = false;
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
                    mass = 0;
                    try {
                        gm = res.getDouble("GM");
                        if (gm > 0) {
                            mass = gm / Constants.G;
                            debug("got from GM for " + objName);
                        }
                        else
                            mass = res.getDouble("Mass");
                    } catch (SQLException e) {
                        mass = res.getDouble("Mass");
                    }
                    if (radius > 0 && mass > 0) {
                        obj = new SpaceObject(objName, mass, radius * 2 * 1000, color);
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
                        obj = new SpaceObject(objName, 1, 1 * 2, color);
                    }

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    SpaceObject getObjectFromTextFile(Statement st, String filePath, String objName, Color color) {
        SpaceObject obj = null;
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
            int nObj = space.nObjects();
            SpaceObject obj;
            try {
                String header = "JDN " + nowDate.getJdN() + "\n" +
                        "Position Vector in AU - x, y, z\n" +
                        "Velocity Vector in AU/day - Vx, vY, vZ\n\n";
                oStream.write(header.getBytes());
                for (int o = 0; o < nObj; o++) {
                    oStream.write(space.getOneObject(o).statusStringForCSV(posFactor, velFactor).toString().getBytes());
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

    double getNumberAfter(String datStr, String dataName1, String dataName2) {
        double val = getNumberAfter(datStr, dataName1);
        if (Double.isNaN(val))
            val = getNumberAfter(datStr, dataName2);
        return val;
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

    SpaceObject getObjectFromXL(Sheet sheet, int gmRow, int posRow,Color color ) {
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

        SpaceObject spObj = new SpaceObject(name, mass, dia, color);
        spObj.setImage(imageName);
        spObj.initPosEtc(new Point3d(x, y, z), new Vector3d(vx, vy, vz));
        spObj.setSpin(new AxisAngle4d(0, 1, 0, Math.PI / 180 * axisTilt ), spinPeriod * 3600);
        spObj.enableLightSrc(isLightSrc);
        return spObj;
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
        return mainF;  //To change body of implemented methods use File | Settings | File Templates.
    }

    void showError(String msg) {
        JOptionPane.showMessageDialog(parent(), msg, "ERROR", JOptionPane.ERROR_MESSAGE);
//        parent().toFront();
    }

    void showMessage(String msg) {
        JOptionPane.showMessageDialog(parent(), msg, "FOR INFORMATION", JOptionPane.INFORMATION_MESSAGE);
//        parent().toFront();
    }


    class MyListener implements ActionListener {
        MyListener() {

        }
        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            aBlock: {
                if (src == start) {
                    duration = ntfDuration.getData();
                    if (duration > 0)
                        startRunThread();
                    start.setEnabled(false);
                    break aBlock;
                }
//                if (src == showOrbitMap)  {
//                    showOrbitMap();
//                    break aBlock;
//                }
//                if (src == fromXL)  {
//                    getFromXL();
//                    break aBlock;
//                }
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
        System.out.println("ObjectsInOrbit: " + msg);
    }

    class CalculThread implements Runnable {
        boolean fresh = true;
        CalculThread(boolean fresh) {
            super();
            this.fresh = fresh;
        }
        public void run() {
            doCalculation(fresh);
        }
    }

    public static void main(String[] args) {
       final ObjectsInOrbit orbit = new ObjectsInOrbit(true);
         orbit.setVisible(true);
     }

}
