package ru.rb.ccdea.search.filter;

import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;

/**
 * Created by ER21595 on 04.06.2015.
 */
public class NumberFilterProcessor extends BasicFilterProcessor {
    public boolean validate(Component component, StringBuilder errorMessage) {
        Text textfield = (Text) component.getControl(controlName);
        if(textfield!=null) {
            String value = textfield.getValue();
            if (value != null && value.length() > 0) {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    errorMessage.append("Введённое в поле значение не являетя числом.");
                    return false;
                }
            }
        }
        return true;
    }

    public String getCondition(Component component) {
        String result ="";
        Text textfield = (Text) component.getControl(controlName);
        if(textfield!=null) {
            String value = textfield.getValue();
            if (value != null && value.length() > 0) {
                result = field + " = " + value;
            }
        }
        return result;
    }

	@Override
	public boolean isEmpty(Component component) {
		boolean result = true;
        Text textfield = (Text) component.getControl(controlName);
        if(textfield!=null) {
            String value = textfield.getValue();
            result = value == null || value.trim().length() == 0;
        }
        return result;
	}
}
