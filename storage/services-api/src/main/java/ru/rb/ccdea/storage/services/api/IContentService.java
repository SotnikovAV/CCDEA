package ru.rb.ccdea.storage.services.api;

import com.documentum.fc.client.IDfService;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.adapters.mq.binding.docput.ContentType;
import ru.rb.ccdea.adapters.mq.binding.docput.DocPutType;

import java.util.List;
import java.util.Set;

public interface IContentService extends IDfService {
	
	String createContentFromMQType(IDfSession dfSession, String contentSourceCode, String contentSourceId, DocPutType docPutXml) throws DfException;
	
	/**
	 * этот метод уже не нужен всвязи с изменением концепции
	 */
	@Deprecated
	String createContentFromMQType(IDfSession dfSession, ContentType contentXmlObject, String contentSourceCode,
			String contentSourceId, List<String> modifiedContentIdList, Set<IDfId> documentIds) throws DfException;
	
	/**
	 * этот метод уже не нужен всвязи с изменением концепции
	 */
	@Deprecated
	String createContentVersionFromMQType(IDfSession dfSession, ContentType contentXmlObject,
			String contentSourceCode, String contentSourceId, List<String> modifiedContentIdList, Set<IDfId> documentIds,
			IDfId contentId) throws DfException;
	
	/**
	 * этот метод уже не нужен всвязи с изменением концепции
	 */
	@Deprecated
	String appendContentFromMQType(IDfSession dfSession, ContentType contentXmlObject, String contentSourceCode,
			String contentSourceId, List<String> modifiedContentIdList, Set<IDfId> documentIds, IDfId contentId)
					throws DfException;
	
	/**
	 * этот метод уже не нужен всвязи с изменением концепции
	 */
	@Deprecated
	String updateContentFromMQType(IDfSession dfSession, ContentType contentXmlObject, String contentSourceCode,
			String contentSourceId, List<String> modifiedContentIdList, Set<IDfId> documentIds, IDfId contentId)
					throws DfException;
}
