package ru.rb.ccdea.adapters.mq.receivers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;

import ru.rb.ccdea.adapters.mq.binding.request.ContractRecordType;
import ru.rb.ccdea.adapters.mq.binding.request.MCDocInfoModifyZAType;
import ru.rb.ccdea.adapters.mq.binding.request.ObjectIdentifiersType;
import ru.rb.ccdea.adapters.mq.binding.request.ZADetailsType;
import ru.rb.ccdea.adapters.mq.utils.BranchValidator;
import ru.rb.ccdea.adapters.mq.utils.CustomerValidator;
import ru.rb.ccdea.adapters.mq.utils.MessageObjectProcessor;
import ru.rb.ccdea.adapters.mq.utils.UnifiedResult;
import ru.rb.ccdea.adapters.mq.utils.XmlContentProcessor;
import ru.rb.ccdea.adapters.mq.utils.XmlContentValidator;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;

public class MCDocInfoModifyZAReceiverMethod extends BaseReceiverMethod {

	@Override
	public Object getXmlContent(IDfSysObject messageSysObject, UnifiedResult result) {
		return new XmlContentProcessor(MCDocInfoModifyZAType.class).unmarshalSysObjectContent(messageSysObject, result);
	}

	@Override
	public XmlContentValidator[] getXmlContentValidators(IDfSession dfSession) {
		XmlContentValidator[] validators = new XmlContentValidator[3];
		validators[0] = new XmlContentValidator(dfSession) {

			@Override
			public String getErrorDescription() {
				return "Cant get or create dossier, key fields is empty.";
			}

			@Override
			public boolean isValid(Object messageXmlContent) throws ValidationException {
				MCDocInfoModifyZAType zaXmlContent = (MCDocInfoModifyZAType) messageXmlContent;
				ZADetailsType zaDetails = zaXmlContent.getZADetails();
				if (zaDetails == null) {
					return false;
				}
				boolean isValid = false;
				List<String> dealPasports = zaDetails.getDealPassport();
				if (dealPasports != null && dealPasports.size() > 0) {
					for (String dealPasport : dealPasports) {
						if (!dealPasport.trim().isEmpty()) {
							isValid = true;
							break;
						}
					}
				} else {
					isValid = false;
				}
				if (isValid) {
					return true;
				} else {
					ContractRecordType contractDetails = zaDetails.getContractDetails();
					if (contractDetails != null) {

						String contractNumber = contractDetails.getContractNumber();
						Date contractDate = null;
						if (contractDetails.getContractDate() != null) {
							contractDate = contractDetails.getContractDate().toGregorianCalendar().getTime();
						}
						return contractNumber != null && !contractNumber.trim().isEmpty() && contractDate != null;
					} else {
						return false;
					}
				}
			}

		};
		validators[1] = new CustomerValidator(dfSession) {
			@Override
			public String getCustomerNumber(Object messageXmlContent) {
				MCDocInfoModifyZAType zaXmlContent = (MCDocInfoModifyZAType) messageXmlContent;
				return zaXmlContent.getCustomer().getCustomerNumber();
			}

			@Override
			public String getCustomerName(Object messageXmlContent) {
				MCDocInfoModifyZAType zaXmlContent = (MCDocInfoModifyZAType) messageXmlContent;
				return zaXmlContent.getCustomer().getCustomerName();
			}
		};
		validators[2] = new BranchValidator(dfSession) {
			@Override
			public String getBranchCode(Object messageXmlContent) {
				MCDocInfoModifyZAType zaXmlContent = (MCDocInfoModifyZAType) messageXmlContent;
				return zaXmlContent.getZADetails().getRegNum();
			}
		};
		return validators;
	}

	@Override
	public MessageObjectProcessor getMessageObjectProcessor(Object messageXmlContent) {
		MessageObjectProcessor messageObjectProcessor = new MessageObjectProcessor(messageXmlContent) {
			@Override
			public String getMessageType() {
				return ExternalMessagePersistence.MESSAGE_TYPE_ZA;
			}

			@Override
			public String getSourceKey() {
				return docSourceSystem + "/" + docSourceId;
			}

			@Override
			protected Date readSourceTime() {
				MCDocInfoModifyZAType zaXmlContent = (MCDocInfoModifyZAType) messageXmlContent;
				return zaXmlContent.getModificationDateTime().toGregorianCalendar().getTime();
			}

			@Override
			protected String readModificationVerb() {
				MCDocInfoModifyZAType zaXmlContent = (MCDocInfoModifyZAType) messageXmlContent;
				return zaXmlContent.getModificationVerb() != null ? zaXmlContent.getModificationVerb().value() : null;
			}

			@Override
			protected List<MessageObjectIdentifiers> readOriginIdentificationList() {
				MCDocInfoModifyZAType zaXmlContent = (MCDocInfoModifyZAType) messageXmlContent;
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
