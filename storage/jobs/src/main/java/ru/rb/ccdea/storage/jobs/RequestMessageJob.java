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
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;

import ru.rb.ccdea.adapters.mq.binding.request.MCDocInfoModifyZAType;
import ru.rb.ccdea.adapters.mq.binding.request.ObjectIdentifiersType;
import ru.rb.ccdea.storage.persistence.BaseDocumentPersistence;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.RequestPersistence;
import ru.rb.ccdea.storage.services.api.IRequestService;

public class RequestMessageJob extends AbstractJob {
	@Override
	public int execute() throws Exception {
		process(dfSession);
		return 0;
	}

	private void process(IDfSession dfSession) throws DfException {
		List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(dfSession,
				ExternalMessagePersistence.MESSAGE_TYPE_ZA, new Date());
		for (IDfId messageId : messageIdList) {
			try {
				process(dfSession, messageId);
			} catch (DfException dfEx) {
				DfLogger.error(this, "Error MessageID: {0}", new String[] { messageId.getId() }, dfEx);
				// throw dfEx;
			} catch (Exception ex) {
				DfLogger.error(this, "Error MessageID: {0}", new String[] { messageId.getId() }, ex);
				// throw new DfException(ex);
			}
		}
	}
	
	public String process(IDfSession dfSession, IDfId messageId) throws Exception {
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {

			DfLogger.info(this, "Start MessageID: {0}", new String[] { messageId.getId() }, null);

			if(!ExternalMessagePersistence.beginProcessDocMsg(dfSession, messageId)) {
				DfLogger.info(this, "Already in process MessageID: {0}", new String[]{messageId.getId()}, null);
				return DfId.DF_NULLID_STR;
			}
			
			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			IDfSysObject messageSysObject = (IDfSysObject) dfSession.getObject(messageId);

			JAXBContext jc = JAXBContext.newInstance(MCDocInfoModifyZAType.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			MCDocInfoModifyZAType requestXmlObject = unmarshaller
					.unmarshal(new StreamSource(messageSysObject.getContent()), MCDocInfoModifyZAType.class)
					.getValue();

			IRequestService requestService = (IRequestService) dfSession.getClient()
					.newService("ucb_ccdea_request_service", dfSession.getSessionManager());

			String docSourceCode = ExternalMessagePersistence.getDocSourceCode(messageSysObject);
			String docSourceId = ExternalMessagePersistence.getDocSourceId(messageSysObject);

			List<String> passportNumberList = requestXmlObject.getZADetails().getDealPassport();
			List<String> modifiedDocIdList = new ArrayList<String>();
			ExternalMessagePersistence.startDocProcessing(messageSysObject);
			String objectId = DfId.DF_NULLID_STR;
			
			for (String passportNumber : passportNumberList) {
				IDfSysObject requestExistingObject = RequestPersistence.searchRequestObject(dfSession,
						docSourceCode, docSourceId, passportNumber);
				ExternalMessagePersistence.validateModificationVerbForObject(messageSysObject,
						requestExistingObject);
				if (requestExistingObject == null) {
					String requestObjectId = requestService.createDocumentFromMQType(dfSession, requestXmlObject,
							docSourceCode, docSourceId, passportNumber);
					requestExistingObject = (IDfSysObject) dfSession.getObject(new DfId(requestObjectId));
					modifiedDocIdList.add(requestObjectId);
				} else {
					requestService.updateDocumentFromMQType(dfSession, requestXmlObject, docSourceCode, docSourceId,
							passportNumber, requestExistingObject.getObjectId());
					modifiedDocIdList.add(requestExistingObject.getObjectId().getId());
				}
				
				int index = requestExistingObject.getValueCount(BaseDocumentPersistence.ATTR_RP_CONTENT_SOURCE_ID);
    			for(ObjectIdentifiersType identifiers: requestXmlObject.getOriginIdentification()) {
    				String sourceSystem = identifiers.getSourceSystem();
    				sourceSystem = sourceSystem == null ? "" : sourceSystem.trim();
    				String sourceId = identifiers.getSourceId();
    				sourceId = sourceId == null ? "" : sourceId.trim();
    				index = BaseDocumentPersistence.setSourceIdentifier(requestExistingObject, sourceSystem, sourceId, index);
    			}
    			requestExistingObject.save();
    			objectId = requestExistingObject.getObjectId().getId();
			}
			if (requestXmlObject.getZADetails().getContractDetails() != null
					&& requestXmlObject.getZADetails().getContractDetails().getContractNumber() != null
					&& !"".equals(requestXmlObject.getZADetails().getContractDetails().getContractNumber())
					&& requestXmlObject.getZADetails().getContractDetails().getContractDate() != null) {

				String contractNumber = requestXmlObject.getZADetails().getContractDetails().getContractNumber();
				Date contractDate = requestXmlObject.getZADetails().getContractDetails().getContractDate()
						.toGregorianCalendar().getTime();

				IDfSysObject requestExistingObject = RequestPersistence.searchRequestObject(dfSession,
						docSourceCode, docSourceId, contractNumber, contractDate);
				ExternalMessagePersistence.validateModificationVerbForObject(messageSysObject,
						requestExistingObject);
				if (requestExistingObject == null) {
					String requestObjectId = requestService.createDocumentFromMQType(dfSession, requestXmlObject,
							docSourceCode, docSourceId, contractNumber, contractDate);
					requestExistingObject = (IDfSysObject)dfSession.getObject(new DfId(requestObjectId));
					modifiedDocIdList.add(requestObjectId);
				} else {
					requestService.updateDocumentFromMQType(dfSession, requestXmlObject, docSourceCode, docSourceId,
							contractNumber, contractDate, requestExistingObject.getObjectId());
					modifiedDocIdList.add(requestExistingObject.getObjectId().getId());
				}
				
				int index = requestExistingObject.getValueCount(BaseDocumentPersistence.ATTR_RP_CONTENT_SOURCE_ID);
    			for(ObjectIdentifiersType identifiers: requestXmlObject.getOriginIdentification()) {
    				String sourceSystem = identifiers.getSourceSystem();
    				sourceSystem = sourceSystem == null ? "" : sourceSystem.trim();
    				String sourceId = identifiers.getSourceId();
    				sourceId = sourceId == null ? "" : sourceId.trim();
    				index = BaseDocumentPersistence.setSourceIdentifier(requestExistingObject, sourceSystem, sourceId, index);
    			}
    			requestExistingObject.save();
    			objectId = requestExistingObject.getObjectId().getId();
			}

			ExternalMessagePersistence.finishDocProcessing(messageSysObject,
					modifiedDocIdList.toArray(new String[modifiedDocIdList.size()]));

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}

			DfLogger.info(this, "Finish MessageID: {0}", new String[] { messageId.getId() }, null);
			
			return objectId;
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
			loginInfo.setPassword("dmadmin");
			loginInfo.setDomain(null);

			sessionManager.setIdentity("UCB", loginInfo);
			testSession = sessionManager.getSession("UCB");

			RequestMessageJob job = new RequestMessageJob();

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
			ex.printStackTrace();
		} finally {
			if (testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}
}
