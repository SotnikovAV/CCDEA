package ru.rb.ccdea.storage.persistence.fileutils;

import java.io.File;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class JschSFTPBuilder {
	
	private String host;
	private String user;
	private String password;
	private String keyFilePath;
	private int port = 22;
	
	public JschSFTPBuilder host(String host) {
		this.host = host;
		return this;
	}
	
	public JschSFTPBuilder port(int port) {
		this.port = port;
		return this;
	}
	
	public JschSFTPBuilder user(String user) {
		this.user = user;
		return this;
	}
	
	public JschSFTPBuilder password(String password) {
		this.password = password;
		return this;
	}
	
	public JschSFTPBuilder keyFilePath(String keyFilePath) {
		this.keyFilePath = keyFilePath;
		return this;
	}
	
	public JschSFTP build() throws JSchException {
		if(host == null || "".equals(host)) {
			throw new JSchException("Не указан хост");
		}
		if(user == null || "".equals(user)) {
			throw new JSchException("Не указан логин для соединения с сервером");
		}
		JSch jsch = new JSch();
		File file = null;
		Session session = null;
		if(keyFilePath != null && !"".equals(keyFilePath) ){
			file = new File(keyFilePath);
			if (file != null && !file.exists()) {
				throw new JSchException("Не найден файл " + keyFilePath);
			}
			jsch.addIdentity(file.getAbsolutePath());
			session = jsch.getSession(user, host, port);
		} else if(password != null && !"".equals(password)) {
			session = jsch.getSession(user, host, port);
			session.setPassword(password);
		} else {
			throw new JSchException("Не указан пароль или ключ для соединения с сервером " + keyFilePath);
		}
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		
		return new JschSFTP(session);
	}

}
