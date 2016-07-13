package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;

import ru.rb.ccdea.adapters.mq.binding.contract.*;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;

import java.util.List;

public class ContractPersistence extends BaseDocumentPersistence{

    public static String DOCUMENT_FOLDER = HOME_FOLDER + "/contract";
    public static String DOCUMENT_TYPE_NAME = "ccdea_contract";
    public static String DOCUMENT_TYPE_DISPLAY_NAME = "Договор";

    public static final String ATTR_DOC_TYPE = "s_doc_type";
    public static final String ATTR_RECEIVE_DATE = "t_receive_date";
    public static final String ATTR_STATE = "s_state";
    public static final String ATTR_STATE_COMMENT = "s_state_comment";
    public static final String ATTR_AMOUNT = "d_amount";
    public static final String ATTR_CURRENCY_CODE = "s_currency_code";
    public static final String ATTR_IS_URGENT = "b_is_urgent";
    public static final String ATTR_IS_VIP = "b_is_vip";
    public static final String ATTR_IS_AML = "b_is_aml";
    public static final String ATTR_IS_PAYMENT_ADVANCE = "b_is_payment_advance";
    public static final String ATTR_IS_PAYMENT_DELAY = "b_is_payment_delay";
    public static final String ATTR_IS_CLOSED = "b_is_closed";
    public static final String ATTR_EXPIRE_DATE = "t_expire_date";
    public static final String ATTR_CLOSE_DATE = "t_close_date";
    public static final String ATTR_ARCHIVE_DOSSIER_NUMBER = "s_archive_dossier_number";
    public static final String ATTR_IS_THIRD_PARTY = "b_is_third_party";
    public static final String ATTR_REPORT402 = "b_report402";
    public static final String ATTR_REPORT405 = "b_report405";
    public static final String ATTR_REPORT406 = "b_report406";
    public static final String ATTR_NO_GENERAL_AMOUNT = "b_no_general_amount";
    public static final String ATTR_GENERAL_AMOUNT = "d_general_amount";
    public static final String ATTR_GENERAL_PAYMENT_AMOUNT = "d_general_payment_amount";
    public static final String ATTR_GENERAL_CURRENCY_CODE = "s_general_currency_code";
    public static final String ATTR_IS_REORGANIZATION = "b_is_reorganization";
    public static final String ATTR_REORGANIZATION_INFO = "s_reorganization_info";
    public static final String ATTR_CUSTOMER_CAPITAL = "d_customer_capital";
    public static final String ATTR_CUSTOMER_REG_DATE = "t_customer_reg_date";
    public static final String ATTR_SHIPPING_COUNTRY_CODE = "s_shipping_country_code";
    public static final String ATTR_SHIPPER_COUNTRY_CODE = "s_shipper_country_code";
    public static final String ATTR_PORT_COUNTRY_CODE = "s_port_country_code";
    public static final String ATTR_SHIP_NAME = "s_ship_name";
    public static final String ATTR_THIRD_COUNTRY_CODE = "s_third_country_code";
    public static final String ATTR_THIRD_BANK_COUNTRY_CODE = "s_third_bank_country_code";
    public static final String ATTR_COMMENT = "s_comment";

    public static final String ATTR_SIGNING_DATE = "t_signing_date";

    public static final String ATTR_SUMS_ORDER_NUM_R = "n_sums_order_num_r";
    public static final String ATTR_SUMS_AMOUNT_R = "d_sums_amount_r";
    public static final String ATTR_SUMS_CURRENCY_CODE_R = "s_sums_currency_code_r";
    public static final String ATTR_CONTR_ORDER_NUM_R = "n_contr_order_num_r";
    public static final String ATTR_CONTR_NAME_R = "s_contr_name_r";
    public static final String ATTR_CONTR_COUNTRY_CODE_R = "s_contr_country_code_r";
    public static final String ATTR_CONTR_BANK_COUNTRY_CODE_R = "s_contr_bank_country_code_r";
    public static final String ATTR_402_SERVICE_CODE_R = "s_402_service_code_r";
    public static final String ATTR_402_SERVICE_NAME_R = "s_402_service_name_r";
    public static final String ATTR_CONTRACT_TYPE_CODE_R = "s_contract_type_code_r";

