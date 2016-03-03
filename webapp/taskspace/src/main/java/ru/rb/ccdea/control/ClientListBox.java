package ru.rb.ccdea.control;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.IReturnListener;
import com.documentum.web.form.control.Option;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.control.action.ActionButton;

import java.util.Map;

/**
 * Контрол для фильтра "Клиент"
 *
 * Created by ER21595 on 27.05.2015.
 */
public class ClientListBox extends SearchListBox {
    private String numberField;
    private String numberSearchString="";

    public String getNumberSearchString() {
        return numberSearchString;
    }

    public void setnumberSearchString(String numberSearchString) {
        this.numberSearchString = numberSearchString;
    }

    public void setNumberField(String numberField) { this.numberField = numberField; }

    @Override
    public void searchDictionaryForOptions(SearchListBox box, ArgumentList arg) {
        boolean searchByNumber =arg.get("searchStr")==null||arg.get("searchStr").length()==0;
        String conditionStr = arg.get("searchStr");
        if(searchByNumber){
            conditionStr = arg.get("numberSearchStr");
        }
        if(conditionStr.length()>=3) {
            StringBuilder strQuery = new StringBuilder("select ");
            strQuery.append(numberField).append(" as num, ");
            strQuery.append(labelField).append(" as lbl, ");
            strQuery.append(valueField).append(" as val from ");
            strQuery.append(fromPart).append(" where ");
            if (wherePart!=null && wherePart.length()>0) {
                strQuery.append(wherePart).append(" and ");
            }
            strQuery.append("lower(");
            if(searchByNumber){
                strQuery.append(numberField).append(") like '");
            }else {
                strQuery.append(labelField).append(") like '");
            }
            strQuery.append(conditionStr.toLowerCase()).append("%'");
            getOptionsFromBase(strQuery.toString(), new OptionCreator() {
                public Option createOption(IDfCollection col) throws DfException {
                    Option o = new Option();
                    o.setLabel(col.getString("num")+":"+col.getString("lbl"));
                    o.setValue(col.getString("val"));
                    return o;
                }
            });
        }
    }

    @Override
    public void updateStateFromRequest() {
        super.updateStateFromRequest();
        String strValue = this.getRequestParameter(this.getNonRenderElementName() + "_number_search_box");
        if(strValue != null) {
            numberSearchString =strValue;
        }
        strValue = this.getRequestParameter(this.getNonRenderElementName()+"_name_search_box");
        if(strValue != null) {
            searchString =strValue;
        }
    }

    public void setSelectedCustomerNumbers(String selectedCustomerNumbers) {
        String dql = "select s_number, s_name from ccdea_customer where s_number in (" + selectedCustomerNumbers + ")";
        IDfQuery query = new DfQuery();
        query.setDQL(dql);
        IDfCollection col = null;
        try {
            IDfSession sess = ((Component) getForm().getTopForm()).getDfSession();
            col = query.execute(sess, IDfQuery.DF_READ_QUERY);
            setMutable(true);
            clearOptions();
            while (col.next()) {
                Option o = new Option();
                o.setLabel(col.getString("s_number")+"-"+col.getString("s_name"));
                o.setValue(col.getString("s_number"));
                addOption(o);
            }
            int size=getOptions().size();
            if (size<userSize){
                size=userSize;
            }
            setSize(Integer.toString(size));
        } catch (DfException e) {
            e.printStackTrace();
        } finally {
            try {
                if (col != null) {
                    col.close();
                }
            } catch (DfException e) {
                e.printStackTrace();
            }
        }

    }
}
