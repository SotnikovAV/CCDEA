package ru.rb.ccdea.adapters.mq.utils;

import com.documentum.fc.client.IDfSession;

public abstract class CustomerValidator extends XmlContentValidator{

    public CustomerValidator(IDfSession dfSession) {
        super(dfSession);
    }

    public abstract String getCustomerNumber(Object messageXmlContent);
    public abstract String getCustomerName(Object messageXmlContent);

    @Override
    public String getErrorDescription() {
        return "Customer not valid";
    }

    @Override
    public boolean isValid(Object messageXmlContent) {
        boolean isValid = true;
        String customerNumber = getCustomerNumber(messageXmlContent);
        String customerName = getCustomerName(messageXmlContent);
        if (customerNumber == null || customerNumber.trim().isEmpty()) {
            isValid = false;
        }
        return isValid;
    }
}
