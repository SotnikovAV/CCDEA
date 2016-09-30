package ru.rb.ccdea.search.filter;

import com.documentum.fc.common.DfUtil;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;

/**
 * Created by ER21595 on 04.06.2015.
 */
public class CurrencyFilterProcessor extends TextFilterProcessor {

    public String getCondition(Component component) {
        String result ="";
        Text textfield = (Text) component.getControl(controlName);
        if(textfield!=null) {
            String value = textfield.getValue();
            if (value != null && value.length() > 0) {
                result = field + " IN ( SELECT s_code FROM ccdea_currency WHERE upper(s_code) like upper(" + DfUtil.toQuotedString(value + '%') + ") or upper(s_name) LIKE upper(" + DfUtil.toQuotedString(value + '%') + "))";
            }
        }
        return result;
    }

}
