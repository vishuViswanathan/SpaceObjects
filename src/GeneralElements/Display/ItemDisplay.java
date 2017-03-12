package GeneralElements.Display;

import GeneralElements.Item;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.vrml97.VrmlLoader;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by M Viswanathan on 09 Mar 2017
 */
public class ItemDisplay extends Group {
    Item theItem;
    AttributeSetter attributeSetter;

    public ItemDisplay(Item theItem) {
        switch (theItem.itemType) {
            case SPHERE:
                ItemSphere theDisplay = new ItemSphere(theItem);
                addChild(theDisplay);
                attributeSetter = theDisplay;
                break;
            case VMRL:
//                BranchGroup grp = loadVrmlFile(theItem.vrmlFile);
                BranchGroup grp = new ItemVRML(theItem);
                addChild(grp);
                break;
        }
    }

    public Item getItem() {
        return theItem;
    }

    public void setRenderingAttribute(RenderingAttributes renderingAttribute) {
        if (attributeSetter != null)
            attributeSetter.setRenderingAttribute(renderingAttribute);
    }

    private BranchGroup loadVrmlFile(String location) {
        BranchGroup sceneGroup = null;
        Scene scene = null;

        VrmlLoader loader = new VrmlLoader();

        try {
            URL loadUrl = new URL("file:/" + location);
            try {
                // load the scene
                scene = loader.load(loadUrl);
            } catch (Exception e) {
                System.out.println("Exception loading URL:" + e);
                e.printStackTrace();
            }
        } catch (MalformedURLException badUrl) {
            // location may be a path name
            try {
                // load the scene
                scene = loader.load(location);
            } catch (Exception e) {
                System.out.println("Exception loading file from path:" + e);
                e.printStackTrace();
            }
        }

        if (scene != null) {
            // get the scene group
            sceneGroup = scene.getSceneGroup();

            sceneGroup.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
            sceneGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        }

        Bounds lightBounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

        AmbientLight ambLight = new AmbientLight(true, new Color3f(1.0f, 1.0f, 1.0f));
        ambLight.setInfluencingBounds(lightBounds);
        sceneGroup.addChild(ambLight);

        DirectionalLight headLight = new DirectionalLight();
        headLight.setInfluencingBounds(lightBounds);
        sceneGroup.addChild(headLight);

        return sceneGroup;
    }
}
