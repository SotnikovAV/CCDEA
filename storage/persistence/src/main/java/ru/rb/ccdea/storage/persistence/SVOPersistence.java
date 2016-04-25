package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import ru.rb.ccdea.adapters.mq.binding.svo.MCDocInfoModifySVOType;
import ru.rb.ccdea.adapters.mq.binding.svo.VODetailsType;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;

import java.util.Date;

public class SVOPersistence extends BaseDocumentPersistence {

    public static String DOCUMENT_FOLDER = HOME_FOLDER + "/svodetail";
    public static String DOCUMENT_TYPE_NAME = "ccdea_svo_detail";
    public static String DOCUMENT_TYPE_DISPLAY_NAME = "СВО";

    public static final String ATTR_DOC_NUMBER = "s_doc_number";
    public static final String ATTR_DOC_DATE = "t_doc_date";
    public static final String ATTR_STATE_CODE = "s_state_code";
    public static final String ATTR_STATE = "s_state";
    public static final String ATTR_STATE_COMMENT = "s_state_comment";
    public static final String ATTR_ACCEPT_DATE = "t_accept_date";
    public static final String ATTR_REJECT_DATE = "t_reject_date";
    public static final String ATTR_RECEIVE_DATE = "t_receive_date";
    public static final String ATTR_IS_CHANGED = "b_is_changed";
    public static final String ATTR_BANK_NAME = "s_bank_name";
    public static final String ATTR_BANK_COUNTRY_CODE = "s_bank_country_code";
    public static final String ATTR_ACCOUNT = "s_account";
    public static final String ATTR_IS_URGENT = "b_is_urgent";
    public static final String ATTR_IS_VIP = "b_is_vip";
    public static final String ATTR_DETAIL_INDEX = "n_detail_index";
    public static final String ATTR_DETAIL_DATE = "t_detail_date";
    public static final String ATTR_VO_CODE = "s_vo_code";
    public static final String ATTR_PAYMENT_NUMBER = "s_payment_number";
    public static final String ATTR_PAYMENT_DATE = "t_payment_date";
    public static final String ATTR_PAYMENT_AMOUNT = "d_payment_amount";
    public static final String ATTR_PAYMENT_CURRENCY_CODE = "s_payment_currency_code";
    public static final String ATTR_PAYMENT_FLAG = "n_payment_flag";
    public static final String ATTR_VALID_DATE = "t_valid_date";
    public static final String ATTR_REMARKS = "s_remarks";
    public static final String ATTR_ACCEPT_REJECT_COLUMN = "t_accept_reject_column";

    public static IDfSysObject createDocument(IDfSession dfSession) throws DfException {
        throwIfNotTransactionActive(dfSession);

        IDfSysObject result = (IDfSysObject)dfSession.newObject(DOCUMENT_TYPE_NAME);
        result.link(DOCUMENT_FOLDER);
        return result;
    }

