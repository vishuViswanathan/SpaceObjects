package SpaceElements.testing;

/**
 * Created by IntelliJ IDEA.
 * User: M Viswanathan
 * Date: 12/20/13
 * Time: 8:51 PM
 * To change this template use File | Settings | File Templates.
 */
import com.sun.j3d.utils.universe.SimpleUniverse;

import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.ViewingPlatform;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

public class Hello3d {

public Hello3d()

{

    SimpleUniverse universe = new SimpleUniverse();

    BranchGroup group = new BranchGroup();
    TransformGroup tg = new TransformGroup();
    group.addChild(tg);
    tg.addChild(new ColorCube(0.3));
    ViewingPlatform vpf = universe.getViewingPlatform();
    vpf.setNominalViewingTransform();
    Transform3D tr = new Transform3D();
    tr.setTranslation(new Vector3d(0, 0, 2.5));
    vpf.getViewPlatformTransform().setTransform(tr);
//    Transform3D tr = new Transform3D();
    vpf.getViewPlatformTransform().getTransform(tr);
    tr.transform(new Vector3d(0, 0, 10));

    Transform3D trG = new Transform3D();
    trG.setTranslation(new Vector3d(0, 0, -2));

//    tg.setTransform(trG);

   universe.addBranchGraph(group);


}

    public static void main( String[] args ) {

       new Hello3d();

    }

} // end of class Hello3d