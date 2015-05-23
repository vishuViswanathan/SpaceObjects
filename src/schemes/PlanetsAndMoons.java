package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import SpaceElements.Constants;
import SpaceElements.time.DateAndJDN;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;
import mvUtils.strings.StringOps;

import javax.swing.*;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.Hashtable;

/**
 * Created by M Viswanathan on 29 Aug 2014
 */
public class PlanetsAndMoons implements DefaultScheme {
    JFrame mainF;
    String planetDataDir = "planetData";
    String subDir = "/20141011";

    public PlanetsAndMoons() {

    }

//    @Override
//    public boolean getScheme(JFrame parent, ItemSpace space) {
//        this.mainF = parent;
//        Color[] colors = {Color.blue, Color.cyan, Color.white, Color.gray, Color.green, Color.BLUE,
//                Color.CYAN, Color.GRAY, Color.GREEN, Color.yellow, Color.darkGray};
//        int nowColor = 0;
//        int nColors = colors.length;
//        boolean bRetVal = false;
//        String folderName = planetDataDir + subDir;
//        File folder = new File(folderName);
//        String[] fileNames = folder.list();
////        String[] fileNames = {"sun.csv", "mercury.csv", "earth.csv", "saturn.csv"};
//        Item item;
//        Statement st = createDBConnection("jdbc:odbc:ODBCtoPlanetData");
////  Trial due to no jdbcodbc in Java 8       Statement st = createDBConnection("jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};" +
////                        "DBQ=J:\\SpaceObjects\\planetData\\20141011\\PlanetAndMoonData1.mdb;DriverID=22;READONLY=true");
//        String itemName;
//        StringBuilder xmlStr = new StringBuilder();
//        int itemCount = 0;
//        if (st != null) {
//            for (String oneFile:fileNames) {
//                if (oneFile.indexOf(".csv") > 0) {
//                    itemName = oneFile.substring(0, oneFile.length() - 4);
//                    StringBuilder oneObjInXml = new StringBuilder();
//                    item = getObjectFromTextFile(st, folderName + "\\" + oneFile, itemName, colors[nowColor], oneObjInXml);
//                    if (item == null)  {
//                        showError("Unable to create object " + itemName);
//                    }
//                    else {
//                        space.addItem(item);
//                        nowColor++;
//                        // recycle colors
//                        if (nowColor >= nColors)
//                            nowColor = 0;
//                        xmlStr.append(XMLmv.putTag("item" + ("" + itemCount++).trim(), oneObjInXml));
//                    }
//                }
//            }
//            closeDBconnection();
//            xmlStr.insert(0, "#Base Data of Planets and Moons\n\n" + XMLmv.putTag("nItems", itemCount));
//            saveBasePlaneData(xmlStr);
//            bRetVal = true;
//        }
//        if (createInputSummary)
//            closeInputFiles();
//        return bRetVal;
//    }

    @Override
    public boolean getScheme(JFrame parent, ItemSpace space) {
        this.mainF = parent;
        Color[] colors = {Color.blue, Color.cyan, Color.white, Color.gray, Color.green, Color.BLUE,
                Color.CYAN, Color.GRAY, Color.GREEN, Color.yellow, Color.darkGray};
        int nowColor = 0;
        int nColors = colors.length;
        boolean bRetVal = false;
        String folderName = planetDataDir + subDir;
        File folder = new File(folderName);
        String[] fileNames = folder.list();
        if (planetsBaseDataFromFile()) {
            Item item;
            String itemName;
            StringBuilder xmlStr = new StringBuilder();
            int itemCount = 0;
                for (String oneFile : fileNames) {
                    if (oneFile.indexOf(".csv") > 0) {
                        itemName = oneFile.substring(0, oneFile.length() - 4);
                        StringBuilder oneObjInXml = new StringBuilder();
                        item = getObjectFromTextFile(folderName + "\\" + oneFile, itemName, colors[nowColor]);
                        if (item == null) {
                            showError("Unable to create object " + itemName);
                        } else {
                            space.addItem(item);
                            nowColor++;
                            // recycle colors
                            if (nowColor >= nColors)
                                nowColor = 0;
                            xmlStr.append(XMLmv.putTag("item" + ("" + itemCount++).trim(), oneObjInXml));
                        }
                    }
                }
                bRetVal = true;
            }
            if (createInputSummary)
                closeInputFiles();
        return bRetVal;
    }
    String baseDataFilePath = planetDataDir + "\\planetsBaseData.data";

