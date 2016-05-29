package ru.rb.ccdea.storage.persistence;

import java.util.ArrayList;
import java.util.List;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.DfRelationType;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfEnumeration;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.client.IDfRelationType;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfUtil;
import com.documentum.fc.common.IDfId;

public class ContentPersistence extends BasePersistence {

	public static final String TYPE_NAME = "ccdea_doc_content";
	public static final String RELATION_NAME = "ccdea_content_relation";
	public static final String CONTENT_FOLDER = BaseDocumentPersistence.HOME_FOLDER + "/content";

	public static final String ATTR_CONTENT_SOURCE_CODE = "s_content_source_code";
	public static final String ATTR_CONTENT_SOURCE_ID = "s_content_source_id";
	public static final String ATTR_IS_PARTS_EXIST = "b_is_parts_exist";
	public static final String ATTR_CONTENT_COPY_ID = "id_content_copy";
	public static final String ATTR_IS_ORIGINAL = "b_is_original";
	public static final String ATTR_CTS_RESULT_ID = "id_cts_result_content";

	public static final String CONTENT_PART_TYPE_NAME = "ccdea_doc_content_part";
	public static final String ATTR_CONTENT_FOR_PART_ID = "id_content";
	public static final String ATTR_PART_INDEX = "n_index";
	public static final String CONTENT_NOT_FOUND = "Content not found.";
	public static final long CONTENT_CHECK_WAITING_TIMEOUT = 180;
	public static final String CONTENT_HAS_BEEN_FOUND = "Content has been found.";

	public static boolean isDocTypeSupportContentVersion(String docTypeName) {
		return PassportPersistence.DOCUMENT_TYPE_NAME.equalsIgnoreCase(docTypeName)
				|| VBKPersistence.DOCUMENT_TYPE_NAME.equalsIgnoreCase(docTypeName)
				|| SPDPersistence.DOCUMENT_TYPE_NAME.equalsIgnoreCase(docTypeName)
				|| SVOPersistence.DOCUMENT_TYPE_NAME.equalsIgnoreCase(docTypeName);
	}

	public static boolean isDocTypeSupportContentAppending(String docTypeName) {
		return PDPersistence.DOCUMENT_TYPE_NAME.equalsIgnoreCase(docTypeName)
				|| ContractPersistence.DOCUMENT_TYPE_NAME.equalsIgnoreCase(docTypeName)
				|| RequestPersistence.DOCUMENT_TYPE_NAME.equalsIgnoreCase(docTypeName);
	}

