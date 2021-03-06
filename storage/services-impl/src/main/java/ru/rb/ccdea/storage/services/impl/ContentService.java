package ru.rb.ccdea.storage.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;

import ru.rb.ccdea.adapters.mq.binding.docput.ContentType;
import ru.rb.ccdea.adapters.mq.binding.docput.DocPutType;
import ru.rb.ccdea.adapters.mq.binding.docput.OldObjectIdentifiersType;
import ru.rb.ccdea.storage.persistence.ContentPersistence;
import ru.rb.ccdea.storage.persistence.fileutils.ContentLoader;
import ru.rb.ccdea.storage.services.api.IContentService;

public class ContentService extends DfService implements IContentService {
	
	@Override
	public String createContentFromMQType(IDfSession dfSession, String contentSourceCode, String contentSourceId, DocPutType docPutXml) throws DfException {
		String contentId = "";
		List<String> documentIds = new ArrayList<String>();
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			IDfSysObject originalContentSysObject = ContentPersistence.getContentObject(dfSession, contentSourceCode,
					contentSourceId);
			if (originalContentSysObject == null) {
				originalContentSysObject = ContentPersistence.createContentObject(dfSession, contentSourceCode,
						contentSourceId, true);
			}
			contentId = originalContentSysObject.getObjectId().getId();
			int index = 0;
			for(OldObjectIdentifiersType identifiers: docPutXml.getOriginIdentification()) {
				String sourceSystem = identifiers.getSourceSystem();
				sourceSystem = sourceSystem == null ? "" : sourceSystem.trim();
				String sourceId = identifiers.getSourceId();
				sourceId = sourceId == null ? "" : sourceId.trim();
				index = ContentPersistence.setSourceIdentifier(originalContentSysObject, sourceSystem, sourceId, index);
				documentIds.add(identifiers.getSourceSystem() + '/' + identifiers.getSourceId());
			}
			for(OldObjectIdentifiersType identifiers: docPutXml.getOriginDocIdentification()) {
				String sourceSystem = identifiers.getSourceSystem();
				sourceSystem = sourceSystem == null ? "" : sourceSystem.trim();
				String sourceId = identifiers.getSourceId();
				sourceId = sourceId == null ? "" : sourceId.trim();
				index = ContentPersistence.setSourceIdentifier(originalContentSysObject, sourceSystem, sourceId, index);
				documentIds.add(identifiers.getSourceSystem() + '/' + identifiers.getSourceId());
			}
			originalContentSysObject.save();

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}
			DfLogger.info(this, "CreateContentFromMQType: {0}/{1}, DocIDs: {2}. Finish (transform request: {3})",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString(), contentId }, null);
			return contentId;
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
	}
	
	@Override
	public String createContentFromMQType(IDfSession dfSession, ContentType contentXmlObject, String contentSourceCode,
			String contentSourceId, List<String> modifiedContentIdList, Set<IDfId> documentIds) throws DfException {
		String transformJobId = null;
//		Set<String> transformResponseIds = new LinkedHashSet<String>();
//		Set<String> contentForAppentingIds = new LinkedHashSet<String>();
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

//			IDfSysObject documentContentSysObject = null;
//			String contentType = originalContentSysObject.getContentType();
//			if (!ContentLoader.isPdfType(contentType)) {
//				documentContentSysObject = ContentPersistence.createContentObject(dfSession, contentSourceCode,
//						contentSourceId, false);
//				for (IDfId documentId : documentIds) {
//					ContentPersistence.createDocumentContentRelation(dfSession, documentId,
//							documentContentSysObject.getObjectId());
//				}
//				if (ContentLoader.isArchiveType(contentType)) {
//					int index = 0;
//					for (IDfSysObject contentPartSysObject : ContentPersistence
//							.getContentPartsSysObject(originalContentSysObject)) {
//						contentType = contentPartSysObject.getContentType();
//						boolean isAlreadyPdf = ContentLoader.isPdfType(contentType);
//						IDfSysObject documentContentPartSysObject = null;
//						if (index == 0) {
//							if (isAlreadyPdf) {
//								ContentLoader.saveContent(documentContentSysObject, "pdf",
//										contentPartSysObject.getContent(), false);
//							} else {
//								documentContentPartSysObject = documentContentSysObject;
//							}
//						} else {
//							if (isAlreadyPdf) {
//								documentContentPartSysObject = contentPartSysObject;
//							} else {
//								documentContentPartSysObject = ContentPersistence.createContentPartSysObject(dfSession,
//										contentPartSysObject.getObjectName(), "pdf",
//										documentContentSysObject.getObjectId(), index);
//							}
//						}
//						if (!isAlreadyPdf) {
//							transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
//									documentContentPartSysObject.getObjectId().getId(), false, contentPartSysObject,
//									false);
//							transformResponseIds.add(transformJobId);
//							if (index > 0) {
//								contentForAppentingIds.add(documentContentPartSysObject.getObjectId().getId());
//							}
//						} else if (index > 0) {
//							contentForAppentingIds.add(documentContentPartSysObject.getObjectId().getId());
//						}
//						index++;
//					}
//				} else {
//					transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
//							documentContentSysObject.getObjectId().getId(), false, originalContentSysObject);
//				}
//			} else {
//				documentContentSysObject = originalContentSysObject;
//			}

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}
//
			modifiedContentIdList.add(originalContentSysObject.getObjectId().getId());
