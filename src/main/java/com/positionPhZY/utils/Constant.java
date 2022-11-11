package com.positionPhZY.utils;

public class Constant {
	
	public static final String NO_PWD_LOGIN_URL="http://"+Constant.SERVICE_IP_STR+":"+Constant.SERVICE_PORT_STR+"/position/login";
	
	public static final String SERVICE_IP_STR="serviceIp";
	
	public static final String SERVICE_PORT_STR="servicePort";

	/**
	 * 潍坊润中精细化工有限公司
	 */
	public static final int WFRZJXHYXGS=1;
	/**
	 * 昌邑市瑞海生物科技有限公司
	 */
	public static final int CYSRHSWKJYXGS=2;

	//润中start
	public static final String SERVICE_IP="124.70.38.226";
	public static final String TENANT_ID="sc21090414";
	public static final String USER_ID="test";
	public static final String PASSWORD="test";
	//润中end
	
	//瑞海start
	public static final String SERVICE_IP_CYSRHSWKJYXGS="120.224.131.123";
	public static final Integer SERVICE_PORT_CYSRHSWKJYXGS=81;
	public static final String TENANT_ID_CYSRHSWKJYXGS="sc21100449";
	public static final String USERNAME_CYSRHSWKJYXGS="rhsw";
	public static final String PASSWORD_CYSRHSWKJYXGS="rhsw";
	//瑞海end
	
	/*
	//这个ip是瑞海华为云的，暂时不用了，换成本地服务器部署了
	public static final String SERVICE_IP_CYSRHSWKJYXGS="124.71.205.164";
	public static final String TENANT_ID_CYSRHSWKJYXGS="sc21100449";
	public static final String USER_ID_CYSRHSWKJYXGS="admin";
	public static final String PASSWORD_CYSRHSWKJYXGS="123";
	*/
}
