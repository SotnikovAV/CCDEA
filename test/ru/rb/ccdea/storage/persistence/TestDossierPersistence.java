package ru.rb.ccdea.storage.persistence;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfLoginInfo;

import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;

public class TestDossierPersistence {

	@Test
	public void testGetDossierByKeyDetails() {
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

			DossierKeyDetails keyDetails = new DossierKeyDetails();
			keyDetails.setBranchCode("0000");
			keyDetails.setCustomerNumber("00640002");
			keyDetails.setContractNumber("08.10.2014");
			keyDetails.setContractDate(new SimpleDateFormat("dd.MM.yyyy").parse("08.10.2014"));

			IDfPersistentObject dossier = DossierPersistence.getDossierByKeyDetails(testSession, keyDetails);
			Assert.assertNotNull(dossier);
			System.out.println("Dossier id = " + dossier);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}

	@Test
	public void testGetDossierKeyDetails() {
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

			IDfPersistentObject dossier = testSession.getObject(new DfId("005bbc6a80713e7c"));
			Assert.assertNotNull(dossier);
			System.out.println("Dossier id = " + dossier);

			DossierKeyDetails keyDetails = DossierPersistence.getKeyDetails(dossier);
			Assert.assertNotNull(keyDetails);
			System.out.println("keyDetails = " + keyDetails);

			IDfPersistentObject foundDossier = DossierPersistence.getDossierByKeyDetails(testSession, keyDetails);
			Assert.assertNotNull(foundDossier);
			System.out.println("Dossier id = " + foundDossier);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}

	@Test
	public void testSearchFirstFreeNumber() {
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

			boolean isTransAlreadyActive = testSession.isTransactionActive();
			try {
				if (!isTransAlreadyActive) {
					testSession.beginTrans();
				}

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());
				int year = calendar.get(Calendar.YEAR);
				
				String prefix = DossierPersistence.DOSSIER_TYPE_CONTRACT_PREFIX;

				int number1 = DossierPersistence.searchFirstFreeNumber(testSession, year, prefix);
				System.out.println("Сгенерированный номер 1: " + number1);
				int number2 = DossierPersistence.searchFirstFreeNumber(testSession, year, prefix);
				System.out.println("Сгенерированный номер 2: " + number2);
				Assert.assertNotEquals(number1, number2);
				if (!isTransAlreadyActive) {
					testSession.commitTrans();
				}
			} catch (DfException dfEx) {
				throw dfEx;
			} catch (Exception ex) {
				throw new DfException(ex);
			} finally {
				if (!isTransAlreadyActive && testSession.isTransactionActive()) {
					testSession.abortTrans();
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

}
