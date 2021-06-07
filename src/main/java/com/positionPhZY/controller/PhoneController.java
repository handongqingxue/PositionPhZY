package com.positionPhZY.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/phone")
public class PhoneController {
	
	//http://139.196.143.225:8080/PositionPhZY/phone/goLogin
	//https://www.liankexing.com/question/825
	private static final String path="http://www.qrcodesy.com:8080/GoodsPublic/merchant";

	@RequestMapping(value="/goLogin")
	public String goLogin() {
		
		return "phone/login";
	}

	@RequestMapping(value="/goIndex")
	public String goIndex() {
		
		return "phone/index";
	}

	@RequestMapping(value="/login")
	@ResponseBody
	public Map<String, Object> login(String staffsNo, String password, String companyID, boolean remPwd, HttpSession session) {
		Map<String, Object> resultMap = null;
		String url=path+"/login";
		List<NameValuePair> params=new ArrayList<NameValuePair>();
		params.add(0, new BasicNameValuePair("userName", "15969878881"));
		params.add(1, new BasicNameValuePair("password", "E10ADC3949BA59ABBE56E057F20F883E"));
		params.add(2, new BasicNameValuePair("from", ""));
			
		try {
			resultMap=getRespJson(url, params);
			JSONObject resultJO = new JSONObject(resultMap.get("result").toString());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultMap;
	}

	@RequestMapping(value="/getCode")
	public void getCode() {
		System.out.println("qqqqqqqqqqqqqq");
		try {
			post("http://139.196.143.225:8081/position/public/embeded.smd","");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		Map<String, Object> resultMap = null;
		String url="http://139.196.143.225:8081/position/public/embeded.smd";
		List<NameValuePair> params=new ArrayList<NameValuePair>();
		params.add(0, new BasicNameValuePair("jsonrpc", "2.0"));
		params.add(1, new BasicNameValuePair("params", "{\"tenantId\":\"sc19070007\",\"userId\":\"yyc\"}"));
		params.add(2, new BasicNameValuePair("method", "getCode"));
		params.add(3, new BasicNameValuePair("id", "1"));
			
		try {
			resultMap=getRespJson(url, params);
			JSONObject resultJO = new JSONObject(resultMap.get("result").toString());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	
	//https://blog.csdn.net/u013652912/article/details/108637590?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-1.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-1.control
	public String post(String actionUrl, String params)
			throws IOException {
		String serverURL = actionUrl;
		StringBuffer sbf = new StringBuffer(); 
		String strRead = null; 
		URL url = new URL(serverURL); 
		HttpURLConnection connection = (HttpURLConnection)url.openConnection(); 
		connection.setRequestMethod("POST");//请求post方式
		connection.setDoInput(true); 
		connection.setDoOutput(true); 
		//header内的的参数在这里set    
		//connection.setRequestProperty("key", "value");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.connect(); 
		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 
		//body参数放这里
		writer.write("{ \"jsonrpc\": \"2.0\", \"params\":{\"tenantId\":\"sc19070007\",\"userId\":\"yyc\"}, \"method\":\"getCode\", \"id\":1 }"); 
		writer.flush();
		InputStream is = connection.getInputStream(); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 
		while ((strRead = reader.readLine()) != null) { 
			sbf.append(strRead); 
			sbf.append("\r\n"); 
		}
		reader.close(); 
		connection.disconnect();
		String results = sbf.toString();
		System.out.println("results==="+results);
		return results;
	}
	
	public Map<String, Object> getRespJson(String url,List<NameValuePair> params) throws Exception {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		// TODO Auto-generated method stub
		//POST的URL
		//建立HttpPost对象
		HttpPost httppost=new HttpPost(url);
		httppost.setHeader("Content-Type", "application/json-rpc;charset=UTF-8");
		//添加参数
		if(params!=null)
			httppost.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
		//设置编码
		HttpResponse response=new DefaultHttpClient().execute(httppost);
		//发送Post,并返回一个HttpResponse对象
		if(response.getStatusLine().getStatusCode()==200){//如果状态码为200,就是正常返回
		String result=EntityUtils.toString(response.getEntity());
		//得到返回的字符串,打印输出
	//	System.out.println(result);
		jsonMap.put("result", result);
		}
		return jsonMap;
	}
}
