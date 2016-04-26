package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfTime;
import ru.rb.ccdea.adapters.mq.binding.spd.MCDocInfoModifySPDType;
import ru.rb.ccdea.adapters.mq.binding.spd.PDRecordType;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;

import java.util.Date;
import java.util.List;

public class SPDPersistence extends BaseDocumentPersistence {

    public static String DOCUMENT_FOLDER = HOME_FOLDER + "/spd";
    public static String DOCUMENT_TYPE_NAME = "ccdea_spd";
    public static String DOCUMENT_TYPE_DISPLAY_NAME = "СПД";

    public static final String ATTR_DOC_NUMBER = "s_doc_number";
    public static final String ATTR_DOC_DATE = "t_doc_date";
    public static final String ATTR_DOC_TYPE = "s_doc_type";
    public static final String ATTR_STATE_CODE = "s_state_code";
    public static final String ATTR_STATE = "s_state";
    public static final String ATTR_STATE_COMMENT = "s_state_comment";
    public static final String ATTR_AMOUNT = "d_amount";
    public static final String ATTR_CURRENCY_CODE = "s_currency_code";
    public static final String ATTR_ACCEPT_DATE = "t_accept_date";
    public static final String ATTR_REJECT_DATE = "t_reject_date";
    public static final String ATTR_RECEIVE_DATE = "t_receive_date";
    public static final String ATTR_IS_CHANGED = "b_is_changed";
    public static final String ATTR_BANK_NAME = "s_bank_name";
    public static final String ATTR_IS_URGENT = "b_is_urgent";
    public static final String ATTR_IS_VIP = "b_is_vip";

    public static final String ATTR_PD_INDEX_R = "n_pd_index_r";
    public static final String ATTR_PD_DOC_NUMBER_R = "s_pd_doc_number_r";
    public static final String ATTR_PD_DOC_DATE_R = "t_pd_doc_date_r";
    public static final String ATTR_PD_DOC_KIND_CODE_R = "s_pd_doc_kind_code_r";
    public static final String ATTR_PD_KVALP_R = "s_pd_kvalp_r";
    public static final String ATTR_PD_SUMMAP_R = "d_pd_summap_r";
    public static final String ATTR_PD_SUMMAP2_R = "d_pd_summap2_r";
    public static final String ATTR_PD_KVALK_R = "s_pd_kvalk_r";
    public static final String ATTR_PD_SUMMAK_R = "d_pd_summak_r";
    public static final String ATTR_PD_SUMMAK2_R = "d_pd_summak2_r";
    public static final String ATTR_PD_PRIZ_POST_R = "b_pd_priz_post_r";
    public static final String ATTR_PD_SROK_WAIT_R = "t_pd_srok_wait_r";
    public static final String ATTR_PD_KSTRANA_R = "s_pd_kstrana_r";
    public static final String ATTR_PD_PRIMESH_R = "s_pd_primesh_r";
    public static final String ATTR_PD_DOC_TYPE_R = "s_pd_doc_type_r";
    public static final String ATTR_PD_CHANNEL_NAME_R = "s_pd_channel_name_r";
    public static final String ATTR_PD_CHANNEL_REF_ID_R = "s_pd_channel_ref_id_r";

    public static final String ATTR_ACCEPT_REJECT_COLUMN = "t_accept_reject_column";

    public static IDfSysObject createDocument(IDfSession dfSession) throws DfException {
        throwIfNotTransactionActive(dfSession);

        IDfSysObject result = (IDfSysObject)dfSession.newObject(DOCUMENT_TYPE_NAME);
        result.link(DOCUMENT_FOLDER);
        return result;
    }

    public static DossierKeyDetails getKeyDetailsFromMQ(MCDocInfoModifySPDType documentXmkObject) throws DfException {
        DossierKeyDetails result = new DossierKeyDetails();
        result.dossierId = null;
        result.branchCode = documentXmkObject.getSPDDetails().getRegNum();
        result.customerNumber = getNormalCustomerNumber(documentXmkObject.getCustomer().getCustomerNumber());
        result.passportNumber = documentXmkObject.getSPDDetails().getDealPassport();
        return result;
    }

