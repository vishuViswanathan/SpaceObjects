package SpaceElements;

import display.NumberLabel;
import mvmath.DoublePoint;
import mvmath.GraphInfoAdapter;
import mvmath.MultiColData;
import mvmath.VariableDataTrace;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.vecmath.Point3d;
import java.text.DecimalFormat;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: M Viswanathan
 * Date: 11/30/13
 * Time: 6:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectHistory {
    public SpaceObject historyOf;
    Vector <ObjectStat> histDat;
    String posUnit = "km";
    double factor = 0.001;  // m to km
    DecimalFormat timeFmt = new DecimalFormat("#,###.000");
    DecimalFormat lenFmt = new DecimalFormat("##0.0000E00");
    DecimalFormat velFmt = new DecimalFormat("#,###");
    DecimalFormat accFmt = new DecimalFormat("##0.0000E00");


    ObjectHistory(SpaceObject historyOf) {
        histDat = new Vector<ObjectStat>();
        this.historyOf = historyOf;
    }

    public void clear() {
        histDat.clear();
    }
    public void add(ObjectStat oneStat) {
        ObjectStat newObj = new ObjectStat(oneStat);
        histDat.add(newObj);
    }

    public JTable getTable() {
        Object[][] allData = new Object[histDat.size() + 1][11];  // for time, x, y, z
        Object[] header = new String[]{"time (h)", "x (km)", "y (km)", "z (km)", "r (km)",
                                        "Vx (m/s)", "Vy (m/s)", "Vz (m/s)",
                                        "Ax (m/s2)", "Ay (m/s2)", "Az (m/s2)"};
        int row = 0;
        int col = 0;
        for (ObjectStat oneStat: histDat) {
            col = 0;
            allData[row][col++] = timeFmt.format(oneStat.time / 3600);
            allData[row][col++] = lenFmt.format(oneStat.pos.x / 1000);
            allData[row][col++] = lenFmt.format(oneStat.pos.y / 1000);
            allData[row][col++] = lenFmt.format(oneStat.pos.z / 1000);
            if (historyOf.primary != null)
                allData[row][col++] = lenFmt.format(oneStat.distFromPrimary / 1000);
            else
                allData[row][col++] = "";
            allData[row][col++] = velFmt.format(oneStat.velocity.x);
            allData[row][col++] = velFmt.format(oneStat.velocity.y);
            allData[row][col++] = velFmt.format(oneStat.velocity.z);

            allData[row][col++] = accFmt.format(oneStat.acc.x);
            allData[row][col++] = accFmt.format(oneStat.acc.y);
            allData[row][col++] = accFmt.format(oneStat.acc.z);
            row++;
        }
        JTable table = new JTable(allData, header);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment( JLabel.RIGHT );
        TableColumnModel colMod =  table.getColumnModel();
        TableColumn colm;
        colm = colMod.getColumn(0);
        colm.setPreferredWidth(50);
        colm.setCellRenderer(rightRenderer);
        for (int c = 1; c < col; c++)   {
            colm = colMod.getColumn(c);
            colm.setPreferredWidth(70);
            colm.setCellRenderer(rightRenderer);
        }
        return table;
     }

    DoublePoint[] getXYHistory() {
        DoublePoint[] xyDat = new DoublePoint[histDat.size()];
        int p = 0;
        for (ObjectStat oneStat: histDat) {
            xyDat[p] = new DoublePoint(oneStat.pos.x, oneStat.pos.y);
            p++;
        }
        return xyDat;
    }

    public Point3d[] getPositionPoints() {
        Point3d[] points = new Point3d[histDat.size()];
        int p = 0;
        for (ObjectStat stat:histDat) {
            points[p++] = stat.pos;
        }
        return points;
    }

}

