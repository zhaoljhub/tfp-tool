package com.wisesoft.tool.transfers.properties;

public class Ftp {
	private String sourceFtpIp;
	private String sourceFtpPort ;
	private String sourceFtpUserName;
	private String sourceFtpUserPwd;

	private String targetFtpIp;
	private String targetFtpPort;
	private String targetFtpUserName;
	private String targetFtpUserPwd;

	private String sourceFtpDir;
	private String lcoalRootDir;
	private String targetFtpDir;
	
	public String getSourceFtpIp() {
		return sourceFtpIp;
	}
	public void setSourceFtpIp(String sourceFtpIp) {
		this.sourceFtpIp = sourceFtpIp;
	}
	public String getSourceFtpUserName() {
		return sourceFtpUserName;
	}
	public void setSourceFtpUserName(String sourceFtpUserName) {
		this.sourceFtpUserName = sourceFtpUserName;
	}
	public String getSourceFtpUserPwd() {
		return sourceFtpUserPwd;
	}
	public void setSourceFtpUserPwd(String sourceFtpUserPwd) {
		this.sourceFtpUserPwd = sourceFtpUserPwd;
	}
	public String getTargetFtpIp() {
		return targetFtpIp;
	}
	public void setTargetFtpIp(String targetFtpIp) {
		this.targetFtpIp = targetFtpIp;
	}
	public String getTargetFtpUserName() {
		return targetFtpUserName;
	}
	public void setTargetFtpUserName(String targetFtpUserName) {
		this.targetFtpUserName = targetFtpUserName;
	}
	public String getTargetFtpUserPwd() {
		return targetFtpUserPwd;
	}
	public void setTargetFtpUserPwd(String targetFtpUserPwd) {
		this.targetFtpUserPwd = targetFtpUserPwd;
	}
	public String getSourceFtpDir() {
		return sourceFtpDir;
	}
	public void setSourceFtpDir(String sourceFtpDir) {
		this.sourceFtpDir = sourceFtpDir;
	}
	public String getLcoalRootDir() {
		return lcoalRootDir;
	}
	public void setLcoalRootDir(String lcoalRootDir) {
		this.lcoalRootDir = lcoalRootDir;
	}
	
	public String getSourceFtpPort() {
		return sourceFtpPort;
	}
	public void setSourceFtpPort(String sourceFtpPort) {
		this.sourceFtpPort = sourceFtpPort;
	}
	public String getTargetFtpPort() {
		return targetFtpPort;
	}
	public void setTargetFtpPort(String targetFtpPort) {
		this.targetFtpPort = targetFtpPort;
	}
	public String getTargetFtpDir() {
		return targetFtpDir;
	}
	public void setTargetFtpDir(String targetFtpDir) {
		this.targetFtpDir = targetFtpDir;
	}
}