//
//			if (transformResponseIds.size() > 0) {
//				for (String transformResponseId : transformResponseIds) {
//					String status = CTSRequestBuilder.waitForJobComplete(dfSession, transformResponseId);
//					DfLogger.info(this, "CTS Response (" + transformResponseId + ") " + status, null, null);
//				}
//				
//				IDfSysObject oldDocumentContentSysObject = documentContentSysObject;
//
//				for (String contentForAppendingId : contentForAppentingIds) {
//
//					isTransAlreadyActive = dfSession.isTransactionActive();
//					if (!isTransAlreadyActive) {
//						dfSession.beginTrans();
//					}
//					IDfSysObject newDocumentContentSysObject = ContentPersistence.createContentObject(dfSession,
//							contentSourceCode, contentSourceId, false);
//					if (!isTransAlreadyActive) {
//						dfSession.commitTrans();
//					}
//
//					IDfSysObject documentContentPartSysObject = (IDfSysObject) dfSession
//							.getObject(new DfId(contentForAppendingId));
//					
//					String status = ContentPersistence.checkContentAvaliable(dfSession, documentContentSysObject);
//					DfLogger.info(this, status, null, null);
//
//					transformJobId = CTSRequestBuilder.mergePdfRequest(dfSession,
//							newDocumentContentSysObject.getObjectId().getId(), false, documentContentSysObject,
//							documentContentPartSysObject, false);
//
//					status = CTSRequestBuilder.waitForJobComplete(dfSession, transformJobId);
//					DfLogger.info(this, "CTS Response (" + transformJobId + ") " + status, null, null);
//					documentContentSysObject = newDocumentContentSysObject;
//
//				}
//
//				for (IDfId docId : documentIds) {
//					ContentPersistence.createDocumentContentRelation(dfSession, docId,
//							documentContentSysObject.getObjectId());
//				}

//				for (String contentForAppendingId : contentForAppentingIds) {
//					IDfSysObject documentContentPartSysObject = (IDfSysObject) dfSession
//							.getObject(new DfId(contentForAppendingId));
//					documentContentPartSysObject.destroy();
//				}
				
//				oldDocumentContentSysObject.destroy();
//			}

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
			String contentSourceCode, String contentSourceId, List<String> modifiedContentIdList, Set<IDfId> documentIds,
			IDfId contentId) throws DfException {
		String transformJobId = null;
//		Set<String> transformResponseIds = new LinkedHashSet<String>();
//		Set<String> contentForAppentingIds = new LinkedHashSet<String>();
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			DfLogger.info(this, "CreateContentVersionFromMQType: {0}/{1}, DocID: {2}. Start",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString() }, null);

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}
			
			IDfId documentId = documentIds.iterator().next();

