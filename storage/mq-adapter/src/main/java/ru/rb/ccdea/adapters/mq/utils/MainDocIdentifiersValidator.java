package ru.rb.ccdea.adapters.mq.utils;

import java.util.Date;

import com.documentum.fc.client.IDfSession;

import ru.rb.ccdea.adapters.mq.receivers.ValidationException;

public abstract class MainDocIdentifiersValidator extends XmlContentValidator{

    public MainDocIdentifiersValidator(IDfSession dfSession) {
        super(dfSession);
    }

    public abstract String getDealPassport(Object messageXmlContent);
    public abstract String getContractNumber(Object messageXmlContent);
    public abstract Date getContractDate(Object messageXmlContent);

    @Override
    public String getErrorDescription() {
        return "DealPassport or ContractNumber/Date not valid";
    }

	@Override
	public boolean isValid(Object messageXmlContent) throws ValidationException {
		try {
			boolean isValid = true;
			String dealPasport = getDealPassport(messageXmlContent);
			if (dealPasport == null || dealPasport.trim().isEmpty()) {
				String contractNumber = getContractNumber(messageXmlContent);
				Date contractDate = getContractDate(messageXmlContent);
				if (contractNumber == null || contractNumber.trim().isEmpty() || contractDate == null) {
					isValid = false;
				}
			}
			return isValid;
		} catch (Exception ex) {
			throw new ValidationException("Ошибка валидации", ex);
		}
	}
}
