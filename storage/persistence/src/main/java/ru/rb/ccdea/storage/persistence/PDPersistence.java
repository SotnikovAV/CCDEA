package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import ru.rb.ccdea.adapters.mq.binding.pd.MCDocInfoModifyPDType;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;

public class PDPersistence extends BaseDocumentPersistence{
    public static String DOCUMENT_FOLDER = HOME_FOLDER + "/pd";
    public static String DOCUMENT_TYPE_NAME = "ccdea_pd";
    public static String DOCUMENT_TYPE_DISPLAY_NAME = "ПД";

    public static final String ATTR_DOC_NUMBER = "s_doc_number";
    public static final String ATTR_DOC_DATE = "t_doc_date";
    public static final String ATTR_AMOUNT = "d_amount";
    public static final String ATTR_CURRENCY_CODE = "s_currency_code";
    public static final String ATTR_STATE = "s_state";
    public static final String ATTR_STATE_COMMENT = "s_state_comment";
    public static final String ATTR_DOC_KIND_CODE = "s_doc_kind_code";
    public static final String ATTR_DOC_TYPE = "s_doc_type";
    public static final String ATTR_KVALP = "s_kvalp";
    public static final String ATTR_SUMMAP = "d_summap";
    public static final String ATTR_KVALK = "s_kvalk";
    public static final String ATTR_SUMMAK = "d_summak";
    public static final String ATTR_PRIZ_POST = "s_priz_post";
    public static final String ATTR_SROK_WAIT = "t_srok_wait";
    public static final String ATTR_KSTRANA = "s_kstrana";
    public static final String ATTR_PRIMESH = "s_primesh";

    public static IDfSysObject createDocument(IDfSession dfSession) throws DfException {
        throwIfNotTransactionActive(dfSession);

        IDfSysObject result = (IDfSysObject)dfSession.newObject(DOCUMENT_TYPE_NAME);
        result.link(DOCUMENT_FOLDER);
        return result;
    }

    public static DossierKeyDetails getKeyDetailsFromMQ(MCDocInfoModifyPDType documentXmlObject) throws DfException {
        DossierKeyDetails result = new DossierKeyDetails();
        result.dossierId = null;
        result.branchCode = documentXmlObject.getPDDetails().getRegNum();
        result.customerNumber = getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber());
        result.passportNumber = documentXmlObject.getPDDetails().getDealPassport();
        if (result.passportNumber == null || result.passportNumber.trim().isEmpty()) {
            result.contractNumber = documentXmlObject.getPDDetails().getContractNumber();
            result.contractDate = documentXmlObject.getPDDetails().getContractDate().toGregorianCalendar().getTime();
        }
        return result;
    }

    public static void saveFieldsToDocument(IDfSysObject document, MCDocInfoModifyPDType documentXmlObject, String dossierId, String docSourceCode, String docSourceId, VersionRecordDetails versionRecordDetails) throws DfException {
        throwIfNotTransactionActive(document.getSession());

        String passportNumber = documentXmlObject.getPDDetails().getDealPassport();
        String passportTypeCode = null;
        if (passportNumber != null && passportNumber.trim().length() >= 20) {
            passportTypeCode = passportNumber.substring(19,20);
        }

        versionRecordDetails.setIdWithHistory(document, ATTR_DOSSIER, new DfId(dossierId));
        versionRecordDetails.setStringWithHistory(document, ATTR_BRANCH_CODE, documentXmlObject.getPDDetails().getRegNum());
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NUMBER, getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber()));
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NAME, documentXmlObject.getCustomer().getCustomerName());
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_NUMBER, passportNumber);
        versionRecordDetails.setStringWithHistory(document, ATTR_CONTRACT_NUMBER, documentXmlObject.getPDDetails().getContractNumber());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_CONTRACT_DATE, documentXmlObject.getPDDetails().getContractDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_LAST_CHANGE_DATE, documentXmlObject.getModificationDateTime());
        versionRecordDetails.setStringWithHistory(document, ATTR_LAST_CHANGE_AUTHOR, documentXmlObject.getUserId());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_CODE, docSourceCode);
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_ID, docSourceId);
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_TYPE_CODE, passportTypeCode);

        versionRecordDetails.setStringWithHistory(document, ATTR_AUTHOR_DEPARTMENT_CODE, documentXmlObject.getUserDep());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_CHANNEL_NAME, documentXmlObject.getPDDetails().getSystemSource());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_CHANNEL_REF_ID, documentXmlObject.getPDDetails().getSystemRefId());
        versionRecordDetails.setStringWithHistory(document, ATTR_CONTENT_URL, documentXmlObject.getPDDetails().getURLScan());

        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_NUMBER, documentXmlObject.getPDDetails().getDocNumber());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_DOC_DATE, documentXmlObject.getPDDetails().getDocDate());
        versionRecordDetails.setBigDecimalWithHistory(document, ATTR_AMOUNT, documentXmlObject.getPDDetails().getDocAmount());
        versionRecordDetails.setStringWithHistory(document, ATTR_CURRENCY_CODE, documentXmlObject.getPDDetails().getDocCurrency());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE, documentXmlObject.getPDDetails().getStatusName());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE_COMMENT, documentXmlObject.getPDDetails().getStatusComment());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_KIND_CODE, documentXmlObject.getPDDetails().getDocTypeCode());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_TYPE, documentXmlObject.getPDDetails().getDocType());
        versionRecordDetails.setStringWithHistory(document, ATTR_KVALP, documentXmlObject.getPDDetails().getKvalp());
        versionRecordDetails.setBigDecimalWithHistory(document, ATTR_SUMMAP, documentXmlObject.getPDDetails().getSummap());
        versionRecordDetails.setStringWithHistory(document, ATTR_KVALK, documentXmlObject.getPDDetails().getKvalk());
        versionRecordDetails.setBigDecimalWithHistory(document, ATTR_SUMMAK, documentXmlObject.getPDDetails().getSummak());
        versionRecordDetails.setStringWithHistory(document, ATTR_PRIZ_POST, documentXmlObject.getPDDetails().getPrizPost());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_SROK_WAIT, documentXmlObject.getPDDetails().getSrokWait());
        versionRecordDetails.setStringWithHistory(document, ATTR_KSTRANA, documentXmlObject.getPDDetails().getKstrana());
        versionRecordDetails.setStringWithHistory(document, ATTR_PRIMESH, documentXmlObject.getPDDetails().getPrimesh());

//        document.appendString(ATTR_RP_CONTENT_SOURCE_CODE, docSourceCode);
//        document.appendString(ATTR_RP_CONTENT_SOURCE_ID, docSourceId);
        document.setACL(getBranchACL(document.getSession(), document.getString(ATTR_BRANCH_CODE)));
        document.setObjectName(getDocumentDescription(document));
        document.save();
    }

    public static String getDocumentDescription(IDfPersistentObject document) throws DfException {
        return document.getString(ATTR_DOC_TYPE) + " ("  + document.getString(ATTR_CUSTOMER_NUMBER) + ")";
    }

    public static IDfSysObject searchPDObject(IDfSession dfSession, String docSourceCode, String docSourceId) throws DfException {
        return (IDfSysObject)dfSession.getObjectByQualification(DOCUMENT_TYPE_NAME + " where " + ATTR_DOC_SOURCE_CODE + " = '" + docSourceCode + "' AND " + ATTR_DOC_SOURCE_ID + " = '" + docSourceId + "'");
    }
}
