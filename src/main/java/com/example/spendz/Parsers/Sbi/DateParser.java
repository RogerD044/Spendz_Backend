package com.example.spendz.Parsers.Sbi;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class DateParser {
    private static final String REGEX = " ";

    public Date parseDate(String dateTxt) {
        String[] dateArr = dateTxt.split(REGEX);

        int day = Integer.parseInt(dateArr[0]);
        int month = convertMonth(dateArr[1]);
        int year = Integer.parseInt(dateArr[2]);

        Date date = new Date();
        // Dummy date of 1 is given, so that setting month doesn't give an error. Ex - With date as 31, setting month as Nov can throw exception
        date = DateUtils.setDays(date, 1);

        date = DateUtils.setYears(date, year);
        date = DateUtils.setMonths(date, month - 1);
        date = DateUtils.setDays(date, day);
        date = DateUtils.setHours(date, 0);
        date = DateUtils.setMinutes(date, 0);
        date = DateUtils.setSeconds(date, 0);
        date = DateUtils.setMilliseconds(date, 0);

        return date;
    }

    public int convertMonth(String monthStr) {
        int month = 0;
        switch (monthStr) {
            case "Jan":
                month = 1;
                break;
            case "Feb":
                month = 2;
                break;
            case "Mar":
                month = 3;
                break;
            case "Apr":
                month = 4;
                break;
            case "May":
                month = 5;
                break;
            case "Jun":
                month = 6;
                break;
            case "Jul":
                month = 7;
                break;
            case "Aug":
                month = 8;
                break;
            case "Sep":
                month = 9;
                break;
            case "Oct":
                month = 10;
                break;
            case "Nov":
                month = 11;
                break;
            case "Dec":
                month = 12;
                break;
            default:
                break;
        }
        return month;
    }
}
