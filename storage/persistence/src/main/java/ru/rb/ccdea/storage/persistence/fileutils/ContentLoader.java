package ru.rb.ccdea.storage.persistence.fileutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import com.documentum.fc.client.*;
import com.documentum.fc.common.DfUtil;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.jcraft.jsch.JSchException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import ru.rb.ccdea.adapters.mq.binding.docput.ContentType;
import ru.rb.ccdea.adapters.mq.binding.docput.ContentType.DocReference;
import ru.rb.ccdea.adapters.mq.binding.docput.ContentType.DocScan;
import ru.rb.ccdea.storage.persistence.ContentLoaderException;
import ru.rb.ccdea.storage.persistence.ContentPersistence;

public class ContentLoader {

    protected static final String SYSPROP_SFTP_USER_NAME = "ccdea.sftp.user.name";
    protected static final String SYSPROP_SFTP_KEYFILE_PATH = "ccdea.sftp.keyfile.path";
    protected static final String SYSPROP_SFTP_NEED_DELETE = "ccdea.sftp.need.delete";

    protected static final String SYSPROP_SMB_USER_DOMAIN = "ccdea.smb.user.domain";
    protected static final String SYSPROP_SMB_USER_NAME = "ccdea.smb.user.name";
    protected static final String SYSPROP_SMB_USER_PASSWORD = "ccdea.smb.user.password";
    protected static final String SYSPROP_SMB_NEED_DELETE = "ccdea.smb.need.delete";

    protected static final Set<String> SUPPORTED_FORMAT = new HashSet<String>();

    static {
        SUPPORTED_FORMAT.add("pdf");
        SUPPORTED_FORMAT.add("jpeg");
        SUPPORTED_FORMAT.add("tiff");
        SUPPORTED_FORMAT.add("jpg");
        SUPPORTED_FORMAT.add("tif");
        SUPPORTED_FORMAT.add("doc");
        SUPPORTED_FORMAT.add("docx");
        SUPPORTED_FORMAT.add("xls");
        SUPPORTED_FORMAT.add("xlsx");
        SUPPORTED_FORMAT.add("ppt");
        SUPPORTED_FORMAT.add("pptx");
        SUPPORTED_FORMAT.add("txt");
        SUPPORTED_FORMAT.add("rtf");
        SUPPORTED_FORMAT.add("odt");
        SUPPORTED_FORMAT.add("xml");
        SUPPORTED_FORMAT.add("png");
        SUPPORTED_FORMAT.add("gif");
        SUPPORTED_FORMAT.add("bmp");
        SUPPORTED_FORMAT.add("prn");
    }
    
    public static final void loadContentFile(IDfSysObject contentSysObject, String filepath) throws DfException {
    	InputStream is = null;
    	try {
    		FileAccessProperties accessProperties = FileAccessProperties.parseUrl(filepath);
    		String fileFormat = getFileFormat(accessProperties.getFileFormat());
    		
    		is = new FileInputStream(filepath);
    		ByteArrayOutputStream os = new ByteArrayOutputStream();
			int b;
			while ((b = is.read()) != -1) {
			    os.write(b);
			}
			contentSysObject.setContentType(fileFormat);
			contentSysObject.setContent(os); 
			contentSysObject.save();
    	} catch (IOException e) {
			throw new DfException("Ошибка",e);
		} finally {
    		if(is != null) {
    			try {
					is.close();
				} catch (IOException ex) {
					DfLogger.warn(contentSysObject, "Ошибка при закрытии потока", null, ex);
				}
    		}
    	}
    }
    
    public static final void loadContentFile(String filepath, OutputStream os) throws DfException {
    	InputStream is = null;
    	try {
    		FileAccessProperties accessProperties = FileAccessProperties.parseUrl(filepath);
    		
    		is = new FileInputStream(filepath);
			int b;
			while ((b = is.read()) != -1) {
			    os.write(b);
			}
    	} catch (IOException e) {
			throw new ContentLoaderException("Ошибка",e);
		} finally {
    		if(is != null) {
    			try {
					is.close();
				} catch (IOException ex) {
					DfLogger.warn(null, "Ошибка при закрытии потока", null, ex);
				}
    		}
    	}
    }
    
