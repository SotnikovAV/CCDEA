package ru.rb.ccdea;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfEnumeration;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;

public class DownloadMessages {

	@Test
	public void test() {
		
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

            IDfEnumeration messageIdList = testSession.getObjectsByQuery("select r_object_id, r_object_type, r_aspect_name, i_vstamp, i_is_reference, i_is_replica from ccdea_external_message", null);
            int counter = 0;
            while (messageIdList.hasMoreElements()) {
                System.out.println(counter++);
                try {
                    IDfSysObject messageSysObject = (IDfSysObject)messageIdList.nextElement();
                    
                    System.out.println(messageSysObject.getObjectId() + " : " + messageSysObject.getModifyDate().asString("dd.MM.yyyy HH:mi:ss"));
                    InputStream is = messageSysObject.getContent();
                    
					OutputStream out = null;
					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder = factory.newDocumentBuilder();
						Document document = builder.parse(new InputSource(is));
						String type = document.getDocumentElement().getNodeName();
						int delim = type.indexOf(':');
						if(delim > -1) {
							type = type.substring(delim + 1);
						}
						String folder = "C:/Development/temp/ccdea/" + type;
						File folderFile = new File(folder);
						if(!folderFile.exists()) {
							folderFile.mkdirs();
						}
						String filename =  folder + '/' + messageSysObject.getObjectId() + ".xml";
						out = new FileOutputStream(filename);
						TransformerFactory tFactory = TransformerFactory.newInstance();
						Transformer transformer = tFactory.newTransformer();

						DOMSource source = new DOMSource(document);
						StreamResult result = new StreamResult(out);
						transformer.transform(source, result);
						
						System.out.println(": " + filename);
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						if (out != null) {
							out.close();
						}
						if (is != null) {
							is.close();
						}
					}

                    

                }
                catch (DfException dfEx) {
                    throw dfEx;
                }
                catch (Exception ex) {
                    throw new DfException(ex);
                }
                
            }
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
        finally {
            if (testSession != null) {
                sessionManager.release(testSession);
            }
        }
	}

}
