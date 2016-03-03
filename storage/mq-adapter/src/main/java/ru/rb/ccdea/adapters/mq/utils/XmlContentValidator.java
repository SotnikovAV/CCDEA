package ru.rb.ccdea.adapters.mq.utils;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

import ru.rb.ccdea.adapters.mq.receivers.ValidationException;

public abstract class XmlContentValidator {

    private IDfSession dfSession = null;

    public XmlContentValidator(IDfSession dfSession) {
        this.dfSession = dfSession;
    }

    protected IDfSession getDfSession() {
        return dfSession;
    }

    public abstract String getErrorDescription();
    public abstract boolean isValid(Object messageXmlContent) throws ValidationException;

	public void validate(Object messageXmlContent, UnifiedResult result) throws ValidationException {
		try {
			boolean isValid = isValid(messageXmlContent);
			if (!isValid) {
				result.addError(getErrorCode(), getErrorDescription());
			}
		} catch (Exception ex) {
			throw new ValidationException("Ошибка валидации", ex);
		}
	}

    protected String getErrorCode() {
        return UnifiedResult.VALIDATE_ERROR_CODE;
    }
}
