package ru.rb.ccdea.storage.jobs;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.adapters.mq.binding.pd.MCDocInfoModifyPDType;
import ru.rb.ccdea.storage.persistence.ContentPersistence;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.PDPersistence;
import ru.rb.ccdea.storage.services.api.IPDService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.util.Date;
import java.util.List;

public class PDMessageJob extends AbstractJob{
    @Override
    public int execute() throws Exception {
        List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(dfSession, ExternalMessagePersistence.MESSAGE_TYPE_PD, new Date());
        for (IDfId messageId : messageIdList) {
            boolean isTransAlreadyActive = dfSession.isTransactionActive();
            try {

                DfLogger.info(this, "Start MessageID: {0}", new String[] {messageId.getId()}, null);

                if(!ExternalMessagePersistence.beginProcessDocMsg(dfSession, messageId)) {
                	DfLogger.info(this, "Already in process MessageID: {0}", new String[]{messageId.getId()}, null);
    				return 0;
    			}
                
                if (!isTransAlreadyActive) {
                    dfSession.beginTrans();
                }

                IDfSysObject messageSysObject = (IDfSysObject)dfSession.getObject(messageId);

                JAXBContext jc = JAXBContext.newInstance(MCDocInfoModifyPDType.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                MCDocInfoModifyPDType pdXmlObject = unmarshaller.unmarshal(new StreamSource(messageSysObject.getContent()), MCDocInfoModifyPDType.class).getValue();

                IPDService pdService = (IPDService) dfSession.getClient().newService("ucb_ccdea_pd_service", dfSession.getSessionManager());

                String docSourceCode = ExternalMessagePersistence.getDocSourceCode(messageSysObject);
                String docSourceId = ExternalMessagePersistence.getDocSourceId(messageSysObject);

                IDfSysObject pdExistingObject = PDPersistence.searchPDObject(dfSession, docSourceCode, docSourceId);
                ExternalMessagePersistence.startDocProcessing(messageSysObject, pdExistingObject);
                if (pdExistingObject == null) {
                    String pdObjectId = pdService.createDocumentFromMQType(dfSession, pdXmlObject, docSourceCode, docSourceId);
                    String contentSourceCode = ExternalMessagePersistence.getContentSourceCode(messageSysObject);
    				String contentSourceId = ExternalMessagePersistence.getContentSourceId(messageSysObject);
    				
    				for (IDfId contentId : ContentPersistence.getUnlinkedContentIds(dfSession, pdObjectId,
    						docSourceId, docSourceCode, contentSourceId, contentSourceCode)) {
    					try {
    						ContentPersistence.createDocumentContentRelation(dfSession, new DfId(pdObjectId),
    								contentId);
    					} catch (DfException ex) {
    						DfLogger.error(this, "Не удалось присоединить контент '" + contentId + "' к документу '"
    								+ pdObjectId + "' ", null, ex);
    					}
    				}
                    ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[] {pdObjectId});
                }
                else {
                    pdService.updateDocumentFromMQType(dfSession, pdXmlObject, docSourceCode, docSourceId, pdExistingObject.getObjectId());
                    ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[] {pdExistingObject.getObjectId().getId()});
                }

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
        return 0;
    }
}
