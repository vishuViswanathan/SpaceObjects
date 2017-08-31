package GeneralElements.Display;

import GeneralElements.Item;
import GeneralElements.ItemInterface;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.loaders.vrml97.VrmlLoader;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by M Viswanathan on 12 Mar 2017
 */
public class ItemVRML extends BranchGroup implements AttributeSetter {
    ItemInterface theItem;
    public ItemVRML(ItemInterface theItem) {
        this.theItem = theItem;
        loadVrmlFile(theItem.getVrmlFile());
    }

    private void loadVrmlFile(String fileName) {
        Scene scene = null;
        if (fileName.endsWith("obj")) {
            ObjectFile loader = new ObjectFile();
            try {
                // load the scene
                scene = loader.load(fileName);
            } catch (Exception e) {
                System.out.println("Exception loading OBJ from path:" + e);
                e.printStackTrace();
            }
        }
        else {
            VrmlLoader loader = new VrmlLoader();

//        try {
//            URL loadUrl = new URL("file:/" + location);
//            try {
//                // load the scene
//                scene = loader.load(loadUrl);
//            } catch (Exception e) {
//                System.out.println("Exception loading URL:" + e);
//                e.printStackTrace();
//            }
//        } catch (MalformedURLException badUrl) {
//            // location may be a path name
            try {
                // load the scene
                scene = loader.load(fileName);
            } catch (Exception e) {
                System.out.println("Exception loading file from path:" + e);
                e.printStackTrace();
            }
//        }
        }

        if (scene != null) {
            // get the scene group
            BranchGroup sceneGrp = scene.getSceneGroup();
            addChild(sceneGrp);

            setCapability(BranchGroup.ALLOW_BOUNDS_READ);
            setCapability(BranchGroup.ALLOW_CHILDREN_READ);
            setCapability(BranchGroup.ENABLE_PICK_REPORTING);
        }

        Bounds lightBounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

        AmbientLight ambLight = new AmbientLight(true, new Color3f(1.0f, 1.0f, 1.0f));
        ambLight.setInfluencingBounds(lightBounds);
        addChild(ambLight);

//        DirectionalLight headLight = new DirectionalLight();
//        headLight.setInfluencingBounds(lightBounds);
//        addChild(headLight);
     }


    @Override
    public ItemInterface getItem() {
        return theItem;
    }

    @Override
    public void setRenderingAttribute(RenderingAttributes renderingAttribute) {

    }
}
