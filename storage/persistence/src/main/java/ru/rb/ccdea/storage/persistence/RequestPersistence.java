package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.DfUtil;
import ru.rb.ccdea.adapters.mq.binding.request.MCDocInfoModifyZAType;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RequestPersistence extends BaseDocumentPersistence{

    public static String DOCUMENT_FOLDER = HOME_FOLDER + "/request";
    public static String DOCUMENT_TYPE_NAME = "ccdea_request";
    public static String DOCUMENT_TYPE_DISPLAY_NAME = "Заявление";

    public static final String ATTR_ORIGIN_DATE = "t_origin_date";
    public static final String ATTR_RECEIVE_DATE = "t_receive_date";
    public static final String ATTR_ACCEPT_DATE = "t_accept_date";
    public static final String ATTR_REJECT_DATE = "t_reject_date";
    public static final String ATTR_ACCEPT_REJECT_COLUMN = "t_accept_reject_column";
    public static final String ATTR_STATE = "s_state";
    public static final String ATTR_STATE_COMMENT = "s_state_comment";
    
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public static IDfSysObject createDocument(IDfSession dfSession) throws DfException {
        throwIfNotTransactionActive(dfSession);

        IDfSysObject result = (IDfSysObject)dfSession.newObject(DOCUMENT_TYPE_NAME);
        result.link(DOCUMENT_FOLDER);
        return result;
    }

    /**
     * 
     * @param documentXmlObject
     * @param passportNumber
     * @return
     * @throws DfException
     */
	public static DossierKeyDetails getKeyDetailsFromMQ(MCDocInfoModifyZAType documentXmlObject, String passportNumber)
			throws DfException {
		DossierKeyDetails result = new DossierKeyDetails();
		result.dossierId = null;
		result.branchCode = documentXmlObject.getZADetails().getRegNum();
		result.customerNumber = getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber());
		result.passportNumber = passportNumber;
		return result;
	}

	/**
	 * 
	 * @param documentXmlObject
	 * @param contractNumber
	 * @param contractDate
	 * @return
	 * @throws DfException
	 */
	public static DossierKeyDetails getKeyDetailsFromMQ(MCDocInfoModifyZAType documentXmlObject, String contractNumber,
			Date contractDate) throws DfException {
		DossierKeyDetails result = new DossierKeyDetails();
		result.dossierId = null;
		result.branchCode = documentXmlObject.getZADetails().getRegNum();
		result.customerNumber = getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber());
		result.contractNumber = contractNumber;
		result.contractDate = contractDate;
		return result;
	}

    public static void saveFieldsToDocument(IDfSysObject document, MCDocInfoModifyZAType documentXmlObject, String passportNumber, String dossierId, String docSourceCode, String docSourceId, VersionRecordDetails versionRecordDetails) throws DfException {
        throwIfNotTransactionActive(document.getSession());

        String passportTypeCode = null;
        if (passportNumber != null && passportNumber.trim().length() >= 20) {
            passportTypeCode = passportNumber.substring(19,20);
        }

        versionRecordDetails.setIdWithHistory(document, ATTR_DOSSIER, new DfId(dossierId));
        versionRecordDetails.setStringWithHistory(document, ATTR_BRANCH_CODE, documentXmlObject.getZADetails().getRegNum());
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NUMBER, getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber()));
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NAME, documentXmlObject.getCustomer().getCustomerName());
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_NUMBER, passportNumber);
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_LAST_CHANGE_DATE, documentXmlObject.getModificationDateTime());
        versionRecordDetails.setStringWithHistory(document, ATTR_LAST_CHANGE_AUTHOR, documentXmlObject.getUserId());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_CODE, docSourceCode);
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_ID, docSourceId);
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_TYPE_CODE, passportTypeCode);

        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_ORIGIN_DATE, documentXmlObject.getZADetails().getOriginDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_RECEIVE_DATE, documentXmlObject.getZADetails().getReceiveDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_ACCEPT_DATE, documentXmlObject.getZADetails().getAcceptDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_REJECT_DATE, documentXmlObject.getZADetails().getRejectDate());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE, documentXmlObject.getZADetails().getStatus());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE_COMMENT, documentXmlObject.getZADetails().getStatusDesc());

        Date lastDesisionDate = null;
        if (documentXmlObject.getZADetails().getAcceptDate() != null) {
            lastDesisionDate = documentXmlObject.getZADetails().getAcceptDate().toGregorianCalendar().getTime();
        }
        if (documentXmlObject.getZADetails().getRejectDate() != null) {
            Date rejectDate = documentXmlObject.getZADetails().getRejectDate().toGregorianCalendar().getTime();
            if (lastDesisionDate == null || rejectDate.compareTo(lastDesisionDate) > 0) {
                lastDesisionDate = rejectDate;
            }
        }
        versionRecordDetails.setTimeWithHistory(document, ATTR_ACCEPT_REJECT_COLUMN, lastDesisionDate != null ? new DfTime(lastDesisionDate) : DfTime.DF_NULLDATE);

        document.setACL(getBranchACL(document.getSession(), document.getString(ATTR_BRANCH_CODE)));
        document.setObjectName(getDocumentDescription(document));
        document.save();
    }
    
    public static void saveFieldsToDocument(IDfSysObject document, MCDocInfoModifyZAType documentXmlObject, String contractNumber, Date contractDate, String dossierId, String docSourceCode, String docSourceId, VersionRecordDetails versionRecordDetails) throws DfException {
        throwIfNotTransactionActive(document.getSession());

        String passportTypeCode = "БП";

        versionRecordDetails.setIdWithHistory(document, ATTR_DOSSIER, new DfId(dossierId));
        versionRecordDetails.setStringWithHistory(document, ATTR_BRANCH_CODE, documentXmlObject.getZADetails().getRegNum());
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NUMBER, getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber()));
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NAME, documentXmlObject.getCustomer().getCustomerName());
        versionRecordDetails.setStringWithHistory(document, ATTR_CONTRACT_NUMBER, contractNumber);
        versionRecordDetails.setTimeWithHistory(document, ATTR_CONTRACT_DATE, new DfTime(contractDate));
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_LAST_CHANGE_DATE, documentXmlObject.getModificationDateTime());
        versionRecordDetails.setStringWithHistory(document, ATTR_LAST_CHANGE_AUTHOR, documentXmlObject.getUserId());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_CODE, docSourceCode);
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_ID, docSourceId);
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_TYPE_CODE, passportTypeCode);

        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_ORIGIN_DATE, documentXmlObject.getZADetails().getOriginDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_RECEIVE_DATE, documentXmlObject.getZADetails().getReceiveDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_ACCEPT_DATE, documentXmlObject.getZADetails().getAcceptDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_REJECT_DATE, documentXmlObject.getZADetails().getRejectDate());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE, documentXmlObject.getZADetails().getStatus());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE_COMMENT, documentXmlObject.getZADetails().getStatusDesc());

        Date lastDesisionDate = null;
        if (documentXmlObject.getZADetails().getAcceptDate() != null) {
            lastDesisionDate = documentXmlObject.getZADetails().getAcceptDate().toGregorianCalendar().getTime();
        }
        if (documentXmlObject.getZADetails().getRejectDate() != null) {
            Date rejectDate = documentXmlObject.getZADetails().getRejectDate().toGregorianCalendar().getTime();
            if (lastDesisionDate == null || rejectDate.compareTo(lastDesisionDate) > 0) {
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

	public static IDfSysObject searchRequestObject(IDfSession dfSession, String docSourceCode, String docSourceId,
			String passportNumber) throws DfException {
		return (IDfSysObject) dfSession.getObjectByQualification(DOCUMENT_TYPE_NAME + " where " + ATTR_DOC_SOURCE_CODE
				+ " = " + DfUtil.toQuotedString(docSourceCode) + " AND " + ATTR_DOC_SOURCE_ID + " = " + DfUtil.toQuotedString(docSourceId) + " AND "
				+ ATTR_PASSPORT_NUMBER + " = " + DfUtil.toQuotedString(passportNumber));
	}
	
	public static IDfSysObject searchRequestObject(IDfSession dfSession, String docSourceCode, String docSourceId,
			String contractNumber, Date contractDate) throws DfException {
		return (IDfSysObject) dfSession.getObjectByQualification(DOCUMENT_TYPE_NAME + " where " + ATTR_DOC_SOURCE_CODE
				+ " = " + DfUtil.toQuotedString(docSourceCode) + " AND " + ATTR_DOC_SOURCE_ID + " = " + DfUtil.toQuotedString(docSourceId) + " AND "
				+ ATTR_CONTRACT_NUMBER + " = " + DfUtil.toQuotedString(contractNumber) + " AND "
				+ ATTR_CONTRACT_DATE + " = date('" + dateFormat.format(contractDate) + "','dd.mm.yyyy')");
	}
}
