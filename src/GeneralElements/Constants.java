package GeneralElements;

/**
 * Created by IntelliJ IDEA.
 * User: M Viswanathan
 * Date: 11/28/13
 * Time: 3:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class Constants {
    public static final double G = 6.67259E-11 ;  // in m3/s2/kg    as per jpl planetary satellites page
//            6.674281E-11 as calculated fro GM and mass og Outer planets data from jpl
//            6.671281903963040991511534289 x 10-11 as per http://www.jlmlasheras.com/grav_en.htm
//              6.6917625079 e-11 as per http://www.gsjournal.net/old/ntham/aikman1.pdf
//              as pr http://www.blazelabs.com/f-u-massvariation.asp mass and G vary with velocity
    public static double oneAuInkm  = 149597870.700;
    public static double secsPerDay = 86400.0;
    public static double c = 2.99792458E8; // speed of light in vacuum in m/s
    public static double cSquared = c * c;
}