    public static void loadContent(DocScan docScan, OutputStream out) throws DfException {
    	try {
    		out.write(docScan.getFileScan());
        }
        catch (IOException e) {
            throw new DfException("Cant create buffer with fileScan", e);
        }
    }
    
    public static void loadContent(IDfSession dfSession, DocReference docReference, OutputStream out) throws DfException {
    	String fileReference = null;
        if (docReference.getFileReference() != null && !docReference.getFileReference().trim().isEmpty()) {
            fileReference = docReference.getFileReference();
        } else if (docReference.getFileReferenceSMB() != null && !docReference.getFileReferenceSMB().trim().isEmpty()) {
            fileReference = docReference.getFileReferenceSMB();
        } else {
            throw new CantFindFileReferenceException("Cant find file reference to load");
        }
        FileAccessProperties accessProperties = FileAccessProperties.parseUrl(fileReference);
        if (JschSFTP.SFTP.equalsIgnoreCase(accessProperties.getProtocol())) {
        	loadContentBySftp(dfSession, accessProperties, out);
        } else if(accessProperties.getUrl().startsWith("\\\\")) {
        	loadContentBySmb(dfSession, accessProperties, out);
        } else {
        	loadContentFile(accessProperties.getUrl(), out);
        }
    }

    public static void loadContentBySmb(IDfSession dfSession, FileAccessProperties accessProperties, OutputStream out) throws DfException {
    	try {
            SmbFile smbFile = getSmbFile(dfSession, accessProperties);
            SmbFileInputStream smbFileOS = null;

            try {
                smbFileOS = new SmbFileInputStream(smbFile);
                byte[] temp = new byte[1024];
                int read;
                while ((read = smbFileOS.read(temp)) >= 0) {
                    out.write(temp, 0, read);
                }
            } finally {
                if (smbFileOS != null) {
                    smbFileOS.close();
                }
            }
        }
        catch (Exception ex) {
            throw new ContentLoaderException("Cant download file by reference: " + accessProperties.getUrl(), ex);
        }
	}

	public static void deleteContentBySmb(IDfSession dfSession, FileAccessProperties accessProperties) throws DfException {
    	try {
			boolean deleteFile = "true".equalsIgnoreCase(getSyspropValue(dfSession, SYSPROP_SMB_NEED_DELETE));
			SmbFile smbFile = getSmbFile(dfSession, accessProperties);
            if (deleteFile) {
				if (smbFile != null) {
					try {
						DfLogger.info(dfSession, "Удалён smb файл " + smbFile.getCanonicalPath(), null, null);
						smbFile.delete();
					} catch (Exception ex) {
						DfLogger.warn(dfSession, "Не удалось удалить файл " + smbFile.getCanonicalPath(), null, ex);
					}
				}
            }
        }
        catch (Exception ex) {
            throw new ContentLoaderException("Cant download file by reference: " + accessProperties.getUrl(), ex);
        }
	}

	private static SmbFile getSmbFile(IDfSession dfSession, FileAccessProperties accessProperties) throws DfException, MalformedURLException {
		String domain = "";
		try {
            domain = getSyspropValue(dfSession, SYSPROP_SMB_USER_DOMAIN);
        } catch (DfException ex) {
            DfLogger.warn(dfSession, "Ошибка при получении наименования домена для авторизации по smb", null,
                    ex);
        }
		String userName = getSyspropValue(dfSession, SYSPROP_SMB_USER_NAME);
		String userPassword = getSyspropValue(dfSession, SYSPROP_SMB_USER_PASSWORD);


		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, userName, userPassword);
		String fileReference = "smb:" + accessProperties.getUrl().replace('\\', '/');

