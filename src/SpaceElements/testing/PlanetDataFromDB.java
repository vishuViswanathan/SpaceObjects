package SpaceElements.testing;
import java.sql.*;
/**
 * Created by M Viswanathan on 15 Apr 2014
 */
public class PlanetDataFromDB {
    public static void main(String[] args) {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Connection conn = DriverManager.getConnection("jdbc:odbc:ODBCtoPlanetData");
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery("SELECT ObjName, Radius, Mass, DistanceFromPrimary FROM PhycialData WHERE (NOT isMoon)");
            while (res.next()) {
                System.out.println((res.getString(1) + ", " + res.getString(2) + ", "+ res.getString(3) + ", " + res.getString(4)));
            }


        }
        catch (Exception e) {

        }
    }
}
