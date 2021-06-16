package com.positionPhZY.controller;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.positionPhZY.entity.*;
import com.positionPhZY.service.*;
import com.positionPhZY.util.date.DateUtil;
import com.positionPhZY.util.sha256.SHA256Utils;

@Controller
@RequestMapping("/phone")
public class PhoneController {
	
	//http://139.196.143.225:8080/PositionPhZY/phone/goLogin
	//https://www.liankexing.com/question/825
	
	//private static final String PUBLIC_URL="http://121.33.253.235:8081/position/public/embeded.smd";
	private static final String PUBLIC_URL="http://139.196.143.225:8081/position/public/embeded.smd";
	//private static final String SERVICE_URL="http://121.33.253.235:8081/position/service/embeded.smd";
	private static final String SERVICE_URL="http://139.196.143.225:8081/position/service/embeded.smd";
	
	@Autowired
	private WarnRecordService warnRecordService;
	@Autowired
	private WarnTriggerService warnTriggerService;

	@RequestMapping(value="/goLogin")
	public String goLogin() {
		
		return "phone/login";
	}

	@RequestMapping(value="/goIndex")
	public String goIndex() {
		
		return "phone/index";
	}

	@RequestMapping(value="/goWarnCount")
	public String goWarnCount() {

		return "phone/warnCount";
	}

	@RequestMapping(value="/initTodayWarnCount")
	@ResponseBody
	public Map<String, Object> initTodayWarnCount() {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Map<String, Object>> todayWarnList = new ArrayList<Map<String,Object>>();
		List<WarnTrigger> warnTriggerList = warnTriggerService.select();
		List<WarnRecord> warnRecordList = warnRecordService.select();
		for (int i = 0; i < warnTriggerList.size(); i++) {
			Map<String, Object> todayWarnMap=new HashMap<String, Object>();
			WarnTrigger warnTrigger = warnTriggerList.get(i);
			int warnCount=0;
			for (int j = 0; j < warnRecordList.size(); j++) {
				WarnRecord warnRecord = warnRecordList.get(j);
				if(warnTrigger.getWarnType()==warnRecord.getWarnType()) {
					warnCount++;
				}
			}
			todayWarnMap.put("name", warnTrigger.getName());
			todayWarnMap.put("count", warnCount);
			todayWarnList.add(todayWarnMap);
		}
		
		resultMap.put("todayWarnList", todayWarnList);
		
		return resultMap;
	}

	@RequestMapping(value="/initBJTJBarChartData")
	@ResponseBody
	public Map<String, Object> initBJTJBarChartData(String startDate,String endDate,String flag) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<String> legendDataList=new ArrayList<String>();
		List<String> xAxisDataLabelList=new ArrayList<String>();
		
