package ru.rb.ccdea.adapters.mq.utils;

import com.documentum.fc.client.IDfSession;

import ru.rb.ccdea.adapters.mq.receivers.ValidationException;

public abstract class DealPassportValidator extends XmlContentValidator {

    public DealPassportValidator(IDfSession dfSession) {
        super(dfSession);
    }

    public abstract String getDealPassport(Object messageXmlContent);

    @Override
    public String getErrorDescription() {
        return "DealPassport not valid";
    }

    @Override
    public boolean isValid(Object messageXmlContent) throws ValidationException {
		try {
			boolean isValid = true;

			String dealPasport = getDealPassport(messageXmlContent);
			if (dealPasport == null || dealPasport.trim().isEmpty()) {
				isValid = false;
			}
			return isValid;
		} catch (Exception ex) {
			throw new ValidationException("Ошибка валидации", ex);
		}
    }
}
