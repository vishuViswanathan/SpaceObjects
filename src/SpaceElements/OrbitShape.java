package SpaceElements;

import javax.media.j3d.Appearance;
import javax.media.j3d.PointArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;

/**
 * Created by M Viswanathan on 04 Feb 2014
 */
public class OrbitShape extends Shape3D {
    public Planet planet;
    public OrbitShape(Planet planet, PointArray ptArr, RenderingAttributes orbitAttrib) {
        super(ptArr);
        Appearance app = new Appearance();
        app.setRenderingAttributes(orbitAttrib);
        setAppearance(app);
        this.planet = planet;
    }

 }
