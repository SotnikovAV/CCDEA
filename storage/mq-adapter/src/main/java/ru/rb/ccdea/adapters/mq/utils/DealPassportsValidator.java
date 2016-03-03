package ru.rb.ccdea.adapters.mq.utils;

import com.documentum.fc.client.IDfSession;

import java.util.List;

public abstract class DealPassportsValidator extends XmlContentValidator {

    public DealPassportsValidator(IDfSession dfSession) {
        super(dfSession);
    }

    public abstract List<String> getDealPassports(Object messageXmlContent);

    @Override
    public String getErrorDescription() {
        return "DealPassports not valid";
    }

    @Override
    public boolean isValid(Object messageXmlContent) {
        boolean isValid = true;
        List<String> dealPasports = getDealPassports(messageXmlContent);
        if (dealPasports != null && dealPasports.size() > 0) {
            for (String dealPasport : dealPasports) {
                if (dealPasport.trim().isEmpty()) {
                    isValid = false;
                }
            }
        } else {
            isValid = false;
        }
        return isValid;
    }
}
