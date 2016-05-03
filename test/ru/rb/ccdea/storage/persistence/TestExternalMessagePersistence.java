/**
 * 
 */
package ru.rb.ccdea.storage.persistence;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;

/**
 * @author SotnikovAV
 *
 */
public class TestExternalMessagePersistence {

	@Test
	public void testGetValidFirstMessageList() {
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

			String messageType = "DocPut";
			List<IDfId> messageIds = ExternalMessagePersistence.getValidFirstMessageList(testSession, messageType, new Date());
			
//			Assert.assertNotNull(dossier);
			System.out.println("Message ids = " + messageIds.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}
	
}
