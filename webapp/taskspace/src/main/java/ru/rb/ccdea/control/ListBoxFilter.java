package ru.rb.ccdea.control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.documentum.web.form.control.ListBox;
import com.documentum.web.form.control.Option;

/**
 * Контрол для фильтра "Список"
 * Created by ER21595 on 28.05.2015.
 */
public class ListBoxFilter extends ListBox {
    public static final String ALL_VALUES="__all__";
    public static final String NO_VALUES="__none__";
    private List<String> selectedValues = new ArrayList<String>();
    private String rawValue="";

    public String getRawValue() {
        return rawValue;
    }


    public List<String> getSelectedValues() {
        return selectedValues;
    }


    @Override
    public void updateStateFromRequest() {
        super.updateStateFromRequest();
        String strValue = this.getRequestParameter(this.getNonRenderElementName()+"_value");
        if(strValue!=null&&strValue.length()>1) {
            if (NO_VALUES.equals(strValue)) {
                selectedValues = new ArrayList<String>();
                rawValue = "";
            } else if (ALL_VALUES.equals(strValue)) {
                selectedValues = new ArrayList<String>();
                rawValue = "";
                Iterator oit = getOptions().iterator();
                while (oit.hasNext()) {
                    Option o = (Option) oit.next();
                    selectedValues.add(o.getValue());
                    rawValue += "," + o.getValue();
                }
            } else if (strValue.startsWith(",")) {
                selectedValues = new ArrayList<String>();
                for (String selectedValue : strValue.substring(1).split(",")) {
                    selectedValues.add(selectedValue);
                }
                rawValue = strValue;
            }
        }
    }
    
	public void clear() {
    	selectedValues = new ArrayList<String>();
        rawValue = "";
    }	
	
}
