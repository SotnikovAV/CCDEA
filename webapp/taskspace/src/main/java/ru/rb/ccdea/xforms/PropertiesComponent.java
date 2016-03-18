package ru.rb.ccdea.xforms;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.imaging.processmanager.configuration.service.ITaskSpaceConfigService;
import com.documentum.imaging.processmanager.configuration.service.impl.TaskSpaceConfigService;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.SessionState;
import com.documentum.web.common.Trace;
import com.documentum.web.form.*;
import com.documentum.web.form.control.Button;
import com.documentum.web.formext.control.xforms.XForms;
import com.documentum.webcomponent.common.WebComponentErrorService;
import com.documentum.webcomponent.util.imaging.TaskSpaceUtil;
import com.documentum.webcomponent.xforms.TaskSpaceXFormsComponent;
import ru.rb.ccdea.storage.persistence.DossierPersistence;
import ru.rb.ccdea.storage.services.api.IDossierService;

/**
 * Класс для компонента, отображающего метаданные объекта в соответствии с
 * xform'ой-шаблоном
 * Created by ER21595 on 22.06.2015.
 */
public class PropertiesComponent extends TaskSpaceXFormsComponent {
    private static final String FORM_TEMPLATE_KEY = "formTemplateId";
    private static final String FORM_TEMPLATE_NAME_KEY = "formTemplateName";
    protected String m_TemplateName = null;
    protected String m_TemplateId = null;

    private static final String CLOSE_DOSSIER="closeDossierButton";
    private static final String OPEN_DOSSIER="openDossierButton";
    private static final String DOSSIER_SERVICE ="ucb_ccdea_dossier_service";
    private String objectId = null;
    private String dossierId = null;
    private String dossierStatus = null;
    private String dossierType = null;
    private String refreshScript;

    public PropertiesComponent(){
    }
    public void onInit(ArgumentList arg) {
        super.onInit(arg);
        String formsTemplateName = arg.get("formName");
        this.m_TemplateName = formsTemplateName;
        this.loadXFormSettings(formsTemplateName, arg);
        objectId =arg.get("objectId");
        getContext().set("objectId", objectId);
        getContext().set("contentObjectId", getContentId());
        readDossierDetails();
        setButtonsState();
        refreshScript ="";
    }

    private void loadXFormSettings(String formsTemplateName, ArgumentList arg) {
        try {
            String e = TaskSpaceUtil.getFormIdByName(formsTemplateName);
            this.m_TemplateId = e;
            XForms xFormControl = (XForms)this.getControl(XFORM, XForms.class);
            xFormControl.setObjectId(new DfId(arg.get("objectId")));
            xFormControl.setReadOnly(true);
            xFormControl.setLockInnerForm(false);
            xFormControl.setTemplateId(getFormTemplateId(arg));
        } catch (Exception var4) {
            WebComponentErrorService.getService().setNonFatalError(this, "MSG_ERROR_GET_FORMID", var4);
        }

    }

    private void readDossierDetails(){
        IDfCollection col = null;
        try{
            IDfQuery query = new DfQuery("select dos.r_object_id as r_object_id, " +
                    "s_dossier_type, dos.s_state as s_state from ccdea_base_doc doc, " +
                    "ccdea_dossier dos where doc.r_object_id = '" + objectId +
                    "' and doc.id_dossier = dos.r_object_id");
            col = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            if(col.next()){
                dossierId = col.getString("r_object_id");
                dossierStatus = col.getString("s_state");
                dossierType = col.getString("s_dossier_type");
            }
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(), null, e);
        } finally {
            if(col!=null){
                try{
                    col.close();
                } catch (DfException e) {
                    DfLogger.error(this, e.getMessage(),null, e);
                }
            }
        }
    }

    private void setButtonsState(){
        boolean validDossier = DossierPersistence.DOSSIER_TYPE_CONTRACT.equals(dossierType);
        boolean openDossier = DossierPersistence.STATE_OPENED.equals(dossierStatus);
        getControl(CLOSE_DOSSIER, Button.class).setEnabled(validDossier && openDossier);
        getControl(OPEN_DOSSIER, Button.class).setEnabled(validDossier && !openDossier);
    }

    public  void onCloseDossier(Control control, ArgumentList arg) {
        try {
            IDossierService srv = (IDossierService)
                    getDfSession().getClient().newService(DOSSIER_SERVICE, getDfSession().getSessionManager());
            srv.closeDossier(getDfSession(), new DfId(dossierId));
            dossierStatus =DossierPersistence.STATE_CLOSED;
            setButtonsState();
            onRefreshData();
        } catch (DfServiceException e) {
            DfLogger.error(this,e.getMessage(),null,e);
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(), null, e);
        }

    }
    public void onOpenDossier(Control control, ArgumentList arg) {
        try {
            IDossierService srv = (IDossierService)
                    getDfSession().getClient().newService(DOSSIER_SERVICE, getDfSession().getSessionManager());
            srv.reopenDossier(getDfSession(), new DfId(dossierId));
            dossierStatus =DossierPersistence.STATE_OPENED;
            setButtonsState();
            onRefreshData();
        } catch (DfServiceException e) {
            DfLogger.error(this,e.getMessage(),null,e);
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(), null, e);
        }
    }

    protected IDfId getFormTemplateId(ArgumentList args) {
        IDfId formTemplateId = null;

        try {
            String e = args.get(FORM_TEMPLATE_KEY);
            if(e != null && e.length() > 0) {
                formTemplateId = this.getDfSession().getIdByQualification("dm_xfm_form where r_object_id=\'" + e + "\' and definition_state=2");
            }
            if(formTemplateId == null || formTemplateId.isNull()) {
                e = args.get(FORM_TEMPLATE_NAME_KEY);
                if(e != null && e.length() > 0) {
                    formTemplateId = this.getDfSession().getIdByQualification("dm_xfm_form where object_name=\'" + e + "\' and definition_state=2");
                }
            }
            if(formTemplateId == null || formTemplateId.isNull()) {
                String app = (String) SessionState.getAttribute("app");
                String roleName = (String)SessionState.getAttribute("selectedrole");
                String strObjectId = args.get("objectId");
                IDfSysObject obj = (IDfSysObject)this.getDfSession().getObject(new DfId(strObjectId));
                ITaskSpaceConfigService configService = TaskSpaceConfigService.getInstance(app);
                formTemplateId = configService.getDocumentViewTemplate(roleName, obj.getTypeName(), this.getDfSession(), this.getContext());
            }
        } catch (Exception var9) {
            Trace.error(var9.getMessage(), var9);
        }
        return formTemplateId;
    }

    @Override
    public void onRefreshData() {
        super.onRefreshData();
        readDossierDetails();
        refreshScript = "parent.frames[1].location=parent.frames[1].location.href;";
    }

    /**
     *
     * @return последний контент: система позволяет документу иметь
     * множество файлов, но как они будут показыватья пользователю, пока не решено.
     */
    private String getContentId(){
        String content = null;
        IDfCollection col = null;
        try{
            IDfQuery query = new DfQuery("select cont.r_object_id as cont_id " +
                    "from dm_relation rel, dm_sysobject cont where rel.relation_name='ccdea_content_relation' and rel.parent_id = '" +
                    objectId + "' and rel.child_id = cont.i_chronicle_id and cont.r_content_size > 0 order by cont.r_creation_date desc");
            col = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            if (col.next()){
                content = col.getString("cont_id");
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
        return content;
    }

    public String getRefreshScript() {
        return refreshScript;
    }
}
