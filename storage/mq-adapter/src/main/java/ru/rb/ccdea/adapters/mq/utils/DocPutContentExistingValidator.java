package ru.rb.ccdea.adapters.mq.utils;

import java.util.List;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfLogger;

import ru.rb.ccdea.adapters.mq.binding.docput.ContentType;
import ru.rb.ccdea.adapters.mq.binding.docput.ContentType.DocReference;
import ru.rb.ccdea.adapters.mq.binding.docput.ContentType.DocScan;
import ru.rb.ccdea.adapters.mq.binding.docput.DocPutType;
import ru.rb.ccdea.adapters.mq.receivers.ValidationException;

public class DocPutContentExistingValidator extends XmlContentValidator {

    public DocPutContentExistingValidator(IDfSession dfSession) {
        super(dfSession);
    }

    @Override
    public String getErrorDescription() {
        return "Single content not found";
    }

    @Override
    public boolean isValid(Object messageXmlContent) throws ValidationException {
        boolean isValid = true;
        DocPutType docPutXmlContent = (DocPutType)messageXmlContent;
        ContentType contentXmlContent = docPutXmlContent.getContent();
        if (contentXmlContent == null) {
            DfLogger.info(this, "docTypeName: " + docPutXmlContent.getDocTypeName(), null, null);
            throw new ValidationException("Parsed class DocPutType not valid");
        }
        List<DocReference> docReferenceList = contentXmlContent.getDocReference();
        List<DocScan> docScanList = contentXmlContent.getDocScan();
        if (docScanList != null && docScanList.size() > 0) {
            if (docReferenceList != null && docReferenceList.size() > 0) {
                isValid = false;
            }
            else if (docScanList.size() > 1) {
                isValid = false;
            }
        }
        else if (docReferenceList != null && docReferenceList.size() > 0){
            if (docScanList != null && docScanList.size() > 0) {
                isValid = false;
            }
            else if (docReferenceList.size() > 1) {
                isValid = false;
            } else {
            	for(DocReference docReference:docReferenceList) {
            		if(docReference.getFileReference() == null && docReference.getFileReferenceSMB() == null) {
            			isValid = false;
            		} else if(docReference.getFileFormat() == null) {
            			isValid = false;
            		}
            	}
            }
        }
        else {
            isValid = false;
        }
        return isValid;
    }
}
