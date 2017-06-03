package GeneralElements.Display.controlPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by mviswanathan on 31-05-2017.
 */
public class Indicator {
    Color onColor;
    Color offColor;
    int size;
    JButton bulb;

    public Indicator (int size, Color onColor, Color offColor) {
        this.size = size;
        this.offColor = offColor;
        this.onColor = onColor;
        bulb = new JButton(" ");
        bulb.setPreferredSize(new Dimension(size, size));
        bulb.setSelected(false);
        bulb.setEnabled(false);
    }

    public void switchIt(boolean on) {
        bulb.setBackground((on) ? onColor : offColor);
    }

    public Component displayUnit() {
        return bulb;
    }
}
