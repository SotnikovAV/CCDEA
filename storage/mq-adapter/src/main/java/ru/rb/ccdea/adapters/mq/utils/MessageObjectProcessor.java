package ru.rb.ccdea.adapters.mq.utils;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;

import java.util.Date;
import java.util.List;

public abstract class MessageObjectProcessor {

    public abstract String getMessageType();
    public abstract String getSourceKey();

    protected abstract String readModificationVerb();
    protected abstract Date readSourceTime();
    protected abstract List<MessageObjectIdentifiers> readOriginIdentificationList();

    protected Object messageXmlContent = null;
    protected String modificationVerb = null;
    protected Date sourceTime = null;
    protected String docSourceId = null;
    protected String docSourceSystem = null;
    protected String contentSourceId = null;
    protected String contentSourceSystem = null;

    public MessageObjectProcessor(Object messageXmlContent) {
        this.messageXmlContent = messageXmlContent;
    }

    public void readValuesFromXml(UnifiedResult result) {
        modificationVerb = readModificationVerb();
        sourceTime = readSourceTime();

        List<MessageObjectIdentifiers> originIdentificationList = readOriginIdentificationList();
        if (originIdentificationList != null) {
            processOriginIdentificationList(originIdentificationList, result);
        }
        else {
            result.setError(UnifiedResult.VALIDATE_ERROR_CODE, "OriginIdentificationList is null");
        }
    }

    protected void processOriginIdentificationList(List<MessageObjectIdentifiers> originIdentificationList, UnifiedResult result) {
        if (originIdentificationList.size() == 1) {
            docSourceId = originIdentificationList.get(0).getSourceId();
            docSourceSystem = originIdentificationList.get(0).getSourceSystem();
        } else {
            boolean isFirstIdentification = true;
            for (MessageObjectIdentifiers originIdentification : originIdentificationList) {
                if (ExternalMessagePersistence.SOURCE_SYSTEM_DVK.equalsIgnoreCase(originIdentification.getSourceSystem())) {
                    if (!isFirstIdentification) {
                        contentSourceId = docSourceId;
                        contentSourceSystem = docSourceSystem;
                    }
                    docSourceId = originIdentification.getSourceId();
                    docSourceSystem = originIdentification.getSourceSystem();
                } else if (ExternalMessagePersistence.SOURCE_SYSTEM_TBSVK.equalsIgnoreCase(originIdentification.getSourceSystem())) {
                    if (!isFirstIdentification) {
                    	if(!ExternalMessagePersistence.SOURCE_SYSTEM_DVK.equals(docSourceSystem)) {
	                        contentSourceId = docSourceId;
	                        contentSourceSystem = docSourceSystem;
	                        docSourceId = originIdentification.getSourceId();
	                        docSourceSystem = originIdentification.getSourceSystem();
                    	} else {
                    		contentSourceId = originIdentification.getSourceId();
                    		contentSourceSystem = originIdentification.getSourceSystem();
                    	}
                    } else {
                    	docSourceId = originIdentification.getSourceId();
                    	docSourceSystem = originIdentification.getSourceSystem();
                    }
                } else {
                    if (isFirstIdentification) {
                        docSourceId = originIdentification.getSourceId();
                        docSourceSystem = originIdentification.getSourceSystem();
                    } else {
                        contentSourceId = originIdentification.getSourceId();
                        contentSourceSystem = originIdentification.getSourceSystem();
                    }
                }
                isFirstIdentification = false;
            }
        }
    }

    public void storeMessageObjectOnValidation(IDfSysObject messageSysObject) throws DfException {
        ExternalMessagePersistence.storeMessageObjectOnValidation(messageSysObject, modificationVerb, getMessageType(), getSourceKey(), sourceTime, docSourceSystem, docSourceId, contentSourceSystem, contentSourceId);
    }

    public void storeMessageObjectOnErrorAfterValidation(IDfSysObject messageSysObject) throws DfException {
        ExternalMessagePersistence.storeMessageObjectOnErrorAfterValidation(messageSysObject, modificationVerb, getMessageType(), getSourceKey(), sourceTime, docSourceSystem, docSourceId, contentSourceSystem, contentSourceId);
    }

    public void storeMessageObjectAfterValidation(IDfSysObject messageSysObject, UnifiedResult result) throws DfException {
        ExternalMessagePersistence.storeMessageObjectAfterValidation(messageSysObject, result.isError(), result.getErrorCode(), result.getErrorDescription(), result.getResultDate());
    }

    public String getModificationVerb() {
        return modificationVerb;
    }

    public boolean isCreateVerbSpecified() {
        return ExternalMessagePersistence.MODIFICATION_VERB_CREATE.equalsIgnoreCase(modificationVerb);
    }

    public boolean isUpdateVerbSpecified() {
        return ExternalMessagePersistence.MODIFICATION_VERB_UPDATE.equalsIgnoreCase(modificationVerb);
    }

    public boolean isFirstValidMessage(IDfSysObject messageSysObject) throws DfException {
        return ExternalMessagePersistence.isFirstValidMessageWithSourceKey(messageSysObject, getSourceKey(), getMessageType(), sourceTime);
    }

    public boolean isValidDocMessageExists(IDfSysObject messageSysObject) throws DfException {
        return ExternalMessagePersistence.isValidMessageExistsForDoc(messageSysObject, docSourceSystem, docSourceId);
    }

    public class MessageObjectIdentifiers {
        String sourceId = null;
        String sourceSystem = null;

        public String getSourceId() {
            return sourceId;
        }

        public String getSourceSystem() {
            return sourceSystem;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }

        public void setSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
        }
    }
}
