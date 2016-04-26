package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;

import ru.rb.ccdea.adapters.mq.binding.passport.ContractorType;
import ru.rb.ccdea.adapters.mq.binding.passport.MCDocInfoModifyPSType;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;

import java.util.List;

public class PassportPersistence extends BaseDocumentPersistence {

    public static String DOCUMENT_FOLDER = HOME_FOLDER + "/passport";
    public static String DOCUMENT_TYPE_NAME = "ccdea_passport";
    public static String DOCUMENT_TYPE_DISPLAY_NAME = "ПС";
    public static String DOCUMENT_TYPE_FULL_DISPLAY_NAME = "Паспорт сделки";

    protected static final String ATTR_PASSPORT_DATE = "t_passport_date";
    protected static final String ATTR_PASSPORT_CLOSE_DATE = "t_close_date";
    protected static final String ATTR_PASSPORT_CLOSE_REASON = "s_close_reason";
    protected static final String ATTR_CONTRACTOR_COLUMN = "s_contractor_column";
    protected static final String ATTR_NO_AMOUNT = "b_no_amount";
    protected static final String ATTR_AMOUNT = "d_amount";
    protected static final String ATTR_CONTRACT_END_DATE = "t_contract_end_date";
    protected static final String ATTR_CONTRACT_CURRENCY_CODE = "s_contract_currency_code";
    protected static final String ATTR_RECEIVE_DATE = "t_receive_date";
    protected static final String ATTR_ACCEPT_DATE = "t_accept_date";

    protected static final String ATTR_CONTRACTOR_NAME_R = "s_contractor_name_r";
    protected static final String ATTR_CONTRACTOR_COUNTRY_CODE_R = "s_contractor_country_code_r";

    public static IDfSysObject createDocument(IDfSession dfSession) throws DfException {
        throwIfNotTransactionActive(dfSession);

        IDfSysObject result = (IDfSysObject)dfSession.newObject(DOCUMENT_TYPE_NAME);
        result.link(DOCUMENT_FOLDER);
        return result;
    }

    public static DossierKeyDetails getKeyDetailsFromMQ(MCDocInfoModifyPSType documentXmlObject) throws DfException {
        DossierKeyDetails result = new DossierKeyDetails();
        result.dossierId = null;
        result.branchCode = documentXmlObject.getPSDetails().getRegNum();
        result.customerNumber = getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber());
        result.passportNumber = documentXmlObject.getPSDetails().getDealPassport();
        return result;
    }

    public static void saveFieldsToDocument(IDfSysObject document, MCDocInfoModifyPSType documentXmlObject, String dossierId, String docSourceCode, String docSourceId, VersionRecordDetails versionRecordDetails) throws DfException {
        throwIfNotTransactionActive(document.getSession());

        String passportNumber = documentXmlObject.getPSDetails().getDealPassport();
        String passportTypeCode = null;
        if (passportNumber != null && passportNumber.trim().length() >= 20) {
            passportTypeCode = passportNumber.substring(19,20);
        }

        versionRecordDetails.setIdWithHistory(document, ATTR_DOSSIER, new DfId(dossierId));
        versionRecordDetails.setStringWithHistory(document, ATTR_BRANCH_CODE, documentXmlObject.getPSDetails().getRegNum());
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NUMBER, getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber()));
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NAME, documentXmlObject.getCustomer().getCustomerName());
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_NUMBER, passportNumber);
        versionRecordDetails.setStringWithHistory(document, ATTR_CONTRACT_NUMBER, documentXmlObject.getPSDetails().getContractNumber());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_CONTRACT_DATE, documentXmlObject.getPSDetails().getContractDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_LAST_CHANGE_DATE, documentXmlObject.getModificationDateTime());
        versionRecordDetails.setStringWithHistory(document, ATTR_LAST_CHANGE_AUTHOR, documentXmlObject.getUserId());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_CODE, docSourceCode);
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_ID, docSourceId);
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_TYPE_CODE, passportTypeCode);

        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_PASSPORT_DATE, documentXmlObject.getPSDetails().getDealPassportDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_PASSPORT_CLOSE_DATE, documentXmlObject.getPSDetails().getCloseDate());
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_CLOSE_REASON, documentXmlObject.getPSDetails().getCloseDesc());
        if (isNotnullTrue(documentXmlObject.getPSDetails().isNoAmount())) {
            versionRecordDetails.setBooleanWithHistory(document, ATTR_NO_AMOUNT, true);
            versionRecordDetails.setBigDecimalWithHistory(document, ATTR_AMOUNT, null);
        }
        else {
            versionRecordDetails.setBooleanWithHistory(document, ATTR_NO_AMOUNT, false);
            versionRecordDetails.setBigDecimalWithHistory(document, ATTR_AMOUNT, documentXmlObject.getPSDetails().getAmount());
        }
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_CONTRACT_END_DATE, documentXmlObject.getPSDetails().getDateExpire());
        versionRecordDetails.setStringWithHistory(document, ATTR_CONTRACT_CURRENCY_CODE, documentXmlObject.getPSDetails().getCurrency());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_RECEIVE_DATE, documentXmlObject.getPSDetails().getReceiveDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_ACCEPT_DATE, documentXmlObject.getPSDetails().getAcceptDate());

        List<ContractorType> contractorList = documentXmlObject.getPSDetails().getContractor();
        int currentContractorNameCount = document.getValueCount(ATTR_CONTRACTOR_NAME_R);
        int currentContractorCountryCount = document.getValueCount(ATTR_CONTRACTOR_COUNTRY_CODE_R);
        int index = 0;
        if (contractorList != null) {
            for (ContractorType contractor : contractorList) {
                versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_CONTRACTOR_NAME_R, index, contractor.getContractorName());
                versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_CONTRACTOR_COUNTRY_CODE_R, index, contractor.getContractorCountry());
                index++;
            }
        }
        while (index < currentContractorNameCount || index < currentContractorCountryCount) {
            if (index < currentContractorNameCount) {
                try {
                	versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_CONTRACTOR_NAME_R, index);
                } catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_CONTRACTOR_NAME_R
							+ " с индексом " + index, null, ex);
				}
            }
            if (index < currentContractorCountryCount) {
            	try {
            		versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_CONTRACTOR_COUNTRY_CODE_R, index);
            	} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_CONTRACTOR_COUNTRY_CODE_R
							+ " с индексом " + index, null, ex);
				}
            }
            index++;
        }

        if (contractorList.size() > 1) {
            document.setString(ATTR_CONTRACTOR_COLUMN, CONTRACTOR_COLUMN_MULTI);
        }
        else {
            document.setString(ATTR_CONTRACTOR_COLUMN, contractorList.get(0).getContractorName());
        }

        document.setACL(getBranchACL(document.getSession(), document.getString(ATTR_BRANCH_CODE)));
        document.setObjectName(getDocumentDescription(document));
        document.save();
    }

    public static String getDocumentDescription(IDfPersistentObject document) throws DfException {
        return DOCUMENT_TYPE_DISPLAY_NAME + " ("  + document.getString(ATTR_CUSTOMER_NUMBER) + ")";
    }

    public static IDfSysObject searchPassportObject(IDfSession dfSession, String passportNumber) throws DfException {
        return (IDfSysObject)dfSession.getObjectByQualification(DOCUMENT_TYPE_NAME + " where " + ATTR_PASSPORT_NUMBER + " = '" + passportNumber + "'");
    }
}
