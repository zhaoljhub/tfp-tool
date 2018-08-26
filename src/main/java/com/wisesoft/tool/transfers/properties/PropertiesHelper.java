package com.wisesoft.tool.transfers.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

public class PropertiesHelper {
	/**
	 * 类型映射
	 */
	private static Properties mappings = new Properties();
	
	private static Ftp ftp = new Ftp();

	private static final String MAPPING_FILE_NAME = "D:\\data\\ftp.properties";// 系统配置文件名称
	
	public static Ftp getInstance(String url) {
		init( url);
		return ftp;
	}
	
	public static Boolean init(String url ) {
		try {
			ftp = new Ftp();
			File file = new File(url);
			InputStream ins = new FileInputStream(file);
			//InputStream ins = PropertiesHelper.class.getResourceAsStream(MAPPING_FILE_NAME);
			mappings.load(ins);
			ins.close();
			System.out.println("mapping初始化成功");
			Field[] fields=ftp.getClass().getDeclaredFields();  
			for(Field field : fields) {
				field.setAccessible(true);
				Object obj = mappings.get(field.getName());
				if(obj==null) {
					ftp = null;
					throw new NullPointerException("对象ftp赋值错误"+field.getName());
				}
				field.set(ftp, mappings.get(field.getName()));
			}
			System.out.println("ftp初始化成功");
			return true;
		} catch (Exception er) {
			ftp = null;
			throw new RuntimeException(er);
		}
	}
	
	public static void main(String[] args) {
		new PropertiesHelper();
	}
}
