package GeneralElements.Display;

import GeneralElements.DarkMatter;
import GeneralElements.Item;
import GeneralElements.ItemInterface;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import java.awt.*;

import static javax.media.j3d.Material.ALLOW_COMPONENT_WRITE;

/**
 * Created by M Viswanathan on 23 May 2014
 */
public class ItemSphere extends Sphere implements AttributeSetter {
    public ItemInterface planet;
    Material blueMat;
    Appearance ap;
    public boolean valid = true;
    public ItemSphere(ItemInterface object) {
        super(((DarkMatter)object).getDiaFloat() / 2,
                (Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS), 48);
        this.planet = object;
        ap = new Appearance();
        try {
            if (planet.getImageName().length() > 3) {
                TextureLoader loader = new TextureLoader("images/" + planet.getImageName(),
                        "LUMINANCE", new Container());
                Texture texture = loader.getTexture();
                texture.setBoundaryModeS(Texture.WRAP);
                texture.setBoundaryModeT(Texture.WRAP);
                texture.setBoundaryColor(new Color4f(0.0f, 1.0f, 0.0f, 0.0f));

                TextureAttributes texAttr = new TextureAttributes();
                texAttr.setTextureMode(TextureAttributes.MODULATE);
    //            Appearance ap = new Appearance();
                ap.setTexture(texture);
                ap.setTextureAttributes(texAttr);
            }

//            Color3f emissC = new Color3f(planet.color);
//            Color3f emissC = new Color3f(((DarkMatter)planet).color); //0.5f, 0.5f, 0.5f);
            Color3f emissC = new Color3f(0.4f, 0.4f, 0.4f);
//            Color3f diffuse = new Color3f(1.0f, 1.0f, 1.0f);
            Color3f diffuse = new Color3f(((DarkMatter)planet).color);
//            Color3f ambient = new Color3f(1.0f, 1.0f, 1.0f);
            Color3f ambient = new Color3f(0.2f, 0.2f, 0.2f);
            Color3f specular = new Color3f(0.9f, 0.9f, 0.9f);

            blueMat = new Material(ambient, emissC, diffuse, specular, 0.5f);
            // sets ambient, emissive, diffuse, specular, shininess
            blueMat.setLightingEnable(true);
            // if not set emissC high
            //           Appearance blueApp = new Appearance();
            blueMat.setCapability(ALLOW_COMPONENT_WRITE);
            ap.setMaterial(blueMat);
            setAppearance(ap);
            setPickable(true);
        } catch (Exception e) {
            valid = false;
        }
    }

    public void setEnableLight(boolean ena) {
        blueMat.setLightingEnable(ena);
        if (ena)
            blueMat.setEmissiveColor(new Color3f(0.4f, 0.4f, 0.4f));
        else
            blueMat.setEmissiveColor(new Color3f(1.0f, 1.0f, 1.0f));
    }

    public ItemInterface getItem() {
        return planet;
    }

    public void setRenderingAttribute(RenderingAttributes renderingAttribute) {
        ap.setRenderingAttributes(renderingAttribute);
    }

    public void updateColor() {
        Color3f c = new Color3f(((DarkMatter)planet).color);
        Material mat = ap.getMaterial();
        mat.setEmissiveColor(c);
        mat.setDiffuseColor(c);
    }

}
