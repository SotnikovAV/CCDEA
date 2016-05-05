package ru.rb.ccdea.storage.jobs;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.adapters.mq.binding.spd.MCDocInfoModifySPDType;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.SPDPersistence;
import ru.rb.ccdea.storage.services.api.ISPDService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.util.Date;
import java.util.List;

public class SPDMessageJob extends AbstractJob{
    @Override
    public int execute() throws Exception {
        List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(dfSession, ExternalMessagePersistence.MESSAGE_TYPE_SPD, new Date());
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

                JAXBContext jc = JAXBContext.newInstance(MCDocInfoModifySPDType.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                MCDocInfoModifySPDType spdXmlObject = unmarshaller.unmarshal(new StreamSource(messageSysObject.getContent()), MCDocInfoModifySPDType.class).getValue();

                ISPDService spdService = (ISPDService) dfSession.getClient().newService("ucb_ccdea_spd_service", dfSession.getSessionManager());

                String docSourceCode = ExternalMessagePersistence.getDocSourceCode(messageSysObject);
                String docSourceId = ExternalMessagePersistence.getDocSourceId(messageSysObject);

                IDfSysObject spdExistingObject = SPDPersistence.searchSPDObject(dfSession, docSourceCode, docSourceId);
                ExternalMessagePersistence.startDocProcessing(messageSysObject, spdExistingObject);
                if (spdExistingObject == null) {
                    String spdObjectId = spdService.createDocumentFromMQType(dfSession, spdXmlObject, docSourceCode, docSourceId);
                    ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[] {spdObjectId});
                }
                else {
                    spdService.updateDocumentFromMQType(dfSession, spdXmlObject, docSourceCode, docSourceId, spdExistingObject.getObjectId());
                    ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[] {spdExistingObject.getObjectId().getId()});
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
