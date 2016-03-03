package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.storage.persistence.details.ActionRecordDetails;
import ru.rb.ccdea.storage.persistence.details.VersionRecordDetails;

import java.text.SimpleDateFormat;

public class AuditPersistence extends BasePersistence{

    public static final String SET_DOC_DOSSIER_EVENT_NAME = "ccdea_set_doc_dossier";
    public static final String CLOSE_DOSSIER_EVENT_NAME = "ccdea_close_dossier";
    public static final String REOPEN_DOSSIER_EVENT_NAME = "ccdea_reopen_dossier";
    public static final String UPDATE_BY_METADATA_SAVE_EVENT_NAME = "ccdea_update_by_metadata_save";
    public static final String UPDATE_BY_DOSSIER_SET_EVENT_NAME = "ccdea_update_by_dossier_set";

    protected static final String HISTORY_TYPE_NAME = "ccdea_attr_history";

    protected static final String HISTORY_ATTR_AUDITTRAIL = "id_audittrail";
    protected static final String HISTORY_ATTR_NAME = "s_attr_name";
    protected static final String HISTORY_ATTR_DESCRIPTION = "s_attr_description";
    protected static final String HISTORY_ATTR_VALUE = "s_value";
    protected static final String HISTORY_ATTR_OLD_VALUE = "s_old_value";

    protected static final String HISTORY_ACTION_RECORD_ATTR_NAME = "id_dossier_description";
    protected static final String HISTORY_ACTION_RECORD_ATTR_DESCRIPTION = "Идентификатор досье";

    public static SimpleDateFormat auditDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    /*
        1. event_name = 'ccdea_set_doc_dossier'
        Колонки:
        Дата операции - string_4
        Признак системы - string_2
        Автор - string_5
        Новый идентификатор досье - связанный ccdea_attr_history.s_value (s_attr_name = id_dossier_description)
        Старый идентификатор досье - связанный ccdea_attr_history.s_old_value (s_attr_name = id_dossier_description)
        Статус досье - string_1
        Архивный номер досье - string_3

        2. event_name = 'ccdea_close_dossier'
        Колонки:
        Дата операции - string_4
        Признак системы - string_2
        Автор - string_5
        Новый идентификатор досье - связанный ccdea_attr_history.s_value (s_attr_name = id_dossier_description)
        Старый идентификатор досье - (пусто)
        Статус досье - string_1
        Архивный номер досье - string_3

        3. event_name = 'ccdea_reopen_dossier'
        Колонки:
        Дата операции - string_4
        Признак системы - string_2
        Автор - string_5
        Новый идентификатор досье - связанный ccdea_attr_history.s_value (s_attr_name = id_dossier_description)
        Старый идентификатор досье - (пусто)
        Статус досье - string_1
        Архивный номер досье - string_3
     */
    public static void createActionRecord(IDfSession dfSession, IDfId documentId, ActionRecordDetails details) throws DfException{
        throwIfNotTransactionActive(dfSession);

        String[] stringArgs = new String[5];
        stringArgs[0] = details.dossierState;
        stringArgs[1] = details.sourceSystem;
        stringArgs[2] = details.dossierFullArchiveNumber;
        stringArgs[3] = auditDateFormat.format(details.operationDate);
        stringArgs[4] = details.userName;
        IDfId audittrailId = dfSession.getAuditTrailManager().createAudit(documentId, details.eventName, stringArgs, null);

        IDfPersistentObject attrHistoryObject = dfSession.newObject(HISTORY_TYPE_NAME);
        attrHistoryObject.setId(HISTORY_ATTR_AUDITTRAIL, audittrailId);
        attrHistoryObject.setString(HISTORY_ATTR_NAME, HISTORY_ACTION_RECORD_ATTR_NAME);
        attrHistoryObject.setString(HISTORY_ATTR_DESCRIPTION, HISTORY_ACTION_RECORD_ATTR_DESCRIPTION);
        attrHistoryObject.setString(HISTORY_ATTR_VALUE, details.dossierDescription);
        attrHistoryObject.setString(HISTORY_ATTR_OLD_VALUE, details.oldDossierDescription);
        attrHistoryObject.save();
    }

    /*
        1. event_name = 'ccdea_update_by_metadata_save'
        Колонки:
        Дата внесения изменений - string_4
        Признак системы - string_2
        Автор - string_5
        Название атрибута - связанный ccdea_attr_history.s_attr_description
        Новое значение - связанный ccdea_attr_history.s_value
        Старое значение - связанный ccdea_attr_history.s_old_value

        2. event_name = 'ccdea_update_by_dossier_set'
        Колонки:
        Дата внесения изменений - string_4
        Признак системы - string_2
        Автор - string_5
        Название атрибута - связанный ccdea_attr_history.s_attr_description
        Новое значение - связанный ccdea_attr_history.s_value
        Старое значение - связанный ccdea_attr_history.s_old_value
     */
    public static void createVersionRecord(IDfSession dfSession, IDfId documentId, VersionRecordDetails details) throws DfException{
        throwIfNotTransactionActive(dfSession);

        String[] stringArgs = new String[5];
        stringArgs[0] = "";
        stringArgs[1] = details.sourceSystem;
        stringArgs[2] = "";
        stringArgs[3] = auditDateFormat.format(details.operationDate);
        stringArgs[4] = details.userName;
        IDfId audittrailId = dfSession.getAuditTrailManager().createAudit(documentId, details.eventName, stringArgs, null);

        if (details.attrHistoryList != null) {
            for (VersionRecordDetails.AttrHistory attrHistory : details.attrHistoryList) {
                IDfPersistentObject attrHistoryObject = dfSession.newObject(HISTORY_TYPE_NAME);
                attrHistoryObject.setId(HISTORY_ATTR_AUDITTRAIL, audittrailId);
                attrHistoryObject.setString(HISTORY_ATTR_NAME, attrHistory.attrName);
                attrHistoryObject.setString(HISTORY_ATTR_DESCRIPTION, attrHistory.attrDescription);
                attrHistoryObject.setString(HISTORY_ATTR_VALUE, attrHistory.attrValue);
                attrHistoryObject.setString(HISTORY_ATTR_OLD_VALUE, attrHistory.attrOldValue);
                attrHistoryObject.save();
            }
        }
    }
}
