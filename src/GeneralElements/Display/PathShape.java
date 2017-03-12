package GeneralElements.Display;

import javax.media.j3d.Appearance;
import javax.media.j3d.PointArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;

/**
 * Created by M Viswanathan on 23 May 2014
 */
public class PathShape  extends Shape3D {
    public ItemDisplay planet;
    public PathShape(ItemDisplay planet, PointArray ptArr, RenderingAttributes orbitAttrib) {
        super(ptArr);
        Appearance app = new Appearance();
        app.setRenderingAttributes(orbitAttrib);
        setAppearance(app);
        this.planet = planet;
    }

}
