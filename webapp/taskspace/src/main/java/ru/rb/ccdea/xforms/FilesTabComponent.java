/**
 * 
 */
package ru.rb.ccdea.xforms;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;

import ru.rb.ccdea.search.SearchResultsComponent;
import ru.rb.ccdea.storage.persistence.BaseDocumentPersistence;
import ru.rb.ccdea.storage.persistence.ContractPersistence;
import ru.rb.ccdea.storage.persistence.PDPersistence;
import ru.rb.ccdea.storage.persistence.PassportPersistence;
import ru.rb.ccdea.storage.persistence.RequestPersistence;
import ru.rb.ccdea.storage.persistence.SPDPersistence;
import ru.rb.ccdea.storage.persistence.SVOPersistence;
import ru.rb.ccdea.storage.persistence.VBKPersistence;

/**
 * @author SotnikovAV
 *
 */
public class FilesTabComponent extends SearchResultsComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private String objectId;

	public static final String DOC_TYPE_AND_STATE = "doc_type_state";

	@Override
	public void onInit(ArgumentList arg) {
		try {
			super.onInit(arg);
			objectId = arg.get("objectId");
			IDfSysObject doc = (IDfSysObject) getDfSession().getObject(new DfId(objectId));
			String docType = getDocType(doc);
			String docState = getDocStatus(doc);
			getContext().set(DOC_TYPE_AND_STATE, docType + '/' + docState);
			String filesQueryStr = "select '" + docType + '/' + docState + "' as " + DOC_TYPE_AND_STATE + ", "
					+ " content.r_object_id, content.object_name, content.s_content_source_id, content.s_content_source_code, "
					+ " content.r_version_label, content.r_creation_date, content.r_modify_date, content.a_content_type, content.r_content_size "
					+ " , content.r_object_type, content.r_link_cnt, content.r_is_virtual_doc, content.r_assembled_from_id, content.r_has_frzn_assembly, content.i_is_replica, i_is_reference "
					+ " from " + " ccdea_doc_content (all) content where i_chronicle_id in ("
					+ " select child_id from dm_relation where relation_name='ccdea_content_relation' and parent_id='"
					+ objectId + "') and content.b_is_original=true ";

			DfLogger.debug(this, filesQueryStr, null, null);
			refreshDatagrid(filesQueryStr);
		} catch (Exception ex) {
			throw new WrapperRuntimeException(ex);
		}
	}

	private String getDocType(IDfSysObject doc) throws DfException {
		String docType = doc.getTypeName();
		if (PassportPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			return PassportPersistence.DOCUMENT_TYPE_FULL_DISPLAY_NAME;
		} else if (VBKPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			return VBKPersistence.DOCUMENT_TYPE_DISPLAY_NAME;
		} else if (ContractPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			docType = doc.getString(ContractPersistence.ATTR_DOC_TYPE);
			return (docType == null || docType.trim().length() == 0) ? ContractPersistence.DOCUMENT_TYPE_DISPLAY_NAME
					: docType;
		} else if (SPDPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			docType = doc.getString(SPDPersistence.ATTR_DOC_TYPE);
			return (docType == null || docType.trim().length() == 0) ? SPDPersistence.DOCUMENT_TYPE_DISPLAY_NAME
					: docType;
		} else if (SVOPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			return SVOPersistence.DOCUMENT_TYPE_DISPLAY_NAME;
		} else if (RequestPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			return RequestPersistence.DOCUMENT_TYPE_DISPLAY_NAME;
		} else if (PDPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			docType = doc.getString(PDPersistence.ATTR_DOC_TYPE);
			return (docType == null || docType.trim().length() == 0) ? PDPersistence.DOCUMENT_TYPE_DISPLAY_NAME
					: docType;
		}
		return "Неизвестный тип документа";
	}

	private String getDocStatus(IDfSysObject doc) throws DfException {
		String docType = doc.getTypeName();
		if (PassportPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			return "";
		} else if (VBKPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			return "";
		} else if (ContractPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			String docState = doc.getString(ContractPersistence.ATTR_STATE);
			return (docState == null || docState.trim().length() == 0) ? "" : docState;
		} else if (SPDPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			String docState = doc.getString(SPDPersistence.ATTR_STATE);
			return (docState == null || docState.trim().length() == 0) ? "" : docState;
		} else if (SVOPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			String docState = doc.getString(SVOPersistence.ATTR_STATE);
			return (docState == null || docState.trim().length() == 0) ? "" : docState;
		} else if (RequestPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			String docState = doc.getString(RequestPersistence.ATTR_STATE);
			return (docState == null || docState.trim().length() == 0) ? "" : docState;
		} else if (PDPersistence.DOCUMENT_TYPE_NAME.equals(docType)) {
			String docState = doc.getString(PDPersistence.ATTR_STATE);
			return (docState == null || docState.trim().length() == 0) ? "" : docState;
		}
		return "";
	}
}
