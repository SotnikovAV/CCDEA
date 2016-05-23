package ru.rb.ccdea.storage.jobs;

import java.util.List;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;

import ru.rb.ccdea.storage.persistence.ContentPersistence;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.ctsutils.CTSRequestBuilder;

public class DocStateNotifyMessageJob extends AbstractJob{

    @Override
    public int execute() throws Exception {
        process(dfSession);
        updateWaitingContents(dfSession);
        return 0;
    }
    
    public void process(IDfSession dfSession) throws DfException {
    	List<IDfId> messageIdList = ExternalMessagePersistence.getLoadedMessageList(dfSession);
        for (IDfId messageId : messageIdList) {
        	process(dfSession, messageId);
        }
    }
    
    public void process(IDfSession dfSession, IDfId messageId) throws DfException {
    	boolean isTransAlreadyActive = dfSession.isTransactionActive();
        try {

            DfLogger.debug(this, "Start MessageID: {0}", new String[]{messageId.getId()}, null);

            if (!isTransAlreadyActive) {
                dfSession.beginTrans();
            }

            IDfSysObject messageObject = (IDfSysObject)dfSession.getObject(messageId);
            String ctsJobResult = null; 
//            ExternalMessagePersistence.getContentProcessingByCTSIfFinished(messageObject);
//			if (ctsJobResult == null) {
				IDfSysObject originalContentObject = ExternalMessagePersistence
						.getOriginalMessageContentObject(messageObject);
				if (originalContentObject != null && originalContentObject.getContentSize() > 0
//						&& "pdf".equalsIgnoreCase(originalContentObject.getContentType())
						) {
					ctsJobResult = CTSRequestBuilder.RESPONSE_STATUS_COMPLITED;
				} else {
					ctsJobResult = CTSRequestBuilder.RESPONSE_STATUS_FAILED;
				}
//			}
            if (CTSRequestBuilder.RESPONSE_STATUS_COMPLITED.equalsIgnoreCase(ctsJobResult)) {
//                IDfSysObject originalContentObject = ExternalMessagePersistence.getOriginalMessageContentObject(messageObject);
                IDfSysObject nextContent = ContentPersistence.getNextContentSysObject(originalContentObject, true);
//                if (nextContent != null) {
//                    DfLogger.info(this, "Process MessageID: {0} - destroy completed content part {1}", new String[] {messageId.getId(), nextContent.getObjectId().getId()}, null);
//                    nextContent.destroy();
//                }
//                nextContent = ContentPersistence.getNextContentSysObject(originalContentObject, true);
//                if (nextContent != null) {
//                    IDfSysObject documentContentObject = ExternalMessagePersistence.getDocumentContentObject(messageObject);
//                    String transformJobId = CTSRequestBuilder.mergePdfRequest(dfSession, documentContentObject.getObjectId().getId(), false, documentContentObject, nextContent);
//                    ExternalMessagePersistence.continueNextContentTransform(messageObject, transformJobId);
//                }
//                else {
                    ExternalMessagePersistence.notifyExternalSystemAboutContentProcessing(messageObject);
//                }
            }
            else if (CTSRequestBuilder.RESPONSE_STATUS_FAILED.equalsIgnoreCase(ctsJobResult)) {
                ExternalMessagePersistence.setConvertationError(messageObject);
            }
            else {
                DfLogger.debug(this, "Skip MessageID: {0} because content not ready or empty", new String[] {messageId.getId()}, null);
            }

            if (!isTransAlreadyActive) {
                dfSession.commitTrans();
            }

            DfLogger.debug(this, "Finish MessageID: {0}", new String[] {messageId.getId()}, null);
        }
        catch (DfException dfEx) {
            DfLogger.error(this, "Error MessageID: {0}", new String[] {messageId.getId()}, dfEx);
            //throw dfEx;
        }
        catch (Exception ex) {
            DfLogger.error(this, "Error MessageID: {0}", new String[] {messageId.getId()}, ex);
            //throw new DfException(ex);
        }
        finally {
            if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
                dfSession.abortTrans();
            }
        }
    }
    
    public void updateWaitingContents(IDfSession dfSession) throws DfException {
    	List<IDfId> docMessageIds = ExternalMessagePersistence.getWaitingForDocMessages(dfSession);
		for (IDfId messageId : docMessageIds) {
			String messageIdStr = messageId.getId();
			boolean isTransAlreadyActive = dfSession.isTransactionActive();
			try {
				DfLogger.debug(this, "Start MessageID: {0}", new String[] { messageIdStr }, null);
				if (!isTransAlreadyActive) {
					dfSession.beginTrans();
				}
				IDfSysObject docMessage = (IDfSysObject) dfSession.getObject(messageId);
				ExternalMessagePersistence.updateWaitingContents(docMessage);
				if (!isTransAlreadyActive) {
					dfSession.commitTrans();
				}

				DfLogger.debug(this, "Finish MessageID: {0}", new String[] { messageIdStr }, null);
			} catch (DfException dfEx) {
				DfLogger.error(this, "Error MessageID: {0}", new String[] { messageIdStr }, dfEx);
				// throw dfEx;
			} catch (Exception ex) {
				DfLogger.error(this, "Error MessageID: {0}", new String[] { messageIdStr }, ex);
				// throw new DfException(ex);
			} finally {
				if (!isTransAlreadyActive && dfSession.isTransactionActive()) {
					dfSession.abortTrans();
				}
			}
		}
    } 
    
    public static final void main(String [] args) {
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
            
            DocStateNotifyMessageJob job = new DocStateNotifyMessageJob();

			String messageIdStr = null;
			if (args != null && args.length > 0) {
				messageIdStr = args[0];
			}

			IDfId messageId = null;
			if (messageIdStr != null) {
				messageId = new DfId(messageIdStr);
			}
			
			if (messageId != null && !messageId.isNull() && messageId.isObjectId()) {
				job.process(testSession, messageId);
			} else {
				job.process(testSession);
	            job.updateWaitingContents(testSession);
			}
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (testSession != null) {
                sessionManager.release(testSession);
            }
        }
    }

}
