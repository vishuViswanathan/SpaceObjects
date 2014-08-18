package SpaceElements.Display;

import mvmath.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: M Viswanathan
 * Date: 12/4/13
 * Time: 7:07 PM
 * To change this template use File | Settings | File Templates.
 */

import java.awt.event.*;

/**
 * class GraphicOrbit
 * Collects data from the GraphInfo interface and displays
 * the trace/traces on a specified Graphics of plot area
 * specified after scaling
 */

public class GraphicOrbit {
    public static final int YPOSITVEISUP = 0;
    public static final int YPOSITVEISDOWN = 1;
    public static final int CURSVALPANALNONE = 0;
    public static final int CURSVALPANALATBOTTOM = 1;
    public static final int CURSVALPANALATLEFT = 2;
    public static final int CURSVALPANALATRIGHT = 3;

    public static enum LineStyle {NORMAL, DASHED, DOTTED; }

    GraphInfo allGInfo;
    Vector<OneTrace> traces;
    int numTraces;
    int traceScale = 0; // trace foe which the axes are shown
    int traceToShow = -2; // -1 is all else the trace number
    public static int MAXTRACES = 6;
    public static final Color[] COLORS = {
            Color.blue, Color.red,
            Color.black, Color.gray, Color.orange, Color.cyan};
    BasicCalculData basicData = null;
    JPanel plotPanel;
    GraphPanel graphPanel = new GraphPanel();
    JPanel titlePanel = new FramedPanel(new GridLayout(1, 0)); //new FramedPanel(new GridLayout(1, 0),false);
    Vector<JTextField> cursorVals = new Vector<JTextField>();
    JTextField tfXpos;
    JTextField tfMshiftX;
    JPanel cursorValPanel;
    Rectangle gPlotRect;
    Rectangle graphRect;
    Point origin; // in pecentage of graph area size
    int xMinPos = 70;
    int lastx = xMinPos; // line cursor position
    EventDespatcher frameEventDespatcher;
    Insets graphInset = new Insets(20, 5, 5, 5);
    IntRange xIntRange, yIntRange;
    IntDoubleScaler xCommScale;
    IntDoubleScaler yCommScale;
    DoubleRange xDRangeZall, yDRangeZall;
    Point intOrigin;
    BasicStroke tWidth = new BasicStroke(2F);
    BasicStroke sWidth = new BasicStroke(0F);
    int curValPanelLoc = CURSVALPANALATBOTTOM;
    JLabel lXName;
    Vector <JLabel> vlYName;
    JButton zoomAll = new JButton("ZoomAll");
    boolean bFirstTime = true;
    /**
     * only plot size defined. Traces to be added
     * susequently using addTrace()
     *
     * @param origin is specified in % of graph area
     */
    public GraphicOrbit(JPanel plotPanel,
                        Point origin,
                        EventDespatcher frameEventDespatcher, int curValPanelLoc) {
        this.plotPanel = plotPanel;
        this.curValPanelLoc = curValPanelLoc;
        vlYName = new Vector<JLabel>();
        int rows = 1 ;
        int cols = 4;
        cursorValPanel = new JPanel();
        plotPanel.setLayout(new BorderLayout());
        Dimension size = plotPanel.getSize();
        gPlotRect = new Rectangle(graphInset.left, graphInset.top,
                (size.width - graphInset.left - graphInset.right),
                (size.height - graphInset.top - graphInset.bottom));
        this.origin = new Point(origin);
        this.frameEventDespatcher = frameEventDespatcher;

        traces = new Vector<OneTrace>();
        tfXpos = new JTextField(6);
        lXName = new JLabel("X  ");
        addCurtValUI(lXName, tfXpos, Color.black);
        tfMshiftX = new JTextField(6);
        numTraces = 0;
        CursorAtMousePos l = new CursorAtMousePos();
        graphPanel.addMouseMotionListener(l);
        graphPanel.addMouseListener(l);
        graphPanel.addMouseWheelListener(l);
    }