    public static final String ATTR_CONTRACTOR_COLUMN = "s_contractor_column";

    public static IDfSysObject createDocument(IDfSession dfSession) throws DfException {
        throwIfNotTransactionActive(dfSession);

        IDfSysObject result = (IDfSysObject)dfSession.newObject(DOCUMENT_TYPE_NAME);
        result.link(DOCUMENT_FOLDER);
        return result;
    }

    public static DossierKeyDetails getKeyDetailsFromMQ(MCDocInfoModifyContractType documentXmlObject) throws DfException {
        DossierKeyDetails result = new DossierKeyDetails();
        result.dossierId = null;
        result.branchCode = documentXmlObject.getContractDetails().getRegNum();
        result.customerNumber = getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber());
        result.passportNumber = documentXmlObject.getContractDetails().getDealPassport();
        if (result.passportNumber == null || result.passportNumber.trim().isEmpty()) {
            result.contractNumber = documentXmlObject.getContractDetails().getDocNumber();
            if(documentXmlObject.getContractDetails() != null && documentXmlObject.getContractDetails().getDocDate() != null) {
            	result.contractDate = documentXmlObject.getContractDetails().getDocDate().toGregorianCalendar().getTime();
            }
        }
        return result;
    }

    public static void saveFieldsToDocument(IDfSysObject document, MCDocInfoModifyContractType documentXmlObject, String dossierId, String docSourceCode, String docSourceId, VersionRecordDetails versionRecordDetails) throws DfException {
        throwIfNotTransactionActive(document.getSession());

        String passportNumber = documentXmlObject.getContractDetails().getDealPassport();
        String passportTypeCode = null;
        if (passportNumber != null && passportNumber.trim().length() >= 20) {
            passportTypeCode = passportNumber.substring(19,20);
        }

        versionRecordDetails.setIdWithHistory(document, ATTR_DOSSIER, new DfId(dossierId));
        versionRecordDetails.setStringWithHistory(document, ATTR_BRANCH_CODE, documentXmlObject.getContractDetails().getRegNum());
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NUMBER, getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber()));
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NAME, documentXmlObject.getCustomer().getCustomerName());
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_NUMBER, passportNumber);
        versionRecordDetails.setStringWithHistory(document, ATTR_CONTRACT_NUMBER, documentXmlObject.getContractDetails().getDocNumber());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_CONTRACT_DATE, documentXmlObject.getContractDetails().getDocDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_LAST_CHANGE_DATE, documentXmlObject.getModificationDateTime());
        versionRecordDetails.setStringWithHistory(document, ATTR_LAST_CHANGE_AUTHOR, documentXmlObject.getUserId());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_CODE, docSourceCode);
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_ID, docSourceId);
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_TYPE_CODE, passportTypeCode);

        versionRecordDetails.setStringWithHistory(document, ATTR_AUTHOR_DEPARTMENT_CODE, documentXmlObject.getUserDep());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_CHANNEL_NAME, documentXmlObject.getContractDetails().getSystemSource());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_CHANNEL_REF_ID, documentXmlObject.getContractDetails().getSystemRefId());
        versionRecordDetails.setStringWithHistory(document, ATTR_CONTENT_URL, documentXmlObject.getContractDetails().getURLScan());

        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_TYPE, documentXmlObject.getContractDetails().getDocType());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_RECEIVE_DATE, documentXmlObject.getContractDetails().getReceiveDate());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE, documentXmlObject.getContractDetails().getStatusName());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE_COMMENT, documentXmlObject.getContractDetails().getStatusComment());
        versionRecordDetails.setBigDecimalWithHistory(document, ATTR_AMOUNT, documentXmlObject.getContractDetails().getDocAmount());
        versionRecordDetails.setStringWithHistory(document, ATTR_CURRENCY_CODE, documentXmlObject.getContractDetails().getDocCurrency());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_URGENT, documentXmlObject.getContractDetails().isUrgency());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_VIP, documentXmlObject.getContractDetails().isIsVIP());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_AML, documentXmlObject.getContractDetails().isInfoAML());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_PAYMENT_ADVANCE, documentXmlObject.getContractDetails().isPaymentAdvance());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_PAYMENT_DELAY, documentXmlObject.getContractDetails().isPaymentDelay());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_CLOSED, documentXmlObject.getContractDetails().isIsClosed());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_EXPIRE_DATE, documentXmlObject.getContractDetails().getDateExpire());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_CLOSE_DATE, documentXmlObject.getContractDetails().getDateFinish());
        versionRecordDetails.setStringWithHistory(document, ATTR_ARCHIVE_DOSSIER_NUMBER, documentXmlObject.getContractDetails().getArchiveNumber());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_THIRD_PARTY, documentXmlObject.getContractDetails().isThirdPartySign());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_REPORT402, documentXmlObject.getContractDetails().isFlagReport402());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_REPORT405, documentXmlObject.getContractDetails().isFlagReport405());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_REPORT406, documentXmlObject.getContractDetails().isFlagReport406());
        if (isNotnullTrue(documentXmlObject.getContractDetails().isNoGeneralAmount())) {
            versionRecordDetails.setBooleanWithHistory(document, ATTR_NO_GENERAL_AMOUNT, true);
            versionRecordDetails.setBigDecimalWithHistory(document, ATTR_GENERAL_AMOUNT, null);
        }
        else {
            versionRecordDetails.setBooleanWithHistory(document, ATTR_NO_GENERAL_AMOUNT, false);
            versionRecordDetails.setBigDecimalWithHistory(document, ATTR_GENERAL_AMOUNT, documentXmlObject.getContractDetails().getGeneralAmount());
        }
        versionRecordDetails.setBigDecimalWithHistory(document, ATTR_GENERAL_PAYMENT_AMOUNT, documentXmlObject.getContractDetails().getGeneralPayments());
        versionRecordDetails.setStringWithHistory(document, ATTR_GENERAL_CURRENCY_CODE, documentXmlObject.getContractDetails().getGeneralCurrencyCode());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_REORGANIZATION, documentXmlObject.getContractDetails().isReorganizationSign());
        versionRecordDetails.setStringWithHistory(document, ATTR_REORGANIZATION_INFO, documentXmlObject.getContractDetails().getReorganizationInfo());
        versionRecordDetails.setBigDecimalWithHistory(document, ATTR_CUSTOMER_CAPITAL, documentXmlObject.getContractDetails().getNominalCapital());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_CUSTOMER_REG_DATE, documentXmlObject.getContractDetails().getDateCustomerRegistration());
        versionRecordDetails.setStringWithHistory(document, ATTR_SHIPPING_COUNTRY_CODE, documentXmlObject.getContractDetails().getCountryShippingCode());
        versionRecordDetails.setStringWithHistory(document, ATTR_SHIPPER_COUNTRY_CODE, documentXmlObject.getContractDetails().getCountryShipperCode());
        versionRecordDetails.setStringWithHistory(document, ATTR_PORT_COUNTRY_CODE, documentXmlObject.getContractDetails().getCountryPortCode());
        versionRecordDetails.setStringWithHistory(document, ATTR_SHIP_NAME, documentXmlObject.getContractDetails().getShipName());
        versionRecordDetails.setStringWithHistory(document, ATTR_THIRD_COUNTRY_CODE, documentXmlObject.getContractDetails().getCountryThirdCode());
        versionRecordDetails.setStringWithHistory(document, ATTR_THIRD_BANK_COUNTRY_CODE, documentXmlObject.getContractDetails().getCountryThirdBankCode());
        versionRecordDetails.setStringWithHistory(document, ATTR_COMMENT, documentXmlObject.getContractDetails().getComment());

        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_SIGNING_DATE, documentXmlObject.getContractDetails().getDateSigning());

        List<DocumentContractAmountType> contractAmountList = documentXmlObject.getContractDetails().getDocumentContractAmount();
        int currentSumsOrderNumCount = document.getValueCount(ATTR_SUMS_ORDER_NUM_R);
        int currentSumsAmountCount = document.getValueCount(ATTR_SUMS_AMOUNT_R);
        int currentSumsCurrencyCodeCount = document.getValueCount(ATTR_SUMS_CURRENCY_CODE_R);
        int contractAmountIndex = 0;
        for (DocumentContractAmountType contractAmount : contractAmountList) {
            versionRecordDetails.setRepeatingIntWithHistory(document, ATTR_SUMS_ORDER_NUM_R, contractAmountIndex, contractAmount.getOrderNum());
            versionRecordDetails.setRepeatingBigDecimalWithHistory(document, ATTR_SUMS_AMOUNT_R, contractAmountIndex, contractAmount.getAmount());
            versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_SUMS_CURRENCY_CODE_R, contractAmountIndex, contractAmount.getCurrencyCode());
            contractAmountIndex++;
        }
        List<DocumentContractPartyType> contractPartyList = documentXmlObject.getContractDetails().getDocumentContractParty();
        int currentContrOrderNumCount = document.getValueCount(ATTR_CONTR_ORDER_NUM_R);
        int currentContrNameCount = document.getValueCount(ATTR_CONTR_NAME_R);
        int currentContrCountryCodeCount = document.getValueCount(ATTR_CONTR_COUNTRY_CODE_R);
        int currentContrBankCountryCodeCount = document.getValueCount(ATTR_CONTR_BANK_COUNTRY_CODE_R);
        int contractPartyIndex = 0;
        for (DocumentContractPartyType contractParty : contractPartyList) {
            versionRecordDetails.setRepeatingIntWithHistory(document, ATTR_CONTR_ORDER_NUM_R, contractPartyIndex, contractParty.getOrderNum());
            versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_CONTR_NAME_R, contractPartyIndex, contractParty.getName());
            versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_CONTR_COUNTRY_CODE_R, contractPartyIndex, contractParty.getCountryCode());
            versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_CONTR_BANK_COUNTRY_CODE_R, contractPartyIndex, contractParty.getCountryBankCode());
            contractPartyIndex++;
        }
        List<DocumentContractRep402Type> contractRep402List = documentXmlObject.getContractDetails().getDocumentContractRep402();
        int current402ServiceCodeCount = document.getValueCount(ATTR_402_SERVICE_CODE_R);
        int current402ServiceNameCount = document.getValueCount(ATTR_402_SERVICE_NAME_R);
        int contractRep402Index = 0;
        for (DocumentContractRep402Type contractRep402 : contractRep402List) {
            versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_402_SERVICE_CODE_R, contractRep402Index, contractRep402.getServiceCode());
            versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_402_SERVICE_NAME_R, contractRep402Index, contractRep402.getServiceNameForLimit());
            contractRep402Index++;
        }
        List<DocumentContractTypeType> contractTypeList = documentXmlObject.getContractDetails().getDocumentContractType();
        int currentContractTypeCodeCount = document.getValueCount(ATTR_CONTRACT_TYPE_CODE_R);
        int contractTypeIndex = 0;
        for (DocumentContractTypeType contractType : contractTypeList) {
            versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_CONTRACT_TYPE_CODE_R, contractTypeIndex, contractType.getTypeCode());
            contractTypeIndex++;
        }

        boolean isRecordToCLeanExist = true;
        while (isRecordToCLeanExist) {
			isRecordToCLeanExist = false;

			if (contractAmountIndex < currentSumsOrderNumCount) {
				try {
					versionRecordDetails.cleanRepeatingIntWithHistory(document, ATTR_SUMS_ORDER_NUM_R,
							contractAmountIndex);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_SUMS_ORDER_NUM_R
							+ " с индексом " + contractAmountIndex, null, ex);
				}
			}
			if (contractAmountIndex < currentSumsAmountCount) {
				try {
					versionRecordDetails.cleanRepeatingBigDecimalWithHistory(document, ATTR_SUMS_AMOUNT_R,
							contractAmountIndex);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_SUMS_AMOUNT_R
							+ " с индексом " + contractAmountIndex, null, ex);
				}
			}
			if (contractAmountIndex < currentSumsCurrencyCodeCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_SUMS_CURRENCY_CODE_R,
							contractAmountIndex);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_SUMS_CURRENCY_CODE_R
							+ " с индексом " + contractAmountIndex, null, ex);
				}
			}

			if (contractPartyIndex < currentContrOrderNumCount) {
				try {
					versionRecordDetails.cleanRepeatingIntWithHistory(document, ATTR_CONTR_ORDER_NUM_R,
							contractPartyIndex);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_CONTR_ORDER_NUM_R
							+ " с индексом " + contractPartyIndex, null, ex);
				}
			}
			if (contractPartyIndex < currentContrNameCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_CONTR_NAME_R,
							contractPartyIndex);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_CONTR_NAME_R
							+ " с индексом " + contractPartyIndex, null, ex);
				}
			}
			if (contractPartyIndex < currentContrCountryCodeCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_CONTR_COUNTRY_CODE_R,
							contractPartyIndex);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_CONTR_COUNTRY_CODE_R
							+ " с индексом " + contractPartyIndex, null, ex);
				}
			}
			if (contractPartyIndex < currentContrBankCountryCodeCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_CONTR_BANK_COUNTRY_CODE_R,
							contractPartyIndex);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_CONTR_BANK_COUNTRY_CODE_R
							+ " с индексом " + contractPartyIndex, null, ex);
				}
			}

			if (contractRep402Index < current402ServiceCodeCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_402_SERVICE_CODE_R,
							contractRep402Index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_402_SERVICE_CODE_R
							+ " с индексом " + contractRep402Index, null, ex);
				}
			}
			if (contractRep402Index < current402ServiceNameCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_402_SERVICE_NAME_R,
							contractRep402Index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_402_SERVICE_NAME_R
							+ " с индексом " + contractRep402Index, null, ex);
				}
			}

			if (contractTypeIndex < currentContractTypeCodeCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_CONTRACT_TYPE_CODE_R,
							contractTypeIndex);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_CONTRACT_TYPE_CODE_R
							+ " с индексом " + contractTypeIndex, null, ex);
				}
			}

            contractAmountIndex++;
            contractPartyIndex++;
            contractRep402Index++;
            contractTypeIndex++;
        }

        if (contractPartyList.size() > 1) {
            document.setString(ATTR_CONTRACTOR_COLUMN, CONTRACTOR_COLUMN_MULTI);
        }
        else if (contractPartyList.size() == 1) {
            document.setString(ATTR_CONTRACTOR_COLUMN, contractPartyList.get(0).getName());
        }

        document.appendString(ATTR_RP_CONTENT_SOURCE_CODE, docSourceCode);
        document.appendString(ATTR_RP_CONTENT_SOURCE_ID, docSourceId);
        document.setACL(getBranchACL(document.getSession(), document.getString(ATTR_BRANCH_CODE)));
        document.setObjectName(getDocumentDescription(document));
        document.save();
    }

    public static String getDocumentDescription(IDfPersistentObject document) throws DfException {
        return DOCUMENT_TYPE_DISPLAY_NAME + " ("  + document.getString(ATTR_CUSTOMER_NUMBER) + ")";
    }

    public static IDfSysObject searchContractObject(IDfSession dfSession, String docSourceCode, String docSourceId) throws DfException {
        return (IDfSysObject)dfSession.getObjectByQualification(DOCUMENT_TYPE_NAME + " where " + ATTR_DOC_SOURCE_CODE + " = '" + docSourceCode + "' AND " + ATTR_DOC_SOURCE_ID + " = '" + docSourceId + "'");
    }
}
