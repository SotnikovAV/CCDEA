package ru.rb.ccdea.adapters.mq.receivers;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import ru.rb.ccdea.adapters.mq.binding.svo.MCDocInfoModifySVOType;
import ru.rb.ccdea.adapters.mq.binding.svo.ObjectIdentifiersType;
import ru.rb.ccdea.adapters.mq.utils.*;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MCDocInfoModifySVOReceiverMethod extends BaseReceiverMethod{

    @Override
    protected Object getXmlContent(IDfSysObject messageSysObject, UnifiedResult result) {
        return new XmlContentProcessor(MCDocInfoModifySVOType.class).unmarshalSysObjectContent(messageSysObject, result);
    }

    @Override
    protected XmlContentValidator[] getXmlContentValidators(IDfSession dfSession) {
        XmlContentValidator[] validators = new XmlContentValidator[4];
        validators[0] = new CustomerValidator(dfSession) {
            @Override
            public String getCustomerNumber(Object messageXmlContent) {
                MCDocInfoModifySVOType svoXmlContent = (MCDocInfoModifySVOType) messageXmlContent;
                return svoXmlContent.getCustomer().getCustomerNumber();
            }
            @Override
            public String getCustomerName(Object messageXmlContent) {
                MCDocInfoModifySVOType svoXmlContent = (MCDocInfoModifySVOType) messageXmlContent;
                return svoXmlContent.getCustomer().getCustomerName();
            }
        };
        validators[1] = new BranchValidator(dfSession) {
            @Override
            public String getBranchCode(Object messageXmlContent) {
                MCDocInfoModifySVOType svoXmlContent = (MCDocInfoModifySVOType) messageXmlContent;
                return svoXmlContent.getSVODetails().getRegNum();
            }
        };
        validators[2] = new ListNotEmptyValidator(dfSession) {
            @Override
            public String getErrorDescription() {
                return "VODetails is empty";
            }

            @Override
            public List getListToValidate(Object messageXmlContent) {
                MCDocInfoModifySVOType svoXmlContent = (MCDocInfoModifySVOType) messageXmlContent;
                return svoXmlContent.getSVODetails().getVODetails();
            }
        };
        validators[3] = new VODetailsMainDocIdentifiersValidator(dfSession);
        return validators;
    }

    @Override
    protected MessageObjectProcessor getMessageObjectProcessor(Object messageXmlContent) {
        MessageObjectProcessor messageObjectProcessor = new MessageObjectProcessor(messageXmlContent) {
            @Override
            public String getMessageType() {
                return ExternalMessagePersistence.MESSAGE_TYPE_SVO;
            }

            @Override
            public String getSourceKey() {
                return docSourceSystem + "/" + docSourceId;
            }

            @Override
            protected Date readSourceTime() {
                MCDocInfoModifySVOType svoXmlContent = (MCDocInfoModifySVOType) messageXmlContent;
                return svoXmlContent.getModificationDateTime().toGregorianCalendar().getTime();
            }

            @Override
            protected String readModificationVerb() {
                MCDocInfoModifySVOType svoXmlContent = (MCDocInfoModifySVOType) messageXmlContent;
                return svoXmlContent.getModificationVerb() != null ? svoXmlContent.getModificationVerb().value() : null;
            }

            @Override
            protected List<MessageObjectIdentifiers> readOriginIdentificationList() {
                MCDocInfoModifySVOType zaXmlContent = (MCDocInfoModifySVOType) messageXmlContent;
                List<MessageObjectIdentifiers> resultList = new ArrayList<MessageObjectIdentifiers>();
                for (ObjectIdentifiersType originIdentifiers : zaXmlContent.getOriginIdentification()) {
                    MessageObjectIdentifiers resultIdentifiers = new MessageObjectIdentifiers();
                    resultIdentifiers.setSourceId(originIdentifiers.getSourceId());
                    resultIdentifiers.setSourceSystem(originIdentifiers.getSourceSystem());
                    resultList.add(resultIdentifiers);
                }
                return resultList;
            }
        };
        return messageObjectProcessor;
    }

}
