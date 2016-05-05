package ru.rb.ccdea.adapters.mq.receivers;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import ru.rb.ccdea.adapters.mq.binding.spd.MCDocInfoModifySPDType;
import ru.rb.ccdea.adapters.mq.binding.spd.ObjectIdentifiersType;
import ru.rb.ccdea.adapters.mq.utils.*;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MCDocInfoModifySPDReceiverMethod extends BaseReceiverMethod{

    @Override
	public Object getXmlContent(IDfSysObject messageSysObject, UnifiedResult result) {
        return new XmlContentProcessor(MCDocInfoModifySPDType.class).unmarshalSysObjectContent(messageSysObject, result);
    }

    @Override
	public XmlContentValidator[] getXmlContentValidators(IDfSession dfSession) {
        XmlContentValidator[] validators = new XmlContentValidator[3];
        validators[0] = new CustomerValidator(dfSession) {
            @Override
            public String getCustomerNumber(Object messageXmlContent) {
                MCDocInfoModifySPDType spdXmlContent = (MCDocInfoModifySPDType) messageXmlContent;
                return spdXmlContent.getCustomer().getCustomerNumber();
            }
            @Override
            public String getCustomerName(Object messageXmlContent) {
                MCDocInfoModifySPDType spdXmlContent = (MCDocInfoModifySPDType) messageXmlContent;
                return spdXmlContent.getCustomer().getCustomerName();
            }
        };
        validators[1] = new BranchValidator(dfSession) {
            @Override
            public String getBranchCode(Object messageXmlContent) {
                MCDocInfoModifySPDType spdXmlContent = (MCDocInfoModifySPDType) messageXmlContent;
                return spdXmlContent.getSPDDetails().getRegNum();
            }
        };
        validators[2] = new DealPassportValidator(dfSession) {
            @Override
            public String getDealPassport(Object messageXmlContent) {
                MCDocInfoModifySPDType spdXmlContent = (MCDocInfoModifySPDType) messageXmlContent;
                return spdXmlContent.getSPDDetails().getDealPassport();
            }
        };
        return validators;
    }

    @Override
	public MessageObjectProcessor getMessageObjectProcessor(Object messageXmlContent) {
        MessageObjectProcessor messageObjectProcessor = new MessageObjectProcessor(messageXmlContent) {
            @Override
            public String getMessageType() {
                return ExternalMessagePersistence.MESSAGE_TYPE_SPD;
            }

            @Override
            public String getSourceKey() {
                return docSourceSystem + "/" + docSourceId;
            }

            @Override
            protected Date readSourceTime() {
                MCDocInfoModifySPDType spdXmlContent = (MCDocInfoModifySPDType) messageXmlContent;
                return spdXmlContent.getModificationDateTime().toGregorianCalendar().getTime();
            }

            @Override
            protected String readModificationVerb() {
                MCDocInfoModifySPDType spdXmlContent = (MCDocInfoModifySPDType) messageXmlContent;
                return spdXmlContent.getModificationVerb() != null ? spdXmlContent.getModificationVerb().value() : null;
            }

            @Override
            protected List<MessageObjectIdentifiers> readOriginIdentificationList() {
                MCDocInfoModifySPDType zaXmlContent = (MCDocInfoModifySPDType) messageXmlContent;
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
