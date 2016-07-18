package ru.rb.ccdea.storage.jobs;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import ru.rb.ccdea.storage.persistence.ContentPersistence;

public class CreateContentRelationJob extends AbstractJob {

    public static void main(String[] args) {
        IDfSession testSession = null;
        IDfClientX clientx = new DfClientX();
        IDfClient client = null;
        IDfSessionManager sessionManager = null;
        try {
            client = clientx.getLocalClient();
            sessionManager = client.newSessionManager();

            IDfLoginInfo loginInfo = clientx.getLoginInfo();
            loginInfo.setUser("dmadmin");
            loginInfo.setPassword("Fkut,hf15");
            loginInfo.setDomain(null);

            sessionManager.setIdentity("ELAR", loginInfo);
            testSession = sessionManager.getSession("ELAR");

            CreateContentRelationJob job = new CreateContentRelationJob();
            job.process(testSession);

        } catch (Exception ex) {
            System.out.println(ex);
        } finally {
            if (testSession != null) {
                sessionManager.release(testSession);
            }
        }
    }

    @Override
    public int execute() throws Exception {
        process(dfSession);
        return 0;
    }

    public void process(IDfSession dfSession) throws DfException {

        boolean isTransAlreadyActive = dfSession.isTransactionActive();
        String dql = "" +
                "select cc.d_id as d_id, cc.c_id as c_id, rel.r_object_id from " +
                "(select doc.r_id as d_id, doc.doc_code, doc.doc_id, ct.r_id as c_id, ct.cnt_code, ct.cnt_id from " +
                "(select r_object_id  as r_id , rp_content_source_code as doc_code, rp_content_source_id as doc_id from dm_dbo.ccdea_base_doc_rp where rp_content_source_code is not nullstring and rp_content_source_id is not nullstring) doc,  " +
                "(select r_object_id  as r_id , rp_doc_content_id as cnt_code, rp_doc_source_id as cnt_id from dm_dbo.ccdea_doc_content_rp where rp_doc_content_id is not nullstring and rp_doc_source_id is not nullstring) ct " +
                "where doc.doc_code = ct.cnt_code and doc.doc_id = ct.cnt_id) " +
                "cc " +
                "left join dm_relation rel on (rel.parent_id = cc.d_id and rel.child_id = cc.c_id) where r_object_id is nullid";
        /*
        1. Ищем сначала все base_doc'и в которых заполнены соответствующие поля
        2. Ищем doc_content'ы, с такими же атрибутами
        3. заворачиваем в парент скобки результат запроса, из него вытаскиваем все поля
        4. делаем джоин на dm_relation, с указанием r_object_id такого relation'а в is nullid, чтобы если такие записи уже связаны они не попадали в запрос.
        5. идем в цикле и вызываем уже написанный метод ContentPersistence#createDocumentContentRelation
        todo: в текущей реализации джоба для base_doc и doc_content может быть только один тип связи, иначе запрос вернёт неверные данные.
         */
        try {

            DfLogger.info(this, "Start CreateContentRelationJob ", null, null);

            long start = System.currentTimeMillis();

            // старт транзакции
            if (!isTransAlreadyActive) {
                dfSession.beginTrans();
            }

            // обработка результатов запроса
            DfQuery query = new DfQuery();
            IDfCollection collection = null;

            try {
                query.setDQL(dql);
                collection = query.execute(dfSession, IDfQuery.READ_QUERY);

                while (collection != null && collection.next()) {
                    IDfId baseDocId = collection.getId("d_id");
                    IDfId docContentId = collection.getId("c_id");
                    ContentPersistence.createDocumentContentRelation(dfSession, baseDocId, docContentId);
                }
            } catch (DfException e) {
                DfLogger.error(this, "Error in CreateContentRelationJob JOB {0}", new String[]{e.getMessage()}, e);
                if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
                    dfSession.abortTrans();
                }
            } finally {
                closeCollection(collection);
            }

            // коммит транзакции
            if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
                dfSession.commitTrans();
            }

            long stop = System.currentTimeMillis();

            DfLogger.info(this, "Finish CreateContentRelationJob in {0} seconds", new Long[]{(stop - start) / 1000}, null);
        } catch (Exception ex) {
            DfLogger.error(this, "Error in CreateContentRelationJob JOB: {0}", new String[]{ex.getMessage()}, ex);
        } finally {
            if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
                dfSession.abortTrans();
            }
        }

    }

    private void closeCollection(IDfCollection collection) {
        try {
            if (collection != null && collection.getState() != IDfCollection.DF_CLOSED_STATE) {
                collection.close();
            }
        } catch (DfException var2) {
            DfLogger.error(this, "Error in close: {0}", new String[]{var2.getMessage()}, var2);
        }
    }
}
