package ru.rb.ccdea.storage.jobs;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
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
        List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(dfSession, ExternalMessagePersistence.MESSAGE_TYPE_SVO, new Date());
        for (IDfId messageId : messageIdList) {
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
        return 0;
    }
}