    public static void saveFieldsToDocument(IDfSysObject document, MCDocInfoModifySPDType documentXmlObject, String dossierId, String docSourceCode, String docSourceId, VersionRecordDetails versionRecordDetails) throws DfException {
        throwIfNotTransactionActive(document.getSession());

        String passportNumber = documentXmlObject.getSPDDetails().getDealPassport();
        String passportTypeCode = null;
        if (passportNumber != null && passportNumber.trim().length() >= 20) {
            passportTypeCode = passportNumber.substring(19,20);
        }

        versionRecordDetails.setIdWithHistory(document, ATTR_DOSSIER, new DfId(dossierId));
        versionRecordDetails.setStringWithHistory(document, ATTR_BRANCH_CODE, documentXmlObject.getSPDDetails().getRegNum());
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NUMBER, getNormalCustomerNumber(documentXmlObject.getCustomer().getCustomerNumber()));
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NAME, documentXmlObject.getCustomer().getCustomerName());
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_NUMBER, passportNumber);
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_LAST_CHANGE_DATE, documentXmlObject.getModificationDateTime());
        versionRecordDetails.setStringWithHistory(document, ATTR_LAST_CHANGE_AUTHOR, documentXmlObject.getUserId());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_CODE, docSourceCode);
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_SOURCE_ID, docSourceId);
        versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_TYPE_CODE, passportTypeCode);

        versionRecordDetails.setStringWithHistory(document, ATTR_AUTHOR_DEPARTMENT_CODE, documentXmlObject.getUserDep());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_CHANNEL_NAME, documentXmlObject.getSPDDetails().getSystemSource());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_CHANNEL_REF_ID, documentXmlObject.getSPDDetails().getSystemRefId());
        versionRecordDetails.setStringWithHistory(document, ATTR_CONTENT_URL, documentXmlObject.getSPDDetails().getURLScan());

        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_NUMBER, documentXmlObject.getSPDDetails().getDocNumber());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_DOC_DATE, documentXmlObject.getSPDDetails().getDocDate());
        versionRecordDetails.setStringWithHistory(document, ATTR_DOC_TYPE, documentXmlObject.getSPDDetails().getDocType());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE_CODE, documentXmlObject.getSPDDetails().getStatus().value());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE, documentXmlObject.getSPDDetails().getStatusName());
        versionRecordDetails.setStringWithHistory(document, ATTR_STATE_COMMENT, documentXmlObject.getSPDDetails().getStatusComment());
        versionRecordDetails.setBigDecimalWithHistory(document, ATTR_AMOUNT, documentXmlObject.getSPDDetails().getDocAmount());
        versionRecordDetails.setStringWithHistory(document, ATTR_CURRENCY_CODE, documentXmlObject.getSPDDetails().getDocCurrency());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_ACCEPT_DATE, documentXmlObject.getSPDDetails().getAcceptDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_REJECT_DATE, documentXmlObject.getSPDDetails().getRejectDate());
        versionRecordDetails.setXMLGregorianCalendarWithHistory(document, ATTR_RECEIVE_DATE, documentXmlObject.getSPDDetails().getReceiveDate());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_CHANGED, documentXmlObject.getSPDDetails().isIsChanged());
        versionRecordDetails.setStringWithHistory(document, ATTR_BANK_NAME, documentXmlObject.getSPDDetails().getBankName());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_URGENT, documentXmlObject.getSPDDetails().isUrgency());
        versionRecordDetails.setBooleanWithHistory(document, ATTR_IS_VIP, documentXmlObject.getSPDDetails().isIsVIP());

        List<PDRecordType> pdRecordList = documentXmlObject.getSPDDetails().getPDDetails();
        int pdRecordIndexCount = document.getValueCount(ATTR_PD_INDEX_R);
        int pdRecordDocNumberCount = document.getValueCount(ATTR_PD_DOC_NUMBER_R);
        int pdRecordDocDateCount = document.getValueCount(ATTR_PD_DOC_DATE_R);
        int pdRecordDocKindCodeCount = document.getValueCount(ATTR_PD_DOC_KIND_CODE_R);
        int pdRecordKvalpCount = document.getValueCount(ATTR_PD_KVALP_R);
        int pdRecordSummapCount = document.getValueCount(ATTR_PD_SUMMAP_R);
        int pdRecordSummap2Count = document.getValueCount(ATTR_PD_SUMMAP2_R);
        int pdRecordKvalkCount = document.getValueCount(ATTR_PD_KVALK_R);
        int pdRecordSummakCount = document.getValueCount(ATTR_PD_SUMMAK_R);
        int pdRecordSummak2Count = document.getValueCount(ATTR_PD_SUMMAK2_R);
        int pdRecordPrizPostCount = document.getValueCount(ATTR_PD_PRIZ_POST_R);
        int pdRecordSrokWaitCount = document.getValueCount(ATTR_PD_SROK_WAIT_R);
        int pdRecordKstranaCount = document.getValueCount(ATTR_PD_KSTRANA_R);
        int pdRecordPrimeshCount = document.getValueCount(ATTR_PD_PRIMESH_R);
        int pdRecordDocTypeCount = document.getValueCount(ATTR_PD_DOC_TYPE_R);
        int pdRecordChannelNameCount = document.getValueCount(ATTR_PD_CHANNEL_NAME_R);
        int pdRecordChannelRefIdCount = document.getValueCount(ATTR_PD_CHANNEL_REF_ID_R);
        int index = 0;
        if (pdRecordList != null) {
            for (PDRecordType pdRecord : pdRecordList) {
                versionRecordDetails.setRepeatingIntWithHistory(document, ATTR_PD_INDEX_R, index, pdRecord.getIndex());
                versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_PD_DOC_NUMBER_R, index, pdRecord.getDocNumber());
                versionRecordDetails.setRepeatingXMLGregorianCalendarWithHistory(document, ATTR_PD_DOC_DATE_R, index, pdRecord.getDocDate());
                versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_PD_DOC_KIND_CODE_R, index, pdRecord.getPdType());
                versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_PD_KVALP_R, index, pdRecord.getKvalp());
                versionRecordDetails.setRepeatingBigDecimalWithHistory(document, ATTR_PD_SUMMAP_R, index, pdRecord.getSummap());
                versionRecordDetails.setRepeatingBigDecimalWithHistory(document, ATTR_PD_SUMMAP2_R, index, pdRecord.getSummap2());
                versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_PD_KVALK_R, index, pdRecord.getKvalk());
                versionRecordDetails.setRepeatingBigDecimalWithHistory(document, ATTR_PD_SUMMAK_R, index, pdRecord.getSummak());
                versionRecordDetails.setRepeatingBigDecimalWithHistory(document, ATTR_PD_SUMMAK2_R, index, pdRecord.getSummak2());
                versionRecordDetails.setRepeatingBooleanWithHistory(document, ATTR_PD_PRIZ_POST_R, index, pdRecord.isPrizPost());
                versionRecordDetails.setRepeatingXMLGregorianCalendarWithHistory(document, ATTR_PD_SROK_WAIT_R, index, pdRecord.getSrokWait());
                versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_PD_KSTRANA_R, index, pdRecord.getKstrana());
                versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_PD_PRIMESH_R, index, pdRecord.getPrimesh());
                versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_PD_DOC_TYPE_R, index, pdRecord.getDocType());
                versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_PD_CHANNEL_NAME_R, index, pdRecord.getSystemSource());
                versionRecordDetails.setRepeatingStringWithHistory(document, ATTR_PD_CHANNEL_REF_ID_R, index, pdRecord.getSystemRefId());
                index++;
            }
        }
        boolean isRecordToCLeanExist = true;
        while (isRecordToCLeanExist) {
			isRecordToCLeanExist = false;
			if (index < pdRecordIndexCount) {
				try {
					versionRecordDetails.cleanRepeatingIntWithHistory(document, ATTR_PD_INDEX_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_INDEX_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordDocNumberCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_PD_DOC_NUMBER_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_DOC_NUMBER_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordDocDateCount) {
				try {
					versionRecordDetails.cleanRepeatingXMLGregorianCalendarWithHistory(document, ATTR_PD_DOC_DATE_R,
							index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_DOC_DATE_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordDocKindCodeCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_PD_DOC_KIND_CODE_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_DOC_KIND_CODE_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordKvalpCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_PD_KVALP_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_KVALP_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordSummapCount) {
				try {
					versionRecordDetails.cleanRepeatingBigDecimalWithHistory(document, ATTR_PD_SUMMAP_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_SUMMAP_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordSummap2Count) {
				try {
					versionRecordDetails.cleanRepeatingBigDecimalWithHistory(document, ATTR_PD_SUMMAP2_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_SUMMAP2_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordKvalkCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_PD_KVALK_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_KVALK_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordSummakCount) {
				try {
					versionRecordDetails.cleanRepeatingBigDecimalWithHistory(document, ATTR_PD_SUMMAK_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_SUMMAK_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordSummak2Count) {
				try {
					versionRecordDetails.cleanRepeatingBigDecimalWithHistory(document, ATTR_PD_SUMMAK2_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_SUMMAK2_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordPrizPostCount) {
				try {
					versionRecordDetails.cleanRepeatingBooleanWithHistory(document, ATTR_PD_PRIZ_POST_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_PRIZ_POST_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordSrokWaitCount) {
				try {
					versionRecordDetails.cleanRepeatingXMLGregorianCalendarWithHistory(document, ATTR_PD_SROK_WAIT_R,
							index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_SROK_WAIT_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordKstranaCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_PD_KSTRANA_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_KSTRANA_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordPrimeshCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_PD_PRIMESH_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_PRIMESH_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordDocTypeCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_PD_DOC_TYPE_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_DOC_TYPE_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordChannelNameCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_PD_CHANNEL_NAME_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_CHANNEL_NAME_R
							+ " с индексом " + index, null, ex);
				}
			}
			if (index < pdRecordChannelRefIdCount) {
				try {
					versionRecordDetails.cleanRepeatingStringWithHistory(document, ATTR_PD_CHANNEL_REF_ID_R, index);
					isRecordToCLeanExist = true;
				} catch (DfException ex) {
					DfLogger.warn(document, "Ошибка при удалении атрибута документа " + ATTR_PD_CHANNEL_REF_ID_R
							+ " с индексом " + index, null, ex);
				}
			}
			index++;
        }

        Date lastDesisionDate = null;
        if (documentXmlObject.getSPDDetails().getAcceptDate() != null) {
            lastDesisionDate = documentXmlObject.getSPDDetails().getAcceptDate().toGregorianCalendar().getTime();
        }
        if (documentXmlObject.getSPDDetails().getRejectDate() != null) {
            Date rejectDate = documentXmlObject.getSPDDetails().getRejectDate().toGregorianCalendar().getTime();
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

    public static IDfSysObject searchSPDObject(IDfSession dfSession, String docSourceCode, String docSourceId) throws DfException {
        return (IDfSysObject)dfSession.getObjectByQualification(DOCUMENT_TYPE_NAME + " where " + ATTR_DOC_SOURCE_CODE + " = '" + docSourceCode + "' AND " + ATTR_DOC_SOURCE_ID + " = '" + docSourceId + "'");
    }
}
