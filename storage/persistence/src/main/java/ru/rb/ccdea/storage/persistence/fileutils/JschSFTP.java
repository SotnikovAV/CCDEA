package ru.rb.ccdea.storage.persistence.fileutils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class JschSFTP {
	
	public static final String SFTP = "sftp";

	private Session session;
	private Channel channel;
	private ChannelSftp schannel;

	public JschSFTP(Session session) {
		this.session = session;
	}

	public void connect() throws JSchException {
		// connect
		session.connect();
		// get SFTP channel
		channel = session.openChannel(SFTP);
		channel.connect();
		schannel = (ChannelSftp) channel;
	}

	public void disconnect() throws JSchException {
		if(schannel != null) {
			schannel.quit();
		}
		if(channel != null) {
			channel.disconnect();
		}
		if(session != null) {
			session.disconnect();
		}
	}

	public void get(String src, OutputStream dst) throws JSchException {
		try {
			schannel.get(src, dst);
		} catch (SftpException e) {
			throw new JSchException("Ошибка чтения файла " + src, e);
		}
	}

	public void put(InputStream src, String dst) throws JSchException {
		try {
			schannel.put(src, dst);
		} catch (SftpException e) {
			throw new JSchException("Ошибка записи файла " + dst, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<ChannelSftp.LsEntry> ls(String dstFolder) throws JSchException {
		try {
			return new ArrayList<ChannelSftp.LsEntry>(schannel.ls(dstFolder));
		} catch (SftpException e) {
			throw new JSchException("Ошибка просмотра содержимого папки "
					+ dstFolder, e);
		}
	}

	public void mkdir(String dstFolder, String newFolderName)
			throws JSchException {
		try {
			schannel.cd(dstFolder);
			schannel.mkdir(newFolderName);
		} catch (SftpException e) {
			throw new JSchException("Ошибка при создании папки "
					+ newFolderName + " в папке " + dstFolder, e);
		}
	}

	public void remove(String dstFilePath) throws JSchException {
		try {
			schannel.rm(dstFilePath);
		} catch (SftpException e) {
			throw new JSchException("Ошибка при удалении файла " + dstFilePath,
					e);
		}
	}

	public void cd(String dstFilePath) throws JSchException {
		try {
			schannel.cd(dstFilePath);
		} catch (SftpException e) {
			throw new JSchException("Ошибка при переходе в папку "
					+ dstFilePath, e);
		}
	}
}