		List<WarnRecord> warnRecordList = warnRecordService.selectBarChartData(startDate,endDate,flag);
		for (int i = 0; i < warnRecordList.size(); i++) {
			WarnRecord warnRecord = warnRecordList.get(i);
			String wtName = warnRecord.getWtName();
			if(!checkWarnRecordWtNameExist(legendDataList,wtName))
				legendDataList.add(wtName);
			String xAxisDataLabel=warnRecord.getxAxisDataLabel();
			if(!checkXAxisDataLabel(xAxisDataLabelList,xAxisDataLabel))
				xAxisDataLabelList.add(xAxisDataLabel);
		}
		System.out.println("legendDataList==="+legendDataList.toString());
		System.out.println("xAxisDataLabelList==="+xAxisDataLabelList.size());
		List<Map<String, Object>> seriesList = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < legendDataList.size(); i++) {
			Map<String, Object> legendDataMap=new HashMap<String, Object>();
			List<Integer> seriesDataList = new ArrayList<Integer>();
			List<Map<String, Object>> seriesLsDataList = new ArrayList<Map<String, Object>>();
			for (int j = 0; j < xAxisDataLabelList.size(); j++) {
				Map<String, Object> seriesLsDataMap=new HashMap<String, Object>();
				seriesLsDataMap.put("key",xAxisDataLabelList.get(j));
				seriesLsDataMap.put("value",0);
				seriesLsDataList.add(seriesLsDataMap);
				
				seriesDataList.add(0);
			}
			legendDataMap.put("name",legendDataList.get(i));
			legendDataMap.put("type","bar");
			legendDataMap.put("lsData", seriesLsDataList);
			legendDataMap.put("data", seriesDataList);
			legendDataMap.put("barGap",0);
			seriesList.add(legendDataMap);
		}

		for (int i = 0; i < legendDataList.size(); i++) {
			for (int j = 0; j < xAxisDataLabelList.size(); j++) {
				Map<String, Object> legendDataMap = seriesList.get(i);
				String name = legendDataMap.get("name").toString();
				List<Map<String, Object>> seriesLsDataList = (List<Map<String, Object>>)legendDataMap.get("lsData");
				Map<String, Object> seriesLsDataMap = seriesLsDataList.get(j);
				String key = seriesLsDataMap.get("key").toString();
				List<Integer> seriesDataList = (List<Integer>)legendDataMap.get("data");
				int warnCount = seriesDataList.get(j);
				for (int k = 0; k < warnRecordList.size(); k++) {
					WarnRecord warnRecord = warnRecordList.get(k);
					if(name.equals(warnRecord.getWtName())&&key.equals(warnRecord.getxAxisDataLabel())) {
						System.out.println("name="+name+",xAxisDataLabel="+key);
						warnCount+=warnRecord.getWarnCount();
						//System.out.println("warnCount==="+warnCount);
						seriesDataList.set(j,warnCount);
						System.out.println(name+",seriesDataList==="+seriesDataList.toString());
					}
				}
			}
		}
		System.out.println("seriesList==="+seriesList.toString());
		
		resultMap.put("legendDataList", legendDataList);
		resultMap.put("xAxisDataLabelList", xAxisDataLabelList);
		resultMap.put("seriesList", seriesList);
		
		return resultMap;
	}
	
	public boolean checkWarnRecordWtNameExist(List<String> legendDataList, String wtName) {
		boolean exist = false;
		for (String legendData : legendDataList) {
			if(wtName.equals(legendData)) {
				exist=true;
				break;
			}
		}
		return exist;
	}
	
	public boolean checkXAxisDataLabel(List<String> xAxisDataLabelList, String xAxisData) {
		boolean exist = false;
		for (String xAxisDataLabel : xAxisDataLabelList) {
			if(xAxisData.equals(xAxisDataLabel)) {
				exist=true;
				break;
			}
		}
		return exist;
	}

	@RequestMapping(value="/insertWarnTriggerData")
	@ResponseBody
	public Map<String, Object> insertWarnTriggerData(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> wtrMap = getWarnTriggers(request);
		List<WarnTrigger> warnTriggerList = JSON.parseArray(wtrMap.get("result").toString(),WarnTrigger.class);
		System.out.println("==="+warnTriggerList.size());
		int count=warnTriggerService.add(warnTriggerList);
		if(count==0) {
			resultMap.put("status", "no");
			resultMap.put("message", "初始化报警触发器失败");
		}
		else {
			resultMap.put("status", "ok");
			resultMap.put("message", "初始化报警触发器成功");
		}
		return resultMap;
	}

	@RequestMapping(value="/insertWarnRecordData")
	@ResponseBody
	public Map<String, Object> insertWarnRecordData(HttpServletRequest request) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> wrrMap = getWarnRecords(request);
		List<WarnRecord> warnRecordList = JSON.parseArray(wrrMap.get("result").toString(),WarnRecord.class);
		System.out.println("==="+warnRecordList.size());
		int count=warnRecordService.add(warnRecordList);
		if(count==0) {
			resultMap.put("status", "no");
			resultMap.put("message", "初始化报警记录失败");
		}
		else {
			resultMap.put("status", "ok");
			resultMap.put("message", "初始化报警记录成功");
		}
		return resultMap;
	}

	@RequestMapping(value="/selectWarnCountBarData")
	@ResponseBody
	public Map<String, Object> selectWarnCountBarData() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<WarnRecord> wrList=warnRecordService.select();
		if(wrList.size()==0) {
			resultMap.put("status", "no");
			resultMap.put("message", "暂无数据");
		}
		else {
			resultMap.put("status", "ok");
			resultMap.put("wrList", wrList);
		}
		return resultMap;
	}

	/*
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
	*/

	@RequestMapping(value="/getCode")
	@ResponseBody
	public Map<String, Object> getCode(String tenantId, String userId,HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			JSONObject paramJO=new JSONObject();
			//paramJO.put("tenantId", "ts00000006");
			paramJO.put("tenantId", tenantId);
			//paramJO.put("userId", "test001");
			paramJO.put("userId", userId);
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("method", "getCode");
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(PUBLIC_URL,bodyParamJO,"getCode",request);
			String result=resultJO.get("result").toString();
			System.out.println("==="+result);
			resultMap.put("result", result);
			//results==={"result":"d9c137a48f074cc9a2d799dfc480be2c","id":1,"jsonrpc":"2.0"}
			//results==={"id":1,"jsonrpc":"2.0","error":{"code":-2,"message":null}}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
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
	
	@RequestMapping(value="/login")
	@ResponseBody
	public Map<String, Object> login(String tenantId, String userId, String password,HttpServletRequest request){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			JSONObject paramJO=new JSONObject();
			//paramJO.put("tenantId", "ts00000006");
			paramJO.put("tenantId", tenantId);
			//paramJO.put("userId", "test001");
			paramJO.put("userId", userId);
			//paramJO.put("key", "415c9486b11c55592bfb20082e5b55184c11d3661e46f37efff7c118ab64bdda");
			String vsCode = getCode(tenantId,userId,request).get("result").toString();
			paramJO.put("key", SHA256Utils.getSHA256(tenantId+userId+password+vsCode));
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("method", "login");
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(PUBLIC_URL,bodyParamJO,"login",request);
			//bodyParamStr==={"method":"login","id":1,"jsonrpc":"2.0","params":{"tenantId":"ts00000006","userId":"test001","key":"415c9486b11c55592bfb20082e5b55184c11d3661e46f37efff7c118ab64bdda"}}
			//result==={"result":{"role":1,"staffId":null},"id":1,"jsonrpc":"2.0"}
			//result==={"id":1,"jsonrpc":"2.0","error":{"code":-2,"message":"ts00000006: code miss"}}
			System.out.println("resultJO==="+resultJO.toString());
			JSONObject resJO=(JSONObject)resultJO.get("result");
			System.out.println(">>>=="+resultJO.get("result"));
			resultMap.put("role", resJO.get("role"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取用户列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getUsers")
	@ResponseBody
	public Map<String, Object> getUsers(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getUsers");
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getUsers",request);
			System.out.println("getUsers:resultJO==="+resultJO.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}
	
	/**
	 * 获取定位设备类型表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getDeviceTypes")
	@ResponseBody
	public Map<String, Object> getDeviceTypes(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getDeviceTypes");
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getDeviceTypes",request);
			System.out.println("getDeviceTypes:resultJO==="+resultJO.toString());
			/*
			 * {"result":[
			 * {"css":"","icon":"sub-menu-icon6","name":"汇聚网关","id":"BTG",
			  	"fields":[
					 {"mode":"","default":3600000,"name":"超时值","id":"overtime","type":"double"},
					 {"mode":"w","name":"网关号","id":"labelId","type":"double"},
					 {"mode":"e","name":"状态","expr":"(time+overtime) > new Date()","id":"online","type":"bool",
					 "list":[
						 {"html":"在线","value":true},
						 {"html":"离线","value":false}
					 ]
				 	},
				 	{"mode":"r","name":"最近激活时间","id":"time","type":"datetime"}
			 	],
			 	engineMask":255},
			 	{"css":"","icon":"sub-menu-icon6","name":"定位器","id":"BTI",
				 	"fields":[
					 	{"mode":"e","name":"电量","expr":"(volt == null) ? \"\" : ((volt >= 2700) ? (volt / 1000 + \"V\") : (\"<span style= \\\"color:red;\\\">\" + (volt / 1000) + \"V(低压)<\/span>\"))","id":"volt","type":"string"},
					 	{"mode":"w","name":"信标号","id":"labelId","type":"double"},
					 	{"mode":"","default":0,"name":"x轴","id":"x","type":"double"},
					 	{"mode":"","default":0,"name":"y轴","id":"y","type":"double"},
					 	{"mode":"","default":0,"name":"z轴","id":"z","type":"double"},
					 	{"mode":"w","default":86400000,"name":"超时值","id":"overtime","type":"double"},
					 	{"mode":"e","name":"状态","expr":"(time+overtime) > new Date()","id":"online","type":"bool",
						 	"list":[
							 	{"html":"在线","value":true},
							 	{"html":"离线","value":false}
						 	]
					 	},
					 	{"mode":"r","name":"最近激活时间","id":"time","type":"datetime"}
				 	],
			 	"engineMask":2},
			 	{"css":"","icon":"sub-menu-icon6","name":"通讯中继","id":"BTR","fields":[{"mode":"w","default":3600000,"name":"超时值","id":"overtime","type":"double"},{"mode":"e","name":"状态","expr":"(time+overtime) > new Date()","id":"online","type":"bool","list":[{"html":"在线","value":true},{"html":"离线","value":false}]},{"mode":"r","name":"最近激活时间","id":"time","type":"datetime"}],"engineMask":2},{"css":"","icon":"sub-menu-icon6","name":"闸机","id":"GAT","fields":[{"mode":"w","name":"名称","id":"name","type":"string"}],"engineMask":255},{"name":"指示牌","id":"LAB","engineMask":255},{"css":"","icon":"sub-menu-icon6","name":"监控摄像头","id":"SXT","fields":[{"mode":"w","name":"摄像头编号","id":"labelId","type":"double"}],"engineMask":255}],"id":1,"jsonrpc":"2.0"}
			 * */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}
	
	/**
	 *  获取定位标签类型表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getTagTypes")
	@ResponseBody
	public Map<String, Object> getTagTypes(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getTagTypes");
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getTagTypes",request);
			System.out.println("getTagTypes:resultJO==="+resultJO.toString());
			/*
			 {"result":[
				 {"name":"普通型","id":"BTT01",
					 "fields":[
						 {"mode":"e","name":"电量","expr":"(volt == null) ? \"\" : ((volt >= 3500) ? (volt / 1000 + \"V\") : (\"<span style= \\\"color:red;\\\">\" + (volt / 1000) + \"V(低压)<\/span>\"))","id":"volt","type":"string"},
						 {"mode":"","name":"激活时间","id":"time","type":"datetime"},
						 {"mode":"w","default":8.64E7,"name":"超时值","id":"overtime","type":"double"},
						 {"mode":"e","name":"状态","expr":"(time+overtime) <new Date()","id":"online","type":"bool",
							 "list":[
								 {"html":"在线","value":true},
								 {"html":"离线","value":false}
							 ]
						 },
						 {"mode":"r","name":"运动状态","id":"slient","type":"bool",
							 "list":[
								 {"html":"静止","value":true},
								 {"html":"","value":false}
							 ]
						 }
					 ]
				 },
				 {"name":"车载型","id":"BTT02",
					 "fields":[
						 {"mode":"e","name":"电量","expr":"(volt == null) ? \"\" : ((volt >= 3500) ? (volt / 1000 + \"V\") : (\"<span style= \\\"color:red;\\\">\" + (volt / 1000) + \"V(低压)<\/span>\"))","id":"volt","type":"string"},
						 {"mode":"","name":"激活时间","id":"time","type":"datetime"},
						 {"mode":"w","default":8.64E7,"name":"超时值","id":"overtime","type":"double"},
						 {"mode":"e","name":"状态","expr":"(time+overtime) <new Date()","id":"online","type":"bool",
							 "list":[
								 {"html":"在线","value":true},
								 {"html":"离线","value":false}
							 ]
						 },
						 {"mode":"r","name":"运动状态","id":"slient","type":"bool",
							 "list":[
								 {"html":"静止","value":true},
								 {"html":"","value":false}
							 ]
						 }
					 ]
				 }
			 ],"id":1,"jsonrpc":"2.0"}
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取定位标签信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getTag")
	@ResponseBody
	public Map<String, Object> getTag(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getTag");
			JSONObject paramJO=new JSONObject();
			paramJO.put("tagId", "BTT32004761");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getTag",request);
			System.out.println("getTag:resultJO==="+resultJO.toString());
			//{"result":{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004761","userId":"4761","engineMask":1},"id":1,"jsonrpc":"2.0"}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取系统实体类型
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getEntityTypes")
	@ResponseBody
	public Map<String, Object> getEntityTypes(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getEntityTypes");
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getEntityTypes",request);
			System.out.println("getEntityTypes:resultJO==="+resultJO.toString());
			/*{"result":[
				{"css":"","icon":"sub-menu-icon6","name":"人员","id":"staff",
					"fields":[
						{"mode":"w","name":"name","type":"text","title":"名称",
							"validate":[
								{"validator":"required","message":"名称未填写"}
							]
						},
						{"mode":"w","sorting":false,"name":"sex","type":"select","valueField":"id","title":"性别",
							"items":[
								{"name":"男","id":1},
								{"name":"女","id":2}
							],
						"filtering":false,"textField":"name"},
						{"mode":"w","sorting":false,"name":"age","type":"text","title":"年龄"},
						{"mode":"w","sorting":false,"name":"phone","type":"text","title":"电话"},
						{"mode":"w","name":"pid","type":"text","title":"工号",
							"validate":[
								{"validator":"required","message":"工号未填写"}
							]
						},
						{"mode":"w","name":"post","type":"text","title":"岗位"}
					]
				},
				{"css":"","icon":"sub-menu-icon6","name":"其他","id":"other",
					"fields":[
						{"mode":"w","name":"name","type":"text","title":"名称",
							"validate":[
								{"validator":"required","message":"名称未填写"}
							]
						},
						{"mode":"w","name":"pid","type":"text","title":"编号",
							"validate":[
								{"validator":"required","message":"编号未填写"}
							]
						}
					]
				},
				{"css":"","icon":"sub-menu-icon6","name":"车辆","id":"car",
					"fields":[
						{"mode":"w","name":"name","type":"text","title":"名称",
							"validate":[
								{"validator":"required","message":"名称未填写"}
							]
						},
						{"mode":"w","name":"pid","type":"text","title":"车牌号",
							"validate":[
								{"validator":"required","message":"车牌号未填写"}
							]
						},
						{"mode":"w","name":"post","type":"text","title":"司机"}
					]
				}
			],"id":1,"jsonrpc":"2.0"}
			*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	@RequestMapping(value="/getTags")
	@ResponseBody
	public Map<String, Object> getTags(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getTags");
			JSONObject paramJO=new JSONObject();
			//paramJO.put("entityType", "");
			//paramJO.put("tagType", "");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getTags",request);
			System.out.println("getTags:resultJO==="+resultJO.toString());
			/*
			 {"result":[
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004761","userId":"4761","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003760","userId":"3760","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003625","userId":"3625","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTTFCC6E902","userId":"6E902"},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003853","userId":"3853","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004903","userId":"4903","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003912","userId":"3912","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004762","userId":"4762","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004816","userId":"4816","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004695","userId":"4695","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004829","userId":"4829","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004896","userId":"4896","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003914","userId":"3914","engineMask":1},
			 {"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039771","userId":"9771","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT19103554","userId":"3554"},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004765","userId":"4765","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004759","userId":"4759","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003756","userId":"3756","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT1E7D5EB9","userId":"5EB9"},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004739","userId":"4739","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003901","userId":"3901","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004744","userId":"4744","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004773","userId":"4773","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003632","userId":"3632","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT20800186","userId":"186"},
			 {"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004669","userId":"4669"},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003823","userId":"3823","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT823DDE32","userId":"3200"},
			 {"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004662","userId":"4662"},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004778","userId":"4778","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004864","userId":"4864","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004753","userId":"4753","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004857","userId":"4857","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004813","userId":"4813","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003840","userId":"3840","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004885","userId":"4885","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003699","userId":"3699","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004758","userId":"4758","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039759","userId":"9759","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039755","userId":"9755","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004921","userId":"4921"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003639","userId":"3639","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004771","userId":"4771","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004763","userId":"4763","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003629","userId":"3629","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004681","userId":"4681"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003637","userId":"3637","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004670","userId":"4670"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004769","userId":"4769","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004776","userId":"4776","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004779","userId":"4779","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003811","userId":"3811","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003929","userId":"3929","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT36003627","userId":"36003627"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT19122156","userId":"2156"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT32003627","userId":"32003627","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004790","userId":"4790","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003697","userId":"3697","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004835","userId":"4835","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003650","userId":"3650","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003906","userId":"3906","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003723","userId":"3723","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003761","userId":"3761","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004772","userId":"4772","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003687","userId":"3687","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004660","userId":"4660"},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004688","userId":"4688"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003715","userId":"3715","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039762","userId":"9762","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004802","userId":"4802","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039754","userId":"9754","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003882","userId":"3882","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004807","userId":"4807","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003766","userId":"3766","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003892","userId":"3892","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003671","userId":"3671","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003724","userId":"3724","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003741","userId":"3741","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003708","userId":"3708","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004831","userId":"4831","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003729","userId":"3729","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003670","userId":"3670","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003624","userId":"3624","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003673","userId":"3673","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003872","userId":"3872","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003716","userId":"3716","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004691","userId":"4691","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004674","userId":"4674"},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004656","userId":"4656"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004793","userId":"4793","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003759","userId":"3759","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004791","userId":"4791","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003726","userId":"3726","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004808","userId":"4808","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004810","userId":"4810","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003690","userId":"3690","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003739","userId":"3739","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004803","userId":"4803","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003751","userId":"3751","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004817","userId":"4817","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004811","userId":"4811","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004851","userId":"4851","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003736","userId":"3736","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003744","userId":"3744","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003702","userId":"3702","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003870","userId":"3870","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003694","userId":"3694","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003612","userId":"3612","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004833","userId":"4833","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004814","userId":"4814","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003743","userId":"3743","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004848","userId":"4848","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT32004677","userId":"4677"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003727","userId":"3727","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003781","userId":"3781","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003686","userId":"3686","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004800","userId":"4800","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004751","userId":"4751","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003609","userId":"3609","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003731","userId":"3731","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004824","userId":"4824","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003710","userId":"3710","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004768","userId":"4768","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004840","userId":"4840","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004780","userId":"4780","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003819","userId":"3819","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000016","userId":"0016","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003676","userId":"3676","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003821","userId":"3821","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003651","userId":"3651","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003746","userId":"3746","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003869","userId":"3869","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004743","userId":"4743","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004678","userId":"4678"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004692","userId":"4692","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004747","userId":"4747","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004855","userId":"4855","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004679","userId":"4679"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003907","userId":"3907","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004887","userId":"4887","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004854","userId":"4854","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003678","userId":"3678","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004663","userId":"4663"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004877","userId":"4877","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003807","userId":"3807","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004730","userId":"4730","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003665","userId":"3665","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004777","userId":"4777","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004694","userId":"4694","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039774","userId":"9774","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004716","userId":"4716","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003685","userId":"3685","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004910","userId":"4910","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004874","userId":"4874","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004880","userId":"4880","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004789","userId":"4789","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004702","userId":"4702","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004757","userId":"4757","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004863","userId":"4863","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004745","userId":"4745","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004668","userId":"4668"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004886","userId":"4886","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003730","userId":"3730","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004893","userId":"4893","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003622","userId":"3622","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004736","userId":"4736","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004908","userId":"4908","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004909","userId":"4909","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003703","userId":"3703","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003844","userId":"3844","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003899","userId":"3899","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003769","userId":"3769","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004693","userId":"4693","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003696","userId":"3696","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004754","userId":"4754","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004861","userId":"4861","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004822","userId":"4822","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004849","userId":"4849","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004731","userId":"4731","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004667","userId":"4667"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004746","userId":"4746","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004687","userId":"4687"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003603","userId":"3603","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004719","userId":"4719","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003860","userId":"3860","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039792","userId":"9792","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004795","userId":"4795","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004682","userId":"4682"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003645","userId":"3645","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039772","userId":"9772","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004870","userId":"4870","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003654","userId":"3654","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004659","userId":"4659"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003605","userId":"3605","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003662","userId":"3662","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003704","userId":"3704","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004704","userId":"4704","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004871","userId":"4871","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003846","userId":"3846","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003925","userId":"3925","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003852","userId":"3852","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004683","userId":"4683"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004740","userId":"4740","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003763","userId":"3763","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003941","userId":"3941","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003610","userId":"3610","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003709","userId":"3709","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003631","userId":"3631","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004901","userId":"4901","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004684","userId":"4684"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004830","userId":"4830","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004828","userId":"4828","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004708","userId":"4708","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003913","userId":"3913","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004726","userId":"4726","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004725","userId":"4725","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003613","userId":"3613","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003664","userId":"3664","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004862","userId":"4862","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004713","userId":"4713","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003802","userId":"3802","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003717","userId":"3717","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004676","userId":"4676"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004792","userId":"4792","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004685","userId":"4685"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003752","userId":"3752","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004732","userId":"4732","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039769","userId":"9769","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003636","userId":"3636","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003866","userId":"3866","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004767","userId":"4767","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003722","userId":"3722","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039766","userId":"9766","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003828","userId":"3828","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003787","userId":"3787","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004847","userId":"4847","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004797","userId":"4797","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004774","userId":"4774","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003682","userId":"3682","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004839","userId":"4839","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003816","userId":"3816","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003728","userId":"3728","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004895","userId":"4895","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003649","userId":"3649","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003754","userId":"3754","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003652","userId":"3652","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003920","userId":"3920","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003655","userId":"3655","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003758","userId":"3758","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003923","userId":"3923","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004876","userId":"4876","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003864","userId":"3864","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003930","userId":"3930","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003894","userId":"3894","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003755","userId":"3755","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003659","userId":"3659","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003784","userId":"3784","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003734","userId":"3734","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003667","userId":"3667","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003711","userId":"3711","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004841","userId":"4841","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003712","userId":"3712","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004755","userId":"4755","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003738","userId":"3738","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003648","userId":"3648","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003656","userId":"3656","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003797","userId":"3797","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003850","userId":"3850","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003745","userId":"3745","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004782","userId":"4782","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004867","userId":"4867","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003862","userId":"3862","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003719","userId":"3719","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004749","userId":"4749","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000017","userId":"0017","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004672","userId":"4672"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003858","userId":"3858","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004752","userId":"4752","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004658","userId":"4658"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004783","userId":"4783","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004733","userId":"4733","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004661","userId":"4661"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003748","userId":"3748","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004712","userId":"4712","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003641","userId":"3641","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004722","userId":"4722","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003776","userId":"3776","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004865","userId":"4865","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004869","userId":"4869","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003642","userId":"3642","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004748","userId":"4748","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003871","userId":"3871","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003742","userId":"3742","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003845","userId":"3845","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003749","userId":"3749","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003668","userId":"3668","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003928","userId":"3928","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004699","userId":"4699","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003782","userId":"3782","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003643","userId":"3643","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003691","userId":"3691","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003607","userId":"3607","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004703","userId":"4703","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004853","userId":"4853","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004721","userId":"4721","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003767","userId":"3767","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004836","userId":"4836","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004832","userId":"4832","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003900","userId":"3900","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004785","userId":"4785","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004890","userId":"4890","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003943","userId":"3943","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004717","userId":"4717","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003897","userId":"3897","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003783","userId":"3783","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003945","userId":"3945","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003835","userId":"3835","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004796","userId":"4796","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004698","userId":"4698","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004728","userId":"4728","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003772","userId":"3772","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003706","userId":"3706","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003660","userId":"3660","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003672","userId":"3672","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003679","userId":"3679","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004821","userId":"4821","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003937","userId":"3937","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003681","userId":"3681","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004823","userId":"4823","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004905","userId":"4905","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004819","userId":"4819","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003841","userId":"3841","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003770","userId":"3770","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003611","userId":"3611","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004826","userId":"4826","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004697","userId":"4697","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004690","userId":"4690"},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004673","userId":"4673"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000013","userId":"0013","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003774","userId":"3774","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003893","userId":"3893","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004715","userId":"4715","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003778","userId":"3778","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003775","userId":"3775","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003873","userId":"3873","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003747","userId":"3747","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003606","userId":"3606","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003688","userId":"3688","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003805","userId":"3805","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003617","userId":"3617","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003904","userId":"3904","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004881","userId":"4881","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003813","userId":"3813","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004720","userId":"4720","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003837","userId":"3837","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003809","userId":"3809","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004664","userId":"4664"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004711","userId":"4711","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003640","userId":"3640","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003733","userId":"3733","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004801","userId":"4801","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004845","userId":"4845","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004741","userId":"4741","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004883","userId":"4883","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003623","userId":"3623","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004651","userId":"4651"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003926","userId":"3926","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004900","userId":"4900","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003868","userId":"3868","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004675","userId":"4675"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004787","userId":"4787","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004718","userId":"4718","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004859","userId":"4859","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003942","userId":"3942","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003887","userId":"3887","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003938","userId":"3938","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003886","userId":"3886","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004873","userId":"4873","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003859","userId":"3859","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003889","userId":"3889","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003933","userId":"3933","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003922","userId":"3922","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004750","userId":"4750","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004879","userId":"4879","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003851","userId":"3851","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004844","userId":"4844","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039773","userId":"9773","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003684","userId":"3684","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004897","userId":"4897","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004899","userId":"4899","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003750","userId":"3750","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003856","userId":"3856","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003950","userId":"3950","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004724","userId":"4724","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039760","userId":"9760","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004838","userId":"4838","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003786","userId":"3786","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003822","userId":"3822","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004680","userId":"4680"},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004686","userId":"4686"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039756","userId":"9756","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004723","userId":"4723","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003647","userId":"3647","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003946","userId":"3946","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003804","userId":"3804","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004891","userId":"4891","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004875","userId":"4875","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003621","userId":"3621","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003601","userId":"3601","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003614","userId":"3614","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004812","userId":"4812","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003857","userId":"3857","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003808","userId":"3808","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004834","userId":"4834","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004665","userId":"4665"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003680","userId":"3680","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003689","userId":"3689","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003794","userId":"3794","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003935","userId":"3935","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003817","userId":"3817","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003604","userId":"3604","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003630","userId":"3630","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004760","userId":"4760","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003833","userId":"3833","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003881","userId":"3881","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003877","userId":"3877","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004666","userId":"4666"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039781","userId":"9781","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003764","userId":"3764","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003790","userId":"3790","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003777","userId":"3777","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003628","userId":"3628","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003884","userId":"3884","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003898","userId":"3898","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003939","userId":"3939","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003698","userId":"3698","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004709","userId":"4709","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004727","userId":"4727","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003919","userId":"3919","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004671","userId":"4671"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003740","userId":"3740","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003771","userId":"3771","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003799","userId":"3799","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003615","userId":"3615","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003855","userId":"3855","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004652","userId":"4652"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004805","userId":"4805","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003880","userId":"3880","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003874","userId":"3874","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003814","userId":"3814","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003633","userId":"3633","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003927","userId":"3927","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003768","userId":"3768","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003827","userId":"3827","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003773","userId":"3773","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003888","userId":"3888","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003677","userId":"3677","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003725","userId":"3725","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003849","userId":"3849","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003902","userId":"3902","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003635","userId":"3635","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003793","userId":"3793","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003917","userId":"3917","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004892","userId":"4892","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004657","userId":"4657"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003944","userId":"3944","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003720","userId":"3720","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003915","userId":"3915","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004884","userId":"4884","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003842","userId":"3842","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003638","userId":"3638","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003663","userId":"3663","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003824","userId":"3824","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004770","userId":"4770","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003701","userId":"3701","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003918","userId":"3918","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003646","userId":"3646","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003879","userId":"3879","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003891","userId":"3891","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003947","userId":"3947","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039793","userId":"9793","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003932","userId":"3932","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003620","userId":"3620","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000022","userId":"0022","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003798","userId":"3798","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003810","userId":"3810","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003779","userId":"3779","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003801","userId":"3801","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003803","userId":"3803","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000007","userId":"0007"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003885","userId":"3885","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004894","userId":"4894","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003666","userId":"3666","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003812","userId":"3812","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003829","userId":"3829","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003839","userId":"3839","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004784","userId":"4784","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003815","userId":"3815","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003675","userId":"3675","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004837","userId":"4837","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004889","userId":"4889","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003602","userId":"3602","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003644","userId":"3644","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003626","userId":"3626","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004689","userId":"4689"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004809","userId":"4809","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003876","userId":"3876","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003820","userId":"3820","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039765","userId":"9765","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003838","userId":"3838","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003890","userId":"3890","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003619","userId":"3619","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003692","userId":"3692","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004888","userId":"4888","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003714","userId":"3714","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003826","userId":"3826","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003737","userId":"3737","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003674","userId":"3674","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004878","userId":"4878","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003867","userId":"3867","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003936","userId":"3936","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003791","userId":"3791","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004654","userId":"4654"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003800","userId":"3800","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004806","userId":"4806","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003707","userId":"3707","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003834","userId":"3834","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003878","userId":"3878","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003718","userId":"3718","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003875","userId":"3875","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003861","userId":"3861","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003910","userId":"3910","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003789","userId":"3789","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004655","userId":"4655"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003721","userId":"3721","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003896","userId":"3896","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003785","userId":"3785","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003705","userId":"3705","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003661","userId":"3661","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003795","userId":"3795","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003657","userId":"3657","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004818","userId":"4818","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003905","userId":"3905","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003806","userId":"3806","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003916","userId":"3916","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003832","userId":"3832","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003843","userId":"3843","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003713","userId":"3713","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003792","userId":"3792","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003818","userId":"3818","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003700","userId":"3700","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003949","userId":"3949","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003934","userId":"3934","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003762","userId":"3762","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003683","userId":"3683","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003616","userId":"3616","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003765","userId":"3765","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004868","userId":"4868","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004858","userId":"4858","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004701","userId":"4701","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004820","userId":"4820","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004742","userId":"4742","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004756","userId":"4756","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003854","userId":"3854","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004804","userId":"4804","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004705","userId":"4705","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004882","userId":"4882","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004866","userId":"4866","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004825","userId":"4825","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039767","userId":"9767","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004653","userId":"4653"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000012","userId":"0012","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003909","userId":"3909","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004907","userId":"4907","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004904","userId":"4904","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003863","userId":"3863","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004788","userId":"4788","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004902","userId":"4902","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039784","userId":"9784","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039761","userId":"9761","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004850","userId":"4850","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004815","userId":"4815","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004764","userId":"4764","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003788","userId":"3788","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004799","userId":"4799","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000015","userId":"0015","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003847","userId":"3847","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039782","userId":"9782","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004827","userId":"4827","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004714","userId":"4714","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003903","userId":"3903","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000018","userId":"0018","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039763","userId":"9763","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000010","userId":"0010","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039775","userId":"9775","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039776","userId":"9776","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000011","userId":"0011","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039788","userId":"9788","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003895","userId":"3895","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039758","userId":"9758","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003865","userId":"3865","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003732","userId":"3732","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004735","userId":"4735","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004898","userId":"4898","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039783","userId":"9783","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000019","userId":"0019","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039757","userId":"9757","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003695","userId":"3695","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039768","userId":"9768","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004842","userId":"4842","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039778","userId":"9778","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004798","userId":"4798","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004734","userId":"4734","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004781","userId":"4781","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039780","userId":"9780","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004852","userId":"4852","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000023","userId":"0023","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039787","userId":"9787","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039764","userId":"9764","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004775","userId":"4775","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039786","userId":"9786","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039770","userId":"9770","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT32003940","userId":"3940"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039789","userId":"9789","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003780","userId":"3780","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000020","userId":"0020","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004856","userId":"4856","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004860","userId":"4860","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003669","userId":"3669","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039791","userId":"9791","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000014","userId":"0014","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004737","userId":"4737","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003608","userId":"3608","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000006","userId":"0006"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004766","userId":"4766","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003653","userId":"3653","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003693","userId":"3693","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003735","userId":"3735","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003796","userId":"3796","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000021","userId":"0021","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003825","userId":"3825","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003924","userId":"3924","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003931","userId":"3931","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003753","userId":"3753","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004700","userId":"4700","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004794","userId":"4794","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039790","userId":"9790","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004696","userId":"4696","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004872","userId":"4872","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003948","userId":"3948","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004710","userId":"4710","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003618","userId":"3618","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039777","userId":"9777","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003883","userId":"3883","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003848","userId":"3848","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004707","userId":"4707","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003911","userId":"3911","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003634","userId":"3634","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003908","userId":"3908","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004786","userId":"4786","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004843","userId":"4843","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003658","userId":"3658","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000024","userId":"0024","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004906","userId":"4906","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004706","userId":"4706","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003831","userId":"3831","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039785","userId":"9785","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004846","userId":"4846","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039779","userId":"9779","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003921","userId":"3921","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003830","userId":"3830","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003836","userId":"3836","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003757","userId":"3757","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004729","userId":"4729","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004738","userId":"4738","engineMask":1}],"id":1,"jsonrpc":"2.0"}
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取定位引擎配置信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getEngineConfig")
	@ResponseBody
	public Map<String, Object> getEngineConfig(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getTags");
			JSONObject paramJO=new JSONObject();
			//paramJO.put("entityType", "");
			paramJO.put("absolute", true);
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getTags",request);
			System.out.println("getEngineConfig:resultJO==="+resultJO.toString());
			/*
			 {"result":[
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004761","userId":"4761","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003760","userId":"3760","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003625","userId":"3625","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTTFCC6E902","userId":"6E902"},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003853","userId":"3853","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004903","userId":"4903","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003912","userId":"3912","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004762","userId":"4762","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004816","userId":"4816","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004695","userId":"4695","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004829","userId":"4829","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004896","userId":"4896","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003914","userId":"3914","engineMask":1},
			 {"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039771","userId":"9771","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT19103554","userId":"3554"},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004765","userId":"4765","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004759","userId":"4759","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003756","userId":"3756","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT1E7D5EB9","userId":"5EB9"},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004739","userId":"4739","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003901","userId":"3901","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004744","userId":"4744","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004773","userId":"4773","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003632","userId":"3632","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT20800186","userId":"186"},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004669","userId":"4669"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003823","userId":"3823","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT823DDE32","userId":"3200"},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004662","userId":"4662"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004778","userId":"4778","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004864","userId":"4864","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004753","userId":"4753","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004857","userId":"4857","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004813","userId":"4813","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003840","userId":"3840","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004885","userId":"4885","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003699","userId":"3699","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004758","userId":"4758","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039759","userId":"9759","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039755","userId":"9755","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004921","userId":"4921"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003639","userId":"3639","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004771","userId":"4771","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004763","userId":"4763","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003629","userId":"3629","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004681","userId":"4681"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003637","userId":"3637","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004670","userId":"4670"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004769","userId":"4769","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004776","userId":"4776","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004779","userId":"4779","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003811","userId":"3811","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003929","userId":"3929","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT36003627","userId":"36003627"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT19122156","userId":"2156"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT32003627","userId":"32003627","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004790","userId":"4790","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003697","userId":"3697","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004835","userId":"4835","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003650","userId":"3650","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003906","userId":"3906","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003723","userId":"3723","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003761","userId":"3761","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004772","userId":"4772","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003687","userId":"3687","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004660","userId":"4660"},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004688","userId":"4688"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003715","userId":"3715","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039762","userId":"9762","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004802","userId":"4802","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039754","userId":"9754","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003882","userId":"3882","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004807","userId":"4807","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003766","userId":"3766","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003892","userId":"3892","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003671","userId":"3671","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003724","userId":"3724","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003741","userId":"3741","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003708","userId":"3708","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004831","userId":"4831","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003729","userId":"3729","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003670","userId":"3670","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003624","userId":"3624","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003673","userId":"3673","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003872","userId":"3872","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003716","userId":"3716","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004691","userId":"4691","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004674","userId":"4674"},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004656","userId":"4656"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004793","userId":"4793","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003759","userId":"3759","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004791","userId":"4791","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003726","userId":"3726","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004808","userId":"4808","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004810","userId":"4810","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003690","userId":"3690","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003739","userId":"3739","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004803","userId":"4803","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003751","userId":"3751","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004817","userId":"4817","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004811","userId":"4811","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004851","userId":"4851","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003736","userId":"3736","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003744","userId":"3744","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003702","userId":"3702","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003870","userId":"3870","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003694","userId":"3694","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003612","userId":"3612","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004833","userId":"4833","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004814","userId":"4814","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003743","userId":"3743","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004848","userId":"4848","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT32004677","userId":"4677"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003727","userId":"3727","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003781","userId":"3781","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003686","userId":"3686","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004800","userId":"4800","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004751","userId":"4751","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003609","userId":"3609","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003731","userId":"3731","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004824","userId":"4824","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003710","userId":"3710","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004768","userId":"4768","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004840","userId":"4840","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004780","userId":"4780","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003819","userId":"3819","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000016","userId":"0016","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003676","userId":"3676","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003821","userId":"3821","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003651","userId":"3651","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003746","userId":"3746","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003869","userId":"3869","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004743","userId":"4743","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004678","userId":"4678"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004692","userId":"4692","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004747","userId":"4747","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004855","userId":"4855","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004679","userId":"4679"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003907","userId":"3907","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004887","userId":"4887","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004854","userId":"4854","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003678","userId":"3678","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004663","userId":"4663"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004877","userId":"4877","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003807","userId":"3807","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004730","userId":"4730","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003665","userId":"3665","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004777","userId":"4777","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004694","userId":"4694","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039774","userId":"9774","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004716","userId":"4716","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003685","userId":"3685","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004910","userId":"4910","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004874","userId":"4874","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004880","userId":"4880","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004789","userId":"4789","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004702","userId":"4702","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004757","userId":"4757","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004863","userId":"4863","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004745","userId":"4745","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004668","userId":"4668"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004886","userId":"4886","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003730","userId":"3730","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004893","userId":"4893","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003622","userId":"3622","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004736","userId":"4736","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004908","userId":"4908","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004909","userId":"4909","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003703","userId":"3703","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003844","userId":"3844","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003899","userId":"3899","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003769","userId":"3769","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004693","userId":"4693","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003696","userId":"3696","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004754","userId":"4754","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004861","userId":"4861","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004822","userId":"4822","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004849","userId":"4849","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004731","userId":"4731","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004667","userId":"4667"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004746","userId":"4746","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004687","userId":"4687"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003603","userId":"3603","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004719","userId":"4719","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003860","userId":"3860","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039792","userId":"9792","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004795","userId":"4795","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004682","userId":"4682"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003645","userId":"3645","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039772","userId":"9772","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004870","userId":"4870","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003654","userId":"3654","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004659","userId":"4659"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003605","userId":"3605","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003662","userId":"3662","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003704","userId":"3704","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004704","userId":"4704","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004871","userId":"4871","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003846","userId":"3846","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003925","userId":"3925","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003852","userId":"3852","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004683","userId":"4683"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004740","userId":"4740","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003763","userId":"3763","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003941","userId":"3941","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003610","userId":"3610","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003709","userId":"3709","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003631","userId":"3631","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004901","userId":"4901","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004684","userId":"4684"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004830","userId":"4830","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004828","userId":"4828","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004708","userId":"4708","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003913","userId":"3913","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004726","userId":"4726","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004725","userId":"4725","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003613","userId":"3613","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003664","userId":"3664","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004862","userId":"4862","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004713","userId":"4713","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003802","userId":"3802","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003717","userId":"3717","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004676","userId":"4676"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004792","userId":"4792","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004685","userId":"4685"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003752","userId":"3752","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004732","userId":"4732","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039769","userId":"9769","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003636","userId":"3636","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003866","userId":"3866","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004767","userId":"4767","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003722","userId":"3722","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039766","userId":"9766","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003828","userId":"3828","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003787","userId":"3787","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004847","userId":"4847","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004797","userId":"4797","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004774","userId":"4774","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003682","userId":"3682","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004839","userId":"4839","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003816","userId":"3816","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003728","userId":"3728","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004895","userId":"4895","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003649","userId":"3649","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003754","userId":"3754","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003652","userId":"3652","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003920","userId":"3920","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003655","userId":"3655","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003758","userId":"3758","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003923","userId":"3923","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004876","userId":"4876","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003864","userId":"3864","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003930","userId":"3930","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003894","userId":"3894","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003755","userId":"3755","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003659","userId":"3659","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003784","userId":"3784","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003734","userId":"3734","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003667","userId":"3667","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003711","userId":"3711","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004841","userId":"4841","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003712","userId":"3712","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004755","userId":"4755","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003738","userId":"3738","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003648","userId":"3648","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003656","userId":"3656","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003797","userId":"3797","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003850","userId":"3850","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003745","userId":"3745","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004782","userId":"4782","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004867","userId":"4867","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003862","userId":"3862","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003719","userId":"3719","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004749","userId":"4749","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000017","userId":"0017","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004672","userId":"4672"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003858","userId":"3858","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004752","userId":"4752","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004658","userId":"4658"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004783","userId":"4783","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004733","userId":"4733","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004661","userId":"4661"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003748","userId":"3748","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004712","userId":"4712","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003641","userId":"3641","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004722","userId":"4722","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003776","userId":"3776","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004865","userId":"4865","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004869","userId":"4869","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003642","userId":"3642","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004748","userId":"4748","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003871","userId":"3871","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003742","userId":"3742","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003845","userId":"3845","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003749","userId":"3749","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003668","userId":"3668","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003928","userId":"3928","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004699","userId":"4699","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003782","userId":"3782","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003643","userId":"3643","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003691","userId":"3691","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003607","userId":"3607","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004703","userId":"4703","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004853","userId":"4853","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004721","userId":"4721","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003767","userId":"3767","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004836","userId":"4836","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004832","userId":"4832","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003900","userId":"3900","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004785","userId":"4785","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004890","userId":"4890","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003943","userId":"3943","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004717","userId":"4717","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003897","userId":"3897","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003783","userId":"3783","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003945","userId":"3945","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003835","userId":"3835","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004796","userId":"4796","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004698","userId":"4698","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004728","userId":"4728","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003772","userId":"3772","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003706","userId":"3706","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003660","userId":"3660","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003672","userId":"3672","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003679","userId":"3679","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004821","userId":"4821","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003937","userId":"3937","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003681","userId":"3681","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004823","userId":"4823","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004905","userId":"4905","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004819","userId":"4819","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003841","userId":"3841","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003770","userId":"3770","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003611","userId":"3611","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004826","userId":"4826","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004697","userId":"4697","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004690","userId":"4690"},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004673","userId":"4673"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000013","userId":"0013","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003774","userId":"3774","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003893","userId":"3893","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004715","userId":"4715","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003778","userId":"3778","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003775","userId":"3775","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003873","userId":"3873","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003747","userId":"3747","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003606","userId":"3606","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003688","userId":"3688","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003805","userId":"3805","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003617","userId":"3617","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003904","userId":"3904","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004881","userId":"4881","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003813","userId":"3813","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004720","userId":"4720","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003837","userId":"3837","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003809","userId":"3809","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004664","userId":"4664"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004711","userId":"4711","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003640","userId":"3640","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003733","userId":"3733","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004801","userId":"4801","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004845","userId":"4845","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004741","userId":"4741","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004883","userId":"4883","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003623","userId":"3623","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004651","userId":"4651"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003926","userId":"3926","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004900","userId":"4900","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003868","userId":"3868","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004675","userId":"4675"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004787","userId":"4787","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004718","userId":"4718","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004859","userId":"4859","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003942","userId":"3942","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003887","userId":"3887","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003938","userId":"3938","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003886","userId":"3886","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004873","userId":"4873","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003859","userId":"3859","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003889","userId":"3889","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003933","userId":"3933","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003922","userId":"3922","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004750","userId":"4750","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004879","userId":"4879","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003851","userId":"3851","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004844","userId":"4844","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039773","userId":"9773","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003684","userId":"3684","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004897","userId":"4897","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004899","userId":"4899","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003750","userId":"3750","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003856","userId":"3856","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003950","userId":"3950","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004724","userId":"4724","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039760","userId":"9760","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004838","userId":"4838","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003786","userId":"3786","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003822","userId":"3822","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004680","userId":"4680"},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004686","userId":"4686"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039756","userId":"9756","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004723","userId":"4723","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003647","userId":"3647","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003946","userId":"3946","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003804","userId":"3804","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004891","userId":"4891","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004875","userId":"4875","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003621","userId":"3621","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003601","userId":"3601","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003614","userId":"3614","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004812","userId":"4812","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003857","userId":"3857","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003808","userId":"3808","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004834","userId":"4834","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004665","userId":"4665"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003680","userId":"3680","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003689","userId":"3689","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003794","userId":"3794","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003935","userId":"3935","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003817","userId":"3817","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003604","userId":"3604","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003630","userId":"3630","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004760","userId":"4760","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003833","userId":"3833","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003881","userId":"3881","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003877","userId":"3877","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004666","userId":"4666"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039781","userId":"9781","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003764","userId":"3764","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003790","userId":"3790","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003777","userId":"3777","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003628","userId":"3628","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003884","userId":"3884","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003898","userId":"3898","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003939","userId":"3939","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003698","userId":"3698","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004709","userId":"4709","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004727","userId":"4727","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003919","userId":"3919","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004671","userId":"4671"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003740","userId":"3740","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003771","userId":"3771","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003799","userId":"3799","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003615","userId":"3615","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003855","userId":"3855","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004652","userId":"4652"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004805","userId":"4805","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003880","userId":"3880","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003874","userId":"3874","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003814","userId":"3814","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003633","userId":"3633","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003927","userId":"3927","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003768","userId":"3768","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003827","userId":"3827","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003773","userId":"3773","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003888","userId":"3888","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003677","userId":"3677","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003725","userId":"3725","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003849","userId":"3849","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003902","userId":"3902","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003635","userId":"3635","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003793","userId":"3793","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003917","userId":"3917","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004892","userId":"4892","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004657","userId":"4657"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003944","userId":"3944","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003720","userId":"3720","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003915","userId":"3915","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004884","userId":"4884","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003842","userId":"3842","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003638","userId":"3638","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003663","userId":"3663","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003824","userId":"3824","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004770","userId":"4770","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003701","userId":"3701","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003918","userId":"3918","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003646","userId":"3646","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003879","userId":"3879","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003891","userId":"3891","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003947","userId":"3947","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039793","userId":"9793","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003932","userId":"3932","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003620","userId":"3620","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000022","userId":"0022","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003798","userId":"3798","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003810","userId":"3810","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003779","userId":"3779","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003801","userId":"3801","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003803","userId":"3803","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000007","userId":"0007"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003885","userId":"3885","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004894","userId":"4894","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003666","userId":"3666","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003812","userId":"3812","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003829","userId":"3829","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003839","userId":"3839","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004784","userId":"4784","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003815","userId":"3815","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003675","userId":"3675","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004837","userId":"4837","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004889","userId":"4889","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003602","userId":"3602","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003644","userId":"3644","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003626","userId":"3626","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004689","userId":"4689"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004809","userId":"4809","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003876","userId":"3876","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003820","userId":"3820","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039765","userId":"9765","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003838","userId":"3838","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003890","userId":"3890","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003619","userId":"3619","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003692","userId":"3692","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004888","userId":"4888","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003714","userId":"3714","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003826","userId":"3826","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003737","userId":"3737","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003674","userId":"3674","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004878","userId":"4878","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003867","userId":"3867","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003936","userId":"3936","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003791","userId":"3791","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004654","userId":"4654"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003800","userId":"3800","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004806","userId":"4806","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003707","userId":"3707","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003834","userId":"3834","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003878","userId":"3878","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003718","userId":"3718","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003875","userId":"3875","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003861","userId":"3861","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003910","userId":"3910","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003789","userId":"3789","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004655","userId":"4655"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003721","userId":"3721","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003896","userId":"3896","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003785","userId":"3785","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003705","userId":"3705","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003661","userId":"3661","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003795","userId":"3795","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003657","userId":"3657","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004818","userId":"4818","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003905","userId":"3905","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003806","userId":"3806","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003916","userId":"3916","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003832","userId":"3832","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003843","userId":"3843","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003713","userId":"3713","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003792","userId":"3792","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003818","userId":"3818","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003700","userId":"3700","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003949","userId":"3949","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003934","userId":"3934","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003762","userId":"3762","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003683","userId":"3683","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003616","userId":"3616","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003765","userId":"3765","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004868","userId":"4868","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004858","userId":"4858","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004701","userId":"4701","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004820","userId":"4820","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004742","userId":"4742","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004756","userId":"4756","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003854","userId":"3854","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004804","userId":"4804","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004705","userId":"4705","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004882","userId":"4882","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004866","userId":"4866","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004825","userId":"4825","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039767","userId":"9767","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004653","userId":"4653"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000012","userId":"0012","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003909","userId":"3909","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004907","userId":"4907","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004904","userId":"4904","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003863","userId":"3863","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004788","userId":"4788","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004902","userId":"4902","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039784","userId":"9784","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039761","userId":"9761","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004850","userId":"4850","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004815","userId":"4815","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004764","userId":"4764","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003788","userId":"3788","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004799","userId":"4799","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000015","userId":"0015","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003847","userId":"3847","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039782","userId":"9782","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004827","userId":"4827","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004714","userId":"4714","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003903","userId":"3903","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000018","userId":"0018","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039763","userId":"9763","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000010","userId":"0010","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039775","userId":"9775","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039776","userId":"9776","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000011","userId":"0011","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039788","userId":"9788","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003895","userId":"3895","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039758","userId":"9758","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003865","userId":"3865","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003732","userId":"3732","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004735","userId":"4735","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004898","userId":"4898","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039783","userId":"9783","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000019","userId":"0019","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039757","userId":"9757","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003695","userId":"3695","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039768","userId":"9768","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004842","userId":"4842","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039778","userId":"9778","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004798","userId":"4798","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004734","userId":"4734","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004781","userId":"4781","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039780","userId":"9780","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004852","userId":"4852","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000023","userId":"0023","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039787","userId":"9787","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039764","userId":"9764","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004775","userId":"4775","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039786","userId":"9786","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039770","userId":"9770","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT32003940","userId":"3940"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039789","userId":"9789","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003780","userId":"3780","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000020","userId":"0020","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004856","userId":"4856","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004860","userId":"4860","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003669","userId":"3669","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039791","userId":"9791","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000014","userId":"0014","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004737","userId":"4737","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003608","userId":"3608","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000006","userId":"0006"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004766","userId":"4766","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003653","userId":"3653","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003693","userId":"3693","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003735","userId":"3735","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003796","userId":"3796","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000021","userId":"0021","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003825","userId":"3825","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003924","userId":"3924","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003931","userId":"3931","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003753","userId":"3753","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004700","userId":"4700","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004794","userId":"4794","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039790","userId":"9790","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004696","userId":"4696","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004872","userId":"4872","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003948","userId":"3948","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004710","userId":"4710","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003618","userId":"3618","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039777","userId":"9777","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003883","userId":"3883","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003848","userId":"3848","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004707","userId":"4707","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003911","userId":"3911","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003634","userId":"3634","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003908","userId":"3908","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004786","userId":"4786","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004843","userId":"4843","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003658","userId":"3658","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000024","userId":"0024","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004906","userId":"4906","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004706","userId":"4706","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003831","userId":"3831","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039785","userId":"9785","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004846","userId":"4846","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039779","userId":"9779","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003921","userId":"3921","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003830","userId":"3830","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003836","userId":"3836","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003757","userId":"3757","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004729","userId":"4729","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004738","userId":"4738","engineMask":1}],"id":1,"jsonrpc":"2.0"}
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取定位点数量
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getLocationPointCount")
	@ResponseBody
	public Map<String, Object> getLocationPointCount(HttpServletRequest request) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getLocationPointCount");
			JSONObject paramJO=new JSONObject();
			paramJO.put("areaId", 0);
			//paramJO.put("deviceType", "");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getLocationPointCount",request);
			System.out.println("getLocationPointCount:resultJO==="+resultJO.toString());
			//{"result":2058,"id":1,"jsonrpc":"2.0"}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取定位点列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getLocationPoints")
	@ResponseBody
	public Map<String, Object> getLocationPoints(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getLocationPoints");
			JSONObject paramJO=new JSONObject();
			paramJO.put("areaId", 0);
			//paramJO.put("deviceType", "");
			paramJO.put("pageIndex", 0);
			paramJO.put("maxCount", 100);
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getLocationPoints",request);
			System.out.println("getLocationPoints:resultJO==="+resultJO.toString());
			/*
			{"result":[
			{"deviceType":"BTI","d":2,"deviceId":"BTI24007913","room":true,"r":10,"areaId":2,"gateId":"7913","name":"行政楼1号门","x":634.23,"y":52.24,"z":0,"id":1226,"device":{"deviceType":"BTI","blocks":["BTI240079130402"],"activeTime":1607069264586,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24007913","voltUnit":"V","labelId":7913,"volt":3600,"state":0,"id":"BTI24007913","time":"2020-12-04T16:07:44.586+0800","rootAreaId":1}},
			{"deviceType":"BTI","d":2,"deviceId":"BTI24007040","room":true,"r":3,"areaId":2,"gateId":"7040","name":"公用MCC-11","x":107.73,"y":313.3,"z":0,"id":1398,"device":{"deviceType":"BTI","blocks":["BTI24007040"],"activeTime":1607569545365,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24007040","voltUnit":"V","labelId":7040,"volt":3600,"state":0,"id":"BTI24007040","time":"2020-12-10T11:05:45.365+0800","rootAreaId":1}},
			{"deviceType":"BTI","d":2,"deviceId":"BTI24007032","room":true,"r":3,"areaId":2,"gateId":"7032","name":"公用MCC-66","x":76.76,"y":296.02,"z":0,"id":1390,"device":{"deviceType":"BTI","blocks":["BTI24007032"],"activeTime":1607584840426,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24007032","voltUnit":"V","labelId":7032,"volt":3600,"state":0,"id":"BTI24007032","time":"2020-12-10T15:20:40.426+0800","rootAreaId":1}},
			{"deviceType":"BTI","d":1,"deviceId":"BTI22085250","room":false,"out":true,"r":8,"areaId":2,"labelId":5250,"name":"","x":685.74,"y":100.39,"z":0,"id":2208,"radius":"10"},
			{"deviceType":"BTI","d":2,"deviceId":"BTI24007890","room":true,"r":1,"areaId":2,"gateId":"7890","name":"联合仓库北1","x":499.95,"y":35.49,"z":0,"id":1242,"device":{"deviceType":"BTI","blocks":["BTI24007890"],"activeTime":1607928858070,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24007890","voltUnit":"V","labelId":7890,"volt":3550,"state":0,"id":"BTI24007890","time":"2020-12-14T14:54:18.070+0800","rootAreaId":1}},
			{"deviceType":"BTI","d":2,"deviceId":"BTI24007847","room":true,"r":5,"areaId":2,"gateId":"7847","name":"远东控制室南门","x":428.53,"y":450.69,"z":0,"id":1207,"device":{"deviceType":"BTI","blocks":["BTI24007847"],"activeTime":1608009879485,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24007847","voltUnit":"V","labelId":7847,"volt":3650,"state":0,"id":"BTI24007847","time":"2020-12-15T13:24:39.485+0800","rootAreaId":1}},
			{"deviceType":"BTI","d":2,"deviceId":"BTI24007837","room":true,"r":5,"areaId":2,"gateId":"7837","name":"远东联控制室西门","x":409.89,"y":474.47,"z":0,"id":1204,"device":{"deviceType":"BTI","blocks":["BTI24007837"],"activeTime":1607996261601,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24007837","voltUnit":"V","labelId":7837,"volt":3600,"state":0,"id":"BTI24007837","time":"2020-12-15T09:37:41.601+0800","rootAreaId":1}},{"deviceType":"BTI","r":8,"areaId":2,"d":0,"x":644.99,"y":33,"z":0,"id":2223,"deviceId":"BTI22085243","room":false},{"deviceType":"BTI","d":2,"deviceId":"BTI22085241","room":true,"r":8,"areaId":2,"gateId":"5241","name":"食堂","x":623.37,"y":32.6,"z":0,"id":2227,"radius":"10"},{"deviceType":"BTI","r":10,"areaId":2,"labelId":6359,"d":0,"name":"","x":368.77,"y":476.55,"z":0,"id":608,"deviceId":"BTI24006359","room":false},{"deviceType":"BTI","d":2,"deviceId":"BTI24007033","room":true,"r":3,"areaId":2,"gateId":"7033","name":"公用MCC-22","x":75.13,"y":307.68,"z":0,"id":1391,"device":{"deviceType":"BTI","blocks":["BTI24007033"],"activeTime":1607567672641,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24007033","voltUnit":"V","labelId":7033,"volt":3600,"state":0,"id":"BTI24007033","time":"2020-12-10T10:34:32.641+0800","rootAreaId":1}},{"deviceType":"BTI","d":2,"deviceId":"BTI24007819","room":true,"r":5,"areaId":2,"gateId":"7819","name":"远东联变电所-88","x":59.77,"y":621.44,"z":0,"id":111,"device":{"deviceType":"BTI","blocks":["BTI24007819"],"activeTime":1607587126490,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24007819","voltUnit":"V","labelId":7819,"volt":3600,"state":0,"id":"BTI24007819","time":"2020-12-10T15:58:46.490+0800","rootAreaId":1}},{"deviceType":"BTI","r":8,"areaId":2,"d":0,"x":642.84,"y":25.54,"z":0,"id":2224,"deviceId":"BTI22085246","room":false},{"deviceType":"BTI","r":0,"areaId":4,"d":0,"name":"","x":209.98,"y":455.51,"z":0,"id":550,"deviceId":"BTI24007341","room":false},{"deviceType":"BTI","d":2,"deviceId":"BTI24007824","room":true,"r":6,"areaId":2,"labelId":7824,"gateId":"7824","name":"远东联变电所-55","x":68.06,"y":644.02,"z":0,"id":117,"radius":"10"},{"deviceType":"BTI","r":0,"areaId":4,"d":0,"name":"","x":33.32,"y":474.14,"z":0,"id":478,"deviceId":"BTI24007018","room":false},{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006628","room":false,"r":0,"areaId":2,"labelId":6628,"name":6628,"x":485.12,"y":562,"z":0,"id":670},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":222.59,"y":46.98,"z":0,"id":264,"deviceId":"BTI24006876","room":false},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":210.51,"y":58.41,"z":0,"id":263,"deviceId":"BTI24006875","room":false},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":234.9,"y":28.02,"z":0,"id":253,"deviceId":"BTI24006868","room":false},
			{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":121,"y":224.85,"z":0,"id":280,"deviceId":"BTR0794E313"},
			{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":644.03,"y":90.25,"z":0,"id":291,"deviceId":"BTR29E02EED"},
			{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":602.85,"y":227.39,"z":0,"id":299,"deviceId":"BTR3FBE52AE"},
			{"deviceType":"BTG","d":0,"start":"0","deviceId":"BTG396FEB21","room":false,"r":0,"areaId":1,"labelId":0,"name":"","x":500.49,"angle":"0","y":22.62,"z":0,"id":275,"radius":"0"},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":234.68,"y":46.51,"z":0,"id":265,"deviceId":"BTI24006877","room":false},
			{"deviceType":"BTI","r":0,"areaId":4,"d":0,"name":"","x":210.2,"y":463.77,"z":0,"id":549,"deviceId":"BTI24007339","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":210.32,"y":41.08,"z":0,"id":260,"deviceId":"BTI24006872","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":232.55532016920003,"y":383.0147428532,"z":0,"id":519,"deviceId":"BTI24007368","room":false},
			{"deviceType":"BTR","d":0,"deviceId":"BTR0360DBC3","room":false,"r":0,"areaId":1,"labelId":898,"name":"","x":646.51,"y":574.79,"z":0,"id":307,"radius":"10"},{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":600.43,"y":133.38,"z":0,"id":298,"deviceId":"BTRB70E581C"},{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":547.61,"y":498.25,"z":0,"id":310,"deviceId":"BTRCB8BF56C"},{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":561.3,"y":92.84,"z":0,"id":293,"deviceId":"BTR63DD1097"},{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":407.85,"y":95.22,"z":0,"id":296,"deviceId":"BTRBB8DB566"},{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":495.43,"y":92.68,"z":0,"id":294,"deviceId":"BTRD87DE688"},{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006626","room":false,"r":0,"areaId":2,"labelId":6626,"name":6626,"x":484.89,"y":543.69,"z":0,"id":668},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":482.85,"y":584.75,"z":0,"id":672,"deviceId":"BTI24006630","room":false},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"x":124.4685481588,"y":515.2444858548,"z":0,"id":178,"deviceId":"BTI24006215"},{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":424.11,"y":204.96,"z":0,"id":328,"deviceId":"BTRA31DD58A","room":false},{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":384.3,"y":340.79,"z":0,"id":312,"deviceId":"BTR26208768"},{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":176.05,"y":341.44,"z":0,"id":323,"deviceId":"BTR9F81F8A6"},{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":115.02,"y":441.66,"z":0,"id":324,"deviceId":"BTREA925B22"},
			{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":518.93,"y":244.05,"z":0,"id":325,"deviceId":"BTR415CAD3E"},
			{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":449.99,"y":288.61,"z":0,"id":326,"deviceId":"BTR90C49633"},
			{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":428.26,"y":430.85,"z":0,"id":329,"deviceId":"BTR2FBCFFE0"},
			{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":521.92,"y":304.45,"z":0,"id":314,"deviceId":"BTRBA0A656C"},
			{"deviceType":"BTI","r":0,"areaId":3,"d":0,"name":"","x":197.64,"y":472.59,"z":0,"id":544,"deviceId":"BTI24007334","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":210.46,"y":51.89,"z":0,"id":262,"deviceId":"BTI24006874","room":false},
			{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":698.99,"y":411.94,"z":0,"id":304,"deviceId":"BTR02913926"},
			{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006631","room":false,"r":0,"areaId":2,"labelId":6631,"name":6631,"x":492.65,"y":578.35,"z":0,"id":673},
			{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006496","room":false,"r":0,"areaId":2,"labelId":6496,"name":6496,"x":103.9,"y":209.43,"z":0,"id":383},
			{"deviceType":"BTI","r":0,"areaId":5,"d":0,"name":"","x":196.74,"y":467.9,"z":0,"id":556,"deviceId":"BTI24007345","room":false},
			{"deviceType":"BTI","r":0,"areaId":5,"d":0,"name":"","x":200.94,"y":451.2,"z":0,"id":559,"deviceId":"BTI24007348","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":294.82,"y":433.19,"z":0,"id":561,"deviceId":"BTI24007402","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":303.94,"y":432.94,"z":0,"id":562,"deviceId":"BTI24007403","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"labelId":6211,"d":0,"name":6211,"icon":"/position/images/entityType/BTI.png","x":124.88,"y":468.4,"z":0,"id":171,"deviceId":"BTI24006211"},{"deviceType":"BTI","r":0,"areaId":3,"d":0,"name":"","x":250.51,"y":434.17,"z":0,"id":2020,"deviceId":"BTI24007558","room":false},{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006580","room":false,"r":0,"areaId":2,"labelId":6580,"name":6580,"x":79.44983320790001,"y":661.2322046255,"z":0,"id":1078},{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006285","room":false,"r":0,"areaId":2,"labelId":6285,"name":6285,"x":705.52,"y":623.93,"z":0,"id":701},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":75.1362029704,"y":587.4251563657,"z":0,"id":109,"deviceId":"BTI24007816","room":false},{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006284","room":false,"r":0,"areaId":2,"labelId":6284,"name":6284,"x":705.7,"y":634.82,"z":0,"id":702},{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006291","room":false,"r":0,"areaId":2,"labelId":6291,"name":6291,"x":705.65,"y":545.19,"z":0,"id":709},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":655.93,"y":137.12,"z":0,"id":982,"deviceId":"BTI24006379","room":false},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":664.19,"y":144.91,"z":0,"id":983,"deviceId":"BTI24006380","room":false},{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":521.17,"y":14.94,"z":0,"id":2026,"deviceId":"BTR20269241"},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":687.89,"y":168.61,"z":0,"id":986,"deviceId":"BTI24006320","room":false},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":672.69,"y":153.53,"z":0,"id":984,"deviceId":"BTI24006366","room":false},{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":9.47,"y":333.79,"z":0,"id":2027,"deviceId":"BTR5398BF20","room":false},{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006592","room":false,"r":0,"areaId":2,"labelId":6592,"name":6592,"x":22.7,"y":575.05,"z":0,"id":1024},{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006567","room":false,"r":0,"areaId":2,"labelId":6567,"name":6567,"x":8.07,"y":596.28,"z":0,"id":1065},{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006569","room":false,"r":0,"areaId":2,"labelId":6569,"name":6569,"x":8.04,"y":618.69,"z":0,"id":1067},{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006568","room":false,"r":0,"areaId":2,"labelId":6568,"name":6568,"x":8.07,"y":606.22,"z":0,"id":1066},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":471.31,"y":369.62,"z":0,"id":1219,"deviceId":"BTI24007859","room":false},{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24007628","room":false,"r":0,"areaId":2,"labelId":7628,"name":7628,"x":493.7,"y":202.79,"z":0,"id":1596},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"x":39.05,"y":28.31,"z":0,"id":2151,"deviceId":null},{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":608.21,"y":486.45,"z":0,"id":1172,"deviceId":"BTI24007782","room":false},{"deviceType":"BTI","r":1,"areaId":3,"d":0,"name":"","x":545.9,"y":165.95,"z":0,"id":1998,"device":{"deviceType":"BTI","labelId":7954,"id":"BTI240079540402","rootAreaId":1,"deviceId":"BTI240079540402"},"deviceId":"BTI24007954","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":610.05,"y":446.19,"z":0,"id":1173,"deviceId":"BTI24007783","room":false},
			{"deviceType":"BTI","r":1,"areaId":3,"d":0,"name":"","x":545.9,"y":141.82,"z":0,"id":1997,"device":{"deviceType":"BTI","labelId":7953,"id":"BTI240079530402","rootAreaId":1,"deviceId":"BTI240079530402"},"deviceId":"BTI24007953","room":false},
			{"deviceType":"BTI","r":1,"areaId":3,"d":0,"name":"","x":569.67,"y":141.94,"z":0,"id":2000,"device":{"deviceType":"BTI","labelId":7956,"id":"BTI240079560402","rootAreaId":1,"deviceId":"BTI240079560402"},"deviceId":"BTI24007956","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":560.39,"y":359.96,"z":0,"id":1184,"deviceId":"BTI24007794","room":false},
			{"deviceType":"BTI","r":0,"areaId":3,"d":0,"name":"","x":430.79,"y":45.08,"z":0,"id":1257,"deviceId":"BTI24007761","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":423.61,"y":75.09,"z":0,"id":1250,"deviceId":"BTI24007898","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":385.45,"y":42.67,"z":0,"id":1252,"deviceId":"BTI24007757","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":445.71,"y":70.51,"z":0,"id":1249,"deviceId":"BTI24007897","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","icon":"/position/images/entityType/BTI.png","x":490.63,"y":47.37,"z":0,"id":1244,"deviceId":"BTI24007892","room":false},
			{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24007199","room":false,"r":0,"areaId":2,"labelId":7199,"name":7199,"x":14.36,"y":50.12,"z":0,"id":1305},
			{"deviceType":"BTI","r":1,"areaId":3,"labelId":1490,"d":0,"name":"","x":588.66,"y":379.46,"z":0,"id":2086,"deviceId":"BTI24011490","room":false},
			{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24007198","room":false,"r":0,"areaId":2,"labelId":7198,"name":7198,"x":29.19,"y":47.93,"z":0,"id":1303},
			{"deviceType":"BTI","d":0,"deviceId":"BTI24007872","room":false,"r":3,"areaId":2,"labelId":7872,"name":"","x":456.59,"y":316.75,"z":0,"id":1275,"radius":"10"},
			{"deviceType":"BTI","r":0,"areaId":3,"d":0,"name":"","x":53.17,"y":74.22,"z":0,"id":1353,"deviceId":"BTI24007159","room":false},
			{"deviceType":"BTI","r":0,"areaId":3,"d":0,"name":"","icon":"/position/images/entityType/BTI.png","x":41.61,"y":81.34,"z":0,"id":1355,"deviceId":"BTI24007162","room":false},
			{"deviceType":"BTI","r":0,"areaId":3,"d":0,"name":"","x":59.8,"y":74.5,"z":0,"id":1352,"deviceId":"BTI24007158","room":false},
			{"deviceType":"BTI","r":0,"areaId":4,"d":0,"name":"","x":52.72,"y":89.06,"z":0,"id":1362,"deviceId":"BTI24007161","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":8.272151301900001,"y":569.7465081588001,"z":0,"id":2135,"deviceId":"BTI24011539","room":false},
			{"deviceType":"BTI","r":0,"areaId":3,"d":0,"name":"","x":67.18,"y":138.24,"z":0,"id":1426,"deviceId":"BTI24007143","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":73.09,"y":150.71,"z":0,"id":1430,"deviceId":"BTI24007147","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":652.45,"y":572.85,"z":0,"id":1508,"deviceId":"BTI24006905","room":false},
			{"deviceType":"BTI","r":0,"areaId":2,"d":0,"name":"","x":608.07,"y":563.44,"z":0,"id":1532,"deviceId":"BTI24006929","room":false},
			{"deviceType":"BTI","d":0,"icon":"/position/images/entityType/BTI.png","deviceId":"BTI24006238","room":false,"r":0,"areaId":2,"labelId":6238,"name":6238,"x":17.2968223562,"y":498.6968436663,"z":0,"id":1090},
			{"deviceType":"BTR","r":0,"areaId":1,"d":0,"x":121,"y":52.25,"z":0,"id":278,"deviceId":"BTR83E5C3D3"}],"id":1,"jsonrpc":"2.0"}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取定位点（暂无数据）
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getLocationPoint")
	@ResponseBody
	public Map<String, Object> getLocationPoint(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getLocationPoint");
			JSONObject paramJO=new JSONObject();
			paramJO.put("id", 1);
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getLocationPoint",request);
			System.out.println("getLocationPoint:resultJO==="+resultJO.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取定位引擎接入点列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getEngines")
	@ResponseBody
	public Map<String, Object> getEngines(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getEngines");
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getEngines",request);
			System.out.println("getEngines:resultJO==="+resultJO.toString());
			/*
			 {"result":[
				 {"initMark":2104230190,"lastRecordId":0,"clientLogoutTime":0,"isEditing":true,"clientLoginTime":0,"id":"a1","clientVersion":0,"schemaTime":1618277921076,"open":true},
				 {"initMark":0,"lastRecordId":0,"clientLogoutTime":0,"isEditing":false,"clientLoginTime":0,"id":"a2","clientVersion":0,"schemaTime":1611898047515,"open":false}
			 ],"id":1,"jsonrpc":"2.0"}
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取根地图列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getRootAreas")
	@ResponseBody
	public Map<String, Object> getRootAreas(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getRootAreas");
			JSONObject paramJO=new JSONObject();
			//paramJO.put("engineId", "");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getRootAreas",request);
			System.out.println("getRootAreas:resultJO==="+resultJO.toString());
			/*
			{"result":[
			{
				"partitions":[
					{"fillColor":"#e33558","areaId":1,"shape":4,"name":"红色",
					"coordinates":[
						{"x":3.46,"y":664.52},
						{"x":367.37,"y":664.52},
						{"x":367.37,"y":340.13},
						{"x":3.46,"y":340.13}
					],
					"id":10003,"height":5},
					{"fillColor":"#8fc219","areaId":1,"shape":1,"coordinates":{"r":107.3,"x":608.58,"y":64.6},"name":"ceshi001","id":10002,"height":5}
				],
				"length":670.49,
				"gps":{"altitude":5.9,"latitude":32.262528,"angle2":0,"longitude":119.108227},
				"platform":{"brightness":2.6,"chart_time_out":120000},
				"parentId":-1,"path":"/sc20080092/area/area2d-1.jpg?t=1598952912791","path1":"/sc20080092/area/area3d-1.gltf?t=1598952912791",
				"name":"总图",
				"width":720.52,"x":0,"y":0,"angle":0,"z":0,"id":1,"file_2d":{},"floor":0,"file_3d":{},"engineId":"a1"}
			],"id":1,"jsonrpc":"2.0"}
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取设备及状态列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getDevices")
	@ResponseBody
	public Map<String, Object> getDevices(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getDevices");
			JSONObject paramJO=new JSONObject();
			paramJO.put("prefix", "BTI");
			paramJO.put("containState", true);
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getDevices",request);
			System.out.println("getDevices:resultJO==="+resultJO.toString());
			/*
			 {"result":[
			 {"deviceType":"BTI","blocks":["BTI24007015"],"activeTime":1619538518223,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24007015","voltUnit":"V","labelId":7015,"volt":3650,"state":0,"id":"BTI24007015","time":"2021-04-27T23:48:38.223+0800","rootAreaId":1},
			 {"deviceType":"BTI","blocks":["BTI24007632"],"activeTime":1619535291622,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24007632","voltUnit":"V","labelId":7632,"volt":3650,"state":0,"id":"BTI24007632","time":"2021-04-27T22:54:51.622+0800","rootAreaId":1},
			 {"deviceType":"BTI","blocks":["BTI24006228"],"activeTime":1619536980523,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24006228","voltUnit":"V","labelId":6228,"volt":3650,"state":0,"id":"BTI24006228","time":"2021-04-27T23:23:00.523+0800","rootAreaId":1},
			 {"deviceType":"BTI","blocks":["BTI24007532"],"activeTime":1619531894024,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24007532","voltUnit":"V","labelId":7532,"volt":3650,"state":0,"id":"BTI24007532","time":"2021-04-27T21:58:14.024+0800","rootAreaId":1},
			 {"deviceType":"BTI","blocks":["BTI24006644"],"activeTime":1619537356659,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24006644","voltUnit":"V","labelId":6644,"volt":3650,"state":0,"id":"BTI24006644","time":"2021-04-27T23:29:16.659+0800","rootAreaId":1},
			 {"deviceType":"BTI","blocks":["BTI24006914"],"activeTime":1619531663248,"rssiM":70,"rssiN":3.5,"deviceId":"BTI24006914","voltUnit":"V","labelId":6914,"volt":3650,"state":0,"id":"BTI24006914","time":"2021-04-27T21:54:23.248+0800","rootAreaId":1},
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 读取标签实时状态
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getTagStateMap")
	@ResponseBody
	public Map<String, Object> getTagStateMap(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getTagStateMap");
			JSONObject paramJO=new JSONObject();
			paramJO.put("areaId", 1);
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getTagStateMap",request);
			System.out.println("getTagStateMap:resultJO==="+resultJO.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取历史轨迹
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getLocationRecords")
	@ResponseBody
	public Map<String, Object> getLocationRecords(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getLocationRecords");
			JSONObject paramJO=new JSONObject();
			paramJO.put("tagId", "BTT34039771");
			paramJO.put("areaId", "1");
			paramJO.put("startTime", "1518277921076");
			paramJO.put("endTime", "1618277921076");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getLocationRecords",request);
			System.out.println("getLocationRecords:resultJO==="+resultJO.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 闸机记录读取
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getDoorRecord")
	@ResponseBody
	public Map<String, Object> getDoorRecord(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getDoorRecord");
			JSONObject paramJO=new JSONObject();
			//paramJO.put("pid", 0);
			paramJO.put("startTime", "1518277921076");
			paramJO.put("endTime", "1618277921076");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getDoorRecord",request);
			System.out.println("getDoorRecord:resultJO==="+resultJO.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取电子围栏列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getEnclosures")
	@ResponseBody
	public Map<String, Object> getEnclosures(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getEnclosures");
			JSONObject paramJO=new JSONObject();
			paramJO.put("areaId", "2");
			//paramJO.put("type", "1");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getEnclosures",request);
			System.out.println("getEnclosures:resultJO==="+resultJO.toString());
			/*
			 {"result":[
				 {"mode":0,"enclosureType":25,"areaId":2,"shape":4,
					 "coordinates":[
						 {"x":531.8,"y":116.28},
						 {"x":679.46,"y":116.28},
						 {"x":679.46,"y":9.23},
						 {"x":531.8,"y":9.23}
					 ],
				 	"name":"巡检1号","overtime":300,"threshold":10,"id":1},
				 {"mode":0,"enclosureType":24,"areaId":2,"shape":4,
					 "coordinates":[
						 {"x":119.08119,"y":508.03805},
						 {"x":238.56203000000002,"y":508.03805},
						 {"x":238.56203000000002,"y":336.66589999999997},
						 {"x":119.08119,"y":336.66589999999997}
					 ],
				 	"name":"1123","entitytypes":["staff"],"overtime":300,"threshold":10,"id":20},
				 {"mode":0,"enclosureType":21,"areaId":2,"shape":4,
					 "coordinates":[
						 {"x":-3.3955900000000003,"y":614.30016},
						 {"x":178.43221000000003,"y":614.30016},
						 {"x":178.43221000000003,"y":482.13828},
						 {"x":-3.3955900000000003,"y":482.13828}
					 ]
				 	,"name":"2222","entitytypes":["car"],"overtime":300,"threshold":10,"id":6},
				 {"mode":0,"enclosureType":20,"areaId":2,"shape":4,
					 "coordinates":[
						 {"x":48.79572,"y":610.93298},
						 {"x":222.20557000000002,"y":610.93298},
						 {"x":222.20557000000002,"y":475.40393},
						 {"x":48.79572,"y":475.40393}
					 ],
				 	"name":"333","entitytypes":["staff"],"overtime":300,"threshold":10,"id":7},
				 {"mode":0,"enclosureType":22,"areaId":2,"shape":4,
					 "coordinates":[
						 {"x":196.95171000000002,"y":528.43704},
						 {"x":341.74051000000003,"y":528.43704},
						 {"x":341.74051000000003,"y":430.78877},
						 {"x":196.95171000000002,"y":430.78877}
					 ],
				 	"name":"9999","entitytypes":["car"],"overtime":300,"threshold":10,"id":8}
			 	],"id":1,"jsonrpc":"2.0"}
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取电子围栏
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getEnclosure")
	@ResponseBody
	public Map<String, Object> getEnclosure(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getEnclosure");
			JSONObject paramJO=new JSONObject();
			paramJO.put("id", "1");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getEnclosure",request);
			System.out.println("getEnclosure:resultJO==="+resultJO.toString());
			/*
			 {"result":
			 	{
				 "mode":0,"enclosureType":25,"areaId":2,"shape":4,
					 "coordinates":[
						 {"x":531.8,"y":116.28},
						 {"x":679.46,"y":116.28},
						 {"x":679.46,"y":9.23},
						 {"x":531.8,"y":9.23}
					 ],
				 "name":"巡检1号","overtime":300,"threshold":10,"id":1
			 	},
			 "id":1,"jsonrpc":"2.0"}
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取电子围栏进出历史记录
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getEnclosureRecords")
	@ResponseBody
	public Map<String, Object> getEnclosureRecords(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getEnclosureRecords");
			JSONObject paramJO=new JSONObject();
			//paramJO.put("enclosureId", "");
			//paramJO.put("tagId", "");
			paramJO.put("startTime", "1518277921076");
			paramJO.put("endTime", "1618277921076");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getEnclosureRecords",request);
			System.out.println("getEnclosureRecords:resultJO==="+resultJO.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取员工职务列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getDutys")
	@ResponseBody
	public Map<String, Object> getDutys(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getDutys");
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getDutys",request);
			System.out.println("getDutys:resultJO==="+resultJO.toString());
			/*
			 {"result":[
				 {"entityType":"staff","onlineIcon":"/sc20080092/duty/onlineIcon-1.png?t=1605840646476","name":"员工","offlineIcon":"/sc20080092/duty/offlineIcon-1.png?t=1605840646476","id":1,"cnEntityType":"人员","key":1},
				 {"entityType":"car","onlineIcon":"/sc20080092/duty/onlineIcon-2.png?t=1605841283591","name":"内部车辆","offlineIcon":"/sc20080092/duty/offlineIcon-2.png?t=1605841283591","id":2,"cnEntityType":"车辆","onlineColor":"","key":2,"offlineColor":""},
				 {"entityType":"staff","onlineIcon":"/sc20080092/duty/onlineIcon--992.png?t=1605841880539","name":"危险作业人员","offlineIcon":"/sc20080092/duty/offlineIcon--992.png?t=1605841880539","id":-992,"cnEntityType":"人员","key":-992},
				 {"entityType":"staff","onlineIcon":"/sc20080092/duty/onlineIcon-5.png?t=1605857821060","name":"仪电","offlineIcon":"/sc20080092/duty/offlineIcon-5.png?t=1605857598742","id":5,"cnEntityType":"人员","onlineColor":"","key":5,"offlineColor":""},
				 {"entityType":"staff","onlineIcon":"/sc20080092/duty/onlineIcon-3.png?t=1605857827419","name":"管理人员","offlineIcon":"/sc20080092/duty/offlineIcon-3.png?t=1605840746705","id":3,"cnEntityType":"人员","onlineColor":"","key":3,"offlineColor":""},
				 {"entityType":"staff","onlineIcon":"/sc20080092/duty/onlineIcon-4.png?t=1605858310995","name":"安环","offlineIcon":"/sc20080092/duty/offlineIcon-4.png?t=1605840775158","id":4,"cnEntityType":"人员","onlineColor":"","key":4,"offlineColor":""},
				 {"entityType":"staff","onlineIcon":"/sc20080092/duty/onlineIcon-6.png?t=1605858330564","name":"EOG","offlineIcon":"/sc20080092/duty/offlineIcon-6.png?t=1605858330564","id":6,"onlineColor":"","offlineColor":""},
				 {"entityType":"car","onlineIcon":"/sc20080092/duty/onlineIcon--991.png?t=1607394653543","name":"外来车辆","offlineIcon":"/sc20080092/duty/offlineIcon--991.png?t=1607394653543","id":-991,"cnEntityType":"车辆","key":-991},
				 {"entityType":"staff","onlineIcon":"/sc20080092/duty/onlineIcon--990.png?t=1607568753403","name":"外来人员","offlineIcon":"/sc20080092/duty/offlineIcon--990.png?t=1607568753403","id":-990,"cnEntityType":"人员","key":-990}
			 ],
			 "id":1,"jsonrpc":"2.0"}
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取职务/车型/资产类别
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getDuty")
	@ResponseBody
	public Map<String, Object> getDuty(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getDuty");
			JSONObject paramJO=new JSONObject();
			paramJO.put("id", "1");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getDuty",request);
			System.out.println("getDuty:resultJO==="+resultJO.toString());
			/*
			{"result":{"entityType":"staff","onlineIcon":"/sc20080092/duty/onlineIcon-1.png?t=1605840646476","name":"员工","offlineIcon":"/sc20080092/duty/offlineIcon-1.png?t=1605840646476","id":1,"cnEntityType":"人员","key":1},"id":1,"jsonrpc":"2.0"} 
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取报警触发器列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getWarnTriggers")
	@ResponseBody
	public Map<String, Object> getWarnTriggers(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getWarnTriggers");
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getWarnTriggers",request);
			System.out.println("getWarnTriggers:resultJO==="+resultJO.toString());
			/*
			 {
			 "result":[
				 {"warnType":4,"name":"超速报警","id":-1,"enclosureId":null,"conditions":{"dutyIds":[2,-991],"maxSpeed":1000}},
				 {"warnType":1,"name":"按键报警","id":1,"enclosureId":null,"conditions":{"areaIds":[2,3,4,5,6,1]}}
			 ],
			 "id":1,"jsonrpc":"2.0"}
			 */
			resultMap=JSON.parseObject(resultJO.toString(), Map.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 获取报警触发条件
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getWarnTrigger")
	@ResponseBody
	public Map<String, Object> getWarnTrigger(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getWarnTrigger");
			JSONObject paramJO=new JSONObject();
			paramJO.put("id", "1");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getWarnTrigger",request);
			System.out.println("getWarnTrigger:resultJO==="+resultJO.toString());
			/*
			 {"result":{"warnType":1,"name":"按键报警","id":1,"enclosureId":null,"conditions":{"areaIds":[2,3,4,5,6,1]}},"id":1,"jsonrpc":"2.0"}
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 报获取警记录
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getWarnRecords")
	@ResponseBody
	public Map<String, Object> getWarnRecords(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getWarnRecords");
			JSONObject paramJO=new JSONObject();
			paramJO.put("triggerIds", "[1]");
			paramJO.put("startTime", "1618267921076");
			paramJO.put("endTime", "1618277921076");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getWarnRecords",request);
			System.out.println("getWarnRecords:resultJO==="+resultJO.toString());
			resultMap=JSON.parseObject(resultJO.toString());
			/*
			 {"result":[
			 {"tagId":"BTT32003917","warnType":1,"triggerId":1,"pid":"FUPY15028","sessionId":1740440342,"userId":"3917","keyCode":1,"uid":"BTT32003917","areaId":2,"absolute":false,"raiseTime":1605060088742,"x":39.26,"y":360.83,"z":0,"startTime":1605060088742,"id":3056,"rootAreaId":1},
			 {"tagId":"BTT32003917","warnType":1,"triggerId":1,"pid":"FUPY15028","sessionId":1740440342,"userId":"3917","keyCode":1,"uid":"BTT32003917","areaId":2,"absolute":false,"raiseTime":1605060110380,"x":39.26,"y":360.83,"z":0,"startTime":1605060110380,"id":3057,"rootAreaId":1},
			 {"tagId":"BTT32003917","warnType":1,"triggerId":1,"pid":"FUPY15028","sessionId":1740440342,"userId":"3917","keyCode":1,"uid":"BTT32003917","areaId":2,"absolute":false,"raiseTime":1605060128936,"x":39.26,"y":360.83,"z":0,"startTime":1605060128936,"id":3058,"rootAreaId":1},
			 {"tagId":"BTT32003917","warnType":1,"triggerId":1,"pid":"FUPY15028","sessionId":1740440342,"userId":"3917","keyCode":1,"uid":"BTT32003917","areaId":1,"absolute":false,"raiseTime":1605060149170,"x":39.26,"y":360.83,"z":0,"startTime":1605060149170,"id":3059,"rootAreaId":1},
			 {"tagId":"BTT32003917","warnType":1,"triggerId":1,"pid":"FUPY15028","sessionId":1740440342,"userId":"3917","keyCode":1,"uid":"BTT32003917","areaId":1,"absolute":false,"raiseTime":1605060170580,"x":39.26,"y":360.83,"z":0,"startTime":1605060170580,"id":3060,"rootAreaId":1},
			 {"tagId":"BTT32003917","warnType":1,"triggerId":1,"pid":"FUPY15028","sessionId":1740440342,"userId":"3917","keyCode":1,"uid":"BTT32003917","areaId":2,"absolute":false,"raiseTime":1605060199076,"x":89.422,"y":331.935,"z":0,"startTime":1605060199076,"id":3061,"rootAreaId":1},
			 {"tagId":"BTT32003917","warnType":1,"triggerId":1,"pid":"FUPY15028","sessionId":1740440342,"userId":"3917","keyCode":1,"uid":"BTT32003917","areaId":2,"absolute":false,"raiseTime":1605060209222,"x":116.498,"y":340.598,"z":0,"startTime":1605060209222,"id":3062,"rootAreaId":1},
			 {"tagId":"BTT32003610","warnType":1,"triggerId":1,"pid":null,"sessionId":810419356,"userId":"3610","keyCode":1,"uid":null,"areaId":2,"absolute":false,"raiseTime":1605188344346,"x":465.039,"y":181.861,"z":0,"startTime":1605188344346,"id":3145,"rootAreaId":1},
			 {"tagId":"BTT32003610","warnType":1,"triggerId":1,"pid":null,"sessionId":810419356,"userId":"3610","keyCode":1,"uid":null,"areaId":2,"absolute":false,"raiseTime":1605188354669,"x":465.172,"y":182.583,"z":0,"startTime":1605188354669,"id":3146,"rootAreaId":1},
			 {"tagId":"BTT32003610","warnType":1,"triggerId":1,"pid":null,"sessionId":810419356,"userId":"3610","keyCode":1,"uid":null,"areaId":2,"absolute":false,"raiseTime":1605188364805,"x":465.304,"y":183.314,"z":0,"startTime":1605188364805,"id":3147,"rootAreaId":1},
			 {"tagId":"BTT32003610","warnType":1,"triggerId":1,"pid":null,"sessionId":810419356,"userId":"3610","keyCode":1,"uid":null,"areaId":2,"absolute":false,"raiseTime":1605188373845,"x":465.426,"y":184.049,"z":0,"startTime":1605188373845,"id":3148,"rootAreaId":1},
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 报警记录汇总
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/summaryWarn")
	@ResponseBody
	public Map<String, Object> summaryWarn(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "summaryWarn");
			JSONObject paramJO=new JSONObject();
			paramJO.put("warnType", "1");
			paramJO.put("areaIds", "0");
			paramJO.put("cascade", "true");
			paramJO.put("year", "2021");
			paramJO.put("month", "6");
			paramJO.put("day", "10");
			//paramJO.put("zone", "");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"summaryWarn",request);
			System.out.println("summaryWarn:resultJO==="+resultJO.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}
	
	public static void main(String[] args) {
		//String s = SHA256Utils.getSHA256("ts00000006"+"test001"+"test001"+"6bc270da3ef14cc6af0f3b1ef37267a2");
		//52ac4c72590ec0d129fac7ffa3f0a2c4841875334709fbe0ac9ba65a104cc2ca
		System.out.println(DateUtil.convertLongToString(1605060088742L));
	}
	
	//https://blog.csdn.net/u013652912/article/details/108637590?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-1.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-1.control
	//https://blog.csdn.net/iteye_1124/article/details/81694031
	//https://blog.csdn.net/zz18435842675/article/details/95341010
	public JSONObject postBody(String serverURL, JSONObject bodyParamJO, String method, HttpServletRequest request)
			throws IOException {
		StringBuffer sbf = new StringBuffer(); 
		String strRead = null; 
		URL url = new URL(serverURL); 
		HttpURLConnection connection = (HttpURLConnection)url.openConnection(); 
		
		//connection.setInstanceFollowRedirects(false); 
		
		HttpSession session = request.getSession();
		if(serverURL.contains("service")) {
			//connection.setRequestProperty("Cookie", "JSESSIONID=849CB322A20324C2F7E11AD0A7A9899E;Path=/position; Domain=139.196.143.225; HttpOnly;");
			connection.setRequestProperty("Cookie", "JSESSIONID=21FA7B15231C5190C0288ACF1A86484E; Path=/position; HttpOnly");
			//connection.setRequestProperty("Cookie", session.getAttribute("Cookie").toString());
		}
		connection.setRequestMethod("POST");//请求post方式
		connection.setDoInput(true); 
		connection.setDoOutput(true); 
		//header内的的参数在这里set    
		//connection.setRequestProperty("key", "value");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.connect(); 
		
		
		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 
		//body参数放这里
		String bodyParamStr = bodyParamJO.toString();
		System.out.println("bodyParamStr==="+bodyParamStr);
		writer.write(bodyParamStr);
		//writer.write("{ \"jsonrpc\": \"2.0\", \"params\":{\"tenantId\":\"ts000000061\",\"userId\":\"test001\"}, \"method\":\"getCode\", \"id\":1 }"); 
		writer.flush();
		InputStream is = connection.getInputStream(); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 
		while ((strRead = reader.readLine()) != null) { 
			sbf.append(strRead); 
			sbf.append("\r\n"); 
		}
		reader.close(); 
		
		if(serverURL.contains("public")&&"login".equals(method)) {
			if(!checkCookieInSession(session)) {
				getCookieFromHeader(connection,session);
			}
		}
		
		connection.disconnect();
		String result = sbf.toString();
		System.out.println("result==="+result);
		JSONObject resultJO = new JSONObject(result);
		return resultJO;
	}
	
	public boolean checkCookieInSession(HttpSession session) {
		Object cookieObj = session.getAttribute("Cookie");
		if(cookieObj==null)
			return false;
		else
			return true;
	}
	
	public String getCookieFromHeader(HttpURLConnection connection,HttpSession session) {
		Map<String,List<String>> map = connection.getHeaderFields();
		for (String key : map.keySet()) {
			String value = map.get(key).get(0);
			if(value.contains("JSESSIONID=")) {
				 System.out.println("key==="+value);
				 session.setAttribute("Cookie", value);
			}
		}
		return "";
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
