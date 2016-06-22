package ru.rb.ccdea.adapters.mq.receivers;

import com.documentum.bpm.IDfWorkitemEx;
import com.documentum.bpm.rtutil.WorkflowMethod;
import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.fc.methodserver.IDfMethod;
import ru.rb.ccdea.adapters.mq.utils.MessageObjectProcessor;
import ru.rb.ccdea.adapters.mq.utils.UnifiedResult;
import ru.rb.ccdea.adapters.mq.utils.XmlContentValidator;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public abstract class BaseReceiverMethod extends WorkflowMethod implements IDfMethod, IDfModule {

    public static String REQUEST_OBJECT_NAME = "sourceXmlObject";
    public static String REPLY_OBJECT_NAME = "replyObject";

    public static String ATTR_STATUS_DETAILS_STATUS = "statusDetails/status";
    public static String ATTR_STATUS_DETAILS_TIMESTAMP = "statusDetails/timeStamp";
    public static String ATTR_STATUS_DETAILS_ERRORCODE = "statusDetails/errorCode";
    public static String ATTR_STATUS_DETAILS_DESCRIPTION = "statusDetails/errorDescription";

    public static SimpleDateFormat replyDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public abstract Object getXmlContent(IDfSysObject messageSysObject, UnifiedResult result);
    public abstract XmlContentValidator[] getXmlContentValidators(IDfSession dfSession) throws ValidationException;
    public abstract MessageObjectProcessor getMessageObjectProcessor(Object messageXmlContent);

    @Override
    protected int doTask(IDfWorkitem iDfWorkitem, IDfProperties iDfProperties, PrintWriter printWriter) throws Exception {
        ClassLoader tcc1 = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader moduleClassLoader = this.getClass().getClassLoader();
                    
            Thread.currentThread().setContextClassLoader(moduleClassLoader);

            UnifiedResult receiverResult = UnifiedResult.getSuccessResultInstance();
            IDfSysObject messageSysObject = (IDfSysObject) getComponent(REQUEST_OBJECT_NAME);

            Object messageXmlContent = getXmlContent(messageSysObject, receiverResult);
            if (receiverResult.isSuccess()) {
                XmlContentValidator[] validators = getXmlContentValidators(messageSysObject.getSession());
                if (receiverResult.isSuccess() && validators != null) {
                    try {
                        for (XmlContentValidator validator : validators) {
                        	if(validator != null) {
                        		validator.validate(messageXmlContent, receiverResult);
                        	}
                        }
                    } catch (Exception ex) {
                        receiverResult.setError(UnifiedResult.INTERNAL_ERROR_CODE, "Internal error during validation: " + ex.getMessage());
                        DfLogger.error(this, "Internal error during validation", null, ex);
                    }
                }
            }

            MessageObjectProcessor messageObjectProcessor = getMessageObjectProcessor(messageXmlContent);

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
                    DfLogger.error(this, "Internal error during check message order", null, dfEx);
                }
            }

            beforeGenearateResult(messageObjectProcessor, messageSysObject, receiverResult);

            receiverResult.generateResultDate();
            messageObjectProcessor.storeMessageObjectAfterValidation(messageSysObject, receiverResult);
            fillReplyObject((IDfWorkitemEx) iDfWorkitem, receiverResult);
            if (receiverResult.isError()) {
                DfLogger.error(this, "Return error " + receiverResult.getErrorCode() + " with description: " + receiverResult.getErrorDescription(), null, null);

                // если произошла любая ошибка, записываем ошибочные значения напрямую в атрибуты
                messageObjectProcessor.readValuesFromXml(receiverResult);
                messageObjectProcessor.storeMessageObjectOnErrorAfterValidation(messageSysObject);
            }
            return 0;
        }
        finally {
            Thread.currentThread().setContextClassLoader(tcc1);
        }
    }

    public void beforeGenearateResult(MessageObjectProcessor messageObjectProcessor, IDfSysObject messageSysObject, UnifiedResult receiverResult) {

    }

    private void fillReplyObject(IDfWorkitemEx workitemEx, UnifiedResult receiverResult) throws DfException {
        workitemEx.setStructuredDataTypeAttrValue(
                REPLY_OBJECT_NAME,
                ATTR_STATUS_DETAILS_STATUS,
                receiverResult.getResult());
        workitemEx.setStructuredDataTypeAttrValue(
                REPLY_OBJECT_NAME,
                ATTR_STATUS_DETAILS_TIMESTAMP,
                replyDateFormat.format(receiverResult.getResultDate()));
        workitemEx.setStructuredDataTypeAttrValue(
                REPLY_OBJECT_NAME,
                ATTR_STATUS_DETAILS_ERRORCODE,
                receiverResult.getErrorCode());
        workitemEx.setStructuredDataTypeAttrValue(
                REPLY_OBJECT_NAME,
                ATTR_STATUS_DETAILS_DESCRIPTION,
                receiverResult.getErrorDescription());
    }


    public static void main(String[] args) {
        String filePath = "c:/085bbc6a816b1ef0.xml";
        if(args.length > 0) {
        	filePath = args[0];
        } 

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

            BaseReceiverMethod receiverMethod = new MCDocInfoModifyZAReceiverMethod();

            UnifiedResult receiverResult = UnifiedResult.getSuccessResultInstance();
            IDfSysObject messageSysObject = ExternalMessagePersistence.createEmptyMessageObjectFromFile(testSession, filePath);

            System.out.println("Message object " + messageSysObject.getObjectId().getId() + " created from file " + filePath);

            Object messageXmlContent = receiverMethod.getXmlContent(messageSysObject, receiverResult);
            if (receiverResult.isSuccess()) {
                XmlContentValidator[] validators = receiverMethod.getXmlContentValidators(messageSysObject.getSession());
                if (receiverResult.isSuccess() && validators != null) {
                    try {
                        for (XmlContentValidator validator : validators) {
                            validator.validate(messageXmlContent, receiverResult);
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
