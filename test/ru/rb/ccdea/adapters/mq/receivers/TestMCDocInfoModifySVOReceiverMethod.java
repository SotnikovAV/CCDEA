/**
 * 
 */
package ru.rb.ccdea.adapters.mq.receivers;

import org.junit.Test;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;

import ru.rb.ccdea.adapters.mq.utils.MessageObjectProcessor;
import ru.rb.ccdea.adapters.mq.utils.UnifiedResult;
import ru.rb.ccdea.adapters.mq.utils.XmlContentValidator;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;

/**
 * @author sotnik
 *
 */
public class TestMCDocInfoModifySVOReceiverMethod {

	@Test
	public void test() {
		String filePath = "C:/Development/Workspaces/CCDEA_GITHUB/test/SVO/svo1.xml";

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

            BaseReceiverMethod receiverMethod = new MCDocInfoModifySVOReceiverMethod();

            UnifiedResult receiverResult = UnifiedResult.getSuccessResultInstance();
            IDfSysObject messageSysObject = ExternalMessagePersistence.createEmptyMessageObjectFromFile(testSession, filePath);

            System.out.println("Message object " + messageSysObject.getObjectId().getId() + " created from file " + filePath);

            Object messageXmlContent = receiverMethod.getXmlContent(messageSysObject, receiverResult);
            if (receiverResult.isSuccess()) {
                XmlContentValidator[] validators = receiverMethod.getXmlContentValidators(messageSysObject.getSession());
                if (receiverResult.isSuccess() && validators != null) {
                    try {
                        for (XmlContentValidator validator : validators) {
                        	if(validator != null) {
                        		validator.validate(messageXmlContent, receiverResult);
                        	}
                        }
                    } catch (Exception ex) {
                        receiverResult.setError(UnifiedResult.INTERNAL_ERROR_CODE, "Internal error during validation: " + ex.getMessage());

                    }
                }
            }

            MessageObjectProcessor messageObjectProcessor = receiverMethod.getMessageObjectProcessor(messageXmlContent);

            if (receiverResult.isSuccess()) {
                messageObjectProcessor.readValuesFromXml(receiverResult);
            }

            messageObjectProcessor.storeMessageObjectOnValidation(messageSysObject);

            if (receiverResult.isSuccess()) {
                try {
                    if (messageObjectProcessor.isCreateVerbSpecified() &&
                            !messageObjectProcessor.isFirstValidMessage(messageSysObject)) {
                        receiverResult.setError(UnifiedResult.WRONG_ORDER_CODE, "Received modification flag " + messageObjectProcessor.getModificationVerb() + " but this is not first message for " + messageObjectProcessor.getSourceKey());
                    } else if (messageObjectProcessor.isUpdateVerbSpecified() &&
                            messageObjectProcessor.isFirstValidMessage(messageSysObject)) {
                        receiverResult.setError(UnifiedResult.WRONG_ORDER_CODE, "Received modification flag " + messageObjectProcessor.getModificationVerb() + " but this is first message for " + messageObjectProcessor.getSourceKey());
                    }
                } catch (DfException dfEx) {
                    receiverResult.setError(UnifiedResult.INTERNAL_ERROR_CODE, "Internal error during check message order: " + dfEx.getMessage());

                }
            }

            receiverMethod.beforeGenearateResult(messageObjectProcessor, messageSysObject, receiverResult);

            receiverResult.generateResultDate();
            messageObjectProcessor.storeMessageObjectAfterValidation(messageSysObject, receiverResult);
            //receiverMethod.fillReplyObject((IDfWorkitemEx) iDfWorkitem, receiverResult);
            if (receiverResult.isError()) {
                System.out.println("Return error " + receiverResult.getErrorCode() + " with description: " + receiverResult.getErrorDescription());
            }
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
        finally {
            if (testSession != null) {
                sessionManager.release(testSession);
            }
        }
	}
	
}
