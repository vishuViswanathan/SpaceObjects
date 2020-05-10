package GeneralElements.Display;

import GeneralElements.Item;
import GeneralElements.ItemInterface;
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
    ItemInterface theItem;
    AttributeSetter attributeSetter;
    boolean valid = false;
    ItemSphere theDisplay;
    ItemInterface.ItemType itemType;
    public ItemDisplay(ItemInterface theItem) {
        this.theItem = theItem;
        itemType = theItem.getItemType();
        switch (itemType) {
            case SPHERE:
                theDisplay = new ItemSphere(theItem);
                if (theDisplay.valid) {
                    addChild(theDisplay);
                    attributeSetter = theDisplay;
                    valid = true;
                }
                break;
            case VMRL:
//                BranchGroup grp = loadVrmlFile(theItem.vrmlFile);
                BranchGroup grp = new ItemVRML(theItem);
                addChild(grp);
                valid = true;
                break;
            case SURFACE:
                break;
        }
    }

    public ItemInterface getItem() {
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

    public void updateColor() {
        if (itemType == ItemInterface.ItemType.SPHERE)
            theDisplay.updateColor();
    }
}
