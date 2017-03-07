package time;

import mvUtils.display.NumberLabel;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by M Viswanathan on 1/26/14.
 */

/* Julian date Number to date time
*/

public class DateAndJDN extends GregorianCalendar{
    double jdN;

    public DateAndJDN() {
        super();
        jdN = jdnFromDate(this);
    }

    public DateAndJDN(GregorianCalendar fromDate) {
        super(TimeZone.getTimeZone("GMT"));
        int year = fromDate.get(Calendar.YEAR);
        int month = fromDate.get(Calendar.MONTH);
        int day = fromDate.get(Calendar.DAY_OF_MONTH);
        int hour = fromDate.get(Calendar.HOUR_OF_DAY);
        int min = fromDate.get(Calendar.MINUTE);
        int sec = fromDate.get(Calendar.SECOND);
        set(year, month, day, hour, min, sec);
    }

    public DateAndJDN(double jdn) {
        super(TimeZone.getTimeZone("GMT"));
        this.jdN = jdn;
        setCalendarFromJDn(jdn);
    }

    public double getJdN() {
        jdN = jdnFromDate(this);
        return jdN;
    }

    void setCalendarFromJDn(double jdn) {
        double fraction = jdn - (int)jdn;
        double timePart;
        int dayPart;
        if (fraction >= 0.5) {
            timePart = fraction - 0.5;
            dayPart = (int)(jdn - fraction + 1);
        }
        else {
            timePart = fraction + 0.5;
            dayPart = (int)(jdn - fraction);
        }
        int y = 4716;
        int j = 1401;
        int m = 2;
        int n = 12;
        int r = 4;
        int p = 1461;
        int v = 3;
        int u = 5;
        int s = 153;
        int w = 2;
        int B = 274277;
        int C = -38;

        int f = dayPart + j +(((4 * dayPart + B) / 146097) * 3) / 4 + C;
        int e = r * f + v ;
        int g = (e % p) / r;
        int h = u * g + w;
        int day = (h % s) / u + 1;
        int month = ((h / s + m) % n) + 1;
        int year = e / p - y + (n + m - month) / n;
        int totSec = (int)(timePart * 24 * 3600);
        int hour = totSec / 3600;
        int min = (totSec % 3600) / 60;
        int sec = totSec - hour * 3600 - min * 60;
        set(year, month - 1, day, hour, min, sec);
    }

    public static double jdnFromDate(GregorianCalendar theDate) {
        int year = theDate.get(Calendar.YEAR);
        int month = theDate.get(Calendar.MONTH);
        int day = theDate.get(Calendar.DAY_OF_MONTH);
        int hour = theDate.get(Calendar.HOUR_OF_DAY);
        int min = theDate.get(Calendar.MINUTE);
        int sec = theDate.get(Calendar.SECOND);
        return jdnFromDate(year, month + 1, day, hour, min, sec);
    }

    public static double jdnFromDate(int year, int month, int day) {
        return jdnFromDate(year, month, day, 0, 0, 0);
    }

    public static double jdnFromDate(int year, int month, int day, int hour, int min, int sec) {
        int a = (14 - month) / 12;
        int y = year + 4800 - a;
        int m = month + 12 * a - 3;
        int jdBase = day + ((m * 153 + 2) / 5) + 365 * y + y / 4 - y/ 100 + y / 400 - 32045;
        double jdTime  = ((double)hour - 12.0) / 24 + (double)min / (24 * 60) + (double)sec / (24 * 60 * 60);
        double jdN = jdTime + jdBase;
        return jdN;
    }

    public JPanel panWithJDN() {
        JPanel jp = new JPanel(new BorderLayout());
        jp.add(new JLabel("JDN "), BorderLayout.WEST);
        jp.add(new NumberLabel(jdN, 100, "#######.#####", true), BorderLayout.EAST);
        return jp;
    }

    public JPanel panWithDateTime() {
        JPanel jp = new JPanel(new BorderLayout());
        jp.add(new JLabel("Date "), BorderLayout.WEST);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
        jp.add(new JLabel(sdf.format(getTime())), BorderLayout.EAST);
        return jp;
    }
}
