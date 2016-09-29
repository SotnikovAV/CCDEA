package ru.rb.ccdea.dialog;

import java.util.ArrayList;
import java.util.List;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfUtil;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.ListBox;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;

public class SelectSingleValueComponent extends Component {

    public static String SELECTED_VALUE = "SelectSingleValueComponent.SELECTED_VALUE";

    private String valueType = null;

    @Override
    public void onInit(ArgumentList arg) {
        super.onInit(arg);
        valueType = arg.get("valueType");
        getValueFilterControl().setValue(arg.get("valueStart"));
        refreshFilteredValues();
    }

    public boolean isContractNumberValue() {
        return "contract_number".equalsIgnoreCase(valueType);
    }

    public boolean isPassportNumberValue() {
        return "passport_number".equalsIgnoreCase(valueType);
    }

    public boolean isCustomerNameValue() {
        return "customer_name".equalsIgnoreCase(valueType);
    }

    public boolean isCustomerNumberValue() {
        return "customer_number".equalsIgnoreCase(valueType);
    }

    public boolean isContractCurrencyValue() {
        return "contract_currency".equalsIgnoreCase(valueType);
    }

    public boolean isDocumentCurrencyValue() {
        return "document_currency".equalsIgnoreCase(valueType);
    }

    public boolean isDocumentNumberValue() {
        return "document_number".equalsIgnoreCase(valueType);
    }

    public ListBox getValueListControl() {
        return (ListBox)getControl("value_list", ListBox.class);
    }

    public Text getValueFilterControl() {
        return (Text)getControl("value_filter", Text.class);
    }

    private String getSelectValueListDql(String valueStart) {
        if (isContractCurrencyValue() || isDocumentCurrencyValue()) {
            return "select s_code as s_value, s_code as s_label from ccdea_currency where s_code like " + DfUtil.toQuotedString(valueStart + '%') ;
        }
        else if (isCustomerNumberValue()) {
            return "select s_number as s_value, s_number as s_label from ccdea_customer where s_number like " + DfUtil.toQuotedString(valueStart + '%') ;
        }
        else if (isCustomerNameValue()) {
            return "select s_name as s_value, s_name as s_label from ccdea_customer where s_name like " + DfUtil.toQuotedString(valueStart + '%') ;
        }
        else if (isDocumentNumberValue()) {
            return "select distinct s_doc_number as s_value, s_doc_number as s_label from ccdea_pd where upper(s_doc_number) like upper(" + DfUtil.toQuotedString(valueStart + '%') +") UNION ALL " +
                    "select distinct s_doc_number as s_value, s_doc_number as s_label from ccdea_spd where upper(s_doc_number) like upper(" + DfUtil.toQuotedString(valueStart + '%') +") UNION ALL " +
                    "select distinct s_doc_number as s_value, s_doc_number as s_label from ccdea_svo_detail where upper(s_doc_number) like upper(" + DfUtil.toQuotedString(valueStart + '%') +")";
        }
        else if (isContractNumberValue()) {
            return "select distinct s_contract_number as s_value, s_contract_number as s_label from ccdea_base_doc where upper(s_contract_number) like upper(" + DfUtil.toQuotedString(valueStart + '%') +")";
        }
        else if (isPassportNumberValue()) {
            return "select distinct s_passport_number as s_value, s_passport_number as s_label from ccdea_base_doc where s_passport_number like " + DfUtil.toQuotedString(valueStart + '%') ;
        }
        else {
            return null;
        }
    }

    public void refreshFilteredValues() {
        String valueStart = getValueFilterControl().getValue();
        IDfCollection col = null;
        try{
            IDfQuery query = new DfQuery(getSelectValueListDql(valueStart));
            col = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            ListBox valueListControl = getValueListControl();
            List<Option> itemList = new ArrayList<Option>();
            while(col.next()){
                Option item = new Option();
                item.setValue(col.getString("s_value"));
                item.setLabel(col.getString("s_label"));
                itemList.add(item);
            }
            valueListControl.setOptions(itemList);
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(), null, e);
        } finally {
            if(col!=null){
                try{
                    col.close();
                } catch (DfException e) {
                    DfLogger.error(this, e.getMessage(), null, e);
                }
            }
        }
    }

    public void onFilterClick(Control c, ArgumentList args) {
        refreshFilteredValues();
    }

    @Override
    public boolean onCommitChanges() {
        setReturnValue(SELECTED_VALUE, getValueListControl().getValue());
        return super.onCommitChanges();
    }
}