    boolean saveBasePlaneData(StringBuilder xmlStr) { // TODO Not Ready yet
        boolean retVal = false;
        String filePath = baseDataFilePath;
        try {
            FileOutputStream stream = new FileOutputStream(filePath);
            BufferedOutputStream oStream = new BufferedOutputStream(stream);
            try {
                oStream.write(xmlStr.toString().getBytes());
                oStream.close();
                retVal = true;
            } catch (IOException e) {
                showError("Some problem wring Planets base data to file " + filePath + "\n" + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            showError("Some problem saving Planets base data to file " + filePath + "\n" + e.getMessage());
        }
        return retVal;
    }

    public ItemMovementsApp.SpaceSize getSpaceSize() {
        return ItemMovementsApp.SpaceSize.ASTRONOMICAL;
    }

    double baseJDN = -1;

    @Override
    public double startJDN() {
        return baseJDN;
    }

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

    Statement createDBConnectionNEW(String connectTo) { // Trial due to no jdbcodbc in Java 8
        Statement st = null;
        try {
            dbConnection = DriverManager.getConnection(connectTo, "", "");
            st = dbConnection.createStatement();
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

    Hashtable<String, String> planetsAndMoonsXML;

    boolean planetsBaseDataFromFile() {
        boolean bRetVal = false;
        String filePath = baseDataFilePath;
            if (!filePath.equals("nullnull")) {
//                debug("Data file name :" + filePath);
                try {
                    BufferedInputStream iStream = new BufferedInputStream(new FileInputStream(filePath));
                    //           FileInputStream iStream = new FileInputStream(fileName);
                    File f = new File(filePath);
                    long len = f.length();
                    if (len > 100 && len < 10000) {
                        int iLen = (int) len;
                        byte[] data = new byte[iLen];
                        iStream.read(data);
                        String xmlStr = new String(data);
                        ValAndPos vp;
                        int nItems;
                        vp = XMLmv.getTag(xmlStr, "nItems");
                        nItems = Integer.valueOf(vp.val);
                        planetsAndMoonsXML = new Hashtable<String, String>();
                        String oneXML;
                        ValAndPos vpOne;
                        for (int i = 0; i < nItems; i++) {
                            vp = XMLmv.getTag(xmlStr, "item" + ("" + i).trim(), vp.endPos);
                            oneXML = vp.val;
                            vpOne = XMLmv.getTag(oneXML, "name");
                            StringBuilder hashKey = new StringBuilder();
                            String[] nameSplit = vpOne.val.split("-");
                            if (nameSplit.length > 0) {
                                for (String s : nameSplit)
                                    hashKey.append(s.trim().toLowerCase());
                                planetsAndMoonsXML.put(hashKey.toString(), oneXML);
                            }
                        }
                        bRetVal = true;
                    } else
                        showError("File size " + len + " for " + filePath);
                } catch (Exception e) {
                    showError("Some Problem in getting file!");
                }
        }
        return bRetVal;
    }

    Item getPlanetOrMoon(String objName, Color color) {
        Item item = null;
        StringBuilder hashKey = new StringBuilder();
        String[] nameSplit = objName.split("-");
        if (nameSplit.length > 0) {
            for (String s:nameSplit)
                hashKey.append(s.trim().toLowerCase());
            String xmlStr = planetsAndMoonsXML.get(hashKey.toString());
            if (xmlStr!= null) {
                ValAndPos vp;
                String name = nameSplit[0].trim();
                String moonOf = "";
                if (nameSplit.length > 1) { // it is a moon of
                    moonOf = nameSplit[1].trim();
                }
                double radius;
                double mass;
                double gm;
                double rotationPeriod = 0;
                double axisInclination = 0;
                boolean isLightSrc = false;
                String imageName;
                try {
                    vp = XMLmv.getTag(xmlStr, "radius", 0);
                    radius = Double.valueOf(vp.val);
                    vp = XMLmv.getTag(xmlStr, "GM", 0);
                    gm = Double.valueOf(vp.val);
                    if (gm > 0) {
                        mass = gm / Constants.G;
//                        debug("got from GM for " + objName);
                    }
                    else {
                        vp = XMLmv.getTag(xmlStr, "mass", 0);
                        mass = Double.valueOf(vp.val);
                    }
                    if (radius > 0 && mass > 0) {
                        item = new Item(objName, mass, radius * 2 * 1000, color, mainF);
                        vp = XMLmv.getTag(xmlStr, "imageName", 0);
                        imageName = vp.val;
                        if (imageName != null && imageName.length() > 3 && !imageName.equalsIgnoreCase("null")) {
                            item.setImage(imageName);
                            vp = XMLmv.getTag(xmlStr, "rotationPeriod", 0);
                            rotationPeriod = Double.valueOf(vp.val);
                            vp = XMLmv.getTag(xmlStr, "axisInclination", 0);
                            axisInclination = Double.valueOf(vp.val);
                            vp = XMLmv.getTag(xmlStr, "isLightSrc", 0);
                            isLightSrc = vp.val.equals("1");
                            item.setSpin(new AxisAngle4d(0, 1, 0, Math.PI / 180 * axisInclination ), rotationPeriod * 3600);
                            item.enableLightSrc(isLightSrc);
                        }
                    }
                    else {
                        showError("There is some problem is Mass or radius of " + objName + ", taking as 1kg and 1m");
                        item = new Item(objName, 1, 1 * 2, color, mainF);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return item;
    }

    Item baseObjectFromDBdata(Statement st, String objName, Color color, StringBuilder xmlStr) {
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
            double gm = 0;
            double rotationPeriod = 0;
            double axisInclination = 0;
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
                    imageName = "";
                    if (radius > 0 && mass > 0) {
                        obj = new Item(objName, mass, radius * 2 * 1000, color, mainF);
                        imageName = res.getString("imageName");
                        if (imageName != null && imageName.length() > 3 && !imageName.equalsIgnoreCase("null")) {
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
                    xmlStr.append(XMLmv.putTag("name", objName));
                    xmlStr.append(XMLmv.putTag("radius", radius));
                    xmlStr.append(XMLmv.putTag("mass", mass));
                    xmlStr.append(XMLmv.putTag("GM", gm));
                    xmlStr.append(XMLmv.putTag("imageName", imageName));
                    xmlStr.append(XMLmv.putTag("rotationPeriod", rotationPeriod));
                    xmlStr.append(XMLmv.putTag("axisInclination", axisInclination));
                    xmlStr.append(XMLmv.putTag("isLightSrc", isLightSrc));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    Item getObjectFromTextFile(String filePath, String objName, Color color) {
        Item obj;
        obj = getPlanetOrMoon(objName, color);
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

    Item getObjectFromTextFile(Statement st, String filePath, String objName, Color color, StringBuilder xmlStr) {
        Item obj;
        obj = baseObjectFromDBdata(st, objName, color, xmlStr);
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

    BufferedOutputStream[] inputSummaryFiles;
    String[] inputJDNs;
    boolean createInputSummary = false; // for noting down input status data in csxv file
    double dataJD = 0;
    double oneAuInM  = Constants.oneAuInkm * 1000;
    double secsPerDay = Constants.secsPerDay;
    DateAndJDN dateAndJDN = new DateAndJDN();

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

    public String toString() {
        return "Planets and Moons";
    }

    void showError(String msg){
        System.out.println("Error: " + msg);
    }

}
