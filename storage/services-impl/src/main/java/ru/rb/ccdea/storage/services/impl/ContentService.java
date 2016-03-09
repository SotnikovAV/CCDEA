package ru.rb.ccdea.storage.services.impl;

import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.*;
import ru.rb.ccdea.adapters.mq.binding.docput.ContentType;
import ru.rb.ccdea.storage.persistence.ContentPersistence;
import ru.rb.ccdea.storage.persistence.ctsutils.CTSRequestBuilder;
import ru.rb.ccdea.storage.persistence.fileutils.ContentLoader;
import ru.rb.ccdea.storage.services.api.IContentService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class ContentService extends DfService implements IContentService {
	@Override
	public String createContentFromMQType(IDfSession dfSession, ContentType contentXmlObject, String contentSourceCode,
			String contentSourceId, List<String> modifiedContentIdList, List<IDfId> documentIds) throws DfException {
		String transformJobId = null;

		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			DfLogger.info(this, "CreateContentFromMQType: {0}/{1}, DocIDs: {2}. Start",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString() }, null);

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			IDfSysObject originalContentSysObject = ContentPersistence.createContentObject(dfSession, contentSourceCode,
					contentSourceId, true);
			ContentLoader.loadContentFile(originalContentSysObject, contentXmlObject);
			
			for (IDfId documentId : documentIds) {
				ContentPersistence.createDocumentContentRelation(dfSession, documentId,
						originalContentSysObject.getObjectId());
			}

			String contentType = originalContentSysObject.getContentType();
			if (!ContentLoader.isPdfType(contentType)) {
				IDfSysObject documentContentSysObject = ContentPersistence.createContentObject(dfSession,
						contentSourceCode, contentSourceId, false);
				for (IDfId documentId : documentIds) {
					ContentPersistence.createDocumentContentRelation(dfSession, documentId,
							documentContentSysObject.getObjectId());
				}
				if(ContentLoader.isArchiveType(contentType)) {
					int index = 0;
					for(IDfSysObject contentPartSysObject:ContentPersistence.getContentPartsSysObject(originalContentSysObject)) {
						contentType = contentPartSysObject.getContentType();
						boolean isAlreadyPdf = ContentLoader.isPdfType(contentType);
						IDfSysObject documentContentPartSysObject = null;
						if(index == 0) {
							if(isAlreadyPdf) {
								ByteArrayInputStream is = contentPartSysObject.getContent();
								ByteArrayOutputStream os = new ByteArrayOutputStream();
								int b;
								while ((b = is.read()) != -1) {
								    os.write(b);
								}
								documentContentSysObject.setContent(os); 
								documentContentSysObject.save();
							} else {
								documentContentPartSysObject = documentContentSysObject;
							}
						} else {
							if(isAlreadyPdf) {
								documentContentPartSysObject = contentPartSysObject;
							} else {
								documentContentPartSysObject = ContentPersistence.createContentPartSysObject(
								dfSession, contentPartSysObject.getObjectName(), "pdf",
								documentContentSysObject.getObjectId(), index);
							}
						}
						if (!isAlreadyPdf) {
							transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
									documentContentPartSysObject.getObjectId().getId(), false, contentPartSysObject,
									false);
						}
						if (index > 0) {
							transformJobId = CTSRequestBuilder.mergePdfRequest(dfSession,
									documentContentSysObject.getObjectId().getId(), true, documentContentSysObject,
									documentContentPartSysObject, false);
						}
						index++;
					}
				} else {
					transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
							documentContentSysObject.getObjectId().getId(), false,
							originalContentSysObject);
				}	
			}

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}

			modifiedContentIdList.add(originalContentSysObject.getObjectId().getId());
			
			DfLogger.info(this, "CreateContentFromMQType: {0}/{1}, DocIDs: {2}. Finish (transform request: {3})",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString(), transformJobId }, null);
		} catch (DfException dfEx) {
			DfLogger.error(this, "CreateContentFromMQType: {0}/{1}, DocIDs: {2}. Error",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString() }, dfEx);
			throw dfEx;
		} catch (Exception ex) {
			DfLogger.error(this, "CreateContentFromMQType: {0}/{1}, DocIDs: {2}. Error",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString() }, ex);
			throw new DfException(ex);
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
		return transformJobId;
	}

	@Override
	public String createContentVersionFromMQType(IDfSession dfSession, ContentType contentXmlObject,
			String contentSourceCode, String contentSourceId, List<String> modifiedContentIdList, IDfId documentId,
			IDfId contentId) throws DfException {
		String transformJobId = null;

		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			DfLogger.info(this, "CreateContentVersionFromMQType: {0}/{1}, DocID: {2}. Start",
					new String[] { contentSourceCode, contentSourceId, documentId.getId() }, null);

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}
			
			IDfSysObject documentContentSysObject = (IDfSysObject) dfSession.getObject(contentId);

			IDfSysObject originalContentSysObject = ContentPersistence.createContentObject(dfSession, contentSourceCode,
					contentSourceId, true);
			ContentLoader.loadContentFile(originalContentSysObject, contentXmlObject);
			modifiedContentIdList.add(originalContentSysObject.getObjectId().getId());

			String contentType = originalContentSysObject.getContentType();
			if (!ContentLoader.isPdfType(contentType)) {
				if(ContentLoader.isArchiveType(contentType)) {
					int index = 0;
					for(IDfSysObject contentPartSysObject:ContentPersistence.getContentPartsSysObject(originalContentSysObject)) {
						contentType = contentPartSysObject.getContentType();
						boolean isAlreadyPdf = ContentLoader.isPdfType(contentType);
						IDfSysObject documentContentPartSysObject = null;
						if(index == 0) {
							if(isAlreadyPdf) {
								ByteArrayInputStream is = contentPartSysObject.getContent();
								ByteArrayOutputStream os = new ByteArrayOutputStream();
								int b;
								while ((b = is.read()) != -1) {
								    os.write(b);
								}
								documentContentSysObject.checkout();
								documentContentSysObject.setContent(os); 
								documentContentSysObject.checkin(false, null);
							} else {
								documentContentPartSysObject = documentContentSysObject;
							}
						} else {
							if (isAlreadyPdf) {
								documentContentPartSysObject = contentPartSysObject;
							} else {
								documentContentPartSysObject = ContentPersistence.createContentPartSysObject(dfSession,
										contentPartSysObject.getObjectName(), "pdf",
										documentContentSysObject.getObjectId(), index);
							}
						}
						if (!isAlreadyPdf) {
							transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
									documentContentPartSysObject.getObjectId().getId(), false, contentPartSysObject,
									false);
						}
						if (index > 0) {
							transformJobId = CTSRequestBuilder.mergePdfRequest(dfSession,
									documentContentSysObject.getObjectId().getId(), true, documentContentSysObject,
									documentContentPartSysObject, false);
						}
						index++;
					}
				} else {
					transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
							documentContentSysObject.getObjectId().getId(), true, originalContentSysObject);
				}
			}
			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}

			DfLogger.info(this, "CreateContentVersionFromMQType: {0}/{1}, DocID: {2}. Finish (transform request: {3})",
					new String[] { contentSourceCode, contentSourceId, documentId.getId(), transformJobId }, null);
		} catch (DfException dfEx) {
			DfLogger.error(this, "CreateContentVersionFromMQType: {0}/{1}, DocID: {2}. Error",
					new String[] { contentSourceCode, contentSourceId, documentId.getId() }, dfEx);
			throw dfEx;
		} catch (Exception ex) {
			DfLogger.error(this, "CreateContentVersionFromMQType: {0}/{1}, DocID: {2}. Error",
					new String[] { contentSourceCode, contentSourceId, documentId.getId() }, ex);
			throw new DfException(ex);
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
		return transformJobId;
	}

	@Override
	public String appendContentFromMQType(IDfSession dfSession, ContentType contentXmlObject, String contentSourceCode,
			String contentSourceId, List<String> modifiedContentIdList, IDfId documentId, IDfId contentId)
					throws DfException {
		String transformJobId = null;

		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			DfLogger.info(this, "AppendContentFromMQType: {0}/{1}, DocID: {2}. Start",
					new String[] { contentSourceCode, contentSourceId, documentId.getId() }, null);

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			IDfSysObject originalContentSysObject = ContentPersistence.createContentObject(dfSession, contentSourceCode,
					contentSourceId, true);
			ContentLoader.loadContentFile(originalContentSysObject, contentXmlObject);
			modifiedContentIdList.add(originalContentSysObject.getObjectId().getId());

			IDfSysObject documentContentSysObject = (IDfSysObject) dfSession.getObject(contentId);
			modifiedContentIdList.add(contentId.getId());
			
			String contentType = originalContentSysObject.getContentType();
			if (!ContentLoader.isPdfType(contentType)) {
				transformJobId = CTSRequestBuilder.mergePdfRequest(dfSession, contentId.getId(), true,
						documentContentSysObject, originalContentSysObject);
			} else if(ContentLoader.isArchiveType(contentType)) {
				int index = 0;
				for(IDfSysObject contentPartSysObject:ContentPersistence.getContentPartsSysObject(originalContentSysObject)) {
					contentType = contentPartSysObject.getContentType();
					boolean isAlreadyPdf = ContentLoader.isPdfType(contentType);
					IDfSysObject documentContentPartSysObject = null;
					if (isAlreadyPdf) {
						documentContentPartSysObject = contentPartSysObject;
					} else {
						documentContentPartSysObject = ContentPersistence.createContentPartSysObject(dfSession,
								contentPartSysObject.getObjectName(), "pdf",
								documentContentSysObject.getObjectId(), index);
					}
					
					if (!isAlreadyPdf) {
						transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
								documentContentPartSysObject.getObjectId().getId(), false, contentPartSysObject, false);
					}

					transformJobId = CTSRequestBuilder.mergePdfRequest(dfSession,
							documentContentSysObject.getObjectId().getId(), true, documentContentSysObject,
							documentContentPartSysObject, false);
					
					index++;
				}
			} else {
				IDfSysObject documentContentPartSysObject = ContentPersistence.createContentPartSysObject(dfSession,
						originalContentSysObject.getObjectName(), "pdf", contentId, 0);
				transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
						documentContentPartSysObject.getObjectId().getId(), false, originalContentSysObject, false);
				transformJobId = CTSRequestBuilder.mergePdfRequest(dfSession, contentId.getId(), true,
						documentContentSysObject, documentContentPartSysObject);
			}

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}

			DfLogger.info(this, "AppendContentFromMQType: {0}/{1}, DocID: {2}. Finish (transform request: {3})",
					new String[] { contentSourceCode, contentSourceId, documentId.getId(), transformJobId }, null);
		} catch (DfException dfEx) {
			DfLogger.error(this, "AppendContentFromMQType: {0}/{1}, DocID: {2}. Error",
					new String[] { contentSourceCode, contentSourceId, documentId.getId() }, dfEx);
			throw dfEx;
		} catch (Exception ex) {
			DfLogger.error(this, "AppendContentFromMQType: {0}/{1}, DocID: {2}. Error",
					new String[] { contentSourceCode, contentSourceId, documentId.getId() }, ex);
			throw new DfException(ex);
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
		return transformJobId;
	}

    @Override
	public String updateContentFromMQType(IDfSession dfSession, ContentType contentXmlObject, String contentSourceCode,
			String contentSourceId, List<String> modifiedContentIdList, IDfId documentId, IDfId contentId)
					throws DfException {
		String transformJobId = null;

		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			DfLogger.info(this, "UpdateContentFromMQType: {0}/{1}, DocID: {2}. Start",
					new String[] { contentSourceCode, contentSourceId, documentId.getId() }, null);

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			IDfSysObject originalContentSysObject = ContentPersistence.createContentObject(dfSession, contentSourceCode,
					contentSourceId, true);
			ContentLoader.loadContentFile(originalContentSysObject, contentXmlObject);
			modifiedContentIdList.add(originalContentSysObject.getObjectId().getId());

			modifiedContentIdList.add(contentId.getId());
			
			String contentType = originalContentSysObject.getContentType();
			if (!ContentLoader.isPdfType(contentType)) {
				transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession, contentId.getId(), false,
						originalContentSysObject);
			} else {
				IDfSysObject documentContentSysObject = (IDfSysObject) dfSession.getObject(contentId);
				ByteArrayInputStream is = originalContentSysObject.getContent();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				int b;
				while ((b = is.read()) != -1) {
				    os.write(b);
				}
				documentContentSysObject.setContent(os); 
				documentContentSysObject.save();
			}

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}

			DfLogger.info(this, "UpdateContentFromMQType: {0}/{1}, DocID: {2}. Finish (transform request: {3})",
					new String[] { contentSourceCode, contentSourceId, documentId.getId(), transformJobId }, null);
		} catch (DfException dfEx) {
			DfLogger.error(this, "UpdateContentFromMQType: {0}/{1}, DocID: {2}. Error",
					new String[] { contentSourceCode, contentSourceId, documentId.getId() }, dfEx);
			throw dfEx;
		} catch (Exception ex) {
			DfLogger.error(this, "UpdateContentFromMQType: {0}/{1}, DocID: {2}. Error",
					new String[] { contentSourceCode, contentSourceId, documentId.getId() }, ex);
			throw new DfException(ex);
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
		return transformJobId;
	}

}