//			IDfSysObject oldDocumentContentSysObject = (IDfSysObject) dfSession.getObject(contentId);

			IDfSysObject originalContentSysObject = ContentPersistence.searchOriginalContentObjectByDocumentId(dfSession, documentId.getId());

			ContentLoader.loadContentFile(originalContentSysObject, contentXmlObject, true);
			

//			IDfSysObject documentContentSysObject = null;
//
//			String contentType = originalContentSysObject.getContentType();
//			if (!ContentLoader.isPdfType(contentType)) {
//				if (ContentLoader.isArchiveType(contentType)) {
//					documentContentSysObject = ContentPersistence.createContentObject(dfSession, contentSourceCode,
//							contentSourceId, false);
//					int index = 0;
//					for (IDfSysObject contentPartSysObject : ContentPersistence
//							.getContentPartsSysObject(originalContentSysObject)) {
//						contentType = contentPartSysObject.getContentType();
//						boolean isAlreadyPdf = ContentLoader.isPdfType(contentType);
//						IDfSysObject documentContentPartSysObject = null;
//						if (index == 0) {
//							if (isAlreadyPdf) {								
//								ContentLoader.saveContent(documentContentSysObject, "pdf",
//										contentPartSysObject.getContent(), false);
//							} else {
//								documentContentPartSysObject = documentContentSysObject;
//							}
//						} else {
//							if (isAlreadyPdf) {
//								documentContentPartSysObject = contentPartSysObject;
//							} else {
//								documentContentPartSysObject = ContentPersistence.createContentPartSysObject(dfSession,
//										contentPartSysObject.getObjectName(), "pdf",
//										documentContentSysObject.getObjectId(), index);
//							}
//						}
//						if (!isAlreadyPdf) {
//							transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
//									documentContentPartSysObject.getObjectId().getId(), false, contentPartSysObject,
//									false);
//							transformResponseIds.add(transformJobId);
//							if (index > 0) {
//								contentForAppentingIds.add(documentContentPartSysObject.getObjectId().getId());
//							}
//						} else if (index > 0) {
//							contentForAppentingIds.add(documentContentPartSysObject.getObjectId().getId());
//						}
//						index++;
//					}
//				} else {
//					transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
//							oldDocumentContentSysObject.getObjectId().getId(), true, originalContentSysObject);
//				}
//			} else {
//				ContentLoader.saveContent(oldDocumentContentSysObject, "pdf",
//						originalContentSysObject.getContent(), true);
//			}
			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}
			
			modifiedContentIdList.add(originalContentSysObject.getObjectId().getId());
