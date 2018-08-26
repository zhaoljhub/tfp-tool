package com.wisesoft.tool.transfers.helper;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wisesoft.tool.transfers.util.StringExtend;

public class FtpHelper implements Closeable {
	/** 本地字符编码 */
	private static String LOCAL_CHARSET = "GBK";
	// FTP协议里面，规定文件名编码为iso-8859-1
	private static String SERVER_CHARSET = "ISO-8859-1";

	static final Logger log = LoggerFactory.getLogger(FtpHelper.class);

	private FTPClient ftp = null;
	boolean login = false;

	public static FtpHelper getInstance() {
		return new FtpHelper();
	}

	/**
	 * 
	 * ftp登录
	 * 
	 * @param ip         ftp服务地址
	 * @param port       端口号
	 * @param uname      用户名
	 * @param pass       密码
	 * @param workingDir ftp 根目目录
	 */
	public boolean login(String ip, int port, String uname, String pass) {
		ftp = new FTPClient();
		try {
			// 连接
			ftp.connect(ip, port);
			login = ftp.login(uname, pass);
			// 检测连接是否成功
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				log.error("FTP服务器拒绝连接");
				return false;
			}
			initFtp();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public void initFtp() {
		if (login) {
			// 解决文件传输假死的问题,文件上传不了的问题。
			// 告诉服务器每次传输文件时新开一个端口
			ftp.enterLocalPassiveMode();
			ftp.setBufferSize(1024);
			try {
				// 解决上传中文文件名 文件乱码和下载文件名乱码的问题
				if (FTPReply.isPositiveCompletion(ftp.sendCommand("OPTS UTF8", "ON"))) {// 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
					LOCAL_CHARSET = "UTF-8";
				}
				ftp.setControlEncoding(LOCAL_CHARSET);
				// 设置文件类型（二进制）
				ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * ftp上传文件
	 * 
	 * @param localFileName 待上传文件
	 * @param ftpDirName    ftp 目录名
	 * @param ftpFileName   ftp目标文件
	 * @return true||false
	 */
	public boolean uploadFile(String localFileName, String ftpDirName, String ftpFileName) {
		return uploadFile(localFileName, ftpDirName, ftpFileName, false);
	}

	/**
	 * 
	 * ftp上传文件
	 * 
	 * @param localFileName   待上传文件
	 * @param ftpDirName      ftp 目录名
	 * @param ftpFileName     ftp目标文件
	 * @param deleteLocalFile 是否删除本地文件
	 * @return true||false
	 */
	private boolean uploadFile(String localFileName, String ftpDirName, String ftpFileName, boolean deleteLocalFile) {
		if (StringExtend.isNullOrEmpty(ftpFileName))
			throw new RuntimeException("上传文件必须填写文件名！");
		File srcFile = new File(localFileName);
		if (!srcFile.exists()) {
			throw new RuntimeException("文件不存在：" + localFileName);
		}
		try (FileInputStream fis = new FileInputStream(srcFile)) {
			// 上传文件
			boolean flag = uploadFile(fis, ftpDirName, ftpFileName);
			// 删除文件
			if (deleteLocalFile) {
				srcFile.delete();
				log.info("本地文件删除成功：" + srcFile);
			}
			fis.close();
			return flag;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
		}
	}

	/**
	 * 
	 * ftp上传文件 (使用inputstream)
	 * 
	 * @param localFileName 待上传文件
	 * @param ftpDirName    ftp 目录名
	 * @param ftpFileName   ftp目标文件
	 * @return true||false Description: <br/>
	 *         Date: 2018年8月9日 下午2:30:49<br/>
	 *         UpdateContent:<br/>
	 * @author ZHAO.LANGJING
	 */
	private boolean uploadFile(InputStream uploadInputStream, String ftpDirName, String ftpFileName) {
		if (StringExtend.isNullOrEmpty(ftpFileName))
			throw new RuntimeException("上传文件必须填写文件名！");
		try {
			// 设置上传目录(没有则创建)
			if (!createDir(ftpDirName)) {
				throw new RuntimeException("切入FTP目录失败：" + ftpDirName);
			}
			// 上传
			String fileName = new String(ftpFileName.getBytes(LOCAL_CHARSET), SERVER_CHARSET);
			if (ftp.storeFile(fileName, uploadInputStream)) {
				uploadInputStream.close();
				log.info("文件上传成功：" + ftpDirName + "/" + ftpFileName);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
		}
	}

	/**
	 * 下载文件
	 * 
	 * @param ftpDirName        ftp目录名
	 * @param ftpFileName       ftp文件名
	 * @param localFileFullName 本地文件名
	 * @return Description: <br/>
	 *         Date: 2018年8月9日 下午2:30:34<br/>
	 *         UpdateContent:<br/>
	 * @author ZHAO.LANGJING
	 */
	public boolean downloadFile(String ftpDirName, String ftpFileName, String localFileFullName) {
		try {
			if ("".equals(ftpDirName))
				ftpDirName = "/";
			String dir = new String(ftpDirName.getBytes(LOCAL_CHARSET), SERVER_CHARSET);
			if (!ftp.changeWorkingDirectory(dir)) {
				log.info("切换目录失败：" + ftpDirName);
				return false;
			}
			FTPFile[] fs = ftp.listFiles();
			// String fileName = new
			// String(ftpFileName.getBytes(LOCAL_CHARSET),SERVER_CHARSET);
			for (FTPFile ff : fs) {
				String[] rt = ftp.doCommandAsStrings("pwd", "");
				System.out.println(rt[0].toString() + ":" + ff.getName());
				if (ff.getName().equals(ftpFileName)) {
					FileOutputStream out = new FileOutputStream(localFileFullName, false);
					InputStream in = ftp
							.retrieveFileStream(new String(ff.getName().getBytes(LOCAL_CHARSET), SERVER_CHARSET));
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);
					return true;
				}
			}
			System.out.println("下载ftp文件失败：" + ftpFileName + ";目录：" + ftpDirName);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 批量上传到ftp的指定目录
	 * @param ftpDirName
	 * @param localRootPath
	 * @return<br/>
	 * Description: <br/>
	 * Date: 2018年8月10日 下午2:58:17<br/>
	 * UpdateContent:<br/>
	 * @author ZHAO.LANGJING
	 */
	public boolean uploads(String ftpDirName, String localRootPath) {
		try {
			List<String> pathArray = getLocalPath(localRootPath, new ArrayList<String>() , true);
			for (String string : pathArray) {
				// 构造ftp路径
				if(!string.endsWith("/")) {
					string += "/";
				}
				String str =ftpDirName + string.replace(localRootPath, "");
				createDir(str);
				//string = new String(string.getBytes(LOCAL_CHARSET), SERVER_CHARSET);
				ftp.changeWorkingDirectory(str);
				File file  = new File(string);
				File[] files = file.listFiles();
				if(files!=null&&files.length>0) {
					for(File childFile : files) {
						if (childFile.getName().equals(".") || childFile.getName().equals(".."))
							continue;
						if(!childFile.isDirectory()) {
							FileInputStream is = new FileInputStream(childFile);
							uploadFile(is, str, childFile.getName());
						}
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 批量下载到本地的目录
	 * @param ftpDirName
	 * @param localRootPath
	 * @return<br/>
	 * Description: <br/>
	 * Date: 2018年8月10日 下午2:17:15<br/>
	 * UpdateContent:<br/>
	 * @author ZHAO.LANGJING
	 */
	public boolean downloads(String ftpDirName, String localRootPath) {
		try {
			List<String> pathArray = getFtpPath(ftpDirName, new ArrayList<String>() ,true);
			for (String string : pathArray) {
				// 构造本地路径和去掉要靠被目录的目录
				String localPath =localRootPath + string.replace(ftpDirName, "");
				File localFile = new File(localPath);
				if (!localFile.exists()) {
					localFile.mkdirs();
				}
				//string = new String(string.getBytes(LOCAL_CHARSET), SERVER_CHARSET);
				ftp.changeWorkingDirectory(string);
				FTPFile[] file = ftp.listFiles();
				for (FTPFile ftpFile : file) {
					if (ftpFile.getName().equals(".") || ftpFile.getName().equals(".."))
						continue;
					if (!ftpFile.isDirectory()) {
						// false 不追加，覆盖原有的，上面这种方式老是失败
						/*
						 * OutputStream out = new FileOutputStream(localFile + "/" +
						 * ftpFile.getName(),false); //ftp.retrieveFile(ftpFileName, is); InputStream in
						 * = ftp.retrieveFileStream(ftpFile.getName()); if (in != null) {
						 * IOUtils.copy(in, out); } IOUtils.closeQuietly(in); IOUtils.closeQuietly(out);
						 */
						String filename = localFile + "/" + ftpFile.getName();
						OutputStream is = new FileOutputStream(filename);
						ftp.retrieveFile(new String(ftpFile.getName().getBytes(LOCAL_CHARSET), SERVER_CHARSET), is);
						is.close();
						System.out.println("ftp文件已下载：" + filename);
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public ArrayList<String> getFtpPath(String path, ArrayList<String> pathArray , boolean addRoot) throws IOException {
		// 添加本目录
		FTPFile[] files = ftp.listFiles();
		if(files!=null&&files.length>0) {
			if(addRoot) {
				pathArray.add(path);
			}
			for (FTPFile ftpFile : files) {
				if (ftpFile.getName().equals(".") || ftpFile.getName().equals(".."))
					continue;
				if (ftpFile.isDirectory()) {// 如果是目录，则递归调用，查找里面所有文件
					if (!path.endsWith("/")) {
						path += "/";
					}
					path += ftpFile.getName();
					pathArray.add(path);
					ftp.changeWorkingDirectory(path);// 改变当前路径
					getFtpPath(path, pathArray ,false);// 递归调用
					path = path.substring(0, path.lastIndexOf("/"));// 避免对之后的同目录下的路径构造作出干扰，
				}
			}
		}
		return pathArray;
	}
	
	public ArrayList<String> getLocalPath(String path, ArrayList<String> pathArray , boolean addRoot) throws IOException {
		//获取文件夹下所有路径
		File file = new File(path);
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			if(files!=null&&files.length>0) {
				if(addRoot) {
					pathArray.add(path);
				}
				for(File childFile : files) {
					if (childFile.getName().equals(".") || childFile.getName().equals(".."))
						continue;
					if(childFile.isDirectory()) {
						if (!path.endsWith("/")) {
							path += "/";
						}
						path += childFile.getName();
						pathArray.add(path);
						getLocalPath(path, pathArray , false);// 递归调用
						path = path.substring(0, path.lastIndexOf("/"));// 避免对之后的同目录下的路径构造作出干扰，
					}
				}
			}
		}
		return pathArray;
	}

	/**
	 * 
	 * 删除ftp上的文件
	 * 
	 * @param ftpFileName
	 * @return true || false
	 */
	public boolean removeFile(String ftpFilePath) {
		boolean flag = false;
		try {
			ftpFilePath = new String(ftpFilePath.getBytes(LOCAL_CHARSET), SERVER_CHARSET);
			flag = ftp.deleteFile(ftpFilePath);
			if (flag) {
				log.info("删除文件：成功");
			} else {
				log.error("删除文件：失败 " + ftpFilePath);
			}
			return flag;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 删除空目录
	 * 
	 * @param dir
	 * @return
	 */
	public boolean removeDir(String dir) {
		if (StringExtend.startWith(dir, "/"))
			dir = "/" + dir;
		try {
			String d = new String(dir.toString().getBytes(LOCAL_CHARSET), SERVER_CHARSET);
			return ftp.removeDirectory(d);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 创建目录(有则切换目录，没有则创建目录)
	 * 
	 * @param dir
	 * @return
	 */
	public boolean createDir(String dir) {
		if (StringExtend.isNullOrEmpty(dir))
			return true;
		String d;
		try {
			// 目录编码，解决中文路径问题
			d = new String(dir.toString().getBytes(LOCAL_CHARSET), SERVER_CHARSET);
			// 尝试切入目录
			if (ftp.changeWorkingDirectory(d)) {
				return true;
			}
			dir = StringExtend.trimStart(dir, "/");
			dir = StringExtend.trimEnd(dir, "/");
			String[] arr = dir.split("/");
			StringBuffer sbfDir = new StringBuffer();
			// 循环生成子目录
			for (String s : arr) {
				sbfDir.append("/");
				sbfDir.append(s);
				// 目录编码，解决中文路径问题
				d = new String(sbfDir.toString().getBytes(LOCAL_CHARSET), SERVER_CHARSET);
				// 尝试切入目录
				if (ftp.changeWorkingDirectory(d))
					continue;
				if (!ftp.makeDirectory(d)) {
					log.info("[失败]ftp创建目录：" + sbfDir.toString());
					return false;
				}
			}
			// 将目录切换至指定路径
			return ftp.changeWorkingDirectory(d);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 *
	 * 销毁ftp连接
	 *
	 */
	public void closeFtpConnection() {
		login = false;
		if (ftp != null) {
			if (ftp.isConnected()) {
				try {
					ftp.logout();
					ftp.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		String ip = "192.168.50.21";
		int port = 2121;
		String userName = "oa1";
		String userPwd = "ziguangoa";
		try {
			FtpHelper ftp = FtpHelper.getInstance();
			ftp.login(ip, port, userName, userPwd);

			String localFile = "D:/data/abc1.txt";
			String ftpDir = "/";
			String ftpFile = "111.txt";

			File file = new File(localFile);
			if (file.exists()) {
				System.out.println("文件存在！");
			}
			FileInputStream fi = new FileInputStream(localFile);
			boolean success =false;
			//上传文件
//			success = ftp.uploadFile(fi, ftpDir, ftpFile);
//			System.out.println("上传结果" + success);
			
			// 删除文件
//			success = ftp.removeFile("/a2.txt");
//			System.out.println("删除结果" + success);

			String localFileFullName = "D:/data/测试测试.txt";
			// success = ftp.downloadFile("/", "a1.txt", localFileFullName);

			// 批量下载
			String localFileDir = "D:/data/test";
			success = ftp.downloads("/", localFileDir);
			//success = ftp.uploads("/test1", localFileDir);
			System.out.println("下载结果" + success);
			ftp.closeFtpConnection();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {

	}

	public boolean isLogin() {
		return login;
	}

	public void setLogin(boolean login) {
		this.login = login;
	}

}
