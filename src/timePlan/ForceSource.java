package timePlan;

import javax.swing.*;

/**
 * Created by M Viswanathan on 29 Mar 2015
 */
public interface ForceSource {
    public boolean anyMassChange();
    public double effectiveForce();
    public double massChange(double duration);
    public boolean anyDetails();
    public JPanel fsDetails();
    public boolean fsTakeDataFromUI();
    public String dataAsString();
}
