package ru.rb.ccdea.adapters.mq.receivers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;

import ru.rb.ccdea.adapters.mq.binding.contract.ContractDetailsType;
import ru.rb.ccdea.adapters.mq.binding.contract.MCDocInfoModifyContractType;
import ru.rb.ccdea.adapters.mq.binding.contract.ObjectIdentifiersType;
import ru.rb.ccdea.adapters.mq.utils.BranchValidator;
import ru.rb.ccdea.adapters.mq.utils.CustomerValidator;
import ru.rb.ccdea.adapters.mq.utils.MessageObjectProcessor;
import ru.rb.ccdea.adapters.mq.utils.UnifiedResult;
import ru.rb.ccdea.adapters.mq.utils.XmlContentProcessor;
import ru.rb.ccdea.adapters.mq.utils.XmlContentValidator;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;

public class MCDocInfoModifyContractReceiverMethod extends BaseReceiverMethod{

    @Override
    protected Object getXmlContent(IDfSysObject messageSysObject, UnifiedResult result) {
        return new XmlContentProcessor(MCDocInfoModifyContractType.class).unmarshalSysObjectContent(messageSysObject, result);
    }

    @Override
    protected XmlContentValidator[] getXmlContentValidators(IDfSession dfSession) {
        XmlContentValidator[] validators = new XmlContentValidator[3];
        validators[0] = new CustomerValidator(dfSession) {
            @Override
            public String getCustomerNumber(Object messageXmlContent) {
                MCDocInfoModifyContractType contractXmlContent = (MCDocInfoModifyContractType) messageXmlContent;
                return contractXmlContent.getCustomer().getCustomerNumber();
            }
            @Override
            public String getCustomerName(Object messageXmlContent) {
                MCDocInfoModifyContractType contractXmlContent = (MCDocInfoModifyContractType) messageXmlContent;
                return contractXmlContent.getCustomer().getCustomerName();
            }
        };
        validators[1] = new BranchValidator(dfSession) {
            @Override
            public String getBranchCode(Object messageXmlContent) throws ValidationException {
                MCDocInfoModifyContractType contractXmlContent = (MCDocInfoModifyContractType) messageXmlContent;
                ContractDetailsType type = contractXmlContent.getContractDetails();
                if(type == null) {
                	throw new ValidationException("Ошибка получения детализации контракта");
                }
                return type.getRegNum();
            }
        };
        validators[2] = new XmlContentValidator(dfSession) {

			@Override
			public String getErrorDescription() {
				return "Cant get or create dossier, key fields is empty.";
			}

			@Override
			public boolean isValid(Object messageXmlContent) throws ValidationException {
				MCDocInfoModifyContractType contractXmlContent = (MCDocInfoModifyContractType) messageXmlContent;
				ContractDetailsType contractDetails = contractXmlContent.getContractDetails();
				if (contractDetails != null) {
					String passportNumber = contractDetails.getDealPassport();
					String contractNumber = contractDetails.getDocNumber();
					Date contractDate = null;
					if (contractDetails.getDocDate() != null) {
						contractDate = contractDetails.getDocDate().toGregorianCalendar().getTime();
					}
					return (passportNumber != null && !passportNumber.trim().isEmpty())
							|| (contractNumber != null && !contractNumber.trim().isEmpty() && contractDate != null);
				} else {
					return false;
				}
			}
            
        };
        return validators;
    }

    @Override
    protected MessageObjectProcessor getMessageObjectProcessor(Object messageXmlContent) {
        MessageObjectProcessor messageObjectProcessor = new MessageObjectProcessor(messageXmlContent) {
            @Override
            public String getMessageType() {
                return ExternalMessagePersistence.MESSAGE_TYPE_CONTRACT;
            }

            @Override
            public String getSourceKey() {
                return docSourceSystem + "/" + docSourceId;
            }

            @Override
            protected Date readSourceTime() {
                MCDocInfoModifyContractType contractXmlContent = (MCDocInfoModifyContractType) messageXmlContent;
                return contractXmlContent.getModificationDateTime().toGregorianCalendar().getTime();
            }

            @Override
            protected String readModificationVerb() {
                MCDocInfoModifyContractType contractXmlContent = (MCDocInfoModifyContractType) messageXmlContent;
                return contractXmlContent.getModificationVerb() != null ? contractXmlContent.getModificationVerb().value() : null;
            }

            @Override
            protected List<MessageObjectIdentifiers> readOriginIdentificationList() {
                MCDocInfoModifyContractType zaXmlContent = (MCDocInfoModifyContractType) messageXmlContent;
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
