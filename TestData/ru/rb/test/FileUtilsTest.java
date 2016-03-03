/**
 * 
 */
package ru.rb.test;

import org.junit.Assert;
import org.junit.Test;

import ru.rb.ccdea.storage.persistence.fileutils.FileAccessProperties;

public class FileUtilsTest {

	@Test
	public void testParseSftpVsLoginPassUrl() {
		String url = "sftp://dmadmin:document@178.63.67.209/home/dmadmin/documentum/logs/documentum.log";
		
		FileAccessProperties fp = FileAccessProperties.parseUrl(url);
		System.out.println(fp.toString());
		Assert.assertTrue("Ожидалось: " + url + " Получено: " + fp.getUrl(),
				url.equals(fp.getUrl()));
		Assert.assertTrue("Ожидалось: " + "sftp" + " Получено: " + fp.getProtocol(),
				"sftp".equals(fp.getProtocol()));
		Assert.assertTrue("Ожидалось: " + "dmadmin" + " Получено: " + fp.getUser(),
				"dmadmin".equals(fp.getUser()));
		Assert.assertTrue("Ожидалось: " + "document" + " Получено: " + fp.getPassword(),
				"document".equals(fp.getPassword()));
		Assert.assertTrue("Ожидалось: " + "178.63.67.209" + " Получено: " + fp.getHost(),
				"178.63.67.209".equals(fp.getHost()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getPort(),
				"".equals(fp.getPort()));
		Assert.assertTrue("Ожидалось: " + "/home/dmadmin/documentum/logs/documentum.log" + " Получено: " + fp.getPath(),
				"/home/dmadmin/documentum/logs/documentum.log".equals(fp.getPath()));
		Assert.assertTrue("Ожидалось: " + "documentum.log" + " Получено: " + fp.getFileName(),
				"documentum.log".equals(fp.getFileName()));
		Assert.assertTrue("Ожидалось: " + "log" + " Получено: " + fp.getFileFormat(),
				"log".equals(fp.getFileFormat()));
	}
	
	@Test
	public void testParseSftpVsLoginUrl() {
		String url = "sftp://dmadmin@178.63.67.209/home/dmadmin/documentum/logs/documentum.log";
		
		FileAccessProperties fp = FileAccessProperties.parseUrl(url);
		System.out.println(fp.toString());
		Assert.assertTrue("Ожидалось: " + url + " Получено: " + fp.getUrl(),
				url.equals(fp.getUrl()));
		Assert.assertTrue("Ожидалось: " + "sftp" + " Получено: " + fp.getProtocol(),
				"sftp".equals(fp.getProtocol()));
		Assert.assertTrue("Ожидалось: " + "dmadmin" + " Получено: " + fp.getUser(),
				"dmadmin".equals(fp.getUser()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getPassword(),
				"".equals(fp.getPassword()));
		Assert.assertTrue("Ожидалось: " + "178.63.67.209" + " Получено: " + fp.getHost(),
				"178.63.67.209".equals(fp.getHost()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getPort(),
				"".equals(fp.getPort()));
		Assert.assertTrue("Ожидалось: " + "/home/dmadmin/documentum/logs/documentum.log" + " Получено: " + fp.getPath(),
				"/home/dmadmin/documentum/logs/documentum.log".equals(fp.getPath()));
		Assert.assertTrue("Ожидалось: " + "documentum.log" + " Получено: " + fp.getFileName(),
				"documentum.log".equals(fp.getFileName()));
		Assert.assertTrue("Ожидалось: " + "log" + " Получено: " + fp.getFileFormat(),
				"log".equals(fp.getFileFormat()));
	}
	
	@Test
	public void testParseSftpUrl() {
		String url = "sftp://178.63.67.209:22/home/dmadmin/documentum/logs/documentum.log";
		
		FileAccessProperties fp = FileAccessProperties.parseUrl(url);
		System.out.println(fp.toString());
		Assert.assertTrue("Ожидалось: " + url + " Получено: " + fp.getUrl(),
				url.equals(fp.getUrl()));
		Assert.assertTrue("Ожидалось: " + "sftp" + " Получено: " + fp.getProtocol(),
				"sftp".equals(fp.getProtocol()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getUser(),
				"".equals(fp.getUser()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getPassword(),
				"".equals(fp.getPassword()));
		Assert.assertTrue("Ожидалось: " + "178.63.67.209" + " Получено: " + fp.getHost(),
				"178.63.67.209".equals(fp.getHost()));
		Assert.assertTrue("Ожидалось: " + "22" + " Получено: " + fp.getPort(),
				"22".equals(fp.getPort()));
		Assert.assertTrue("Ожидалось: " + "/home/dmadmin/documentum/logs/documentum.log" + " Получено: " + fp.getPath(),
				"/home/dmadmin/documentum/logs/documentum.log".equals(fp.getPath()));
		Assert.assertTrue("Ожидалось: " + "documentum.log" + " Получено: " + fp.getFileName(),
				"documentum.log".equals(fp.getFileName()));
		Assert.assertTrue("Ожидалось: " + "log" + " Получено: " + fp.getFileFormat(),
				"log".equals(fp.getFileFormat()));
		
		url = "sftp://bravo04.imb.ru:22/oracle/u01/upload/JETDOCO1/EA/1166092_6653254.file";
		
		fp = FileAccessProperties.parseUrl(url);
		System.out.println(fp.toString());
		Assert.assertTrue("Ожидалось: " + url + " Получено: " + fp.getUrl(),
				url.equals(fp.getUrl()));
		Assert.assertTrue("Ожидалось: " + "sftp" + " Получено: " + fp.getProtocol(),
				"sftp".equals(fp.getProtocol()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getUser(),
				"".equals(fp.getUser()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getPassword(),
				"".equals(fp.getPassword()));
		Assert.assertTrue("Ожидалось: " + "bravo04.imb.ru" + " Получено: " + fp.getHost(),
				"bravo04.imb.ru".equals(fp.getHost()));
		Assert.assertTrue("Ожидалось: " + "22" + " Получено: " + fp.getPort(),
				"22".equals(fp.getPort()));
		Assert.assertTrue("Ожидалось: " + "/oracle/u01/upload/JETDOCO1/EA/1166092_6653254.file" + " Получено: " + fp.getPath(),
				"/oracle/u01/upload/JETDOCO1/EA/1166092_6653254.file".equals(fp.getPath()));
		Assert.assertTrue("Ожидалось: " + "1166092_6653254.file" + " Получено: " + fp.getFileName(),
				"1166092_6653254.file".equals(fp.getFileName()));
		Assert.assertTrue("Ожидалось: " + "file" + " Получено: " + fp.getFileFormat(),
				"file".equals(fp.getFileFormat()));
		
	}
	
	@Test
	public void testParseLocalUrl() {
		String url = "/home/dmadmin/documentum/logs/documentum.log";
		
		FileAccessProperties fp = FileAccessProperties.parseUrl(url);
		System.out.println(fp.toString());
		Assert.assertTrue("Ожидалось: " + url + " Получено: " + fp.getUrl(),
				url.equals(fp.getUrl()));
		Assert.assertTrue("Ожидалось: " + "''" + " Получено: " + fp.getProtocol(),
				"".equals(fp.getProtocol()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getUser(),
				"".equals(fp.getUser()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getPassword(),
				"".equals(fp.getPassword()));
		Assert.assertTrue("Ожидалось: " + "''" + " Получено: " + fp.getHost(),
				"".equals(fp.getHost()));
		Assert.assertTrue("Ожидалось: " + "''" + " Получено: " + fp.getPort(),
				"".equals(fp.getPort()));
		Assert.assertTrue("Ожидалось: " + "/home/dmadmin/documentum/logs/documentum.log" + " Получено: " + fp.getPath(),
				"/home/dmadmin/documentum/logs/documentum.log".equals(fp.getPath()));
		Assert.assertTrue("Ожидалось: " + "documentum.log" + " Получено: " + fp.getFileName(),
				"documentum.log".equals(fp.getFileName()));
		Assert.assertTrue("Ожидалось: " + "log" + " Получено: " + fp.getFileFormat(),
				"log".equals(fp.getFileFormat()));
		
		url = "/filestore/earch_siebel/1-24S7UI_1-2ABNMW.pdf";
		
		fp = FileAccessProperties.parseUrl(url);
		System.out.println(fp.toString());
		Assert.assertTrue("Ожидалось: " + url + " Получено: " + fp.getUrl(),
				url.equals(fp.getUrl()));
		Assert.assertTrue("Ожидалось: " + "''" + " Получено: " + fp.getProtocol(),
				"".equals(fp.getProtocol()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getUser(),
				"".equals(fp.getUser()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getPassword(),
				"".equals(fp.getPassword()));
		Assert.assertTrue("Ожидалось: " + "''" + " Получено: " + fp.getHost(),
				"".equals(fp.getHost()));
		Assert.assertTrue("Ожидалось: " + "''" + " Получено: " + fp.getPort(),
				"".equals(fp.getPort()));
		Assert.assertTrue("Ожидалось: " + "/filestore/earch_siebel/1-24S7UI_1-2ABNMW.pdf" + " Получено: " + fp.getPath(),
				"/filestore/earch_siebel/1-24S7UI_1-2ABNMW.pdf".equals(fp.getPath()));
		Assert.assertTrue("Ожидалось: " + "1-24S7UI_1-2ABNMW.pdf" + " Получено: " + fp.getFileName(),
				"1-24S7UI_1-2ABNMW.pdf".equals(fp.getFileName()));
		Assert.assertTrue("Ожидалось: " + "pdf" + " Получено: " + fp.getFileFormat(),
				"pdf".equals(fp.getFileFormat()));
		
	}
	
	@Test
	public void testParseSmbUrl() {
		String url = "\\\\home\\dmadmin\\documentum\\logs\\documentum.log";
		
		FileAccessProperties fp = FileAccessProperties.parseUrl(url);
		System.out.println(fp.toString());
		Assert.assertTrue("Ожидалось: " + url + " Получено: " + fp.getUrl(),
				url.equals(fp.getUrl()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getProtocol(),
				"".equals(fp.getProtocol()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getUser(),
				"".equals(fp.getUser()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getPassword(),
				"".equals(fp.getPassword()));
		Assert.assertTrue("Ожидалось: " + "home" + " Получено: " + fp.getHost(),
				"home".equals(fp.getHost()));
		Assert.assertTrue("Ожидалось: " + "" + " Получено: " + fp.getPort(),
				"".equals(fp.getPort()));
		Assert.assertTrue("Ожидалось: " + "\\dmadmin\\documentum\\logs\\documentum.log" + " Получено: " + fp.getPath(),
				"\\dmadmin\\documentum\\logs\\documentum.log".equals(fp.getPath()));
		Assert.assertTrue("Ожидалось: " + "documentum.log" + " Получено: " + fp.getFileName(),
				"documentum.log".equals(fp.getFileName()));
		Assert.assertTrue("Ожидалось: " + "log" + " Получено: " + fp.getFileFormat(),
				"log".equals(fp.getFileFormat()));
	}

}
