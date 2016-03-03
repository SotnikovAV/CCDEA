package ru.rb.ccdea.storage.jobs;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.adapters.mq.binding.passport.MCDocInfoModifyPSType;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.PassportPersistence;
import ru.rb.ccdea.storage.services.api.IPassportService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.util.Date;
import java.util.List;

public class PassportMessageJob extends AbstractJob{
    @Override
    public int execute() throws Exception {
        List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(dfSession, ExternalMessagePersistence.MESSAGE_TYPE_PS, new Date());
        for (IDfId messageId : messageIdList) {
            boolean isTransAlreadyActive = dfSession.isTransactionActive();
            try {

                DfLogger.info(this, "Start MessageID: {0}", new String[] {messageId.getId()}, null);

                if (!isTransAlreadyActive) {
                    dfSession.beginTrans();
                }

                IDfSysObject messageSysObject = (IDfSysObject)dfSession.getObject(messageId);

                JAXBContext jc = JAXBContext.newInstance(MCDocInfoModifyPSType.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                MCDocInfoModifyPSType passportXmlObject = unmarshaller.unmarshal(new StreamSource(messageSysObject.getContent()), MCDocInfoModifyPSType.class).getValue();

                IPassportService passportService = (IPassportService) dfSession.getClient().newService("ucb_ccdea_passport_service", dfSession.getSessionManager());

                String docSourceCode = ExternalMessagePersistence.getDocSourceCode(messageSysObject);
                String docSourceId = ExternalMessagePersistence.getDocSourceId(messageSysObject);

                IDfSysObject passportExistingObject = PassportPersistence.searchPassportObject(dfSession, passportXmlObject.getPSDetails().getDealPassport());
                ExternalMessagePersistence.startDocProcessing(messageSysObject, passportExistingObject);
                if (passportExistingObject == null) {
                    String passportObjectId = passportService.createDocumentFromMQType(dfSession, passportXmlObject, docSourceCode, docSourceId);
                    ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[] {passportObjectId});
                }
                else {
                    passportService.updateDocumentFromMQType(dfSession, passportXmlObject, docSourceCode, docSourceId, passportExistingObject.getObjectId());
                    ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[] {passportExistingObject.getObjectId().getId()});
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