	/**
	 * Получить объект контента по идентификатору документа
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param documentId
	 *            - идентификатор документа
	 * @return объект контента с трансформированным в PDF контентом, если есть;
	 *         иначе объект контента с оригинальным контентом; если ничего не
	 *         найдено, то null
	 * @throws DfException
	 */
	public static IDfSysObject searchContentObjectByDocumentId(IDfSession dfSession, String documentId)
			throws DfException {
		String contentId = null;
		String dql = "select r_object_id as cont_id from ccdea_doc_content where i_chronicle_id in (select child_id from dm_relation where parent_id="
				+ DfUtil.toQuotedString(documentId) + ") order by r_modify_date desc";

		DfLogger.info(dfSession, dql, null, null);
		IDfCollection rs = null;
		try {
			IDfQuery query = new DfQuery();
			query.setDQL(dql);
			rs = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
			if (rs.next()) {
				contentId = rs.getString("cont_id");
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		if (contentId != null) {
			return (IDfSysObject) dfSession
					.getObjectByQualification(TYPE_NAME + " where r_object_id = '" + contentId + "'");
		} else {
			return null;
		}
	}
	
	/**
	 * Получить объект оригинального контента по идентификатору документа
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param documentId
	 *            - идентификатор документа
	 * @return объект с оригинальным контентом
	 * @throws DfException
	 */
	public static IDfSysObject searchOriginalContentObjectByDocumentId(IDfSession dfSession, String documentId)
			throws DfException {
		String contentId = null;
		String dql = "select r_object_id as cont_id from ccdea_doc_content where i_chronicle_id in (select child_id from dm_relation where parent_id="
				+ DfUtil.toQuotedString(documentId) + ") and b_is_original=true order by r_modify_date desc";

		DfLogger.info(dfSession, dql, null, null);
		IDfCollection rs = null;
		try {
			IDfQuery query = new DfQuery();
			query.setDQL(dql);
			rs = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
			if (rs.next()) {
				contentId = rs.getString("cont_id");
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		if (contentId != null) {
			return (IDfSysObject) dfSession
					.getObjectByQualification(TYPE_NAME + " where r_object_id = '" + contentId + "'");
		} else {
			return null;
		}
	}

	public static IDfRelation createDocumentContentRelation(IDfSession dfSession, IDfId documentId, IDfId contentId)
			throws DfException {
		IDfRelationType relationType = (IDfRelationType) dfSession
				.getObjectByQualification("dm_relation_type where relation_name = '" + RELATION_NAME + "'");
		if (relationType == null) {
			relationType = (IDfRelationType) dfSession.newObject("dm_relation_type");
			relationType.setRelationName(RELATION_NAME);
			relationType.setDescription(RELATION_NAME + " relation type");
			relationType.setParentType(BaseDocumentPersistence.TYPE_NAME);
			relationType.setChildType(TYPE_NAME);
			relationType.setSecurityType(DfRelationType.SYSTEM);
			relationType.save();
		}

		IDfRelation relation = (IDfRelation) dfSession.newObject("dm_relation");
		relation.setRelationName(RELATION_NAME);
		relation.setChildId(contentId);
		relation.setParentId(documentId);
		relation.save();

		IDfSysObject document = (IDfSysObject) dfSession.getObject(documentId);
		IDfACL acl = document.getACL();

		IDfSysObject content = (IDfSysObject) dfSession.getObject(contentId);
		content.setACL(acl);
		content.save();

		return relation;
	}

	public static IDfSysObject createContentObject(IDfSession dfSession, String contentSourceCode,
			String contentSourceId, boolean isOriginal) throws DfException {
		throwIfNotTransactionActive(dfSession);

		IDfSysObject result = (IDfSysObject) dfSession.newObject(TYPE_NAME);
		result.setObjectName(contentSourceCode + '_' + contentSourceId);
		result.setString(ATTR_CONTENT_SOURCE_CODE, contentSourceCode);
		result.setString(ATTR_CONTENT_SOURCE_ID, contentSourceId);
		result.setBoolean(ATTR_IS_ORIGINAL, isOriginal);
		if (!isOriginal) {
			result.setContentType("pdf");
		}
		result.link(CONTENT_FOLDER);
		result.save();

		return result;
	}

	/**
	 * Создать объект для хранения части контента. Используется, если контент
	 * приходит в виде архива.
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param name
	 *            - наименование
	 * @param contentType
	 *            - тип контента (формат)
	 * @param contentSysObjectId
	 *            - идентификатор основного объекта контента
	 * @param index
	 *            - порядковый номер части контента
	 * @return объект для хранения части контента
	 * @throws DfException
	 */
	public static final IDfSysObject createContentPartSysObject(IDfSession dfSession, String name, String contentType,
			IDfId contentSysObjectId, int index) throws DfException {
		IDfSysObject contentPartSysObject = (IDfSysObject) dfSession.newObject(CONTENT_PART_TYPE_NAME);
		contentPartSysObject.setObjectName(name);
		contentPartSysObject.setContentType(contentType);
		contentPartSysObject.setId("id_content", contentSysObjectId);
		contentPartSysObject.setInt("n_index", index);
		contentPartSysObject.save();
		return contentPartSysObject;
	}

	public static void setCtsResultId(IDfSysObject contentObject, IDfId ctsResultId) throws DfException {
		throwIfNotTransactionActive(contentObject.getSession());

		contentObject.setId(ATTR_CTS_RESULT_ID, ctsResultId);
		contentObject.save();
	}

	public static IDfSysObject getNextContentSysObject(IDfSysObject contentSysObject) throws DfException {
		return getNextContentSysObject(contentSysObject, false);
	}

	public static IDfSysObject getNextContentSysObject(IDfSysObject contentSysObject, boolean checkOnlyParts)
			throws DfException {
		IDfSysObject result = checkOnlyParts ? null : contentSysObject;

		String dql = "select r_object_id " + " from ccdea_doc_content_part " + " where id_content = "
				+ DfUtil.toQuotedString(contentSysObject.getObjectId().getId()) + " order by n_index";
		IDfCollection rs = null;
		try {
			IDfQuery query = new DfQuery();
			query.setDQL(dql);
			rs = query.execute(contentSysObject.getSession(), IDfQuery.DF_READ_QUERY);
			if (rs.next()) {
				result = (IDfSysObject) contentSysObject.getSession().getObject(rs.getId("r_object_id"));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return result;
	}

	public static List<IDfSysObject> getContentPartsSysObject(IDfSysObject contentSysObject) throws DfException {
		List<IDfSysObject> partList = new ArrayList<IDfSysObject>();
		String dql = "select r_object_id, r_object_type, r_aspect_name, i_vstamp, i_is_reference, i_is_replica "
				+ " from ccdea_doc_content_part " + " where id_content = " + DfUtil.toQuotedString(contentSysObject.getObjectId().getId())
				+ " order by n_index";
		IDfEnumeration result = contentSysObject.getSession().getObjectsByQuery(dql, null);
		while (result.hasMoreElements()) {
			partList.add((IDfSysObject) result.nextElement());
		}
		return partList;
	}

	public static boolean isDocumentRelationForContentExists(IDfSession session, String contentId) throws DfException {
		boolean result = false;

		String dql = "select child_id " + " from dm_relation " + " where child_id = " + DfUtil.toQuotedString(contentId)
				+ " and relation_name = " + DfUtil.toQuotedString(RELATION_NAME);
		IDfCollection rs = null;
		try {
			IDfQuery query = new DfQuery();
			query.setDQL(dql);
			rs = query.execute(session, IDfQuery.DF_READ_QUERY);
			if (rs.next()) {
				result = true;
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return result;
	}

	/**
	 * Получить идентификаторы документов, к которым присоединен указанный
	 * контент
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param contentId
	 *            - идентификатор контента
	 * @return список идентификаторов документов, к которым присоединен
	 *         указанный контент
	 * @throws DfException
	 */
	public static List<IDfId> getDocIdsByContentId(IDfSession dfSession, IDfId contentId) throws DfException {
		List<IDfId> docIds = new ArrayList<IDfId>();
		String dql = "select parent_id from dm_relation where relation_name=" + DfUtil.toQuotedString(RELATION_NAME) + " and child_id="
				+ DfUtil.toQuotedString(contentId.getId());
		IDfCollection rs = null;
		try {
			IDfQuery query = new DfQuery();
			query.setDQL(dql);
			rs = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
			while (rs.next()) {
				docIds.add(rs.getId("parent_id"));
			}
			return docIds;
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}

	/**
	 * Проверка наличия контента
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param contentSysObject
	 *            - объект, содержащий контент
	 * @return состояние
	 * @throws DfException
	 */
	public static String checkContentAvaliable(IDfSession dfSession, IDfSysObject contentSysObject) throws DfException {
		long counter = 0;
		while (true) {
			contentSysObject.fetch(null);
			try {
				if (contentSysObject.getContent() != null) {
					return CONTENT_HAS_BEEN_FOUND;
				}

			} catch (Exception ex) {
				DfLogger.debug(contentSysObject,
						"Content for " + contentSysObject.getObjectId().getId() + " not avaliable yet.", null, null);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
			}
			counter++;
			if (CONTENT_CHECK_WAITING_TIMEOUT == counter) {
				throw new DfException("Content for " + contentSysObject.getObjectId().getId() + " not avaliable.");
			}
		}
		
	}
	
	public static List<IDfId> getUnlinkedContentIds(IDfSession dfSession, String documentId, String docSourceId,
			String docSourceSystem, String contentSourceId, String contentSourceSystem) throws DfException {
		List<IDfId> contentIds = new ArrayList<IDfId>();
		String dql = "select c.r_object_id from ccdea_doc_content c " + "left join ccdea_external_message m "
				+ "on any m.id_content_r=c.r_object_id and m.s_message_type='DocPut' " + "where " + "( "
				+ "	(m.s_doc_source_id = " + DfUtil.toQuotedString(docSourceId)
				+ " and upper(m.s_doc_source_code) = upper(" + DfUtil.toQuotedString(docSourceSystem) + ")) "
				+ "	or  " + "	(m.s_doc_source_id = " + DfUtil.toQuotedString(contentSourceId)
				+ " and upper(m.s_doc_source_code) = upper(" + DfUtil.toQuotedString(contentSourceSystem) + ")) "
				+ ")  " + "and not exists( " + "	select child_id from dm_relation where parent_id="
				+ DfUtil.toQuotedString(documentId)
				+ " and child_id=c.i_chronicle_id and relation_name='ccdea_content_relation' ) ";
		IDfCollection rs = null;
		try {
			IDfQuery query = new DfQuery();
			query.setDQL(dql);
			rs = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
			while (rs.next()) {
				IDfId dfId = rs.getId("r_object_id");
				contentIds.add(dfId);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		return contentIds;
	}
}
