package ru.rb.ccdea.storage.persistence;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.rb.ccdea.storage.jobs.DocPutMesssageJob;

import java.util.Calendar;

import static ru.rb.ccdea.storage.persistence.ExternalMessagePersistence.*;

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

        IDfSysObject ccdea_external_message = (IDfSysObject) testSession.newObject("ccdea_external_message");
        ccdea_external_message.setObjectName("ccdea_external_message test");
        ccdea_external_message.setInt(ATTR_CURRENT_STATE, MESSAGE_STATE_VALIDATION_PASSED);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        IDfTime modificationTime = new DfTime(calendar.getTime());
        ccdea_external_message.setTime(ATTR_MODIFICATION_TIME, modificationTime);
        ccdea_external_message.setString(ATTR_MESSAGE_TYPE, MESSAGE_TYPE_DOCPUT);
        ccdea_external_message.save();

        System.out.println(String.format("new message created with %s id", ccdea_external_message.getObjectId()));

        DocPutMesssageJob job = new DocPutMesssageJob();
        job.process(testSession);

        Assert.assertTrue(String.format("test object %s should have n_current_state = 5", ccdea_external_message.getObjectId()), ccdea_external_message.getInt(ATTR_CURRENT_STATE) == MESSAGE_STATE_LOADED);
    }


    @After
    public void after() {
        if (testSession != null) {
            sessionManager.release(testSession);
        }
    }
}
