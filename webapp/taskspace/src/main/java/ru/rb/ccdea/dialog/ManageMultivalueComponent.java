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
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.multiselector.MultiSelector;
import com.documentum.web.formext.component.Component;

public class ManageMultivalueComponent extends Component {

    public static String SELECTED_MULTIVALUE = "ManageMultivalueComponent.SELECTED_MULTIVALUE";

    private String valueType = null;

    @Override
    public void onInit(ArgumentList arg) {
        super.onInit(arg);
        valueType = arg.get("valueType");
        String multivalue = arg.get("multivalue");
        if (multivalue != null && !multivalue.trim().isEmpty()) {
            loadSelectedValues(multivalue);
        }
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

    private String getFilteredValueListDql(String valueStart) {
        if (isContractCurrencyValue() || isDocumentCurrencyValue()) {
            return "select s_code as s_value, s_code as s_label from ccdea_currency where upper(s_code) like  upper(" + DfUtil.toQuotedString(valueStart + '%') + ")" ;
        }
        else if (isCustomerNumberValue()) {
            return "select s_number as s_value, s_number as s_label from ccdea_customer where upper(s_number) like  upper(" + DfUtil.toQuotedString(valueStart + '%') + ")" ;
        }
        else if (isCustomerNameValue()) {
            return "select s_name as s_value, s_name as s_label from ccdea_customer where upper(s_name) like  upper(" + DfUtil.toQuotedString(valueStart + '%') + ")" ;
        }
        else if (isDocumentNumberValue()) {
            return "select distinct s_doc_number as s_value, s_doc_number as s_label from ccdea_pd where upper(s_doc_number) like upper(" + DfUtil.toQuotedString(valueStart + '%') + ") UNION ALL " +
                    "select distinct s_doc_number as s_value, s_doc_number as s_label from ccdea_spd where upper(s_doc_number) like upper(" + DfUtil.toQuotedString(valueStart + '%') + ") UNION ALL " +
                    "select distinct s_doc_number as s_value, s_doc_number as s_label from ccdea_svo_detail where upper(s_doc_number) like  upper(" + DfUtil.toQuotedString(valueStart + '%') + ")" ;
        }
        else if (isContractNumberValue()) {
            return "select distinct s_contract_number as s_value, s_contract_number as s_label from ccdea_base_doc where upper(s_contract_number) like  upper(" + DfUtil.toQuotedString(valueStart + '%') + ")" ;
        }
        else if (isPassportNumberValue()) {
            return "select distinct s_passport_number as s_value, s_passport_number as s_label from ccdea_base_doc where upper(s_passport_number) like  upper(" + DfUtil.toQuotedString(valueStart + '%') + ")";
        }
        else {
            return null;
        }
    }

    private String getSelectedValueListDql(String multivalue) {
        if (isContractCurrencyValue() || isDocumentCurrencyValue()) {
            return "select s_code as s_value, s_code as s_label from ccdea_currency where s_code in(" + multivalue + ")";
        }
        else if (isCustomerNumberValue()) {
            return "select s_number as s_value, s_number as s_label from ccdea_customer where s_number in(" + multivalue + ")";
        }
        else if (isCustomerNameValue()) {
            return "select s_name as s_value, s_name as s_label from ccdea_customer where s_name in(" + multivalue + ")";
        }
        else if (isDocumentNumberValue()) {
            return "select distinct s_doc_number as s_value, s_doc_number as s_label from ccdea_pd where s_doc_number in(" + multivalue + ") UNION ALL " +
                    "select distinct s_doc_number as s_value, s_doc_number as s_label from ccdea_spd where s_doc_number in(" + multivalue + ") UNION ALL " +
                    "select distinct s_doc_number as s_value, s_doc_number as s_label from ccdea_svo_detail where s_doc_number in(" + multivalue + ")";
        }
        else if (isContractNumberValue()) {
            return "select distinct s_contract_number as s_value, s_contract_number as s_label from ccdea_base_doc where s_contract_number in(" + multivalue + ")";
        }
        else if (isPassportNumberValue()) {
            return "select distinct s_passport_number as s_value, s_passport_number as s_label from ccdea_base_doc where s_passport_number in(" + multivalue + ")";
        }
        else {
            return null;
        }
    }

    protected void loadSelectedValues(String multivalue) {
        IDfCollection col = null;
        try{
            String dql = getSelectedValueListDql(multivalue);
            IDfQuery query = new DfQuery(dql);
            col = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            MultiSelector multivalueSelector = getMultivalueSelectorControl();
            List<String[]> itemList = new ArrayList<String[]>();
            while(col.next()){
                String[] item = new String[] {col.getString("s_value"), col.getString("s_label")};
                itemList.add(item);
            }
            multivalueSelector.setSelectedItems(itemList);
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

    public MultiSelector getMultivalueSelectorControl() {
        return (MultiSelector)getControl("multivalue_selector", MultiSelector.class);
    }

    public Text getValueFilterControl() {
        return (Text)getControl("value_filter", Text.class);
    }

    public void loadFilteredValues(String valueStart) {
        IDfCollection col = null;
        try{
            String dql = getFilteredValueListDql(valueStart);
            IDfQuery query = new DfQuery(dql);
            col = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            MultiSelector multivalueSelector = getMultivalueSelectorControl();
            List<String[]> itemList = new ArrayList<String[]>();
            while(col.next()){
                String[] item = new String[] {col.getString("s_value"), col.getString("s_label")};
                itemList.add(item);
            }
            multivalueSelector.setItems(itemList);
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
        loadFilteredValues(getValueFilterControl().getValue());
    }

    @Override
    public boolean onCommitChanges() {
        String multivalue = null;
        List<String[]> selectedItemList = getMultivalueSelectorControl().getSelectedItems();
        for (String[] selectedItem : selectedItemList) {
            if (multivalue == null) {
                multivalue = "'" + selectedItem[0] + "'";
            }
            else {
                multivalue += ",'" + selectedItem[0] + "'";
            }
        }
        setReturnValue(SELECTED_MULTIVALUE, multivalue);
        return super.onCommitChanges();
    }
}
