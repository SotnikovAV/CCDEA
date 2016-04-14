package ru.rb.ccdea.jobs;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by dimaz on 14.04.2016.
 */
public class DocPutMesssageJobTest {

    IDfSession testSession = null;
    IDfClientX clientx = new DfClientX();
    IDfClient client = null;
    IDfSessionManager sessionManager = null;

    @Before
    public void before() throws DfException {
        client = clientx.getLocalClient();
        sessionManager = client.newSessionManager();

        IDfLoginInfo loginInfo = clientx.getLoginInfo();
        loginInfo.setUser("dmadmin");
        loginInfo.setPassword("Fkut,hf15");
        loginInfo.setDomain(null);

        sessionManager.setIdentity("ELAR", loginInfo);
        testSession = sessionManager.getSession("ELAR");
    }

    @Test
    public void test() throws DfException {

    }


    @After
    public void after() {
        if (testSession != null) {
            sessionManager.release(testSession);
        }
    }
}
