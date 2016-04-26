package ru.rb.ccdea.storage.services.api;

import com.documentum.fc.client.IDfService;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.adapters.mq.binding.docput.ContentType;

import java.util.List;
import java.util.Set;

public interface IContentService extends IDfService {
	public String createContentFromMQType(IDfSession dfSession, ContentType contentXmlObject, String contentSourceCode,
			String contentSourceId, List<String> modifiedContentIdList, Set<IDfId> documentIds) throws DfException;

	public String createContentVersionFromMQType(IDfSession dfSession, ContentType contentXmlObject,
			String contentSourceCode, String contentSourceId, List<String> modifiedContentIdList, Set<IDfId> documentIds,
			IDfId contentId) throws DfException;

	public String appendContentFromMQType(IDfSession dfSession, ContentType contentXmlObject, String contentSourceCode,
			String contentSourceId, List<String> modifiedContentIdList, Set<IDfId> documentIds, IDfId contentId)
					throws DfException;

	public String updateContentFromMQType(IDfSession dfSession, ContentType contentXmlObject, String contentSourceCode,
			String contentSourceId, List<String> modifiedContentIdList, Set<IDfId> documentIds, IDfId contentId)
					throws DfException;
}
