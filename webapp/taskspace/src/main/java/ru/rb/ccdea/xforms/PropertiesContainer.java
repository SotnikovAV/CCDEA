package ru.rb.ccdea.xforms;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.formext.component.Container;
import com.documentum.webcomponent.library.imaging.uiframework.tabs.IOpenItemContainer;
import com.documentum.webcomponent.library.imaging.uiframework.tabs.IOpenItemUcfLauncher;
import com.documentum.webcomponent.library.imaging.uiframework.tabs.NavigationItemHandler;

import javax.servlet.http.HttpSession;

/**
 * Контейнер для компонента, отображающего Xform'у на закладке открытых документов
 * Created by ER21595 on 19.06.2015.
 */
public class PropertiesContainer extends Container implements IOpenItemContainer, IOpenItemUcfLauncher {
    private NavigationItemHandler m_navItemHandler = null;
    private boolean m_isReadOnly = false;
    private boolean m_isLockInstance = false;
    public static final String LAUNCHER_NAME = "ucflauncher";
    private static final String READONLY_KEY = "readOnly";
    private static final String LOCK_INSTANCE_KEY = "lockInstance";
    private static final String FORM_TEMPLATE_KEY = "formTemplate";
    private String objectId;
    private String formTemplateId;
    private String formTemplateName;

    private String currentTab = "dossier_tab";

    public PropertiesContainer() {
    }

    public void onInit(ArgumentList args) {
        String componentId = this.lookupString("contains.component");
        args.add("component", componentId);
        initReadOnly(args);
        args.add("readOnly", String.valueOf(this.m_isReadOnly));

        Boolean lockInstance1 = this.lookupBoolean("lockInstance");
        if(lockInstance1 != null) {
            this.m_isLockInstance = lockInstance1.booleanValue();
        }
        objectId = args.get("objectId");
        formTemplateId=args.get("formTemplateId");
        formTemplateName=args.get("formTemplateName");
        args.add("lockInstance", String.valueOf(this.m_isLockInstance));
        args.add("popupKey", this.getComponentId() + "_" + objectId);
        super.onInit(args);
        setCurrentComponent(componentId);
        registerOpenItem(this.getPageContext().getSession());

    }

    public String getContainedUrl(){
        return "/component/"+getContainedComponentId()+"?objectId="+objectId
                +"&formTemplateId="+formTemplateId+"&formTemplateName="+formTemplateName;
    }

    public String getTabComponentUrl(){
        return "/component/property_tabs?objectId=" +objectId;
    }

    private void initReadOnly(ArgumentList args) {
        Boolean readOnly = this.lookupBoolean("readOnly");
        if(readOnly != null) {
            this.m_isReadOnly = readOnly.booleanValue();
            if(!this.m_isReadOnly) {
                try {
                    String e = args.get("objectId");
                    if(e != null && e.length() > 0) {
                        IDfPersistentObject object = this.getDfSession().getObject(new DfId(e));
                        if(object instanceof IDfSysObject) {
                            String lockOwner = ((IDfSysObject)object).getLockOwner();
                            if(lockOwner != null && lockOwner.length() > 0 && !lockOwner.equals(this.getDfSession().getUser((String)null).getUserName()) || ((IDfSysObject)object).getPermit() < 6) {
                                this.m_isReadOnly = true;
                            }
                        }
                    }
                } catch (DfException var6) {
                    ;
                }
            }
        }
    }

    public void setCurrentComponent(String strComponentId) {
        this.setContainedComponentId(strComponentId);
        String strComponentName = strComponentId + "_contents";
        this.setContainedComponentName(strComponentName);
    }

    public NavigationItemHandler getNavigationItemHandler() {
        if(this.m_navItemHandler == null) {
            String frameId =this.getPageContext().getRequest().getParameter("__dmfFrameId");
            if( frameId== null|| frameId.length()==0){
                frameId="ccdea_vk_user_ccdea_vk_search_tab_frame";
            }
            this.m_navItemHandler = new NavigationItemHandler(frameId);
        }
        return this.m_navItemHandler;
    }

    public void registerOpenItem(HttpSession session) {
        if(this.m_navItemHandler == null) {
            this.getNavigationItemHandler();
        }
        String tabName = this.m_navItemHandler.getTabName();
        this.m_navItemHandler.registerOpenItem(this.getPageContext().getSession(), tabName);
    }

    public void closeOpenItem() {
        this.m_navItemHandler.closeOpenItem(this, this.getTabName());
    }

    public String getTabName() {
        if(this.m_navItemHandler == null) {
            this.getNavigationItemHandler();
        }

        return this.m_navItemHandler.getTabName();
    }

    public void onCacheFormInformation(Control control, ArgumentList args) {
    }

    public String getDocumentObjectId() {
        return this.getInitArgs().get("objectId") == null?"null":this.getInitArgs().get("objectId");
    }

    public String getLaunchUcf() {
        return this.getInitArgs().get("launchUcf") == null?"false":this.getInitArgs().get("launchUcf");
    }
}