    /**
     * This constructor is used for a single graph 'trace' in
     * graphInfo object.
     *
     * @param
     * @param origin in % of plot area
     */
    public GraphicOrbit(GraphInfo graphInfo,
                        int trace,
                        JPanel plotPanel, Point origin,
                        EventDespatcher frameEventDespatcher) {
        this(plotPanel, origin, frameEventDespatcher);
        allGInfo = graphInfo;
        addTrace(graphInfo, trace, Color.black);
    }

    /**
     * Constructor
     * This constructor is used for a set of all graphs available
     * in graphInfo object.
     */
    public GraphicOrbit(GraphInfo graphInfo,
                        JPanel plotPanel,
                        Point origin,
                        EventDespatcher frameEventDespatcher) {
        this(plotPanel, origin, frameEventDespatcher);
        allGInfo = graphInfo;
        int nTraces = graphInfo.traceCount();
        for (int trace = 0; trace < nTraces; trace++) {
            addTrace(graphInfo, trace, COLORS[trace % COLORS.length]);
        }
    }
 int nCursorVals = 0;
    void addCurtValUI(JLabel nameL, JTextField textF, Color color) {
        textF.setEditable(false);
        textF.setBackground(SystemColor.lightGray);
        FramedPanel pan = new FramedPanel(new GridBagLayout());
        nameL.setPreferredSize(new Dimension(120, 20));
        nameL.setForeground(color);
        nameL.setHorizontalAlignment(JLabel.RIGHT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        pan.add(nameL, gbc);
        gbc.gridx++;
        pan.add(textF, gbc);
        gbc.gridx++;
        zoomAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomAll();
            }
        });
        pan.add(zoomAll, gbc);
        pan.setBackground(SystemColor.lightGray);
        nCursorVals++;
        cursorValPanel.add(pan);
    }

    void addCurtValUI(String name, JTextField textF, Color color) {
        addCurtValUI(new JLabel(name + "  "), textF, color);
    }


    public GraphicOrbit(JPanel plotPanel,
                        Point origin,
                        EventDespatcher frameEventDespatcher) {
        this(plotPanel, origin, frameEventDespatcher, CURSVALPANALATBOTTOM);
    }

    public void setBasicCalculData(BasicCalculData basicData) {
        this.basicData = basicData;
    }

    public void prepareDisplay() {
        setupPanels();
    }

    void setupPanels() {
        plotPanel.add(titlePanel, BorderLayout.NORTH);
        graphPanel.setSize(gPlotRect.width, gPlotRect.height);
        plotPanel.add(graphPanel);
        JPanel outerP = new JPanel();
        outerP.add(cursorValPanel);
        switch(curValPanelLoc) {
            case CURSVALPANALATLEFT:
                plotPanel.add(outerP, BorderLayout.WEST);
                break;
            case CURSVALPANALATRIGHT:
                plotPanel.add(outerP, BorderLayout.EAST);
                break;
            case CURSVALPANALATBOTTOM:
                plotPanel.add(outerP, BorderLayout.SOUTH);
                break;
            default:
                break;
        }
     }

    /**
     * @param graphInfo
     * @param trace
     * @param color
     * @return
     */
    public int addTrace(GraphInfo graphInfo,
                        int trace, Color color) {
         return addTrace(graphInfo, trace, color, LineStyle.NORMAL,  YPOSITVEISUP);
    }

    public int addTrace(GraphInfo graphInfo,
                        int trace, Color color, LineStyle lStyle) {
//        allGInfo = graphInfo;
        return addTrace(graphInfo, trace, color, lStyle, YPOSITVEISUP);
    }

    /**
     * @param graphInfo
     * @param trace
     * @param color
     * @return
     */
    public int addTrace(GraphInfo graphInfo,
                        int trace, Color color, LineStyle lStyle, int mode) {
        allGInfo = graphInfo;
        JTextField tf = new JTextField(6);
        cursorVals.addElement(tf);
        OneTrace oneTrace = new OneTrace(graphInfo, trace, color, lStyle, mode, tf, tfXpos);
        traces.addElement(oneTrace);
        if (oneTrace.traceName.length() > 0) {
            JLabel lName = new JLabel(oneTrace.traceName);
            addCurtValUI(lName, tf, oneTrace.color);
            vlYName.add(lName);
        }

        tf.setForeground(color);
        tf.setBackground(Color.lightGray);
        tf.setEditable(false);

        cursorValPanel.add(tf);
        numTraces++;
        return numTraces;
    }

    public int addTrace(GraphInfo graphInfo,
                        int trace, Color color, int mode) {
        return addTrace(graphInfo, trace, color, LineStyle.NORMAL,  mode);
    }

     public void setbShowVal(int trace, boolean bShowVal) {
        OneTrace oneT = traces.get(trace);
        if (oneT != null)
            oneT.setbShowVal(bShowVal);
    }

    public int traceCount() {
        return numTraces;
    }

    void setXname() {
        int traceForX = (traceToShow < 0) ? 0 : traceToShow;
        TraceHeader header = traces.get(traceForX).getHeader();
        if (header != null && header.getxName().length() > 0)
            lXName.setText(header.getxName() + "  ");
    }

    void setYnames() {
        String tName;
        OneTrace tr;
        JLabel jL;
        for (int t = 0; t < vlYName.size(); t++) {
            jL = vlYName.get(t);
            tr = traces.get(t);
            tName = tr.getHeader().getTraceName();
            jL.setText(tName);
        }
    }

    public void headerChanged() {
        setXname();
        setYnames();
    }

    public void setTraceToShow(int toShow) {
        toShow = (toShow < 0) ? -1 : ((toShow < numTraces) ? toShow : traceToShow);
        if (toShow != traceToShow) {
            traceToShow = toShow;
            traceScale = (toShow < 0) ? 0 : toShow;
            setXname();
            graphPanel.repaint();
        }
    }


    /**
     * @param g
     * @param refresh
     */
    public void drawGraph(Graphics g, boolean refresh) {
        int left = xMinPos, right = 30, top = 10, bottom = 40;
        Dimension dim = graphPanel.getSize();
        OneTrace oneTrace;
        graphRect = new Rectangle(left, top,
                dim.width - (left + right),
                dim.height - (top + bottom));
        intOrigin = new Point(
                (int) ((double) graphRect.width * origin.x / 100 + left),
                (int) ((double) graphRect.height * (1 - (double) origin.y / 100)
                        + top));
            if(bFirstTime)   {
                setCommonGraphSize();
                bFirstTime = false;
            }
            setGraphSizes();
            g.setColor(Color.black);
            Rectangle r = graphRect;
            if (basicData != null) {
                basicData.drawBasePic(g, graphRect, intOrigin);
            }
            for (int trace = 0; trace < numTraces; trace++) {
                oneTrace = (OneTrace) traces.elementAt(trace);
                oneTrace.drawTrace(g, true); //refresh);
            }
    }

    void setGraphSizes() {
        for (OneTrace ot:traces) {
            ot.setGraphSize(graphRect, intOrigin, xCommScale, yCommScale);
        }
    }

    void setCommonGraphSize() {
        if (intOrigin != null) {
            xIntRange = new IntRange(graphRect.x,
                    graphRect.x + graphRect.width);
            yIntRange = new IntRange(intOrigin.y, graphRect.y);

        }
        xDRangeZall =  allGInfo.getCommonXrange();
        yDRangeZall =  allGInfo.getCommonYrange();
        xCommScale = new IntDoubleScaler(xIntRange, new DoubleRange(xDRangeZall));
        yCommScale = new IntDoubleScaler(yIntRange, new DoubleRange(yDRangeZall));
        normaliseScales();
        xDRangeZall = new DoubleRange(xCommScale.getDoubleRange());
        yDRangeZall = new DoubleRange(yCommScale.getDoubleRange());
//        debug("Factors , xCommScale = " + xCommScale.factor() + ", yCommScale = " + yCommScale.factor());
    }

    void normaliseScales()  {   // both x y scales to have same factor
        double xFactor = xCommScale.factor();     // pixels /m
        double yFactor = -yCommScale.factor();    // since drawn reverse
        double commFactor =  Math.min(xFactor, yFactor);
        DoubleRange drX = xCommScale.getDoubleRange();
        DoubleRange drY = yCommScale.getDoubleRange();
        double mulX = xFactor / commFactor;
        double mulY = yFactor  /commFactor;
        drX.scaleDouble(mulX) ;
        drY.scaleDouble(mulY);
        xCommScale.setDoubleRange(drX);
        yCommScale.setDoubleRange(drY);
    }

    Point lastPoint;

    void notePoint(int x, int y) {
        lastPoint = new Point(x, y);
    }

    void shiftDisplay(int x, int y) {
        double lastDx = traces.elementAt(0).getXdouble(lastPoint.x);
        double lastDy = traces.elementAt(0).getYdouble(lastPoint.y);
        double nowDx = traces.elementAt(0).getXdouble(x);
        double nowDy = traces.elementAt(0).getYdouble(y);
        DoubleRange xR = xCommScale.getDoubleRange();
        DoubleRange yR = yCommScale.getDoubleRange();
        xR.shift(lastDx - nowDx);
        yR.shift(lastDy - nowDy);
        lastPoint = new Point(x, y);
        setGraphSizes();
        graphPanel.repaint();
    }

    void zoom(int x, int y, int zn) {
//        debug("zoom " + z);
        double reqdDx = traces.elementAt(0).getXdouble(x);
        double reqdDy = traces.elementAt(0).getYdouble(y);
        double factor = (zn > 0) ? 1.5 : 0.66;
        DoubleRange xR = xCommScale.getDoubleRange();
        DoubleRange yR = yCommScale.getDoubleRange();
        xR.scaleDouble(factor);
        yR.scaleDouble(factor);
        xCommScale.setDoubleRange(xR);
        yCommScale.setDoubleRange(yR);
        setGraphSizes();
        double nowDx = traces.elementAt(0).getXdouble(x);
        double nowDy = traces.elementAt(0).getYdouble(y);
        xR.shift(reqdDx - nowDx);
        yR.shift(reqdDy - nowDy);
        setGraphSizes();

        graphPanel.repaint();
    }

    void zoomAll() {
        DoubleRange xR = xCommScale.getDoubleRange();
        DoubleRange yR = yCommScale.getDoubleRange();
        xR.setMinMax(xDRangeZall.min, xDRangeZall.max);
        yR.setMinMax(yDRangeZall.min, yDRangeZall.max);
        xCommScale.setDoubleRange(xR);
        yCommScale.setDoubleRange(yR);
        setGraphSizes();
        graphPanel.repaint();
    }

    public void showGraph() {
        graphPanel.repaint(graphRect.x, graphRect.y, graphRect.width + 100, graphRect.height + 2);
    }

    void debug(String msg) {
        System.out.println("GraphicOrbit: " + msg);
    }

    class GraphPanel
            extends FramedPanel {

        GraphPanel() {
//			super(false); // lowered panel
//			setInsets(5, 5, 5, 5);
            setBackground(SystemColor.lightGray);
        }

        public void paint(Graphics g) {
            super.paint(g);
            drawGraph(g, false);
//            drawLineCursor(g);
        }
    }

    class CursorAtMousePos
            extends MouseAdapter
            implements MouseMotionListener, MouseWheelListener {
        public void mouseDragged(MouseEvent me) {
            shiftDisplay(me.getX(), me.getY());
        }

        public void mouseMoved(MouseEvent me) {
        }

        public void mousePressed(MouseEvent me) {
            notePoint(me.getX(), me.getY());
//            setCenter(me.getX(), me.getY());
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int posx = e.getX();
            int posy = e.getY();
            int iScroll = e.getUnitsToScroll();
            zoom(posx, posy, iScroll) ;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
//            zoom(10) ;
            super.mouseEntered(e);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }


     class OneTrace {
        int graphMode;
        GraphInfo gInfo;
        int traceIngInfo;
        IntRange xPostvIntRange;
        IntRange yPostvIntRange;
        //  IntRange xNegtvIntRange;
//      IntRange yNegtvIntRange;
        DoublePoint[] graph;
        Point[] points;
        Color color;
        int[] xPos;
        int[] yPos;
        int nPoints;
        IntDoubleScaler xScale;
        IntDoubleScaler yScale;

        Rectangle plotRect;
        String traceName = "";
        Point origin;
        JTextField yTextF;
        JTextField xTextF;
        boolean bShowVal = true;
        LineStyle lStyle = LineStyle.NORMAL;
        Stroke lStroke;
        TraceHeader header;

        OneTrace(GraphInfo graphInfo, int trace, Color color, LineStyle lStyle, int mode, JTextField yTextField, JTextField xTextField) {
            gInfo = graphInfo;
            header= gInfo.getTraceHeader(trace);
            if (header != null)
                traceName = gInfo.getTraceHeader(trace).getTraceName();
            traceIngInfo = trace;
            this.color = color;
            this.lStyle = lStyle;
            setLineStyle();
            this.graphMode = checkMode(mode);
            yTextF = yTextField;
            xTextF = xTextField;
        }

        OneTrace(GraphInfo graphInfo, int trace, Color color, int mode, JTextField yTextField, JTextField xTextField) {
            this(graphInfo, trace, color, LineStyle.NORMAL, mode, yTextField, xTextField);
        }

        TraceHeader getHeader() {
            return header;
        }

        void setLineStyle() {
            switch (lStyle) {
                case DASHED:
                    lStroke = new BasicStroke(2, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND, 1.0f, new float[]{4f, 2f},2f);
                    break;
                case DOTTED:
                    lStroke = new BasicStroke(2, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND, 1.0f, new float[]{2f, 2f, 2f},2f);
                    break;
            }
        }

         void setbShowVal(boolean bShowVal) {
            this.bShowVal = bShowVal;
        }

        int checkMode(int mode) {
            if (mode == GraphDisplay.YPOSITVEISDOWN) {
                return mode;
            } else {
                return GraphDisplay.YPOSITVEISUP;
            }
        }


        void showCursorXVal(int x) {
            xTextF.setText(SetNumberFormat.format((double)getXdouble(x)));
        }

        void showCursorYVal(int y) {
            yTextF.setText(SetNumberFormat.format((double)getYdouble(y)));
        }

        String getXFormat() {
            return gInfo.getXFormat(traceIngInfo);
        }

        String getYFormat() {
            return gInfo.getYFormat(traceIngInfo);
        }

        void setGraphSize(Rectangle plotRect, Point origin, IntDoubleScaler commXscale, IntDoubleScaler commYScale) {
            this.plotRect = plotRect;
            if (origin != null) {
                this.origin = new Point(origin);
                xPostvIntRange = new IntRange(plotRect.x,
                        plotRect.x + plotRect.width);
                yPostvIntRange = new IntRange(origin.y, plotRect.y);

            }
            xScale = commXscale;
            yScale = commYScale;
        }

        void drawTrace(Graphics g, boolean refresh) {
            ((Graphics2D) g).setStroke(tWidth);
            if (refresh || graph == null) {
                getGraph();
            }
            //    drawAxes(g);
            g.clipRect(plotRect.x, plotRect.y, plotRect.width + 1,
                    plotRect.height + 1);
            g.setColor(color);
            if (lStyle != LineStyle.NORMAL)
                ((Graphics2D) g).setStroke(lStroke);
            if (nPoints > 1)
                g.drawPolyline(xPos, yPos, nPoints);
            else if (nPoints == 1)
                g.drawString("One Data", plotRect.width/ 2, plotRect.height / 2 );
            else
                g.drawString("NO DATA", plotRect.width/ 2, plotRect.height / 2 );
            ((Graphics2D) g).setStroke(tWidth);

        }

        void getGraph() {
            graph = gInfo.getGraph(traceIngInfo);
            if (graph != null) {
                nPoints = graph.length;
                xPos = new int[nPoints];
                yPos = new int[nPoints];
                for (int p = 0; p < nPoints; p++) {
                    xPos[p] = xScale.intVal(graph[p].x);
                    yPos[p] = yScale.intVal(graph[p].y);
                }
            }
            else
                nPoints = 0;
//debug("traceIngInfo, nPoints = " + traceIngInfo + ", " + nPoints);
        }

        double getXdouble(int x) {
            return xScale.doubleVal(x);

        }

        double getYdouble(int y) {
            return yScale.doubleVal(y);
        }
    } // calss OneTrace
}
