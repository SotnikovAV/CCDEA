package ru.rb.ccdea.storage.services.api;

import com.documentum.fc.client.IDfService;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.adapters.mq.binding.pd.MCDocInfoModifyPDType;

public interface IPDService extends IDfService {
    public String createDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyPDType documentXmlObject, String docSourceCode, String docSourceId) throws DfException;
    public void updateDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyPDType documentXmlObject, String docSourceCode, String docSourceId, IDfId documentId) throws DfException;
}
