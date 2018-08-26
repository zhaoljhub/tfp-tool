package com.wisesoft.tool.transfers.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coyee.core.mvc.M;
import com.wisesoft.tool.transfers.helper.FtpHelper;
import com.wisesoft.tool.transfers.properties.Ftp;
import com.wisesoft.tool.transfers.properties.PropertiesHelper;

@Controller
@RequestMapping("")
public class FtpController {
	/**
	 * 将文件从一个服务器拷贝到本地，在从本地拷贝到另一台ftp服务器上去
	 * url 存放配置的文件路径
	 * eg : http://127.0.0.1:8080/tool-transfers/copy.json?url=D:%5Cdata%5Cftp.properties
	 * 需要对参数编码
	 * @return<br/>
	 * Description: <br/>
	 * Date: 2018年8月10日 上午9:27:46<br/>
	 * UpdateContent:<br/>
	 * @author ZHAO.LANGJING
	 */
	@RequestMapping("/copy")
	@ResponseBody
	public M ftpCopy(Model model, String url) {
		if(StringUtils.isBlank(url)) {
			return M.error(model, "参数url为空！");
		}
		Ftp ftp = PropertiesHelper.getInstance(url);
		if(ftp==null) {
			return M.error(model, "初始化参数失败！");
		}
		//先下载
		FtpHelper sourceFtpHelper = FtpHelper.getInstance();
		sourceFtpHelper.login(ftp.getSourceFtpIp(), Integer.parseInt(ftp.getSourceFtpPort()) , ftp.getSourceFtpUserName(), ftp.getSourceFtpUserPwd());
		FtpHelper targetFtpHelper = FtpHelper.getInstance();
		targetFtpHelper.login(ftp.getTargetFtpIp(), Integer.parseInt(ftp.getTargetFtpPort()), ftp.getTargetFtpUserName(), ftp.getTargetFtpUserPwd());
		if(!sourceFtpHelper.isLogin()) {
			return M.error(model, "连接源frp失败！");
		}
		if(!targetFtpHelper.isLogin()) {
			return M.error(model, "连接目标frp失败！");
		}
		//下载文件到本地
		if(sourceFtpHelper.downloads(ftp.getSourceFtpDir(), ftp.getLcoalRootDir())) {
			if(targetFtpHelper.uploads(ftp.getTargetFtpDir(), ftp.getLcoalRootDir())) {
				return M.success(model).msg("拷贝到目标服务器成功！");
			}
			return M.error(model).msg("复制到本地成功，但是拷贝到目标服务器失败！");
		}
		return M.error(model).msg("复制到本地失败！");
	}
}
