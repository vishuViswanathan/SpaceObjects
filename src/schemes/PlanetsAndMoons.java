package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import SpaceElements.Constants;
import SpaceElements.time.DateAndJDN;
import mvUtils.mvXML.XMLmv;
import mvUtils.strings.StringOps;

import javax.swing.*;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.io.*;
import java.sql.*;

/**
 * Created by M Viswanathan on 29 Aug 2014
 */
public class PlanetsAndMoons implements DefaultScheme {
    JFrame mainF;
    String subDir = "/20141011";

    public PlanetsAndMoons() {

    }

    @Override
    public boolean getScheme(JFrame parent, ItemSpace space) {
        this.mainF = parent;
        Color[] colors = {Color.blue, Color.cyan, Color.white, Color.gray, Color.green, Color.BLUE,
                Color.CYAN, Color.GRAY, Color.GREEN, Color.yellow, Color.darkGray};
        int nowColor = 0;
        int nColors = colors.length;
        boolean bRetVal = false;
        String folderName = "planetData" + subDir;
        File folder = new File(folderName);
        String[] fileNames = folder.list();
//        String[] fileNames = {"sun.csv", "mercury.csv", "earth.csv", "saturn.csv"};
        Item item;
        Statement st = createDBConnection("jdbc:odbc:ODBCtoPlanetData");
// Trial due to no jdbcodbc in Java 8       Statement st = createDBConnection("jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};" +
//                        "DBQ=J:\\SpaceObjects\\planetData\\20141011\\PlanetAndMoonData1.mdb;DriverID=22;READONLY=true");
        String itemName;
        StringBuilder xmlStr = new StringBuilder("#Base Data of Planets and Moons\n\n");
        int itemCount = 0;
        if (st != null) {
            for (String oneFile:fileNames) {
                if (oneFile.indexOf(".csv") > 0) {
                    itemName = oneFile.substring(0, oneFile.length() - 4);
                    StringBuilder oneObjInXml = new StringBuilder();
                    item = getObjectFromTextFile(st, folderName + "\\" + oneFile, itemName, colors[nowColor], oneObjInXml);
                    if (item == null)  {
                        showError("Unable to create object " + itemName);
                    }
                    else {
                        space.addItem(item);
                        nowColor++;
                        // recycle colors
                        if (nowColor >= nColors)
                            nowColor = 0;
                        xmlStr.append(XMLmv.putTag("item" + ("" + itemCount++).trim(), oneObjInXml));
                    }
                }
            }
            closeDBconnection();
            saveBasePlaneData(xmlStr);
            bRetVal = true;
        }
        if (createInputSummary)
            closeInputFiles();
        return bRetVal;
    }

    boolean saveBasePlaneData(StringBuilder xmlStr) { // TODO Not Ready yet
        return false;
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
            double gm;
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
                    xmlStr.append(XMLmv.putTag("name", objName));
                    xmlStr.append(XMLmv.putTag("radius", radius));
                    xmlStr.append(XMLmv.putTag("mass", mass));
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
