package ru.rb.ccdea.storage.jobs;

import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;

import ru.rb.ccdea.adapters.mq.binding.passport.MCDocInfoModifyPSType;
import ru.rb.ccdea.storage.persistence.ContentPersistence;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.PassportPersistence;
import ru.rb.ccdea.storage.services.api.IPassportService;

public class PassportMessageJob extends AbstractJob {
	@Override
	public int execute() throws Exception {
		List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(dfSession,
				ExternalMessagePersistence.MESSAGE_TYPE_PS, new Date());
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
		return 0;
	}

	public String process(IDfSession dfSession, IDfId messageId) throws Exception {
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {

			DfLogger.info(this, "Start MessageID: {0}", new String[] { messageId.getId() }, null);

			String passportObjectId = DfId.DF_NULLID_STR;
			if (!ExternalMessagePersistence.beginProcessDocMsg(dfSession, messageId)) {
				DfLogger.info(this, "Already in process MessageID: {0}", new String[] { messageId.getId() }, null);
				return passportObjectId;
			}

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			IDfSysObject messageSysObject = (IDfSysObject) dfSession.getObject(messageId);

			JAXBContext jc = JAXBContext.newInstance(MCDocInfoModifyPSType.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			MCDocInfoModifyPSType passportXmlObject = unmarshaller
					.unmarshal(new StreamSource(messageSysObject.getContent()), MCDocInfoModifyPSType.class).getValue();

			IPassportService passportService = (IPassportService) dfSession.getClient()
					.newService("ucb_ccdea_passport_service", dfSession.getSessionManager());

			String docSourceCode = ExternalMessagePersistence.getDocSourceCode(messageSysObject);
			String docSourceId = ExternalMessagePersistence.getDocSourceId(messageSysObject);

			IDfSysObject passportExistingObject = PassportPersistence.searchPassportObject(dfSession,
					passportXmlObject.getPSDetails().getDealPassport());
			ExternalMessagePersistence.startDocProcessing(messageSysObject, passportExistingObject);
			if (passportExistingObject == null) {
				passportObjectId = passportService.createDocumentFromMQType(dfSession, passportXmlObject, docSourceCode,
						docSourceId);
				String contentSourceCode = ExternalMessagePersistence.getContentSourceCode(messageSysObject);
				String contentSourceId = ExternalMessagePersistence.getContentSourceId(messageSysObject);
				
				for (IDfId contentId : ContentPersistence.getUnlinkedContentIds(dfSession, passportObjectId,
						docSourceId, docSourceCode, contentSourceId, contentSourceCode)) {
					try {
						ContentPersistence.createDocumentContentRelation(dfSession, new DfId(passportObjectId),
								contentId);
					} catch (DfException ex) {
						DfLogger.error(this, "Не удалось присоединить контент '" + contentId + "' к документу '"
								+ passportObjectId + "' ", null, ex);
					}
				}
				ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[] { passportObjectId });
			} else {
				passportObjectId = passportExistingObject.getObjectId().getId();
				passportService.updateDocumentFromMQType(dfSession, passportXmlObject, docSourceCode, docSourceId,
						passportExistingObject.getObjectId());
				ExternalMessagePersistence.finishDocProcessing(messageSysObject,
						new String[] { passportExistingObject.getObjectId().getId() });
			}

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}

			DfLogger.info(this, "Finish MessageID: {0}", new String[] { messageId.getId() }, null);

			return passportObjectId;
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
	}
}
