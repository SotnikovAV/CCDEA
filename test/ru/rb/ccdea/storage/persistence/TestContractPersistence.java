package ru.rb.ccdea.storage.persistence;

import static org.junit.Assert.*;

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
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfLoginInfo;

import ru.rb.ccdea.adapters.mq.binding.contract.MCDocInfoModifyContractType;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;

public class TestContractPersistence {

	@Test
	public void testGetKeyDetailsFromMQ() {
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
            
            IDfSysObject messageSysObject = (IDfSysObject) testSession.getObject(new DfId("095bbc6a816e86c6"));
            Assert.assertNotNull(messageSysObject);
            System.out.println("Dossier id = " + messageSysObject);
            
            JAXBContext jc = JAXBContext.newInstance(MCDocInfoModifyContractType.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            MCDocInfoModifyContractType contractXmlObject = unmarshaller.unmarshal(new StreamSource(messageSysObject.getContent()), MCDocInfoModifyContractType.class).getValue();
            
            DossierKeyDetails keyDetails = ContractPersistence.getKeyDetailsFromMQ(contractXmlObject);
            Assert.assertNotNull(keyDetails);
            System.out.println("keyDetails = " + keyDetails);
            
            IDfPersistentObject foundDossier = DossierPersistence.getDossierByKeyDetails(testSession, keyDetails);
            Assert.assertNotNull(foundDossier);
            System.out.println("Dossier id = " + foundDossier);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (testSession != null) {
                sessionManager.release(testSession);
            }
        }
	}
	
	@Test
	public void testSearchContentObjectByDocumentId() {
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
            
            IDfSysObject sysObject = (IDfSysObject) ContentPersistence.searchContentObjectByDocumentId(testSession, "095bbc6a816e86c6");
            Assert.assertNotNull(sysObject);
            System.out.println(" id = " + sysObject);
            
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (testSession != null) {
                sessionManager.release(testSession);
            }
        }
	}

}
