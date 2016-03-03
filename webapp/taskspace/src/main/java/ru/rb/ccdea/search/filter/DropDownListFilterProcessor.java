package ru.rb.ccdea.search.filter;

import com.documentum.web.form.control.DropDownList;
import com.documentum.web.formext.component.Component;

/**
 * Created by ER21595 on 04.06.2015.
 */
public class DropDownListFilterProcessor extends ListFilterProcessor {

    @Override
    public String getCondition(Component component) {
        DropDownList list = (DropDownList) component.getControl(controlName);
        if(list!=null) {
            String value = list.getValue();
            if (value != null && value.length() > 0) {
                return field + " = " + quoteValue(value);
            } else {
                return "";
            }
        }
        return "";
    }

    @Override
    public void clearFilter(Component component) {
        DropDownList list = (DropDownList) component.getControl(controlName);
        if(list!=null) {
            list.setValue(null);
        }
    }
    
    @Override
	public boolean isEmpty(Component component) {
		boolean result = true;
		DropDownList list = (DropDownList) component.getControl(controlName);
		if (list != null) {
			String value = list.getValue();
            result = value == null || value.trim().length() == 0;
		}
		return result;
	}
}
