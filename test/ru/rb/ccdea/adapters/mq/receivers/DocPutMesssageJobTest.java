package ru.rb.ccdea.adapters.mq.receivers;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.rb.ccdea.adapters.mq.binding.passport.MCDocInfoModifyPSType;
import ru.rb.ccdea.adapters.mq.utils.MessageObjectProcessor;
import ru.rb.ccdea.adapters.mq.utils.UnifiedResult;
import ru.rb.ccdea.storage.jobs.DocPutMesssageJob;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.PassportPersistence;
import ru.rb.ccdea.storage.services.api.IPassportService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import static ru.rb.ccdea.storage.persistence.ExternalMessagePersistence.*;

/**
 * Created by dimaz on 14.04.2016.
 */
public class DocPutMesssageJobTest {

    IDfSession dfSession = null;
    IDfClientX clientx = new DfClientX();
    IDfClient client = null;
    IDfSessionManager sessionManager = null;

    @Before
    public void before() throws DfException {
        client = clientx.getLocalClient();
        sessionManager = client.newSessionManager();

        IDfLoginInfo loginInfo = clientx.getLoginInfo();
        loginInfo.setUser("dmadmin");
        loginInfo.setPassword("Fkut,hf15");
        loginInfo.setDomain(null);

        sessionManager.setIdentity("ELAR", loginInfo);
        dfSession = sessionManager.getSession("ELAR");

    }

    @Test
    public void test() throws DfException, ValidationException, JAXBException {
        dfSession.beginTrans();
        String filePath = "c:\\Projects\\CCDEA\\TestData\\085bbc6a8170101a.xml";

        MCDocInfoModifyPSReceiverMethod receiverMethod = new MCDocInfoModifyPSReceiverMethod();

        UnifiedResult receiverResult = UnifiedResult.getSuccessResultInstance();
        IDfSysObject messageSysObject = ExternalMessagePersistence.createEmptyMessageObjectFromFile(dfSession, filePath);

        System.out.println("Message object " + messageSysObject.getObjectId().getId() + " created from file " + filePath);

        Object messageXmlContent = receiverMethod.getXmlContent(messageSysObject, receiverResult);
        MessageObjectProcessor messageObjectProcessor = receiverMethod.getMessageObjectProcessor(messageXmlContent);

        if (receiverResult.isSuccess()) {
            messageObjectProcessor.readValuesFromXml(receiverResult);
        }

        messageObjectProcessor.storeMessageObjectOnValidation(messageSysObject);

        receiverMethod.beforeGenearateResult(messageObjectProcessor, messageSysObject, receiverResult);

        receiverResult.generateResultDate();
        messageObjectProcessor.storeMessageObjectAfterValidation(messageSysObject, receiverResult);
        if (receiverResult.isError()) {
            System.out.println("Return error " + receiverResult.getErrorCode() + " with description: " + receiverResult.getErrorDescription());
        }

        IDfSysObject docPutSysobject = createDocPutMesage();

        IPassportService passportService = (IPassportService) dfSession.getClient().newService("ucb_ccdea_passport_service", dfSession.getSessionManager());

        String docSourceCode = ExternalMessagePersistence.getDocSourceCode(messageSysObject);
        String docSourceId = ExternalMessagePersistence.getDocSourceId(messageSysObject);

        JAXBContext jc = JAXBContext.newInstance(MCDocInfoModifyPSType.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        MCDocInfoModifyPSType passportXmlObject = unmarshaller.unmarshal(new StreamSource(messageSysObject.getContent()), MCDocInfoModifyPSType.class).getValue();

        IDfSysObject passportExistingObject = PassportPersistence.searchPassportObject(dfSession, passportXmlObject.getPSDetails().getDealPassport());
        ExternalMessagePersistence.startDocProcessing(messageSysObject, passportExistingObject);
        if (passportExistingObject == null) {
            String passportObjectId = passportService.createDocumentFromMQType(dfSession, passportXmlObject, docSourceCode, docSourceId);
            ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[]{passportObjectId});
        } else {
            passportService.updateDocumentFromMQType(dfSession, passportXmlObject, docSourceCode, docSourceId, passportExistingObject.getObjectId());
            ExternalMessagePersistence.finishDocProcessing(messageSysObject, new String[]{passportExistingObject.getObjectId().getId()});
        }

        DocPutMesssageJob job = new DocPutMesssageJob();
        job.process(dfSession, docPutSysobject.getObjectId());
        dfSession.commitTrans();

        Assert.assertTrue(String.format("test object %s should have n_current_state = 5", docPutSysobject.getObjectId()), docPutSysobject.getInt("n_current_state") == MESSAGE_STATE_LOADED);

    }

    private IDfSysObject createDocPutMesage() throws DfException {
        DocPutReceiverMethod docPutReceiverMethod = new DocPutReceiverMethod();

        UnifiedResult docPutReceiverResult = UnifiedResult.getSuccessResultInstance();
        IDfSysObject docPutSysobject = ExternalMessagePersistence.createEmptyMessageObjectFromFile(dfSession, "c:\\Projects\\CCDEA\\TestData\\085bbc6a8170122c.xml");

        System.out.println("Message object " + docPutSysobject.getObjectId().getId() + " created from file c:\\Projects\\CCDEA\\TestData\\085bbc6a8170122c.xml");

        Object docPutReceiverMethodXmlContent = docPutReceiverMethod.getXmlContent(docPutSysobject, docPutReceiverResult);
        MessageObjectProcessor docPutReceiverMethodMessageObjectProcessor = docPutReceiverMethod.getMessageObjectProcessor(docPutReceiverMethodXmlContent);

        if (docPutReceiverResult.isSuccess()) {
            docPutReceiverMethodMessageObjectProcessor.readValuesFromXml(docPutReceiverResult);
        }

        docPutReceiverMethodMessageObjectProcessor.storeMessageObjectOnValidation(docPutSysobject);

        docPutReceiverMethod.beforeGenearateResult(docPutReceiverMethodMessageObjectProcessor, docPutSysobject, docPutReceiverResult);

        docPutReceiverResult.generateResultDate();
        docPutReceiverMethodMessageObjectProcessor.storeMessageObjectAfterValidation(docPutSysobject, docPutReceiverResult);
        return docPutSysobject;
    }


    @After
    public void after() {
        if (dfSession != null) {
            sessionManager.release(dfSession);
        }
    }
}
