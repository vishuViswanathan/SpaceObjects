package GeneralElements.Display;

import GeneralElements.Item;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import java.awt.*;

/**
 * Created by M Viswanathan on 23 May 2014
 */
public class ItemSphere extends Sphere implements AttributeSetter {
    public Item planet;
    Appearance ap;
    public ItemSphere(Item object) {
        super(object.getDiaFloat() / 2,
                (Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS), 48);
        this.planet = object;
        ap = new Appearance();
        if (planet.imageName != null) {
            TextureLoader loader = new TextureLoader("images/" + planet.imageName,
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
        Color3f emissC = new Color3f(planet.color); //0.5f, 0.5f, 0.5f);
//            Color3f diffuse = new Color3f(1.0f, 1.0f, 1.0f);
        Color3f diffuse = new Color3f(planet.color);
//            Color3f ambient = new Color3f(1.0f, 1.0f, 1.0f);
        Color3f ambient = new Color3f(0.001f, 0.00f, 0.00f);
        Color3f specular = new Color3f(0.9f, 0.9f, 0.9f);

        Material blueMat = new Material(ambient, emissC, diffuse, specular, 1f);
        // sets ambient, emissive, diffuse, specular, shininess
        blueMat.setLightingEnable(true);

        //           Appearance blueApp = new Appearance();
        ap.setMaterial(blueMat);
        setAppearance(ap);
        setPickable(true);
    }

    public Item getItem() {
        return planet;
    }

    public void setRenderingAttribute(RenderingAttributes renderingAttribute) {
        ap.setRenderingAttributes(renderingAttribute);
    }

}
