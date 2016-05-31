package ru.rb.ccdea.xforms;

import com.documentum.fc.common.DfUtil;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.databound.DataProvider;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.component.Component;

public class ContractContragentComponent extends Component {
    @Override
    public void onInit(ArgumentList arg) {
        super.onInit(arg);
        String objectId = arg.get("objectId");
        DataProvider dataProvider = ((Datagrid)getControl("contragent_datagrid", Datagrid.class)).getDataProvider();
        dataProvider.setDfSession(getDfSession());
        dataProvider.setQuery("select s_contr_name_r, s_contr_country_code_r, s_contr_bank_country_code_r from ccdea_contract where r_object_id = " + DfUtil.toQuotedString(objectId) + " and s_contr_name_r is not null enable(row_based)");
    }
}
