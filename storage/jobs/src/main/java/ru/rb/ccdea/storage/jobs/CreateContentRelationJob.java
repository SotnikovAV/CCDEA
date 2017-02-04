package ru.rb.ccdea.storage.jobs;

import java.util.HashSet;
import java.util.Set;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfUtil;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import ru.rb.ccdea.storage.persistence.ContentPersistence;
import ru.rb.ccdea.storage.persistence.fileutils.ContentLoader;

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
			loginInfo.setPassword("dmadmin");
			loginInfo.setDomain(null);

			sessionManager.setIdentity("UCB", loginInfo);
			testSession = sessionManager.getSession("UCB");

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
		String dql = "select "
				+ "distinct d.r_object_id as d_id, d.r_object_type as d_type, c.r_object_id as c_id, c.r_creation_date "
				+ "from ccdea_doc_content c, ccdea_base_doc d "
				+ "where  upper(c.rp_doc_source_code)=upper(d.rp_content_source_code) "
				+ "and c.rp_doc_source_id=d.rp_content_source_id "
				+ "and not exists("
				+ "select r_object_id from dm_relation "
				+ "where relation_name='ccdea_content_relation'"
				+ "and parent_id=d.r_object_id and child_id=c.i_chronicle_id"
				+ ") order by c.r_creation_date enable (ROW_BASED)";

		try {
			DfLogger.info(this, "Start CreateContentRelationJob ", null, null);

			long start = System.currentTimeMillis();

			// обработка результатов запроса
			IDfQuery query = new DfQuery();
			IDfCollection collection = null;

			try {
				query.setDQL(dql);
				collection = query.execute(dfSession, IDfQuery.READ_QUERY);
				Set<String> processedContentIds = new HashSet<String>();
				while (collection != null && collection.next()) {
					try {
						// старт транзакции
						if (!isTransAlreadyActive) {
							dfSession.beginTrans();
						}

						IDfId baseDocId = collection.getId("d_id");
						IDfId docContentId = collection.getId("c_id");
						String baseDocType = collection.getString("d_type");
						IDfSysObject doc = (IDfSysObject) dfSession.getObject(baseDocId);
						try {
							doc.lock();
							if (ContentPersistence.isDocTypeSupportContentVersion(baseDocType)) {
								IDfSysObject contentObj = (IDfSysObject) dfSession.getObjectByQualification(
										"ccdea_doc_content where i_chronicle_id in (select child_id from dm_relation where relation_name='ccdea_content_relation' and parent_id="
												+ DfUtil.toQuotedString(baseDocId.getId()) + ")");
								if (contentObj != null && !processedContentIds.contains(docContentId.getId())
										&& !contentObj.getObjectId().equals(docContentId)) {
									processedContentIds.add(docContentId.getId());

									IDfSysObject newContentObj = (IDfSysObject) dfSession.getObject(docContentId);
									if (newContentObj != null) {
										try {
											ContentLoader.saveContent(contentObj, newContentObj.getContentType(),
													newContentObj.getContent(), true);
											newContentObj.destroy();
										} catch (DfException e) {
											throw new DfException("error while creating content with new id="
													+ newContentObj.getObjectId().getId() + " old id="
													+ contentObj.getObjectId().getId(), e);
										}
									}

									continue;
								}
							}
							ContentPersistence.createDocumentContentRelation(dfSession, baseDocId, docContentId);
						} catch (DfException e) {
							DfLogger.error(this, "Error while creating content with id=" + docContentId
									+ " for document with id=" + baseDocId, null, e);
						} finally {
							doc.save();
						}
						
						// коммит транзакции
						if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
							dfSession.commitTrans();
						}
						
					} catch (DfException e) {
						DfLogger.error(this, "Error in CreateContentRelationJob JOB {0}",
								new String[] { e.getMessage() }, e);
						if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
							dfSession.abortTrans();
						}
					}
				}
			} finally {
				closeCollection(collection);
			}

			long stop = System.currentTimeMillis();

			DfLogger.info(this, "Finish CreateContentRelationJob in {0} seconds", new Long[] { (stop - start) / 1000 },
					null);
		} catch (Exception ex) {
			DfLogger.error(this, "Error in CreateContentRelationJob JOB: {0}", new String[] { ex.getMessage() }, ex);
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
			DfLogger.error(this, "Error in close: {0}", new String[] { var2.getMessage() }, var2);
		}
	}
}
