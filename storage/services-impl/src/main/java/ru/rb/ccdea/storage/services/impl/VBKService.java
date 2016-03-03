package ru.rb.ccdea.storage.services.impl;

import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.adapters.mq.binding.vbk.MCDocInfoModifyVBKType;
import ru.rb.ccdea.storage.persistence.AuditPersistence;
import ru.rb.ccdea.storage.persistence.DossierPersistence;
import ru.rb.ccdea.storage.persistence.VBKPersistence;
import ru.rb.ccdea.storage.persistence.details.ActionRecordDetails;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;
import ru.rb.ccdea.storage.services.api.IVBKService;

public class VBKService extends DfService implements IVBKService {
    @Override
    public String createDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyVBKType documentXmlObject, String docSourceCode, String docSourceId) throws DfException {
        String vbkObjectId = null;
        boolean isTransAlreadyActive = dfSession.isTransactionActive();
        try {
            DfLogger.info(this, "CreateDocumentFromMQType: {0}/{1}. Start", new String[] {docSourceCode, docSourceId}, null);

            if (!isTransAlreadyActive) {
                dfSession.beginTrans();
            }

            IDfSysObject vbkSysObject = VBKPersistence.createDocument(dfSession);
            DossierKeyDetails keyDetailsFromMQ = VBKPersistence.getKeyDetailsFromMQ(documentXmlObject);
            IDfPersistentObject dossier = DossierPersistence.getOrCreateDossierByKeyDetails(dfSession, keyDetailsFromMQ);

            VersionRecordDetails versionRecordDetails = new VersionRecordDetails();
            versionRecordDetails.eventName = AuditPersistence.UPDATE_BY_METADATA_SAVE_EVENT_NAME;
            versionRecordDetails.sourceSystem = docSourceCode;
            versionRecordDetails.userName = documentXmlObject.getUserId();
            versionRecordDetails.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar().getTime();

            VBKPersistence.saveFieldsToDocument(vbkSysObject, documentXmlObject, dossier.getObjectId().getId(), docSourceCode, docSourceId, versionRecordDetails);
            vbkObjectId = vbkSysObject.getObjectId().getId();

            ActionRecordDetails attachDocActionRecord = new ActionRecordDetails();
            attachDocActionRecord.eventName = AuditPersistence.SET_DOC_DOSSIER_EVENT_NAME;
            attachDocActionRecord.sourceSystem = docSourceCode;
            attachDocActionRecord.userName = documentXmlObject.getUserId();
            attachDocActionRecord.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar().getTime();
            attachDocActionRecord.dossierState = "";
            attachDocActionRecord.dossierFullArchiveNumber = "";
            attachDocActionRecord.dossierDescription = DossierPersistence.getDossierDescription(dossier);
            attachDocActionRecord.oldDossierDescription = "";

            AuditPersistence.createActionRecord(dfSession, new DfId(vbkObjectId), attachDocActionRecord);
            AuditPersistence.createVersionRecord(dfSession, new DfId(vbkObjectId), versionRecordDetails);

            if (!isTransAlreadyActive) {
                dfSession.commitTrans();
            }

            DfLogger.info(this, "CreateDocumentFromMQType: {0}/{1}. Finish (new ID: {2})", new String[] {docSourceCode, docSourceId, vbkObjectId}, null);
        }
        catch (DfException dfEx) {
            DfLogger.error(this, "CreateDocumentFromMQType: {0}/{1}. Error", new String[]{docSourceCode, docSourceId}, dfEx);
            throw dfEx;
        }
        catch (Exception ex) {
            DfLogger.error(this, "CreateDocumentFromMQType: {0}/{1}. Error", new String[]{docSourceCode, docSourceId}, ex);
            throw new DfException(ex);
        }
        finally {
            if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
                dfSession.abortTrans();
            }
        }
        return vbkObjectId;
    }

    @Override
    public void updateDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyVBKType documentXmlObject, String docSourceCode, String docSourceId, IDfId documentId) throws DfException {
        boolean isTransAlreadyActive = dfSession.isTransactionActive();
        try {
            DfLogger.info(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Start", new String[]{docSourceCode, docSourceId, documentId.getId()}, null);

            if (!isTransAlreadyActive) {
                dfSession.beginTrans();
            }

            IDfSysObject vbkSysObject = VBKPersistence.lockDocumentForUpdate(dfSession, documentId);
            IDfPersistentObject dossier = null;
            IDfPersistentObject oldDossier = null;
            DossierKeyDetails keyDetailsFromMQ = VBKPersistence.getKeyDetailsFromMQ(documentXmlObject);
            if (!keyDetailsFromMQ.equals(VBKPersistence.getKeyDetails(vbkSysObject))) {
                dossier = DossierPersistence.getOrCreateDossierByKeyDetails(dfSession, keyDetailsFromMQ);
            }
            else {
                dossier = dfSession.getObject(VBKPersistence.getDossierId(vbkSysObject));
            }

            VersionRecordDetails versionRecordDetails = new VersionRecordDetails();
            versionRecordDetails.eventName = AuditPersistence.UPDATE_BY_METADATA_SAVE_EVENT_NAME;
            versionRecordDetails.sourceSystem = docSourceCode;
            versionRecordDetails.userName = documentXmlObject.getUserId();
            versionRecordDetails.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar().getTime();

            VBKPersistence.saveFieldsToDocument(vbkSysObject, documentXmlObject, dossier.getObjectId().getId(), docSourceCode, docSourceId, versionRecordDetails);

            if (oldDossier != null) {
                ActionRecordDetails attachDocActionRecord = new ActionRecordDetails();
                attachDocActionRecord.eventName = AuditPersistence.SET_DOC_DOSSIER_EVENT_NAME;
                attachDocActionRecord.sourceSystem = docSourceCode;
                attachDocActionRecord.userName = documentXmlObject.getUserId();
                attachDocActionRecord.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar().getTime();
                attachDocActionRecord.dossierState = "";
                attachDocActionRecord.dossierFullArchiveNumber = "";
                attachDocActionRecord.dossierDescription = DossierPersistence.getDossierDescription(dossier);
                attachDocActionRecord.oldDossierDescription = DossierPersistence.getDossierDescription(oldDossier);;

                AuditPersistence.createActionRecord(dfSession, documentId, attachDocActionRecord);
            }

            AuditPersistence.createVersionRecord(dfSession, documentId, versionRecordDetails);

            if (!isTransAlreadyActive) {
                dfSession.commitTrans();
            }

            DfLogger.info(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Finish", new String[] {docSourceCode, docSourceId, documentId.getId()}, null);
        }
        catch (DfException dfEx) {
            DfLogger.error(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Error", new String[]{docSourceCode, docSourceId, documentId.getId()}, dfEx);
            throw dfEx;
        }
        catch (Exception ex) {
            DfLogger.error(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Error", new String[] {docSourceCode, docSourceId, documentId.getId()}, ex);
            throw new DfException(ex);
        }
        finally {
            if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
                dfSession.abortTrans();
            }
        }
    }
}
