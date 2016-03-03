package ru.rb.ccdea.dialog;

import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfUtil;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.*;
import com.documentum.web.formext.component.Component;
import ru.rb.ccdea.storage.persistence.BaseDocumentPersistence;
import ru.rb.ccdea.storage.persistence.DossierPersistence;
import ru.rb.ccdea.storage.services.api.IDossierService;

/**
 * Компонент для диалога поиска досье
 * Created by ER21595 on 14.07.2015.
 */
public class DossierSearchComponent extends Component {
    String dossierType = DossierPersistence.DOSSIER_TYPE_CONTRACT;
    String objectId = null;
    int MIN_LIST_SIZE = 3;
    int MAX_LIST_SIZE = 8;

    @Override
    public void onInit(ArgumentList arg) {
        super.onInit(arg);
        setVisibleControls();
        objectId = arg.get("objectId");
        ListBox dossierList = (ListBox) getControl("results_list", ListBox.class);
        dossierList.setWidth("400");
        try {
            IDfGroup grp = (IDfGroup) getDfSession().getObjectByQualification("dm_group where group_name ='"+ BaseDocumentPersistence.CCDEA_PRIVELEGED_USERS_GROUP_NAME+"'");
            if (grp!=null){
                boolean privilegedUser = grp.isUserInGroup(getDfSession().getLoginUserName());
                getControl("reg_branch_code", Text.class).setVisible(privilegedUser);
                getControl("reg_branch_code_lbl", Label.class).setVisible(privilegedUser);
            }
        } catch (DfException e) {
            e.printStackTrace();
        }
    }

    private void setVisibleControls(){
        boolean isContract = dossierType.equals(DossierPersistence.DOSSIER_TYPE_CONTRACT);
        getControl("passport_number_lbl", Label.class).setVisible(!isContract);
        getControl("passport_number", Text.class).setVisible(!isContract);
        getControl("contract_date_lbl",Label.class).setVisible(isContract);
        getControl("contract_date", DateInput.class).setVisible(isContract);
        getControl("contract_number_lbl",Label.class).setVisible(isContract);
        getControl("contract_number", Text.class).setVisible(isContract);
    }

    public void onDossierTypeSelect(Control c, ArgumentList arg){
        dossierType = ((DropDownList)c).getValue();
        ListBox dossierList = (ListBox) getControl("results_list");
        dossierList.setMutable(true);
        dossierList.clearOptions();
        dossierList.setWidth("400");
        setVisibleControls();
    }

    public void onClickSearch(Control c, ArgumentList arg){
        IDfCollection col = null;
        try{
            IDfQuery query = new DfQuery(getDossierSearchDql());
            col = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            ListBox dossierList = (ListBox) getControl("results_list");
            dossierList.setWidth(null);
            dossierList.setMutable(true);
            dossierList.clearOptions();
            while(col.next()){
                Option opt = new Option();
                opt.setValue(col.getString("r_object_id"));
                opt.setLabel(DossierPersistence.getDossierDescription(getDfSession().getObject(col.getId("r_object_id"))));
                dossierList.addOption(opt);
            }
            int length=dossierList.getOptions().size();
            if(length<MIN_LIST_SIZE){
                dossierList.setSize(String.valueOf(MIN_LIST_SIZE));
            }
            if (length>=MIN_LIST_SIZE &&length <=MAX_LIST_SIZE){
                dossierList.setSize(String.valueOf(length));
            }
            if(length>MAX_LIST_SIZE){
                dossierList.setSize(String.valueOf(MAX_LIST_SIZE));
            }
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

    private String getDossierSearchDql(){
        StringBuilder bld = new StringBuilder("select * from ccdea_dossier where ");
        DropDownList dossierType = (DropDownList) getControl("dossier_type");
        Text branch = (Text) getControl("reg_branch_code");
        if(branch.isVisible() && branch.getValue()!=null &&branch.getValue().length()>0){
            bld.append("s_reg_branch_code='");
            bld.append(branch.getValue()).append("' and ");
        }
        if(dossierType.getValue().equals(DossierPersistence.DOSSIER_TYPE_CONTRACT)){
            bld.append("s_dossier_type='");
            bld.append(DossierPersistence.DOSSIER_TYPE_CONTRACT).append("'");
            Text contractNumber = (Text) getControl("contract_number");
            if(contractNumber.getValue()!=null && contractNumber.getValue().length()>0){
                bld.append(" and lower(s_contract_number) like ");
                bld.append( DfUtil.toQuotedString(contractNumber.getValue().toLowerCase() + '%') );
            }
            DateInput contrDate = (DateInput) getControl("contract_date");
            if(!contrDate.isUndefinedDate() && contrDate.isValidDate()){
                bld.append(" and t_contract_date = DATE('");
                bld.append(contrDate.getValue()).append("')");
            }
        }else {
            bld.append("s_dossier_type='");
            bld.append(DossierPersistence.DOSSIER_TYPE_PASSPORT).append("'");
            Text psNumber = (Text) getControl("passport_number");
            if(psNumber.getValue()!=null && psNumber.getValue().length()>0){
                bld.append(" and lower(s_passport_number) like '");
                bld.append(DfUtil.toQuotedString(psNumber.getValue().toLowerCase() + '%') );
            }
        }
        return bld.toString();
    }

    @Override
    public boolean onCommitChanges() {
        try {
            ListBox dossierList = (ListBox) getControl("results_list");
            String selectedDossierId =dossierList.getValue();
            if(selectedDossierId!=null && selectedDossierId.length()>0) {
                IDossierService srv = (IDossierService)
                        getDfSession().getClient().newService("ucb_ccdea_dossier_service", getDfSession().getSessionManager());
                srv.attachDoc(getDfSession(), new DfId(selectedDossierId), new DfId(objectId));
            }
        } catch (DfServiceException e) {
            DfLogger.error(this,e.getMessage(),null,e);
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(), null, e);
        }
        return super.onCommitChanges();
    }
}
