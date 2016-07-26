package ru.rb.ccdea.storage.jobs;

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

import ru.rb.ccdea.adapters.mq.binding.docput.DocPutType;
import ru.rb.ccdea.storage.persistence.ContentLoaderException;
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
			
			String contentSourceCode = ExternalMessagePersistence.getContentSourceCode(messageSysObject);
			String contentSourceId = ExternalMessagePersistence.getContentSourceId(messageSysObject);

			JAXBContext jc = JAXBContext.newInstance(DocPutType.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			DocPutType docPutXmlObject = unmarshaller
					.unmarshal(new StreamSource(messageSysObject.getContent()), DocPutType.class).getValue();
			
			IContentService contentService = (IContentService) dfSession.getClient()
					.newService("ucb_ccdea_content_service", dfSession.getSessionManager());
			
			contentService.createContentFromMQType(dfSession, contentSourceCode, contentSourceId, docPutXmlObject);

			DfLogger.info(this, "Finish MessageID: {0}", new String[] { messageId.getId() }, null);
		}
		catch (ContentLoaderException ex) {
			DfLogger.warn(this, "Finish MessageID: {0} with " + ex.toString(), new String[] { messageId.getId() }, null);
			try {
				IDfSysObject messageObject = (IDfSysObject) dfSession.getObject(messageId);
				ExternalMessagePersistence.setMessageOnWaiting(messageObject);
			} catch (Exception e) {
				DfLogger.error(this, "Error MessageID: {0}", new String[] { messageId.getId() }, ex);
			}
		}
		catch (CantFindDocException ex) {
			DfLogger.warn(this, "Finish MessageID: {0} " + ex.toString(), new String[] { messageId.getId() }, null);
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
