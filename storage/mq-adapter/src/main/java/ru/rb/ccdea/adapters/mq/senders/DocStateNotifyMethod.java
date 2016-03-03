package ru.rb.ccdea.adapters.mq.senders;

import com.documentum.bpm.IDfWorkitemEx;
import com.documentum.bpm.rtutil.WorkflowMethod;
import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.fc.impl.util.StringUtil;
import com.documentum.fc.methodserver.IDfMethod;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.fileutils.ContentLoader;

import java.io.PrintWriter;

public class DocStateNotifyMethod extends WorkflowMethod implements IDfMethod, IDfModule {

    public static String REPLY_OBJECT_NAME = "replyObject";
    public static String ATTR_SOURCE_SYSTEM = "originDocIdentification/sourceSystem";
    public static String ATTR_SOURCE_ID = "originDocIdentification/sourceId";
    public static String ATTR_DOC_STATE = "docState";
    public static String ATTR_DOC_REFERENCE = "docReference";

    public static String DOC_STATE_LOADED = "Loaded";

    protected static final String SYSPROP_CCDEA_DRL = "ccdea.drl";

    @Override
    protected int doTask(IDfWorkitem iDfWorkitem, IDfProperties iDfProperties, PrintWriter printWriter) throws Exception {
        IDfSysObject messageObject = getComponentFromFirstPackage(iDfWorkitem.getSession(), iDfWorkitem);
        IDfWorkitemEx iDfWorkitemEx = (IDfWorkitemEx) iDfWorkitem;
        iDfWorkitemEx.setStructuredDataTypeAttrValue(REPLY_OBJECT_NAME, ATTR_SOURCE_SYSTEM, ExternalMessagePersistence.getContentSourceCode(messageObject));
        iDfWorkitemEx.setStructuredDataTypeAttrValue(REPLY_OBJECT_NAME, ATTR_SOURCE_ID, ExternalMessagePersistence.getContentSourceId(messageObject));
        iDfWorkitemEx.setStructuredDataTypeAttrValue(REPLY_OBJECT_NAME, ATTR_DOC_STATE, DOC_STATE_LOADED);
        String contentId = ExternalMessagePersistence.getLastModifiedContentId(messageObject);
        if (contentId != null) {
            String docbaseName = iDfWorkitem.getSession().getDocbaseName();
            String drlUrl = ContentLoader.getSyspropValue(iDfWorkitem.getSession(), SYSPROP_CCDEA_DRL);
            String documentUrl = drlUrl + "&docbase=" + docbaseName + "&objectId=" + contentId;
            iDfWorkitemEx.setStructuredDataTypeAttrValue(REPLY_OBJECT_NAME, ATTR_DOC_REFERENCE, documentUrl);
        }
        return 0;
    }

    public static IDfSysObject getComponentFromFirstPackage(IDfSession session, IDfWorkitem wi) throws DfException {
        IDfSysObject result = null;
        IDfPackage pkg = getPackage(session, wi);
        if (pkg != null) {
            IDfId componentId = pkg.getComponentId(0);
            DfLogger.warn(null, "component id: " + (componentId == null ? "null" : componentId), null, null);
            result = (IDfSysObject) session.getObject(componentId);
        }
        return result;
    }


    public static IDfPackage getPackage(IDfSession session, IDfWorkitem wi) throws DfException {
        IDfPackage result = null;
        IDfCollection coll = null;
        try {
            String packageId = null;
            coll = wi.getPackages(null);
            if (coll.next()) {
                packageId = coll.getString("r_object_id");
            }
            DfLogger.warn(null, "package id: " + (packageId == null ? "null" : packageId), null, null);
            if (!StringUtil.isEmptyOrNull(packageId)) {
                result = wi.getPackage(new DfId(packageId));
            }
        } finally {
            if (coll != null) {
                coll.close();
            }
        }
        return result;
    }
}
