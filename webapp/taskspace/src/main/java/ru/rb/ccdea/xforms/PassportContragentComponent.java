package ru.rb.ccdea.xforms;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.databound.DataProvider;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.component.Component;

public class PassportContragentComponent extends Component {

    @Override
    public void onInit(ArgumentList arg) {
        super.onInit(arg);
        String objectId = arg.get("objectId");
        DataProvider dataProvider = ((Datagrid)getControl("contragent_datagrid", Datagrid.class)).getDataProvider();
        dataProvider.setDfSession(getDfSession());
        dataProvider.setQuery("select s_contractor_name_r, s_contractor_country_code_r from ccdea_passport where r_object_id = '" + objectId + "' and s_contractor_name_r is not null enable(row_based)");
    }
}
