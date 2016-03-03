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

import ru.rb.ccdea.adapters.mq.binding.request.MCDocInfoModifyZAType;
import ru.rb.ccdea.storage.persistence.AuditPersistence;
import ru.rb.ccdea.storage.persistence.DossierPersistence;
import ru.rb.ccdea.storage.persistence.RequestPersistence;
import ru.rb.ccdea.storage.persistence.details.ActionRecordDetails;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;
import ru.rb.ccdea.storage.services.api.IRequestService;

/**
 * Реализация сервиса работы с документами "Заявка"
 * 
 * @author SotnikovAV
 *
 */
public class RequestService extends DfService implements IRequestService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.rb.ccdea.storage.services.api.IRequestService#createDocumentFromMQType
	 * (com.documentum.fc.client.IDfSession,
	 * ru.rb.ccdea.adapters.mq.binding.request.MCDocInfoModifyZAType,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String createDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyZAType documentXmlObject,
			String docSourceCode, String docSourceId, String passportNumber) throws DfException {
		String requestObjectId = null;
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			DfLogger.info(this, "CreateDocumentFromMQType: {0}/{1}. Start", new String[] { docSourceCode, docSourceId },
					null);

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			IDfSysObject requestSysObject = RequestPersistence.createDocument(dfSession);
			DossierKeyDetails keyDetailsFromMQ = RequestPersistence.getKeyDetailsFromMQ(documentXmlObject,
					passportNumber);
			IDfPersistentObject dossier = DossierPersistence.getOrCreateDossierByKeyDetails(dfSession,
					keyDetailsFromMQ);

			VersionRecordDetails versionRecordDetails = new VersionRecordDetails();
			versionRecordDetails.eventName = AuditPersistence.UPDATE_BY_METADATA_SAVE_EVENT_NAME;
			versionRecordDetails.sourceSystem = docSourceCode;
			versionRecordDetails.userName = documentXmlObject.getUserId();
			versionRecordDetails.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar()
					.getTime();

			RequestPersistence.saveFieldsToDocument(requestSysObject, documentXmlObject, passportNumber,
					dossier.getObjectId().getId(), docSourceCode, docSourceId, versionRecordDetails);
			requestObjectId = requestSysObject.getObjectId().getId();

			ActionRecordDetails attachDocActionRecord = new ActionRecordDetails();
			attachDocActionRecord.eventName = AuditPersistence.SET_DOC_DOSSIER_EVENT_NAME;
			attachDocActionRecord.sourceSystem = docSourceCode;
			attachDocActionRecord.userName = documentXmlObject.getUserId();
			attachDocActionRecord.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar()
					.getTime();
			attachDocActionRecord.dossierState = "";
			attachDocActionRecord.dossierFullArchiveNumber = "";
			attachDocActionRecord.dossierDescription = DossierPersistence.getDossierDescription(dossier);
			attachDocActionRecord.oldDossierDescription = "";

			AuditPersistence.createActionRecord(dfSession, new DfId(requestObjectId), attachDocActionRecord);
			AuditPersistence.createVersionRecord(dfSession, new DfId(requestObjectId), versionRecordDetails);

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}

			DfLogger.info(this, "CreateDocumentFromMQType: {0}/{1}. Finish (new ID: {2})",
					new String[] { docSourceCode, docSourceId, requestObjectId }, null);
		} catch (DfException dfEx) {
			DfLogger.error(this, "CreateDocumentFromMQType: {0}/{1}. Error",
					new String[] { docSourceCode, docSourceId }, dfEx);
			throw dfEx;
		} catch (Exception ex) {
			DfLogger.error(this, "CreateDocumentFromMQType: {0}/{1}. Error",
					new String[] { docSourceCode, docSourceId }, ex);
			throw new DfException(ex);
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
		return requestObjectId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.rb.ccdea.storage.services.api.IRequestService#createDocumentFromMQType
	 * (com.documentum.fc.client.IDfSession,
	 * ru.rb.ccdea.adapters.mq.binding.request.MCDocInfoModifyZAType,
	 * java.lang.String, java.lang.String, java.lang.String, java.util.Date)
	 */
	@Override
	public String createDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyZAType documentXmlObject,
			String docSourceCode, String docSourceId, String contractNumber, Date contractDate) throws DfException {
		String requestObjectId = null;
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			DfLogger.info(this, "CreateDocumentFromMQType: {0}/{1}. Start", new String[] { docSourceCode, docSourceId },
					null);

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			IDfSysObject requestSysObject = RequestPersistence.createDocument(dfSession);
			DossierKeyDetails keyDetailsFromMQ = RequestPersistence.getKeyDetailsFromMQ(documentXmlObject,
					contractNumber, contractDate);
			IDfPersistentObject dossier = DossierPersistence.getOrCreateDossierByKeyDetails(dfSession,
					keyDetailsFromMQ);

			VersionRecordDetails versionRecordDetails = new VersionRecordDetails();
			versionRecordDetails.eventName = AuditPersistence.UPDATE_BY_METADATA_SAVE_EVENT_NAME;
			versionRecordDetails.sourceSystem = docSourceCode;
			versionRecordDetails.userName = documentXmlObject.getUserId();
			versionRecordDetails.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar()
					.getTime();

			RequestPersistence.saveFieldsToDocument(requestSysObject, documentXmlObject, contractNumber, contractDate,
					dossier.getObjectId().getId(), docSourceCode, docSourceId, versionRecordDetails);
			requestObjectId = requestSysObject.getObjectId().getId();

			ActionRecordDetails attachDocActionRecord = new ActionRecordDetails();
			attachDocActionRecord.eventName = AuditPersistence.SET_DOC_DOSSIER_EVENT_NAME;
			attachDocActionRecord.sourceSystem = docSourceCode;
			attachDocActionRecord.userName = documentXmlObject.getUserId();
			attachDocActionRecord.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar()
					.getTime();
			attachDocActionRecord.dossierState = "";
			attachDocActionRecord.dossierFullArchiveNumber = "";
			attachDocActionRecord.dossierDescription = DossierPersistence.getDossierDescription(dossier);
			attachDocActionRecord.oldDossierDescription = "";

			AuditPersistence.createActionRecord(dfSession, new DfId(requestObjectId), attachDocActionRecord);
			AuditPersistence.createVersionRecord(dfSession, new DfId(requestObjectId), versionRecordDetails);

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}

			DfLogger.info(this, "CreateDocumentFromMQType: {0}/{1}. Finish (new ID: {2})",
					new String[] { docSourceCode, docSourceId, requestObjectId }, null);
		} catch (DfException dfEx) {
			DfLogger.error(this, "CreateDocumentFromMQType: {0}/{1}. Error",
					new String[] { docSourceCode, docSourceId }, dfEx);
			throw dfEx;
		} catch (Exception ex) {
			DfLogger.error(this, "CreateDocumentFromMQType: {0}/{1}. Error",
					new String[] { docSourceCode, docSourceId }, ex);
			throw new DfException(ex);
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
		return requestObjectId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.rb.ccdea.storage.services.api.IRequestService#updateDocumentFromMQType
	 * (com.documentum.fc.client.IDfSession,
	 * ru.rb.ccdea.adapters.mq.binding.request.MCDocInfoModifyZAType,
	 * java.lang.String, java.lang.String, java.lang.String,
	 * com.documentum.fc.common.IDfId)
	 */
	@Override
	public void updateDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyZAType documentXmlObject,
			String docSourceCode, String docSourceId, String passportNumber, IDfId documentId) throws DfException {
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			DfLogger.info(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Start",
					new String[] { docSourceCode, docSourceId, documentId.getId() }, null);

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			IDfSysObject requestSysObject = RequestPersistence.lockDocumentForUpdate(dfSession, documentId);
			IDfPersistentObject dossier = null;
			IDfPersistentObject oldDossier = null;
			DossierKeyDetails keyDetailsFromMQ = RequestPersistence.getKeyDetailsFromMQ(documentXmlObject,
					passportNumber);
			if (!keyDetailsFromMQ.equals(RequestPersistence.getKeyDetails(requestSysObject))) {
				dossier = DossierPersistence.getOrCreateDossierByKeyDetails(dfSession, keyDetailsFromMQ);
			} else {
				dossier = dfSession.getObject(RequestPersistence.getDossierId(requestSysObject));
			}

			VersionRecordDetails versionRecordDetails = new VersionRecordDetails();
			versionRecordDetails.eventName = AuditPersistence.UPDATE_BY_METADATA_SAVE_EVENT_NAME;
			versionRecordDetails.sourceSystem = docSourceCode;
			versionRecordDetails.userName = documentXmlObject.getUserId();
			versionRecordDetails.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar()
					.getTime();

			RequestPersistence.saveFieldsToDocument(requestSysObject, documentXmlObject, passportNumber,
					dossier.getObjectId().getId(), docSourceCode, docSourceId, versionRecordDetails);

			if (oldDossier != null) {
				ActionRecordDetails attachDocActionRecord = new ActionRecordDetails();
				attachDocActionRecord.eventName = AuditPersistence.SET_DOC_DOSSIER_EVENT_NAME;
				attachDocActionRecord.sourceSystem = docSourceCode;
				attachDocActionRecord.userName = documentXmlObject.getUserId();
				attachDocActionRecord.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar()
						.getTime();
				attachDocActionRecord.dossierState = "";
				attachDocActionRecord.dossierFullArchiveNumber = "";
				attachDocActionRecord.dossierDescription = DossierPersistence.getDossierDescription(dossier);
				attachDocActionRecord.oldDossierDescription = DossierPersistence.getDossierDescription(oldDossier);
				;

				AuditPersistence.createActionRecord(dfSession, documentId, attachDocActionRecord);
			}

			AuditPersistence.createVersionRecord(dfSession, documentId, versionRecordDetails);

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}

			DfLogger.info(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Finish",
					new String[] { docSourceCode, docSourceId, documentId.getId() }, null);
		} catch (DfException dfEx) {
			DfLogger.error(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Error",
					new String[] { docSourceCode, docSourceId, documentId.getId() }, dfEx);
			throw dfEx;
		} catch (Exception ex) {
			DfLogger.error(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Error",
					new String[] { docSourceCode, docSourceId, documentId.getId() }, ex);
			throw new DfException(ex);
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.rb.ccdea.storage.services.api.IRequestService#updateDocumentFromMQType
	 * (com.documentum.fc.client.IDfSession,
	 * ru.rb.ccdea.adapters.mq.binding.request.MCDocInfoModifyZAType,
	 * java.lang.String, java.lang.String, java.lang.String, java.util.Date,
	 * com.documentum.fc.common.IDfId)
	 */
	@Override
	public void updateDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyZAType documentXmlObject,
			String docSourceCode, String docSourceId, String contractNumber, Date contractDate, IDfId documentId)
					throws DfException {
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			DfLogger.info(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Start",
					new String[] { docSourceCode, docSourceId, documentId.getId() }, null);

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			IDfSysObject requestSysObject = RequestPersistence.lockDocumentForUpdate(dfSession, documentId);
			IDfPersistentObject dossier = null;
			IDfPersistentObject oldDossier = null;
			DossierKeyDetails keyDetailsFromMQ = RequestPersistence.getKeyDetailsFromMQ(documentXmlObject,
					contractNumber, contractDate);
			if (!keyDetailsFromMQ.equals(RequestPersistence.getKeyDetails(requestSysObject))) {
				dossier = DossierPersistence.getOrCreateDossierByKeyDetails(dfSession, keyDetailsFromMQ);
			} else {
				dossier = dfSession.getObject(RequestPersistence.getDossierId(requestSysObject));
			}

			VersionRecordDetails versionRecordDetails = new VersionRecordDetails();
			versionRecordDetails.eventName = AuditPersistence.UPDATE_BY_METADATA_SAVE_EVENT_NAME;
			versionRecordDetails.sourceSystem = docSourceCode;
			versionRecordDetails.userName = documentXmlObject.getUserId();
			versionRecordDetails.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar()
					.getTime();

			RequestPersistence.saveFieldsToDocument(requestSysObject, documentXmlObject, contractNumber, contractDate,
					dossier.getObjectId().getId(), docSourceCode, docSourceId, versionRecordDetails);

			if (oldDossier != null) {
				ActionRecordDetails attachDocActionRecord = new ActionRecordDetails();
				attachDocActionRecord.eventName = AuditPersistence.SET_DOC_DOSSIER_EVENT_NAME;
				attachDocActionRecord.sourceSystem = docSourceCode;
				attachDocActionRecord.userName = documentXmlObject.getUserId();
				attachDocActionRecord.operationDate = documentXmlObject.getModificationDateTime().toGregorianCalendar()
						.getTime();
				attachDocActionRecord.dossierState = "";
				attachDocActionRecord.dossierFullArchiveNumber = "";
				attachDocActionRecord.dossierDescription = DossierPersistence.getDossierDescription(dossier);
				attachDocActionRecord.oldDossierDescription = DossierPersistence.getDossierDescription(oldDossier);
				;

				AuditPersistence.createActionRecord(dfSession, documentId, attachDocActionRecord);
			}

			AuditPersistence.createVersionRecord(dfSession, documentId, versionRecordDetails);

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}

			DfLogger.info(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Finish",
					new String[] { docSourceCode, docSourceId, documentId.getId() }, null);
		} catch (DfException dfEx) {
			DfLogger.error(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Error",
					new String[] { docSourceCode, docSourceId, documentId.getId() }, dfEx);
			throw dfEx;
		} catch (Exception ex) {
			DfLogger.error(this, "UpdateDocumentFromMQType: {0}/{1}, ID: {2}. Error",
					new String[] { docSourceCode, docSourceId, documentId.getId() }, ex);
			throw new DfException(ex);
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
	}
}