		return new SmbFile(fileReference, auth);
	}

	public static void loadContentBySftp(IDfSession dfSession, FileAccessProperties accessProperties, OutputStream out) throws DfException {
        JschSFTP jsftp = null;
        try {
			jsftp = getJschSFTP(dfSession, accessProperties, jsftp);
            jsftp.get(accessProperties.getPath(), out);
        } catch (JSchException e) {
            throw new ContentLoaderException("Ошибка при работе с файлом: " + accessProperties.getUrl(), e);
        } finally {
            if (jsftp != null) {
                try {
                    jsftp.disconnect();
                } catch (JSchException e) {
                    DfLogger.error(
                            null,
                            "Ошибка при закрытии sftp-соединения c "
                                    + accessProperties.getHost(), null, e);
                }
            }
        }
	}

	public static void deleteContentBySftp(IDfSession dfSession, FileAccessProperties accessProperties) throws DfException {
        boolean deleteFile = "true".equalsIgnoreCase(getSyspropValue(dfSession, SYSPROP_SFTP_NEED_DELETE));
        JschSFTP jsftp = null;
        try {
			jsftp = getJschSFTP(dfSession, accessProperties, jsftp);
			if (deleteFile) {
				try {
					jsftp.remove(accessProperties.getPath());
					DfLogger.info(dfSession, "Удалён sftp файл " + accessProperties.getPath(), null, null);
				} catch (Exception ex) {
					DfLogger.warn(dfSession, "Не удалось удалить файл " + accessProperties.getPath(),
							null, ex);
				}
			}
        } catch (JSchException e) {
            throw new ContentLoaderException("Ошибка при работе с файлом: " + accessProperties.getUrl(), e);
        } finally {
            if (jsftp != null) {
                try {
                    jsftp.disconnect();
                } catch (JSchException e) {
                    DfLogger.error(
                            null,
                            "Ошибка при закрытии sftp-соединения c "
                                    + accessProperties.getHost(), null, e);
                }
            }
        }
	}

	private static JschSFTP getJschSFTP(IDfSession dfSession, FileAccessProperties accessProperties, JschSFTP jsftp) throws JSchException, DfException {
		accessProperties.setUser(getSyspropValue(dfSession, SYSPROP_SFTP_USER_NAME));
		accessProperties.setKeyFilePath(getSyspropValue(dfSession, SYSPROP_SFTP_KEYFILE_PATH));
		JschSFTPBuilder builder = new JschSFTPBuilder();
		jsftp = builder.host(accessProperties.getHost())
                .user(accessProperties.getUser())
                .password(accessProperties.getPassword())
                .keyFilePath(accessProperties.getKeyFilePath()).build();
		jsftp.connect();
		return jsftp;
	}

	/**
	 * 
	 * @param contentSysObject
	 * @param contentXmlObject
	 * @throws DfException
	 */
	public static void loadContentFile(IDfSysObject contentSysObject, ContentType contentXmlObject) throws DfException {
		loadContentFile(contentSysObject, contentXmlObject, false);
	}

	/**
	 * 
	 * @param contentSysObject
	 * @param contentXmlObject
	 * @param newVersion
	 * @throws DfException
	 */
	public static void loadContentFile(IDfSysObject contentSysObject, ContentType contentXmlObject, boolean newVersion)
			throws DfException {
		String fileFormat = null;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		if (contentXmlObject.getDocScan() != null && contentXmlObject.getDocScan().size() > 0) {
			ContentType.DocScan docScan = contentXmlObject.getDocScan().get(0);
			if (docScan.getFileFormat() == null) {
				throw new DfException("Не указан формат файла");
			}
			fileFormat = getFileFormat(docScan.getFileFormat());
			loadContent(docScan, buffer);
		} else if (contentXmlObject.getDocReference() != null && contentXmlObject.getDocReference().size() > 0) {
			ContentType.DocReference docReference = contentXmlObject.getDocReference().get(0);
			if (docReference.getFileFormat() == null) {
				throw new DfException("Не указан формат файла");
			}
			fileFormat = getFileFormat(docReference.getFileFormat());
			loadContent(contentSysObject.getSession(), docReference, buffer);
		}
		if (newVersion) {
			contentSysObject.checkout();
			contentSysObject.setContentType(fileFormat);
			contentSysObject.setContent(buffer);
			contentSysObject.checkin(false, null);
		} else {
			contentSysObject.setContentType(fileFormat);
			contentSysObject.setContent(buffer);
			contentSysObject.save();
		}

//		processIfArchive(contentSysObject, fileFormat);
	}

	/**
	 * 
	 * @param contentSysObject
	 * @param contentType
	 * @param baos
	 * @param newVersion
	 * @throws DfException
	 */
	public static void saveContent(IDfSysObject contentSysObject, String contentType, ByteArrayOutputStream baos,
			boolean newVersion) throws DfException {
		if (newVersion) {
			contentSysObject.checkout();
			contentSysObject.setContentType(contentType);
			contentSysObject.setContent(baos);
			contentSysObject.checkin(false, null);
		} else {
			contentSysObject.setContentType(contentType);
			contentSysObject.setContent(baos);
			contentSysObject.save();
		}
	}
	
	/**
	 * 
	 * @param contentSysObject
	 * @param contentType
	 * @param bais
	 * @param newVersion
	 * @throws DfException
	 */
	public static void saveContent(IDfSysObject contentSysObject, String contentType, ByteArrayInputStream bais,
			boolean newVersion) throws DfException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b;
		while ((b = bais.read()) != -1) {
			baos.write(b);
		}
		saveContent(contentSysObject, contentType, baos, newVersion);
	}
    
	protected static String getFileFormat(String fileExt) {
    	if("ppt".equalsIgnoreCase(fileExt)) {
        	return "ppt8";
        } else if("pptx".equalsIgnoreCase(fileExt)) {
        	return "ppt12";
        } else if("txt".equalsIgnoreCase(fileExt)) {
        	return "crtext";
        } else if("doc".equalsIgnoreCase(fileExt)) {
        	return "msw8";
        } else if("docx".equalsIgnoreCase(fileExt)) {
        	return "msw12";
        } else if("odt".equalsIgnoreCase(fileExt)) {
        	return "odt";
        } else if("xls".equalsIgnoreCase(fileExt)) {
        	return "excel8book";
        } else if("xlsx".equalsIgnoreCase(fileExt)) {
        	return "excel12book";
        } else if("jpg".equalsIgnoreCase(fileExt)) {
        	return "jpeg";
        } else if("tif".equalsIgnoreCase(fileExt)) {
        	return "tiff";
        } else if("xml".equalsIgnoreCase(fileExt)) {
        	return "crtext";
        } else if("sevenz".equalsIgnoreCase(fileExt) || "7z".equalsIgnoreCase(fileExt)) {
        	return "7z";
        } else {
        	return fileExt;
        }
    }

    protected static void processIfArchive(IDfSysObject contentSysObject, String fileFormat) throws DfException{
        if ("zip".equalsIgnoreCase(fileFormat)) {
        	processArchive(contentSysObject, ArchiveStreamFactory.ZIP);
        } else if("arj".equalsIgnoreCase(fileFormat)) {
        	processArchive(contentSysObject, ArchiveStreamFactory.ARJ);
		} else if ("rar".equalsIgnoreCase(fileFormat)) {
            processRarArchive(contentSysObject);
        } else if ("sevenz".equalsIgnoreCase(fileFormat) || "7z".equalsIgnoreCase(fileFormat)) {
        	process7zArchive(contentSysObject);
        }
    }
    
	/**
	 * Это формат архива?
	 * 
	 * @param contentType
	 *            - формат
	 * @return true, если указанный формат - это формат архива; иначе - false
	 */
	public static final boolean isArchiveType(String contentType) {
		return "zip".equalsIgnoreCase(contentType)
				|| "arj".equalsIgnoreCase(contentType)
				|| "rar".equalsIgnoreCase(contentType)
				|| "sevenz".equalsIgnoreCase(contentType) 
				|| "7z".equalsIgnoreCase(contentType);
	}

	/**
	 * Это PDF?
	 * 
	 * @param contentType
	 *            - формат
	 * @return true, если указанный формат - это формат PDF; иначе - false
	 */
	public static final boolean isPdfType(String contentType) {
		return "pdf".equalsIgnoreCase(contentType);
	}

    public static void processArchive(IDfSysObject contentSysObject, String archniveType) throws DfException {
		ArchiveInputStream archiveInputStream = null;
		try {
			archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(archniveType, contentSysObject.getContent());
			
			ArchiveEntry archiveEntry = null;
			int entryIndex = 0;
			while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
				String archiveEntryName = archiveEntry.getName();
				if (archiveEntryName.trim().length() > 3) {
					int index = archiveEntryName.lastIndexOf('.');
					String archiveEntryType = index > -1 ? archiveEntryName.substring(index + 1)
							: archiveEntryName.substring(archiveEntryName.length() - 3);
					if (SUPPORTED_FORMAT.contains(archiveEntryType.toLowerCase())) {
						byte[] zipBuffer = new byte[1024];
						int count = 0;
						ByteArrayOutputStream entryStream = new ByteArrayOutputStream();
						while ((count = archiveInputStream.read(zipBuffer)) != -1) {
							entryStream.write(zipBuffer, 0, count);
						}
						IDfSysObject contentPartSysObject = (IDfSysObject) contentSysObject.getSession()
								.newObject(ContentPersistence.CONTENT_PART_TYPE_NAME);
						contentPartSysObject.setObjectName(archiveEntryName);
						contentPartSysObject.setContentType(getFileFormat(archiveEntryType));
						contentPartSysObject.setContent(entryStream);
						contentPartSysObject.setId(ContentPersistence.ATTR_CONTENT_FOR_PART_ID, contentSysObject.getObjectId());
						contentPartSysObject.setInt(ContentPersistence.ATTR_PART_INDEX, entryIndex);
						contentPartSysObject.save();
						entryIndex++;
					} else {
						DfLogger.warn(
								ContentLoader.class, "Wrong format found in archive entry: "
										+ contentSysObject.getObjectId().getId() + " - " + archiveEntryName,
								null, null);
					}
				} else {
					DfLogger.warn(ContentLoader.class, "Wrong format found in archive entry: "
							+ contentSysObject.getObjectId().getId() + " - " + archiveEntryName, null, null);
				}
			}
		} catch (IOException ioEx) {
			throw new DfException("Cant unzip content: " + contentSysObject.getObjectId().getId(), ioEx);
		} catch (ArchiveException e) {
			throw new DfException("Cant unzip content: " + contentSysObject.getObjectId().getId(), e);
		} catch (DfException e) {
			throw new DfException("Cant unzip content: " + contentSysObject.getObjectId().getId(), e);
		} finally {
			if(archiveInputStream != null) {
				try {
					archiveInputStream.close();
				} catch (IOException ex) {
					DfLogger.warn(ContentLoader.class, "Не удалось закрыть поток чтения zip: "
							+ contentSysObject.getObjectId().getId(), null, ex);
				}
			}
		}
    }
    
    public static void process7zArchive(IDfSysObject contentSysObject) throws DfException {
    	File archiveFile = new File(contentSysObject.getFile(System.getProperty("dfc.data.dir") + "/ccdea/" + contentSysObject.getObjectId().getId() + ".7z"));
    	SevenZFile sevenZFile = null;		
		try {
			sevenZFile = new SevenZFile(archiveFile);			
			SevenZArchiveEntry archiveEntry = null;
			int entryIndex = 0;
			while ((archiveEntry = sevenZFile.getNextEntry()) != null) {
				String archiveEntryName = archiveEntry.getName();
				
				if (archiveEntryName.trim().length() > 3) {
					int index = archiveEntryName.lastIndexOf('.');
					String archiveEntryType = index > -1 ? archiveEntryName.substring(index + 1)
							: archiveEntryName.substring(archiveEntryName.length() - 3);
					if (SUPPORTED_FORMAT.contains(archiveEntryType.toLowerCase())) {
						byte[] zipBuffer = new byte[1024];
						int count = 0;
						ByteArrayOutputStream entryStream = new ByteArrayOutputStream();
						while ((count = sevenZFile.read(zipBuffer, 0, zipBuffer.length)) != -1) {
							entryStream.write(zipBuffer, 0, count);
						}
						IDfSysObject contentPartSysObject = (IDfSysObject) contentSysObject.getSession()
								.newObject(ContentPersistence.CONTENT_PART_TYPE_NAME);
						contentPartSysObject.setObjectName(archiveEntryName);
						contentPartSysObject.setContentType(getFileFormat(archiveEntryType));
						contentPartSysObject.setContent(entryStream);
						contentPartSysObject.setId(ContentPersistence.ATTR_CONTENT_FOR_PART_ID, contentSysObject.getObjectId());
						contentPartSysObject.setInt(ContentPersistence.ATTR_PART_INDEX, entryIndex);
						contentPartSysObject.save();
						entryIndex++;
					} else {
						DfLogger.warn(
								ContentLoader.class, "Wrong format found in archive entry: "
										+ contentSysObject.getObjectId().getId() + " - " + archiveEntryName,
								null, null);
					}
				} else {
					DfLogger.warn(ContentLoader.class, "Wrong format found in archive entry: "
							+ contentSysObject.getObjectId().getId() + " - " + archiveEntryName, null, null);
				}
			}
		} catch (IOException ioEx) {
			throw new DfException("Cant unzip content: " + contentSysObject.getObjectId().getId(), ioEx);
		} catch (DfException e) {
			throw new DfException("Cant unzip content: " + contentSysObject.getObjectId().getId(), e);
		} finally {
			if(sevenZFile != null) {
				try {
					sevenZFile.close();
				} catch (IOException ex) {
					DfLogger.warn(ContentLoader.class, "Не удалось закрыть поток чтения 7z: "
							+ contentSysObject.getObjectId().getId(), null, ex);
				}
			}
		}
    }
    
    public static void processRarArchive(IDfSysObject contentSysObject) throws DfException {
    	File archiveFile = new File(contentSysObject.getFile(System.getProperty("dfc.data.dir") + "/ccdea/" + contentSysObject.getObjectId().getId() + ".rar"));
        Archive arch = null;
        try {
            arch = new Archive(archiveFile);
            if (arch != null) {
                if (arch.isEncrypted()) {
                	throw new DfException("Unrar error: file is encrypted " + contentSysObject.getObjectId().getId());
                } else {
                    FileHeader fh = null;
                    int entryIndex = 0;
                    while ((fh = arch.nextFileHeader()) != null) {
                        String fileNameString = fh.getFileNameString();
                        if (fh.isEncrypted()) {
                            DfLogger.error(ContentLoader.class, "Unrar error: file is encrypted " + fileNameString, null, null);
                        }
                        else {
                            try {
                                if (fileNameString.trim().length() > 3) {
                                    int index = fileNameString.lastIndexOf('.');
                					String entryType = index > -1 ? fileNameString.substring(index + 1)
                							: fileNameString.substring(fileNameString.length() - 3);
                                    if (SUPPORTED_FORMAT.contains(entryType.toLowerCase())) {
                                        ByteArrayOutputStream entryStream = new ByteArrayOutputStream();
                                        arch.extractFile(fh, entryStream);
                                        IDfSysObject contentPartSysObject = (IDfSysObject) contentSysObject.getSession().newObject("ccdea_doc_content_part");
                                        contentPartSysObject.setObjectName(fileNameString);
                                        contentPartSysObject.setContentType(getFileFormat(entryType));
                                        contentPartSysObject.setContent(entryStream);
                                        contentPartSysObject.setId("id_content", contentSysObject.getObjectId());
                                        contentPartSysObject.setInt("n_index", entryIndex);
                                        contentPartSysObject.save();
                                        entryIndex++;
                                    }
                                    else {
                                        DfLogger.warn(ContentLoader.class, "Wrong format found in archive entry: " + contentSysObject.getObjectId().getId() + " - " + fileNameString, null, null);
                                    }
                                }
                                else {
                                    DfLogger.warn(ContentLoader.class, "Wrong format found in archive entry: " + contentSysObject.getObjectId().getId() + " - " + fileNameString, null, null);
                                }
                            } catch (RarException e) {
                                DfLogger.error(ContentLoader.class, "Unrar error", null, e);
                            }
                        }
                    }
                }
            }
        } catch (RarException e) {
            throw new DfException("Unrar error " + contentSysObject.getObjectId().getId(), e);
        } catch (IOException e) {
        	throw new DfException("Unrar error " + contentSysObject.getObjectId().getId(), e);
        } finally {
        	if(arch != null) {
        		try {
					arch.close();
				} catch (IOException e) {
					DfLogger.warn(ContentLoader.class, "Ошибка при закрытии потока чтения архива " + contentSysObject.getObjectId().getId(), null, e);
				}
        	}
            if (archiveFile != null) {
            	try {
            		archiveFile.delete();
            	} catch (Exception ex) {
            		DfLogger.warn(ContentLoader.class, "Ошибка при удалении архива" + contentSysObject.getObjectId().getId(), null, ex);
            	}
            }
        }
    }
    
    public static String getSyspropValue(IDfSession dfSession, String name) throws DfException {
        String value = null;
        String dql = "SELECT \"value\" FROM ucb_sysprop WHERE name=" + DfUtil.toQuotedString(name);
        IDfCollection rs = null;
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            rs = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
            if (rs.next()) {
                value = rs.getString("value");
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        if (value != null && !value.trim().isEmpty()) {
            return value;
        } else {
            throw new DfException(
                    "Не указано значение в ucb_sysprop for name:" + name);
        }
    }

	/**
	 * Удалить контент по заваршению обработки сообщения
	 * Если сообщение самос одержит контент, удаляем его. Если нет - идём на сервер и удаляем оттуда.
	 * @param dfSession сессия
	 * @param messageObject DocPut сообщение
	 * @param contentXmlObject метаданные контента.
	 * @throws DfException ошибка обработки
     */
	public static void deleteContentFile(IDfSession dfSession, IDfSysObject messageObject, ContentType contentXmlObject) throws DfException {

		DfLogger.info(dfSession, "Удаление файла из сообщения " + messageObject.getObjectId(), null, null);

		if (contentXmlObject.getDocScan() != null && contentXmlObject.getDocScan().size() > 0) {
			DfLogger.info(dfSession, "Невозможно удалить файл из сообщения " + messageObject.getObjectId() + " , т.к. контент находится в xml", null, null);
		} else if (contentXmlObject.getDocReference() != null && contentXmlObject.getDocReference().size() > 0) {
			ContentType.DocReference docReference = contentXmlObject.getDocReference().get(0);
			if (docReference.getFileFormat() == null) {
				throw new DfException("Не указан формат файла.");
			}
			String fileReference;
			if (docReference.getFileReference() != null && !docReference.getFileReference().trim().isEmpty()) {
				fileReference = docReference.getFileReference();
			} else if (docReference.getFileReferenceSMB() != null && !docReference.getFileReferenceSMB().trim().isEmpty()) {
				fileReference = docReference.getFileReferenceSMB();
			} else {
				throw new CantFindFileReferenceException("Не найдена ссылка на файл контента.");
			}
			FileAccessProperties accessProperties = FileAccessProperties.parseUrl(fileReference);
			if (JschSFTP.SFTP.equalsIgnoreCase(accessProperties.getProtocol())) {
				deleteContentBySftp(dfSession, accessProperties);
			} else if(accessProperties.getUrl().startsWith("\\\\")) {
				deleteContentBySmb(dfSession, accessProperties);
			}
		}
	}
}
