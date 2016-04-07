package ru.rb.ccdea.storage.persistence.fileutils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;

import ru.rb.ccdea.adapters.mq.binding.docput.DocPutType;
import ru.rb.ccdea.storage.persistence.ContentPersistence;
import ru.rb.ccdea.storage.persistence.ContractPersistence;
import ru.rb.ccdea.storage.persistence.ctsutils.CTSRequestBuilder;
import ru.rb.ccdea.storage.services.impl.ContentService;

public class TestContentService {

	@Test
	public void test() {
		System.setProperty("dfc.data.dir", "C:/Development/temp");
		String[] formats = { 
//				"doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
//				"rtf", "odt", "xml", "tif", "tiff", "jpg", "jpeg", "png", "gif",
//				"bmp", "prn", 
				"zip"
//				, "arj", "rar", "7z" 
		};

		String documentId = null;

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

			for (String format : formats) {
				try {
					List<IDfId> documentIds = new ArrayList<IDfId>();

					IDfSysObject doc = (IDfSysObject) testSession.newObject(ContractPersistence.DOCUMENT_TYPE_NAME);
					doc.setObjectName("TST000001");
					doc.save();

					documentId = doc.getObjectId().getId();

					documentIds.add(doc.getObjectId());
					// try {
					IDfSysObject messageSysObject = (IDfSysObject) testSession.getObject(new DfId("085bbc6a81761627"));
					JAXBContext jc = JAXBContext.newInstance(DocPutType.class);
					Unmarshaller unmarshaller = jc.createUnmarshaller();
					DocPutType docPutXmlObject = unmarshaller
							.unmarshal(new StreamSource(messageSysObject.getContent()), DocPutType.class).getValue();

					docPutXmlObject.getContent().getDocReference().get(0)
							.setFileReference("c:/Development/Workspaces/CCDEA_GITHUB/test/test." + format);
					docPutXmlObject.getContent().getDocReference().get(0).setFileFormat(format);

					List<String> modifiedContentIdList = new ArrayList<String>();

					ContentService cs = new ContentService();
					cs.createContentFromMQType(testSession, docPutXmlObject.getContent(), "TST", "000001",
							modifiedContentIdList, documentIds);

					System.out.println(documentIds.toString());

					IDfSysObject existingObject = ContentPersistence.searchContentObjectByDocumentId(testSession,
							doc.getObjectId().getId());
					String status = ContentPersistence.checkContentAvaliable(testSession, existingObject);
					System.out.println(status);

					Assert.assertNotNull(existingObject.getContent());
					System.out.println("Content has been found");

					cs.updateContentFromMQType(testSession, docPutXmlObject.getContent(), "TST", "000001",
							modifiedContentIdList, doc.getObjectId(), existingObject.getObjectId());

					existingObject = ContentPersistence.searchContentObjectByDocumentId(testSession,
							doc.getObjectId().getId());
					status = ContentPersistence.checkContentAvaliable(testSession, existingObject);
					System.out.println(status);

					Assert.assertNotNull(existingObject.getContent());

					cs.appendContentFromMQType(testSession, docPutXmlObject.getContent(), "TST", "000001",
							modifiedContentIdList, doc.getObjectId(), existingObject.getObjectId());

					existingObject = ContentPersistence.searchContentObjectByDocumentId(testSession,
							doc.getObjectId().getId());
					status = ContentPersistence.checkContentAvaliable(testSession, existingObject);
					System.out.println(status);

					Assert.assertNotNull(existingObject.getContent());

					cs.createContentVersionFromMQType(testSession, docPutXmlObject.getContent(), "TST", "000001",
							modifiedContentIdList, doc.getObjectId(), existingObject.getObjectId());
					status = ContentPersistence.checkContentAvaliable(testSession, existingObject);
					System.out.println(status);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (testSession != null) {
				sessionManager.release(testSession);
			}
		}

	}

	@Test
	public void testContentTransform() {
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

			String filepath = "C:/Development/Workspaces/CCDEA_GITHUB/test/test";
			String[] formats = { ".doc", ".docx", ".xls", "_2010.xls", "_2013.xls", ".xlsx", ".ppt", ".pptx", ".txt",
					".rtf", ".odt", "_2010.odt", "_2013.odt", ".xml", ".tif", ".tiff", ".jpg", ".jpeg", ".png", ".gif",
					".bmp", ".prn" };

			IDfSysObject fullContentObj = null;
			for (String format : formats) {
				try {
					IDfId dfId = testXmlContentTransform(testSession, filepath + format);
					if (fullContentObj == null) {
						fullContentObj = (IDfSysObject) testSession.getObject(dfId);
					} else {
						IDfSysObject currentContentObj = (IDfSysObject) testSession.getObject(dfId);
						System.out.println("Merge pdf:");
						String responseId = CTSRequestBuilder.mergePdfRequest(testSession,
								fullContentObj.getObjectId().getId(), true, fullContentObj, currentContentObj, false);

						System.out.println("Result: " + responseId);
						String status = "";
						while (!"Completed".equals(status) && !"Failed".equals(status)) {
							IDfPersistentObject response = testSession.getObject(new DfId(responseId));
							status = response.getString("job_status");
							System.out.print(".");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

						IDfPersistentObject response = testSession.getObject(new DfId(responseId));
						System.out.println("Result: " + status + " ( " + response.getString("job_error_details")
								+ " ) -> " + responseId);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			fullContentObj = (IDfSysObject) testSession.getObjectByQualification("dm_sysobject where i_chronicle_id = '"
					+ fullContentObj.getChronicleId() + "' order by r_modify_date desc");
			System.out.println("Full content object: " + fullContentObj.getObjectId());

			ByteArrayInputStream is = fullContentObj.getContent();
			OutputStream os = null;
			try {
				String outputFilePath = filepath + "full.pdf";
				os = new FileOutputStream(outputFilePath);
				int b;
				while ((b = is.read()) != -1) {
					os.write(b);
				}
				System.out.println(outputFilePath);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				if (os != null) {
					try {
						os.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}

	private IDfId testXmlContentTransform(IDfSession testSession, String inputFilePath) throws DfException {

		FileAccessProperties accessProperties = FileAccessProperties.parseUrl(inputFilePath);
		IDfSysObject jpgContentObj = null;
		boolean isTransAlreadyActive = testSession.isTransactionActive();
		try {
			if (!isTransAlreadyActive) {
				testSession.beginTrans();
			}
			jpgContentObj = ContentPersistence.createContentObject(testSession,
					"TEST" + accessProperties.getFileFormat(), "000001", true);

			System.out.println(jpgContentObj.getObjectName() + " -> " + jpgContentObj.getObjectId());
			if (!isTransAlreadyActive) {
				testSession.commitTrans();
			}
		} finally {
			if (!isTransAlreadyActive && testSession.isTransactionActive()) {
				testSession.abortTrans();
			}
		}

		ContentLoader.loadContentFile(jpgContentObj, inputFilePath);

		IDfId pdfId = null;
		IDfSysObject pdfContentObj = null;
		isTransAlreadyActive = testSession.isTransactionActive();
		try {
			if (!isTransAlreadyActive) {
				testSession.beginTrans();
			}
			pdfContentObj = ContentPersistence.createContentObject(testSession,
					accessProperties.getFileFormat() + "2PDF", "000001", false);

			pdfId = pdfContentObj.getObjectId();
			System.out.println(pdfContentObj.getObjectName() + " -> " + pdfId);
			if (!isTransAlreadyActive) {
				testSession.commitTrans();
			}
		} finally {
			if (!isTransAlreadyActive && testSession.isTransactionActive()) {
				testSession.abortTrans();
			}
		}

		System.out.println("Transform " + accessProperties.getFileFormat() + " to pdf:");
		String responseId = CTSRequestBuilder.convertToPdfRequest(testSession, pdfContentObj.getObjectId().getId(),
				false, jpgContentObj, false);
		System.out.println("Result: " + responseId);
		String status = CTSRequestBuilder.waitForJobComplete(testSession, responseId);

		IDfPersistentObject response = testSession.getObject(new DfId(responseId));
		System.out
				.println("Result: " + status + " ( " + response.getString("job_error_details") + " ) -> " + responseId);

		if ("Completed".equals(status)) {

			pdfContentObj = (IDfSysObject) testSession.getObject(pdfId);

			ByteArrayInputStream is = pdfContentObj.getContent();
			OutputStream os = null;
			try {
				String outputFilePath = accessProperties.getUrl().substring(0,
						accessProperties.getUrl().lastIndexOf('/')) + '/' + accessProperties.getFileFormat()
						+ "2PDF.pdf";
				os = new FileOutputStream(outputFilePath);
				int b;
				while ((b = is.read()) != -1) {
					os.write(b);
				}
				System.out.println(outputFilePath);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				if (os != null) {
					try {
						os.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		return pdfId;
	}

	@Test
	public void downloadPdf() {
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

			IDfSysObject pdfContentObj = (IDfSysObject) testSession.getObject(new DfId("095bbc6a8174ac91"));

			ByteArrayInputStream is = pdfContentObj.getContent();
			OutputStream os = null;
			try {
				os = new FileOutputStream("C:/Development/Workspaces/CCDEA_GIT/test/FULLPDF.pdf");
				int b;
				while ((b = is.read()) != -1) {
					os.write(b);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				if (os != null) {
					try {
						os.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}

	@Test
	public void testIO() {
		InputStream is = null;
		OutputStream os = null;
		try {

			is = new FileInputStream("C:/Development/Workspaces/CCDEA_GIT/test/test.JPG");
			os = new FileOutputStream("C:/Development/Workspaces/CCDEA_GIT/test/test1.JPG");
			int b;
			while ((b = is.read()) != -1) {
				os.write(b);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