    public static DossierKeyDetails getKeyDetailsFromMQ(MCDocInfoModifySVOType documentXmlObject, VODetailsType voDetailsXmlObject) throws DfException {
        DossierKeyDetails result = new DossierKeyDetails();
        result.dossierId = null;
        result.branchCode = documentXmlObject.getSVODetails().getRegNum();
        result.customerNumber = getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber());
        result.passportNumber = voDetailsXmlObject.getDealPassport();
        if (result.passportNumber == null || result.passportNumber.trim().isEmpty()) {
            result.contractNumber = voDetailsXmlObject.getContractNumber();
            result.contractDate = voDetailsXmlObject.getContractDate().toGregorianCalendar().getTime();
        }
        return result;
    }

    public static void saveFieldsToDocument(IDfSysObject document, MCDocInfoModifySVOType documentXmlObject, VODetailsType voDetailsXmlObject, String dossierId, String docSourceCode, String docSourceId, VersionRecordDetails versionRecordDetails) throws DfException {
        throwIfNotTransactionActive(document.getSession());

        String passportNumber = voDetailsXmlObject.getDealPassport();
        String passportTypeCode = null;
        if (passportNumber != null && passportNumber.trim().length() >= 20) {
            passportTypeCode = passportNumber.substring(19,20);
        }

        versionRecordDetails.setIdWithHistory(document, ATTR_DOSSIER, new DfId(dossierId));
        versionRecordDetails.setStringWithHistory(document, ATTR_BRANCH_CODE, documentXmlObject.getSVODetails().getRegNum());
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NUMBER, getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber()));
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NAME, documentXmlObject.getCustomer().getCustomerName());
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_NUMBER, passportNumber);
        versionRecordDetails.setStringWithHistory(document, ATTR_CONTRACT_NUMBER, voDetailsXmlObject.getContractNumber());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_CONTRACT_DATE, voDetailsXmlObject.getContractDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_LAST_CHANGE_DATE, documentXmlObject.getModificationDateTime());
        versionRecordDetails.setStringWithHistory(document, ATTR_LAST_CHANGE_AUTHOR, documentXmlObject.getUserId());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_CODE, docSourceCode);
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_ID, docSourceId);
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_TYPE_CODE, passportTypeCode);

        versionRecordDetails.setStringWithHistory(document, ATTR_AUTHOR_DEPARTMENT_CODE, documentXmlObject.getUserDep());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_CHANNEL_NAME, documentXmlObject.getSVODetails().getSystemSource());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_CHANNEL_REF_ID, documentXmlObject.getSVODetails().getSystemRefId());
        versionRecordDetails.setStringWithHistory(document, ATTR_CONTENT_URL, documentXmlObject.getSVODetails().getURLScan());

        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_NUMBER, documentXmlObject.getSVODetails().getDocNumber());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_DOC_DATE, documentXmlObject.getSVODetails().getDocDate());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE_CODE, documentXmlObject.getSVODetails().getStatus().value());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE, documentXmlObject.getSVODetails().getStatusName());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE_COMMENT, documentXmlObject.getSVODetails().getStatusComment());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_ACCEPT_DATE, documentXmlObject.getSVODetails().getAcceptDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_REJECT_DATE, documentXmlObject.getSVODetails().getRejectDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_RECEIVE_DATE, documentXmlObject.getSVODetails().getReceiveDate());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_CHANGED, documentXmlObject.getSVODetails().isIsChanged());
        versionRecordDetails.setStringWithHistory(document, ATTR_BANK_NAME, documentXmlObject.getSVODetails().getBankName());
        versionRecordDetails.setStringWithHistory(document, ATTR_BANK_COUNTRY_CODE, documentXmlObject.getSVODetails().getBankCountryCode());
        versionRecordDetails.setStringWithHistory(document, ATTR_ACCOUNT, documentXmlObject.getSVODetails().getAccount());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_URGENT, documentXmlObject.getSVODetails().isUrgency());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_VIP, documentXmlObject.getSVODetails().isIsVIP());

        versionRecordDetails.setIntWithHistory(document, ATTR_DETAIL_INDEX, voDetailsXmlObject.getIndex());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_DETAIL_DATE, voDetailsXmlObject.getDate());
        versionRecordDetails.setStringWithHistory(document, ATTR_VO_CODE, voDetailsXmlObject.getVOCode());
        versionRecordDetails.setStringWithHistory(document, ATTR_PAYMENT_NUMBER, voDetailsXmlObject.getPaymentNumber());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_PAYMENT_DATE, voDetailsXmlObject.getPaymentDate());
        versionRecordDetails.setBigDecimalWithHistory(document, ATTR_PAYMENT_AMOUNT, voDetailsXmlObject.getPaymentAmount());
        versionRecordDetails.setStringWithHistory(document, ATTR_PAYMENT_CURRENCY_CODE, voDetailsXmlObject.getPaymentCurrency());
        versionRecordDetails.setIntWithHistory(document, ATTR_PAYMENT_FLAG, voDetailsXmlObject.getOperationType());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_VALID_DATE, voDetailsXmlObject.getValidDate());
        versionRecordDetails.setStringWithHistory(document, ATTR_REMARKS, voDetailsXmlObject.getRemarks());

        Date lastDesisionDate = null;
        if (documentXmlObject.getSVODetails().getAcceptDate() != null) {
            lastDesisionDate = documentXmlObject.getSVODetails().getAcceptDate().toGregorianCalendar().getTime();
        }
        if (documentXmlObject.getSVODetails().getRejectDate() != null) {
            Date rejectDate = documentXmlObject.getSVODetails().getRejectDate().toGregorianCalendar().getTime();
            if (rejectDate != null && (lastDesisionDate == null || rejectDate.compareTo(lastDesisionDate) > 0)) {
                lastDesisionDate = rejectDate;
            }
        }
        versionRecordDetails.setTimeWithHistory(document, ATTR_ACCEPT_REJECT_COLUMN, lastDesisionDate != null ? new DfTime(lastDesisionDate) : DfTime.DF_NULLDATE);

        document.setACL(getBranchACL(document.getSession(), document.getString(ATTR_BRANCH_CODE)));
        document.setObjectName(getDocumentDescription(document));
        document.save();
    }

    public static String getDocumentDescription(IDfPersistentObject document) throws DfException {
        return DOCUMENT_TYPE_DISPLAY_NAME + " ("  + document.getString(ATTR_CUSTOMER_NUMBER) + ")";
    }

    public static IDfSysObject searchSVODetailObject(IDfSession dfSession, String docSourceCode, String docSourceId, int detailIndex) throws DfException {
        return (IDfSysObject)dfSession.getObjectByQualification(DOCUMENT_TYPE_NAME + " where " + ATTR_DOC_SOURCE_CODE + " = '" + docSourceCode + "' AND " + ATTR_DOC_SOURCE_ID + " = '" + docSourceId + "' AND " + ATTR_DETAIL_INDEX + " = " + detailIndex);
    }
}
