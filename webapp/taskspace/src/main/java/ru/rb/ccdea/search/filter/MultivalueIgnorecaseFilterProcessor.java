package ru.rb.ccdea.search.filter;

import com.documentum.web.form.control.Hidden;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;

public class MultivalueIgnorecaseFilterProcessor extends MultivalueFilterProcessor {
    @Override
    public String getCondition(Component component) {
        String result = "";
        Hidden valueControl = (Hidden) component.getControl(controlName + "_value");
        String multivalue = valueControl.getValue();
        if (multivalue != null && !multivalue.trim().isEmpty()) {
            result = (repeating ? " ANY " : "") + field + " IN (" + multivalue + ")";
        } else {
            Text textfield = (Text) component.getControl(controlName);
            if (textfield != null) {
                String value = textfield.getValue();
                if (value != null && value.length() > 0) {
                    result = (repeating ? " ANY " : "") + "upper(" + field + ") LIKE upper('" + value + "%')";
                }
            }
        }
        return result;
    }
}
