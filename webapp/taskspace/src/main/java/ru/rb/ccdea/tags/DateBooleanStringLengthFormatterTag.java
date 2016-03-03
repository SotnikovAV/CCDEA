package ru.rb.ccdea.tags;

import com.documentum.web.form.control.format.StringLengthFormatterTag;
import ru.rb.ccdea.control.DateBooleanStringLengthFormatter;

/**
 * Created by ER21595 on 17.07.2015.
 */
public class DateBooleanStringLengthFormatterTag extends StringLengthFormatterTag {
    @Override
    protected Class getControlClass() {
        return DateBooleanStringLengthFormatter.class;
    }
}
