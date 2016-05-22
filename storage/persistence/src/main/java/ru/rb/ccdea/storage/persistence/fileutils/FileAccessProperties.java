package ru.rb.ccdea.storage.persistence.fileutils;

import java.text.MessageFormat;

public class FileAccessProperties {

	private static final String TO_STRING_TEMPLATE = "Url: {0}\nProtocol: {1}\nUser: {2}\nPassword: {3}\nHost: {4}\nPort: {5}\nPath: {6}\nFile name: {7}\nFile format: {8}";

	private String url = "";

	private String protocol = "";

	private String user = "";

	private String password = "";

	private String host = "";

	private String port = "";

	private String path = "";

	private String fileName = "";

	private String fileFormat = "";
	
	private String keyFilePath = "";

	/**
	 * @return the keyFilePath
	 */
	public String getKeyFilePath() {
		return keyFilePath;
	}

	/**
	 * @param keyFilePath the keyFilePath to set
	 */
	public void setKeyFilePath(String keyFilePath) {
		this.keyFilePath = keyFilePath;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol
	 *            the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the fileFormat
	 */
	public String getFileFormat() {
		return fileFormat;
	}

	/**
	 * @param fileFormat
	 *            the fileFormat to set
	 */
	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return MessageFormat.format(TO_STRING_TEMPLATE, url, protocol, user,
				password, host, port, path, fileName, fileFormat);
	}

	public static final String PATTERN =
			// "^" + // # Start of text
			// "sftp:" + // # Protocol
			// "//" + // # Double slash
			// "([^:]+)" + // # $1 = User Name
			// ":" + // # Colon
			// "([^@]+)" + // # $2 = Password
			// "@" + // # AT sign
			// "(.*?)" + // # $3 = Server name
			// "/" + // # Single slash
			// "(.*?)" + // # $4 = Directory name
			// "(\\?.*)?" + // # Question mark ends URI
			// "$";// # End of text"

			// Match #0. URL целиком (#0 - это HREF, в терминах window.location).
			// Например, #0 ==
			// "https://example.com:8080/some/path/index.html?p=1&q=2&r=3#some-hash"
			"^" +
					// Match #1 & #2. SCHEME (#1 - это PROTOCOL, в терминах
					// window.location).
					// Например, #1 == "https:", #2 == "https"
					"(([^:/\\?#]+):)?" +
					// Match #3-#6. AUTHORITY (#4 = HOST, #5 = HOSTNAME и #6 = PORT,
					// в терминах window.location)
					// Например, #3 == "//example.com:8080", #4 ==
					// "example.com:8080", #5 == "example.com", #6 == "8080"
					"(" + "//(([^:/\\?#]*)(?::([^/\\?#]*))?)" + ")?" +
					// Match #7. PATH (#7 = PATHNAME, в терминах window.location).
					// Например, #7 == "/some/path/index.html"
					"([^\\?#]*)" +
					// Match #8 & #9. QUERY (#8 = SEARCH, в терминах
					// window.location).
					// Например, #8 == "?p=1&q=2&r=3", #9 == "p=1&q=2&r=3"
					"(\\?([^#]*))?" +
					// Match #10 & #11. FRAGMENT (#10 = HASH, в терминах
					// window.location).
					// Например, #10 == "#some-hash", #11 == "some-hash"
					"(#(.*))?" + "$";

	public static final FileAccessProperties parseUrl(String url) {
		FileAccessProperties fp = new FileAccessProperties();
		fp.setUrl(url);
		int protocolEndIndex = url.indexOf(':');
		if (protocolEndIndex < 0) {
			protocolEndIndex = 0;
		} else {
			fp.setProtocol(url.substring(0, protocolEndIndex));
		}
		int indexDubleSlash = url.indexOf("//", protocolEndIndex);
		if (indexDubleSlash < 0) {
			indexDubleSlash = url.indexOf("\\\\", protocolEndIndex);
			if (indexDubleSlash < 0) {
				indexDubleSlash = protocolEndIndex;
			} else {
				indexDubleSlash = indexDubleSlash + 2;
			}
		} else {
			indexDubleSlash = indexDubleSlash + 2;
		}

		int indexDog = -1;
		if ("ftp".equals(fp.getProtocol()) || "smb".equals(fp.getProtocol())) {
			indexDog = url.indexOf("@", indexDubleSlash);
			if (indexDog > 0) {
				int indexUser = url.indexOf(':', indexDubleSlash);
				if (indexUser < 0 || indexUser > indexDog) {
					indexUser = indexDog;
				} else {
					fp.setPassword(url.substring(indexUser + 1, indexDog));
				}
				fp.setUser(url.substring(indexDubleSlash, indexUser));
			}
		}

		int indexHost = url.indexOf(':', indexDog < 0?indexDubleSlash:indexDog);
		if (indexHost < 0) {
			indexHost = url.indexOf('/', indexDog < 0?indexDubleSlash:indexDog);
		}
		if (indexHost < 0) {
			indexHost = url.indexOf('\\', indexDog < 0?indexDubleSlash:indexDog);
		}
		if (indexHost > -1) {
			fp.setHost(url.substring(indexDog < 0?indexDubleSlash:indexDog + 1, indexHost));
		} 
		
		if(indexDog < 0) {
			indexDog = indexDubleSlash;
		}

		int indexPort = url.indexOf('/', indexDog);
		if (indexPort > -1 && indexPort >= indexHost + 1) {
			if (indexHost > -1) {
				fp.setPort(url.substring(indexHost + 1, indexPort));
			} else {
				fp.setHost(url.substring(
						indexDog >= indexDubleSlash ? indexDog + 1 : indexDog,
						indexPort));
			}
		} else {
			indexPort = indexHost;
//			throw new RuntimeException("Не получилось разобрать URL: " + url);
		}

		fp.setPath(url.substring(indexPort));

		int lastSlashIndex = url.lastIndexOf('/');
		if (lastSlashIndex < 0) {
			lastSlashIndex = url.lastIndexOf('\\');
		}
		if (lastSlashIndex > -1) {
			fp.setFileName(url.substring(lastSlashIndex + 1));
		}

		int lastPointIndex = url.lastIndexOf('.');
		if (lastPointIndex > -1) {
			fp.setFileFormat(url.substring(lastPointIndex + 1));
		}

		return fp;
	}

	public static void main(String[] args) {
		String s = "\\\\ntd1\\dfs\\LN2_2JD\\20160518\\F6F0936CEA2CBFA643257FB7002670C0\\FFSRU0767324054283379928_info@italengineering.ru_20160518_064043.pdf";
		String s2 = "C:/Development/Workspaces/CCDEA_GITHUB/test/test.zip";
		String s3 = "ftp://links:pass@test.exavault.com/transfer.pdf";
		FileAccessProperties fileAccessProperties = FileAccessProperties.parseUrl(s);
		FileAccessProperties fileAccessProperties2 = FileAccessProperties.parseUrl(s2);
		FileAccessProperties fileAccessProperties3 = FileAccessProperties.parseUrl(s3);
		System.out.println("done without errors.");
	}

}
