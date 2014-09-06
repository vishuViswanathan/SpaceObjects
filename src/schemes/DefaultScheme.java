package schemes;

import Applications.ItemMovementsApp;
import GeneralElements.ItemSpace;

import javax.swing.*;

/**
 * Created by M Viswanathan on 29 Aug 2014
 */
public interface DefaultScheme {

    public boolean getScheme(JFrame parent, ItemSpace space);

    public double startJDN();
}
