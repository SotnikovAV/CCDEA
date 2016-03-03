/**
 * 
 */
package ru.rb.ccdea.search.filter;

import com.documentum.fc.common.DfUtil;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;

/**
 * @author sotnik
 *
 */
public class ClientNameFilterProcessor extends TextFilterProcessor {

	public String getCondition(Component component) {
        String result ="";
        Text textfield = (Text) component.getControl(controlName);
        if(textfield!=null) {
            String value = textfield.getValue();
            if (value != null && value.length() > 0) {
                result = (repeating ? " ANY " : "") + field + " IN ( SELECT s_number FROM ccdea_customer WHERE s_name LIKE " + DfUtil.toQuotedString(value + '%') + " )";
            }
        }
        return result;
    }
	
}
