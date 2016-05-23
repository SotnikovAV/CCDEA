package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import ru.rb.ccdea.storage.persistence.ctsutils.CTSRequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExternalMessagePersistence extends BasePersistence {

    public static final String TYPE_NAME = "ccdea_external_message";

    public static final String ATTR_MESSAGE_ESB_ID = "s_message_id";
    public static final String ATTR_MODIFICATION_VERB = "s_modification_verb";
    public static final String ATTR_MESSAGE_TYPE = "s_message_type";
    public static final String ATTR_SOURCE_KEY = "s_source_key";
    public static final String ATTR_MODIFICATION_TIME = "t_modification_time";
    public static final String ATTR_CURRENT_STATE = "n_current_state";
    public static final String ATTR_DOC_SOURCE_ID = "s_doc_source_id";
    public static final String ATTR_DOC_SOURCE_CODE = "s_doc_source_code";
    public static final String ATTR_CONTENT_SOURCE_ID = "s_content_source_id";
    public static final String ATTR_CONTENT_SOURCE_CODE = "s_content_source_code";
    public static final String ATTR_REPLY_TIME = "t_reply_time";
    public static final String ATTR_REPLY_ERROR_CODE = "s_reply_error_code";
    public static final String ATTR_REPLY_ERROR_DESCRIPTION = "s_reply_error_description";
    public static final String ATTR_RESULT_DOCUMENT_IDS = "id_document_r";
    public static final String ATTR_RESULT_CONTENT_IDS = "id_content_r";
    public static final String ATTR_CTS_REQUEST_ID = "id_cts_request";

    public static int MESSAGE_STATE_ON_VALIDATION = 0;
    public static int MESSAGE_STATE_VALIDATION_ERROR = 1;
    public static int MESSAGE_STATE_VALIDATION_PASSED = 2;
    public static int MESSAGE_STATE_ON_WAITING = 3;
    public static int MESSAGE_STATE_ON_PROCESSING = 4;
    public static int MESSAGE_STATE_LOADED = 5;
    public static int MESSAGE_STATE_CONVERTATION_ERROR = 6;
    public static int MESSAGE_STATE_PROCESSED = 7;

    public static String MESSAGE_TYPE_DOCPUT = "DocPut";
    public static String MESSAGE_TYPE_CONTRACT = "MCDocInfoModifyContract";
    public static String MESSAGE_TYPE_PD = "MCDocInfoModifyPD";
    public static String MESSAGE_TYPE_PS = "MCDocInfoModifyPS";
    public static String MESSAGE_TYPE_SPD = "MCDocInfoModifySPD";
    public static String MESSAGE_TYPE_SVO = "MCDocInfoModifySVO";
    public static String MESSAGE_TYPE_VBK = "MCDocInfoModifyVBK";
    public static String MESSAGE_TYPE_ZA = "MCDocInfoModifyZA";

    public static String MODIFICATION_VERB_CREATE = "C";
    public static String MODIFICATION_VERB_UPDATE = "U";

    public static SimpleDateFormat dqlDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public static String dqlDateFormatString = "dd.mm.yyyy hh:mi:ss";

    public static final String NOTIFY_EXTERNAL_SYSTEM_WORKFLOW_NAME = "ucb_ccdea_docstate_notify_wf";


    public static void storeMessageObjectOnValidation(IDfSysObject messageSysObject, String modificationVerb, String messageType, String sourceKey, Date sourceTime, String docSourceSystem, String docSourceId, String contentSourceSystem, String contentSourceId) throws DfException {
        messageSysObject.setString(ATTR_MODIFICATION_VERB, modificationVerb);
        messageSysObject.setString(ATTR_MESSAGE_TYPE, messageType);
        messageSysObject.setString(ATTR_SOURCE_KEY, sourceKey);
        messageSysObject.setTime(ATTR_MODIFICATION_TIME, new DfTime(sourceTime));
        messageSysObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_ON_VALIDATION);
        messageSysObject.setString(ATTR_DOC_SOURCE_ID, docSourceId);
        messageSysObject.setString(ATTR_DOC_SOURCE_CODE, docSourceSystem);
        messageSysObject.setString(ATTR_CONTENT_SOURCE_ID, contentSourceId);
        messageSysObject.setString(ATTR_CONTENT_SOURCE_CODE, contentSourceSystem);
        messageSysObject.save();

        logMessageState(messageSysObject, "validation");
    }

    public static void storeMessageObjectAfterValidation(IDfSysObject messageSysObject, boolean isError, String errorCode, String errorDescription, Date resultDate) throws DfException {
        messageSysObject.setTime(ATTR_REPLY_TIME, new DfTime(resultDate));
        messageSysObject.setString(ATTR_REPLY_ERROR_CODE, errorCode);
        messageSysObject.setString(ATTR_REPLY_ERROR_DESCRIPTION, errorDescription);
        if (isError) {
            messageSysObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_VALIDATION_ERROR);
        } else {
            messageSysObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_VALIDATION_PASSED);
        }
        messageSysObject.save();

        if (isError) {
            logMessageState(messageSysObject, "validationError");
        } else {
            logMessageState(messageSysObject, "validationPassed");
        }
    }

    public static boolean isFirstValidMessageWithSourceKey(IDfSysObject messageSysObject, String sourceKey, String messageType, Date sourceTime) throws DfException {
        boolean result = true;
        String dql = "select r_object_id " +
                " from " + TYPE_NAME +
                " where " + ATTR_SOURCE_KEY + " = '" + sourceKey + "'" +
                " and " + ATTR_MESSAGE_TYPE + " = '" + messageType + "'" +
                " and " + ATTR_CURRENT_STATE + " > " + MESSAGE_STATE_VALIDATION_ERROR +
                " and r_object_id != '" + messageSysObject.getObjectId().getId() + "'";
                //" and " + ATTR_MODIFICATION_TIME + " < date('" + dqlDateFormat.format(sourceTime) + "','" + dqlDateFormatString + "')";
        IDfCollection rs = null;
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            rs = query.execute(messageSysObject.getSession(), IDfQuery.DF_READ_QUERY);
            if (rs.next()) {
                result = false;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return result;
    }

    public static boolean isValidMessageExistsForDoc(IDfSysObject messageSysObject, String docSourceSystem, String docSourceId) throws DfException {
        boolean result = false;
        //TODO здесь должно проверяться произвольное количество идентификаторов. Для этого необходимо вынести идентификаторы в репитинг атрибуты
        String dql = "select r_object_id" +
                " from " + TYPE_NAME +
                " where ((upper(" + ATTR_DOC_SOURCE_CODE + ") = upper('" + docSourceSystem + "')" +
                " and " + ATTR_DOC_SOURCE_ID + " = '" + docSourceId + 
                "' ) OR (upper(" + ATTR_CONTENT_SOURCE_CODE + ") = upper('" + docSourceSystem + "')" +
                " and " + ATTR_CONTENT_SOURCE_ID + " = '" + docSourceId + "' ))"  +            
                " and " + ATTR_MESSAGE_TYPE + " != '" + MESSAGE_TYPE_DOCPUT + "'" +
                " and " + ATTR_CURRENT_STATE + " > " + MESSAGE_STATE_VALIDATION_ERROR;
        IDfCollection rs = null;
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            rs = query.execute(messageSysObject.getSession(), IDfQuery.DF_READ_QUERY);
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

    public static List<IDfId> getValidFirstMessageList(IDfSession dfSession, String messageType, Date sourceTime) throws DfException {
        HashMap<String, IDfId> firstMessageIdMap = new HashMap<String, IDfId>();
        HashSet<String> blockedMessageSet = new HashSet<String>();

        String dql = "select r_object_id" +
                ", " + ATTR_SOURCE_KEY +
                ", " + ATTR_MODIFICATION_TIME +
                ", " + ATTR_CURRENT_STATE +
                " from " + TYPE_NAME +
                " where " + ATTR_MESSAGE_TYPE + " = '" + messageType + "'" +
                " and " + ATTR_MODIFICATION_TIME + " < date('" + dqlDateFormat.format(sourceTime) + "','" + dqlDateFormatString + "')" +
                " and " + ATTR_CURRENT_STATE + " > " + MESSAGE_STATE_VALIDATION_ERROR +
                " and " + ATTR_CURRENT_STATE + " < " + MESSAGE_STATE_PROCESSED + 
                " order by r_creation_date asc";
        IDfCollection rs = null;
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            rs = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
            while (rs.next()) {
                IDfId messageId = rs.getId("r_object_id");
                String messageSourceKey = rs.getString(ATTR_SOURCE_KEY);
                int messageCurentState = rs.getInt(ATTR_CURRENT_STATE);
                if (!firstMessageIdMap.containsKey(messageSourceKey)) {
                    firstMessageIdMap.put(messageSourceKey, messageId);
                    if (MESSAGE_STATE_VALIDATION_PASSED != messageCurentState) {
                        blockedMessageSet.add(messageSourceKey);
                    }
                } else if (!blockedMessageSet.contains(messageSourceKey)) {
                    if (MESSAGE_STATE_ON_PROCESSING == messageCurentState) {
                        blockedMessageSet.add(messageSourceKey);
                        DfLogger.warn(null, "Found processing message not first in chain for sourceKey: " + messageSourceKey, null, null);
                    }
                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }

        List<IDfId> result = new ArrayList<IDfId>();
        for (String messageSourceKey : firstMessageIdMap.keySet()) {
            if (!blockedMessageSet.contains(messageSourceKey)) {
                result.add(firstMessageIdMap.get(messageSourceKey));
            }
        }
        return result;
    }

    public static List<IDfId> getLoadedMessageList(IDfSession dfSession) throws DfException {
        List<IDfId> result = new ArrayList<IDfId>();

        String dql = "select r_object_id" +
                ", " + ATTR_SOURCE_KEY +
                ", " + ATTR_MODIFICATION_TIME +
                ", " + ATTR_CURRENT_STATE +
                " from " + TYPE_NAME +
                " where " + ATTR_MESSAGE_TYPE + " = '" + MESSAGE_TYPE_DOCPUT + "'" +
                " and " + ATTR_CURRENT_STATE + " = " + MESSAGE_STATE_LOADED +  
                " order by " + ATTR_MODIFICATION_TIME + " asc";
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

    public static String getDocSourceId(IDfSysObject messageObject) throws DfException {
        return messageObject.getString(ATTR_DOC_SOURCE_ID);
    }

    public static String getDocSourceCode(IDfSysObject messageObject) throws DfException {
        return messageObject.getString(ATTR_DOC_SOURCE_CODE);
    }

    public static String getContentSourceId(IDfSysObject messageObject) throws DfException {
        return messageObject.getString(ATTR_CONTENT_SOURCE_ID);
    }

    public static String getContentSourceCode(IDfSysObject messageObject) throws DfException {
        return messageObject.getString(ATTR_CONTENT_SOURCE_CODE);
    }

    public static String getLastModifiedContentId(IDfSysObject messageObject) throws DfException {
        int modifiedContentCount = messageObject.getValueCount(ATTR_RESULT_CONTENT_IDS);
        if (modifiedContentCount > 0) {
            return messageObject.getRepeatingString(ATTR_RESULT_CONTENT_IDS, modifiedContentCount - 1);
        }
        else {
            return null;
        }
    }

    public static boolean checkContentMessageForWaiting(IDfSysObject contentMessageObject) throws DfException{
//        throwIfNotTransactionActive(contentMessageObject.getSession());

        boolean result = false;
        
        List<IDfSysObject> docMessages = getProcessedDocMessage(contentMessageObject);
        if(docMessages.size() == 0) {
        	return false;
        }
        IDfSysObject docMessageObject = docMessages.get(0);
        String docSourceId = getDocSourceId(docMessageObject);
        String docSourceSystem = getDocSourceCode(docMessageObject);
        
		String dql = "SELECT * FROM ccdea_external_message con WHERE con.r_object_id != '"
				+ contentMessageObject.getObjectId().getId() + "' and r_creation_date < date('"
				+ contentMessageObject.getCreationDate().asString("yyyy-MM-dd HH:mi:ss") + "','yyyy-MM-dd HH:mi:ss') "
				+ " and n_current_state > 1 and n_current_state < 7 and EXISTS( "
				+ " SELECT * FROM ccdea_external_message doc WHERE upper(doc.s_doc_source_code) = upper('"
				+ docSourceSystem + "') AND doc.s_doc_source_id = '" + docSourceId
				+ "' and con.s_doc_source_id = doc.s_content_source_id and upper(con.s_doc_source_code) = upper(doc.s_content_source_code))";
        
        IDfCollection rs = null;
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            rs = query.execute(contentMessageObject.getSession(), IDfQuery.DF_READ_QUERY);
            if (rs.next()) {
            	setMessageOnWaiting(contentMessageObject);
                result = true;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return result;
    }
    
    public static void setMessageOnWaiting(IDfSysObject contentMessageObject) throws DfException {
    	contentMessageObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_ON_WAITING);
        contentMessageObject.save();
        logMessageState(contentMessageObject, "contentOnWaiting");
    }
    
    public static List<IDfSysObject> getProcessedDocMessage(IDfSysObject contentMessageObject) throws DfException{
    	List<IDfSysObject> docMessages = new ArrayList<IDfSysObject>();
        String docSourceId = getDocSourceId(contentMessageObject);
        String docSourceSystem = getDocSourceCode(contentMessageObject);
        String dql = "select r_object_id, r_object_type, r_aspect_name, i_vstamp, i_is_reference, i_is_replica " +
                " from " + TYPE_NAME +
                " where ((upper(" + ATTR_DOC_SOURCE_CODE + ") = upper('" + docSourceSystem + "')" +
                " and " + ATTR_DOC_SOURCE_ID + " = '" + docSourceId + 
                "' ) OR (upper(" + ATTR_CONTENT_SOURCE_CODE + ") = upper('" + docSourceSystem + "')" +
                " and " + ATTR_CONTENT_SOURCE_ID + " = '" + docSourceId + "' ))"  + 
               
                " and " + ATTR_MESSAGE_TYPE + " != '" + MESSAGE_TYPE_DOCPUT + "'" +
                " and " + ATTR_CURRENT_STATE + " = " + MESSAGE_STATE_PROCESSED + " order by r_creation_date desc";
    	IDfEnumeration result = contentMessageObject.getSession().getObjectsByQuery(dql, null);
    	while(result.hasMoreElements()) {
    		docMessages.add((IDfSysObject)result.nextElement());
    	}
    	return docMessages;
    }

    /**
     * проверка на дубликаты при обработке сообщений DocPut. Проверку производить по полям s_content_source_id и s_content_source_code.
     * Если сообщение - дубликат, то переводить его в состояние 7. В поле s_reply_error_description записать "Дубликат".
     * @param contentMessageObject сообщение, для которого ищем дубликаты.
     * @throws DfException ошибка
     */
    public static void processDuplicateDocPutMessages(IDfSysObject contentMessageObject) throws DfException {
        String contentSourceId = getContentSourceId(contentMessageObject);
        String contentSourceCode = getContentSourceCode(contentMessageObject);
        StringBuilder dql = new StringBuilder("select r_object_id, r_object_type, r_aspect_name, i_vstamp, i_is_reference, i_is_replica from ").append(TYPE_NAME).append(" where r_object_id !=").append(DfUtil.toQuotedString(contentMessageObject.getObjectId().getId())).
                append(" and (upper(").append(ATTR_CONTENT_SOURCE_CODE).append(") = upper(").append(DfUtil.toQuotedString(contentSourceCode)).append("))").
                append(" and (").append(ATTR_CONTENT_SOURCE_ID).append(" = ").append(DfUtil.toQuotedString(contentSourceId)).append(")");

        IDfEnumeration result = contentMessageObject.getSession().getObjectsByQuery(dql.toString(), null);
        while (result.hasMoreElements()) {
            IDfSysObject docPutSysObject = (IDfSysObject) result.nextElement();
            docPutSysObject.setString(ATTR_REPLY_ERROR_DESCRIPTION, "Дубликат");
            docPutSysObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_PROCESSED);
            docPutSysObject.save();
            DfLogger.debug(ExternalMessagePersistence.class, "Duplicate found for MessageID {0} with content source id = {1} and content source code = {2}",
                    new String[]{contentMessageObject.getObjectId().getId(), contentSourceId, contentSourceCode}, null);
        }
    }

    public static void updateWaitingContents(IDfSysObject docMessageObject) throws DfException {
        String dql = "select r_object_id" +
                " from " + TYPE_NAME +
                " where ((upper(" + ATTR_DOC_SOURCE_CODE + ") = upper('" + getDocSourceCode(docMessageObject) + "')" +
                " and " + ATTR_DOC_SOURCE_ID + " = '" + getDocSourceId(docMessageObject) + 
                "') OR (upper(" + ATTR_DOC_SOURCE_CODE + ") = upper('" + getContentSourceCode(docMessageObject) + "')" +
                " and " + ATTR_DOC_SOURCE_ID + " = '" + getContentSourceId(docMessageObject) + "'))" +
                " and " + ATTR_MESSAGE_TYPE + " = '" + MESSAGE_TYPE_DOCPUT + "'" +
                " and " + ATTR_CURRENT_STATE + " = " + MESSAGE_STATE_ON_WAITING;
        IDfCollection rs = null;
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            rs = query.execute(docMessageObject.getSession(), IDfQuery.DF_READ_QUERY);
            while (rs.next()) {
                IDfPersistentObject contentObject = docMessageObject.getSession().getObject(rs.getId("r_object_id"));
                contentObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_VALIDATION_PASSED);
                contentObject.save();

                logMessageState((IDfSysObject)contentObject, "contentReturnFromWaiting");

            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }
    
    /**
     * Получить список сообщений, которые находятся в состоянии ожидания создания документа, но документ уже создан.
     * @param dfSession
     * @return
     * @throws DfException
     */
    public static List<IDfSysObject> getWaitingForDocMessages(IDfSession dfSession) throws DfException {
    	String dql = "select "
    			+ " r_object_id, r_object_type, r_aspect_name, i_vstamp, i_is_reference, i_is_replica "
    			+ " from ccdea_external_message doc "
    			+ " where s_message_type != 'DocPut' "
    			+ " and exists ("
    			+ " select r_object_id from ccdea_external_message docput "
    			+ " where s_message_type = 'DocPut' and n_current_state=3 "
    			+ " and (("
    			+ "	docput.s_doc_source_id=doc.s_content_source_id "
    			+ " and upper(docput.s_doc_source_code)=upper(doc.s_content_source_code) "
    			+ " ) or ( " +
    			" docput.s_doc_source_id=doc.s_doc_source_id " + 
    			" and upper(docput.s_doc_source_code)=upper(doc.s_doc_source_code)))) ";
    	List<IDfSysObject> msgList = new ArrayList<IDfSysObject>();
    	IDfEnumeration result = dfSession.getObjectsByQuery(dql, null);
    	while(result.hasMoreElements()) {
    		msgList.add((IDfSysObject)result.nextElement());
    	}
    	return msgList;
    }

    public static void startDocProcessing(IDfSysObject messageObject, IDfSysObject existingObject) throws DfException {
        throwIfNotTransactionActive(messageObject.getSession());

        validateModificationVerbForObject(messageObject, existingObject);

        messageObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_ON_PROCESSING);
        messageObject.save();

        logMessageState(messageObject, "startDocProcessing");
    }

    public static void startDocProcessing(IDfSysObject messageObject) throws DfException {
        throwIfNotTransactionActive(messageObject.getSession());

        messageObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_ON_PROCESSING);
        messageObject.save();

        logMessageState(messageObject, "startDocProcessing");
    }

    public static void validateModificationVerbForObject(IDfSysObject messageObject, IDfSysObject existingObject) throws DfException {
        if (MODIFICATION_VERB_CREATE.equalsIgnoreCase(messageObject.getString(ATTR_MODIFICATION_VERB))) {
            if (existingObject != null) {
                throw new DfException("Cant create document. Document already exists: " + existingObject.getObjectId().getId() + "ExternalMessageId: " + messageObject.getObjectId().getId());
            }
        } else if (MODIFICATION_VERB_UPDATE.equalsIgnoreCase(messageObject.getString(ATTR_MODIFICATION_VERB))) {
            if (existingObject == null) {
                throw new DfException("Cant update document. Document not found. ExternalMessageId: " + messageObject.getObjectId().getId());
            }
        }
    }

    public static void finishDocProcessing(IDfSysObject messageObject, String[] modifiedDocIds) throws DfException {
        throwIfNotTransactionActive(messageObject.getSession());

        messageObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_PROCESSED);
        for (String modifiedDocId : modifiedDocIds) {
            messageObject.appendString(ATTR_RESULT_DOCUMENT_IDS, modifiedDocId);
        }
        messageObject.save();

        logMessageState(messageObject, "finishDocProcessing");

        updateWaitingContents(messageObject);
    }

	public static void startContentProcessing(IDfSysObject messageObject, IDfSysObject existingObject)
			throws DfException {
		IDfSession dfSession = messageObject.getSession();
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			if (MODIFICATION_VERB_CREATE.equalsIgnoreCase(messageObject.getString(ATTR_MODIFICATION_VERB))) {
				if (existingObject != null) {
					throw new DfException(
							"Cant create content. Content already exists: " + existingObject.getObjectId().getId()
									+ "ExternalMessageId: " + messageObject.getObjectId().getId());
				}
			} else if (MODIFICATION_VERB_UPDATE.equalsIgnoreCase(messageObject.getString(ATTR_MODIFICATION_VERB))) {
				if (existingObject == null) {
					throw new DfException("Cant update content. Content not found. ExternalMessageId: "
							+ messageObject.getObjectId().getId());
				}
			}
			messageObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_ON_PROCESSING);
			messageObject.save();

			logMessageState(messageObject, "startContentProcessing");
			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
	}

	public static void finishContentProcessing(IDfSysObject messageObject, List<String> modifiedContentIdList,
			String ctsRequestId) throws DfException {
		IDfSession dfSession = messageObject.getSession();
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			messageObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_LOADED);
			if (ctsRequestId != null) {
				messageObject.setString(ATTR_CTS_REQUEST_ID, ctsRequestId);
			}
			for (String modifiedContentId : modifiedContentIdList) {
				messageObject.appendString(ATTR_RESULT_CONTENT_IDS, modifiedContentId);
			}
			messageObject.save();

			logMessageState(messageObject, "finishContentProcessing");
			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
	}

    public static String getContentProcessingByCTSIfFinished(IDfSysObject messageObject) throws DfException {
        String ctsJobId = messageObject.getString(ATTR_CTS_REQUEST_ID);
        if (ctsJobId != null && !"0000000000000000".equalsIgnoreCase(ctsJobId)) {
            return CTSRequestBuilder.getCTSResponseIfFinished(messageObject.getSession(), ctsJobId);
        }
        else {
            return null;
        }
    }

    public static void setConvertationError(IDfSysObject messageObject) throws DfException {
        throwIfNotTransactionActive(messageObject.getSession());

        messageObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_CONVERTATION_ERROR);
        messageObject.save();

        logMessageState(messageObject, "setConvertationError");
    }

    public static void notifyExternalSystemAboutContentProcessing(IDfSysObject messageObject) throws DfException {
        throwIfNotTransactionActive(messageObject.getSession());

        messageObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_PROCESSED);
        messageObject.save();

