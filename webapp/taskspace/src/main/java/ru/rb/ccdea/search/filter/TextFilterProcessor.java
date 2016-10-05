package ru.rb.ccdea.search.filter;

import com.documentum.fc.common.DfUtil;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;

/**
 * Created by ER21595 on 04.06.2015.
 */
public class TextFilterProcessor extends BasicFilterProcessor {
    public boolean validate(Component component, StringBuilder errorMessage) {
        boolean result = true;
        Text textfield = (Text) component.getControl(controlName);
        if(textfield!=null) {
            String value = textfield.getValue();
            if (value != null && value.length() > 0 && value.length() < 3) {
                errorMessage.append("Слишком короткая строка поиска");
                result = false;
            }
        }
        return result;
    }

    public String getCondition(Component component) {
        String result ="";
        Text textfield = (Text) component.getControl(controlName);
        if(textfield!=null) {
            String value = textfield.getValue();
            if (value != null && value.length() > 0) {
                result = (repeating ? " ANY " : "") + "upper(" + field + ") LIKE upper(" + DfUtil.toQuotedString(value + '%') +")";
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
