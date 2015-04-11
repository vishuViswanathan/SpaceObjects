package timePlan;

import javax.swing.*;

/**
 * Created by M Viswanathan on 29 Mar 2015
 */
public interface ForceSource {
    public boolean isValid();
    public double effectiveForce();
    public double massChange(double duration);
    public JPanel fsDetails();
    public boolean fsTakeDataFromUI();
    public String dataAsString();
    public ForceSource clone();

    public StringBuilder dataInXML();
    public boolean takeFromXML(String xmlStr) throws NumberFormatException;
}
