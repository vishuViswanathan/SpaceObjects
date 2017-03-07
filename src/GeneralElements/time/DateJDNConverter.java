package GeneralElements.time;

import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.display.NumberTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

/**
 * Created by M Viswanathan on 27 Jan 2014
 */
public class DateJDNConverter extends JDialog {
    double  year = 1969, month = 5, day = 28;
    double hour = 0, min = 0, sec = 0;
    double jdn;
    NumberTextField ntYear, ntMonth, ntDay;
    NumberTextField ntHour, ntMin, ntSec;
    NumberTextField ntJDN;
    JButton dateToJDN, jdnToDate, quit;
    InputControl control;
    public DateJDNConverter(InputControl control) {
        this.control = control;
        dbInit();
    }

    void dbInit() {
        Container dlgP = getContentPane();
        MultiPairColPanel jp = new MultiPairColPanel("Date JDN Converter");
        ntYear = new NumberTextField(control, year, 6, true, 1900, 2099, "####", "Year");
        ntMonth = new NumberTextField(control, month + 1, 6, true, 1, 12, "####", "Month");
        ntDay = new NumberTextField(control, day, 6, true, 1, 31, "####", "Day");
        ntHour = new NumberTextField(control, hour, 6, true, 0, 23, "####", "Hour");
        ntMin = new NumberTextField(control, min, 6, true, 0, 59, "####", "Minutes");
        ntSec = new NumberTextField(control, sec, 6, true, 0, 59, "####", "Seconds");
        ntJDN = new NumberTextField(control, jdn, 8, true, 2e6, 3e6, "########.#####", "JDN");

        dateToJDN = new JButton("Get JDN ");
        jdnToDate = new JButton("Get Date");
        quit = new JButton("Quit");

        ButtonListener li = new ButtonListener();
        dateToJDN.addActionListener(li);
        jdnToDate.addActionListener(li);
        quit.addActionListener(li);
        jp.addItemPair(ntYear);
        jp.addItemPair(ntMonth);
        jp.addItemPair(ntDay);
        jp.addItemPair(ntHour);
        jp.addItemPair(ntMin);
        jp.addItemPair(ntSec);
        jp.addBlank();
        jp.addItemPair(dateToJDN, jdnToDate);
        jp.addBlank();
        jp.addItemPair(ntJDN);

        jp.addBlank();
        jp.addBlank();
        jp.addItemPair(" ", quit);
        dlgP.add(jp);
        pack();
    }

    void dataFromUI() {
        year = ntYear.getData();
        month = ntMonth.getData() - 1;
        day = ntDay.getData();
        hour = ntHour.getData();
        min = ntMin.getData();
        sec = ntSec.getData();
        jdn = ntJDN.getData();
    }

    void setUIData() {
        ntYear.setData(year);
        ntMonth.setData(month + 1);
        ntDay.setData(day);
        ntHour.setData(hour);
        ntMin.setData(min);
        ntSec.setData(sec);
        ntJDN.setData(jdn);
    }

    void getJDN() {
        dataFromUI();
        jdn = DateAndJDN.jdnFromDate((int)year, (int)month + 1, (int)day, (int)hour, (int)min, (int)sec);
        setUIData();
    }

    void getDate() {
        dataFromUI();
        DateAndJDN dateJDN = new DateAndJDN(jdn);
        year = dateJDN.get(Calendar.YEAR);
        month = dateJDN.get(Calendar.MONTH);
        day = dateJDN.get(Calendar.DAY_OF_MONTH);
        hour = dateJDN.get(Calendar.HOUR_OF_DAY);
        min = dateJDN.get(Calendar.MINUTE);
        sec = dateJDN.get(Calendar.SECOND);
        setUIData();
    }

    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == quit) {
                setVisible(false);
                dispose();
            }
            if (src == dateToJDN)
                getJDN();
            if (src == jdnToDate)
                getDate();
        }
    }
    public static void main(String[] args) {
        final DateJDNConverter conv = new DateJDNConverter(null);
        conv.setVisible(true);
    }
}
