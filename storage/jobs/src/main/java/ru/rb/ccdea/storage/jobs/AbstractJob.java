package ru.rb.ccdea.storage.jobs;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.fc.methodserver.*;
import java.io.PrintWriter;
import java.util.Map;

public abstract class AbstractJob implements IDfMethod, IDfModule{

    protected CustomJobArguments jobArguments;
    protected IDfTime startDate;
    protected IDfSession dfSession;
    protected IDfSessionManager sessionManager;
    protected IDfClientX clientx;
    protected IDfClient client;

    @Override
    public int execute(Map map, PrintWriter printWriter) throws Exception {

        DfLogger.info(this, "Initialize job class", null, null);

        setup(map, printWriter);
        try {
            DfLogger.info(this, "Start job execution", null, null);

            int retCode = execute();

            DfLogger.info(this, "Finish job execution", null, null);

            printJobStatusReport(retCode);
            return retCode;
        } catch (Exception e) {
            DfLogger.error(this, "Job terminated", null, e);
            printException(e, true);
            throw e;
        } finally {
            if (dfSession != null)
                sessionManager.release(dfSession);
        }
    }

    private void printException(Throwable cause, boolean isRoot) {
        if (isRoot) {
            System.out.println("Error ocured in " + this.getClass().getName() + ": " + cause.getMessage());
        }
        else {
            System.out.println("Cause: " + cause.getMessage());
        }
        for (StackTraceElement element : cause.getStackTrace()) {
            System.out.println("    " + element.toString());
        }
        if (cause.getCause() != null && !cause.equals(cause.getCause())) {
            printException(cause.getCause(), false);
        }
    }

    public abstract int execute() throws Exception;

    public void setup(Map args, PrintWriter writer) throws DfMethodArgumentException, DfException, Exception {
        startDate = new DfTime();
        jobArguments = new CustomJobArguments(new DfMethodArgumentManager(args));
        String username = jobArguments.getUserName();
        String password = jobArguments.getString("password");
        String domain = jobArguments.getString("domain");
        String docbase = jobArguments.getDocbaseName();
        setupSessionManager(username, password, domain, docbase);
    }

    private void setupSessionManager(String username, String password, String domain, String docbase) throws DfServiceException, DfException {
        //DfLogger.debug(this, String.format("setupSessionManager-> username[%s] password[%s] domain[%s] docbase[%s]", username, password, domain, docbase), null, null);
        clientx = new DfClientX();
        client = clientx.getLocalClient();
        sessionManager = client.newSessionManager();
        IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
        loginInfoObj.setUser(username);
        if (password != null && !password.equals(""))
            loginInfoObj.setPassword(password);
        loginInfoObj.setPassword(password);
        loginInfoObj.setDomain(domain);
        sessionManager.setIdentity(docbase, loginInfoObj);
        dfSession = sessionManager.getSession(docbase);
    }

    private void printJobStatusReport(int retCode) throws Exception {
        String jobStatus = null;
        IDfTime end_date = new DfTime();
        long ms_duration = (end_date.getDate().getTime() - startDate.getDate().getTime());
        if (retCode == 0)
            jobStatus = "Job completed at " + end_date.asString("dd.mm.yyyy hh:mi:ss") + ". Total duration: " + ms_duration + " ms.";
        else if (retCode > 0)
            jobStatus = "Job completed with Warnings at " + end_date.asString("dd.mm.yyyy hh:mi:ss") + ". Total duration: " + ms_duration + " ms.";
        else
            jobStatus = "Job completed with Errors at " + end_date.asString("dd.mm.yyyy hh:mi:ss") + ". Total duration: " + ms_duration + " ms. Check job report for details.";
        updateJobStatus(jobStatus, jobArguments.getJobId());
    }

    public void updateJobStatus(String sJobStatus, IDfId jobId) throws Exception {
        if (dfSession == null) {
            DfLogger.error(this, "setJobStatus: (session==null)", null, null);
            throw new NullPointerException("setJobStatus: (session==null)");
        }
        try {
            IDfPersistentObject job = dfSession.getObject(jobId);
            if (job == null)
                throw new DfException("Failed to retrieve dm_job object from id '" + jobId.getId() + "'.");
            job.setString("a_current_status", sJobStatus);
            job.save();
        } catch (Exception e) {
            throw e;
        }
    }

    public class CustomJobArguments extends DfStandardJobArguments {

        protected IDfMethodArgumentManager methodArgumentManager;

        public CustomJobArguments(IDfMethodArgumentManager manager) throws DfMethodArgumentException {
            super(manager);
            methodArgumentManager=manager;
        }

        public String getString(String paramName) throws DfMethodArgumentException {
            return methodArgumentManager.getString(paramName);
        }

        public int getInt(String paramName) throws DfMethodArgumentException {
            return methodArgumentManager.getInt(paramName).intValue();
        }
    }
}
