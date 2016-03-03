package ru.rb.ccdea.search.filter;

import com.documentum.web.formext.component.Component;
import ru.rb.ccdea.control.ClientListBox;

/**
 * Created by ER21595 on 04.06.2015.
 */
public class ClientFilterProcessor extends ListFilterProcessor {
    private final String clientNumber ="s_customer_number";
    private final String clientName = "s_customer_name";

    @Override
    public void setField(String field) {
    }

    @Override
    public String getCondition(Component component) {
        ClientListBox list = (ClientListBox) component.getControl(controlName);
        if(list.getSelectedValues().size()==0) {
            StringBuilder result = new StringBuilder();
            String value =list.getNumberSearchString();
            if(value !=null &&value.length()>0){
                result.append(clientNumber).append(" like '").append(value).append("%'");
            }
            value =list.getSearchString();
            if(value !=null &&value.length()>0){
                if( result.length()>0){
                    result.append(" AND ");
                }
                result.append(clientName).append(" like '").append(value).append("%'");
            }
            return result.toString();
        }else{
            return super.getCondition(component);

        }
    }

    @Override
    public void clearFilter(Component component) {
        super.clearFilter(component);
        ClientListBox list = (ClientListBox) component.getControl(controlName);
        list.setnumberSearchString("");
    }
}
