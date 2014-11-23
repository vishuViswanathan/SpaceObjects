package SpaceElements;

import mvUtils.math.*;

import javax.swing.*;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: M Viswanathan
 * Date: 11/28/13
 * Time: 4:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class TheSpace {//extends GraphInfoAdapter {
    LinkedList<SpaceObject> objects;
    int reportSteps;
    int stepCount;

    public TheSpace() {
        objects = new LinkedList<SpaceObject>();
    }

    LinkedList<SpaceObject> getAllObjects() {
        return objects;
    }

    public int addObject(SpaceObject oneObject)  {
        objects.add(oneObject);
        oneObject.setSpace(this);
        return objects.size();
    }

    public void initAcc() {
        for (SpaceObject o: objects)
            o.initAcc();
    }

    public void initForces() {
        for (SpaceObject o: objects)
            o.initForce();
    }

    void updatePosAndVel(double deltaT, double nowT) {
        for (SpaceObject o: objects)
            o.updatePosAndVel(deltaT, nowT);
    }

    void evalInfluence(double deltaT, double nowT)  {
        SpaceObject obj1;
        int oLen = objects.size();
//        initAcc();
        initForces();
        for (int o = 0; o < oLen; o++) {
            obj1 = objects.get(o);
            for (int n = o + 1; n < oLen; n++)
                obj1.evalForce(objects.get(n));
//            obj1.evalAcceleration(objects.get(n));
        }
        updatePosAndVel(deltaT, nowT);
    }

/*
    public void showStatus() {
        for (SpaceObject o: objects)
            o.showStatus();
    }
*/

    public DoubleMaxMin xMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin(0, 0);
        for (SpaceObject o: objects)
            maxMin.takeMaxValue(o.status.pos.getX());
        return maxMin;
    }

    public DoubleMaxMin yMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin(0, 0);
        for (SpaceObject o: objects)
            maxMin.takeMaxValue(o.status.pos.getY());
        return maxMin;
    }

    public DoubleMaxMin zMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin(0, 0);
        for (SpaceObject o: objects)
            maxMin.takeMaxValue(o.status.pos.getZ());
        return maxMin;
    }

    public int nObjects() {
        return objects.size();
    }

    public JPanel dataPanel(int o) {
        if (o < objects.size())
            return objects.get(o).dataPanel(o + 1);
        else
            return null;
    }

    public JPanel resultPanel()   {
        JPanel jp = new JPanel();
        return jp;
    }

    public void noteInput() {
        for (SpaceObject o: objects)
            o.noteInput();
    }

    public void doCalculation(double deltaT, double nowT) {
        evalInfluence(deltaT, nowT);
    }

    public void updateDisplayData() {
        for (SpaceObject o:objects)
            o.updateOrbitAndPos();
    }

/*
//    @Override
    public DoublePoint[] getGraph(int o) {
        return objects.get(o).getXYHistory();
     }

//    @Override
    public int traceCount() {
        return objects.size();
    }

//    @Override
    public DoubleRange getCommonXrange() {
        double xMax = Double.NEGATIVE_INFINITY;
        double xMin = Double.POSITIVE_INFINITY;
        for (SpaceObject o: objects) {
            xMax = Math.max(xMax, o.xMax);
            xMin = Math.min(xMin, o.xMin);
        }

        double addRange =  0.1 * (xMax - xMin);
        return new DoubleRange(xMin - addRange, xMax + addRange);
     }

//    @Override
    public DoubleRange getCommonYrange() {
        double yMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        for (SpaceObject o: objects) {
            yMax = Math.max(yMax, o.yMax);
            yMin = Math.min(yMin, o.yMin);
        }

        double addRange =  0.1 * (yMax - yMin);
        return new DoubleRange(yMin - addRange, yMax + addRange);
    }

//    @Override
    public DoubleRange getXrange(int trace) {
        return getCommonXrange();
    }

//    @Override
    public DoubleRange getYrange(int trace) {
        return getCommonYrange();
    }

*/
    public SpaceObject getOneObject(int o) {
        return objects.get(o);
    }

}
