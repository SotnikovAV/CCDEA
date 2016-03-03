package ru.rb.ccdea.storage.services.api;

import com.documentum.fc.client.IDfService;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.adapters.mq.binding.spd.MCDocInfoModifySPDType;

public interface ISPDService extends IDfService {
    public String createDocumentFromMQType(IDfSession dfSession, MCDocInfoModifySPDType documentXmlObject, String docSourceCode, String docSourceId) throws DfException;
    public void updateDocumentFromMQType(IDfSession dfSession, MCDocInfoModifySPDType documentXmlObject, String docSourceCode, String docSourceId, IDfId documentId) throws DfException;
}
