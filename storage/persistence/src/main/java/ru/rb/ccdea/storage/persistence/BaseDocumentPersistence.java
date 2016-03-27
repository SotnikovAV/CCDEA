package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;

import java.util.ArrayList;
import java.util.List;

public class BaseDocumentPersistence extends BasePersistence{

    protected static final String TYPE_NAME = "ccdea_base_doc";

    protected static final String ATTR_DOSSIER = "id_dossier";
    protected static final String ATTR_BRANCH_CODE = "s_reg_branch_code";
    protected static final String ATTR_CUSTOMER_NUMBER = "s_customer_number";
    protected static final String ATTR_CUSTOMER_NAME = "s_customer_name";
    protected static final String ATTR_PASSPORT_NUMBER = "s_passport_number";
    protected static final String ATTR_CONTRACT_NUMBER = "s_contract_number";
    protected static final String ATTR_CONTRACT_DATE = "t_contract_date";
    protected static final String ATTR_LAST_CHANGE_DATE = "t_last_change_date";
    protected static final String ATTR_LAST_CHANGE_AUTHOR = "s_last_change_author";
    protected static final String ATTR_DOC_SOURCE_CODE = "s_doc_source_code";
    protected static final String ATTR_DOC_SOURCE_ID = "s_doc_source_id";
    protected static final String ATTR_PASSPORT_TYPE_CODE = "s_passport_type_code";
    protected static final String ATTR_AUTHOR_DEPARTMENT_CODE = "s_author_department_code";
    protected static final String ATTR_DOC_CHANNEL_NAME = "s_doc_channel_name";
    protected static final String ATTR_DOC_CHANNEL_REF_ID = "s_doc_channel_ref_id";
    protected static final String ATTR_CONTENT_URL = "s_content_url";
    protected static final String ATTR_DOCUMENT_COPY = "id_document_copy";

    public static final String CONTRACTOR_COLUMN_MULTI = "Мульти";
    public static final String CCDEA_PRIVELEGED_USERS_GROUP_NAME = "ccdea_privelege_users_group";

    public static List<IDfId> searchDocumentsByDossier(IDfSession dfSession, String dossierId) throws DfException{
        List<IDfId> result = new ArrayList<IDfId>();
        String dql = "select r_object_id "  +
                " from " + TYPE_NAME +
                " where " + ATTR_DOSSIER + " = '" + dossierId + "'";
        IDfCollection rs = null;
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            rs = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
            while (rs.next()) {
                result.add(rs.getId("r_object_id"));
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return result;
    }

	/**
	 * Получить список документов по идентификаторам
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param docSourceCode
	 *            - код системы документа
	 * @param docSourceId
	 *            - идентификатор документа
	 * @param contentSourceCode
	 *            - код системы контента
	 * @param contentSourceId
	 *            - идентификатор контента
	 * @return список документов
	 * @throws DfException
	 */
	public static List<IDfSysObject> searchDocumentByExternalKey(IDfSession dfSession, String docSourceCode,
			String docSourceId, String contentSourceCode, String contentSourceId) throws DfException {
		List<IDfSysObject> objs = new ArrayList<IDfSysObject>();
		String dql ="SELECT r_object_id, r_object_type, r_aspect_name, i_vstamp, i_is_reference, i_is_replica FROM "
				+ TYPE_NAME + " WHERE (" + ATTR_DOC_SOURCE_CODE + " = '" + docSourceCode + "' AND "
				+ ATTR_DOC_SOURCE_ID + " = '" + docSourceId + "') OR (" + ATTR_DOC_SOURCE_CODE + " = '"
				+ contentSourceCode + "' AND " + ATTR_DOC_SOURCE_ID + " = '" + contentSourceId + "')";
		DfLogger.debug(null, dql, null, null);
		IDfEnumeration en = dfSession
				.getObjectsByQuery(
						dql,
						null);
		while (en.hasMoreElements()) {
			objs.add((IDfSysObject) en.nextElement());
		}

		return objs;
	}

    public static IDfPersistentObject lockDocumentForAttach(IDfPersistentObject document) throws DfException {
        throwIfNotTransactionActive(document.getSession());

        IDfPersistentObject result = null;
        document.lock();
        if (!DfId.DF_NULLID.equals(document.getId(ATTR_DOSSIER))) {
            result = document.getSession().getObject(document.getId(ATTR_DOSSIER));
        }
        return result;
    }

    public static void saveDossierKeysToDocument(IDfSysObject document, DossierKeyDetails keyDetails, VersionRecordDetails versionRecordDetails) throws DfException {
        IDfSession dfSession = document.getSession();
    	throwIfNotTransactionActive(dfSession);
        
        document.setACL(getBranchACL(dfSession, keyDetails.branchCode));
        
        String oldCustomerNumber = document.getString(ATTR_CUSTOMER_NUMBER);
        String oldDocumentName = document.getObjectName();
        String newDocumentName = oldDocumentName.replace(oldCustomerNumber, keyDetails.customerNumber);
        document.setObjectName(newDocumentName);

        versionRecordDetails.setIdWithHistory(document, ATTR_DOSSIER, new DfId(keyDetails.dossierId));
        versionRecordDetails.setStringWithHistory(document, ATTR_BRANCH_CODE, keyDetails.branchCode);
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NUMBER, keyDetails.customerNumber);
        versionRecordDetails.setStringWithHistory(document, ATTR_CUSTOMER_NAME, keyDetails.customerName);
        if (keyDetails.passportNumber != null && !keyDetails.passportNumber.trim().isEmpty()) {
            versionRecordDetails.setStringWithHistory(document, ATTR_PASSPORT_NUMBER, keyDetails.passportNumber);
        }
        else {
            versionRecordDetails.setStringWithHistory(document, ATTR_CONTRACT_NUMBER, keyDetails.contractNumber);
            versionRecordDetails.setTimeWithHistory(document, ATTR_CONTRACT_DATE, keyDetails.contractDate != null ? new DfTime(keyDetails.contractDate) : DfTime.DF_NULLDATE);
        }
        document.save();
    }

