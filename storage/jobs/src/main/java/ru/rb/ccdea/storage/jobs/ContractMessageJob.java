package ru.rb.ccdea.storage.jobs;

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

import ru.rb.ccdea.adapters.mq.binding.contract.MCDocInfoModifyContractType;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.ContractPersistence;
import ru.rb.ccdea.storage.services.api.IContractService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.util.Date;
import java.util.List;

public class ContractMessageJob extends AbstractJob {
	@Override
	public int execute() throws Exception {
		process(dfSession);
		return 0;
	}

	public void process(IDfSession dfSession) throws Exception {
		List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(dfSession,
				ExternalMessagePersistence.MESSAGE_TYPE_CONTRACT, new Date());
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

			JAXBContext jc = JAXBContext.newInstance(MCDocInfoModifyContractType.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			MCDocInfoModifyContractType contractXmlObject = unmarshaller
					.unmarshal(new StreamSource(messageSysObject.getContent()), MCDocInfoModifyContractType.class)
					.getValue();

			IContractService contractService = (IContractService) dfSession.getClient()
					.newService("ucb_ccdea_contract_service", dfSession.getSessionManager());

			String docSourceCode = ExternalMessagePersistence.getDocSourceCode(messageSysObject);
			String docSourceId = ExternalMessagePersistence.getDocSourceId(messageSysObject);

			IDfSysObject contractExistingObject = ContractPersistence.searchContractObject(dfSession, docSourceCode,
					docSourceId);
			ExternalMessagePersistence.startDocProcessing(messageSysObject, contractExistingObject);
			String contractObjectId = DfId.DF_NULLID_STR;
			if (contractExistingObject == null) {
				contractObjectId = contractService.createDocumentFromMQType(dfSession, contractXmlObject, docSourceCode,
						docSourceId);
				ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[] { contractObjectId });
			} else {
				contractObjectId = contractExistingObject.getObjectId().getId();
				contractService.updateDocumentFromMQType(dfSession, contractXmlObject, docSourceCode, docSourceId,
						contractExistingObject.getObjectId());
				ExternalMessagePersistence.finishDocProcessing(messageSysObject,
						new String[] { contractExistingObject.getObjectId().getId() });
			}

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}

			DfLogger.info(this, "Finish MessageID: {0}", new String[] { messageId.getId() }, null);
			return contractObjectId;
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

			ContractMessageJob job = new ContractMessageJob();

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
