/**
 * 
 */
package ru.rb.ccdea;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;

import ru.rb.ccdea.adapters.mq.binding.docput.DocPutType;
import ru.rb.ccdea.adapters.mq.receivers.BaseReceiverMethod;
import ru.rb.ccdea.adapters.mq.receivers.DocPutReceiverMethod;
import ru.rb.ccdea.adapters.mq.receivers.MCDocInfoModifyContractReceiverMethod;
import ru.rb.ccdea.adapters.mq.receivers.MCDocInfoModifyPSReceiverMethod;
import ru.rb.ccdea.adapters.mq.utils.MessageObjectProcessor;
import ru.rb.ccdea.adapters.mq.utils.UnifiedResult;
import ru.rb.ccdea.adapters.mq.utils.XmlContentValidator;
import ru.rb.ccdea.storage.jobs.ContractMessageJob;
import ru.rb.ccdea.storage.jobs.DocPutMesssageJob;
import ru.rb.ccdea.storage.jobs.DocStateNotifyMessageJob;
import ru.rb.ccdea.storage.jobs.PassportMessageJob;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;

/**
 * @author sotnik
 *
 */
public class TestIntegration {

	@Test
	public void test() throws Exception {
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

			String[] formats = { "doc",
					 "docx",
					 "xls",
					 "xlsx",
					 "ppt", "pptx", "txt", "rtf", "odt", "xml", "tif", "tiff",
					 "jpg",
					 "jpeg", "png", "gif", "bmp", "prn", "zip", 
//					 "arj", 
					 "rar",
					 "7z",
					 "pdf"
			};

			String filePath = "c:/Development/Workspaces/CCDEA_GITHUB/test/Contract/contract1.xml";
			String contentMsgFilePath = "c:/Development/Workspaces/CCDEA_GITHUB/test/DocPut/DocReference/zip.xml";
			String baseContentPath = "c:/Development/Workspaces/CCDEA_GITHUB/test/test";
			String[] contentFilePathes = new String[formats.length];
			for (int i = 0; i < formats.length; i++) {
				contentFilePathes[i] = baseContentPath + '.' + formats[i];
			}
			

			MCDocInfoModifyContractReceiverMethod receiverMethod = new MCDocInfoModifyContractReceiverMethod();
			UnifiedResult receiverResult = UnifiedResult.getSuccessResultInstance();
			
			IDfSysObject messageSysObject = ExternalMessagePersistence.createEmptyMessageObjectFromFile(testSession,
					filePath);

			System.out.println(
					"Message object " + messageSysObject.getObjectId().getId() + " created from file " + filePath);

			Object messageXmlContent = receiverMethod.getXmlContent(messageSysObject, receiverResult);
			if (receiverResult.isSuccess()) {
				XmlContentValidator[] validators = receiverMethod
						.getXmlContentValidators(messageSysObject.getSession());
				if (receiverResult.isSuccess() && validators != null) {
					try {
						for (XmlContentValidator validator : validators) {
							validator.validate(messageXmlContent, receiverResult);
						}
					} catch (Exception ex) {
						receiverResult.setError(UnifiedResult.INTERNAL_ERROR_CODE,
								"Internal error during validation: " + ex.getMessage());

					}
				}
			}

			MessageObjectProcessor messageObjectProcessor = receiverMethod.getMessageObjectProcessor(messageXmlContent);

			if (receiverResult.isSuccess()) {
				messageObjectProcessor.readValuesFromXml(receiverResult);
			}

			messageObjectProcessor.storeMessageObjectOnValidation(messageSysObject);

			if (receiverResult.isSuccess()) {
				try {
					if (messageObjectProcessor.isCreateVerbSpecified()
							&& !messageObjectProcessor.isFirstValidMessage(messageSysObject)) {
						receiverResult.setError(UnifiedResult.WRONG_ORDER_CODE,
								"Received modification flag " + messageObjectProcessor.getModificationVerb()
										+ " but this is not first message for "
										+ messageObjectProcessor.getSourceKey());
					} else if (messageObjectProcessor.isUpdateVerbSpecified()
							&& messageObjectProcessor.isFirstValidMessage(messageSysObject)) {
						receiverResult.setError(UnifiedResult.WRONG_ORDER_CODE,
								"Received modification flag " + messageObjectProcessor.getModificationVerb()
										+ " but this is first message for " + messageObjectProcessor.getSourceKey());
					}
				} catch (DfException dfEx) {
					receiverResult.setError(UnifiedResult.INTERNAL_ERROR_CODE,
							"Internal error during check message order: " + dfEx.getMessage());

				}
			}

			receiverMethod.beforeGenearateResult(messageObjectProcessor, messageSysObject, receiverResult);

			receiverResult.generateResultDate();
			messageObjectProcessor.storeMessageObjectAfterValidation(messageSysObject, receiverResult);
			// receiverMethod.fillReplyObject((IDfWorkitemEx) iDfWorkitem,
			// receiverResult);
			if (receiverResult.isError()) {
				System.out.println("Return error " + receiverResult.getErrorCode() + " with description: "
						+ receiverResult.getErrorDescription());
			}

			ContractMessageJob contractJob = new ContractMessageJob();
			String contractId = contractJob.process(testSession, messageSysObject.getObjectId());

			DocStateNotifyMessageJob notifyJob = new DocStateNotifyMessageJob();
			notifyJob.process(testSession);
			notifyJob.updateWaitingContents(testSession);
			
			IDfSysObject contract = (IDfSysObject) testSession.getObject(new DfId(contractId));

			for (int i=0;i<contentFilePathes.length;i++) {
				BaseReceiverMethod contentReceiverMethod = new DocPutReceiverMethod();

				receiverResult = UnifiedResult.getSuccessResultInstance();
				messageSysObject = ExternalMessagePersistence.createEmptyMessageObjectFromFile(testSession,
						contentMsgFilePath);
				
				JAXBContext jc = JAXBContext.newInstance(DocPutType.class);
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				DocPutType docPutXmlObject = unmarshaller
						.unmarshal(new StreamSource(messageSysObject.getContent()), DocPutType.class).getValue();

				docPutXmlObject.getContent().getDocReference().get(0)
						.setFileReference(contentFilePathes[i]);
				docPutXmlObject.getContent().getDocReference().get(0).setFileFormat(formats[i]);
				docPutXmlObject.getOriginIdentification().setSourceId(contract.getString(ExternalMessagePersistence.ATTR_DOC_SOURCE_ID));
				docPutXmlObject.getOriginIdentification().setSourceSystem(contract.getString(ExternalMessagePersistence.ATTR_DOC_SOURCE_CODE));
				
				Marshaller marshaller = jc.createMarshaller();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				marshaller.marshal(new JAXBElement<DocPutType>(new QName("uri","local"), DocPutType.class, docPutXmlObject), os);
				messageSysObject.setContent(os);
				messageSysObject.save();
				

				System.out.println(
						"Message object " + messageSysObject.getObjectId().getId() + " created from file " + filePath);

				messageXmlContent = contentReceiverMethod.getXmlContent(messageSysObject, receiverResult);
				if (receiverResult.isSuccess()) {
					XmlContentValidator[] validators = contentReceiverMethod
							.getXmlContentValidators(messageSysObject.getSession());
					if (receiverResult.isSuccess() && validators != null) {
						try {
							for (XmlContentValidator validator : validators) {
								validator.validate(messageXmlContent, receiverResult);
							}
						} catch (Exception ex) {
							receiverResult.setError(UnifiedResult.INTERNAL_ERROR_CODE,
									"Internal error during validation: " + ex.getMessage());

						}
					}
				}

				messageObjectProcessor = contentReceiverMethod.getMessageObjectProcessor(messageXmlContent);

				if (receiverResult.isSuccess()) {
					messageObjectProcessor.readValuesFromXml(receiverResult);
				}

				messageObjectProcessor.storeMessageObjectOnValidation(messageSysObject);

				if (receiverResult.isSuccess()) {
					try {
						if (messageObjectProcessor.isCreateVerbSpecified()
								&& !messageObjectProcessor.isFirstValidMessage(messageSysObject)) {
							receiverResult.setError(UnifiedResult.WRONG_ORDER_CODE,
									"Received modification flag " + messageObjectProcessor.getModificationVerb()
											+ " but this is not first message for "
											+ messageObjectProcessor.getSourceKey());
						} else if (messageObjectProcessor.isUpdateVerbSpecified()
								&& messageObjectProcessor.isFirstValidMessage(messageSysObject)) {
							receiverResult.setError(UnifiedResult.WRONG_ORDER_CODE,
									"Received modification flag " + messageObjectProcessor.getModificationVerb()
											+ " but this is first message for "
											+ messageObjectProcessor.getSourceKey());
						}
					} catch (DfException dfEx) {
						receiverResult.setError(UnifiedResult.INTERNAL_ERROR_CODE,
								"Internal error during check message order: " + dfEx.getMessage());

					}
				}

				contentReceiverMethod.beforeGenearateResult(messageObjectProcessor, messageSysObject, receiverResult);

				receiverResult.generateResultDate();
				messageObjectProcessor.storeMessageObjectAfterValidation(messageSysObject, receiverResult);
				// receiverMethod.fillReplyObject((IDfWorkitemEx) iDfWorkitem,
				// receiverResult);
				if (receiverResult.isError()) {
					System.out.println("Return error " + receiverResult.getErrorCode() + " with description: "
							+ receiverResult.getErrorDescription());
				}
				
			}
			
			DocPutMesssageJob contentJob = new DocPutMesssageJob();
			
			while(true) {
				contentJob.process(testSession);
				notifyJob.process(testSession);
				notifyJob.updateWaitingContents(testSession);
				List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(testSession,
						ExternalMessagePersistence.MESSAGE_TYPE_DOCPUT, new Date());
				if(messageIdList.size() == 0) {
					break;
				}
			}

		} finally {
			if (sessionManager != null && testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}

	
	@Test
	public void testPS() throws Exception {
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

			String[] formats = { "doc",
					 "docx",
					 "xls",
					 "xlsx",
					 "ppt", "pptx", "txt", "rtf", "odt", "xml", "tif", "tiff",
					 "jpg",
					 "jpeg", "png", "gif", "bmp", "prn", "zip", 
//					 "arj", 
					 "rar",
					 "7z",
					 "pdf"
			};

			String filePath = "c:/Development/Workspaces/CCDEA_GITHUB/TestData/ps.xml";
			String contentMsgFilePath = "c:/Development/Workspaces/CCDEA_GITHUB/test/DocPut/DocReference/zip.xml";
			String baseContentPath = "c:/Development/Workspaces/CCDEA_GITHUB/test/test";
			String[] contentFilePathes = new String[formats.length];
			for (int i = 0; i < formats.length; i++) {
				contentFilePathes[i] = baseContentPath + '.' + formats[i];
			}
			

			MCDocInfoModifyPSReceiverMethod receiverMethod = new MCDocInfoModifyPSReceiverMethod();
			UnifiedResult receiverResult = UnifiedResult.getSuccessResultInstance();
			
			IDfSysObject messageSysObject = ExternalMessagePersistence.createEmptyMessageObjectFromFile(testSession,
					filePath);

			System.out.println(
					"Message object " + messageSysObject.getObjectId().getId() + " created from file " + filePath);

			Object messageXmlContent = receiverMethod.getXmlContent(messageSysObject, receiverResult);
			if (receiverResult.isSuccess()) {
				XmlContentValidator[] validators = receiverMethod
						.getXmlContentValidators(messageSysObject.getSession());
				if (receiverResult.isSuccess() && validators != null) {
					try {
						for (XmlContentValidator validator : validators) {
							validator.validate(messageXmlContent, receiverResult);
						}
					} catch (Exception ex) {
						receiverResult.setError(UnifiedResult.INTERNAL_ERROR_CODE,
								"Internal error during validation: " + ex.getMessage());

					}
				}
			}

			MessageObjectProcessor messageObjectProcessor = receiverMethod.getMessageObjectProcessor(messageXmlContent);

			if (receiverResult.isSuccess()) {
				messageObjectProcessor.readValuesFromXml(receiverResult);
			}

			messageObjectProcessor.storeMessageObjectOnValidation(messageSysObject);

			if (receiverResult.isSuccess()) {
				try {
					if (messageObjectProcessor.isCreateVerbSpecified()
							&& !messageObjectProcessor.isFirstValidMessage(messageSysObject)) {
						receiverResult.setError(UnifiedResult.WRONG_ORDER_CODE,
								"Received modification flag " + messageObjectProcessor.getModificationVerb()
										+ " but this is not first message for "
										+ messageObjectProcessor.getSourceKey());
					} else if (messageObjectProcessor.isUpdateVerbSpecified()
							&& messageObjectProcessor.isFirstValidMessage(messageSysObject)) {
						receiverResult.setError(UnifiedResult.WRONG_ORDER_CODE,
								"Received modification flag " + messageObjectProcessor.getModificationVerb()
										+ " but this is first message for " + messageObjectProcessor.getSourceKey());
					}
				} catch (DfException dfEx) {
					receiverResult.setError(UnifiedResult.INTERNAL_ERROR_CODE,
							"Internal error during check message order: " + dfEx.getMessage());

				}
			}

			receiverMethod.beforeGenearateResult(messageObjectProcessor, messageSysObject, receiverResult);

			receiverResult.generateResultDate();
			messageObjectProcessor.storeMessageObjectAfterValidation(messageSysObject, receiverResult);
			// receiverMethod.fillReplyObject((IDfWorkitemEx) iDfWorkitem,
			// receiverResult);
			if (receiverResult.isError()) {
				System.out.println("Return error " + receiverResult.getErrorCode() + " with description: "
						+ receiverResult.getErrorDescription());
			}

			PassportMessageJob docJob = new PassportMessageJob();
			String docId = docJob.process(testSession, messageSysObject.getObjectId());

			DocStateNotifyMessageJob notifyJob = new DocStateNotifyMessageJob();
			notifyJob.process(testSession);
			notifyJob.updateWaitingContents(testSession);
			
			IDfSysObject doc = (IDfSysObject) testSession.getObject(new DfId(docId));

			for (int i=0;i<contentFilePathes.length;i++) {
				BaseReceiverMethod contentReceiverMethod = new DocPutReceiverMethod();

				receiverResult = UnifiedResult.getSuccessResultInstance();
				messageSysObject = ExternalMessagePersistence.createEmptyMessageObjectFromFile(testSession,
						contentMsgFilePath);
				
				JAXBContext jc = JAXBContext.newInstance(DocPutType.class);
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				DocPutType docPutXmlObject = unmarshaller
						.unmarshal(new StreamSource(messageSysObject.getContent()), DocPutType.class).getValue();

				docPutXmlObject.getContent().getDocReference().get(0)
						.setFileReference(contentFilePathes[i]);
				docPutXmlObject.getContent().getDocReference().get(0).setFileFormat(formats[i]);
				docPutXmlObject.getOriginIdentification().setSourceId(doc.getString(ExternalMessagePersistence.ATTR_DOC_SOURCE_ID));
				docPutXmlObject.getOriginIdentification().setSourceSystem(doc.getString(ExternalMessagePersistence.ATTR_DOC_SOURCE_CODE));
				
				Marshaller marshaller = jc.createMarshaller();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				marshaller.marshal(new JAXBElement<DocPutType>(new QName("uri","local"), DocPutType.class, docPutXmlObject), os);
				messageSysObject.setContent(os);
				messageSysObject.save();
				

				System.out.println(
						"Message object " + messageSysObject.getObjectId().getId() + " created from file " + filePath);

				messageXmlContent = contentReceiverMethod.getXmlContent(messageSysObject, receiverResult);
				if (receiverResult.isSuccess()) {
					XmlContentValidator[] validators = contentReceiverMethod
							.getXmlContentValidators(messageSysObject.getSession());
					if (receiverResult.isSuccess() && validators != null) {
						try {
							for (XmlContentValidator validator : validators) {
								validator.validate(messageXmlContent, receiverResult);
							}
						} catch (Exception ex) {
							receiverResult.setError(UnifiedResult.INTERNAL_ERROR_CODE,
									"Internal error during validation: " + ex.getMessage());

						}
					}
				}

				messageObjectProcessor = contentReceiverMethod.getMessageObjectProcessor(messageXmlContent);

				if (receiverResult.isSuccess()) {
					messageObjectProcessor.readValuesFromXml(receiverResult);
				}

				messageObjectProcessor.storeMessageObjectOnValidation(messageSysObject);

				if (receiverResult.isSuccess()) {
					try {
						if (messageObjectProcessor.isCreateVerbSpecified()
								&& !messageObjectProcessor.isFirstValidMessage(messageSysObject)) {
							receiverResult.setError(UnifiedResult.WRONG_ORDER_CODE,
									"Received modification flag " + messageObjectProcessor.getModificationVerb()
											+ " but this is not first message for "
											+ messageObjectProcessor.getSourceKey());
						} else if (messageObjectProcessor.isUpdateVerbSpecified()
								&& messageObjectProcessor.isFirstValidMessage(messageSysObject)) {
							receiverResult.setError(UnifiedResult.WRONG_ORDER_CODE,
									"Received modification flag " + messageObjectProcessor.getModificationVerb()
											+ " but this is first message for "
											+ messageObjectProcessor.getSourceKey());
						}
					} catch (DfException dfEx) {
						receiverResult.setError(UnifiedResult.INTERNAL_ERROR_CODE,
								"Internal error during check message order: " + dfEx.getMessage());

					}
				}

				contentReceiverMethod.beforeGenearateResult(messageObjectProcessor, messageSysObject, receiverResult);

				receiverResult.generateResultDate();
				messageObjectProcessor.storeMessageObjectAfterValidation(messageSysObject, receiverResult);
				// receiverMethod.fillReplyObject((IDfWorkitemEx) iDfWorkitem,
				// receiverResult);
				if (receiverResult.isError()) {
					System.out.println("Return error " + receiverResult.getErrorCode() + " with description: "
							+ receiverResult.getErrorDescription());
				}
				
			}
			
			DocPutMesssageJob contentJob = new DocPutMesssageJob();
			
			while(true) {
				contentJob.process(testSession);
				notifyJob.process(testSession);
				notifyJob.updateWaitingContents(testSession);
				List<IDfId> messageIdList = ExternalMessagePersistence.getValidFirstMessageList(testSession,
						ExternalMessagePersistence.MESSAGE_TYPE_DOCPUT, new Date());
				if(messageIdList.size() == 0) {
					break;
				}
			}

		} finally {
			if (sessionManager != null && testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}
}
