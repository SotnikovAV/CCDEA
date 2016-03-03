package ru.rb.ccdea.search.filter;

import com.documentum.web.form.control.Hidden;
import com.documentum.web.form.control.StringInputControl;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;

public class MultivalueFilterProcessor extends BasicFilterProcessor {
    @Override
    public String getCondition(Component component) {
        String result ="";
        Hidden valueControl = (Hidden)component.getControl(controlName + "_value");
        String multivalue = valueControl.getValue();
        if (multivalue != null && !multivalue.trim().isEmpty()) {
            result = (repeating ? " ANY " : "") + field + " IN (" + multivalue + ")";
        }
        else {
            Text textfield = (Text) component.getControl(controlName);
            if (textfield != null) {
                String value = textfield.getValue();
                if (value != null && value.length() > 0) {
                    result = (repeating ? " ANY " : "") + field + " LIKE '" + value + "%'";
                }
            }
        }
        return result;
    }

    @Override
    public boolean validate(Component component, StringBuilder errorMessage) {
        return true;
    }

    @Override
    public void clearFilter(Component component) {
        StringInputControl control  = (StringInputControl) component.getControl(controlName);
        if(control !=null) {
            control.setValue("");
        }
        Hidden valueControl = (Hidden)component.getControl(controlName + "_value");
        if (valueControl != null) {
            valueControl.setValue("");
        }
    }

	@Override
	public boolean isEmpty(Component component) {
		boolean result = true;
        Hidden valueControl = (Hidden)component.getControl(controlName + "_value");
        String multivalue = valueControl.getValue();
        if (multivalue != null && !multivalue.trim().isEmpty()) {
            result = false;
        } else {
            Text textfield = (Text) component.getControl(controlName);
            if (textfield != null) {
                String value = textfield.getValue();
                result = value == null || value.trim().length() == 0;
            }
        }
        return result;
	}
}
