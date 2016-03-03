package ru.rb.ccdea.search.filter;

import com.documentum.web.formext.component.Component;
import ru.rb.ccdea.control.SearchListBox;

/**
 * Created by ER21595 on 04.06.2015.
 */
public class SearchListFilterProcessor extends ListFilterProcessor {
    @Override
    public String getCondition(Component component) {
        String result = "";
        SearchListBox list = (SearchListBox) component.getControl(controlName);
        if(list!=null) {
            if (list.getSelectedValues().size() == 0) {
                String value = list.getSearchString();
                if (value != null && value.length() > 0) {
                    result = field + " like '" + value + "%'";
                }
            } else {
                result = super.getCondition(component);

            }
        }
        return result;
    }

    @Override
    public void clearFilter(Component component) {
        super.clearFilter(component);
        SearchListBox list = (SearchListBox) component.getControl(controlName);
        list.setSearchString("");
    }
}
