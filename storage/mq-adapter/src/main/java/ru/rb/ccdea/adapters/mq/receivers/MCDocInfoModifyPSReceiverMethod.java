package ru.rb.ccdea.adapters.mq.receivers;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import ru.rb.ccdea.adapters.mq.binding.passport.CustomerDetailsType;
import ru.rb.ccdea.adapters.mq.binding.passport.MCDocInfoModifyPSType;
import ru.rb.ccdea.adapters.mq.binding.passport.ObjectIdentifiersType;
import ru.rb.ccdea.adapters.mq.utils.*;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MCDocInfoModifyPSReceiverMethod extends BaseReceiverMethod{
    @Override
	public Object getXmlContent(IDfSysObject messageSysObject, UnifiedResult result) {
        return new XmlContentProcessor(MCDocInfoModifyPSType.class).unmarshalSysObjectContent(messageSysObject, result);
    }

    @Override
	public XmlContentValidator[] getXmlContentValidators(IDfSession dfSession) {
        XmlContentValidator[] validators = new XmlContentValidator[3];
        validators[0] = new DealPassportValidator(dfSession) {
            @Override
            public String getDealPassport(Object messageXmlContent) {
                MCDocInfoModifyPSType psXmlContent = (MCDocInfoModifyPSType) messageXmlContent;
                return psXmlContent.getPSDetails().getDealPassport();
            }
        };
        validators[1] = new CustomerValidator(dfSession) {
            @Override
            public String getCustomerNumber(Object messageXmlContent) {
                MCDocInfoModifyPSType psXmlContent = (MCDocInfoModifyPSType) messageXmlContent;
                return psXmlContent.getCustomer().getCustomerNumber();
            }
            @Override
            public String getCustomerName(Object messageXmlContent) {
                MCDocInfoModifyPSType psXmlContent = (MCDocInfoModifyPSType) messageXmlContent;
                return psXmlContent.getCustomer().getCustomerName();
            }
        };
        validators[2] = new BranchValidator(dfSession) {
            @Override
            public String getBranchCode(Object messageXmlContent) {
                MCDocInfoModifyPSType psXmlContent = (MCDocInfoModifyPSType) messageXmlContent;
                return psXmlContent.getPSDetails().getRegNum();
            }
        };
        return validators;
    }

    @Override
	public MessageObjectProcessor getMessageObjectProcessor(Object messageXmlContent) {
        MessageObjectProcessor messageObjectProcessor = new MessageObjectProcessor(messageXmlContent) {
            @Override
            public String getMessageType() {
                return ExternalMessagePersistence.MESSAGE_TYPE_PS;
            }

            @Override
            public String getSourceKey() {
                MCDocInfoModifyPSType psXmlContent = (MCDocInfoModifyPSType) messageXmlContent;
                if (psXmlContent != null && psXmlContent.getPSDetails() != null) {
                    return "PS/" + psXmlContent.getPSDetails().getDealPassport();
                }
                else {
                    return "";
                }
            }

            @Override
            protected Date readSourceTime() {
                MCDocInfoModifyPSType psXmlContent = (MCDocInfoModifyPSType) messageXmlContent;
                return psXmlContent.getModificationDateTime().toGregorianCalendar().getTime();
            }

            @Override
            protected String readModificationVerb() {
                MCDocInfoModifyPSType psXmlContent = (MCDocInfoModifyPSType) messageXmlContent;
                return psXmlContent.getModificationVerb() != null ? psXmlContent.getModificationVerb().value() : null;
            }

            @Override
            protected List<MessageObjectIdentifiers> readOriginIdentificationList() {
                MCDocInfoModifyPSType zaXmlContent = (MCDocInfoModifyPSType) messageXmlContent;
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
