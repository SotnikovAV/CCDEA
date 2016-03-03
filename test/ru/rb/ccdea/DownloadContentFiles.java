/**
 * 
 */
package ru.rb.ccdea;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfEnumeration;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;

/**
 * @author ER19391
 *
 */
public class DownloadContentFiles {

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

			IDfEnumeration contentObjList = testSession.getObjectsByQuery(
					"select r_object_id, r_object_type, r_aspect_name, i_vstamp, i_is_reference, i_is_replica from dm_sysobject where r_object_type in ('ccdea_content','ccdea_doc_content')",
					null);
			int counter = 0;
			while (contentObjList.hasMoreElements()) {
				System.out.println(counter++);
				try {
					IDfSysObject contentObj = (IDfSysObject) contentObjList.nextElement();

					System.out.println(contentObj.getObjectId() + " : "
							+ contentObj.getModifyDate().asString("dd.MM.yyyy HH:mi:ss"));
					InputStream is = contentObj.getContent();

					OutputStream out = null;
					try {
						String folder = "C:/Development/temp/ccdea/content/" + contentObj.getObjectId();
						File folderFile = new File(folder);
						if (!folderFile.exists()) {
							folderFile.mkdirs();
						}
						String filename = folder + '/' + (contentObj.getObjectId());
						out = new FileOutputStream(filename);

						IOUtils.copy(is, out);

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

					IDfEnumeration contentPartObjList = testSession.getObjectsByQuery(
						  " select r_object_id, r_object_type, r_aspect_name, i_vstamp, i_is_reference, i_is_replica from ccdea_content_part where id_content='"
									+ contentObj.getObjectId() + "'" 
						+ " union "
						+ " select r_object_id, r_object_type, r_aspect_name, i_vstamp, i_is_reference, i_is_replica from ccdea_doc_content_part where id_content='"
									+ contentObj.getObjectId() + "'",
							null);
					while (contentPartObjList.hasMoreElements()) {
						System.out.println(counter++);
						try {
							IDfSysObject contentPartObj = (IDfSysObject) contentObjList.nextElement();

							System.out.println(contentPartObj.getObjectId() + " : "
									+ contentObj.getModifyDate().asString("dd.MM.yyyy HH:mi:ss"));
							is = contentPartObj.getContent();

							out = null;
							try {
								String folder = "C:/Development/temp/ccdea/content/" + contentObj.getObjectId();
								File folderFile = new File(folder);
								if (!folderFile.exists()) {
									folderFile.mkdirs();
								}
								String filename = folder + '/'
										+ (contentPartObj.getObjectName() == null
												|| contentPartObj.getObjectName().trim().length() == 0
														? contentPartObj.getObjectId()
														: contentPartObj.getObjectName());
								out = new FileOutputStream(filename);

								IOUtils.copy(is, out);

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
						} catch (DfException dfEx) {
							throw dfEx;
						} catch (Exception ex) {
							throw new DfException(ex);
						}
					}
				} catch (DfException dfEx) {
					throw dfEx;
				} catch (Exception ex) {
					throw new DfException(ex);
				}

			}
		} catch (Exception ex) {
			System.out.println(ex);
		} finally {
			if (testSession != null) {
				sessionManager.release(testSession);
			}
		}
	}
}
