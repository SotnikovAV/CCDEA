package ru.rb.ccdea.storage.jobs;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

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

import ru.rb.ccdea.adapters.mq.binding.docput.DocPutType;
import ru.rb.ccdea.storage.persistence.ContentPersistence;
import ru.rb.ccdea.storage.persistence.ExternalMessagePersistence;
import ru.rb.ccdea.storage.persistence.fileutils.ContentLoader;

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

			DfLogger.debug(this, "Start MessageID: {0}", new String[] { messageId.getId() }, null);

			if (!isTransAlreadyActive) {
				dfSession.beginTrans();
			}

			IDfSysObject messageObject = (IDfSysObject) dfSession.getObject(messageId);
			
			IDfSysObject originalContentObject = ExternalMessagePersistence
					.getOriginalMessageContentObject(messageObject);
			if (originalContentObject == null
					|| (originalContentObject != null && originalContentObject.getContentSize() > 0
							&& ContentPersistence.isDocumentRelationForContentExists(dfSession,
									originalContentObject.getObjectId().getId()))) {
				ExternalMessagePersistence.notifyExternalSystemAboutContentProcessing(messageObject);
				deleteContent(messageObject);
			} else if (originalContentObject != null && originalContentObject.getContentSize() == 0) {
				ExternalMessagePersistence.setConvertationError(messageObject);
			} else {
				DfLogger.debug(this, "Skip MessageID: {0} because content not ready or empty",
						new String[] { messageId.getId() }, null);
			}

			if (!isTransAlreadyActive) {
				dfSession.commitTrans();
			}

			DfLogger.debug(this, "Finish MessageID: {0}", new String[] { messageId.getId() }, null);
		} catch (DfException dfEx) {
			DfLogger.error(this, "Error MessageID: {0}", new String[] { messageId.getId() }, dfEx);
			// throw dfEx;
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

    private void deleteContent(IDfSysObject messageObject) throws JAXBException, DfException {
        JAXBContext jc = JAXBContext.newInstance(DocPutType.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        DocPutType docPutXmlObject = unmarshaller.unmarshal(new StreamSource(messageObject.getContent()), DocPutType.class).getValue();

        ContentLoader.deleteContentFile(messageObject.getSession(), messageObject, docPutXmlObject.getContent());
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
            loginInfo.setPassword("dmadmin");
            loginInfo.setDomain(null);

            sessionManager.setIdentity("UCB", loginInfo);
            testSession = sessionManager.getSession("UCB");
            
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
