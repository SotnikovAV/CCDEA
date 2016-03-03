package ru.rb.ccdea.xforms;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.databound.DataProvider;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.component.Component;

public class ContractMultisumComponent extends Component {
    @Override
    public void onInit(ArgumentList arg) {
        super.onInit(arg);
        String objectId = arg.get("objectId");
        DataProvider dataProvider = ((Datagrid)getControl("multisum_datagrid", Datagrid.class)).getDataProvider();
        dataProvider.setDfSession(getDfSession());
        dataProvider.setQuery("select d_sums_amount_r, s_sums_currency_code_r from ccdea_contract where r_object_id = '" + objectId + "' and d_sums_amount_r is not null enable(row_based)");
    }
}