    public static DossierKeyDetails getKeyDetails(IDfPersistentObject document) throws DfException {
        DossierKeyDetails result = new DossierKeyDetails();
        result.dossierId = document.getId(ATTR_DOSSIER).getId();
        result.branchCode = document.getString(ATTR_BRANCH_CODE);
        result.customerNumber = document.getString(ATTR_CUSTOMER_NUMBER);
        result.passportNumber = document.getString(ATTR_PASSPORT_NUMBER);
        if (result.passportNumber == null || result.passportNumber.trim().isEmpty()) {
            result.contractNumber = document.getString(ATTR_CONTRACT_NUMBER);
            result.contractDate = document.getTime(ATTR_CONTRACT_DATE).getDate();
        }
        return result;
    }

    public static IDfId getDossierId(IDfPersistentObject document) throws DfException {
        return document.getId(ATTR_DOSSIER);
    }

    public static IDfSysObject lockDocumentForUpdate(IDfSession dfSession, IDfId documentId) throws DfException {
        throwIfNotTransactionActive(dfSession);

        IDfSysObject result = (IDfSysObject)dfSession.getObject(documentId);
        result.lock();
        return result;
    }

    public static IDfGroup getBranchUsersGroup(IDfSession dfSession, String branchCode) throws DfException{
        String groupName = "ccdea_branch_" + branchCode + "_users_group";
        IDfGroup result = dfSession.getGroup(groupName);
        if (result == null) {
            result = (IDfGroup)dfSession.newObject("dm_group");
            result.setGroupName(groupName);
            result.save();
        }
        return result;
    }

    public static IDfGroup getPrivelegeUsersGroup(IDfSession dfSession) throws DfException{
        IDfGroup result = dfSession.getGroup(CCDEA_PRIVELEGED_USERS_GROUP_NAME);
        if (result == null) {
            result = (IDfGroup)dfSession.newObject("dm_group");
            result.setGroupName(CCDEA_PRIVELEGED_USERS_GROUP_NAME);
            result.save();
        }
        return result;
    }

    public static IDfACL getBranchACL(IDfSession dfSession, String branchCode) throws DfException{
        String aclName = "ccdea_branch_" + branchCode + "_docs_acl";
        IDfACL result = dfSession.getACL(dfSession.getDocbaseOwnerName(), aclName);
        if (result == null) {
            result = (IDfACL)dfSession.newObject("dm_acl");
            result.setObjectName(aclName);
            result.setDomain(dfSession.getDocbaseOwnerName());
            result.grant("docu", IDfACL.DF_PERMIT_DELETE, null);
            result.grant(getPrivelegeUsersGroup(dfSession).getGroupName(), IDfACL.DF_PERMIT_WRITE, null);
            result.grant(getBranchUsersGroup(dfSession, branchCode).getGroupName(), IDfACL.DF_PERMIT_WRITE, null);
            result.save();
        }
        return result;
    }

    public static String getNormalCustomerNumber(String customerNumber) {
        String result = null;
        if (customerNumber != null && !customerNumber.trim().isEmpty()) {
            result = customerNumber.trim();
            while (result.length() < 8) {
                result = "0" + result;
            }
        }
        return result;
    }

    public static boolean isNotnullTrue(Boolean value) {
        return value == null ? false : value.booleanValue();
    }
}
