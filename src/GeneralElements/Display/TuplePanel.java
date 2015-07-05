package GeneralElements.Display;

import mvUtils.display.InputControl;
import mvUtils.display.NumberTextField;

import javax.swing.*;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Created by M Viswanathan on 07 Jun 2014
 */
public class TuplePanel extends JPanel {
    InputControl control;
    String title;
    Vector3d vec3d;
    NumberTextField ntX, ntY, ntZ;
    public TuplePanel (InputControl control,  Tuple3d val3d, int size, double min, double max, String fmtStr, String title) {
        super(new BorderLayout());
        this.title = title;
        this.control = control;
        vec3d = new Vector3d(val3d);
        init(size, min, max, fmtStr);
//        ntX = new NumberTextField(control, vec3d.x, size, false, min, max, fmtStr, "X " + title);
//        ntY = new NumberTextField(control, vec3d.y, size, false, min, max, fmtStr, "Y " + title);
//        ntZ = new NumberTextField(control, vec3d.z, size, false, min, max, fmtStr, "Z " + title);
//        add(ntX, BorderLayout.WEST);
//        add(ntY, BorderLayout.CENTER);
//        add(ntZ, BorderLayout.EAST);
    }

    public TuplePanel (InputControl control, double min, double max, String fmtStr, String title) {
        super(new BorderLayout());
        this.title = title;
        this.control = control;
        vec3d = new Vector3d();
        init(6, min, max, fmtStr);
    }

    void init(int size, double min, double max, String fmtStr) {
        ntX = new NumberTextField(control, vec3d.x, size, false, min, max, fmtStr, "X " + title);
        ntY = new NumberTextField(control, vec3d.y, size, false, min, max, fmtStr, "Y " + title);
        ntZ = new NumberTextField(control, vec3d.z, size, false, min, max, fmtStr, "Z " + title);
        add(ntX, BorderLayout.WEST);
        add(ntY, BorderLayout.CENTER);
        add(ntZ, BorderLayout.EAST);
    }

    public void setEditable(boolean ena) {
        ntX.setEditable(ena);
        ntY.setEditable(ena);
        ntZ.setEditable(ena);
    }

    public String getTitle() {
        return title;
    }

    public Tuple3d getTuple3d() {
        if (ntX.isInError() || ntY.isInError() || ntY.isInError())
            return null;
        else {
            vec3d.set(ntX.getData(), ntY.getData(), ntZ.getData());
            return vec3d;
        }
    }

    public void setEnabled(boolean ena) {
        ntX.setEnabled(ena);
        ntY.setEnabled(ena);
        ntZ.setEnabled(ena);
        super.setEnabled(ena);
    }

    public void updateTuple(Tuple3d val3d) {
        vec3d.set(val3d);
        ntX.setData(val3d.x);
        ntY.setData(val3d.y);
        ntZ.setData(val3d.z);
    }
}
