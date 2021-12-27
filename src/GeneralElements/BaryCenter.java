package GeneralElements;

import javax.vecmath.Vector3d;
import java.awt.*;

public class BaryCenter extends Item{

    public BaryCenter(Window parent) {
        super("Baryceneter", 0, 0.0001, Color.WHITE, parent);
        itemType = ItemType.BARY;
        bFixedLocation = true;
    }

    public void setPosition(Vector3d pos) {
        status.pos.set(pos);
    }
}
