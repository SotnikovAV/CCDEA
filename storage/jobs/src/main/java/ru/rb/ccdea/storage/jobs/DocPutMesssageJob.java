package ru.rb.ccdea.storage.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	
	private void process(IDfSession dfSession) throws DfException {
		List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(dfSession,
				ExternalMessagePersistence.MESSAGE_TYPE_DOCPUT, new Date());
		for (IDfId messageId : messageIdList) {
			boolean isTransAlreadyActive = dfSession.isTransactionActive();
			try {

				DfLogger.info(this, "Start MessageID: {0}", new String[] { messageId.getId() }, null);

				if (!isTransAlreadyActive) {
					dfSession.beginTrans();
				}

				IDfSysObject messageSysObject = (IDfSysObject) dfSession.getObject(messageId);

				JAXBContext jc = JAXBContext.newInstance(DocPutType.class);
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				DocPutType docPutXmlObject = unmarshaller
						.unmarshal(new StreamSource(messageSysObject.getContent()), DocPutType.class).getValue();

				IContentService contentService = (IContentService) dfSession.getClient()
						.newService("ucb_ccdea_content_service", dfSession.getSessionManager());

				boolean placedOnWaiting = ExternalMessagePersistence.checkContentMessageForWaiting(messageSysObject);
				if (!placedOnWaiting) {
					
					IDfSysObject docMessageObject = ExternalMessagePersistence.getProcessedDocMessage(messageSysObject);
					if(docMessageObject == null) {
						throw new DfException("Cant find document for content message: " + messageId);
					}
					
					String docSourceCode = ExternalMessagePersistence.getDocSourceCode(docMessageObject);
					String docSourceId = ExternalMessagePersistence.getDocSourceId(docMessageObject);
					String contentSourceCode = ExternalMessagePersistence.getContentSourceCode(messageSysObject);
					String contentSourceId = ExternalMessagePersistence.getContentSourceId(messageSysObject);
								

					List<IDfSysObject> docs = BaseDocumentPersistence.searchDocumentByExternalKey(dfSession,
							docSourceCode, docSourceId, contentSourceCode, contentSourceId);

					if (docs.size() == 0) {
						throw new DfException("Cant find document for content message: " + messageId);
					}

					List<IDfId> documentIds = new ArrayList<IDfId>(docs.size());
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
							ctsRequestId = contentService.createContentFromMQType(dfSession,
									docPutXmlObject.getContent(), contentSourceCode, contentSourceId,
									modifiedObjectIdList, documentIds);
						} else {
							ctsRequestId = contentService.createContentVersionFromMQType(dfSession,
									docPutXmlObject.getContent(), contentSourceCode, contentSourceId,
									modifiedObjectIdList, documentObject.getObjectId(), existingObject.getObjectId());
						}
						ExternalMessagePersistence.finishContentProcessing(messageSysObject, modifiedObjectIdList,
								ctsRequestId);
					} else if (ContentPersistence.isDocTypeSupportContentAppending(documentObject.getTypeName())) {
						ExternalMessagePersistence.startContentProcessing(messageSysObject, existingObject);
						if (existingObject == null) {
							ctsRequestId = contentService.createContentFromMQType(dfSession,
									docPutXmlObject.getContent(), contentSourceCode, contentSourceId,
									modifiedObjectIdList, documentIds);
						} else {
							ctsRequestId = contentService.appendContentFromMQType(dfSession,
									docPutXmlObject.getContent(), contentSourceCode, contentSourceId,
									modifiedObjectIdList, documentObject.getObjectId(), existingObject.getObjectId());
						}
						ExternalMessagePersistence.finishContentProcessing(messageSysObject, modifiedObjectIdList,
								ctsRequestId);
					} else {
						ExternalMessagePersistence.startContentProcessing(messageSysObject, existingObject);
						if (existingObject == null) {
							ctsRequestId = contentService.createContentFromMQType(dfSession,
									docPutXmlObject.getContent(), contentSourceCode, contentSourceId,
									modifiedObjectIdList, documentIds);
						} else {
							ctsRequestId = contentService.updateContentFromMQType(dfSession,
									docPutXmlObject.getContent(), contentSourceCode, contentSourceId,
									modifiedObjectIdList, documentObject.getObjectId(), existingObject.getObjectId());
						}
						ExternalMessagePersistence.finishContentProcessing(messageSysObject, modifiedObjectIdList,
								ctsRequestId);
					}
				}

				if (!isTransAlreadyActive) {
					dfSession.commitTrans();
				}

				DfLogger.info(this, "Finish MessageID: {0}", new String[] { messageId.getId() }, null);
			} catch (DfException dfEx) {
				DfLogger.error(this, "Error MessageID: {0}", new String[] { messageId.getId() }, dfEx);
				// throw dfEx;
			} catch (Exception ex) {
				DfLogger.error(this, "Error MessageID: {0}", new String[] { messageId.getId() }, ex);
				// throw new DfException(ex);
			} finally {
				if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
					dfSession.abortTrans();
				}
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
			loginInfo.setPassword("dmadmin");
			loginInfo.setDomain(null);

			sessionManager.setIdentity("UCB", loginInfo);
			testSession = sessionManager.getSession("UCB");
			
			new DocPutMesssageJob().process(testSession);

		} catch (Exception ex) {
			System.out.println(ex);
		} finally {
			if (testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}
}
