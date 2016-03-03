package ru.rb.ccdea.adapters.mq.receivers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;

import ru.rb.ccdea.adapters.mq.binding.pd.MCDocInfoModifyPDType;
import ru.rb.ccdea.adapters.mq.binding.pd.ObjectIdentifiersType;
import ru.rb.ccdea.adapters.mq.utils.BranchValidator;
import ru.rb.ccdea.adapters.mq.utils.CustomerValidator;
import ru.rb.ccdea.adapters.mq.utils.MainDocIdentifiersValidator;
import ru.rb.ccdea.adapters.mq.utils.MessageObjectProcessor;
import ru.rb.ccdea.adapters.mq.utils.UnifiedResult;
import ru.rb.ccdea.adapters.mq.utils.XmlContentProcessor;
import ru.rb.ccdea.adapters.mq.utils.XmlContentValidator;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;

public class MCDocInfoModifyPDReceiverMethod extends BaseReceiverMethod{

    @Override
    protected Object getXmlContent(IDfSysObject messageSysObject, UnifiedResult result) {
        return new XmlContentProcessor(MCDocInfoModifyPDType.class).unmarshalSysObjectContent(messageSysObject, result);
    }

    @Override
    protected XmlContentValidator[] getXmlContentValidators(IDfSession dfSession) {
        XmlContentValidator[] validators = new XmlContentValidator[3];
        validators[0] = new CustomerValidator(dfSession) {
            @Override
            public String getCustomerNumber(Object messageXmlContent) {
                MCDocInfoModifyPDType pdXmlContent = (MCDocInfoModifyPDType) messageXmlContent;
                return pdXmlContent.getCustomer().getCustomerNumber();
            }
            @Override
            public String getCustomerName(Object messageXmlContent) {
                MCDocInfoModifyPDType pdXmlContent = (MCDocInfoModifyPDType) messageXmlContent;
                return pdXmlContent.getCustomer().getCustomerName();
            }
        };
        validators[1] = new BranchValidator(dfSession) {
            @Override
            public String getBranchCode(Object messageXmlContent) {
                MCDocInfoModifyPDType pdXmlContent = (MCDocInfoModifyPDType) messageXmlContent;
                return pdXmlContent.getPDDetails().getRegNum();
            }
        };
        validators[2] = new MainDocIdentifiersValidator(dfSession) {
            @Override
            public String getDealPassport(Object messageXmlContent) {
                MCDocInfoModifyPDType pdXmlContent = (MCDocInfoModifyPDType) messageXmlContent;
                return pdXmlContent.getPDDetails().getDealPassport();
            }
            @Override
            public String getContractNumber(Object messageXmlContent) {
                MCDocInfoModifyPDType pdXmlContent = (MCDocInfoModifyPDType) messageXmlContent;
                return pdXmlContent.getPDDetails().getContractNumber();
            }
            @Override
            public Date getContractDate(Object messageXmlContent) {
                MCDocInfoModifyPDType pdXmlContent = (MCDocInfoModifyPDType) messageXmlContent;
                if(messageXmlContent != null && pdXmlContent.getPDDetails() != null && pdXmlContent.getPDDetails().getContractDate() != null) {
                	return pdXmlContent.getPDDetails().getContractDate().toGregorianCalendar().getTime();
                }
                return null;
            }
        };
        return validators;
    }

    @Override
    protected MessageObjectProcessor getMessageObjectProcessor(Object messageXmlContent) {
        MessageObjectProcessor messageObjectProcessor = new MessageObjectProcessor(messageXmlContent) {
            @Override
            public String getMessageType() {
                return ExternalMessagePersistence.MESSAGE_TYPE_PD;
            }

            @Override
            public String getSourceKey() {
                return docSourceSystem + "/" + docSourceId;
            }

            @Override
            protected Date readSourceTime() {
                MCDocInfoModifyPDType pdXmlContent = (MCDocInfoModifyPDType) messageXmlContent;
                return pdXmlContent.getModificationDateTime().toGregorianCalendar().getTime();
            }

            @Override
            protected String readModificationVerb() {
                MCDocInfoModifyPDType pdXmlContent = (MCDocInfoModifyPDType) messageXmlContent;
                return pdXmlContent.getModificationVerb() != null ? pdXmlContent.getModificationVerb().value() : null;
            }

            @Override
            protected List<MessageObjectIdentifiers> readOriginIdentificationList() {
                MCDocInfoModifyPDType zaXmlContent = (MCDocInfoModifyPDType) messageXmlContent;
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
