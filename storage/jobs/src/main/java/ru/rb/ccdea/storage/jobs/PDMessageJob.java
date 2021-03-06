package ru.rb.ccdea.storage.jobs;

import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;

import ru.rb.ccdea.adapters.mq.binding.pd.MCDocInfoModifyPDType;
import ru.rb.ccdea.adapters.mq.binding.pd.ObjectIdentifiersType;
import ru.rb.ccdea.storage.persistence.BaseDocumentPersistence;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.PDPersistence;
import ru.rb.ccdea.storage.services.api.IPDService;

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
                    pdExistingObject = (IDfSysObject)dfSession.getObject(new DfId(pdObjectId));
                    ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[] {pdObjectId});
                }
                else {
                    pdService.updateDocumentFromMQType(dfSession, pdXmlObject, docSourceCode, docSourceId, pdExistingObject.getObjectId());
                    ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[] {pdExistingObject.getObjectId().getId()});
                }
                 
                int index = pdExistingObject.getValueCount(BaseDocumentPersistence.ATTR_RP_CONTENT_SOURCE_ID);
    			for(ObjectIdentifiersType identifiers: pdXmlObject.getOriginIdentification()) {
    				String sourceSystem = identifiers.getSourceSystem();
    				sourceSystem = sourceSystem == null ? "" : sourceSystem.trim();
    				String sourceId = identifiers.getSourceId();
    				sourceId = sourceId == null ? "" : sourceId.trim();
    				index = BaseDocumentPersistence.setSourceIdentifier(pdExistingObject, sourceSystem, sourceId, index);
    			}
    			pdExistingObject.save();

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
