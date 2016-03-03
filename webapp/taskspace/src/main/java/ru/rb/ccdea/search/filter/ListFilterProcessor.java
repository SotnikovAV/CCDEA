package ru.rb.ccdea.search.filter;

import com.documentum.web.formext.component.Component;
import ru.rb.ccdea.control.ListBoxFilter;
import ru.rb.ccdea.utils.StringUtils;

/**
 * Created by ER21595 on 04.06.2015.
 */
public class ListFilterProcessor extends BasicFilterProcessor {
    public boolean validate(Component component, StringBuilder errorMessage) {
        return true;
    }

    protected boolean stringField =true;

    @Override
    public void setField(String field) {
        super.setField(field);
        stringField = field.startsWith("s_");
    }

    protected String quoteValue(String value){
        if(stringField){
            return "'"+value+"'";
        }else{
            return value;
        }
    }

    public String getCondition(Component component) {
        StringBuilder result = new StringBuilder();
        ListBoxFilter list = (ListBoxFilter) component.getControl(controlName);
        if(list!=null) {
            int valueCount = list.getSelectedValues().size();
            if (valueCount > 0) {
                if (valueCount == 1) {
                    result.append(field).append(" = ").append(quoteValue(list.getSelectedValues().get(0)));
                } else {
                    result.append(field).append(" in(");
                    result.append(StringUtils.joinStrings(list.getSelectedValues(), ",",
                            new StringUtils.IWorker<String>() {
                                public String doWork(String param) {
                                    return quoteValue(param);
                                }
                            }));
                    result.append(")");
                }
            }
        }
        return result.toString();
    }

    @Override
    public void clearFilter(Component component) {
        ListBoxFilter list = (ListBoxFilter) component.getControl(controlName);
        if(list!=null) {
            list.getSelectedValues().clear();
        }
    }

	@Override
	public boolean isEmpty(Component component) {
		boolean result = true;
		ListBoxFilter list = (ListBoxFilter) component.getControl(controlName);
		if (list != null) {
			result = list.getSelectedValues().size() == 0;
		}
		return result;
	}
}
