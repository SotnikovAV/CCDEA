package ru.rb.ccdea.storage.services.impl;

import java.util.Date;

import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;

import ru.rb.ccdea.adapters.mq.binding.passport.MCDocInfoModifyPSType;
import ru.rb.ccdea.adapters.mq.binding.passport.PSDetailsType;
import ru.rb.ccdea.storage.persistence.AuditPersistence;
import ru.rb.ccdea.storage.persistence.DossierPersistence;
import ru.rb.ccdea.storage.persistence.PassportPersistence;
import ru.rb.ccdea.storage.persistence.details.ActionRecordDetails;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;
import ru.rb.ccdea.storage.services.api.IPassportService;

public class PassportService extends DfService implements IPassportService{
    @Override
    public String createDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyPSType documentXmlObject, String docSourceCode, String docSourceId) throws DfException {
        String passportObjectId = null;
        boolean isTransAlreadyActive = dfSession.isTransactionActive();
        try {
            DfLogger.info(this, "CreateDocumentFromMQType: {0}/{1}. Start", new String[]{docSourceCode, docSourceId}, null);

            if (!isTransAlreadyActive) {
                dfSession.beginTrans();
            }

            IDfSysObject passportSysObject = PassportPersistence.createDocument(dfSession);
            DossierKeyDetails keyDetailsFromMQ = PassportPersistence.getKeyDetailsFromMQ(documentXmlObject);
            IDfPersistentObject dossier = DossierPersistence.getOrCreateDossierByKeyDetails(dfSession, keyDetailsFromMQ);

            VersionRecordDetails versionRecordDetails = new VersionRecordDetails();
            versionRecordDetails.eventName = AuditPersistence.UPDATE_BY_METADATA_SAVE_EVENT_NAME;
            versionRecordDetails.sourceSystem = docSourceCode;
            versionRecordDetails.userName = documentXmlObject.getUserId();
            versionRecordDetails.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar().getTime();

            PassportPersistence.saveFieldsToDocument(passportSysObject, documentXmlObject, dossier.getObjectId().getId(), docSourceCode, docSourceId, versionRecordDetails);
            passportObjectId = passportSysObject.getObjectId().getId();

            ActionRecordDetails attachDocActionRecord = new ActionRecordDetails();
            attachDocActionRecord.eventName = AuditPersistence.SET_DOC_DOSSIER_EVENT_NAME;
            attachDocActionRecord.sourceSystem = docSourceCode;
            attachDocActionRecord.userName = documentXmlObject.getUserId();
            attachDocActionRecord.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar().getTime();
            attachDocActionRecord.dossierState = "";
            attachDocActionRecord.dossierFullArchiveNumber = "";
            attachDocActionRecord.dossierDescription = DossierPersistence.getDossierDescription(dossier);
            attachDocActionRecord.oldDossierDescription = "";

            AuditPersistence.createActionRecord(dfSession, new DfId(passportObjectId), attachDocActionRecord);
            AuditPersistence.createVersionRecord(dfSession, new DfId(passportObjectId), versionRecordDetails);
            
            PSDetailsType psDetails = documentXmlObject.getPSDetails();
           
            Date closeDate = null;
            String closeDesc = null;
            String changeAuthor = null;
            if(psDetails.getCloseDate() != null && psDetails.getCloseDesc() != null) {
            	 closeDate = psDetails.getCloseDate().toGregorianCalendar().getTime();
                 closeDesc = psDetails.getCloseDesc().trim();
                 changeAuthor = documentXmlObject.getUserId();
                 if(changeAuthor == null) {
                	 changeAuthor = "";
                 }
            }
            
            if(closeDate != null && closeDesc != null && closeDesc.length() > 0) {
            	if(DossierPersistence.isOpened(dossier)) {
            		DossierPersistence.closeDossier(dossier, closeDate, changeAuthor);
            	}
            } else {
            	if(DossierPersistence.isClosed(dossier)) {
            		DossierPersistence.reopenDossier(dossier, changeAuthor);
            	}
            }
            

            if (!isTransAlreadyActive) {
                dfSession.commitTrans();
            }

            DfLogger.info(this, "CreateDocumentFromMQType: {0}/{1}. Finish (new ID: {2})", new String[] {docSourceCode, docSourceId, passportObjectId}, null);
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
        return passportObjectId;
    }

    @Override
    public void updateDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyPSType documentXmlObject, String docSourceCode, String docSourceId, IDfId documentId) throws DfException {
        boolean isTransAlreadyActive = dfSession.isTransactionActive();
        try {
            DfLogger.info(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Start", new String[] {docSourceCode, docSourceId, documentId.getId()}, null);

            if (!isTransAlreadyActive) {
                dfSession.beginTrans();
            }

            IDfSysObject passportSysObject = PassportPersistence.lockDocumentForUpdate(dfSession, documentId);
            IDfPersistentObject dossier = null;
            IDfPersistentObject oldDossier = null;
            DossierKeyDetails keyDetailsFromMQ = PassportPersistence.getKeyDetailsFromMQ(documentXmlObject);
            if (!keyDetailsFromMQ.equals(PassportPersistence.getKeyDetails(passportSysObject))) {
                dossier = DossierPersistence.getOrCreateDossierByKeyDetails(dfSession, keyDetailsFromMQ);
                oldDossier = dossier;
            }
            else {
                dossier = dfSession.getObject(PassportPersistence.getDossierId(passportSysObject));
            }

            VersionRecordDetails versionRecordDetails = new VersionRecordDetails();
            versionRecordDetails.eventName = AuditPersistence.UPDATE_BY_METADATA_SAVE_EVENT_NAME;
            versionRecordDetails.sourceSystem = docSourceCode;
            versionRecordDetails.userName = documentXmlObject.getUserId();
            versionRecordDetails.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar().getTime();

            PassportPersistence.saveFieldsToDocument(passportSysObject, documentXmlObject, dossier.getObjectId().getId(), docSourceCode, docSourceId, versionRecordDetails);
            
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
            
            PSDetailsType psDetails = documentXmlObject.getPSDetails();
            
            Date closeDate = null;
            String closeDesc = null;
            String changeAuthor = null;
            if(psDetails.getCloseDate() != null && psDetails.getCloseDesc() != null) {
            	 closeDate = psDetails.getCloseDate().toGregorianCalendar().getTime();
                 closeDesc = psDetails.getCloseDesc().trim();
                 changeAuthor = documentXmlObject.getUserId();
                 if(changeAuthor == null) {
                	 changeAuthor = "";
                 }
            }
            
            if(closeDate != null && closeDesc != null && closeDesc.length() > 0) {
            	if(DossierPersistence.isOpened(dossier)) {
            		DossierPersistence.closeDossier(dossier, closeDate, changeAuthor);
            	}
            } else {
            	if(DossierPersistence.isClosed(dossier)) {
            		DossierPersistence.reopenDossier(dossier, changeAuthor);
            	}
            }

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
