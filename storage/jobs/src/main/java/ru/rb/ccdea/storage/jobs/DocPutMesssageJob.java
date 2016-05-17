package ru.rb.ccdea.storage.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;

import ru.rb.ccdea.adapters.mq.binding.docput.DocPutType;
import ru.rb.ccdea.storage.persistence.BaseDocumentPersistence;
import ru.rb.ccdea.storage.persistence.ContentPersistence;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.services.api.IContentService;

public class DocPutMesssageJob extends AbstractJob {

	@Override
	public int execute() throws Exception {
		process(dfSession);
		return 0;
	}

	public void process(IDfSession dfSession) throws DfException {
		List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(dfSession,
				ExternalMessagePersistence.MESSAGE_TYPE_DOCPUT, new Date());
		for (IDfId messageId : messageIdList) {
			process(dfSession, messageId);
		}
	}

	public void process(IDfSession dfSession, IDfId messageId) throws DfException {
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {

			DfLogger.info(this, "Start MessageID: {0}", new String[] { messageId.getId() }, null);

			
			
			if(!ExternalMessagePersistence.beginProcessContentMsg(dfSession, messageId)) {
				DfLogger.info(this, "Already in process MessageID: {0}", new String[]{messageId.getId()}, null);
				return;
			}

			IDfSysObject messageSysObject = (IDfSysObject) dfSession.getObject(messageId);

			// проверка на дубликаты
			ExternalMessagePersistence.processDuplicateDocPutMessages(messageSysObject);

			JAXBContext jc = JAXBContext.newInstance(DocPutType.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			DocPutType docPutXmlObject = unmarshaller
					.unmarshal(new StreamSource(messageSysObject.getContent()), DocPutType.class).getValue();

			IContentService contentService = (IContentService) dfSession.getClient()
					.newService("ucb_ccdea_content_service", dfSession.getSessionManager());

			boolean placedOnWaiting = ExternalMessagePersistence.checkContentMessageForWaiting(messageSysObject);
			if (!placedOnWaiting) {

				List<IDfSysObject> docMessages = ExternalMessagePersistence.getProcessedDocMessage(messageSysObject);
				if (docMessages.size() == 0) {
					throw new CantFindDocException("Cant find document message for content message: " + messageId);
				}

				String contentSourceCode = null;
				String contentSourceId = null;
				List<IDfSysObject> docs = new ArrayList<IDfSysObject>();
				for (IDfSysObject docMessageObject : docMessages) {
					String docSourceCode = ExternalMessagePersistence.getDocSourceCode(docMessageObject);
					String docSourceId = ExternalMessagePersistence.getDocSourceId(docMessageObject);
					contentSourceCode = ExternalMessagePersistence.getContentSourceCode(messageSysObject);
					contentSourceId = ExternalMessagePersistence.getContentSourceId(messageSysObject);

					docs.addAll(BaseDocumentPersistence.searchDocumentByExternalKey(dfSession, docSourceCode,
							docSourceId, contentSourceCode, contentSourceId));
				}
				if (docs.size() == 0) {
					throw new CantFindDocException("Cant find document for content message: " + messageId);
				}

				Set<IDfId> documentIds = new HashSet<IDfId>(docs.size());
				for (IDfSysObject doc : docs) {
					documentIds.add(doc.getObjectId());
				}

				IDfSysObject documentObject = docs.get(0);

				IDfSysObject existingObject = ContentPersistence.searchContentObjectByDocumentId(dfSession,
						documentObject.getObjectId().getId());
				List<String> modifiedObjectIdList = new ArrayList<String>();
				String ctsRequestId = null;
				if (ContentPersistence.isDocTypeSupportContentVersion(documentObject.getTypeName())) {
					ExternalMessagePersistence.startContentProcessing(messageSysObject, null);
					if (existingObject == null) {
						ctsRequestId = contentService.createContentFromMQType(dfSession, docPutXmlObject.getContent(),
								contentSourceCode, contentSourceId, modifiedObjectIdList, documentIds);
					} else {
						ctsRequestId = contentService.createContentVersionFromMQType(dfSession,
								docPutXmlObject.getContent(), contentSourceCode, contentSourceId, modifiedObjectIdList,
								documentIds, existingObject.getObjectId());
					}
					ExternalMessagePersistence.finishContentProcessing(messageSysObject, modifiedObjectIdList,
							ctsRequestId);
				} else if (ContentPersistence.isDocTypeSupportContentAppending(documentObject.getTypeName())) {
					ExternalMessagePersistence.startContentProcessing(messageSysObject, existingObject);
					if (existingObject == null) {
						ctsRequestId = contentService.createContentFromMQType(dfSession, docPutXmlObject.getContent(),
								contentSourceCode, contentSourceId, modifiedObjectIdList, documentIds);
					} else {
						ctsRequestId = contentService.appendContentFromMQType(dfSession, docPutXmlObject.getContent(),
								contentSourceCode, contentSourceId, modifiedObjectIdList, documentIds,
								existingObject.getObjectId());
					}
					ExternalMessagePersistence.finishContentProcessing(messageSysObject, modifiedObjectIdList,
							ctsRequestId);
				} else {
					ExternalMessagePersistence.startContentProcessing(messageSysObject, existingObject);
					if (existingObject == null) {
						ctsRequestId = contentService.createContentFromMQType(dfSession, docPutXmlObject.getContent(),
								contentSourceCode, contentSourceId, modifiedObjectIdList, documentIds);
					} else {
						ctsRequestId = contentService.updateContentFromMQType(dfSession, docPutXmlObject.getContent(),
								contentSourceCode, contentSourceId, modifiedObjectIdList, documentIds,
								existingObject.getObjectId());
					}
					ExternalMessagePersistence.finishContentProcessing(messageSysObject, modifiedObjectIdList,
							ctsRequestId);
				}
			}

//			if (!isTransAlreadyActive) {
//				dfSession.commitTrans();
//			}

			DfLogger.info(this, "Finish MessageID: {0}", new String[] { messageId.getId() }, null);
		} catch (CantFindDocException ex) {
			DfLogger.warn(this, "Finish MessageID: {0}", new String[] { messageId.getId() }, null);
			try {
				IDfSysObject messageObject = (IDfSysObject) dfSession.getObject(messageId);
				ExternalMessagePersistence.setMessageOnWaiting(messageObject);
			} catch (Exception e) {
				DfLogger.error(this, "Error MessageID: {0}", new String[] { messageId.getId() }, ex);
			}
		} catch (Exception ex) {
			DfLogger.error(this, "Error MessageID: {0}", new String[] { messageId.getId() }, ex);
			try {
				IDfSysObject messageObject = (IDfSysObject) dfSession.getObject(messageId);
				ExternalMessagePersistence.setConvertationError(messageObject);
			} catch (Exception e) {
				DfLogger.error(this, "Error MessageID: {0}", new String[] { messageId.getId() }, ex);
			}
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}

	}

	public static void main(String[] args) {
		IDfSession testSession = null;
		IDfClientX clientx = new DfClientX();
		IDfClient client = null;
		IDfSessionManager sessionManager = null;
		try {
			client = clientx.getLocalClient();
			sessionManager = client.newSessionManager();

			IDfLoginInfo loginInfo = clientx.getLoginInfo();
			loginInfo.setUser("dmadmin");
			loginInfo.setPassword("Fkut,hf15");
			loginInfo.setDomain(null);

			sessionManager.setIdentity("ELAR", loginInfo);
			testSession = sessionManager.getSession("ELAR");

			DocPutMesssageJob job = new DocPutMesssageJob();

			String messageIdStr = null;
			if (args != null && args.length > 0) {
				messageIdStr = args[0];
			}

			IDfId messageId = null;
			if (messageIdStr != null) {
				messageId = new DfId(messageIdStr);
			}
			
			if (messageId != null && !messageId.isNull() && messageId.isObjectId()) {
				job.process(testSession, messageId);
			} else {
				job.process(testSession);
			}
		} catch (Exception ex) {
			System.out.println(ex);
		} finally {
			if (testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}
}
