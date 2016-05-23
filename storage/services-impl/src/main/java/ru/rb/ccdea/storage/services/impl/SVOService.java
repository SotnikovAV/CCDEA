package ru.rb.ccdea.storage.services.impl;

import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.adapters.mq.binding.svo.MCDocInfoModifySVOType;
import ru.rb.ccdea.adapters.mq.binding.svo.VODetailsType;
import ru.rb.ccdea.storage.persistence.AuditPersistence;
import ru.rb.ccdea.storage.persistence.DossierPersistence;
import ru.rb.ccdea.storage.persistence.SVOPersistence;
import ru.rb.ccdea.storage.persistence.details.ActionRecordDetails;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;
import ru.rb.ccdea.storage.services.api.ISVOService;

public class SVOService extends DfService implements ISVOService{
    @Override
    public String createDocumentFromMQType(IDfSession dfSession, MCDocInfoModifySVOType documentXmlObject, VODetailsType voDetailsXmlObject, String docSourceCode, String docSourceId) throws DfException {
        String svoDetailObjectId = null;
        boolean isTransAlreadyActive = dfSession.isTransactionActive();
        try {
            DfLogger.info(this, "CreateDocumentFromMQType: {0}/{1}. Start", new String[]{docSourceCode, docSourceId}, null);

            if (!isTransAlreadyActive) {
                dfSession.beginTrans();
            }

            IDfSysObject passportSysObject = SVOPersistence.createDocument(dfSession);
            IDfPersistentObject dossier = null;
			try {
				DossierKeyDetails keyDetailsFromMQ = SVOPersistence.getKeyDetailsFromMQ(documentXmlObject,
						voDetailsXmlObject);
				dossier = DossierPersistence.getOrCreateDossierByKeyDetails(dfSession, keyDetailsFromMQ);
			} catch (Exception ex) {
				DfLogger.warn(this,
						"Не удалось получить досье для документа '" + passportSysObject.getObjectId().getId() + "'",
						null, null);
			}

            VersionRecordDetails versionRecordDetails = new VersionRecordDetails();
            versionRecordDetails.eventName = AuditPersistence.UPDATE_BY_METADATA_SAVE_EVENT_NAME;
            versionRecordDetails.sourceSystem = docSourceCode;
            versionRecordDetails.userName = documentXmlObject.getUserId();
            versionRecordDetails.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar().getTime();

            SVOPersistence.saveFieldsToDocument(passportSysObject, documentXmlObject, voDetailsXmlObject, (dossier == null ? DfId.DF_NULLID_STR : dossier.getObjectId().getId()), docSourceCode, docSourceId, versionRecordDetails);
            svoDetailObjectId = passportSysObject.getObjectId().getId();

            ActionRecordDetails attachDocActionRecord = new ActionRecordDetails();
            attachDocActionRecord.eventName = AuditPersistence.SET_DOC_DOSSIER_EVENT_NAME;
            attachDocActionRecord.sourceSystem = docSourceCode;
            attachDocActionRecord.userName = documentXmlObject.getUserId();
            attachDocActionRecord.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar().getTime();
            attachDocActionRecord.dossierState = "";
            attachDocActionRecord.dossierFullArchiveNumber = "";
            attachDocActionRecord.dossierDescription = dossier == null ? "" : DossierPersistence.getDossierDescription(dossier);
            attachDocActionRecord.oldDossierDescription = "";

            AuditPersistence.createActionRecord(dfSession, new DfId(svoDetailObjectId), attachDocActionRecord);
            AuditPersistence.createVersionRecord(dfSession, new DfId(svoDetailObjectId), versionRecordDetails);

            if (!isTransAlreadyActive) {
                dfSession.commitTrans();
            }

            DfLogger.info(this, "CreateDocumentFromMQType: {0}/{1}. Finish (new ID: {2})", new String[] {docSourceCode, docSourceId, svoDetailObjectId}, null);
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
        return svoDetailObjectId;
    }

    @Override
    public void updateDocumentFromMQType(IDfSession dfSession, MCDocInfoModifySVOType documentXmlObject, VODetailsType voDetailsXmlObject, String docSourceCode, String docSourceId, IDfId documentId) throws DfException {
        boolean isTransAlreadyActive = dfSession.isTransactionActive();
        try {
            DfLogger.info(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Start", new String[] {docSourceCode, docSourceId, documentId.getId()}, null);

            if (!isTransAlreadyActive) {
                dfSession.beginTrans();
            }

            IDfSysObject passportSysObject = SVOPersistence.lockDocumentForUpdate(dfSession, documentId);
            IDfPersistentObject dossier = null;
            IDfPersistentObject oldDossier = null;
			try {
				DossierKeyDetails keyDetailsFromMQ = SVOPersistence.getKeyDetailsFromMQ(documentXmlObject,
						voDetailsXmlObject);
				if (!keyDetailsFromMQ.equals(SVOPersistence.getKeyDetails(passportSysObject))) {
					IDfId oldDossierId = SVOPersistence.getDossierId(passportSysObject);
					if(oldDossierId != null && !oldDossierId.isNull()) {
						oldDossier = dfSession.getObject(oldDossierId);
					}
					dossier = DossierPersistence.getOrCreateDossierByKeyDetails(dfSession, keyDetailsFromMQ);
				} else {
					dossier = dfSession.getObject(SVOPersistence.getDossierId(passportSysObject));
				}
			} catch (Exception ex) {
				DfLogger.warn(this,
						"Не удалось получить досье для документа '" + passportSysObject.getObjectId().getId() + "'",
						null, null);
			}

            VersionRecordDetails versionRecordDetails = new VersionRecordDetails();
            versionRecordDetails.eventName = AuditPersistence.UPDATE_BY_METADATA_SAVE_EVENT_NAME;
            versionRecordDetails.sourceSystem = docSourceCode;
            versionRecordDetails.userName = documentXmlObject.getUserId();
            versionRecordDetails.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar().getTime();

            SVOPersistence.saveFieldsToDocument(passportSysObject, documentXmlObject, voDetailsXmlObject, (dossier == null ? DfId.DF_NULLID_STR : dossier.getObjectId().getId()), docSourceCode, docSourceId, versionRecordDetails);

            if (oldDossier != null) {
                ActionRecordDetails attachDocActionRecord = new ActionRecordDetails();
                attachDocActionRecord.eventName = AuditPersistence.SET_DOC_DOSSIER_EVENT_NAME;
                attachDocActionRecord.sourceSystem = docSourceCode;
                attachDocActionRecord.userName = documentXmlObject.getUserId();
                attachDocActionRecord.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar().getTime();
                attachDocActionRecord.dossierState = "";
                attachDocActionRecord.dossierFullArchiveNumber = "";
                attachDocActionRecord.dossierDescription = dossier == null ? "" : DossierPersistence.getDossierDescription(dossier);
                attachDocActionRecord.oldDossierDescription =  oldDossier == null ? "" : DossierPersistence.getDossierDescription(oldDossier);

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
