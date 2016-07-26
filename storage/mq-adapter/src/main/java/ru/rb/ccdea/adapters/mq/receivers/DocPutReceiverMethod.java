package ru.rb.ccdea.adapters.mq.receivers;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import ru.rb.ccdea.adapters.mq.binding.docput.DocPutType;
import ru.rb.ccdea.adapters.mq.utils.*;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DocPutReceiverMethod extends BaseReceiverMethod{
    @Override
	public Object getXmlContent(IDfSysObject messageSysObject, UnifiedResult result) {
        return new XmlContentProcessor(DocPutType.class).unmarshalSysObjectContent(messageSysObject, result);
    }

    @Override
	public XmlContentValidator[] getXmlContentValidators(IDfSession dfSession) {
        return new XmlContentValidator[] {new DocPutContentExistingValidator(dfSession)};
    }

    @Override
	public MessageObjectProcessor getMessageObjectProcessor(Object messageXmlContent) {
        MessageObjectProcessor messageObjectProcessor = new MessageObjectProcessor(messageXmlContent) {
            @Override
            public String getMessageType() {
                return ExternalMessagePersistence.MESSAGE_TYPE_DOCPUT;
            }

            @Override
            public String getSourceKey() {
                return contentSourceSystem + "/" + contentSourceId;
            }

            @Override
            protected Date readSourceTime() {
                return new Date();
            }

            @Override
            protected String readModificationVerb() {
                DocPutType docPutXmlContent = (DocPutType) messageXmlContent;
                return docPutXmlContent.getModificationVerb() != null ? docPutXmlContent.getModificationVerb().value() : null;
            }

            @Override
            protected List<MessageObjectIdentifiers> readOriginIdentificationList() {
                DocPutType docPutXmlContent = (DocPutType) messageXmlContent;
                List<MessageObjectIdentifiers> result = new ArrayList<MessageObjectIdentifiers>();
                MessageObjectIdentifiers docIdentifiers = new MessageObjectIdentifiers();
                docIdentifiers.setSourceSystem(docPutXmlContent.getOriginIdentification().get(0).getSourceSystem());
                docIdentifiers.setSourceId(docPutXmlContent.getOriginIdentification().get(0).getSourceId());
                result.add(docIdentifiers);
                MessageObjectIdentifiers contentIdentifiers = new MessageObjectIdentifiers();
                contentIdentifiers.setSourceSystem(docPutXmlContent.getOriginDocIdentification().get(0).getSourceSystem());
                contentIdentifiers.setSourceId(docPutXmlContent.getOriginDocIdentification().get(0).getSourceId());
                result.add(contentIdentifiers);
                return result;
            }
        };
        return messageObjectProcessor;
    }

    @Override
	public void beforeGenearateResult(MessageObjectProcessor processor, IDfSysObject messageSysObject, UnifiedResult receiverResult) {
        try {
            if (receiverResult.isSuccess() && !processor.isValidDocMessageExists(messageSysObject)) {
                receiverResult.setWarning("Cant find docput doc");
            }
        }
        catch (DfException dfEx) {
            receiverResult.setError(UnifiedResult.INTERNAL_ERROR_CODE, "Internal error during search docput doc: " + dfEx.getMessage());
            DfLogger.error(this, "Internal error during search docput doc", null, dfEx);
        }
    }
}
