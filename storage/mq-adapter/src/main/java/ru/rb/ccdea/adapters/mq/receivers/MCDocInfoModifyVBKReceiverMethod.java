package ru.rb.ccdea.adapters.mq.receivers;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import ru.rb.ccdea.adapters.mq.binding.vbk.MCDocInfoModifyVBKType;
import ru.rb.ccdea.adapters.mq.binding.vbk.ObjectIdentifiersType;
import ru.rb.ccdea.adapters.mq.utils.*;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MCDocInfoModifyVBKReceiverMethod extends BaseReceiverMethod{
    @Override
    protected Object getXmlContent(IDfSysObject messageSysObject, UnifiedResult result) {
        return new XmlContentProcessor(MCDocInfoModifyVBKType.class).unmarshalSysObjectContent(messageSysObject, result);
    }

    @Override
    protected XmlContentValidator[] getXmlContentValidators(IDfSession dfSession) {
        XmlContentValidator[] validators = new XmlContentValidator[3];
        validators[0] = new DealPassportValidator(dfSession) {
            @Override
            public String getDealPassport(Object messageXmlContent) {
                MCDocInfoModifyVBKType vbkXmlContent = (MCDocInfoModifyVBKType) messageXmlContent;
                return vbkXmlContent.getVBKDetails().getDealPassport();
            }
        };
        validators[1] = new CustomerValidator(dfSession) {
            @Override
            public String getCustomerNumber(Object messageXmlContent) {
                MCDocInfoModifyVBKType vbkXmlContent = (MCDocInfoModifyVBKType) messageXmlContent;
                return vbkXmlContent.getCustomer().getCustomerNumber();
            }
            @Override
            public String getCustomerName(Object messageXmlContent) {
                MCDocInfoModifyVBKType vbkXmlContent = (MCDocInfoModifyVBKType) messageXmlContent;
                return vbkXmlContent.getCustomer().getCustomerName();
            }
        };
        validators[2] = new BranchValidator(dfSession) {
            @Override
            public String getBranchCode(Object messageXmlContent) {
                MCDocInfoModifyVBKType vbkXmlContent = (MCDocInfoModifyVBKType) messageXmlContent;
                return vbkXmlContent.getVBKDetails().getRegNum();
            }
        };
        return validators;
    }

    @Override
    protected MessageObjectProcessor getMessageObjectProcessor(Object messageXmlContent) {
        MessageObjectProcessor messageObjectProcessor = new MessageObjectProcessor(messageXmlContent) {
            @Override
            public String getMessageType() {
                return ExternalMessagePersistence.MESSAGE_TYPE_VBK;
            }

            @Override
            public String getSourceKey() {
                MCDocInfoModifyVBKType vbkXmlContent = (MCDocInfoModifyVBKType) messageXmlContent;
                if (vbkXmlContent != null && vbkXmlContent.getVBKDetails() != null) {
                    return "VBK/" + vbkXmlContent.getVBKDetails().getDealPassport();
                }
                else {
                    return "";
                }
            }

            @Override
            protected Date readSourceTime() {
                MCDocInfoModifyVBKType vbkXmlContent = (MCDocInfoModifyVBKType) messageXmlContent;
                return vbkXmlContent.getModificationDateTime().toGregorianCalendar().getTime();
            }

            @Override
            protected String readModificationVerb() {
                MCDocInfoModifyVBKType vbkXmlContent = (MCDocInfoModifyVBKType) messageXmlContent;
                return vbkXmlContent.getModificationVerb() != null ? vbkXmlContent.getModificationVerb().value() : null;
            }

            @Override
            protected List<MessageObjectIdentifiers> readOriginIdentificationList() {
                MCDocInfoModifyVBKType zaXmlContent = (MCDocInfoModifyVBKType) messageXmlContent;
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
