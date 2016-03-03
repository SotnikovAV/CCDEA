package ru.rb.ccdea.storage.services.api;

import com.documentum.fc.client.IDfService;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.adapters.mq.binding.svo.MCDocInfoModifySVOType;
import ru.rb.ccdea.adapters.mq.binding.svo.VODetailsType;

public interface ISVOService extends IDfService {
    public String createDocumentFromMQType(IDfSession dfSession, MCDocInfoModifySVOType documentXmlObject, VODetailsType voDetailsXmlObject, String docSourceCode, String docSourceId) throws DfException;
    public void updateDocumentFromMQType(IDfSession dfSession, MCDocInfoModifySVOType documentXmlObject, VODetailsType voDetailsXmlObject, String docSourceCode, String docSourceId, IDfId documentId) throws DfException;
}
