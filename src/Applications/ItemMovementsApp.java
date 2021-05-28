package Applications;

import GeneralElements.DarkMatter;
import GeneralElements.Display.MotionDisplay;
import GeneralElements.ItemInterface;
import GeneralElements.ItemSpace;
import mvUtils.display.FramedPanel;
import mvUtils.time.DateAndJDN;
import evaluations.EvalOnce;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;
import org.apache.log4j.Logger;
import schemes.DefaultScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

/**
 * Created by M Viswanathan on 23 May 2014
 */
public class ItemMovementsApp extends Panel implements InputControl {
    public enum SpaceSize {
        BLANK("Blank Set as Daily Objects"),
        BLANKSPACE("Blank Set as Space Objects"),
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

    String[] schemesList = {"BallAndFloor",
                            "BoundedBalls",
                            "ContactSpread",
                            "ContactSpreadWithChoice",
                            "TwoTypesOfBallsInContainer",
                            "BungeeJumping",
                            "BungeeJumpingWithRope",
                            "ChainWithBall",
                            "Mesh",
                            "MultiPendulum",
                            "PlanetsAndMoons",
                            "VRMLSample"
    };

    ItemMovementsApp mainApp;
    boolean useAllCPUs = false;
    public SpaceSize spSize;
    JComboBox<SpaceSize> cbSpaceSize = new JComboBox(SpaceSize.values());
    ItemSpace space;
    boolean asApp = false;
    static JFrame mainF;
    DateAndJDN dateAndJDN = new DateAndJDN();
    JButton pbStart = new JButton("Start");
    NumberTextField ntfDuration;  // , ntfStep;
    String endDate = "YYYYmmdd HH:MM:SS";
    TextField tfEndTime;
    boolean bEndTime = false;
    JRadioButton rbEndTime;
    JRadioButton rbDuration;
    ButtonGroup bgroup;

    double duration = 2000; // in h
    public double calculationStep =2; //in seconds
    public int repeats = 1; // number of times each step is repeated for position-force accuracy
    static public boolean bShowPaths = false;
    static public boolean bShowLinks = false;
    static public boolean bShowRelOrbits = false;

    boolean xmlHistory = false;
    String historyFilePath = "results\\history.csv";
    double fileHistoryInterval = 3600; // in s
    boolean bHistoryToFileON = false;

    public static Logger log;

    public ItemMovementsApp() {
        mainApp = this;
    }

    public ItemMovementsApp(boolean asApp) {
        this();
        this.asApp = asApp;
        init();
    }

    public void init() {
        if (log == null)
            log = Logger.getLogger(ItemMovementsApp.class);
        modifyJTextEdit();
        space = new ItemSpace(this);
        mainF = new JFrame("Items in Motion");
        mainF.setJMenuBar(getMenuBar());
        mainF.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                mainF.dispose();
                System.exit(0);
            }
        });
        mainF.add(cbSpaceSize);
        duration = 200;
        calculationStep = 0.0002; // was 0.000002;
        refreshInterval = 200 * calculationStep; // was 20000
        proceedToItemList(false);
        space.enableItemGravity(false);
        space.enableItemCollision(false);
        space.enableLight(false);
        bShowPaths = false;
        bShowLinks = true;
        bRealTime = true;
        setSpaceSize(SpaceSize.DAILY);
        mainF.pack();
        mainF.setVisible(true);

    }

