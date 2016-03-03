package ru.rb.ccdea.storage.services.impl;

import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.storage.persistence.*;
import ru.rb.ccdea.storage.persistence.details.ActionRecordDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;
import ru.rb.ccdea.storage.services.api.IDossierService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DossierService extends DfService implements IDossierService {
    @Override
    public void closeDossier(IDfSession dfSession, IDfId dossierId) throws DfException {
        boolean isTransAlreadyActive = dfSession.isTransactionActive();
        try {
            if (!isTransAlreadyActive) {
                dfSession.beginTrans();
            }

            IDfPersistentObject dossier = dfSession.getObject(dossierId);
            DossierPersistence.checkDossierForClosing(dossier);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int year = calendar.get(Calendar.YEAR);
       
            String dossierPrefix = DossierPersistence.getDossierPrefix(dossier);
            int freeNumber = DossierPersistence.searchFirstFreeNumber(dfSession, year, dossierPrefix);
                        
            String fullArchiveNumber = DossierPersistence.closeDossierWithNumber(dossier, dossierPrefix, freeNumber, year);

            ActionRecordDetails closeDossierActionRecord = new ActionRecordDetails();
            closeDossierActionRecord.eventName = AuditPersistence.CLOSE_DOSSIER_EVENT_NAME;
            closeDossierActionRecord.sourceSystem = BasePersistence.SOURCE_SYSTEM_CCDEA;
            closeDossierActionRecord.userName = dfSession.getLoginUserName();
            closeDossierActionRecord.operationDate = new Date();
            closeDossierActionRecord.dossierState = DossierPersistence.STATE_CLOSED;
            closeDossierActionRecord.dossierFullArchiveNumber = fullArchiveNumber;
            closeDossierActionRecord.dossierDescription = DossierPersistence.getDossierDescription(dossier);
            closeDossierActionRecord.oldDossierDescription = "";

            List<IDfId> dossierDocumentIds = BaseDocumentPersistence.searchDocumentsByDossier(dfSession, dossier.getObjectId().getId());
            for (IDfId documentId : dossierDocumentIds) {
                AuditPersistence.createActionRecord(dfSession, documentId, closeDossierActionRecord);
            }

            if (!isTransAlreadyActive) {
                dfSession.commitTrans();
            }
        }
        catch (DfException dfEx) {
            throw dfEx;
        }
        catch (Exception ex) {
            throw new DfException(ex);
        }
        finally {
            if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
                dfSession.abortTrans();
            }
        }
    }

    @Override
    public void reopenDossier(IDfSession dfSession, IDfId dossierId) throws DfException {
        boolean isTransAlreadyActive = dfSession.isTransactionActive();
        try {
            if (!isTransAlreadyActive) {
                dfSession.beginTrans();
            }

            IDfPersistentObject dossier = dfSession.getObject(dossierId);
            DossierPersistence.checkDossierForReopen(dossier);
            
            DossierPersistence.reopenDossier(dossier);

            ActionRecordDetails reopenDossierActionRecord = new ActionRecordDetails();
            reopenDossierActionRecord.eventName = AuditPersistence.REOPEN_DOSSIER_EVENT_NAME;
            reopenDossierActionRecord.sourceSystem = BasePersistence.SOURCE_SYSTEM_CCDEA;
            reopenDossierActionRecord.userName = dfSession.getLoginUserName();
            reopenDossierActionRecord.operationDate = new Date();
            reopenDossierActionRecord.dossierState = DossierPersistence.STATE_OPENED;
            reopenDossierActionRecord.dossierFullArchiveNumber = "";
            reopenDossierActionRecord.dossierDescription = DossierPersistence.getDossierDescription(dossier);
            reopenDossierActionRecord.oldDossierDescription = "";

            List<IDfId> dossierDocumentIds = BaseDocumentPersistence.searchDocumentsByDossier(dfSession, dossier.getObjectId().getId());
            for (IDfId documentId : dossierDocumentIds) {
                AuditPersistence.createActionRecord(dfSession, documentId, reopenDossierActionRecord);
            }

            if (!isTransAlreadyActive) {
                dfSession.commitTrans();
            }
        }
        catch (DfException dfEx) {
            throw dfEx;
        }
        catch (Exception ex) {
            throw new DfException(ex);
        }
        finally {
            if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
                dfSession.abortTrans();
            }
        }
    }

    @Override
    public void attachDoc(IDfSession dfSession, IDfId dossierId, IDfId docId) throws DfException {
        boolean isTransAlreadyActive = dfSession.isTransactionActive();
        try {
            if (!isTransAlreadyActive) {
                dfSession.beginTrans();
            }

            IDfPersistentObject dossier = dfSession.getObject(dossierId);
            DossierPersistence.checkDossierForAttachDoc(dossier);

            IDfPersistentObject document = dfSession.getObject(docId);
            IDfPersistentObject oldDossier = BaseDocumentPersistence.lockDocumentForAttach(document);

            if (oldDossier != null) {
                if (oldDossier.getObjectId().equals(dossierId)) {
                    throw new DfException("Document is already attached to dossier: " + dossierId.getId());
                }
                if (DossierPersistence.isDossierTypeDoc(dossier, docId)) {
                    throw new DfException("Document is DossierTypeDoc for current dossier: " + dossierId.getId());
                }
            }

            VersionRecordDetails versionRecordDetails = new VersionRecordDetails();
            versionRecordDetails.eventName = AuditPersistence.UPDATE_BY_DOSSIER_SET_EVENT_NAME;
            versionRecordDetails.sourceSystem = BasePersistence.SOURCE_SYSTEM_CCDEA;
            versionRecordDetails.userName = dfSession.getLoginUserName();
            versionRecordDetails.operationDate = new Date();

            BaseDocumentPersistence.saveDossierKeysToDocument(document, DossierPersistence.getKeyDetails(dossier), versionRecordDetails);

            ActionRecordDetails attachDocActionRecord = new ActionRecordDetails();
            attachDocActionRecord.eventName = AuditPersistence.SET_DOC_DOSSIER_EVENT_NAME;
            attachDocActionRecord.sourceSystem = BasePersistence.SOURCE_SYSTEM_CCDEA;
            attachDocActionRecord.userName = dfSession.getLoginUserName();
            attachDocActionRecord.operationDate = versionRecordDetails.operationDate;
            attachDocActionRecord.dossierState = "";
            attachDocActionRecord.dossierFullArchiveNumber = "";
            attachDocActionRecord.dossierDescription = DossierPersistence.getDossierDescription(dossier);
            attachDocActionRecord.oldDossierDescription = DossierPersistence.getDossierDescription(oldDossier);

            AuditPersistence.createActionRecord(dfSession, docId, attachDocActionRecord);
            AuditPersistence.createVersionRecord(dfSession, docId, versionRecordDetails);

            if (!isTransAlreadyActive) {
                dfSession.commitTrans();
            }
        }
        catch (DfException dfEx) {
            throw dfEx;
        }
        catch (Exception ex) {
            throw new DfException(ex);
        }
        finally {
            if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
                dfSession.abortTrans();
            }
        }
    }
}