//        attachWorkflow(messageObject.getSession(), messageObject.getObjectId().getId(), NOTIFY_EXTERNAL_SYSTEM_WORKFLOW_NAME, null);

        logMessageState(messageObject, "notifyExternalSystemAboutContentProcessing");
    }

    public static void continueNextContentTransform(IDfSysObject messageObject, String ctsRequestId) throws DfException {
        throwIfNotTransactionActive(messageObject.getSession());

        messageObject.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_LOADED);
        if (ctsRequestId != null) {
            messageObject.setString(ATTR_CTS_REQUEST_ID, ctsRequestId);
        }
        messageObject.save();

        logMessageState(messageObject, "continueNextContentTransform");
    }

    public static void logMessageState(IDfSysObject messageObject, String operationDescription) throws DfException{
        DfLogger.info(ExternalMessagePersistence.class, "{0} {1}: {2}, {3}: {4}, {5}: {6}, {7}: {8}, {9}: {10}, {11}: {12}, {13}: {14}, {15}: {16}, {17}: {18}, {19}: {20}, {21}: {22}, {23}: {24}, {25}: {26}, {27}: {28}, {29}: {30}, {31}: {32}, {33}: {34}",
                new String[]{operationDescription, TYPE_NAME, messageObject.getObjectId().getId(),
                        ATTR_MESSAGE_ESB_ID, messageObject.getString(ATTR_MESSAGE_ESB_ID),
                        ATTR_MESSAGE_TYPE, messageObject.getString(ATTR_MESSAGE_TYPE),
                        ATTR_SOURCE_KEY, messageObject.getString(ATTR_SOURCE_KEY),
                        ATTR_MODIFICATION_TIME, messageObject.getString(ATTR_MODIFICATION_TIME),
                        ATTR_MODIFICATION_VERB, messageObject.getString(ATTR_MODIFICATION_VERB),
                        ATTR_CURRENT_STATE, messageObject.getString(ATTR_CURRENT_STATE),
                        ATTR_DOC_SOURCE_CODE, messageObject.getString(ATTR_DOC_SOURCE_CODE),
                        ATTR_DOC_SOURCE_ID, messageObject.getString(ATTR_DOC_SOURCE_ID),
                        ATTR_CONTENT_SOURCE_CODE, messageObject.getString(ATTR_CONTENT_SOURCE_CODE),
                        ATTR_CONTENT_SOURCE_ID, messageObject.getString(ATTR_CONTENT_SOURCE_ID),
                        ATTR_RESULT_DOCUMENT_IDS, splitRepeatingStringValues(messageObject, ATTR_RESULT_DOCUMENT_IDS),
                        ATTR_RESULT_CONTENT_IDS, splitRepeatingStringValues(messageObject, ATTR_RESULT_CONTENT_IDS),
                        ATTR_CTS_REQUEST_ID, messageObject.getString(ATTR_CTS_REQUEST_ID),
                        ATTR_REPLY_TIME, messageObject.getString(ATTR_REPLY_TIME),
                        ATTR_REPLY_ERROR_CODE, messageObject.getString(ATTR_REPLY_ERROR_CODE),
                        ATTR_REPLY_ERROR_DESCRIPTION, messageObject.getString(ATTR_REPLY_ERROR_DESCRIPTION)},
                null);
    }

    private static String splitRepeatingStringValues(IDfSysObject messageObject, String fieldName) throws DfException{
        String result = "[";
        int index = 0;
        while (index < messageObject.getValueCount(fieldName)) {
            if (result.length() > 1) {
                result += ",";
            }
            result += messageObject.getRepeatingString(fieldName, index);
            index++;
        }
        return result + "]";
    }

    public static void attachWorkflow(IDfSession session, String objectId, String wfName, Map<String, IDfList> activitiesPerformers) throws DfException {
        IDfWorkflowBuilder wfBuilder = session.newWorkflowBuilder(session.getIdByQualification("dm_process where object_name = '" + wfName + "'"));
        wfBuilder.initWorkflow();
        wfBuilder.runWorkflow();

        if (activitiesPerformers != null) {
            for (String activityName : activitiesPerformers.keySet()) {
                wfBuilder.getWorkflow().setPerformers(activityName, activitiesPerformers.get(activityName));
            }
        }

        IDfList objectIds = new DfList();
        objectIds.appendId(new DfId(objectId));
        IDfList activityIds = wfBuilder.getStartActivityIds();
        for (int i = 0; i < activityIds.getCount(); i++) {
            IDfActivity activity = (IDfActivity) session.getObject(activityIds.getId(i));
            for (int j = 0; j < activity.getPackageCount(); j++) {
                if ("INPUT".equalsIgnoreCase(activity.getPortType(j))) {
                    wfBuilder.addPackage(activity.getObjectName(), activity.getPortName(j), activity.getPackageName(j), activity.getPackageType(j), "", false, objectIds);
                }
            }
        }
    }

    public static IDfSysObject getOriginalMessageContentObject(IDfSysObject messageObject) throws DfException{
        String resultId = null;
        if (messageObject.getValueCount(ATTR_RESULT_CONTENT_IDS) > 0) {
            resultId = messageObject.getRepeatingString(ATTR_RESULT_CONTENT_IDS, 0);
            if (messageObject.getValueCount(ATTR_RESULT_CONTENT_IDS) > 1) {
                if (ContentPersistence.isDocumentRelationForContentExists(messageObject.getSession(), resultId)) {
                    resultId = messageObject.getRepeatingString(ATTR_RESULT_CONTENT_IDS, 1);
                }
            }
        }
        if (resultId != null) {
            return (IDfSysObject) messageObject.getSession().getObject(new DfId(resultId));
        }
        else {
            return null;
        }
    }

    public static IDfSysObject getDocumentContentObject(IDfSysObject messageObject) throws DfException{
        String resultId = null;
        if (messageObject.getValueCount(ATTR_RESULT_CONTENT_IDS) > 0) {
            resultId = messageObject.getRepeatingString(ATTR_RESULT_CONTENT_IDS, 0);
            if (messageObject.getValueCount(ATTR_RESULT_CONTENT_IDS) > 1) {
                if (!ContentPersistence.isDocumentRelationForContentExists(messageObject.getSession(), resultId)) {
                    resultId = messageObject.getRepeatingString(ATTR_RESULT_CONTENT_IDS, 1);
                }
            }
        }
        if (resultId != null) {
            return (IDfSysObject) messageObject.getSession().getObject(new DfId(resultId));
        }
        else {
            return null;
        }
    }

    public static IDfSysObject createEmptyMessageObjectFromFile(IDfSession dfSession, String filePath) throws DfException{
        IDfSysObject messageObject = (IDfSysObject)dfSession.newObject(TYPE_NAME);
        FileInputStream fin = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            fin = new FileInputStream(filePath);
            byte[] temp = new byte[1024];
            int read;
            while ((read = fin.read(temp)) >= 0) {
                buffer.write(temp, 0, read);
            }
            messageObject.setContentType("xml");
            messageObject.setContent(buffer);
            messageObject.save();
        } catch (FileNotFoundException e) {
            throw new DfException("Cant read file: " + filePath, e);
        } catch (IOException e) {
            throw new DfException("Cant read file: " + filePath, e);
        } finally {
            if (fin != null) {
                try {fin.close();} catch (Exception ex) {};
            }
        }
        return messageObject;
    }
    
	public static boolean beginProcessContentMsg(IDfSession dfSession, IDfId messageId) throws DfException {
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}
			IDfSysObject messageSysObject = (IDfSysObject) dfSession.getObject(messageId);
			messageSysObject.lock();
			int currentState = messageSysObject.getInt(ExternalMessagePersistence.ATTR_CURRENT_STATE);
			if (ExternalMessagePersistence.MESSAGE_STATE_VALIDATION_PASSED == currentState) {
				ExternalMessagePersistence.startContentProcessing(messageSysObject, null);
				messageSysObject.save();
				if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
					dfSession.commitTrans();
				}
				return true;
			} else {
				return false;
			}
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
	}
	
	public static boolean beginProcessDocMsg(IDfSession dfSession, IDfId messageId) throws DfException {
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}
			IDfSysObject messageSysObject = (IDfSysObject) dfSession.getObject(messageId);
			messageSysObject.lock();
			int currentState = messageSysObject.getInt(ExternalMessagePersistence.ATTR_CURRENT_STATE);
			if (ExternalMessagePersistence.MESSAGE_STATE_VALIDATION_PASSED == currentState) {
				ExternalMessagePersistence.startDocProcessing(messageSysObject);
				messageSysObject.save();
				if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
					dfSession.commitTrans();
				}
				return true;
			} else {
				return false;
			}
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
	}
}