//    public boolean useAllCPUs() {
//        return useAllCPUs;
//    }

    void setTimingValues(double calculationStep, double refreshInterval, double duration, boolean bEnableItemGravity,
                         boolean bShowLinks, boolean bShowOrbit, boolean bRealTime)  {
        this.calculationStep = calculationStep;
        this.refreshInterval = refreshInterval;
        this.duration = duration;
        ntfDuration.setData(duration);
        tfEndTime.setText(endDate);
//        bShowItems = true;
        this.bShowLinks = bShowLinks;
        this.bShowPaths = bShowOrbit;
        this.bRealTime = bRealTime;
        space.enableItemGravity(bEnableItemGravity);
    }

    class TimingValuesDlg extends JDialog {
        NumberTextField ntCalculStep;
        NumberTextField ntUpdate; // multiplier on calculation step;
        NumberTextField ntRepeats;
        JCheckBox chBTimeDilation;
        JCheckBox chBGravityPropagationTime;
        JCheckBox cbHistoryToFile;
        NumberTextField ntFileHistoryInterval;
        JButton jbHistoryFilePath;
        JTextField tfHistoryFilePath;

        JButton jbOk = new JButton("Ok");
        JButton jbCancel = new JButton("Cancel");
        double upDateMultiplier;
        TimingValuesDlg() {
            setModal(true);
            jbOk.addActionListener(e -> {
                if (noteValues())
                    setVisible(false);
            });
            jbCancel.addActionListener(e -> setVisible(false));
            upDateMultiplier = refreshInterval / calculationStep;
            ntCalculStep = new NumberTextField(mainApp, calculationStep, 6, false, 0.0000001, 10000, "0.000E00",
                    "Calculation step in seconds");
            ntUpdate = new NumberTextField(mainApp, upDateMultiplier, 6, false, 1, 10000, "#,###",
                    "Update once in this many steps");
            ntRepeats = new NumberTextField(mainApp, repeats, 6, false, 0, 10, "#0",
                    "Step repeats for Position/Force accuracy");
            ntRepeats.setEditable(false);
            chBTimeDilation = new JCheckBox("Consider Time Dilation due to Gravity", space.bConsiderTimeDilation);
            chBTimeDilation.setEnabled(false);
            chBGravityPropagationTime = new JCheckBox("Consider Gravity propagation time", space.bConsiderGravityVelocity);
            chBGravityPropagationTime.setEnabled(false);
            tfHistoryFilePath = new JTextField(20);
            tfHistoryFilePath.setText(historyFilePath);
            tfHistoryFilePath.setToolTipText(historyFilePath);
            jbHistoryFilePath = new JButton("Set File Path");
            jbHistoryFilePath.addActionListener(e-> {
                String path = getHistoryFilePath();
                if (path.length() > 0) {
                    historyFilePath = path;
                    tfHistoryFilePath.setText(path);
                    tfHistoryFilePath.setToolTipText(historyFilePath);
                }

            });
            ntFileHistoryInterval = new NumberTextField(mainApp, fileHistoryInterval, 6, false, 0,
                    3600*24, "#,##0.000", "History step interval (s)");
            cbHistoryToFile = new JCheckBox("History To File", bHistoryToFileON);
            cbHistoryToFile.addActionListener(e-> {bHistoryToFileON = cbHistoryToFile.isSelected();
                    setHistoryFields();
            });
            setHistoryFields();
            MultiPairColPanel jp = new MultiPairColPanel("Calculation Timings");
            jp.addItemPair(ntCalculStep);
            jp.addItemPair(ntUpdate);
            jp.addBlank();
            jp.addItemPair(ntRepeats);
            jp.addBlank();
            jp.addItem(cbHistoryToFile);
            jp.addItemPair(ntFileHistoryInterval);
            jp.addItemPair(jbHistoryFilePath, tfHistoryFilePath);
            jp.addItem(chBTimeDilation);
            jp.addItem(chBGravityPropagationTime);
            jp.addBlank();
            jp.addItemPair(jbCancel, jbOk);
            add(jp);
            pack();
        }

        boolean noteValues() {
            boolean retVal = false;
            if (dataOK()) {
                calculationStep = ntCalculStep.getData();
                refreshInterval = calculationStep * ntUpdate.getData();
                repeats = (int)ntRepeats.getData();
                space.bConsiderTimeDilation = chBTimeDilation.isSelected();
                space.bConsiderGravityVelocity = chBGravityPropagationTime.isSelected();
                retVal = true;
            }
            fileHistoryInterval = ntFileHistoryInterval.getData();
            return retVal;
        }

        void setHistoryFields() {
            ntFileHistoryInterval.setEnabled(bHistoryToFileON);
            jbHistoryFilePath.setEnabled(bHistoryToFileON);
        }

        boolean dataOK() {
            return ntCalculStep.dataOK() && ntUpdate.dataOK() &&ntRepeats.dataOK();
        }
    }

    void getTimingValues() {
        TimingValuesDlg dlg = new TimingValuesDlg();
        dlg.setLocationRelativeTo(mainF);
        dlg.setVisible(true);
    }

    JMenuItem mITuning = new JMenuItem("Tune Calculations step");
    JMenuItem mIReadData = new JMenuItem("Read Data From File");
    JMenuItem mISaveData =new JMenuItem("Save Data to File");
    JMenuItem mIClearData = new JMenuItem("Start with Clean Slate");
    JMenuItem mIExit = new JMenuItem("Exit");

    JRadioButtonMenuItem rIDaily = new JRadioButtonMenuItem("Daily objects (m)");
    JRadioButtonMenuItem rIEarth = new JRadioButtonMenuItem("Earth and Environment (m)");
    JRadioButtonMenuItem rIAstronomical = new JRadioButtonMenuItem("Astronomical (km)");
    JRadioButtonMenuItem rIMolecular = new JRadioButtonMenuItem("Molecular (micrometer)");
    JComboBox cbSchemes;
