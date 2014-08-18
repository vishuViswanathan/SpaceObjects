package SpaceElements.testing;

/**
 * Created by IntelliJ IDEA.
 * User: M Viswanathan
 * Date: 12/22/13
 * Time: 3:15 PM
 * To change this template use File | Settings | File Templates.
 */
import com.sun.j3d.utils.universe.SimpleUniverse;

import com.sun.j3d.utils.geometry.ColorCube;

import javax.media.j3d.BranchGroup;

import javax.media.j3d.Canvas3D;

import java.awt.GraphicsConfiguration;

import java.awt.BorderLayout;

import java.awt.Label;

import java.applet.Applet;

import com.sun.j3d.utils.applet.MainFrame;

public class CanvasDemo extends Applet {

public CanvasDemo() {


   setLayout(new BorderLayout());

   GraphicsConfiguration config =

   SimpleUniverse.getPreferredConfiguration();

   Canvas3D canvas = new Canvas3D(config);

   add("North",new Label("This is the top"));

   add("Center", canvas);

   add("South",new Label("This is the bottom"));

   BranchGroup contents = new BranchGroup();

   contents.addChild(new ColorCube(0.3));

   SimpleUniverse universe = new SimpleUniverse(canvas);

   universe.getViewingPlatform().setNominalViewingTransform();

   universe.addBranchGraph(contents);

}

public static void main( String[] args ) {

   CanvasDemo demo = new CanvasDemo();

   new MainFrame(demo,400,400);

}

}

