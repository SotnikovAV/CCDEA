/**
 * 
 */
package ru.rb.ccdea.tags;

import com.documentum.web.form.control.format.ValueFormatterTag;

import ru.rb.ccdea.control.DateValueFormatter;

import com.documentum.web.form.Control;

public class DateValueFormatterTag extends ValueFormatterTag {

    private String pattern = null;

    public void release() {
        super.release();
        pattern = null;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    protected void setControlProperties(Control control) {
        super.setControlProperties(control);
        ((DateValueFormatter) control).setPattern(pattern);
    }

    protected Class<? extends Control> getControlClass() {
        return DateValueFormatter.class;
    }
}
