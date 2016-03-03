package ru.rb.ccdea.adapters.mq.utils;

import java.util.List;

import com.documentum.fc.client.IDfSession;

import ru.rb.ccdea.adapters.mq.receivers.ValidationException;

public abstract class ListNotEmptyValidator extends XmlContentValidator{

    public ListNotEmptyValidator(IDfSession dfSession) {
        super(dfSession);
    }

    public abstract String getErrorDescription();
    public abstract List getListToValidate(Object messageXmlContent);

	@Override
	public boolean isValid(Object messageXmlContent) throws ValidationException {
		try {
			boolean isValid = true;
			List listToValidate = getListToValidate(messageXmlContent);
			if (listToValidate == null || listToValidate.size() == 0) {
				isValid = false;
			}
			return isValid;
		} catch (Exception ex) {
			throw new ValidationException("Ошибка валидации", ex);
		}
	}
}