//
//			if (transformResponseIds.size() > 0) {
//				for (String transformResponseId : transformResponseIds) {
//					String status = CTSRequestBuilder.waitForJobComplete(dfSession, transformResponseId);
//					DfLogger.info(this, "CTS Response (" + transformResponseId + ") " + status, null, null);
//				}
//
//				for (String contentForAppendingId : contentForAppentingIds) {
//
//					isTransAlreadyActive = dfSession.isTransactionActive();
//					if (!isTransAlreadyActive) {
//						dfSession.beginTrans();
//					}
//					IDfSysObject newDocumentContentSysObject = ContentPersistence.createContentObject(dfSession,
//							contentSourceCode, contentSourceId, false);
//					if (!isTransAlreadyActive) {
//						dfSession.commitTrans();
//					}
//
//					IDfSysObject documentContentPartSysObject = (IDfSysObject) dfSession
//							.getObject(new DfId(contentForAppendingId));
//					
//					String status = ContentPersistence.checkContentAvaliable(dfSession, documentContentSysObject);
//					DfLogger.info(this, status, null, null);
//
//					transformJobId = CTSRequestBuilder.mergePdfRequest(dfSession,
//							newDocumentContentSysObject.getObjectId().getId(), false, documentContentSysObject,
//							documentContentPartSysObject, false);
//
//					status = CTSRequestBuilder.waitForJobComplete(dfSession, transformJobId);
//					DfLogger.info(this, "CTS Response (" + transformJobId + ") " + status, null, null);
//					documentContentSysObject = newDocumentContentSysObject;
//
//				}
//
////				for (String contentForAppendingId : contentForAppentingIds) {
////					IDfSysObject documentContentPartSysObject = (IDfSysObject) dfSession
////							.getObject(new DfId(contentForAppendingId));
////					documentContentPartSysObject.destroy();
////				}
//
//				if (documentContentSysObject != null) {
//					ContentLoader.saveContent(oldDocumentContentSysObject, "pdf",
//							documentContentSysObject.getContent(), true);
//				}
//			}

			DfLogger.info(this, "CreateContentVersionFromMQType: {0}/{1}, DocID: {2}. Finish (transform request: {3})",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString(), transformJobId }, null);
		} catch (DfException dfEx) {
			DfLogger.error(this, "CreateContentVersionFromMQType: {0}/{1}, DocID: {2}. Error",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString() }, dfEx);
			throw dfEx;
		} catch (Exception ex) {
			DfLogger.error(this, "CreateContentVersionFromMQType: {0}/{1}, DocID: {2}. Error",
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
	public String appendContentFromMQType(IDfSession dfSession, ContentType contentXmlObject, String contentSourceCode,
			String contentSourceId, List<String> modifiedContentIdList, Set<IDfId> documentIds, IDfId contentId)
					throws DfException {
		String transformJobId = null;
//		Set<String> transformResponseIds = new LinkedHashSet<String>();
//		Set<String> contentForAppentingIds = new LinkedHashSet<String>();
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			DfLogger.info(this, "AppendContentFromMQType: {0}/{1}, DocID: {2}. Start",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString() }, null);

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			IDfSysObject originalContentSysObject = ContentPersistence.createContentObject(dfSession, contentSourceCode,
					contentSourceId, true);
			ContentLoader.loadContentFile(originalContentSysObject, contentXmlObject);
						
			for (IDfId docId : documentIds) {
				ContentPersistence.createDocumentContentRelation(dfSession, docId,
						originalContentSysObject.getObjectId());
			}

//			IDfSysObject documentContentSysObject = (IDfSysObject) dfSession.getObject(contentId);
//			modifiedContentIdList.add(contentId.getId());

//			String contentType = originalContentSysObject.getContentType();
//			if (ContentLoader.isPdfType(contentType)) {
//				transformJobId = CTSRequestBuilder.mergePdfRequest(dfSession, contentId.getId(), true,
//						documentContentSysObject, originalContentSysObject);
//			} else if (ContentLoader.isArchiveType(contentType)) {
//				int index = 0;
//				for (IDfSysObject contentPartSysObject : ContentPersistence
//						.getContentPartsSysObject(originalContentSysObject)) {
//					contentType = contentPartSysObject.getContentType();
//					boolean isAlreadyPdf = ContentLoader.isPdfType(contentType);
//					IDfSysObject documentContentPartSysObject = null;
//					if (isAlreadyPdf) {
//						documentContentPartSysObject = contentPartSysObject;
//					} else {
//						documentContentPartSysObject = ContentPersistence.createContentPartSysObject(dfSession,
//								contentPartSysObject.getObjectName(), "pdf", documentContentSysObject.getObjectId(),
//								index);
//					}
//
//					if (!isAlreadyPdf) {
//						transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
//								documentContentPartSysObject.getObjectId().getId(), false, contentPartSysObject, false);
//						transformResponseIds.add(transformJobId);
//						contentForAppentingIds.add(documentContentPartSysObject.getObjectId().getId());
//					} else {
//						contentForAppentingIds.add(documentContentPartSysObject.getObjectId().getId());
//					}
//					index++;
//				}
//			} else {
//				IDfSysObject documentContentPartSysObject = ContentPersistence.createContentPartSysObject(dfSession,
//						originalContentSysObject.getObjectName(), "pdf", contentId, 0);
//				transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
//						documentContentPartSysObject.getObjectId().getId(), false, originalContentSysObject, false);
//				transformResponseIds.add(transformJobId);
//				contentForAppentingIds.add(documentContentPartSysObject.getObjectId().getId());
//			}
//
			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}
			
			modifiedContentIdList.add(originalContentSysObject.getObjectId().getId());
//
//			if (transformResponseIds.size() > 0) {
//				for (String transformResponseId : transformResponseIds) {
//					String status = CTSRequestBuilder.waitForJobComplete(dfSession, transformResponseId);
//					DfLogger.info(this, "CTS Response (" + transformResponseId + ") " + status, null, null);
//				}
//
//				IDfSysObject oldDocumentContentSysObject = documentContentSysObject;
//
//				for (String contentForAppendingId : contentForAppentingIds) {
//
//					isTransAlreadyActive = dfSession.isTransactionActive();
//					if (!isTransAlreadyActive) {
//						dfSession.beginTrans();
//					}
//					IDfSysObject newDocumentContentSysObject = ContentPersistence.createContentObject(dfSession,
//							contentSourceCode, contentSourceId, false);
//					if (!isTransAlreadyActive) {
//						dfSession.commitTrans();
//					}
//
//					IDfSysObject documentContentPartSysObject = (IDfSysObject) dfSession
//							.getObject(new DfId(contentForAppendingId));
//					
//					String status = ContentPersistence.checkContentAvaliable(dfSession, documentContentSysObject);
//					DfLogger.info(this, status, null, null);
//
//					transformJobId = CTSRequestBuilder.mergePdfRequest(dfSession,
//							newDocumentContentSysObject.getObjectId().getId(), false, documentContentSysObject,
//							documentContentPartSysObject, false);
//
//					status = CTSRequestBuilder.waitForJobComplete(dfSession, transformJobId);
//					DfLogger.info(this, "CTS Response (" + transformJobId + ") " + status, null, null);
//					documentContentSysObject = newDocumentContentSysObject;
//
//				}
//
//				for (IDfId docId : documentIds) {
//					ContentPersistence.createDocumentContentRelation(dfSession, docId,
//							documentContentSysObject.getObjectId());
//				}
//
////				for (String contentForAppendingId : contentForAppentingIds) {
////					IDfSysObject documentContentPartSysObject = (IDfSysObject) dfSession
////							.getObject(new DfId(contentForAppendingId));
////					documentContentPartSysObject.destroy();
////				}
////				oldDocumentContentSysObject.destroy();
//			}

			DfLogger.info(this, "AppendContentFromMQType: {0}/{1}, DocID: {2}. Finish (transform request: {3})",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString(), transformJobId }, null);
		} catch (DfException dfEx) {
			DfLogger.error(this, "AppendContentFromMQType: {0}/{1}, DocID: {2}. Error",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString() }, dfEx);
			throw dfEx;
		} catch (Exception ex) {
			DfLogger.error(this, "AppendContentFromMQType: {0}/{1}, DocID: {2}. Error",
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
	public String updateContentFromMQType(IDfSession dfSession, ContentType contentXmlObject, String contentSourceCode,
			String contentSourceId, List<String> modifiedContentIdList, Set<IDfId> documentIds, IDfId contentId)
					throws DfException {
		String transformJobId = null;
//		Set<String> transformResponseIds = new LinkedHashSet<String>();
//		Set<String> contentForAppentingIds = new LinkedHashSet<String>();
		boolean isTransAlreadyActive = dfSession.isTransactionActive();
		try {
			DfLogger.info(this, "UpdateContentFromMQType: {0}/{1}, DocID: {2}. Start",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString() }, null);

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

//			IDfSysObject documentContentSysObject = null;
			IDfSysObject originalContentSysObject = ContentPersistence.createContentObject(dfSession, contentSourceCode,
					contentSourceId, true);
			ContentLoader.loadContentFile(originalContentSysObject, contentXmlObject);
			
//			String contentType = originalContentSysObject.getContentType();
//			if (!ContentLoader.isPdfType(contentType)) {
//				transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession, contentId.getId(), false,
//						originalContentSysObject);
//			} else if (ContentLoader.isArchiveType(contentType)) {
//				documentContentSysObject = (IDfSysObject) dfSession.getObject(contentId);
//				int index = 0;
//				for (IDfSysObject contentPartSysObject : ContentPersistence
//						.getContentPartsSysObject(originalContentSysObject)) {
//					contentType = contentPartSysObject.getContentType();
//					boolean isAlreadyPdf = ContentLoader.isPdfType(contentType);
//					IDfSysObject documentContentPartSysObject = null;
//					if (isAlreadyPdf) {
//						documentContentPartSysObject = contentPartSysObject;
//					} else {
//						documentContentPartSysObject = ContentPersistence.createContentPartSysObject(dfSession,
//								contentPartSysObject.getObjectName(), "pdf", documentContentSysObject.getObjectId(),
//								index);
//					}
//
//					if (!isAlreadyPdf) {
//						transformJobId = CTSRequestBuilder.convertToPdfRequest(dfSession,
//								documentContentPartSysObject.getObjectId().getId(), false, contentPartSysObject, false);
//						transformResponseIds.add(transformJobId);
//						contentForAppentingIds.add(documentContentPartSysObject.getObjectId().getId());
//					} else {
//						contentForAppentingIds.add(documentContentPartSysObject.getObjectId().getId());
//					}
//					index++;
//				}
//			} else {
//				ContentLoader.saveContent(documentContentSysObject, "pdf", originalContentSysObject.getContent(),
//						false);
//			}

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}
			
			modifiedContentIdList.add(originalContentSysObject.getObjectId().getId());

//			if (transformResponseIds.size() > 0) {
//				IDfSysObject oldDocumentContentSysObject = documentContentSysObject;
//
//				for (String transformResponseId : transformResponseIds) {
//					String status = CTSRequestBuilder.waitForJobComplete(dfSession, transformResponseId);
//					DfLogger.info(this, "CTS Response (" + transformResponseId + ") " + status, null, null);
//				}
//
//				for (String contentForAppendingId : contentForAppentingIds) {
//
//					isTransAlreadyActive = dfSession.isTransactionActive();
//					if (!isTransAlreadyActive) {
//						dfSession.beginTrans();
//					}
//					IDfSysObject newDocumentContentSysObject = ContentPersistence.createContentObject(dfSession,
//							contentSourceCode, contentSourceId, false);
//					if (!isTransAlreadyActive) {
//						dfSession.commitTrans();
//					}
//
//					IDfSysObject documentContentPartSysObject = (IDfSysObject) dfSession
//							.getObject(new DfId(contentForAppendingId));
//					
//					String status = ContentPersistence.checkContentAvaliable(dfSession, documentContentSysObject);
//					DfLogger.info(this, status, null, null);
//
//					transformJobId = CTSRequestBuilder.mergePdfRequest(dfSession,
//							newDocumentContentSysObject.getObjectId().getId(), true, documentContentSysObject,
//							documentContentPartSysObject, false);
//
//					status = CTSRequestBuilder.waitForJobComplete(dfSession, transformJobId);
//					DfLogger.info(this, "CTS Response (" + transformJobId + ") " + status, null, null);
//					documentContentSysObject = newDocumentContentSysObject;
//
//				}
//
//				for (IDfId docId : documentIds) {
//					ContentPersistence.createDocumentContentRelation(dfSession, docId,
//							documentContentSysObject.getObjectId());
//				}
//
////				for (String contentForAppendingId : contentForAppentingIds) {
////					IDfSysObject documentContentPartSysObject = (IDfSysObject) dfSession
////							.getObject(new DfId(contentForAppendingId));
////					documentContentPartSysObject.destroy();
////				}
////				oldDocumentContentSysObject.destroy();
//			}

			DfLogger.info(this, "UpdateContentFromMQType: {0}/{1}, DocID: {2}. Finish (transform request: {3})",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString(), transformJobId }, null);
		} catch (DfException dfEx) {
			DfLogger.error(this, "UpdateContentFromMQType: {0}/{1}, DocID: {2}. Error",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString() }, dfEx);
			throw dfEx;
		} catch (Exception ex) {
			DfLogger.error(this, "UpdateContentFromMQType: {0}/{1}, DocID: {2}. Error",
					new String[] { contentSourceCode, contentSourceId, documentIds.toString() }, ex);
			throw new DfException(ex);
		} finally {
			if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
				dfSession.abortTrans();
			}
		}
		return transformJobId;
	}

	

}
