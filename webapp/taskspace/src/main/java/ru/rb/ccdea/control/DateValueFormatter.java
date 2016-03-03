/**
 * 
 */
package ru.rb.ccdea.control;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.documentum.web.form.control.format.ValueFormatter;

public class DateValueFormatter extends ValueFormatter {
	
	private static final String dateformat = "dd.MM.yyyy";
	private static final String [] nulldates = {"02.01.1753", "01.02.1753"};
	static {
		Arrays.sort(nulldates);
	}

    private String pattern = null;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String format(String s) {
        if ((s != null) && (s.length() > 0)) {
            Date date = new Date(Long.parseLong(s));
            String dateStr = new SimpleDateFormat(dateformat).format(date);
            
            if(Arrays.binarySearch(nulldates, dateStr) > -1) {
            	return "&nbsp;";
            }
            if ((pattern != null) && (pattern.length() > 0)) {
                return new SimpleDateFormat(pattern).format(date);
            } else {
                return date.toString();
            }
        } else {
            return "&nbsp;";
        }
    }
}