//    JCheckBoxMenuItem ckAllCPU;

    JMenu mScheme;
    JMenuItem mIScheme;

    JMenuBar getMenuBar() {
        MenuListener ml = new MenuListener();
        mIExit.addActionListener(ml);
        mIReadData.addActionListener(ml);
        mISaveData.addActionListener(ml);
        mIClearData.addActionListener(ml);
        mIExit.addActionListener(ml);
        mITuning.addActionListener(ml);
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(mITuning);
        fileMenu.addSeparator();
        fileMenu.add(mIReadData);
        fileMenu.add(mISaveData);
        fileMenu.addSeparator();
        fileMenu.add(mIClearData);
        fileMenu.addSeparator();
        fileMenu.add(mIExit);
        menuBar.add(fileMenu);
        JMenu mSpaceSize = new JMenu("Space Size");
        ButtonGroup rbGroup = new ButtonGroup();
        rbGroup.add(rIAstronomical);
        rIAstronomical.addActionListener(ml);
        rIEarth.addActionListener(ml);
        rIDaily.addActionListener(ml);
        rIMolecular.addActionListener(ml);
        rbGroup.add(rIEarth);
        rbGroup.add(rIDaily);
        rbGroup.add(rIMolecular);
        rIEarth.setEnabled(false);
        rIMolecular.setEnabled(false);
        mSpaceSize.add(rIAstronomical);
        mSpaceSize.add(rIEarth);
        mSpaceSize.add(rIDaily);
        mSpaceSize.add(rIMolecular);
//        ckAllCPU = new JCheckBoxMenuItem("Use Multi CPU", useAllCPUs);
//        ckAllCPU.addActionListener(ml);
//        mSpaceSize.addSeparator();
//        mSpaceSize.add(ckAllCPU);
        menuBar.add(mSpaceSize);
        loadDefaultSchemes();
        mScheme = new JMenu("Select Scheme");
        mIScheme = new JMenuItem("Select");
        mIScheme.addActionListener(ml);
        mScheme.add(mIScheme);
        menuBar.add(mScheme);
        return menuBar;
    }

    Vector<DefaultScheme> defaultSchemes;

    void loadDefaultSchemes() {
        defaultSchemes = new Vector<>();
        try {
            for (String schemeName: schemesList)
//                defaultSchemes.add((DefaultScheme)Class.forName("schemes." + schemeName).newInstance());
                defaultSchemes.add((DefaultScheme)Class.forName("schemes." + schemeName).
                        getDeclaredConstructor().newInstance());
            cbSchemes = new JComboBox(defaultSchemes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void selectScheme() {
        SchemeSelector selector = new SchemeSelector();
        selector.setLocationRelativeTo(mainF);
        selector.setVisible(true);
        int selIndex = cbSchemes.getSelectedIndex();
        space.clearSpace();
        DefaultScheme scheme = defaultSchemes.get(selIndex);
        scheme.getScheme(mainF, space);
        setSpaceSize(scheme.getSpaceSize());
        setCalculationDate(scheme.startJDN());
        proceedToItemList(true);
    }

    class SchemeSelector extends JDialog {
        JButton jbProceed = new JButton("Proceed");
        JButton jbCancel = new JButton("Cancel");
        SchemeSelector() {
            setModal(true);
            setTitle("Selecting from Available Schemes");
            jbProceed.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });
            jbCancel.addActionListener(e -> setVisible(false));
            MultiPairColPanel jp = new MultiPairColPanel(150, 150);
            jp.addItemPair("Select Scheme", cbSchemes);
            jp.addBlank();
            jp.addItemPair(jbCancel, jbProceed);
            add(jp);
            pack();
        }
    }

    JComponent jcItemList;
    JComponent jcInfluenceList;
    JComponent jcAstroDate;
    JComponent dataEntryPanel;

    JComponent jcJDN;
    JComponent jcDateTime;

    void proceedToItemList(boolean reDo) {
        // reDo can ber removed    REMOVE
        if (reDo) {
            if (dataEntryPanel != null) mainF.remove(dataEntryPanel);
            if (jcItemList != null) mainF.remove(jcItemList);
            if (jcInfluenceList != null) mainF.remove(jcInfluenceList);
            if (jcAstroDate != null) mainF.remove(jcAstroDate);
        }
        else
            mainF.remove(cbSpaceSize);
        if (spSize == SpaceSize.ASTRONOMICAL) {
            JPanel dateP = new JPanel(new BorderLayout());
            dateP.add((jcJDN = dateAndJDN.panWithJDN()), BorderLayout.WEST);
            dateP.add((jcDateTime= dateAndJDN.panWithDateTime()), BorderLayout.EAST);
            jcAstroDate = dateP;
            mainF.add(jcAstroDate, BorderLayout.NORTH);
        }
        dataEntryPanel = space.dataEntryPanel();
        mainF.add(dataEntryPanel, BorderLayout.CENTER);
        if (!reDo) {
            mainF.add(buttonPanel(), BorderLayout.SOUTH);
        }
        mainF.pack();
        mainF.setVisible(true);
    }

    void updateJCastroDate() {
        if (spSize == SpaceSize.ASTRONOMICAL && nowDate != null) {
            dateAndJDN = new DateAndJDN(nowDate.getJdN());
            if (jcAstroDate != null) {
                jcAstroDate.remove(jcJDN);
                jcAstroDate.remove(jcDateTime);
                jcAstroDate.add((jcJDN = dateAndJDN.panWithJDN()), BorderLayout.WEST);
                jcAstroDate.add((jcDateTime = dateAndJDN.panWithDateTime()), BorderLayout.EAST);
                jcAstroDate.updateUI();
            }

//                mainF.remove(jcAstroDate);
//            JPanel dateP = new JPanel(new BorderLayout());
//            dateP.add(dateAndJDN.panWithJDN(), BorderLayout.WEST);
//            dateP.add(dateAndJDN.panWithDateTime(), BorderLayout.EAST);
//            jcAstroDate = dateP;
//            mainF.add(jcAstroDate, BorderLayout.NORTH);
//            mainF.pack();
        }
    }

    JPanel buttonPanel() {
        rbDuration = new JRadioButton("Set Duration", !bEndTime);
        ntfDuration = new NumberTextField(this, duration, 8, false, 0.0001, 1e10, "#,###.######", "Duration");
        ntfDuration.setEditable(!bEndTime);

        rbEndTime = new JRadioButton("Set End Time(GMT)", bEndTime);
        rbEndTime.setEnabled(spSize == SpaceSize.ASTRONOMICAL);
        tfEndTime = new TextField(endDate);
        tfEndTime.setEnabled(bEndTime);

        bgroup = new ButtonGroup();
        bgroup.add(rbDuration);
        bgroup.add(rbEndTime);
        MyListener listener = new MyListener();
        pbStart.addActionListener(listener);
        rbDuration.addActionListener(listener);

        rbEndTime.addActionListener(listener);

        JPanel durPanel = new JPanel();
        durPanel.add(rbDuration);
        durPanel.add(ntfDuration);

        JPanel endDatePanel = new JPanel();
        endDatePanel.add(rbEndTime);
        endDatePanel.add(tfEndTime);
        endDatePanel.add(new JLabel("  enter all"));

        FramedPanel jp = new FramedPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.X_AXIS));
        jp.add(endDatePanel);
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
        return (DarkMatter)(space.getOneItem(i));
    }

    //    Thread calTh;
    SpaceEvaluator evaluator;

    boolean startRunThread() {
        boolean running = false;
        continueIt = false;
        if (space.noteItemData()) {
            if (showOrbitMap()) {
                space.setEnableLight();
                runIt = true;
                SpaceEvaluator.closePool();
                evaluator = SpaceEvaluator.getSpaceEvaluator(this, true);
                evaluator.start();
                running = true;
            }
        }
        return running;
    }

    public void oneMoreTime() {
        evaluator = SpaceEvaluator.getSpaceEvaluator(this, false); // new SpaceEvaluator(this, false);
        evaluator.start();
    }

    boolean continueIt = true;
    boolean runIt = true;
    boolean itsON = false;

    public void continueOrbit(boolean bContinue) {
        continueIt = bContinue;
    }

    public void stopIt() {
        runIt = false;
//        updateJCastroDate();
    }

    public double refreshInterval = 60; // sec

    public void setRefreshInterval(double interval) {
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
//    boolean updateDisplayNow = false;

    void doCalculationSERIAL(boolean fresh)   {
        enableButtons(false);
        double step = calculationStep;
        double hrsPerSec = 0;
        double nextHistorySave = 0;
        double endT;
        boolean wasInContinue = false;
//        debug("in doCalculationSERIAL");
        if (fresh) {
            space.initAllItemConnections();
            space.setGlobalLinksAndActions();
            space.setActiveActions();
            nowT = 0;
            setRefreshInterval(refreshInterval);
            nextRefresh = 0;
            endT = ntfDuration.getData() * 3600;
            lastTnano = System.nanoTime(); // new Date()).getTime();
            nowDate = new DateAndJDN(dateAndJDN);
        }
        else {
            endT = nowT + ntfDuration.getData() * 3600;
        }
        boolean bLive = false;
        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec, bLive); //.format(nowDate.getTime()));
        if (bHistoryToFileON) {
            openHistoryFile();
            updateHistoryFile(nowT);
            nextHistorySave += fileHistoryInterval;
        }
        runIt = true;
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        debug("ItemMovementsApp.#565:before runIt && nowT < endT and continueIt " + runIt + ", " + nowT + ", " + endT + ", " + continueIt);
        boolean firstTime = true;
        while (runIt && nowT < endT) {
            itsON = true;
            if (firstTime || !continueIt) {
                try { // why is this required ????
                    Thread.sleep(2);
//                    debug("nowT = " + nowT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                firstTime = false;
            }
            if (continueIt) {
                if (bHistoryToFileON && nowT >= nextHistorySave) {
                    updateHistoryFile(nowT);
                    nextHistorySave += fileHistoryInterval;
                }
                try {
                    if ((endT - nowT) < calculationStep)
                        step = (endT - nowT);
                    if (!doOneStep(step, nowT)) {
                        showError("Error in one step at " + nowT + "\nSuggest restart program");
                        break;
                    }
                    nowT += step;
                    if (nowT > nextRefresh) {
                        space.updateLinkDisplay();
                        showNow = false;
                        nowTnano = System.nanoTime(); //new Date().getTime();
                        double deltaT = ((double)(nowTnano - lastTnano))/ 1e9;
                        hrsPerSec = (refreshInterval / 3600) / deltaT;
                        if (bRealTime && deltaT <= refreshInterval) {
                            try {
                                Thread.sleep((long)((refreshInterval - deltaT) * 1000));
                                bLive = true;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            bLive = false;
                        nowDate = new DateAndJDN(dateAndJDN);
                        nowDate.add(Calendar.SECOND, (int)nowT);
                        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec, bLive);
                        lastRefresh = nowT;
                        nextRefresh += refreshInterval;
                        lastTnano = System.nanoTime(); //nowTnano;
                    }
                } catch (Exception e) {
                    showError("ItemMoementsApp.#613: Aborting in 'doCalculation' at nowT = " + nowT + " due to :" + e.getMessage());
                    runIt = false;
                }
                wasInContinue = true;
            }
            else {
                if (wasInContinue)
                    space.updateEndGraphic();
                wasInContinue = false;
            }
        }
        continueIt = false;

        space.updateEndGraphic();

        nowDate = new DateAndJDN(dateAndJDN);
        nowDate.add(Calendar.SECOND, (int)nowT);
        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec, bLive);
        if (bHistoryToFileON) {
            updateHistoryFile(nowT);
            closeHistoryFile();
        }

//        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec, bLive);
        orbitDisplay.resultsReady();
        enableButtons(true);
        itsON = false;
        updateJCastroDate();
    }

    boolean doOneStep(double deltaT, double nowT) throws Exception {
        return space.doCalculation(deltaT, nowT);
    }

//    void doCalculationPARELLEL(boolean fresh)   {
//        enableButtons(false);
//        double step = calculationStep;
//        double hrsPerSec = 0;
//        double endT;
//        if (fresh) {
//            space.initAllItemConnections();
//            space.setGlobalLinksAndActions();
//            nowT = 0;
//            setRefreshInterval(refreshInterval);
//            nextRefresh = 0;
//            endT = ntfDuration.getData() * 3600;
//            lastTnano = System.nanoTime(); // new Date()).getTime();
//            nowDate = new DateAndJDN(dateAndJDN);
//        }
//        else {
//            endT = nowT + ntfDuration.getData() * 3600;
//        }
//        boolean bLive = false;
//        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec, bLive); //.format(nowDate.getTime()));
//
//        runIt = true;
//        evaluator.startTasks();
//        while (runIt && nowT < endT) {
//            itsON = true;
//            if (continueIt) {
//                try {
//                    if (!doOneStepPARELLEL(step, nowT)) {
//                        showError("Error in doOneStepPARELLEL one step at " + nowT + "\nSuggest restart program");
//                        break;
//                    }
//                    step = calculationStep;
//                    nowT += step;
//                    if (nowT > nextRefresh) {
//                        space.updateLinkDisplay();
//                        showNow = false;
//                        nowTnano = System.nanoTime(); //new Date().getTime();
//                        double deltaT = ((double)(nowTnano - lastTnano))/ 1e9;
//                        hrsPerSec = (refreshInterval / 3600) / deltaT;
//                        if (bRealTime && deltaT <= refreshInterval) {
//                            try {
//                                Thread.sleep((long)((refreshInterval - deltaT) * 1000));
//                                bLive = true;
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        else
//                            bLive = false;
//                        nowDate.add(Calendar.SECOND, (int) (nowT - lastRefresh));
//                        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec, bLive);
//                        lastRefresh = nowT;
//                        nextRefresh += refreshInterval;
//                        lastTnano = System.nanoTime(); //nowTnano;
//                    }
//                } catch (Exception e) {
//                    showError("Aborting in 'doCalculation' at nowT = " + nowT + " due to :" + e.getMessage());
//                    runIt = false;
//                }
//            }
//        }
//        evaluator.stopTasks();
//        nowDate.add(Calendar.SECOND, (int) (nowT - lastRefresh));
//        orbitDisplay.updateDisplay(nowT, nowDate, hrsPerSec, bLive);
//        orbitDisplay.resultsReady();
//        enableButtons(true);
//        itsON = false;
//        updateJCastroDate();
//    }
//
//    boolean doOneStepPARELLEL(double deltaT, double nowT) throws Exception {
//        return space.doCalculation(evaluator, deltaT, nowT);
//    }

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
                    space.updateItemTable();
                }
            });
            orbitDisplay.setVisible(true);
            return true;
        } catch (Exception e) {
            showError("ItemMovementsApp.782: In showOrbitMap : " + e.getMessage());
//            e.printStackTrace();
            return false;
        }
    }

    public boolean writeCurrentVectorsToFile() {
        boolean bRetVal = false;
        String filePath = "results\\statusVectors.csv";
        double posFactor = 1.0;
        double velFactor = 1.0;
        try {
            FileOutputStream stream = new FileOutputStream(filePath);
            BufferedOutputStream oStream = new BufferedOutputStream(stream);
            int nObj = space.nItems();

            try {
                String header = "JDN " + nowDate.getJdN() + "\n" +
                        "Name, HorizonID, GM\n" +
                        "Position Vector in m, x, y, z\n" +
                        "Velocity Vector in m/s, Vx, vY, vZ\n";
                oStream.write(header.getBytes());
                for (int o = 0; o < nObj; o++) {
                    oStream.write(space.getOneItem(o).statusStringForCSV(posFactor, velFactor).toString().getBytes());
                }
                oStream.close();
                bRetVal = true;
            } catch (IOException e) {
                showError("Some problem writing Vectors to file " + filePath + "\n" + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            showError("Some problem saving Vectors to file " + filePath + "\n" + e.getMessage());
        }
        return bRetVal;
    }

    BufferedOutputStream historyFileStream;

    void openHistoryFile() {
        try {
            FileOutputStream stream = new FileOutputStream(historyFilePath);
            historyFileStream = new BufferedOutputStream(stream);
            try {
                if (xmlHistory) {
                    historyFileStream.write(("# History path: " + historyFilePath + "\n\n").getBytes());
                    historyFileStream.write("# 'at' has JDN, DateAndTime\n".getBytes());
                    historyFileStream.write("# 'obj' has name, HorizonID, gm, x, y, z, Vx, Vy, Vz, V, Ax, Ay, Az, A\n\n".getBytes());
                }
                else {
                    historyFileStream.write(("# History path: " + historyFilePath + "\n\n").getBytes());
                    historyFileStream.write("JDN,DateAndTime,NowT,ObjectName,HorizonID,mass,gm,dia,x,y,z,Vx,Vy,Vz,V,Ax,Ay,Az,A,Infected,Cured\n".getBytes());
                }

            } catch (IOException e) {
                showError("Some problem in Wring header to History File " + historyFilePath + "\n" + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            showError("Some problem in opening History File " + historyFilePath + "\n" + e.getMessage());
        }
    }

    void closeHistoryFile() {
        try {
            historyFileStream.close();
        } catch (IOException e) {
            showError("Some problem in CLOSING History File " + historyFilePath + "\n" + e.getMessage());
        }
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");

    public boolean updateHistoryFile(double nowT) {
        DateAndJDN jdn = new DateAndJDN(dateAndJDN);
        jdn.add(Calendar.SECOND, (int)nowT);

        boolean bRetVal = false;
        double posFactor = 1.0;
        double velFactor = 1.0;
        int nObj = space.nItems();
        try {
            if (xmlHistory) {
                StringBuilder data = new StringBuilder(XMLmv.putTag("at", jdn.getJdN() + "," + sdf.format(jdn.getTime())));
                for (int o = 0; o < nObj; o++) {
                    data.append(XMLmv.putTag("obj",
                            space.getOneItem(o).statusStringForHistory(posFactor, velFactor)));
                }
                String toFile = XMLmv.putTag("set", data);
                historyFileStream.write(toFile.getBytes());
            }
            else {
                for (int o = 0; o < nObj; o++) {
                    ItemInterface ifc = space.getOneItem(o);
                    if (ifc.getItemType() != ItemInterface.ItemType.SURFACE) {
                        String toFile = "" + jdn.getJdN() + "," + sdf.format(jdn.getTime()) + "," + nowT + "," +
                                ifc.statusStringForHistory(posFactor, velFactor) + "\n";
                        historyFileStream.write(toFile.getBytes());
                    }
                }
            }
            bRetVal = true;
        } catch (IOException e) {
            showError("Some problem writing Vectors to history file " + historyFilePath + "\n" + e.getMessage());
        }
        return bRetVal;
    }

    String getHistoryFilePath() {
        String fileName = historyFilePath;
        String title = "History File Name";
        FileDialog fileDlg =
                new FileDialog(mainF, title,
                        FileDialog.SAVE);
        fileDlg.setFile("*.csv");
        fileDlg.setVisible(true);

        String bareFile = fileDlg.getFile();
        if (!(bareFile == null)) {
            int len = bareFile.length();
            if ((len < 8) || !(bareFile.substring(len - 4).equalsIgnoreCase(".csv"))) {
                showMessage("Adding '.csv' to file name");
                bareFile = bareFile + ".csv";
            }
            fileName = fileDlg.getDirectory() + bareFile;
        }
        return fileName;
    }

    void saveDataToFile() {
        space.noteItemData();
        space.saveInfluenceList();
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
                    if (len > 100 && len < 5e6) {
                        int iLen = (int) len;
                        byte[] data = new byte[iLen + 100];
                        iStream.read(data);
                        String xmlStr = new String(data);
                        if (getBaseDataFromXML(xmlStr) && space.takeFromXML(xmlStr)) {
                            bRetVal = true;
                            space.initAllItemConnections();
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
        if (spSize == SpaceSize.ASTRONOMICAL)
            if (nowDate == null)
                nowDate = new DateAndJDN(dateAndJDN);
        return XMLmv.putTag("baseData" , XMLmv.putTag("calculationStep", calculationStep) +
                XMLmv.putTag("refreshInterval", refreshInterval) +
                XMLmv.putTag("bRealTime", bRealTime) +
                XMLmv.putTag("duration", duration) +
                XMLmv.putTag("bShowPaths", bShowPaths) +
                XMLmv.putTag("bShowLinks", bShowLinks) +
                XMLmv.putTag("spSize", spSize.toString()) +
                XMLmv.putTag("bConsiderTimeDilation", space.bConsiderTimeDilation) +
                XMLmv.putTag("bConsiderGravityVelocity", space.bConsiderGravityVelocity) +
                ((spSize == SpaceSize.ASTRONOMICAL)  ?
                        (XMLmv.putTag("dateAndJDN", dateAndJDN.getJdN())): ""));
    }

    boolean getBaseDataFromXML(String xmlStr) {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "baseData", 0);
        String basicData = vp.val;
        if (basicData.length() > 20) {
            try {
                vp = XMLmv.getTag(basicData, "calculationStep", 0);
                calculationStep = Double.valueOf(vp.val);
                vp = XMLmv.getTag(basicData, "refreshInterval", 0);
                refreshInterval = Double.valueOf(vp.val) ;
                vp = XMLmv.getTag(basicData, "bRealTime", 0);
                bRealTime = vp.val.equals("1");
                vp = XMLmv.getTag(basicData, "duration", 0);
                duration = Double.valueOf(vp.val) ;
                vp = XMLmv.getTag(basicData, "bShowPaths", 0);
                bShowPaths = vp.val.equals("1");
                vp = XMLmv.getTag(basicData, "bShowLinks", 0);
                bShowLinks = vp.val.equals("1");
                vp = XMLmv.getTag(basicData, "spSize", 0);
                setSpaceSize(SpaceSize.getEnum(vp.val));
                vp = XMLmv.getTag(basicData, "bConsiderTimeDilation", 0);
                space.bConsiderTimeDilation = vp.val.equals("1");
                if (space.bConsiderTimeDilation)
                    showMessage("bConsiderTimeDilation is set ON!");
                vp = XMLmv.getTag(basicData, "Warning: bConsiderGravityVelocity", 0);
                space.bConsiderGravityVelocity = vp.val.equals("1");
                if (space.bConsiderGravityVelocity)
                    showMessage("Warning: bConsiderGravityVelocity is set ON!");
                if (spSize == SpaceSize.ASTRONOMICAL) {
                    vp = XMLmv.getTag(basicData, "dateAndJDN", 0);
                    if (vp.val.length() > 0)
                        dateAndJDN = new DateAndJDN(Double.valueOf(vp.val));
                    else {
                        showMessage("The planet data does not contain Date-Time\n" +
                                "taking NOW time");
                        dateAndJDN = new DateAndJDN();
                    }
                }

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
        return mainF;
    }

    public static void showError(String msg, Component caller) {
        JOptionPane.showMessageDialog(caller, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
        mainF.toFront();
    }

    public static void showError(String msg) {
        showError(msg, mainF);
    }

    public static void showMessage(String msg, Component caller) {
        JOptionPane.showMessageDialog(caller, msg, "FOR INFORMATION", JOptionPane.INFORMATION_MESSAGE);
        mainF.toFront();
    }

    public static void showMessage(String title, String msg, Window caller) {
        JOptionPane.showMessageDialog(caller, msg, title, JOptionPane.INFORMATION_MESSAGE);
        if (caller != null)
            caller.toFront();
        else
            mainF.toFront();
    }

    public static void showMessage(String msg) {
        showMessage(msg, mainF);
    }

     public static boolean decide(String title, String msg, Component caller) {
        int resp = JOptionPane.showConfirmDialog(caller, msg, title, JOptionPane.YES_NO_OPTION);
        return resp == JOptionPane.YES_OPTION;
    }

    public static boolean decide(String title, String msg) {
        return decide(title, msg, mainF);
    }

    boolean setDuration() {
        boolean retVal = true;
        if (bEndTime) {
            String endStr = tfEndTime.getText();
            if (endStr.length() >= 8) {
                double endJDN = DateAndJDN.jdnFromString(endStr);
                double startJDN = dateAndJDN.getJdN();
                if (endJDN > startJDN) {
                    duration = (endJDN - startJDN) * 24;
                    showMessage("The duration is " + duration + " h");
                    ntfDuration.setData(duration);
                }
                else
                    retVal = false;
            }
            else
                retVal = false;
        }
        else {
            duration = ntfDuration.getData();
        }
        return retVal;
    }

    class MyListener implements ActionListener {
        MyListener() {

        }
        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            aBlock: {
                if (src == pbStart) {
                    duration = ntfDuration.getData();
                    boolean running = false;
                    if (setDuration() && duration > 0) {
                        space.saveInfluenceList();
                        running = startRunThread();
                    }
                    if (running)
                        pbStart.setEnabled(false);
                    break aBlock;
                }
                else if (src == rbEndTime) {
                    bEndTime = rbEndTime.isSelected();
                    setEndSetting();
                }
                else if (src == rbDuration) {
                    bEndTime = !rbDuration.isSelected();
                    setEndSetting();
                }

            }
        }

        void setEndSetting() {
            tfEndTime.setEnabled(bEndTime);
            ntfDuration.setEnabled(!bEndTime);
        }
    }

    class MenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            bBlock: {
                if (src == mIScheme) {
                    selectScheme();
                    break bBlock;
                }
                if (src == mISaveData) {
                    saveDataToFile();
                    break bBlock;
                }
                if (src == mIClearData) {
                    space.clearSpace();
                    setSpaceSize(SpaceSize.DAILY);
                    proceedToItemList(true);
                    break bBlock;
                }
                if (src == mIReadData) {
                    if (getDataFromFile())
                        proceedToItemList(true);
                    break bBlock;
                }
                if (src == mIExit) {
                    mainF.setVisible(false);
                    break bBlock;
                }
                if (src == rIAstronomical) {
                    setSpaceSize(SpaceSize.ASTRONOMICAL);
                    showMessage("Astronomical Selected");
                    break bBlock;
                }
                if (src == rIDaily) {
                    setSpaceSize(SpaceSize.DAILY);
                    showMessage("Daily Selected");
                    break bBlock;
                }
                if (src == mITuning) {
                    getTimingValues();
                    break bBlock;
                }
            }
        }
    }

    public void setSpaceSize(SpaceSize size) {
        switch(size) {
            case DAILY:
                setTimingValues(0.0002, 0.02, 200, false, true, false, true);
                repeats = 1;
                rIDaily.setSelected(true);
                rbDuration.setSelected(true);
                rbEndTime.setEnabled(false);
                break;
            case ASTRONOMICAL:
                setTimingValues(10, 10 * 100, 200000, true, false, true, false);
                repeats = 2;
                rIAstronomical.setSelected(true);
                rbEndTime.setEnabled(true);
                break;
        }
        spSize = size;
    }

    void setCalculationDate(double jdn) {
        if (jdn > 0)
            dateAndJDN = new DateAndJDN(jdn);
    }

    void modifyJTextEdit() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addPropertyChangeListener("permanentFocusOwner", new PropertyChangeListener() {

                    public void propertyChange(final PropertyChangeEvent e) {

                        if (e.getOldValue() instanceof JTextField) {
                            SwingUtilities.invokeLater(() -> {
                                JTextField oldTextField = (JTextField) e.getOldValue();
                                oldTextField.setSelectionStart(0);
                                oldTextField.setSelectionEnd(0);
                            });

                        }

                        if (e.getNewValue() instanceof JTextField) {
                            SwingUtilities.invokeLater(() -> {
                                JTextField textField = (JTextField) e.getNewValue();
                                textField.selectAll();
                            });

                        }
                    }
                });
    }

    static public void debug(String msg) {
        System.out.println("DEBUG: " + msg);
    }

    public static void main(String[] args) {
        final ItemMovementsApp orbit = new ItemMovementsApp(true);
        orbit.setVisible(true);
    }
}
