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

import ru.rb.ccdea.adapters.mq.binding.svo.MCDocInfoModifySVOType;
import ru.rb.ccdea.adapters.mq.binding.svo.VODetailsType;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.SVOPersistence;
import ru.rb.ccdea.storage.services.api.ISVOService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SVOMessageJob extends AbstractJob{
    @Override
    public int execute() throws Exception {
    	process(dfSession);
        return 0;
    }
    
    public void process(IDfSession dfSession) throws DfException {
    	List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(dfSession, ExternalMessagePersistence.MESSAGE_TYPE_SVO, new Date());
        for (IDfId messageId : messageIdList) {
            
        }
    }
    
    public void process(IDfSession dfSession, IDfId messageId) throws DfException {
    	boolean isTransAlreadyActive = dfSession.isTransactionActive();
        try {

            DfLogger.info(this, "Start MessageID: {0}", new String[]{messageId.getId()}, null);

            if (!isTransAlreadyActive) {
                dfSession.beginTrans();
            }

            IDfSysObject messageSysObject = (IDfSysObject)dfSession.getObject(messageId);

            JAXBContext jc = JAXBContext.newInstance(MCDocInfoModifySVOType.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            MCDocInfoModifySVOType svoXmlObject = unmarshaller.unmarshal(new StreamSource(messageSysObject.getContent()), MCDocInfoModifySVOType.class).getValue();

            ISVOService svoService = (ISVOService) dfSession.getClient().newService("ucb_ccdea_svo_service", dfSession.getSessionManager());

            String docSourceCode = ExternalMessagePersistence.getDocSourceCode(messageSysObject);
            String docSourceId = ExternalMessagePersistence.getDocSourceId(messageSysObject);

            List<VODetailsType> voDetailsList = svoXmlObject.getSVODetails().getVODetails();
            List<String> modifiedDocIdList = new ArrayList<String>();
            ExternalMessagePersistence.startDocProcessing(messageSysObject);
            for (VODetailsType voDetails : voDetailsList) {
                IDfSysObject svoExistingObject = SVOPersistence.searchSVODetailObject(dfSession, docSourceCode, docSourceId, voDetails.getIndex());
                ExternalMessagePersistence.validateModificationVerbForObject(messageSysObject, svoExistingObject);
                if (svoExistingObject == null) {
                    String svoObjectId = svoService.createDocumentFromMQType(dfSession, svoXmlObject, voDetails, docSourceCode, docSourceId);
                    modifiedDocIdList.add(svoObjectId);
                } else {
                    svoService.updateDocumentFromMQType(dfSession, svoXmlObject, voDetails, docSourceCode, docSourceId, svoExistingObject.getObjectId());
                    modifiedDocIdList.add(svoExistingObject.getObjectId().getId());
                }
            }
            ExternalMessagePersistence.finishDocProcessing(messageSysObject, modifiedDocIdList.toArray(new String[modifiedDocIdList.size()]));

            if (!isTransAlreadyActive) {
                dfSession.commitTrans();
            }

            DfLogger.info(this, "Finish MessageID: {0}", new String[] {messageId.getId()}, null);
        }
        catch (DfException dfEx) {
            DfLogger.error(this, "Error MessageID: {0}", new String[] {messageId.getId()}, dfEx);
            //throw dfEx;
        }
        catch (Exception ex) {
            DfLogger.error(this, "Error MessageID: {0}", new String[] {messageId.getId()}, ex);
            //throw new DfException(ex);
        }
        finally {
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
			
			SVOMessageJob job = new SVOMessageJob();
			
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
