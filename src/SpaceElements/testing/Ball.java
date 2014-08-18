package SpaceElements.testing;

/**
 * Created by IntelliJ IDEA.
 * User: M Viswanathan
 * Date: 12/21/13
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.*;

import com.sun.j3d.utils.universe.*;

import javax.media.j3d.*;

import javax.vecmath.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Ball implements MouseListener, MouseMotionListener {

    public Ball() {

        // Create the universe

        SimpleUniverse universe = new SimpleUniverse();

        // Create a structure to contain objects

        BranchGroup group = new BranchGroup();

        // Create a ball and add it to the group of objects

        Sphere sphere = new Sphere(0.5f);

        group.addChild(sphere);

        LineArray curveArr =  new LineArray(6, GeometryArray.COORDINATES|GeometryArray.COLOR_3);
        Point3d[] points =  {new Point3d(0.2, 0.2, 0.2), new Point3d(0.2, 0.3, 0.3),
                                            new Point3d(0.2, 0.3, 0.3), new Point3d(0.4, 0.2, 0.4),
                                            new Point3d(0.4, 0.2, 0.4), new Point3d(0.4, 0.3, 0.6)};
        Color3f color = new Color3f(1f, 11f, 1f);
        Color3f[] colors = {color, color, color};
        curveArr.setCoordinates(0, points);
        curveArr.setColors(0, colors);
        Shape3D curve = new Shape3D(curveArr);
        TransformGroup mainTG = new TransformGroup();


        PointArray pointArr =  new PointArray(6, GeometryArray.COORDINATES|GeometryArray.COLOR_3);
        Point3d[] ps =  {new Point3d(0.2, 0.2, 0.2), new Point3d(0.2, 0.3, 0.3),
                                            new Point3d(0.2, 0.3, 0.3), new Point3d(0.4, 0.2, 0.4),
                                            new Point3d(0.4, 0.2, 0.4), new Point3d(0.4, 0.3, 0.6)};
        Color3f ptcolor = new Color3f(1f, 11f, 1f);
        Color3f[] ptcolors = {ptcolor, ptcolor, ptcolor};
        pointArr.setCoordinates(0, ps);
        pointArr.setColors(0, ptcolors);
        Shape3D p = new Shape3D(pointArr);

        mainTG.addChild(p);
//        mainTG.addChild(curve);
                // Create a red light that shines for 100m from the origin
                Color3f light1Color = new Color3f(1.8f, 0.1f, 0.1f);

        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 0.1);

        Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -7.0f);

        DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);

        light1.setInfluencingBounds(bounds);

       mainTG.addChild(light1);

       // look towards the ball
       ViewingPlatform vgPlat = universe.getViewingPlatform();
       vgPlat.setNominalViewingTransform();
       ViewPlatform vPlat = vgPlat.getViewPlatform();

       // add the group of objects to the Universe
       group.addChild(mainTG);
       universe.addBranchGraph(group);

    }

    void addMouseAction(TransformGroup tg) {
        MouseRotate behavior = new MouseRotate();
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        behavior.setTransformGroup(tg);
        BoundingSphere bounds =
  	        new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        behavior.setSchedulingBounds(bounds);
        tg.addChild(behavior);
        MouseTranslate trBehavior = new MouseTranslate();
        trBehavior.setTransformGroup(tg);
        trBehavior.setSchedulingBounds(bounds);
        tg.addChild(trBehavior);
        MouseZoom zoom = new MouseZoom();
        zoom.setTransformGroup(tg);
        zoom.setSchedulingBounds(bounds);
        tg.addChild(zoom);
    }

    @Override
   	public void mouseClicked(MouseEvent arg0) {
   	}

   	@Override
   	public void mouseEntered(MouseEvent arg0) {
   	}

   	@Override
   	public void mouseExited(MouseEvent arg0) {
   	}

   	@Override
   	public void mousePressed(MouseEvent event) {
   	}

   	@Override
   	public void mouseReleased(MouseEvent arg0) {
   	}

   	@Override
   	public void mouseDragged(MouseEvent event) {
   	}

   	@Override
   	public void mouseMoved(MouseEvent arg0) {
   	}
    public static void main(String[] args) { new Ball(); }

    }