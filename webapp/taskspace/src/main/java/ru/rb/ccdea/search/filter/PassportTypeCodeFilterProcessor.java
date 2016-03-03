package ru.rb.ccdea.search.filter;

import com.documentum.web.form.control.Checkbox;
import com.documentum.web.formext.component.Component;

public class PassportTypeCodeFilterProcessor extends BasicFilterProcessor{
    @Override
    public String getCondition(Component component) {
        String result ="";
        for (int i = 0; i < 10; i++) {
            Checkbox control = (Checkbox) component.getControl(controlName + "_" + i);
            if(control != null && control.getValue()) {
                if (result.length() == 0) {
                    result = (repeating ? " ANY " : "") + field + " in ('";
                }
                else {
                    result += "','";
                }
                result += i;
            }
        }
        if (result.length() > 0) {
            result += "')";
        }
        return result;
    }

    @Override
    public boolean validate(Component component, StringBuilder errorMessage) {
        return true;
    }

    @Override
    public void clearFilter(Component component) {
        for (int i = 0; i < 10; i++) {
            Checkbox control = (Checkbox) component.getControl(controlName + "_" + i);
            if (control != null) {
                control.setValue(false);
            }
        }
    }

	@Override
	public boolean isEmpty(Component component) {
		boolean result = true;
		for (int i = 0; i < 10; i++) {
			Checkbox control = (Checkbox) component.getControl(controlName + "_" + i);
			if (control != null && control.getValue()) {
				result = false;
				break;
			}
		}
		return result;
	}
}
