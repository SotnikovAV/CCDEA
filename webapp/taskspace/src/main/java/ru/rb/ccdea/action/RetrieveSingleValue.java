package ru.rb.ccdea.action;

import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfUtil;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionExecution;
import com.documentum.web.formext.action.IInlineCapableAction;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;

import java.util.Map;

public class RetrieveSingleValue implements IActionExecution, IInlineCapableAction {
    @Override
    public boolean execute(String s, IConfigElement iConfigElement, ArgumentList argumentList, Context context, Component component, Map resultMap) {
        String valueType = argumentList.get("valueType");
        String valueStart = argumentList.get("valueStart");
        String fullValue = trySelectSingleValue(valueType, valueStart, component.getDfSession());
        resultMap.put("RESPONSE_DATA", valueType + "," + fullValue);
        return true;
    }

    @Override
    public String[] getRequiredParams() {
        return new String[] {"valueStart","valueType"};
    }

    public String getSelectSingleValueDql(String valueType, String valueStart) {
        String dql = null;
        if (valueType.equalsIgnoreCase("contract_currency") ||
                valueType.equalsIgnoreCase("document_currency")) {
            dql = "select s_code as s_value from ccdea_currency where s_code like " + DfUtil.toQuotedString(valueStart + '%');
        }
        else if (valueType.equalsIgnoreCase("customer_name")) {
            dql = "select s_name as s_value from ccdea_customer where s_name like " + DfUtil.toQuotedString(valueStart + '%');
        }
        else if (valueType.equalsIgnoreCase("customer_number")) {
            dql = "select s_number as s_value from ccdea_customer where s_number like " + DfUtil.toQuotedString(valueStart + '%');
        }
        else if (valueType.equalsIgnoreCase("document_number")) {
            dql = "select distinct s_doc_number as s_value from ccdea_pd where s_doc_number like " + DfUtil.toQuotedString(valueStart + '%') + " UNION ALL " +
                    "select distinct s_doc_number as s_value from ccdea_spd where s_doc_number like " + DfUtil.toQuotedString(valueStart + '%') + " UNION ALL " +
                    "select distinct s_doc_number as s_value from ccdea_svo_detail where s_doc_number like " + DfUtil.toQuotedString(valueStart + '%');
        }
        else if (valueType.equalsIgnoreCase("contract_number")) {
            dql = "select distinct s_contract_number as s_value from ccdea_base_doc where s_contract_number like " + DfUtil.toQuotedString(valueStart + '%');
        }
        else if (valueType.equalsIgnoreCase("passport_number")) {
            dql = "select distinct s_passport_number as s_value from ccdea_base_doc where s_passport_number like " + DfUtil.toQuotedString(valueStart + '%');
        }
        return dql;
    }

    public String trySelectSingleValue(String valueType, String valueStart, IDfSession dfSession) {
        String fullValue = null;
        int valueCount = 0;
        IDfCollection col = null;
        try {
            IDfQuery query = new DfQuery();
            String dql = getSelectSingleValueDql(valueType, valueStart);
            query.setDQL(dql);
            col = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
            while (col.next() && valueCount < 2) {
                fullValue = col.getString("s_value");
                valueCount++;
            }
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(), null, e);
        } finally {
            if (col != null) {
                try {
                    col.close();
                } catch (DfException e) {
                    DfLogger.error(this, e.getMessage(), null, e);
                }
            }
        }
        if (valueCount == 1) {
            return fullValue;
        }
        else {
            return null;
        }
    }
}
