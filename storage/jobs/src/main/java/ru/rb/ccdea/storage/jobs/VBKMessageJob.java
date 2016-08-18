package ru.rb.ccdea.storage.jobs;

import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;

import ru.rb.ccdea.adapters.mq.binding.vbk.MCDocInfoModifyVBKType;
import ru.rb.ccdea.adapters.mq.binding.vbk.ObjectIdentifiersType;
import ru.rb.ccdea.storage.persistence.BaseDocumentPersistence;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.VBKPersistence;
import ru.rb.ccdea.storage.services.api.IVBKService;

public class VBKMessageJob extends AbstractJob {
    @Override
    public int execute() throws Exception {
        List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(dfSession, ExternalMessagePersistence.MESSAGE_TYPE_VBK, new Date());
        for (IDfId messageId : messageIdList) {
            boolean isTransAlreadyActive = dfSession.isTransactionActive();
            try {

                DfLogger.info(this, "Start MessageID: {0}", new String[]{messageId.getId()}, null);

                if(!ExternalMessagePersistence.beginProcessDocMsg(dfSession, messageId)) {
                	DfLogger.info(this, "Already in process MessageID: {0}", new String[]{messageId.getId()}, null);
    				return 0;
    			}
                
                if (!isTransAlreadyActive) {
                    dfSession.beginTrans();
                }

                IDfSysObject messageSysObject = (IDfSysObject)dfSession.getObject(messageId);

                JAXBContext jc = JAXBContext.newInstance(MCDocInfoModifyVBKType.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                MCDocInfoModifyVBKType vbkXmlObject = unmarshaller.unmarshal(new StreamSource(messageSysObject.getContent()), MCDocInfoModifyVBKType.class).getValue();

                IVBKService vbkService = (IVBKService) dfSession.getClient().newService("ucb_ccdea_vbk_service", dfSession.getSessionManager());

                String docSourceCode = ExternalMessagePersistence.getDocSourceCode(messageSysObject);
                String docSourceId = ExternalMessagePersistence.getDocSourceId(messageSysObject);

                IDfSysObject vbkExistingObject = VBKPersistence.searchVBKObject(dfSession, vbkXmlObject.getVBKDetails().getDealPassport());
                ExternalMessagePersistence.startDocProcessing(messageSysObject, vbkExistingObject);
                if (vbkExistingObject == null) {
                    String vbkObjectId = vbkService.createDocumentFromMQType(dfSession, vbkXmlObject, docSourceCode, docSourceId);
                    
                    vbkExistingObject = (IDfSysObject)dfSession.getObject(new DfId(vbkObjectId));
                    
                    ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[] {vbkObjectId});
                }
                else {
                    vbkService.updateDocumentFromMQType(dfSession, vbkXmlObject, docSourceCode, docSourceId, vbkExistingObject.getObjectId());
                    ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[] {vbkExistingObject.getObjectId().getId()});
                }
                
                int index = vbkExistingObject.getValueCount(BaseDocumentPersistence.ATTR_RP_CONTENT_SOURCE_ID);
    			for(ObjectIdentifiersType identifiers: vbkXmlObject.getOriginIdentification()) {
    				String sourceSystem = StringUtils.trimToEmpty(identifiers.getSourceSystem());
    				String sourceId = StringUtils.trimToEmpty(identifiers.getSourceId());
    				
    				index = BaseDocumentPersistence.setSourceIdentifier(vbkExistingObject, sourceSystem, sourceId, index);
    			}
    			vbkExistingObject.save();

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
