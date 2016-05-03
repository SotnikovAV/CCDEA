/**
 * 
 */
package ru.rb.ccdea.storage.persistence.fileutils;

import org.junit.Test;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.IDfLoginInfo;

import ru.rb.ccdea.storage.persistence.ContentPersistence;

/**
 * @author ER19391
 *
 */
public class TestContentLoader {
	
	@Test
	public void testProcessZipArchive() {
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

			String filepath = "C:/Development/Workspaces/CCDEA_GITHUB/test/test.zip";
			FileAccessProperties accessProperties = FileAccessProperties.parseUrl(filepath);
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

			ContentLoader.loadContentFile(jpgContentObj, filepath);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}
	
	@Test
	public void testProcessRarArchive() {
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

			String filepath = "C:/Development/Workspaces/CCDEA_GITHUB/test/test.rar";
			FileAccessProperties accessProperties = FileAccessProperties.parseUrl(filepath);
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

			ContentLoader.loadContentFile(jpgContentObj, filepath);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}

}
