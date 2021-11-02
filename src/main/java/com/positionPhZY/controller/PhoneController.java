package com.positionPhZY.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.positionPhZY.entity.*;
import com.positionPhZY.service.*;
import com.positionPhZY.utils.*;

@Controller
@RequestMapping(PhoneController.MODULE_NAME)
public class PhoneController {
	
	//http://139.196.143.225:8080/PositionPhZY/phone/goLogin
	//https://www.liankexing.com/question/825
	
	//这些接口不允许跨域访问，一律用127.0.0.1在本地搭建接口服务访问本地接口
	//private static final String PUBLIC_URL="http://127.0.0.1:8081/position/public/embeded.smd";
	private static final String PUBLIC_URL="http://124.70.38.226:8081/position/public/embeded.smd";
	//private static final String PUBLIC_URL="http://139.196.143.225:8081/position/public/embeded.smd";
	//private static final String SERVICE_URL="http://127.0.0.1:8081/position/service/embeded.smd";
	private static final String SERVICE_URL="http://124.70.38.226:8081/position/service/embeded.smd";
	//private static final String SERVICE_URL="http://139.196.143.225:8081/position/service/embeded.smd";
	private static final String HWY_URL="http://124.70.38.226:8080/PositionPhZY/phone/";
	public static final String MODULE_NAME="/phone";
	//以下这些常量是在本地接口服务不可用时，根据ip判断是本地还是远程华为云服务，若是本地，间接请求华为云服务那边的接口服务
	public static final String LOCAL_Server_NAME="localhost";
	public static final String HWY_SERVER_NAME="124.70.38.226";
	public static final String TEST_USER_Id="test001";

	@Autowired
	private AreaService areaService;
	@Autowired
	private WarnRecordService warnRecordService;
	@Autowired
	private WarnTriggerService warnTriggerService;
	@Autowired
	private LocationService locationService;
	@Autowired
	private EntityService entityService;
	@Autowired
	private TagService tagService;
	@Autowired
	private DutyService dutyService;
	@Autowired
	private DeviceTypeService deviceTypeService;
	@Autowired
	private EntityTypeService entityTypeService;
	@Autowired
	private LoginUserService loginUserService;
	@Autowired
	private LocationRecordService locationRecordService;

	@RequestMapping(value="/goPage")
	public String goPage(HttpServletRequest request) {
		String url = null;
		Map<String, Object> ccvMap = checkCookieValid(request);
		String page = request.getParameter("page");
		if("ok".equals(ccvMap.get("status").toString())) {
			url=MODULE_NAME+"/"+page;
		}
		else if("login".equals(page)){
			url=MODULE_NAME+"/login";
		}
		else if("syncDBManager".equals(page)){
			url=MODULE_NAME+"/syncDBManager";
		}
		else {
			url="redirect:goPage?page=login";
		}
		return url;
	}

	@RequestMapping(value="/initEntitySelect")
	@ResponseBody
	public Map<String, Object> initEntitySelect(String entityType) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Entity> list=entityService.querySelectData(entityType);
		
		if(list.size()==0) {
			resultMap.put("status","no");
			resultMap.put("message", "暂无数据");
		}
		else {
			resultMap.put("status","ok");
			resultMap.put("list", list);
		}
			
		return resultMap;
	}

	@RequestMapping(value="/summaryOnlineData")
	@ResponseBody
	public Map<String, Object> summaryOnlineData(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		Map<String, Object> ccvMap = checkCookieValid(request);
		if("ok".equals(ccvMap.get("status").toString())) {
			Map<String, Object> soerMap = summaryOnlineEntity(request);
			resultMap.put("entityResult", soerMap.get("result"));
				
			Map<String, Object> sodrMap = summaryOnlineDuty();
			resultMap.put("dutyResult", sodrMap);
		}
		resultMap.putAll(ccvMap);
		
		return resultMap;
	}

	@RequestMapping(value="/initTodayWarnCount")
	@ResponseBody
	public Map<String, Object> initTodayWarnCount(String todayDate, String nowTime) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Map<String, Object>> todayWarnList = new ArrayList<Map<String,Object>>();
		List<WarnTrigger> warnTriggerList = warnTriggerService.select();
		List<WarnRecord> warnRecordList = warnRecordService.select(todayDate,nowTime);
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
			todayWarnMap.put("wtName", warnTrigger.getName());
			todayWarnMap.put("warnCount", warnCount);
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
			if(!checkNameExistInList(legendDataList,wtName))
				legendDataList.add(wtName);
			String xAxisDataLabel=warnRecord.getxAxisDataLabel();
			if(!checkNameExistInList(xAxisDataLabelList,xAxisDataLabel))
				xAxisDataLabelList.add(xAxisDataLabel);
		}
		//System.out.println("legendDataList==="+legendDataList.toString());
		//System.out.println("xAxisDataLabelList==="+xAxisDataLabelList.size());
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
			Map<String, Object> itemStyleMap=new HashMap<String, Object>();
			Map<String, Object> normalMap=new HashMap<String, Object>();
			//normalMap.put("color", "#f00");
			itemStyleMap.put("normal", normalMap);
			legendDataMap.put("itemStyle", itemStyleMap);
			//itemStyle:{normal:{color:this.state.barSeriesColorList[item]}}}
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
						//System.out.println("name="+name+",xAxisDataLabel="+key);
						warnCount+=warnRecord.getWarnCount();
						//System.out.println("warnCount==="+warnCount);
						seriesDataList.set(j,warnCount);
						//System.out.println(name+",seriesDataList==="+seriesDataList.toString());
					}
				}
				Map<String, Object> itemStyleMap=(Map<String, Object>)legendDataMap.get("itemStyle");
				Map<String, Object> normalMap=(Map<String, Object>)itemStyleMap.get("normal");
				if(WarnTrigger.CHAO_SU_NAME.equals(name))
					normalMap.put("color", "#f00");
				else if(WarnTrigger.AN_JIAN_NAME.equals(name))
					normalMap.put("color", "#0f0");
			}
		}
		//System.out.println("seriesList==="+seriesList.toString());
		
		resultMap.put("legendDataList", legendDataList);
		resultMap.put("xAxisDataLabelList", xAxisDataLabelList);
		resultMap.put("seriesList", seriesList);
		
		return resultMap;
	}
	
	public boolean checkNameExistInList(List<String> legendDataList, String name) {
		boolean exist = false;
		for (String legendData : legendDataList) {
			if(name.equals(legendData)) {
				exist=true;
				break;
			}
		}
		return exist;
	}

	@RequestMapping(value="/initBJTJPieChartData")
	@ResponseBody
	public Map<String, Object> initBJTJPieChartData(String startDate,String endDate,String flag) {

		Map<String, Object> resultMap = new HashMap<String, Object>();//seriesDataList
		List<String> legendDataList=new ArrayList<String>();
		List<String> bjlxList=new ArrayList<String>();
		List<WarnRecord> warnRecordList=warnRecordService.selectPieChartData(startDate,endDate);
		for (int i = 0; i < warnRecordList.size(); i++) {
			WarnRecord warnRecord = warnRecordList.get(i);
			String areaName = warnRecord.getAreaName();
			//System.out.println("areaName==="+areaName);
			if(!checkNameExistInList(legendDataList,areaName))
				legendDataList.add(areaName);
			
			String wtName = warnRecord.getWtName();
			//System.out.println("wtName==="+wtName);
			if(!checkNameExistInList(bjlxList,wtName))
				bjlxList.add(wtName);
		}
		//System.out.println("legendDataList==="+legendDataList.toString());
		//System.out.println("bjlxList==="+bjlxList.toString());

		List<Map<String, Object>> seriesList = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < legendDataList.size(); i++) {
			Map<String, Object> seriesMap=new HashMap<>();
			seriesMap.put("name", legendDataList.get(i));
			seriesMap.put("value", 0);
			List<Map<String, Object>> seriesBjlxList = new ArrayList<Map<String, Object>>();
			for (int j = 0; j < bjlxList.size(); j++) {
				Map<String, Object> seriesBjlxMap=new HashMap<>();
				seriesBjlxMap.put("name", bjlxList.get(j));
				seriesBjlxMap.put("count", 0);
				seriesBjlxList.add(seriesBjlxMap);
			}
			seriesMap.put("bjlxList", seriesBjlxList);
			seriesList.add(seriesMap);
		}
		//System.out.println("seriesList==="+seriesList.toString());
		
		for (int i = 0; i < legendDataList.size(); i++) {
			String legendData = legendDataList.get(i);
			for (int j = 0; j < warnRecordList.size(); j++) {
				WarnRecord warnRecord = warnRecordList.get(j);
				String areaName = warnRecord.getAreaName();
				String wtName = warnRecord.getWtName();
				for (int k = 0; k < bjlxList.size(); k++) {
					if(legendData.equals(areaName)&&wtName.equals(bjlxList.get(k))) {
						Map<String, Object> seriesMap = seriesList.get(i);
						Integer value = Integer.valueOf(seriesMap.get("value").toString());
						value++;
						seriesMap.put("value", value);
						
						List<Map<String, Object>> seriesBjlxList=(List<Map<String, Object>>)seriesMap.get("bjlxList");
						Map<String, Object> seriesBjlxMap = seriesBjlxList.get(k);
						Integer count = Integer.valueOf(seriesBjlxMap.get("count").toString());
						count++;
						//System.out.println("areaName="+legendData+",value="+value+",wtName="+wtName+",count="+count);
						seriesBjlxMap.put("count", count);
					}
				}
			}
		}
		//System.out.println("seriesList1==="+seriesList.toString());
		
		resultMap.put("seriesList", seriesList);
		
		return resultMap;
	}

	@RequestMapping(value="/initSSDWLabelData")
	@ResponseBody
	public Map<String, Object> initSSDWLabelData() {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<DeviceType> deviceTypeList = deviceTypeService.select();
		List<EntityType> entityTypeList = entityTypeService.select();
		net.sf.json.JSONArray labelJA = net.sf.json.JSONArray.fromObject(deviceTypeList);
		labelJA.addAll(net.sf.json.JSONArray.fromObject(entityTypeList));
		
		for (int i = 0; i < labelJA.size(); i++) {
			net.sf.json.JSONObject labelJO = labelJA.getJSONObject(i);
			if("staff".equals(labelJO.getString("id"))) {
				labelJO.put("labelChecked", true);
			}
		}
		
		if(deviceTypeList.size()==0) {
			resultMap.put("status", "no");
			resultMap.put("message", "暂无数据");
		}
		else{
			resultMap.put("status", "ok");
			resultMap.put("list", labelJA);
		}
		return resultMap;
	}

	@RequestMapping(value="/initSSDWCanvasData")
	@ResponseBody
	public Map<String, Object> initSSDWCanvasData(Integer floor, String floorArrStr) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Location> locationList = locationService.selectSSDWCanvasData(floor,floorArrStr.split(","));
		if(locationList.size()==0) {
			resultMap.put("status", "no");
			resultMap.put("message", "暂无数据");
		}
		else{
			resultMap.put("status", "ok");
			resultMap.put("list", locationList);
		}
		return resultMap;
	}

	@RequestMapping(value="/selectEntityLocation")
	@ResponseBody
	public Map<String, Object> selectEntityLocation(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Location> locationList=locationService.selectEntityLocation(request.getParameter("entityName"));
		if(locationList==null) {
			resultMap.put("status", "no");
			resultMap.put("message", "找不到该人员");
		}
		else {
			resultMap.put("status", "ok");
			resultMap.put("list", locationList);
		}
		return resultMap;
	}
	
	@RequestMapping(value="/insertEntityData")
	@ResponseBody
	public Map<String, Object> insertEntityData(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> erMap = getEntities("staff", request);
		List<Entity> entityList = JSON.parseArray(erMap.get("result").toString(),Entity.class);
		int count=entityService.add(entityList);
		if(count==0) {
			resultMap.put("status", "no");
			resultMap.put("message", "初始化实体信息失败");
		}
		else {
			resultMap.put("status", "ok");
			resultMap.put("message", "初始化实体信息成功");
		}
		return resultMap;
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

	@RequestMapping(value="/insertTagData")
	@ResponseBody
	public Map<String, Object> insertTagData(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> trMap = getTags(request);
		List<Tag> tagList = JSON.parseArray(trMap.get("result").toString(),Tag.class);
		int count=tagService.add(tagList);
		if(count==0) {
			resultMap.put("status", "no");
			resultMap.put("message", "初始化定位标签列表失败");
		}
		else {
			resultMap.put("status", "ok");
			resultMap.put("message", "初始化定位标签列表成功");
		}
		
		return resultMap;
	}

	@RequestMapping(value="/insertLocationData")
	@ResponseBody
	public Map<String, Object> insertLocationData(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> tsrMap = getTagStateMap(request);
		com.alibaba.fastjson.JSONObject tagListMap = JSON.parseObject(tsrMap.get("result").toString());
		List<Tag> tagList = tagService.select();
		for (Tag tag : tagList) {
			String tagId = tag.getId();
			Object tagObj = tagListMap.get(tagId);
			if(tagObj!=null) {
				Location location = JSON.parseObject(tagObj.toString(), Location.class);
				//System.out.println("altitude==="+location.getAltitude());
				locationService.add(location);
			}
		}
		
		//System.out.println("tagListMap==="+tagListMap.toString());
		return resultMap;
	}

	@RequestMapping(value="/insertDutyData")
	@ResponseBody
	public Map<String, Object> insertDutyData(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> drMap = getDutys(request);
		List<Duty> dutyList = JSON.parseArray(drMap.get("result").toString(),Duty.class);
		int count=dutyService.add(dutyList);
		if(count==0) {
			resultMap.put("status", "no");
			resultMap.put("message", "初始化员工职务列表失败");
		}
		else {
			resultMap.put("status", "ok");
			resultMap.put("message", "初始化员工职务列表成功");
		}
		
		return resultMap;
	}

	@RequestMapping(value="/insertDeviceTypeData")
	@ResponseBody
	public Map<String, Object> insertDeviceTypeData(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> dtrMap = getDeviceTypes(request);
		List<DeviceType> deviceTypeList = JSON.parseArray(dtrMap.get("result").toString(),DeviceType.class);
		int count=deviceTypeService.add(deviceTypeList);
		if(count==0) {
			resultMap.put("status", "no");
			resultMap.put("message", "初始化定位设备类型表失败");
		}
		else {
			resultMap.put("status", "ok");
			resultMap.put("message", "初始化定位设备类型表成功");
		}
		
		return resultMap;
	}

	@RequestMapping(value="/insertEntityTypeData")
	@ResponseBody
	public Map<String, Object> insertEntityTypeData(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> etrMap = getEntityTypes(request);
		List<EntityType> entityTypeList = JSON.parseArray(etrMap.get("result").toString(),EntityType.class);
		int count=entityTypeService.add(entityTypeList);
		if(count==0) {
			resultMap.put("status", "no");
			resultMap.put("message", "初始化系统实体类型表失败");
		}
		else {
			resultMap.put("status", "ok");
			resultMap.put("message", "初始化系统实体类型表成功");
		}
		
		return resultMap;
	}

	@RequestMapping(value="/summaryOnlineDuty")
	@ResponseBody
	public Map<String, Object> summaryOnlineDuty() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Map<String, Object>> dutyList = dutyService.summaryOnlineDuty();
		if(dutyList.size()==0) {
			resultMap.put("status", "no");
			resultMap.put("message", "暂无数据");
		}
		else {
			resultMap.put("status", "ok");
			resultMap.put("dutyList", dutyList);
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

	/**
	 * 2.1.1 获取验证码
	 * @param tenantId
	 * @param userId
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getCode")
	@ResponseBody
	public Map<String, Object> getCode(String tenantId, String userId,HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject resultJO = null;
			/*
			if(LOCAL_Server_NAME.equals(request.getServerName())) {
				JSONObject paramJO=new JSONObject();
				//paramJO.put("tenantId", "ts00000006");
				paramJO.put("tenantId", tenantId);
				//paramJO.put("userId", "test001");
				paramJO.put("userId", userId);
				resultJO = getRespJson("getCode", paramJO);
			}
			else {
			*/
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
				resultJO = postBody(PUBLIC_URL,bodyParamJO,"getCode",request);
			//}
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
	
	/**
	 * 2.1.2 登录
	 * @param tenantId
	 * @param userId
	 * @param password
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/login")
	@ResponseBody
	public Map<String, Object> login(String tenantId, String userId, String password,HttpServletRequest request){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject resultJO = null;
			/*
			if(LOCAL_Server_NAME.equals(request.getServerName())) {
				JSONObject paramJO=new JSONObject();
				//paramJO.put("tenantId", "ts00000006");
				paramJO.put("tenantId", tenantId);
				//paramJO.put("userId", "test001");
				paramJO.put("userId", userId);
				//paramJO.put("password", "test001");
				paramJO.put("password", password);
				resultJO = getRespJson("login", paramJO);
			}
			else {
			*/
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
				resultJO = postBody(PUBLIC_URL,bodyParamJO,"login",request);
			//}
			//bodyParamStr==={"method":"login","id":1,"jsonrpc":"2.0","params":{"tenantId":"ts00000006","userId":"test001","key":"415c9486b11c55592bfb20082e5b55184c11d3661e46f37efff7c118ab64bdda"}}
			//result==={"result":{"role":1,"staffId":null},"id":1,"jsonrpc":"2.0"}
			//result==={"id":1,"jsonrpc":"2.0","error":{"code":-2,"message":"ts00000006: code miss"}}
			String resultStr = resultJO.toString();
			System.out.println("resultJO==="+resultStr);
			resultMap=JSON.parseObject(resultStr, Map.class);
			if("ok".equals(resultJO.get("status").toString())) {
				Map<String, Object> resultJOMap = (Map<String, Object>)resultMap.get("result");
				
				HttpSession session = request.getSession();
				LoginUser loginUser=(LoginUser)session.getAttribute("loginUser");
				loginUser.setUserId(userId);
				loginUser.setRole(Integer.valueOf(resultJOMap.get("role").toString()));
			}
			else {
				resultMap.put("message", "用户不存在");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	@RequestMapping(value="/exit")
	public String exit(HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		session.removeAttribute("loginUser");
		
		return MODULE_NAME+"/login";
	}

	/**
	 * 2.1.3 获取用户列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getUsers")
	@ResponseBody
	public Map<String, Object> getUsers(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject resultJO = null;
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getUsers");
			bodyParamJO.put("id", 1);
			
			/*
			if(LOCAL_Server_NAME.equals(request.getServerName())) {
				resultJO = getRespJson("getUsers", null);
			}
			else {
			*/
				resultJO = postBody(SERVICE_URL,bodyParamJO,"getUsers",request);
			//}
			/*
			 {"result":[
				 {"role":1,"userId":"admin"},
				 {"role":4,"userId":"sg001"},
				 {"role":1,"userId":"yzyd"},
				 {"role":1,"userId":"wangjie"},
				 {"role":1,"userId":"qy"},
				 {"role":1,"userId":"luzq"},
				 {"role":1,"userId":"jzp"},
				 {"role":1,"userId":"yjj"},
				 {"role":2,"userId":"ydd"},
				 {"role":2,"userId":"yyc"},
				 {"role":1,"userId":"rxw"},
				 {"role":2,"userId":"test"},
				 {"role":2,"userId":"dll"},
				 {"role":2,"userId":"lzh"},
				 {"role":28277,"userId":"visitor"},
				 {"role":28277,"userId":"jx01"},
				 {"role":1,"userId":"test001"},
				 {"role":1,"userId":"csq"},
				 {"role":1,"userId":"test7475"}
			 ],"id":1,"jsonrpc":"2.0"}
			 * */
			System.out.println("getUsers:resultJO==="+resultJO.toString());
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
	 * 2.2.1 获取定位设备类型表
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
			
			//JSONObject resultJO = APIResultUtil.getDeviceTypes();
			
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
				 	"engineMask":255},
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
				 	{"css":"","icon":"sub-menu-icon6","name":"通讯中继","id":"BTR",
					 	"fields":[
						 	{"mode":"w","default":3600000,"name":"超时值","id":"overtime","type":"double"},
						 	{"mode":"e","name":"状态","expr":"(time+overtime) > new Date()","id":"online","type":"bool",
							 	"list":[
								 	{"html":"在线","value":true},
								 	{"html":"离线","value":false}
							 	]
						 	},
						 	{"mode":"r","name":"最近激活时间","id":"time","type":"datetime"}
					 	],
				 	"engineMask":2},
				 	{"css":"","icon":"sub-menu-icon6","name":"闸机","id":"GAT",
					 	"fields":[
					 		{"mode":"w","name":"名称","id":"name","type":"string"}
					 	],
				 	"engineMask":255},
				 	{"name":"指示牌","id":"LAB","engineMask":255},
				 	{"css":"","icon":"sub-menu-icon6","name":"监控摄像头","id":"SXT",
					 	"fields":[
					 		{"mode":"w","name":"摄像头编号","id":"labelId","type":"double"}
					 	],
				 	"engineMask":255}
			 	],
			 	"id":1,"jsonrpc":"2.0"}
			 * */
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
	 *  2.2.2 获取定位标签类型表
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
	 * 2.2.3 获取定位标签信息
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
	 * 2.2.4 获取系统实体类型
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
			//JSONObject resultJO = APIResultUtil.getEntityTypes();
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
			resultMap=JSON.parseObject(resultJO.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 2.2.5获取定位标签列表
	 * @param request
	 * @return
	 */
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
			resultMap=JSON.parseObject(resultJO.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 2.2.6获取定位引擎配置信息
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
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004739","userId":"4739","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003901","userId":"3901","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004744","userId":"4744","engineMask":1},
			 {"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004773","userId":"4773","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003632","userId":"3632","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT20800186","userId":"186"},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004669","userId":"4669"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003823","userId":"3823","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT823DDE32","userId":"3200"},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004662","userId":"4662"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004778","userId":"4778","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004864","userId":"4864","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004753","userId":"4753","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004857","userId":"4857","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004813","userId":"4813","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003840","userId":"3840","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004885","userId":"4885","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003699","userId":"3699","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004758","userId":"4758","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039759","userId":"9759","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039755","userId":"9755","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004921","userId":"4921"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003639","userId":"3639","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004771","userId":"4771","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004763","userId":"4763","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003629","userId":"3629","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004681","userId":"4681"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003637","userId":"3637","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004670","userId":"4670"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004769","userId":"4769","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004776","userId":"4776","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004779","userId":"4779","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003811","userId":"3811","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003929","userId":"3929","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT36003627","userId":"36003627"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT19122156","userId":"2156"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT32003627","userId":"32003627","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004790","userId":"4790","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003697","userId":"3697","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004835","userId":"4835","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003650","userId":"3650","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003906","userId":"3906","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003723","userId":"3723","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003761","userId":"3761","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004772","userId":"4772","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003687","userId":"3687","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004660","userId":"4660"},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004688","userId":"4688"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003715","userId":"3715","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039762","userId":"9762","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004802","userId":"4802","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039754","userId":"9754","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003882","userId":"3882","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004807","userId":"4807","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003766","userId":"3766","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003892","userId":"3892","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003671","userId":"3671","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003724","userId":"3724","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003741","userId":"3741","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003708","userId":"3708","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004831","userId":"4831","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003729","userId":"3729","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003670","userId":"3670","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003624","userId":"3624","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003673","userId":"3673","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003872","userId":"3872","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003716","userId":"3716","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004691","userId":"4691","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004674","userId":"4674"},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004656","userId":"4656"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004793","userId":"4793","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003759","userId":"3759","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004791","userId":"4791","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003726","userId":"3726","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004808","userId":"4808","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004810","userId":"4810","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003690","userId":"3690","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003739","userId":"3739","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004803","userId":"4803","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003751","userId":"3751","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004817","userId":"4817","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004811","userId":"4811","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004851","userId":"4851","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003736","userId":"3736","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003744","userId":"3744","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003702","userId":"3702","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003870","userId":"3870","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003694","userId":"3694","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003612","userId":"3612","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004833","userId":"4833","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004814","userId":"4814","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003743","userId":"3743","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004848","userId":"4848","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT32004677","userId":"4677"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003727","userId":"3727","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003781","userId":"3781","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003686","userId":"3686","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004800","userId":"4800","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004751","userId":"4751","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003609","userId":"3609","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003731","userId":"3731","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004824","userId":"4824","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003710","userId":"3710","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004768","userId":"4768","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004840","userId":"4840","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004780","userId":"4780","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003819","userId":"3819","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000016","userId":"0016","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003676","userId":"3676","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003821","userId":"3821","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003651","userId":"3651","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003746","userId":"3746","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003869","userId":"3869","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004743","userId":"4743","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004678","userId":"4678"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004692","userId":"4692","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004747","userId":"4747","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004855","userId":"4855","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004679","userId":"4679"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003907","userId":"3907","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004887","userId":"4887","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004854","userId":"4854","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003678","userId":"3678","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004663","userId":"4663"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004877","userId":"4877","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003807","userId":"3807","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004730","userId":"4730","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003665","userId":"3665","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004777","userId":"4777","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004694","userId":"4694","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039774","userId":"9774","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004716","userId":"4716","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003685","userId":"3685","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004910","userId":"4910","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004874","userId":"4874","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004880","userId":"4880","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004789","userId":"4789","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004702","userId":"4702","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004757","userId":"4757","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004863","userId":"4863","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004745","userId":"4745","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004668","userId":"4668"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004886","userId":"4886","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003730","userId":"3730","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004893","userId":"4893","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003622","userId":"3622","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004736","userId":"4736","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004908","userId":"4908","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004909","userId":"4909","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003703","userId":"3703","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003844","userId":"3844","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003899","userId":"3899","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003769","userId":"3769","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004693","userId":"4693","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003696","userId":"3696","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004754","userId":"4754","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004861","userId":"4861","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004822","userId":"4822","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004849","userId":"4849","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004731","userId":"4731","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004667","userId":"4667"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004746","userId":"4746","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004687","userId":"4687"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003603","userId":"3603","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004719","userId":"4719","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003860","userId":"3860","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039792","userId":"9792","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004795","userId":"4795","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004682","userId":"4682"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003645","userId":"3645","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039772","userId":"9772","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004870","userId":"4870","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003654","userId":"3654","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004659","userId":"4659"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003605","userId":"3605","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003662","userId":"3662","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003704","userId":"3704","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004704","userId":"4704","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004871","userId":"4871","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003846","userId":"3846","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003925","userId":"3925","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003852","userId":"3852","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004683","userId":"4683"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004740","userId":"4740","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003763","userId":"3763","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003941","userId":"3941","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003610","userId":"3610","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003709","userId":"3709","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003631","userId":"3631","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004901","userId":"4901","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004684","userId":"4684"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004830","userId":"4830","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004828","userId":"4828","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004708","userId":"4708","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003913","userId":"3913","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004726","userId":"4726","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004725","userId":"4725","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003613","userId":"3613","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003664","userId":"3664","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004862","userId":"4862","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004713","userId":"4713","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003802","userId":"3802","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003717","userId":"3717","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004676","userId":"4676"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004792","userId":"4792","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004685","userId":"4685"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003752","userId":"3752","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004732","userId":"4732","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039769","userId":"9769","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003636","userId":"3636","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003866","userId":"3866","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004767","userId":"4767","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003722","userId":"3722","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039766","userId":"9766","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003828","userId":"3828","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003787","userId":"3787","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004847","userId":"4847","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004797","userId":"4797","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004774","userId":"4774","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003682","userId":"3682","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004839","userId":"4839","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003816","userId":"3816","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003728","userId":"3728","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004895","userId":"4895","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003649","userId":"3649","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003754","userId":"3754","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003652","userId":"3652","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003920","userId":"3920","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003655","userId":"3655","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003758","userId":"3758","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003923","userId":"3923","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004876","userId":"4876","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003864","userId":"3864","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003930","userId":"3930","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003894","userId":"3894","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003755","userId":"3755","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003659","userId":"3659","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003784","userId":"3784","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003734","userId":"3734","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003667","userId":"3667","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003711","userId":"3711","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004841","userId":"4841","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003712","userId":"3712","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004755","userId":"4755","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003738","userId":"3738","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003648","userId":"3648","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003656","userId":"3656","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003797","userId":"3797","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003850","userId":"3850","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003745","userId":"3745","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004782","userId":"4782","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004867","userId":"4867","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003862","userId":"3862","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003719","userId":"3719","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004749","userId":"4749","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000017","userId":"0017","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004672","userId":"4672"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003858","userId":"3858","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004752","userId":"4752","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004658","userId":"4658"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004783","userId":"4783","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004733","userId":"4733","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004661","userId":"4661"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003748","userId":"3748","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004712","userId":"4712","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003641","userId":"3641","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004722","userId":"4722","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003776","userId":"3776","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004865","userId":"4865","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004869","userId":"4869","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003642","userId":"3642","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004748","userId":"4748","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003871","userId":"3871","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003742","userId":"3742","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003845","userId":"3845","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003749","userId":"3749","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003668","userId":"3668","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003928","userId":"3928","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004699","userId":"4699","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003782","userId":"3782","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003643","userId":"3643","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003691","userId":"3691","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003607","userId":"3607","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004703","userId":"4703","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004853","userId":"4853","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004721","userId":"4721","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003767","userId":"3767","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004836","userId":"4836","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004832","userId":"4832","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003900","userId":"3900","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004785","userId":"4785","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004890","userId":"4890","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003943","userId":"3943","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004717","userId":"4717","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003897","userId":"3897","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003783","userId":"3783","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003945","userId":"3945","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003835","userId":"3835","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004796","userId":"4796","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004698","userId":"4698","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004728","userId":"4728","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003772","userId":"3772","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003706","userId":"3706","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003660","userId":"3660","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003672","userId":"3672","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003679","userId":"3679","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004821","userId":"4821","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003937","userId":"3937","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003681","userId":"3681","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004823","userId":"4823","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004905","userId":"4905","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004819","userId":"4819","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003841","userId":"3841","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003770","userId":"3770","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003611","userId":"3611","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004826","userId":"4826","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004697","userId":"4697","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004690","userId":"4690"},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004673","userId":"4673"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000013","userId":"0013","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003774","userId":"3774","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003893","userId":"3893","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004715","userId":"4715","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003778","userId":"3778","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003775","userId":"3775","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003873","userId":"3873","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003747","userId":"3747","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003606","userId":"3606","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003688","userId":"3688","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003805","userId":"3805","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003617","userId":"3617","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003904","userId":"3904","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004881","userId":"4881","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003813","userId":"3813","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004720","userId":"4720","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003837","userId":"3837","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003809","userId":"3809","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004664","userId":"4664"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004711","userId":"4711","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003640","userId":"3640","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003733","userId":"3733","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004801","userId":"4801","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004845","userId":"4845","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004741","userId":"4741","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004883","userId":"4883","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003623","userId":"3623","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004651","userId":"4651"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003926","userId":"3926","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004900","userId":"4900","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003868","userId":"3868","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004675","userId":"4675"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004787","userId":"4787","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004718","userId":"4718","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004859","userId":"4859","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003942","userId":"3942","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003887","userId":"3887","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003938","userId":"3938","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003886","userId":"3886","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004873","userId":"4873","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003859","userId":"3859","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003889","userId":"3889","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003933","userId":"3933","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003922","userId":"3922","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004750","userId":"4750","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004879","userId":"4879","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003851","userId":"3851","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004844","userId":"4844","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039773","userId":"9773","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003684","userId":"3684","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004897","userId":"4897","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004899","userId":"4899","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003750","userId":"3750","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003856","userId":"3856","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003950","userId":"3950","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004724","userId":"4724","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039760","userId":"9760","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004838","userId":"4838","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003786","userId":"3786","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003822","userId":"3822","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004680","userId":"4680"},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004686","userId":"4686"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039756","userId":"9756","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004723","userId":"4723","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003647","userId":"3647","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003946","userId":"3946","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003804","userId":"3804","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004891","userId":"4891","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004875","userId":"4875","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003621","userId":"3621","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003601","userId":"3601","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003614","userId":"3614","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004812","userId":"4812","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003857","userId":"3857","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003808","userId":"3808","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004834","userId":"4834","engineMask":1},{"temporary":true,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004665","userId":"4665"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003680","userId":"3680","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003689","userId":"3689","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003794","userId":"3794","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003935","userId":"3935","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003817","userId":"3817","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003604","userId":"3604","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003630","userId":"3630","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004760","userId":"4760","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003833","userId":"3833","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003881","userId":"3881","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003877","userId":"3877","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004666","userId":"4666"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039781","userId":"9781","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003764","userId":"3764","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003790","userId":"3790","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003777","userId":"3777","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003628","userId":"3628","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003884","userId":"3884","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003898","userId":"3898","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003939","userId":"3939","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003698","userId":"3698","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004709","userId":"4709","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004727","userId":"4727","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003919","userId":"3919","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004671","userId":"4671"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003740","userId":"3740","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003771","userId":"3771","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003799","userId":"3799","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003615","userId":"3615","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003855","userId":"3855","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004652","userId":"4652"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004805","userId":"4805","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003880","userId":"3880","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003874","userId":"3874","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003814","userId":"3814","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003633","userId":"3633","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003927","userId":"3927","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003768","userId":"3768","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003827","userId":"3827","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003773","userId":"3773","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003888","userId":"3888","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003677","userId":"3677","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003725","userId":"3725","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003849","userId":"3849","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003902","userId":"3902","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003635","userId":"3635","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003793","userId":"3793","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003917","userId":"3917","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004892","userId":"4892","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004657","userId":"4657"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003944","userId":"3944","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003720","userId":"3720","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003915","userId":"3915","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004884","userId":"4884","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003842","userId":"3842","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003638","userId":"3638","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003663","userId":"3663","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003824","userId":"3824","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004770","userId":"4770","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003701","userId":"3701","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003918","userId":"3918","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003646","userId":"3646","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003879","userId":"3879","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003891","userId":"3891","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003947","userId":"3947","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039793","userId":"9793","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003932","userId":"3932","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003620","userId":"3620","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000022","userId":"0022","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003798","userId":"3798","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003810","userId":"3810","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003779","userId":"3779","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003801","userId":"3801","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003803","userId":"3803","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000007","userId":"0007"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003885","userId":"3885","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004894","userId":"4894","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003666","userId":"3666","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003812","userId":"3812","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003829","userId":"3829","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003839","userId":"3839","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004784","userId":"4784","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003815","userId":"3815","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003675","userId":"3675","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004837","userId":"4837","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004889","userId":"4889","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003602","userId":"3602","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003644","userId":"3644","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003626","userId":"3626","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004689","userId":"4689"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004809","userId":"4809","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003876","userId":"3876","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003820","userId":"3820","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039765","userId":"9765","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003838","userId":"3838","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003890","userId":"3890","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003619","userId":"3619","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003692","userId":"3692","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004888","userId":"4888","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003714","userId":"3714","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003826","userId":"3826","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003737","userId":"3737","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003674","userId":"3674","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004878","userId":"4878","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003867","userId":"3867","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003936","userId":"3936","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003791","userId":"3791","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004654","userId":"4654"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003800","userId":"3800","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004806","userId":"4806","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003707","userId":"3707","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003834","userId":"3834","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003878","userId":"3878","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003718","userId":"3718","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003875","userId":"3875","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003861","userId":"3861","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003910","userId":"3910","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003789","userId":"3789","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004655","userId":"4655"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003721","userId":"3721","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003896","userId":"3896","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003785","userId":"3785","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003705","userId":"3705","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003661","userId":"3661","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003795","userId":"3795","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003657","userId":"3657","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004818","userId":"4818","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003905","userId":"3905","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003806","userId":"3806","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003916","userId":"3916","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003832","userId":"3832","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003843","userId":"3843","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003713","userId":"3713","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003792","userId":"3792","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003818","userId":"3818","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003700","userId":"3700","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003949","userId":"3949","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003934","userId":"3934","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003762","userId":"3762","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003683","userId":"3683","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003616","userId":"3616","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003765","userId":"3765","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004868","userId":"4868","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004858","userId":"4858","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004701","userId":"4701","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004820","userId":"4820","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004742","userId":"4742","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004756","userId":"4756","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003854","userId":"3854","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004804","userId":"4804","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004705","userId":"4705","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004882","userId":"4882","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004866","userId":"4866","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004825","userId":"4825","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039767","userId":"9767","engineMask":1},{"temporary":true,"entityType":"car","tagStyle":"BTT02","id":"BTT32004653","userId":"4653"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000012","userId":"0012","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003909","userId":"3909","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004907","userId":"4907","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004904","userId":"4904","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003863","userId":"3863","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004788","userId":"4788","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004902","userId":"4902","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039784","userId":"9784","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039761","userId":"9761","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004850","userId":"4850","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004815","userId":"4815","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004764","userId":"4764","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003788","userId":"3788","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004799","userId":"4799","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000015","userId":"0015","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003847","userId":"3847","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039782","userId":"9782","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004827","userId":"4827","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004714","userId":"4714","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003903","userId":"3903","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000018","userId":"0018","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039763","userId":"9763","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000010","userId":"0010","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039775","userId":"9775","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039776","userId":"9776","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000011","userId":"0011","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039788","userId":"9788","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003895","userId":"3895","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039758","userId":"9758","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003865","userId":"3865","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003732","userId":"3732","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004735","userId":"4735","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004898","userId":"4898","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039783","userId":"9783","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000019","userId":"0019","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039757","userId":"9757","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003695","userId":"3695","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039768","userId":"9768","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004842","userId":"4842","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039778","userId":"9778","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004798","userId":"4798","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004734","userId":"4734","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004781","userId":"4781","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039780","userId":"9780","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004852","userId":"4852","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000023","userId":"0023","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039787","userId":"9787","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039764","userId":"9764","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004775","userId":"4775","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039786","userId":"9786","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039770","userId":"9770","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT32003940","userId":"3940"},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039789","userId":"9789","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003780","userId":"3780","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000020","userId":"0020","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004856","userId":"4856","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004860","userId":"4860","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003669","userId":"3669","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039791","userId":"9791","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000014","userId":"0014","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004737","userId":"4737","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003608","userId":"3608","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000006","userId":"0006"},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004766","userId":"4766","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003653","userId":"3653","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003693","userId":"3693","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003735","userId":"3735","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003796","userId":"3796","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000021","userId":"0021","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003825","userId":"3825","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003924","userId":"3924","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003931","userId":"3931","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003753","userId":"3753","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004700","userId":"4700","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004794","userId":"4794","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039790","userId":"9790","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004696","userId":"4696","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004872","userId":"4872","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003948","userId":"3948","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004710","userId":"4710","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003618","userId":"3618","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039777","userId":"9777","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003883","userId":"3883","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003848","userId":"3848","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004707","userId":"4707","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003911","userId":"3911","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003634","userId":"3634","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003908","userId":"3908","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004786","userId":"4786","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004843","userId":"4843","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003658","userId":"3658","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT36000024","userId":"0024","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004906","userId":"4906","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004706","userId":"4706","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003831","userId":"3831","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039785","userId":"9785","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004846","userId":"4846","engineMask":1},{"temporary":false,"entityType":"car","tagStyle":"BTT02","id":"BTT34039779","userId":"9779","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003921","userId":"3921","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003830","userId":"3830","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003836","userId":"3836","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32003757","userId":"3757","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004729","userId":"4729","engineMask":1},{"temporary":false,"entityType":"staff","tagStyle":"BTT01","id":"BTT32004738","userId":"4738","engineMask":1}],"id":1,"jsonrpc":"2.0"}
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
	 * 2.2.7获取定位点数量
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
	 * 2.2.9获取定位点列表
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
	 * 2.2.8获取定位点（暂无数据）
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
	 * 2.2.15获取定位引擎接入点列表
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
	 * 2.2.16获取根地图列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getRootAreas")
	@ResponseBody
	public Map<String, Object> getRootAreas(HttpServletRequest request) {

		Map<String, Object> resultMap = null;
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getRootAreas");
			JSONObject paramJO=new JSONObject();
			//paramJO.put("engineId", "");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getRootAreas",request);
			
			areaService.putInResult(resultJO);
			
			String resultStr = resultJO.toString();
			System.out.println("getRootAreas:resultJO==="+resultStr);
			resultMap=JSON.parseObject(resultStr);
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
					{"fillColor":"#8fc219","areaId":1,"shape":1,
					"coordinates":{"r":107.3,"x":608.58,"y":64.6},
					"name":"ceshi001","id":10002,"height":5}
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
	 * 2.7.17获取设备及状态列表
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
	 * 2.2.18读取标签实时状态
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
			/*
			 {"result":
			 {"BTT32003683":{"altitude":5.9,"tagId":"BTT32003683","latitude":32.262735298435175,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003683","gateId":"7901","labId":2156,"floor":1,"longitude":119.11353856538909,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827379823,"locationTime":1623829502361,"pingTime":1623829502361,"entityType":"staff","labInGate":"7900","inDoor":1623813324947,"entityId":24152,"userId":"3683","beacons":"BTI24007901(3950)","areaId":2,"volt":3955,"absolute":true,"x":500.47,"y":23,"z":0,"rootAreaId":1},
			  "BTT34039783":{"altitude":5.9,"tagId":"BTT34039783","latitude":32.2632656287448,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT34039783","gateId":null,"labId":0,"floor":1,"longitude":119.11551307900704,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829382387,"pingTime":1623829382387,"entityType":"car","labInGate":null,"inDoor":1623829196894,"userId":"9783","beacons":"BTI22085202(4800)","areaId":2,"volt":3550,"absolute":true,"x":686.51,"y":81.819,"z":0,"rootAreaId":1},
			  "BTT34039782":{"altitude":5.9,"tagId":"BTT34039782","latitude":32.26516990137776,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT34039782","gateId":null,"labId":0,"floor":1,"longitude":119.1118492601622,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829469624,"pingTime":1623829469624,"entityType":"car","labInGate":null,"inDoor":1623825277948,"userId":"9782","beacons":"BTI24007866(3450)","areaId":2,"volt":3650,"absolute":true,"x":341.29,"y":292.97,"z":0,"rootAreaId":1},
			  "BTT32003839":{"altitude":5.9,"tagId":"BTT32003839","latitude":32.266808672691276,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003839","gateId":"7843","labId":2157,"floor":1,"longitude":119.11299814440703,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623810791369,"locationTime":1623828009299,"pingTime":1623828009299,"entityType":"staff","labInGate":"7847","inDoor":1623810172284,"entityId":24263,"userId":"3839","beacons":"BTI24007843(4500)","areaId":2,"volt":3893,"absolute":true,"x":449.529,"y":474.7,"z":0,"rootAreaId":1},
			  "BTT32003719":{"altitude":5.9,"tagId":"BTT32003719","latitude":32.263140145988984,"locationType":"location","lostTime":0,"speed":0.32,"nowTime":1623829502542,"out":false,"uid":"BTT32003719","gateId":null,"labId":0,"floor":1,"longitude":119.11546272790285,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499893,"pingTime":1623829501176,"entityType":"staff","labInGate":null,"inDoor":1623829371647,"entityId":28037,"userId":"3719","beacons":"BTI24007919(4500),BTI22085225(5500)","areaId":2,"volt":3800,"absolute":true,"x":681.766,"y":67.904,"z":0,"rootAreaId":1},
			  "BTT32004808":{"altitude":5.9,"tagId":"BTT32004808","latitude":32.26295494506864,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32004808","gateId":"7886","labId":2154,"floor":1,"longitude":119.11416114731922,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827732365,"locationTime":1623828880844,"pingTime":1623828880844,"entityType":"staff","labInGate":"7907","inDoor":1623827548499,"userId":"4808","beacons":"BTI24007886(3950)","areaId":2,"volt":3750,"absolute":true,"x":559.13,"y":47.36,"z":0,"rootAreaId":1},
			  "BTT34039774":{"altitude":5.9,"tagId":"BTT34039774","latitude":32.2632656287448,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT34039774","gateId":null,"labId":0,"floor":1,"longitude":119.11551307900704,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829182919,"pingTime":1623829182919,"entityType":"car","labInGate":null,"inDoor":1623829179695,"userId":"9774","beacons":"BTI22085202(5500)","areaId":2,"volt":3700,"absolute":true,"x":686.51,"y":81.819,"z":0,"rootAreaId":1},
			  "BTT36000013":{"altitude":5.9,"tagId":"BTT36000013","latitude":32.26535130763234,"locationType":"location","lostTime":0,"speed":4.914,"nowTime":1623829502542,"out":false,"uid":"BTT36000013","gateId":null,"labId":0,"floor":1,"longitude":119.1149799569272,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499864,"pingTime":1623829499864,"entityType":"car","labInGate":null,"inDoor":1623810186117,"userId":"0013","beacons":"BTI24007736(2850),BTI24007732(4500),BTI24007735(5500),BTI24007737(5900)","areaId":2,"volt":4043,"absolute":true,"x":636.263,"y":313.1,"z":0,"rootAreaId":1},
			  "BTT36000012":{"altitude":5.9,"tagId":"BTT36000012","latitude":32.263203440658884,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT36000012","gateId":null,"labId":0,"floor":1,"longitude":119.11538943999012,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623828748908,"pingTime":1623828748908,"entityType":"car","labInGate":null,"inDoor":1623825643696,"userId":"0012","beacons":"BTI22085237(3950),BTI24007919(5500)","areaId":2,"volt":3600,"absolute":true,"x":674.86,"y":74.923,"z":0,"rootAreaId":1},
			  "BTT32003838":{"altitude":5.9,"tagId":"BTT32003838","latitude":32.26283447994423,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003838","gateId":"7904","labId":2154,"floor":1,"longitude":119.11418522483652,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623815375568,"locationTime":1623828251375,"pingTime":1623828251375,"entityType":"staff","labInGate":"7908","inDoor":1623813690454,"entityId":24262,"userId":"3838","beacons":"BTI24007904(4250),BTI24007886(5500)","areaId":2,"volt":3850,"absolute":true,"x":561.399,"y":34.001,"z":0,"rootAreaId":1},
			  "BTT32004806":{"altitude":5.9,"tagId":"BTT32004806","latitude":32.26441722907367,"locationType":"location","lostTime":0,"speed":0.176,"nowTime":1623829502542,"out":false,"uid":"BTT32004806","gateId":null,"labId":0,"floor":1,"longitude":119.10947650971212,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502466,"pingTime":1623829502466,"entityType":"staff","labInGate":null,"inDoor":1623810186092,"userId":"4806","beacons":"BTI24006185(12750)","areaId":2,"volt":4000,"absolute":true,"x":117.729,"y":209.5,"z":0,"rootAreaId":1},
			  "BTT36000010":{"altitude":5.9,"tagId":"BTT36000010","latitude":32.26323552150749,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT36000010","gateId":null,"labId":0,"floor":1,"longitude":119.11533500225818,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623827706427,"pingTime":1623827706427,"entityType":"car","labInGate":null,"inDoor":1623824373115,"userId":"0010","beacons":"BTI22085237(5500)","areaId":2,"volt":3550,"absolute":true,"x":669.731,"y":78.48,"z":0,"rootAreaId":1},
			  "BTT36000017":{"altitude":5.9,"tagId":"BTT36000017","latitude":32.265535304625956,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT36000017","gateId":null,"labId":0,"floor":1,"longitude":119.11449129282602,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829447732,"pingTime":1623829447732,"entityType":"car","labInGate":null,"inDoor":1623810824616,"userId":"0017","beacons":"BTI24007747(3700),BTI24007742(4250),BTI24007745(4250),BTI24007746(4250)","areaId":2,"volt":3800,"absolute":true,"x":590.22,"y":333.501,"z":0,"rootAreaId":1},
			  "BTT32003799":{"altitude":5.9,"tagId":"BTT32003799","latitude":32.26305124702239,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003799","gateId":"7907","labId":2154,"floor":1,"longitude":119.11433881840587,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623826815975,"locationTime":1623828178986,"pingTime":1623828178986,"entityType":"staff","labInGate":"7909","inDoor":1623826689769,"entityId":24225,"userId":"3799","beacons":"BTI24007907(4250)","areaId":2,"volt":3700,"absolute":true,"x":575.87,"y":58.039,"z":0,"rootAreaId":1},
			  "BTT36000016":{"altitude":5.9,"tagId":"BTT36000016","latitude":32.26555946430873,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT36000016","gateId":null,"labId":0,"floor":1,"longitude":119.11451904367044,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502425,"pingTime":1623829502425,"entityType":"car","labInGate":null,"inDoor":1623811948025,"userId":"0016","beacons":"BTI24007746(2050),BTI24007747(2650),BTI22085233(4250),BTI24007741(4500)","areaId":2,"volt":4000,"absolute":true,"x":592.834,"y":336.18,"z":0,"rootAreaId":1},
			  "BTT36000015":{"altitude":5.9,"tagId":"BTT36000015","latitude":32.26320496085595,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT36000015","gateId":null,"labId":0,"floor":1,"longitude":119.1153868603818,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623828730577,"pingTime":1623828730577,"entityType":"car","labInGate":null,"inDoor":1623824902460,"userId":"0015","beacons":"BTI22085237(3450),BTI24007919(4500)","areaId":2,"volt":3550,"absolute":true,"x":674.617,"y":75.091,"z":0,"rootAreaId":1},
			  "BTT32003673":{"altitude":5.9,"tagId":"BTT32003673","latitude":32.26280759706392,"locationType":"location","lostTime":0,"speed":0.418,"nowTime":1623829502542,"out":false,"uid":"BTT32003673","gateId":"5241","labId":2153,"floor":1,"longitude":119.11477664470144,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623828922388,"locationTime":1623829499874,"pingTime":1623829499874,"entityType":"staff","labInGate":"5226","inDoor":1623810177165,"entityId":24144,"userId":"3673","beacons":"BTI22085241(2650),BTI22085226(4250)","areaId":2,"volt":3950,"absolute":true,"x":617.124,"y":31.023,"z":0,"rootAreaId":1},
			  "BTT32003950":{"altitude":5.9,"tagId":"BTT32003950","latitude":32.26299683644942,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003950","gateId":"7886","labId":2154,"floor":1,"longitude":119.1142384341958,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623828078542,"locationTime":1623829359213,"pingTime":1623829406223,"entityType":"staff","labInGate":"7886","inDoor":1623816307288,"entityId":24368,"userId":"3950","beacons":"BTI24007886(4500),BTI24007907(5500)","areaId":2,"volt":4093,"absolute":true,"x":566.411,"y":52.005,"z":0,"rootAreaId":1},
			  "BTT32003675":{"altitude":5.9,"tagId":"BTT32003675","latitude":32.262735298435175,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003675","gateId":"7901","labId":2156,"floor":1,"longitude":119.11353856538909,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623828283811,"locationTime":1623829499855,"pingTime":1623829499855,"entityType":"staff","labInGate":"7902","inDoor":1623816770905,"entityId":128905,"userId":"3675","beacons":"BTI24007901(3050)","areaId":2,"volt":3550,"absolute":true,"x":500.47,"y":23,"z":0,"rootAreaId":1},
			  "BTT36000018":{"altitude":5.9,"tagId":"BTT36000018","latitude":32.26323552150749,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT36000018","gateId":null,"labId":0,"floor":1,"longitude":119.11533500225818,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623827810248,"pingTime":1623827810248,"entityType":"car","labInGate":null,"inDoor":1623824878624,"userId":"0018","beacons":"BTI22085237(5500)","areaId":2,"volt":3650,"absolute":true,"x":669.731,"y":78.48,"z":0,"rootAreaId":1},
			  "BTT32003790":{"altitude":5.9,"tagId":"BTT32003790","latitude":32.26305124702239,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003790","gateId":"7907","labId":2154,"floor":1,"longitude":119.11433881840587,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623815480494,"locationTime":1623829499869,"pingTime":1623829499869,"entityType":"staff","labInGate":"7909","inDoor":1623814676368,"entityId":128915,"userId":"3790","beacons":"BTI24007907(3700)","areaId":2,"volt":3650,"absolute":true,"x":575.87,"y":58.039,"z":0,"rootAreaId":1},
			  "BTT32004880":{"altitude":5.9,"tagId":"BTT32004880","latitude":32.26548956511607,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32004880","gateId":null,"labId":0,"floor":1,"longitude":119.11471676224716,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829447726,"pingTime":1623829447727,"entityType":"staff","labInGate":null,"inDoor":1623810177188,"userId":"4880","beacons":"BTI22085235(1800),BTI22085233(2850)","areaId":2,"volt":3650,"absolute":true,"x":611.464,"y":328.43,"z":0,"rootAreaId":1},
			  "BTT32003792":{"altitude":5.9,"tagId":"BTT32003792","latitude":32.26305124702239,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003792","gateId":"7907","labId":2154,"floor":1,"longitude":119.11433881840587,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623826172971,"locationTime":1623829499867,"pingTime":1623829499867,"entityType":"staff","labInGate":"7886","inDoor":1623812616102,"entityId":24218,"userId":"3792","beacons":"BTI24007907(2200)","areaId":2,"volt":4043,"absolute":true,"x":575.87,"y":58.039,"z":0,"rootAreaId":1},"BTT34039772":{"altitude":5.9,"tagId":"BTT34039772","latitude":32.2632656287448,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT34039772","gateId":null,"labId":0,"floor":1,"longitude":119.11551307900704,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829201173,"pingTime":1623829201227,"entityType":"car","labInGate":null,"inDoor":1623829192764,"userId":"9772","beacons":"BTI22085202(4250)","areaId":2,"volt":3600,"absolute":true,"x":686.51,"y":81.819,"z":0,"rootAreaId":1},"BTT32003707":{"altitude":5.9,"tagId":"BTT32003707","latitude":32.262735298435175,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003707","gateId":"7901","labId":2156,"floor":1,"longitude":119.11353856538909,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623816026613,"locationTime":1623829124011,"pingTime":1623829124011,"entityType":"staff","labInGate":"7902","inDoor":1623810230830,"entityId":24163,"userId":"3707","beacons":"BTI24007901(2650)","areaId":2,"volt":3943,"absolute":true,"x":500.47,"y":23,"z":0,"rootAreaId":1},"BTT36000020":{"altitude":5.9,"tagId":"BTT36000020","latitude":32.26546435609904,"locationType":"location","lostTime":0,"speed":7.104,"nowTime":1623829502542,"out":false,"uid":"BTT36000020","gateId":null,"labId":0,"floor":1,"longitude":119.11387924105244,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499857,"pingTime":1623829499857,"entityType":"car","labInGate":null,"inDoor":1623810178549,"userId":"0020","beacons":"BTI22085232(4800),BTI22085236(5150),BTI24006430(5900),BTI24006431(12750)","areaId":2,"volt":3750,"absolute":true,"x":532.553,"y":325.63,"z":0,"rootAreaId":1},"BTT32003829":{"altitude":5.9,"tagId":"BTT32003829","latitude":32.26525543802015,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003829","gateId":"7045","labId":2160,"floor":2,"longitude":119.10926828535297,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623829260278,"locationTime":1623829499868,"pingTime":1623829499868,"entityType":"staff","labInGate":"7031","inDoor":1623828808716,"entityId":24253,"userId":"3829","beacons":"BTI24007045(4250)","areaId":3,"volt":4093,"absolute":true,"x":98.11,"y":302.45,"z":0,"rootAreaId":1},"BTT34039769":{"altitude":5.9,"tagId":"BTT34039769","latitude":32.2655792159364,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT34039769","gateId":null,"labId":0,"floor":1,"longitude":119.11431161045248,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829496482,"pingTime":1623829496482,"entityType":"car","labInGate":null,"inDoor":1623824120304,"userId":"9769","beacons":"BTI24007747(12750),BTI24007746(12750),BTI22085233(12750)","areaId":2,"volt":3500,"absolute":true,"x":573.29,"y":338.369,"z":0,"rootAreaId":1},"BTT32003946":{"altitude":5.9,"tagId":"BTT32003946","latitude":32.26282180802786,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003946","gateId":"5241","labId":2153,"floor":1,"longitude":119.11484292834353,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623818786704,"locationTime":1623829013271,"pingTime":1623829013271,"entityType":"staff","labInGate":"5241","inDoor":1623818579084,"entityId":24179,"userId":"3946","beacons":"BTI22085241(1550)","areaId":2,"volt":4000,"absolute":true,"x":623.37,"y":32.6,"z":0,"rootAreaId":1},"BTT36000023":{"altitude":5.9,"tagId":"BTT36000023","latitude":32.26319404751764,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT36000023","gateId":null,"labId":0,"floor":1,"longitude":119.11540537912053,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623828748918,"pingTime":1623828748918,"entityType":"car","labInGate":null,"inDoor":1623828722931,"userId":"0023","beacons":"BTI24007919(5150),BTI22085237(5900)","areaId":2,"volt":3700,"absolute":true,"x":676.362,"y":73.881,"z":0,"rootAreaId":1},"BTT32003947":{"altitude":5.9,"tagId":"BTT32003947","latitude":32.26283766460917,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003947","gateId":"7904","labId":2154,"floor":1,"longitude":119.11418458831433,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827520646,"locationTime":1623829103159,"pingTime":1623829103159,"entityType":"staff","labInGate":"7886","inDoor":1623810172178,"entityId":24236,"userId":"3947","beacons":"BTI24007904(3950),BTI24007886(4250)","areaId":2,"volt":4093,"absolute":true,"x":561.339,"y":34.354,"z":0,"rootAreaId":1},"BTT36000022":{"altitude":5.9,"tagId":"BTT36000022","latitude":32.26296756577826,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT36000022","gateId":"7886","labId":2154,"floor":1,"longitude":119.11414874216862,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623812601544,"locationTime":1623829447735,"pingTime":1623829447736,"entityType":"car","labInGate":"7886","inDoor":1623811522238,"userId":"0022","beacons":"BTI24007886(1450),BTI24007910(3700),BTI24007904(3950),BTI24006846(5150)","areaId":2,"volt":4100,"absolute":true,"x":557.961,"y":48.759,"z":0,"rootAreaId":1},"BTT32004876":{"altitude":5.9,"tagId":"BTT32004876","latitude":32.26441948701075,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32004876","gateId":null,"labId":0,"floor":1,"longitude":119.10905282479483,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829304815,"pingTime":1623829334721,"entityType":"staff","labInGate":null,"inDoor":1623826445309,"userId":"4876","beacons":"BTI24011534(12750)","areaId":2,"volt":3950,"absolute":true,"x":77.81,"y":209.749,"z":0,"rootAreaId":1},"BTT32003700":{"altitude":5.9,"tagId":"BTT32003700","latitude":32.26323552150749,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003700","gateId":null,"labId":0,"floor":1,"longitude":119.11533500225818,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502378,"pingTime":1623829502378,"entityType":"staff","labInGate":null,"inDoor":1623810177100,"entityId":24162,"userId":"3700","beacons":"BTI22085237(600),BTI22085238(3450)","areaId":2,"volt":4143,"absolute":true,"x":669.731,"y":78.48,"z":0,"rootAreaId":1},"BTT32003788":{"altitude":5.9,"tagId":"BTT32003788","latitude":32.266808672691276,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003788","gateId":"7843","labId":2157,"floor":1,"longitude":119.11299814440703,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623828145797,"locationTime":1623828487518,"pingTime":1623828487518,"entityType":"staff","labInGate":"7845","inDoor":1623810172287,"entityId":24216,"userId":"3788","beacons":"BTI24007843(2200)","areaId":2,"volt":3750,"absolute":true,"x":449.529,"y":474.7,"z":0,"rootAreaId":1},"BTT32003701":{"altitude":5.9,"tagId":"BTT32003701","latitude":32.264571984927336,"locationType":"location","lostTime":0,"speed":4.578,"nowTime":1623829502542,"out":false,"uid":"BTT32003701","gateId":null,"labId":0,"floor":1,"longitude":119.11342332214087,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502500,"pingTime":1623829502500,"entityType":"staff","labInGate":null,"inDoor":1623819858176,"userId":"3701","beacons":"BTI24007629(3700),BTI24007641(12750)","areaId":2,"volt":3800,"absolute":true,"x":489.601,"y":226.672,"z":0,"rootAreaId":1},"BTT32003823":{"altitude":5.9,"tagId":"BTT32003823","latitude":32.26525543802015,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003823","gateId":"7045","labId":2160,"floor":2,"longitude":119.10926828535297,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623822190422,"locationTime":1623829502404,"pingTime":1623829502404,"entityType":"staff","labInGate":"7031","inDoor":1623810383776,"entityId":24248,"userId":"3823","beacons":"BTI24007045(5900)","areaId":3,"volt":3850,"absolute":true,"x":98.11,"y":302.45,"z":0,"rootAreaId":1},"BTT32003944":{"altitude":5.9,"tagId":"BTT32003944","latitude":32.262821913687326,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003944","gateId":"7904","labId":2154,"floor":1,"longitude":119.11418773646649,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827859360,"locationTime":1623828652776,"pingTime":1623828652776,"entityType":"staff","labInGate":"7904","inDoor":1623810172147,"entityId":24364,"userId":"3944","beacons":"BTI24007904(2850),BTI24007886(5900)","areaId":2,"volt":4143,"absolute":true,"x":561.636,"y":32.608,"z":0,"rootAreaId":1},"BTT32003669":{"altitude":5.9,"tagId":"BTT32003669","latitude":32.26282180802786,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003669","gateId":"5241","labId":2153,"floor":1,"longitude":119.11484292834353,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623816453322,"locationTime":1623829462196,"pingTime":1623829462196,"entityType":"staff","labInGate":"5221","inDoor":1623816451215,"entityId":24141,"userId":"3669","beacons":"BTI22085241(2500)","areaId":2,"volt":3700,"absolute":true,"x":623.37,"y":32.6,"z":0,"rootAreaId":1},"BTT32003783":{"altitude":5.9,"tagId":"BTT32003783","latitude":32.2629989121229,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003783","gateId":"7913","labId":2153,"floor":1,"longitude":119.11495820038135,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623816080374,"locationTime":1623829436471,"pingTime":1623829496544,"entityType":"staff","labInGate":"5241","inDoor":1623816072693,"entityId":24211,"userId":"3783","beacons":"BTI24007913(5900)","areaId":2,"volt":3550,"absolute":true,"x":634.23,"y":52.24,"z":0,"rootAreaId":1},"BTT32003663":{"altitude":5.9,"tagId":"BTT32003663","latitude":32.26292352315963,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003663","gateId":"7913","labId":2153,"floor":1,"longitude":119.11495639057719,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623815859764,"locationTime":1623829425791,"pingTime":1623829425791,"entityType":"staff","labInGate":"5221","inDoor":1623815859206,"entityId":24135,"userId":"3663","beacons":"BTI22085221(5900)","areaId":2,"volt":3600,"absolute":true,"x":634.06,"y":43.88,"z":0,"rootAreaId":1},"BTT32003784":{"altitude":5.9,"tagId":"BTT32003784","latitude":32.26282180802786,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003784","gateId":"5241","labId":2153,"floor":1,"longitude":119.11484292834353,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623812003078,"locationTime":1623829499873,"pingTime":1623829499873,"entityType":"staff","labInGate":"5241","inDoor":1623812001403,"entityId":24212,"userId":"3784","beacons":"BTI22085241(3250)","areaId":2,"volt":3626,"absolute":true,"x":623.37,"y":32.6,"z":0,"rootAreaId":1},"BTT32004752":{"altitude":5.9,"tagId":"BTT32004752","latitude":32.26702823746975,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32004752","gateId":null,"labId":0,"floor":1,"longitude":119.11042763502509,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829495734,"pingTime":1623829497353,"entityType":"staff","labInGate":null,"inDoor":1623820130617,"userId":"4752","beacons":"BTI24006770(12750)","areaId":2,"volt":3550,"absolute":true,"x":207.339,"y":499.04,"z":0,"rootAreaId":1},"BTT32004874":{"altitude":5.9,"tagId":"BTT32004874","latitude":32.26525420190171,"locationType":"location","lostTime":0,"speed":0.171,"nowTime":1623829502542,"out":false,"uid":"BTT32004874","gateId":null,"labId":0,"floor":1,"longitude":119.1139236208508,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499886,"pingTime":1623829499886,"entityType":"staff","labInGate":null,"inDoor":1623824999122,"userId":"4874","beacons":"BTI22085203(2500),BTI22085204(3450),BTI24007754(3950),BTI22085236(4500)","areaId":2,"volt":3600,"absolute":true,"x":536.736,"y":302.326,"z":0,"rootAreaId":1},"BTT32003940":{"altitude":5.9,"tagId":"BTT32003940","latitude":32.26274469962506,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003940","gateId":"7899","labId":2156,"floor":1,"longitude":119.11296948853794,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623821637616,"locationTime":1623828960864,"pingTime":1623829002071,"entityType":"car","labInGate":"7899","inDoor":1623821635526,"entityId":26927,"userId":"3940","beacons":"BTI24007899(4500)","areaId":2,"volt":3711,"absolute":true,"x":446.85,"y":24.039,"z":0,"rootAreaId":1},"BTT32003786":{"altitude":5.9,"tagId":"BTT32003786","latitude":32.262735298435175,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003786","gateId":"7901","labId":2156,"floor":1,"longitude":119.11353856538909,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623828421116,"locationTime":1623829499856,"pingTime":1623829499856,"entityType":"staff","labInGate":"7901","inDoor":1623828277252,"entityId":24214,"userId":"3786","beacons":"BTI24007901(4800)","areaId":2,"volt":3993,"absolute":true,"x":500.47,"y":23,"z":0,"rootAreaId":1},"BTT32003780":{"altitude":5.9,"tagId":"BTT32003780","latitude":32.26295736712981,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003780","gateId":"7913","labId":2153,"floor":1,"longitude":119.11495720304268,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623815521734,"locationTime":1623829359361,"pingTime":1623829359361,"entityType":"staff","labInGate":"5221","inDoor":1623815518649,"entityId":24210,"userId":"3780","beacons":"BTI22085221(4500),BTI24007913(4800)","areaId":2,"volt":3650,"absolute":true,"x":634.136,"y":47.633,"z":0,"rootAreaId":1},"BTT32003660":{"altitude":5.9,"tagId":"BTT32003660","latitude":32.266808672691276,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003660","gateId":"7843","labId":2157,"floor":1,"longitude":119.11299814440703,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623829404365,"locationTime":1623829499872,"pingTime":1623829499872,"entityType":"staff","labInGate":"7844","inDoor":1623829216216,"entityId":24132,"userId":"3660","beacons":"BTI24007843(1050)","areaId":2,"volt":4000,"absolute":true,"x":449.529,"y":474.7,"z":0,"rootAreaId":1},"BTT34039761":{"altitude":5.9,"tagId":"BTT34039761","latitude":32.26316054600418,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT34039761","gateId":null,"labId":0,"floor":1,"longitude":119.11546222745953,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829267294,"pingTime":1623829327270,"entityType":"car","labInGate":null,"inDoor":1623829146595,"userId":"9761","beacons":"BTI24007919(5900)","areaId":2,"volt":3900,"absolute":true,"x":681.719,"y":70.166,"z":0,"rootAreaId":1},"BTT34039760":{"altitude":5.9,"tagId":"BTT34039760","latitude":32.26313404623533,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT34039760","gateId":null,"labId":0,"floor":1,"longitude":119.11546287753902,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829500360,"pingTime":1623829500362,"entityType":"car","labInGate":null,"inDoor":1623829192773,"userId":"9760","beacons":"BTI22085225(4800),BTI24007919(5500)","areaId":2,"volt":3600,"absolute":true,"x":681.78,"y":67.228,"z":0,"rootAreaId":1},"BTT34039755":{"altitude":5.9,"tagId":"BTT34039755","latitude":32.26444724495523,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT34039755","gateId":null,"labId":0,"floor":1,"longitude":119.11533582427033,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499894,"pingTime":1623829499894,"entityType":"car","labInGate":null,"inDoor":1623824279327,"userId":"9755","beacons":"BTI24007719(12750)","areaId":2,"volt":4043,"absolute":true,"x":669.8,"y":212.849,"z":0,"rootAreaId":1},"BTT32004908":{"altitude":5.9,"tagId":"BTT32004908","latitude":32.262967385829285,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32004908","gateId":"7913","labId":2153,"floor":1,"longitude":119.11495744355379,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827416684,"locationTime":1623829400285,"pingTime":1623829400285,"entityType":"staff","labInGate":"7916","inDoor":1623810185974,"userId":"4908","beacons":"BTI24007913(2650),BTI22085221(2850)","areaId":2,"volt":3950,"absolute":true,"x":634.158,"y":48.744,"z":0,"rootAreaId":1},"BTT32003659":{"altitude":5.9,"tagId":"BTT32003659","latitude":32.26295583271542,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003659","gateId":"5221","labId":2153,"floor":1,"longitude":119.11495716620719,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827435074,"locationTime":1623827764987,"pingTime":1623827764987,"entityType":"staff","labInGate":"5221","inDoor":1623827404394,"entityId":24131,"userId":"3659","beacons":"BTI22085221(3450),BTI24007913(4250)","areaId":2,"volt":3800,"absolute":true,"x":634.132,"y":47.462,"z":0,"rootAreaId":1},"BTT32003935":{"altitude":5.9,"tagId":"BTT32003935","latitude":32.26305920501626,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003935","gateId":"7888","labId":2155,"floor":1,"longitude":119.1138589965235,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827825001,"locationTime":1623829034518,"pingTime":1623829045003,"entityType":"staff","labInGate":"7888","inDoor":1623811267003,"entityId":24355,"userId":"3935","beacons":"BTI24007888(2350)","areaId":2,"volt":3750,"absolute":true,"x":530.66,"y":58.919,"z":0,"rootAreaId":1},"BTT32003937":{"altitude":5.9,"tagId":"BTT32003937","latitude":32.266835262109225,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003937","gateId":"7843","labId":2157,"floor":1,"longitude":119.11302007431915,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827065621,"locationTime":1623828634917,"pingTime":1623828654498,"entityType":"staff","labInGate":"7843","inDoor":1623810186184,"entityId":24357,"userId":"3937","beacons":"BTI24007843(3700),BTI24007842(12750)","areaId":2,"volt":3850,"absolute":true,"x":451.596,"y":477.648,"z":0,"rootAreaId":1},"BTT32004900":{"altitude":5.9,"tagId":"BTT32004900","latitude":32.26292352315963,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32004900","gateId":"5221","labId":2153,"floor":1,"longitude":119.11495639057719,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623816065938,"locationTime":1623828871037,"pingTime":1623828871038,"entityType":"staff","labInGate":"5241","inDoor":1623816060304,"userId":"4900","beacons":"BTI22085221(4800)","areaId":2,"volt":3700,"absolute":true,"x":634.06,"y":43.88,"z":0,"rootAreaId":1},"BTT32003657":{"altitude":5.9,"tagId":"BTT32003657","latitude":32.26315084803759,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003657","gateId":"7909","labId":2154,"floor":1,"longitude":119.1144878377227,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827788229,"locationTime":1623827842846,"pingTime":1623827891742,"entityType":"staff","labInGate":"7886","inDoor":1623810952097,"entityId":24129,"userId":"3657","beacons":"BTI24011580(3050),BTI24006112(4500),BTI24007909(5150)","areaId":2,"volt":3950,"absolute":true,"x":589.91,"y":69.085,"z":0,"rootAreaId":1},"BTT32004868":{"altitude":5.9,"tagId":"BTT32004868","latitude":32.266701085505275,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32004868","gateId":null,"labId":0,"floor":3,"longitude":119.10928830852626,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502507,"pingTime":1623829502507,"entityType":"staff","labInGate":null,"inDoor":1623820008307,"userId":"4868","beacons":"BTI24007010(12750),BTI24007011(12750)","areaId":4,"volt":3843,"absolute":true,"x":99.995,"y":462.76,"z":0,"rootAreaId":1},"BTT32003772":{"altitude":5.9,"tagId":"BTT32003772","latitude":32.262735298435175,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003772","gateId":"7901","labId":2156,"floor":1,"longitude":119.11353856538909,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623816050380,"locationTime":1623829097096,"pingTime":1623829097096,"entityType":"staff","labInGate":"7901","inDoor":1623810177096,"entityId":24204,"userId":"3772","beacons":"BTI24007901(3450)","areaId":2,"volt":3600,"absolute":true,"x":500.47,"y":23,"z":0,"rootAreaId":1},"BTT32003773":{"altitude":5.9,"tagId":"BTT32003773","latitude":32.26446174863453,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32003773","gateId":null,"labId":0,"floor":1,"longitude":119.10904407378624,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829335818,"pingTime":1623829380622,"entityType":"staff","labInGate":null,"inDoor":1623819869872,"entityId":28039,"userId":"3773","beacons":"BTI24011534(2850),BTI24007063(12750),BTI24007114(12750)","areaId":2,"volt":3943,"absolute":true,"x":76.985,"y":214.436,"z":0,"rootAreaId":1},"BTT32004742":{"altitude":5.9,"tagId":"BTT32004742","latitude":32.26547918200732,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502542,"out":false,"uid":"BTT32004742","gateId":null,"labId":0,"floor":1,"longitude":119.1147154520274,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829029562,"pingTime":1623829029562,"entityType":"staff","labInGate":null,"inDoor":1623828615173,"userId":"4742","beacons":"BTI22085235(800),BTI22085233(4800)","areaId":2,"volt":3600,"absolute":true,"x":611.341,"y":327.279,"z":0,"rootAreaId":1},"BTT32003775":{"altitude":5.9,"tagId":"BTT32003775","latitude":32.26533781602231,"locationType":"location","lostTime":0,"speed":2.418,"nowTime":1623829502543,"out":false,"uid":"BTT32003775","gateId":null,"labId":0,"floor":1,"longitude":119.11497789731477,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502480,"pingTime":1623829502480,"entityType":"staff","labInGate":null,"inDoor":1623827951800,"userId":"3775","beacons":"BTI24007734(5500),BTI24007735(12750),BTI24007737(12750),BTI24007736(12750)","areaId":2,"volt":4093,"absolute":true,"x":636.069,"y":311.604,"z":0,"rootAreaId":1},"BTT32003806":{"altitude":5.9,"tagId":"BTT32003806","latitude":32.262922201992595,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003806","gateId":"7908","labId":2154,"floor":1,"longitude":119.1143363687274,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827658967,"locationTime":1623828795078,"pingTime":1623828795078,"entityType":"staff","labInGate":"7909","inDoor":1623827520563,"entityId":24232,"userId":"3806","beacons":"BTI24007908(1350)","areaId":2,"volt":4050,"absolute":true,"x":575.64,"y":43.73,"z":0,"rootAreaId":1},"BTT32003927":{"altitude":5.9,"tagId":"BTT32003927","latitude":32.26305920501626,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003927","gateId":"7888","labId":2155,"floor":1,"longitude":119.1138589965235,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623828545971,"locationTime":1623829502488,"pingTime":1623829502488,"entityType":"staff","labInGate":"7888","inDoor":1623810907060,"entityId":24347,"userId":"3927","beacons":"BTI24007888(3050)","areaId":2,"volt":3750,"absolute":true,"x":530.66,"y":58.919,"z":0,"rootAreaId":1},"BTT32003928":{"altitude":5.9,"tagId":"BTT32003928","latitude":32.263205941815514,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003928","gateId":"7912","labId":2155,"floor":1,"longitude":119.11347088052494,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827779890,"locationTime":1623828176297,"pingTime":1623828232135,"entityType":"staff","labInGate":"7912","inDoor":1623827543383,"entityId":24348,"userId":"3928","beacons":"BTI24007912(4800)","areaId":2,"volt":3850,"absolute":true,"x":494.09,"y":75.19,"z":0,"rootAreaId":1},"BTT32003808":{"altitude":5.9,"tagId":"BTT32003808","latitude":32.262972013633686,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003808","gateId":"7913","labId":2153,"floor":1,"longitude":119.11495755464992,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623824619083,"locationTime":1623827825710,"pingTime":1623827887865,"entityType":"staff","labInGate":"7913","inDoor":1623814774840,"entityId":24233,"userId":"3808","beacons":"BTI24007913(1800),BTI22085221(2350)","areaId":2,"volt":3550,"absolute":true,"x":634.169,"y":49.257,"z":0,"rootAreaId":1},"BTT32003769":{"altitude":5.9,"tagId":"BTT32003769","latitude":32.263919804928335,"locationType":"location","lostTime":0,"speed":0.186,"nowTime":1623829502543,"out":false,"uid":"BTT32003769","gateId":null,"labId":0,"floor":1,"longitude":119.10954846101842,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499874,"pingTime":1623829499874,"entityType":"staff","labInGate":null,"inDoor":1623827207689,"entityId":28095,"userId":"3769","beacons":"BTI24006179(5900)","areaId":2,"volt":3700,"absolute":true,"x":124.51,"y":154.34,"z":0,"rootAreaId":1},"BTT32004737":{"altitude":5.9,"tagId":"BTT32004737","latitude":32.26283447994423,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004737","gateId":"7904","labId":2154,"floor":1,"longitude":119.11418522483652,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623828452478,"locationTime":1623829502472,"pingTime":1623829502472,"entityType":"staff","labInGate":"7886","inDoor":1623810183301,"userId":"4737","beacons":"BTI24007904(4250),BTI24007886(5500)","areaId":2,"volt":3700,"absolute":true,"x":561.399,"y":34.001,"z":0,"rootAreaId":1},"BTT32004858":{"altitude":5.9,"tagId":"BTT32004858","latitude":32.26274691832213,"locationType":"location","lostTime":0,"speed":5.531,"nowTime":1623829502543,"out":false,"uid":"BTT32004858","gateId":"7906","labId":2154,"floor":1,"longitude":119.11441112440549,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623828992962,"locationTime":1623828997299,"pingTime":1623829051767,"entityType":"staff","labInGate":"7906","inDoor":1623819230366,"userId":"4858","beacons":"BTI24006117(3450)","areaId":2,"volt":3600,"absolute":true,"x":582.684,"y":24.292,"z":0,"rootAreaId":1},"BTT32003803":{"altitude":5.9,"tagId":"BTT32003803","latitude":32.26305124702239,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003803","gateId":"7907","labId":2154,"floor":1,"longitude":119.11433881840587,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623824418494,"locationTime":1623829443068,"pingTime":1623829449875,"entityType":"staff","labInGate":"7910","inDoor":1623810178545,"entityId":24229,"userId":"3803","beacons":"BTI24007907(3250)","areaId":2,"volt":3593,"absolute":true,"x":575.87,"y":58.039,"z":0,"rootAreaId":1},"BTT32003925":{"altitude":5.9,"tagId":"BTT32003925","latitude":32.266808672691276,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003925","gateId":"7843","labId":2157,"floor":1,"longitude":119.11299814440703,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623814429069,"locationTime":1623828634815,"pingTime":1623828634815,"entityType":"staff","labInGate":"7843","inDoor":1623814421800,"entityId":24345,"userId":"3925","beacons":"BTI24007843(4800)","areaId":2,"volt":3850,"absolute":true,"x":449.529,"y":474.7,"z":0,"rootAreaId":1},"BTT32003765":{"altitude":5.9,"tagId":"BTT32003765","latitude":32.262967956957795,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003765","gateId":"7913","labId":2153,"floor":1,"longitude":119.11495745726444,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623824364629,"locationTime":1623829428618,"pingTime":1623829484871,"entityType":"staff","labInGate":"5221","inDoor":1623810254291,"entityId":24202,"userId":"3765","beacons":"BTI24007913(3450),BTI22085221(4800)","areaId":2,"volt":4143,"absolute":true,"x":634.16,"y":48.807,"z":0,"rootAreaId":1},"BTT32004735":{"altitude":5.9,"tagId":"BTT32004735","latitude":32.26277794147428,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004735","gateId":"6859","labId":2210,"floor":1,"longitude":119.11110518269018,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623828890801,"locationTime":1623829002052,"pingTime":1623829032772,"entityType":"staff","labInGate":"6859","inDoor":1623819352363,"userId":"4735","beacons":"BTI24006859(4500)","areaId":2,"volt":3800,"absolute":true,"x":271.19,"y":27.719,"z":0,"rootAreaId":1},"BTT32003889":{"altitude":5.9,"tagId":"BTT32003889","latitude":32.266808672691276,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003889","gateId":"7843","labId":2157,"floor":1,"longitude":119.11299814440703,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623829408483,"locationTime":1623829499882,"pingTime":1623829499882,"entityType":"staff","labInGate":"7845","inDoor":1623829290820,"entityId":24311,"userId":"3889","beacons":"BTI24007843(1200)","areaId":2,"volt":4043,"absolute":true,"x":449.529,"y":474.7,"z":0,"rootAreaId":1},"BTT32004851":{"altitude":5.9,"tagId":"BTT32004851","latitude":32.26550822348437,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004851","gateId":null,"labId":0,"floor":1,"longitude":119.11471911670297,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829433663,"pingTime":1623829433664,"entityType":"staff","labInGate":null,"inDoor":1623810903246,"userId":"4851","beacons":"BTI22085233(750),BTI22085235(3950)","areaId":2,"volt":3450,"absolute":true,"x":611.686,"y":330.499,"z":0,"rootAreaId":1},"BTT32004697":{"altitude":5.9,"tagId":"BTT32004697","latitude":32.26305920501626,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004697","gateId":"7888","labId":2155,"floor":1,"longitude":119.1138589965235,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827934586,"locationTime":1623828415062,"pingTime":1623828415062,"entityType":"staff","labInGate":"7883","inDoor":1623811387958,"userId":"4697","beacons":"BTI24007888(4800)","areaId":2,"volt":3600,"absolute":true,"x":530.66,"y":58.919,"z":0,"rootAreaId":1},"BTT32003884":{"altitude":5.9,"tagId":"BTT32003884","latitude":32.26320231655702,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003884","gateId":"7883","labId":2155,"floor":1,"longitude":119.11389073885546,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827816397,"locationTime":1623827816397,"pingTime":1623827886580,"entityType":"staff","labInGate":"7883","inDoor":1623812847459,"entityId":24306,"userId":"3884","beacons":"BTI24007883(3050)","areaId":2,"volt":3743,"absolute":true,"x":533.65,"y":74.789,"z":0,"rootAreaId":1},"BTT32004698":{"altitude":5.9,"tagId":"BTT32004698","latitude":32.26396119872558,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004698","gateId":null,"labId":0,"floor":1,"longitude":119.1093451108463,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829408455,"pingTime":1623829464016,"entityType":"staff","labInGate":null,"inDoor":1623819217972,"userId":"4698","beacons":"BTI24007108(5500)","areaId":2,"volt":3700,"absolute":true,"x":105.35,"y":158.93,"z":0,"rootAreaId":1},"BTT32003881":{"altitude":5.9,"tagId":"BTT32003881","latitude":32.2628330538563,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003881","gateId":"7904","labId":2154,"floor":1,"longitude":119.11418550987014,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827790406,"locationTime":1623829502516,"pingTime":1623829502516,"entityType":"staff","labInGate":"7886","inDoor":1623813277188,"entityId":24303,"userId":"3881","beacons":"BTI24007904(3250),BTI24007886(3950)","areaId":2,"volt":4043,"absolute":true,"x":561.426,"y":33.843,"z":0,"rootAreaId":1},"BTT32004690":{"altitude":5.9,"tagId":"BTT32004690","latitude":32.26262652972322,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004690","gateId":null,"labId":0,"floor":1,"longitude":119.11120770086121,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829417323,"pingTime":1623829449940,"entityType":"staff","labInGate":null,"inDoor":1623812126571,"userId":"4690","beacons":"BTI24006148(12750)","areaId":2,"volt":3450,"absolute":true,"x":280.85,"y":10.93,"z":0,"rootAreaId":1},"BTT32004691":{"altitude":5.9,"tagId":"BTT32004691","latitude":32.262807834826305,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004691","gateId":"5241","labId":2153,"floor":1,"longitude":119.11479180310005,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623815142439,"locationTime":1623829398524,"pingTime":1623829398524,"entityType":"staff","labInGate":"5241","inDoor":1623815138984,"userId":"4691","beacons":"BTI22085241(2850),BTI24007915(3950)","areaId":2,"volt":3500,"absolute":true,"x":618.552,"y":31.05,"z":0,"rootAreaId":1},"BTT32003917":{"altitude":5.9,"tagId":"BTT32003917","latitude":32.266808672691276,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003917","gateId":"7843","labId":2157,"floor":1,"longitude":119.11299814440703,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623829412064,"locationTime":1623829499878,"pingTime":1623829499878,"entityType":"staff","labInGate":"7845","inDoor":1623829259060,"entityId":24337,"userId":"3917","beacons":"BTI24007843(2350)","areaId":2,"volt":3850,"absolute":true,"x":449.529,"y":474.7,"z":0,"rootAreaId":1},"BTT32003919":{"altitude":5.9,"tagId":"BTT32003919","latitude":32.263956872569146,"locationType":"location","lostTime":0,"speed":2.689,"nowTime":1623829502543,"out":false,"uid":"BTT32003919","gateId":null,"labId":0,"floor":1,"longitude":119.10902915295442,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499883,"pingTime":1623829499883,"entityType":"staff","labInGate":null,"inDoor":1623810172288,"entityId":24339,"userId":"3919","beacons":"BTI24007092(4250),BTI24007148(5500)","areaId":2,"volt":3643,"absolute":true,"x":75.58,"y":158.45,"z":0,"rootAreaId":1},"BTT32003758":{"altitude":5.9,"tagId":"BTT32003758","latitude":32.26283758823524,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003758","gateId":"7904","labId":2154,"floor":1,"longitude":119.11418460357926,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827520591,"locationTime":1623828495591,"pingTime":1623828495591,"entityType":"staff","labInGate":"7886","inDoor":1623811281403,"entityId":24198,"userId":"3758","beacons":"BTI24007904(4500),BTI24007886(5150)","areaId":2,"volt":3900,"absolute":true,"x":561.34,"y":34.346,"z":0,"rootAreaId":1},"BTT32004727":{"altitude":5.9,"tagId":"BTT32004727","latitude":32.266200251158914,"locationType":"location","lostTime":0,"speed":1.051,"nowTime":1623829502543,"out":false,"uid":"BTT32004727","gateId":null,"labId":0,"floor":1,"longitude":119.11131746086821,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502383,"pingTime":1623829502383,"entityType":"staff","labInGate":null,"inDoor":1623810477901,"userId":"4727","beacons":"BTI24007477(4500),BTI24007478(12750)","areaId":2,"volt":3900,"absolute":true,"x":291.18,"y":407.225,"z":0,"rootAreaId":1},"BTT32004849":{"altitude":5.9,"tagId":"BTT32004849","latitude":32.265487543949874,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004849","gateId":null,"labId":0,"floor":1,"longitude":119.11471650720098,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623828013203,"pingTime":1623828013203,"entityType":"staff","labInGate":null,"inDoor":1623813539323,"userId":"4849","beacons":"BTI22085235(1450),BTI22085233(5900)","areaId":2,"volt":3500,"absolute":true,"x":611.44,"y":328.206,"z":0,"rootAreaId":1},"BTT32003915":{"altitude":5.9,"tagId":"BTT32003915","latitude":32.263205941815514,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003915","gateId":"7912","labId":2155,"floor":1,"longitude":119.11347088052494,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623829468941,"locationTime":1623829502410,"pingTime":1623829502410,"entityType":"staff","labInGate":"7912","inDoor":1623816508189,"entityId":24335,"userId":"3915","beacons":"BTI24007912(4250)","areaId":2,"volt":3550,"absolute":true,"x":494.09,"y":75.19,"z":0,"rootAreaId":1},"BTT32004689":{"altitude":5.9,"tagId":"BTT32004689","latitude":32.26645475638512,"locationType":"location","lostTime":0,"speed":0.088,"nowTime":1623829502543,"out":false,"uid":"BTT32004689","gateId":null,"labId":0,"floor":1,"longitude":119.10994563165346,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502342,"pingTime":1623829502342,"entityType":"car","labInGate":null,"inDoor":1623819917510,"userId":"4689","beacons":"BTI24007295(5900)","areaId":2,"volt":4093,"absolute":true,"x":161.927,"y":435.444,"z":0,"rootAreaId":1},"BTT32003755":{"altitude":5.9,"tagId":"BTT32003755","latitude":32.263225864523974,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003755","gateId":"6816","labId":2155,"floor":1,"longitude":119.11362976144677,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827824952,"locationTime":1623828455666,"pingTime":1623828455666,"entityType":"staff","labInGate":"6816","inDoor":1623810172150,"entityId":24197,"userId":"3755","beacons":"BTI24006816(2350)","areaId":2,"volt":3993,"absolute":true,"x":509.06,"y":77.4,"z":0,"rootAreaId":1},"BTT32003910":{"altitude":5.9,"tagId":"BTT32003910","latitude":32.26305124702239,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003910","gateId":"7907","labId":2154,"floor":1,"longitude":119.11433881840587,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623826815987,"locationTime":1623828340566,"pingTime":1623828340566,"entityType":"staff","labInGate":"7909","inDoor":1623826738175,"entityId":24330,"userId":"3910","beacons":"BTI24007907(4500)","areaId":2,"volt":3800,"absolute":true,"x":575.87,"y":58.039,"z":0,"rootAreaId":1},"BTT32003635":{"altitude":5.9,"tagId":"BTT32003635","latitude":32.263292956449604,"locationType":"location","lostTime":0,"speed":0.939,"nowTime":1623829502543,"out":false,"uid":"BTT32003635","gateId":null,"labId":0,"floor":1,"longitude":119.11544897734554,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499881,"pingTime":1623829499881,"entityType":"staff","labInGate":null,"inDoor":1623829492264,"userId":"3635","beacons":"BTI24006800(5900)","areaId":2,"volt":4043,"absolute":true,"x":680.47,"y":84.85,"z":0,"rootAreaId":1},"BTT32003750":{"altitude":5.9,"tagId":"BTT32003750","latitude":32.26540477813246,"locationType":"location","lostTime":0,"speed":2.879,"nowTime":1623829502543,"out":false,"uid":"BTT32003750","gateId":null,"labId":0,"floor":1,"longitude":119.11496591351853,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502446,"pingTime":1623829502446,"entityType":"staff","labInGate":null,"inDoor":1623810186087,"entityId":24193,"userId":"3750","beacons":"BTI24007736(3950)","areaId":2,"volt":3743,"absolute":true,"x":634.94,"y":319.03,"z":0,"rootAreaId":1},"BTT32003630":{"altitude":5.9,"tagId":"BTT32003630","latitude":32.26318392035775,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003630","gateId":null,"labId":0,"floor":1,"longitude":119.11550146157911,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502370,"pingTime":1623829502370,"entityType":"staff","labInGate":null,"inDoor":1623829316885,"entityId":28047,"userId":"3630","beacons":"BTI24007919(4800),BTI22085201(5900)","areaId":2,"volt":4000,"absolute":true,"x":685.415,"y":72.759,"z":0,"rootAreaId":1},"BTT32003632":{"altitude":5.9,"tagId":"BTT32003632","latitude":32.26316345704328,"locationType":"location","lostTime":0,"speed":0.075,"nowTime":1623829502543,"out":false,"uid":"BTT32003632","gateId":null,"labId":0,"floor":1,"longitude":119.11550771117629,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499888,"pingTime":1623829499888,"entityType":"staff","labInGate":null,"inDoor":1623829442485,"entityId":28043,"userId":"3632","beacons":"BTI22085201(5150),BTI22085225(5150)","areaId":2,"volt":3800,"absolute":true,"x":686.005,"y":70.49,"z":0,"rootAreaId":1},"BTT32004680":{"altitude":5.9,"tagId":"BTT32004680","latitude":32.26674742432508,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004680","gateId":null,"labId":0,"floor":4,"longitude":119.1103151238622,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829495767,"pingTime":1623829495767,"entityType":"car","labInGate":null,"inDoor":1623821006789,"userId":"4680","beacons":"BTI24007345(12750)","areaId":5,"volt":3800,"absolute":true,"x":196.739,"y":467.9,"z":0,"rootAreaId":1},"BTT32003901":{"altitude":5.9,"tagId":"BTT32003901","latitude":32.26305920501626,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003901","gateId":"7888","labId":2155,"floor":1,"longitude":119.1138589965235,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623826110501,"locationTime":1623829264239,"pingTime":1623829264239,"entityType":"staff","labInGate":"7889","inDoor":1623825963504,"entityId":24322,"userId":"3901","beacons":"BTI24007888(4500)","areaId":2,"volt":3850,"absolute":true,"x":530.66,"y":58.919,"z":0,"rootAreaId":1},"BTT32003903":{"altitude":5.9,"tagId":"BTT32003903","latitude":32.263205941815514,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003903","gateId":"7912","labId":2155,"floor":1,"longitude":119.11347088052494,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623823896750,"locationTime":1623829499866,"pingTime":1623829499866,"entityType":"staff","labInGate":"7883","inDoor":1623810172182,"entityId":24324,"userId":"3903","beacons":"BTI24007912(4800)","areaId":2,"volt":4000,"absolute":true,"x":494.09,"y":75.19,"z":0,"rootAreaId":1},"BTT32003749":{"altitude":5.9,"tagId":"BTT32003749","latitude":32.26292352315963,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003749","gateId":"7913","labId":2153,"floor":1,"longitude":119.11495639057719,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623815939181,"locationTime":1623829417308,"pingTime":1623829417308,"entityType":"staff","labInGate":"5241","inDoor":1623815935820,"entityId":24192,"userId":"3749","beacons":"BTI22085221(4250)","areaId":2,"volt":4100,"absolute":true,"x":634.06,"y":43.88,"z":0,"rootAreaId":1},"BTT32003629":{"altitude":5.9,"tagId":"BTT32003629","latitude":32.26659216294071,"locationType":"location","lostTime":0,"speed":2.471,"nowTime":1623829502543,"out":false,"uid":"BTT32003629","gateId":"7847","labId":2157,"floor":1,"longitude":119.11277524741026,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827444664,"locationTime":1623828361071,"pingTime":1623828367132,"entityType":"staff","labInGate":"7847","inDoor":1623810230816,"entityId":24112,"userId":"3629","beacons":"BTI24007847(4800)","areaId":2,"volt":3950,"absolute":true,"x":428.53,"y":450.69,"z":0,"rootAreaId":1},"BTT32004839":{"altitude":5.9,"tagId":"BTT32004839","latitude":32.26305920501626,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004839","gateId":"7888","labId":2155,"floor":1,"longitude":119.1138589965235,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623828004553,"locationTime":1623829400188,"pingTime":1623829400188,"entityType":"staff","labInGate":"7888","inDoor":1623811598260,"userId":"4839","beacons":"BTI24007888(4800)","areaId":2,"volt":3950,"absolute":true,"x":530.66,"y":58.919,"z":0,"rootAreaId":1},"BTT32004799":{"altitude":5.9,"tagId":"BTT32004799","latitude":32.264329670477885,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004799","gateId":null,"labId":0,"floor":2,"longitude":119.10887547507092,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499816,"pingTime":1623829499816,"entityType":"staff","labInGate":null,"inDoor":1623819291143,"userId":"4799","beacons":"BTI24007126(5900),BTI24011534(5900)","areaId":3,"volt":3800,"absolute":true,"x":61.1,"y":199.789,"z":0,"rootAreaId":1},"BTT32003623":{"altitude":5.9,"tagId":"BTT32003623","latitude":32.26282180802786,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003623","gateId":"5241","labId":2153,"floor":1,"longitude":119.11484292834353,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623816002030,"locationTime":1623829404411,"pingTime":1623829404411,"entityType":"staff","labInGate":"5241","inDoor":1623816001941,"entityId":24108,"userId":"3623","beacons":"BTI22085241(5900)","areaId":2,"volt":3500,"absolute":true,"x":623.37,"y":32.6,"z":0,"rootAreaId":1},"BTT32003861":{"altitude":5.9,"tagId":"BTT32003861","latitude":32.262964911321745,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003861","gateId":"7913","labId":2153,"floor":1,"longitude":119.11495738415022,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623823459027,"locationTime":1623828546648,"pingTime":1623828604845,"entityType":"staff","labInGate":"5221","inDoor":1623823312262,"entityId":24283,"userId":"3861","beacons":"BTI24007913(4800),BTI22085221(5150)","areaId":2,"volt":4000,"absolute":true,"x":634.153,"y":48.469,"z":0,"rootAreaId":1},"BTT32004830":{"altitude":5.9,"tagId":"BTT32004830","latitude":32.263225864523974,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004830","gateId":"6816","labId":2155,"floor":1,"longitude":119.11362976144677,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623826646651,"locationTime":1623829362931,"pingTime":1623829418391,"entityType":"staff","labInGate":"6816","inDoor":1623822019428,"userId":"4830","beacons":"BTI24006816(2850)","areaId":2,"volt":4143,"absolute":true,"x":509.06,"y":77.4,"z":0,"rootAreaId":1},"BTT32004671":{"altitude":5.9,"tagId":"BTT32004671","latitude":32.263043971508225,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004671","gateId":"7889","labId":2155,"floor":1,"longitude":119.1137083944118,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623828763151,"locationTime":1623829318389,"pingTime":1623829318389,"entityType":"staff","labInGate":"7888","inDoor":1623814160799,"entityId":24259,"userId":"4671","beacons":"BTI24007889(4800)","areaId":2,"volt":4143,"absolute":true,"x":516.47,"y":57.23,"z":0,"rootAreaId":1},"BTT32004672":{"altitude":5.9,"tagId":"BTT32004672","latitude":32.2638542453209,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004672","gateId":null,"labId":0,"floor":1,"longitude":119.10954771713854,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502447,"pingTime":1623829502447,"entityType":"staff","labInGate":null,"inDoor":1623811083037,"userId":"4672","beacons":"BTI24006178(5900)","areaId":2,"volt":3500,"absolute":true,"x":124.44,"y":147.07,"z":0,"rootAreaId":1},"BTT32004794":{"altitude":5.9,"tagId":"BTT32004794","latitude":32.26295720940465,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004794","gateId":"5221","labId":2153,"floor":1,"longitude":119.11495719925628,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623810665402,"locationTime":1623829499859,"pingTime":1623829499859,"entityType":"staff","labInGate":"5221","inDoor":1623810663256,"userId":"4794","beacons":"BTI22085221(5150),BTI24007913(5900)","areaId":2,"volt":3700,"absolute":true,"x":634.135,"y":47.615,"z":0,"rootAreaId":1},"BTT32004704":{"altitude":5.9,"tagId":"BTT32004704","latitude":32.265586162808525,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004704","gateId":null,"labId":0,"floor":1,"longitude":119.11424951660351,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829260226,"pingTime":1623829304859,"entityType":"staff","labInGate":null,"inDoor":1623810177105,"userId":"4704","beacons":"BTI24007747(12750)","areaId":2,"volt":3500,"absolute":true,"x":567.44,"y":339.139,"z":0,"rootAreaId":1},"BTT32003616":{"altitude":5.9,"tagId":"BTT32003616","latitude":32.26305920501626,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003616","gateId":"7888","labId":2155,"floor":1,"longitude":119.1138589965235,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623815784670,"locationTime":1623829079672,"pingTime":1623829130934,"entityType":"staff","labInGate":"7888","inDoor":1623814637200,"entityId":24101,"userId":"3616","beacons":"BTI24007888(2050)","areaId":2,"volt":3900,"absolute":true,"x":530.66,"y":58.919,"z":0,"rootAreaId":1},"BTT32003737":{"altitude":5.9,"tagId":"BTT32003737","latitude":32.2629989121229,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003737","gateId":"7913","labId":2153,"floor":1,"longitude":119.11495820038135,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623815813131,"locationTime":1623828649730,"pingTime":1623828654625,"entityType":"staff","labInGate":"5221","inDoor":1623815813905,"entityId":24184,"userId":"3737","beacons":"BTI24007913(5900)","areaId":2,"volt":4143,"absolute":true,"x":634.23,"y":52.24,"z":0,"rootAreaId":1},"BTT32003617":{"altitude":5.9,"tagId":"BTT32003617","latitude":32.2629989121229,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003617","gateId":"7913","labId":2153,"floor":1,"longitude":119.11495820038135,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623815887896,"locationTime":1623829431617,"pingTime":1623829431621,"entityType":"staff","labInGate":"5241","inDoor":1623815887540,"entityId":24102,"userId":"3617","beacons":"BTI24007913(4500)","areaId":2,"volt":3850,"absolute":true,"x":634.23,"y":52.24,"z":0,"rootAreaId":1},"BTT32003739":{"altitude":5.9,"tagId":"BTT32003739","latitude":32.26550822348437,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003739","gateId":null,"labId":0,"floor":1,"longitude":119.11471911670297,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829450237,"pingTime":1623829450238,"entityType":"staff","labInGate":null,"inDoor":1623810809071,"userId":"3739","beacons":"BTI22085233(900),BTI22085235(2350)","areaId":2,"volt":4050,"absolute":true,"x":611.686,"y":330.499,"z":0,"rootAreaId":1},"BTT32004822":{"altitude":5.9,"tagId":"BTT32004822","latitude":32.266808672691276,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004822","gateId":"7843","labId":2157,"floor":1,"longitude":119.11299814440703,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623828072160,"locationTime":1623829477431,"pingTime":1623829477431,"entityType":"staff","labInGate":"7845","inDoor":1623827889862,"userId":"4822","beacons":"BTI24007843(4800)","areaId":2,"volt":4000,"absolute":true,"x":449.529,"y":474.7,"z":0,"rootAreaId":1},"BTT32004823":{"altitude":5.9,"tagId":"BTT32004823","latitude":32.26286863255495,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004823","gateId":"7886","labId":2154,"floor":1,"longitude":119.11417839871747,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623828264186,"locationTime":1623829443060,"pingTime":1623829446431,"entityType":"staff","labInGate":"7886","inDoor":1623811522215,"userId":"4823","beacons":"BTI24007886(4500),BTI24007904(4800)","areaId":2,"volt":3850,"absolute":true,"x":560.756,"y":37.788,"z":0,"rootAreaId":1},"BTT32003856":{"altitude":5.9,"tagId":"BTT32003856","latitude":32.266498427358194,"locationType":"location","lostTime":0,"speed":3.855,"nowTime":1623829502543,"out":false,"uid":"BTT32003856","gateId":"7847","labId":2157,"floor":1,"longitude":119.11272244508818,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623811190772,"locationTime":1623828876949,"pingTime":1623828922337,"entityType":"staff","labInGate":"7844","inDoor":1623810620665,"entityId":24278,"userId":"3856","beacons":"BTI24006609(5900)","areaId":2,"volt":3600,"absolute":true,"x":423.555,"y":440.295,"z":0,"rootAreaId":1},"BTT32004663":{"altitude":5.9,"tagId":"BTT32004663","latitude":32.26646968791875,"locationType":"location","lostTime":0,"speed":4.136,"nowTime":1623829502543,"out":false,"uid":"BTT32004663","gateId":null,"labId":0,"floor":1,"longitude":119.11149350891272,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502349,"pingTime":1623829502349,"entityType":"staff","labInGate":null,"inDoor":1623820008225,"userId":"4663","beacons":"BTI24007414(12750),BTI24007404(12750),BTI24007403(12750),BTI24007415(12750)","areaId":2,"volt":3600,"absolute":true,"x":307.766,"y":437.104,"z":0,"rootAreaId":1},"BTT32003850":{"altitude":5.9,"tagId":"BTT32003850","latitude":32.2632096008528,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003850","gateId":"7909","labId":2154,"floor":1,"longitude":119.11432354599614,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623829150895,"locationTime":1623829499865,"pingTime":1623829499865,"entityType":"staff","labInGate":"7886","inDoor":1623810172162,"entityId":24272,"userId":"3850","beacons":"BTI24007909(5900)","areaId":2,"volt":3800,"absolute":true,"x":574.43,"y":75.599,"z":0,"rootAreaId":1},"BTT32003730":{"altitude":5.9,"tagId":"BTT32003730","latitude":32.263218953646735,"locationType":"location","lostTime":0,"speed":0.106,"nowTime":1623829502543,"out":false,"uid":"BTT32003730","gateId":null,"labId":0,"floor":1,"longitude":119.11549049201881,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499879,"pingTime":1623829499879,"entityType":"staff","labInGate":null,"inDoor":1623829495772,"entityId":28054,"userId":"3730","beacons":"BTI22085202(4800),BTI24007919(5500)","areaId":2,"volt":3600,"absolute":true,"x":684.382,"y":76.643,"z":0,"rootAreaId":1},"BTT32004787":{"altitude":5.9,"tagId":"BTT32004787","latitude":32.26590396514772,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004787","gateId":null,"labId":0,"floor":4,"longitude":119.11137550164923,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502431,"pingTime":1623829502431,"entityType":"staff","labInGate":null,"inDoor":1623810183276,"userId":"4787","beacons":"BTI24007532(12750)","areaId":5,"volt":3943,"absolute":true,"x":296.65,"y":374.369,"z":0,"rootAreaId":1},"BTT32004661":{"altitude":5.9,"tagId":"BTT32004661","latitude":32.26546357077879,"locationType":"location","lostTime":0,"speed":0.137,"nowTime":1623829502543,"out":false,"uid":"BTT32004661","gateId":"7036","labId":2160,"floor":1,"longitude":119.10909358998174,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623828414541,"locationTime":1623828807339,"pingTime":1623828859115,"entityType":"staff","labInGate":"7036","inDoor":1623810185981,"userId":"4661","beacons":"BTI24007036(4800)","areaId":2,"volt":3650,"absolute":true,"x":81.65,"y":325.53,"z":0,"rootAreaId":1},"BTT34039792":{"altitude":5.9,"tagId":"BTT34039792","latitude":32.2632656287448,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT34039792","gateId":null,"labId":0,"floor":1,"longitude":119.11551307900704,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623828340529,"pingTime":1623828340529,"entityType":"car","labInGate":null,"inDoor":1623828302411,"userId":"9792","beacons":"BTI22085202(5900)","areaId":2,"volt":3600,"absolute":true,"x":686.51,"y":81.819,"z":0,"rootAreaId":1},"BTT34039790":{"altitude":5.9,"tagId":"BTT34039790","latitude":32.2632656287448,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT34039790","gateId":null,"labId":0,"floor":1,"longitude":119.11551307900704,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829446447,"pingTime":1623829446449,"entityType":"car","labInGate":null,"inDoor":1623827351933,"userId":"9790","beacons":"BTI22085202(5150)","areaId":2,"volt":3800,"absolute":true,"x":686.51,"y":81.819,"z":0,"rootAreaId":1},"BTT32003608":{"altitude":5.9,"tagId":"BTT32003608","latitude":32.26518964879175,"locationType":"location","lostTime":0,"speed":2.683,"nowTime":1623829502543,"out":false,"uid":"BTT32003608","gateId":null,"labId":0,"floor":1,"longitude":119.11184772912132,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499863,"pingTime":1623829499863,"entityType":"staff","labInGate":null,"inDoor":1623810172144,"entityId":24093,"userId":"3608","beacons":"BTI24007866(5500)","areaId":2,"volt":4043,"absolute":true,"x":341.145,"y":295.159,"z":0,"rootAreaId":1},"BTT32004818":{"altitude":5.9,"tagId":"BTT32004818","latitude":32.264031451694926,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004818","gateId":null,"labId":0,"floor":1,"longitude":119.10869409156375,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829389802,"pingTime":1623829429457,"entityType":"staff","labInGate":null,"inDoor":1623819922956,"userId":"4818","beacons":"BTI24007074(3950)","areaId":2,"volt":3900,"absolute":true,"x":44.01,"y":166.72,"z":0,"rootAreaId":1},"BTT34039789":{"altitude":5.9,"tagId":"BTT34039789","latitude":32.263244412193316,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT34039789","gateId":null,"labId":0,"floor":1,"longitude":119.11550617872314,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829447718,"pingTime":1623829447719,"entityType":"car","labInGate":null,"inDoor":1623829130847,"userId":"9789","beacons":"BTI22085202(3950)","areaId":2,"volt":4000,"absolute":true,"x":685.86,"y":79.467,"z":0,"rootAreaId":1},"BTT32003605":{"altitude":5.9,"tagId":"BTT32003605","latitude":32.262954935227775,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003605","gateId":"5221","labId":2153,"floor":1,"longitude":119.1149571446619,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623816174125,"locationTime":1623829233357,"pingTime":1623829233357,"entityType":"staff","labInGate":"5221","inDoor":1623816173151,"entityId":24090,"userId":"3605","beacons":"BTI22085221(3250),BTI24007913(4250)","areaId":2,"volt":3900,"absolute":true,"x":634.13,"y":47.363,"z":0,"rootAreaId":1},"BTT34039784":{"altitude":5.9,"tagId":"BTT34039784","latitude":32.2632656287448,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT34039784","gateId":null,"labId":0,"floor":1,"longitude":119.11551307900704,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829182945,"pingTime":1623829182945,"entityType":"car","labInGate":null,"inDoor":1623829162487,"userId":"9784","beacons":"BTI22085202(4800)","areaId":2,"volt":3600,"absolute":true,"x":686.51,"y":81.819,"z":0,"rootAreaId":1},"BTT32003849":{"altitude":5.9,"tagId":"BTT32003849","latitude":32.26456240038791,"locationType":"location","lostTime":0,"speed":7.603,"nowTime":1623829502543,"out":false,"uid":"BTT32003849","gateId":null,"labId":0,"floor":1,"longitude":119.11371212988044,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499880,"pingTime":1623829499880,"entityType":"staff","labInGate":null,"inDoor":1623810231770,"entityId":24271,"userId":"3849","beacons":"BTI24011563(3950)","areaId":2,"volt":3650,"absolute":true,"x":516.813,"y":225.61,"z":0,"rootAreaId":1},"BTT34039786":{"altitude":5.9,"tagId":"BTT34039786","latitude":32.2632656287448,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT34039786","gateId":null,"labId":0,"floor":1,"longitude":119.11551307900704,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829289896,"pingTime":1623829289896,"entityType":"car","labInGate":null,"inDoor":1623827878788,"userId":"9786","beacons":"BTI22085202(3950)","areaId":2,"volt":3650,"absolute":true,"x":686.51,"y":81.819,"z":0,"rootAreaId":1},"BTT32004777":{"altitude":5.9,"tagId":"BTT32004777","latitude":32.26278128023975,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004777","gateId":"6860","labId":2210,"floor":1,"longitude":119.11100839077851,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623828700374,"locationTime":1623829029064,"pingTime":1623829054953,"entityType":"staff","labInGate":"6860","inDoor":1623820143126,"userId":"4777","beacons":"BTI24006860(3450)","areaId":2,"volt":3600,"absolute":true,"x":262.07,"y":28.09,"z":0,"rootAreaId":1},"BTT36000006":{"altitude":5.9,"tagId":"BTT36000006","latitude":32.26274469962506,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT36000006","gateId":"7899","labId":2156,"floor":1,"longitude":119.11296948853794,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":1623815062222,"locationTime":1623829442357,"pingTime":1623829501170,"entityType":"car","labInGate":"7899","inDoor":1623815059817,"entityId":25432,"userId":"0006","beacons":"BTI24007899(3700),BTI24007761(12750)","areaId":2,"volt":3950,"absolute":true,"x":446.85,"y":24.039,"z":0,"rootAreaId":1},"BTT32003843":{"altitude":5.9,"tagId":"BTT32003843","latitude":32.26282180802786,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32003843","gateId":"5241","labId":2153,"floor":1,"longitude":119.11484292834353,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623824180345,"locationTime":1623829499862,"pingTime":1623829499862,"entityType":"staff","labInGate":"7916","inDoor":1623810178551,"entityId":24266,"userId":"3843","beacons":"BTI22085241(1200)","areaId":2,"volt":3800,"absolute":true,"x":623.37,"y":32.6,"z":0,"rootAreaId":1},"BTT32004657":{"altitude":5.9,"tagId":"BTT32004657","latitude":32.26674742432508,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004657","gateId":null,"labId":0,"floor":4,"longitude":119.1103151238622,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829499875,"pingTime":1623829499875,"entityType":"car","labInGate":null,"inDoor":1623821155479,"userId":"4657","beacons":"BTI24007345(12750)","areaId":5,"volt":3600,"absolute":true,"x":196.739,"y":467.9,"z":0,"rootAreaId":1},"BTT32004658":{"altitude":5.9,"tagId":"BTT32004658","latitude":32.26431507998437,"locationType":"location","lostTime":0,"speed":1.419,"nowTime":1623829502543,"out":false,"uid":"BTT32004658","gateId":null,"labId":0,"floor":1,"longitude":119.10943973309551,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502388,"pingTime":1623829502388,"entityType":"car","labInGate":null,"inDoor":1623820232382,"userId":"4658","beacons":"BTI24011529(12750)","areaId":2,"volt":3600,"absolute":true,"x":114.265,"y":198.172,"z":0,"rootAreaId":1},"BTT32004659":{"altitude":5.9,"tagId":"BTT32004659","latitude":32.266355164858524,"locationType":"location","lostTime":0,"speed":7.779,"nowTime":1623829502543,"out":false,"uid":"BTT32004659","gateId":null,"labId":0,"floor":1,"longitude":119.1115892312966,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829502522,"pingTime":1623829502522,"entityType":"car","labInGate":null,"inDoor":1623810177134,"userId":"4659","beacons":"BTI24007460(4250),BTI24007413(4800)","areaId":2,"volt":3400,"absolute":true,"x":316.785,"y":424.404,"z":0,"rootAreaId":1},"BTT32004652":{"altitude":5.9,"tagId":"BTT32004652","latitude":32.26643849801839,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004652","gateId":"7844","labId":2157,"floor":1,"longitude":119.11309288907792,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623827779894,"locationTime":1623827785873,"pingTime":1623827834780,"entityType":"car","labInGate":"7844","inDoor":1623827470900,"userId":"4652","beacons":"BTI24007844(3450),BTI24006613(4250),BTI24007845(4800)","areaId":2,"volt":3900,"absolute":true,"x":458.458,"y":433.651,"z":0,"rootAreaId":1},"BTT32004654":{"altitude":5.9,"tagId":"BTT32004654","latitude":32.26624374461967,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004654","gateId":null,"labId":0,"floor":1,"longitude":119.11158393771618,"deviceType":"Tag","outDoor":0,"silent":true,"labInTime":0,"locationTime":1623829390859,"pingTime":1623829392820,"entityType":"car","labInGate":null,"inDoor":1623825807319,"userId":"4654","beacons":"BTI24007460(4500),BTI24007462(4500),BTI24007461(12750)","areaId":2,"volt":3850,"absolute":true,"x":316.287,"y":412.049,"z":0,"rootAreaId":1},"BTT32004897":{"altitude":5.9,"tagId":"BTT32004897","latitude":32.26262159007003,"locationType":"location","lostTime":0,"speed":0,"nowTime":1623829502543,"out":false,"uid":"BTT32004897","gateId":null,"labId":0,"floor":1,"longitude":119.11017451140597,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":0,"locationTime":1623829354546,"pingTime":1623829410860,"entityType":"staff","labInGate":null,"inDoor":1623819681344,"userId":"4897","beacons":"BTI24006157(12750)","areaId":2,"volt":4093,"absolute":true,"x":183.5,"y":10.38,"z":0,"rootAreaId":1},"BTT36000007":{"altitude":5.9,"tagId":"BTT36000007","latitude":32.26272581142244,"locationType":"location","lostTime":0,"speed":0.941,"nowTime":1623829502543,"out":false,"uid":"BTT36000007","gateId":"6859","labId":2210,"floor":1,"longitude":119.1110197451609,"deviceType":"Tag","outDoor":0,"silent":false,"labInTime":1623829457031,"locationTime":1623829461511,"pingTime":1623829492293,"entityType":"car","labInGate":"6859","inDoor":1623816786383,"entityId":25435,"userId":"0007","beacons":"BTI24006859(3950),BTI24006151(12750)","areaId":2,"volt":4000,"absolute":true,"x":263.14,"y":21.939,"z":0,"rootAreaId":1}},"id":1,"jsonrpc":"2.0"}
			 */
			resultMap=JSON.parseObject(resultJO.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 2.2.19获取历史轨迹
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getLocationRecords")
	@ResponseBody
	public Map<String, Object> getLocationRecords(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String tagId=request.getParameter("tagId");
			String todayDate=request.getParameter("todayDate");
			String startTime=request.getParameter("startTime");
			String endTime=request.getParameter("endTime");
			Integer ysb = Integer.valueOf(request.getParameter("ysb"));
		
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getLocationRecords");
			JSONObject paramJO=new JSONObject();
			/*
			paramJO.put("tagId", "BTT32003897");
			paramJO.put("areaId", "1");
			paramJO.put("startTime", "1624204800000");
			paramJO.put("endTime", "1624266053514");
			*/
			paramJO.put("tagId", tagId);
			paramJO.put("areaId", "1");
			Long startTimeLong = DateUtil.convertStringToLong(todayDate+" "+startTime);
			Long endTimeLong = DateUtil.convertStringToLong(todayDate+" "+endTime);
			//Long startTimeLong = Long.valueOf("1635696011258");
			//Long endTimeLong = Long.valueOf("1635702257624");
			paramJO.put("startTime", startTimeLong);
			paramJO.put("endTime", endTimeLong);
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getLocationRecords",request);
			//JSONObject resultJO = APIResultUtil.getLocationRecords();
			System.out.println("getLocationRecords:resultJO==="+resultJO.toString());
			/*
			 {"result":[
			 {"altitude":5.9,"flag":1,"tagId":"BTT32003897","latitude":32.26538412419374,"engineType":null,"speed":0.185,"recordId":210610029309169657,"voltUnit":"V","labelId":"2160","gateId":"7034","routerId":"BTR0794E313","routerMark":274,"jGateId":"7034","id":63726006,"state":0,"floor":1,"routerFlowId":144213,"flowId":603981723,"direction":6,"longitude":119.10902258516153,"entityId":24318,"raiseTime2":1624241823586,"uploadTime":1624241823588,"userId":"3897","beacons":"BTI24007034(4500)","blockId":null,"intensity":4500,"areaId":2,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:17:03.586+0800","x":74.959,"y":316.72,"gpsType":"wgs84","z":0,"step":1,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.26538412419374,"engineType":null,"speed":0,"recordId":210610029309171037,"voltUnit":"V","labelId":"2160","gateId":"7034","routerId":"BTR0794E313","routerMark":274,"jGateId":"7034","id":63726446,"state":0,"floor":1,"routerFlowId":144213,"flowId":603981723,"direction":6,"longitude":119.10902258516153,"entityId":24318,"raiseTime2":1624241854675,"uploadTime":1624241854685,"userId":"3897","beacons":"BTI24007034(4500)","blockId":null,"intensity":4500,"areaId":2,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:17:34.675+0800","x":74.959,"y":316.72,"gpsType":"wgs84","z":0,"step":1,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.265325310116005,"engineType":null,"speed":0.099,"recordId":210610029309171395,"voltUnit":"V","labelId":"2160","gateId":"7043","routerId":"BTR0794E313","routerMark":274,"jGateId":"7043","id":63726551,"state":0,"floor":2,"routerFlowId":144980,"flowId":603981769,"direction":6,"longitude":119.10906042099948,"entityId":24318,"raiseTime2":1624241863226,"uploadTime":1624241863232,"userId":"3897","beacons":"BTI24007043(5500),BTI24007042(5500),BTI24007044(5900)","blockId":null,"intensity":5633,"areaId":3,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:17:43.226+0800","x":78.524,"y":310.198,"gpsType":"wgs84","z":0,"step":3,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.26526649602639,"engineType":null,"speed":0.162,"recordId":210610029309171484,"voltUnit":"V","labelId":"2160","gateId":"7043","routerId":"BTR0794E313","routerMark":274,"jGateId":"7043","id":63726587,"state":0,"floor":2,"routerFlowId":144980,"flowId":603981769,"direction":6,"longitude":119.10909825678866,"entityId":24318,"raiseTime2":1624241864738,"uploadTime":1624241864748,"userId":"3897","beacons":"BTI24007043(5500),BTI24007042(5500),BTI24007044(5900)","blockId":null,"intensity":5633,"areaId":3,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:17:44.738+0800","x":82.089,"y":303.676,"gpsType":"wgs84","z":0,"step":1,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.26533807026467,"engineType":null,"speed":0.212,"recordId":210610029309172571,"voltUnit":"V","labelId":"2160","gateId":"7044","routerId":"BTR9F81F8A6","routerMark":270,"jGateId":"7044","id":63726973,"state":0,"floor":2,"routerFlowId":114817,"flowId":603981795,"direction":6,"longitude":119.10907188365,"entityId":24318,"raiseTime2":1624241889102,"uploadTime":1624241889110,"userId":"3897","beacons":"BTI24007044(1800)","blockId":null,"intensity":1800,"areaId":3,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:18:09.102+0800","x":79.604,"y":311.613,"gpsType":"wgs84","z":0,"step":3,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.265409644496614,"engineType":null,"speed":0.347,"recordId":210610029309172661,"voltUnit":"V","labelId":"2160","gateId":"7044","routerId":"BTR9F81F8A6","routerMark":270,"jGateId":"7044","id":63726998,"state":0,"floor":2,"routerFlowId":114817,"flowId":603981795,"direction":6,"longitude":119.10904551046995,"entityId":24318,"raiseTime2":1624241891163,"uploadTime":1624241891173,"userId":"3897","beacons":"BTI24007044(1800)","blockId":null,"intensity":1800,"areaId":3,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:18:11.163+0800","x":77.119,"y":319.55,"gpsType":"wgs84","z":0,"step":1,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.265409644496614,"engineType":null,"speed":0,"recordId":210610029309173832,"voltUnit":"V","labelId":"2160","gateId":"7044","routerId":"BTR0794E313","routerMark":274,"jGateId":"7044","id":63727394,"state":0,"floor":2,"routerFlowId":145771,"flowId":603981819,"direction":6,"longitude":119.10904551046995,"entityId":24318,"raiseTime2":1624241917493,"uploadTime":1624241924130,"userId":"3897","beacons":"BTI24007044(2050)","blockId":null,"intensity":2050,"areaId":3,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:18:37.493+0800","x":77.119,"y":319.55,"gpsType":"wgs84","z":0,"step":1,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.265409644496614,"engineType":null,"speed":0,"recordId":210610029309173926,"voltUnit":"V","labelId":"2160","gateId":"7044","routerId":"BTR0794E313","routerMark":274,"jGateId":"7044","id":63727481,"state":0,"floor":2,"routerFlowId":145771,"flowId":603981819,"direction":6,"longitude":119.10904551046995,"entityId":24318,"raiseTime2":1624241919626,"uploadTime":1624241924245,"userId":"3897","beacons":"BTI24007044(2050)","blockId":null,"intensity":2050,"areaId":3,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:18:39.626+0800","x":77.119,"y":319.55,"gpsType":"wgs84","z":0,"step":1,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.265409644496614,"engineType":null,"speed":0,"recordId":210610029309174025,"voltUnit":"V","labelId":"2160","gateId":"7044","routerId":"BTR0794E313","routerMark":274,"jGateId":"7044","id":63727564,"state":0,"floor":2,"routerFlowId":145771,"flowId":603981819,"direction":6,"longitude":119.10904551046995,"entityId":24318,"raiseTime2":1624241921814,"uploadTime":1624241924412,"userId":"3897","beacons":"BTI24007044(2050)","blockId":null,"intensity":2050,"areaId":3,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:18:41.814+0800","x":77.119,"y":319.55,"gpsType":"wgs84","z":0,"step":1,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.265409644496614,"engineType":null,"speed":0,"recordId":210610029309173559,"voltUnit":"V","labelId":"2160","gateId":"7044","routerId":"BTR0794E313","routerMark":274,"jGateId":"7044","id":63727266,"state":0,"floor":2,"routerFlowId":145771,"flowId":603981819,"direction":6,"longitude":119.10904551046995,"entityId":24318,"raiseTime2":1624241922252,"uploadTime":1624241922263,"userId":"3897","beacons":"BTI24007044(2050)","blockId":null,"intensity":2050,"areaId":3,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:18:42.252+0800","x":77.119,"y":319.55,"gpsType":"wgs84","z":0,"step":1,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.26542256257703,"engineType":null,"speed":0.034,"recordId":210610029309178920,"voltUnit":"V","labelId":"2160","gateId":"7035","routerId":"BTR0794E313","routerMark":274,"jGateId":"7035","id":63727699,"state":0,"floor":1,"routerFlowId":147676,"flowId":603981940,"direction":6,"longitude":119.10903871796692,"entityId":24318,"raiseTime2":1624242034177,"uploadTime":1624242034429,"userId":"3897","beacons":"BTI24007035(4800)","blockId":null,"intensity":4800,"areaId":2,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:20:34.177+0800","x":76.479,"y":320.982,"gpsType":"wgs84","z":0,"step":4,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.26543951755698,"engineType":null,"speed":1.221,"recordId":210610029309179006,"voltUnit":"V","labelId":"2160","gateId":"7035","routerId":"BTR7129ED92","routerMark":107,"jGateId":"7035","id":63727741,"state":0,"floor":1,"routerFlowId":73783,"flowId":603981941,"direction":6,"longitude":119.10902980280378,"entityId":24318,"raiseTime2":1624242038570,"uploadTime":1624242038576,"userId":"3897","beacons":"BTI24007035(3700)","blockId":null,"intensity":4250,"areaId":2,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:20:38.570+0800","x":75.64,"y":322.862,"gpsType":"wgs84","z":0,"step":4,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.26545716794778,"engineType":null,"speed":3.8,"recordId":210610029309179175,"voltUnit":"V","labelId":"2160","gateId":"7035","routerId":"BTRCE59E8D3","routerMark":113,"jGateId":"7035","id":63727833,"state":0,"floor":1,"routerFlowId":56128,"flowId":603981943,"direction":6,"longitude":119.10908048915192,"entityId":24318,"raiseTime2":1624242039488,"uploadTime":1624242043381,"userId":"3897","beacons":"BTI24007036(3700),BTI24007037(12750)","blockId":null,"intensity":7066,"areaId":2,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:20:39.488+0800","x":80.415,"y":324.819,"gpsType":"wgs84","z":0,"step":3,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.265450765289486,"engineType":null,"speed":2.258,"recordId":210610029309179092,"voltUnit":"V","labelId":"2160","gateId":"7035","routerId":"BTR0794E313","routerMark":274,"jGateId":"7035","id":63727771,"state":0,"floor":1,"routerFlowId":147707,"flowId":603981942,"direction":6,"longitude":119.10904079456402,"entityId":24318,"raiseTime2":1624242043132,"uploadTime":1624242043141,"userId":"3897","beacons":"BTI24007035(3950),BTI24007036(4500),BTI24007037(12750)","blockId":null,"intensity":3700,"areaId":2,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:20:43.132+0800","x":76.675,"y":324.109,"gpsType":"wgs84","z":0,"step":4,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.265502557014734,"engineType":null,"speed":7.71,"recordId":210610029309179263,"voltUnit":"V","labelId":"2160","gateId":"7036","routerId":"BTR63F5C243","routerMark":270,"jGateId":"7036","id":63727878,"state":0,"floor":1,"routerFlowId":146946,"flowId":603981945,"direction":6,"longitude":119.10915563978497,"entityId":24318,"raiseTime2":1624242045600,"uploadTime":1624242047468,"userId":"3897","beacons":"BTI24007036(2650),BTI24007037(12750),BTI24007304(12750)","blockId":null,"intensity":8225,"areaId":2,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:20:45.600+0800","x":87.496,"y":329.853,"gpsType":"wgs84","z":0,"step":3,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.26550101563167,"engineType":null,"speed":7.699,"recordId":210610029309179347,"voltUnit":"V","labelId":"2160","gateId":"7036","routerId":"BTR63F5C243","routerMark":270,"jGateId":"7036","id":63727901,"state":0,"floor":1,"routerFlowId":147007,"flowId":603981949,"direction":6,"longitude":119.10916645512913,"entityId":24318,"raiseTime2":1624242047798,"uploadTime":1624242047812,"userId":"3897","beacons":"BTI24007037(3050),BTI24007036(3250)","blockId":null,"intensity":6650,"areaId":2,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:20:47.798+0800","x":88.515,"y":329.682,"gpsType":"wgs84","z":0,"step":4,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.265480300316725,"engineType":null,"speed":6.899,"recordId":210610029309179429,"voltUnit":"V","labelId":"2160","gateId":"7036","routerId":"BTR13E2D61D","routerMark":273,"jGateId":"7036","id":63727920,"state":0,"floor":1,"routerFlowId":120898,"flowId":603981950,"direction":6,"longitude":119.10916402631705,"entityId":24318,"raiseTime2":1624242048703,"uploadTime":1624242048707,"userId":"3897","beacons":"BTI24007038(3050),BTI24007037(3250),BTI24007036(4500)","blockId":null,"intensity":3150,"areaId":2,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:20:48.703+0800","x":88.286,"y":327.385,"gpsType":"wgs84","z":0,"step":3,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.26546306070541,"engineType":null,"speed":8.294,"recordId":210610029309179778,"voltUnit":"V","labelId":null,"gateId":null,"routerId":"BTR13E2D61D","routerMark":273,"jGateId":null,"id":63728051,"state":0,"floor":1,"routerFlowId":121008,"flowId":603981959,"direction":1,"longitude":119.10932164579933,"entityId":24318,"raiseTime2":1624242054335,"uploadTime":1624242060236,"userId":"3897","beacons":"BTI24007039(3450),BTI24006196(12750)","blockId":null,"intensity":6866,"areaId":2,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:20:54.335+0800","x":103.137,"y":325.473,"gpsType":"wgs84","z":0,"step":4,"rootAreaId":1,"engineId":"a1"},
			 {"altitude":5.9,"flag":0,"tagId":"BTT32003897","latitude":32.26546383313037,"engineType":null,"speed":5.871,"recordId":210610029309179515,"voltUnit":"V","labelId":"2160","gateId":"7036","routerId":"BTR0794E313","routerMark":274,"jGateId":"7036","id":63727945,"state":0,"floor":1,"routerFlowId":147867,"flowId":604047488,"direction":6,"longitude":119.10917778514415,"entityId":24318,"raiseTime2":1624242054474,"uploadTime":1624242054480,"userId":"3897","beacons":"BTI24007038(3050),BTI24007037(3250),BTI24007036(4500)","blockId":null,"intensity":3600,"areaId":2,"absolute":true,"volt":3.7,"raiseTime":"2021-06-21T10:20:54.474+0800","x":89.582,"y":325.559,"gpsType":"wgs84","z":0,"step":1,"rootAreaId":1,"engineId":"a1"}],"id":1,"jsonrpc":"2.0"}
			 */

			List<LocationRecord> locationRecordList = JSON.parseArray(resultJO.get("result").toString(),LocationRecord.class);
			locationRecordService.add(locationRecordList);
			locationRecordList=locationRecordService.select(startTimeLong,endTimeLong);
			int lrListSize = locationRecordList.size();
			//System.out.println("rlrJALength==="+rlrJALength);
			//System.out.println("ysb==="+ysb);
			double space = (double)lrListSize/ysb;
			//System.out.println("space==="+space);
			double cursor=0;
			List<LocationRecord> locRecList = new ArrayList<>();
			for(int i=0;i<lrListSize;i++) {
				if(i==lrListSize-1) {
					//System.out.println("cursor1==="+cursor+",i==="+i);
					LocationRecord locationRecord = locationRecordList.get(i);
					//APIResultUtil.addMoreLocationRecord(locationRecord,locRecList);
					locRecList.add(locationRecord);
				}
				else if(i>=cursor) {
					//System.out.println("cursor2==="+cursor+",i==="+i);
					LocationRecord locationRecord = locationRecordList.get(i);
					//APIResultUtil.addMoreLocationRecord(locationRecord,locRecList);
					locRecList.add(locationRecord);
					cursor+=space;
				}
			}
			resultMap.put("locRecList", locRecList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return resultMap;
		}
	}

	/**
	 * 2.4.2闸机记录读取
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
	 * 2.5.1获取电子围栏列表
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
	 * 2.5.2获取电子围栏
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
	 * 2.5.5获取电子围栏进出历史记录
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
	 * 2.6.2获取员工职务列表
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
			resultMap=JSON.parseObject(resultJO.toString(), Map.class);
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
	 * 2.6.1获取职务/车型/资产类别
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
	 * 2.6.5 获取实体数量
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getEntityCount")
	@ResponseBody
	public Map<String, Object> getEntityCount(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getEntityCount");
			JSONObject paramJO=new JSONObject();
			paramJO.put("entityType", "staff");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getEntityCount",request);
			System.out.println("getEntityCount:resultJO==="+resultJO.toString());
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
	 * 2.6.9 获取实体列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getEntities")
	@ResponseBody
	public Map<String, Object> getEntities(String entityType, HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getEntities");
			JSONObject paramJO=new JSONObject();
			paramJO.put("entityType", entityType);
			//paramJO.put("pageIndex", "0");
			//paramJO.put("maxCount", "100");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getEntities",request);
			System.out.println("getEntities:resultJO==="+resultJO.toString());
			/*
			 {"result":[
			 {"tagId":"BTT1E7D5EB9","entityType":"staff","sex":1,"departmentId":0,"photo":null,"pid":"phone001","userId":"5EB9","post":"","phone":"1","name":"陈锡棋测试手机","dutyId":1,"id":128813,"age":"1"},
			 {"post":"员工","phone":"3316","tagId":"BTT32003607","entityType":"staff","sex":1,"departmentId":4,"name":"王勇-test","photo":null,"dutyId":1,"pid":"FUPY00000","id":24092,"userId":"3607"},
			 {"security":"","post":"","phone":"3222","tagId":"BTT32003729","entityType":"staff","departmentId":33,"name":"范丽龙","photo":"","dutyId":1,"pid":"FUPYJX008","id":28041,"userId":"3729"},
			 {"post":"员工","phone":"123","tagId":"BTT32003618","entityType":"staff","departmentId":13,"name":"郭玉明","photo":null,"dutyId":4,"pid":"FUPY20013","id":24103,"userId":"3618"},
			 {"post":"员工","phone":"123","tagId":"BTT32003628","entityType":"staff","departmentId":13,"name":"李星","photo":null,"dutyId":4,"pid":"FUPY19008","id":24111,"userId":"3628"},
			 {"post":"员工","phone":"123","tagId":"BTT32003660","entityType":"staff","departmentId":13,"name":"丁杰","photo":null,"dutyId":4,"pid":"FUPY20007","id":24132,"userId":"3660"},
			 {"post":"员工","phone":"123","tagId":"BTT32003668","entityType":"staff","departmentId":13,"name":"李京","photo":null,"dutyId":4,"pid":"FUPY13042","id":24140,"userId":"3668"},
			 {"post":"员工","phone":"123","tagId":"BTT32003605","entityType":"staff","departmentId":5,"name":"秦莹莹","photo":null,"dutyId":3,"pid":"FUPY12006","id":24090,"userId":"3605"},
			 {"post":"员工","phone":"123","tagId":"BTT32003611","entityType":"staff","departmentId":6,"name":"陆志文","photo":null,"dutyId":3,"pid":"FUPY14064","id":24096,"userId":"3611"},
			 {"post":"员工","phone":"123","tagId":"BTT32003624","entityType":"staff","departmentId":5,"name":"周汉栋","photo":null,"dutyId":3,"pid":"FUPY12001","id":24109,"userId":"3624"},
			 {"post":"员工","phone":"123","tagId":"BTT32003658","entityType":"staff","departmentId":6,"name":"黄尉辉","photo":null,"dutyId":3,"pid":"FUPY17025","id":24130,"userId":"3658"},
			 {"post":"员工","phone":"123","tagId":"BTT32003688","entityType":"staff","departmentId":6,"name":"陈燕","photo":null,"dutyId":3,"pid":"FUPY13038","id":24154,"userId":"3688"},
			 {"post":"员工","phone":"123","tagId":"BTT32003828","entityType":"staff","departmentId":22,"name":"傅元婷","photo":null,"dutyId":3,"pid":"FUPYYC15011","id":24137,"userId":"3828"},{"post":"员工","tagId":"BTT32003603","entityType":"staff","sex":1,"departmentId":3,"name":"刘建华","photo":null,"dutyId":1,"pid":"FUPY20018","id":24088,"userId":"3603"},{"post":"员工","tagId":"BTT32003602","entityType":"staff","sex":1,"departmentId":2,"name":"谢松","photo":null,"dutyId":1,"pid":"FUPY20017","id":24087,"userId":"3602"},{"post":"员工","tagId":"BTT32003604","entityType":"staff","sex":1,"departmentId":4,"name":"张元","photo":null,"dutyId":1,"pid":"FUPY20019","id":24089,"userId":"3604"},{"post":"员工","tagId":"BTT32003608","entityType":"staff","sex":1,"departmentId":4,"name":"王春华","photo":null,"dutyId":1,"pid":"FUPY17017","id":24093,"userId":"3608"},{"post":"员工","tagId":"BTT32003609","entityType":"staff","sex":1,"departmentId":17,"name":"张清山","photo":null,"dutyId":1,"pid":"FUPY88888","id":24094,"userId":"3609"},{"post":"员工","tagId":"BTT32003612","entityType":"staff","sex":1,"departmentId":7,"name":"梁胜琴","photo":null,"dutyId":1,"pid":"FUPY12020","id":24097,"userId":"3612"},{"post":"员工","tagId":"BTT32003613","entityType":"staff","sex":1,"departmentId":8,"name":"郭延山","photo":null,"dutyId":1,"pid":"FUPY20091","id":24098,"userId":"3613"},{"post":"员工","tagId":"BTT32003614","entityType":"staff","sex":1,"departmentId":9,"name":"李春琴","photo":null,"dutyId":1,"pid":"FUPY13065","id":24099,"userId":"3614"},{"post":"员工","tagId":"BTT32003616","entityType":"staff","sex":1,"departmentId":11,"name":"曹荆","photo":null,"dutyId":1,"pid":"YC10055","id":24101,"userId":"3616"},{"post":"员工","tagId":"BTT32003617","entityType":"staff","sex":1,"departmentId":12,"name":"王雪","photo":null,"dutyId":1,"pid":"FUPY17002","id":24102,"userId":"3617"},{"post":"员工","tagId":"BTT32003619","entityType":"staff","sex":1,"departmentId":4,"name":"尹国香","photo":null,"dutyId":1,"pid":"FUPY19003","id":24104,"userId":"3619"},{"post":"员工","tagId":"BTT32003620","entityType":"staff","sex":1,"departmentId":14,"name":"孙黎","photo":null,"dutyId":1,"pid":"FUPY13058","id":24105,"userId":"3620"},{"post":"员工","tagId":"BTT32003621","entityType":"staff","sex":1,"departmentId":15,"name":"洪生林","photo":null,"dutyId":1,"pid":"FEITYZ0007","id":24106,"userId":"3621"},{"post":"员工","tagId":"BTT32003622","entityType":"staff","sex":1,"departmentId":4,"name":"杜恒峰","photo":null,"dutyId":1,"pid":"YC18006","id":24107,"userId":"3622"},{"post":"员工","tagId":"BTT32003623","entityType":"staff","sex":1,"departmentId":9,"name":"张森","photo":null,"dutyId":1,"pid":"FUPY15037","id":24108,"userId":"3623"},{"post":"员工","tagId":"BTT32003626","entityType":"staff","sex":1,"departmentId":16,"name":"宗慧","photo":null,"dutyId":1,"pid":"FUPY13060","id":24110,"userId":"3626"},{"post":"员工","tagId":"BTT32003631","entityType":"staff","sex":1,"departmentId":4,"name":"王金金","photo":null,"dutyId":1,"pid":"FUPY17024","id":24113,"userId":"3631"},{"post":"员工","tagId":"BTT32003633","entityType":"staff","sex":1,"departmentId":17,"name":"李福贵","photo":null,"dutyId":1,"pid":"FUPY13012","id":24114,"userId":"3633"},{"post":"员工","tagId":"BTT32003634","entityType":"staff","sex":1,"departmentId":4,"name":"王招稳","photo":null,"dutyId":1,"pid":"FUPY14083","id":24115,"userId":"3634"},{"post":"员工","tagId":"BTT32003636","entityType":"staff","sex":1,"departmentId":9,"name":"王莹","photo":null,"dutyId":1,"pid":"FUPY13046","id":24116,"userId":"3636"},{"post":"员工","tagId":"BTT32003638","entityType":"staff","sex":1,"departmentId":14,"name":"吴敏","photo":null,"dutyId":1,"pid":"FUPY15041","id":24117,"userId":"3638"},{"post":"员工","tagId":"BTT32003639","entityType":"staff","sex":1,"departmentId":7,"name":"李冬丽","photo":null,"dutyId":1,"pid":"FUPY13076","id":24118,"userId":"3639"},{"post":"员工","tagId":"BTT32003642","entityType":"staff","sex":1,"departmentId":14,"name":"钟耀翰","photo":null,"dutyId":1,"pid":"FUPY13055","id":24119,"userId":"3642"},{"post":"员工","tagId":"BTT32003643","entityType":"staff","sex":1,"departmentId":7,"name":"罗瑞文","photo":null,"dutyId":1,"pid":"FUPY19001","id":24120,"userId":"3643"},{"post":"员工","tagId":"BTT32003644","entityType":"staff","sex":1,"departmentId":4,"name":"强乃超","photo":null,"dutyId":1,"pid":"FUPY17001","id":24121,"userId":"3644"},{"post":"员工","tagId":"BTT32003645","entityType":"staff","sex":1,"departmentId":9,"name":"郑金花","photo":null,"dutyId":1,"pid":"FUPY15030","id":24122,"userId":"3645"},{"post":"员工","tagId":"BTT32003646","entityType":"staff","sex":1,"departmentId":4,"name":"周萍","photo":null,"dutyId":1,"pid":"FUPY19006","id":24123,"userId":"3646"},{"post":"员工","tagId":"BTT32003650","entityType":"staff","sex":1,"departmentId":16,"name":"钱长露","photo":null,"dutyId":1,"pid":"FUPY17004","id":24124,"userId":"3650"},{"post":"员工","tagId":"BTT32003653","entityType":"staff","sex":1,"departmentId":18,"name":"周宝平","photo":null,"dutyId":1,"pid":"YC12002","id":24125,"userId":"3653"},{"post":"员工","tagId":"BTT32003654","entityType":"staff","sex":1,"departmentId":7,"name":"戴亚南","photo":null,"dutyId":1,"pid":"FUPY18022","id":24126,"userId":"3654"},{"post":"员工","tagId":"BTT32003655","entityType":"staff","sex":1,"departmentId":9,"name":"冯辉棋","photo":null,"dutyId":1,"pid":"FUPY13030","id":24127,"userId":"3655"},{"post":"员工","tagId":"BTT32003656","entityType":"staff","sex":1,"departmentId":16,"name":"胡杨","photo":null,"dutyId":1,"pid":"FUPY18005","id":24128,"userId":"3656"},{"post":"员工","tagId":"BTT32003657","entityType":"staff","sex":1,"departmentId":4,"name":"陶萍萍","photo":null,"dutyId":1,"pid":"FUPY17016","id":24129,"userId":"3657"},{"post":"员工","tagId":"BTT32003659","entityType":"staff","sex":1,"departmentId":19,"name":"王洁丽","photo":null,"dutyId":1,"pid":"FEITYZ0034","id":24131,"userId":"3659"},{"post":"员工","tagId":"BTT32003661","entityType":"staff","sex":1,"departmentId":12,"name":"陈弯弯","photo":null,"dutyId":1,"pid":"FUPY14172","id":24133,"userId":"3661"},{"post":"员工","tagId":"BTT32003662","entityType":"staff","sex":1,"departmentId":20,"name":"张亦廷","photo":null,"dutyId":1,"pid":"YCK99001","id":24134,"userId":"3662"},{"post":"员工","tagId":"BTT32003663","entityType":"staff","sex":1,"departmentId":21,"name":"孙爱霞","photo":null,"dutyId":1,"pid":"FUPY14056","id":24135,"userId":"3663"},{"post":"员工","tagId":"BTT32003666","entityType":"staff","sex":1,"departmentId":9,"name":"陶念慈","photo":null,"dutyId":1,"pid":"FUPY12015","id":24138,"userId":"3666"},{"post":"员工","tagId":"BTT32003667","entityType":"staff","sex":1,"departmentId":16,"name":"高秋菊","photo":null,"dutyId":1,"pid":"FUPY13061","id":24139,"userId":"3667"},{"post":"员工","tagId":"BTT32003669","entityType":"staff","sex":1,"departmentId":8,"name":"宋莹","photo":null,"dutyId":1,"pid":"FUPY12012","id":24141,"userId":"3669"},{"post":"员工","tagId":"BTT32003671","entityType":"staff","sex":1,"departmentId":15,"name":"焦俊","photo":null,"dutyId":1,"pid":"FEITYZ0005","id":24142,"userId":"3671"},{"post":"员工","tagId":"BTT32003672","entityType":"staff","sex":1,"departmentId":16,"name":"赵静","photo":null,"dutyId":1,"pid":"FUPY15001","id":24143,"userId":"3672"},{"post":"员工","tagId":"BTT32003673","entityType":"staff","sex":1,"departmentId":16,"name":"曹桂明","photo":null,"dutyId":1,"pid":"FUPY16003","id":24144,"userId":"3673"},{"post":"员工","tagId":"BTT32003676","entityType":"staff","sex":1,"departmentId":16,"name":"刘婷婷","photo":null,"dutyId":1,"pid":"FUPY14052","id":24145,"userId":"3676"},{"post":"员工","tagId":"BTT32003677","entityType":"staff","sex":1,"departmentId":4,"name":"王登平","photo":null,"dutyId":1,"pid":"FUPY20005","id":24146,"userId":"3677"},{"post":"员工","tagId":"BTT32003678","entityType":"staff","sex":1,"departmentId":4,"name":"陈兵","photo":null,"dutyId":1,"pid":"FUPY14072","id":24147,"userId":"3678"},{"post":"员工","tagId":"BTT32003679","entityType":"staff","sex":1,"departmentId":21,"name":"俞春莲","photo":null,"dutyId":1,"pid":"FUPY17010","id":24148,"userId":"3679"},{"post":"员工","tagId":"BTT32003680","entityType":"staff","sex":1,"departmentId":11,"name":"潘菲菲","photo":null,"dutyId":1,"pid":"YC10051","id":24149,"userId":"3680"},{"post":"员工","tagId":"BTT32003681","entityType":"staff","sex":1,"departmentId":23,"name":"朱杰","photo":null,"dutyId":1,"pid":"FUPY19014","id":24150,"userId":"3681"},{"post":"员工","tagId":"BTT32003682","entityType":"staff","sex":1,"departmentId":4,"name":"强晨晨","photo":null,"dutyId":1,"pid":"FUPY14087","id":24151,"userId":"3682"},{"post":"员工","tagId":"BTT32003683","entityType":"staff","sex":1,"departmentId":4,"name":"豆彪","photo":null,"dutyId":1,"pid":"FUPY14060","id":24152,"userId":"3683"},{"post":"员工","tagId":"BTT32003685","entityType":"staff","sex":1,"departmentId":15,"name":"江启明","photo":null,"dutyId":1,"pid":"FEITYZ0002","id":24153,"userId":"3685"},{"post":"员工","tagId":"BTT32003689","entityType":"staff","sex":1,"departmentId":24,"name":"陈静","photo":null,"dutyId":1,"pid":"FUPY15024","id":24155,"userId":"3689"},{"post":"员工","tagId":"BTT32003693","entityType":"staff","sex":1,"departmentId":16,"name":"顾玉婷","photo":null,"dutyId":1,"pid":"FUPY13062","id":24156,"userId":"3693"},{"post":"员工","tagId":"BTT32003695","entityType":"staff","sex":1,"departmentId":4,"name":"李明","photo":null,"dutyId":1,"pid":"FUPY15038","id":24157,"userId":"3695"},{"post":"员工","tagId":"BTT32003696","entityType":"staff","sex":1,"departmentId":18,"name":"蔡雪芳","photo":null,"dutyId":1,"pid":"FUPYYC12005","id":24158,"userId":"3696"},{"post":"员工","phone":"123","tagId":"BTT32003615","entityType":"staff","departmentId":10,"name":"黄成军","photo":null,"dutyId":4,"pid":"FUPY12035","id":24100,"userId":"3615"},{"tagId":"BTTFCC6E902","entityType":"staff","sex":1,"departmentId":15,"photo":"","pid":"FEITYZ1000","userId":"6E902","security":"","post":"","phone":"2515","name":"开发测试手机","dutyId":1,"id":38466},{"tagId":"BTT32003709","entityType":"staff","sex":2,"departmentId":15,"photo":"","pid":"feityz1000","userId":"3709","security":"","post":"","phone":"2515","name":"行政楼测试","dutyId":1,"id":129978},{"post":"员工","phone":"123","tagId":"BTT32003741","entityType":"staff","departmentId":26,"name":"姚凤华","photo":null,"dutyId":3,"pid":"FUPY14055","id":24187,"userId":"3741"},{"post":"员工","phone":"123","tagId":"BTT32003762","entityType":"staff","departmentId":32,"name":"丁小芳","photo":null,"dutyId":3,"pid":"FUPY20009","id":24199,"userId":"3762"},{"post":"员工","phone":"123","tagId":"BTT32003763","entityType":"staff","departmentId":22,"name":"黄仕国","photo":null,"dutyId":3,"pid":"FUPY13004","id":24200,"userId":"3763"},{"post":"员工","phone":"123","tagId":"BTT32003774","entityType":"staff","departmentId":6,"name":"郑晓平","photo":null,"dutyId":3,"pid":"FUPY13108","id":24205,"userId":"3774"},{"post":"员工","phone":"123","tagId":"BTT32003777","entityType":"staff","departmentId":6,"name":"熊用恩","photo":null,"dutyId":3,"pid":"FUPY12033","id":24207,"userId":"3777"},{"post":"员工","phone":"123","tagId":"BTT32003785","entityType":"staff","departmentId":6,"name":"金玉康","photo":null,"dutyId":3,"pid":"FUPY20010","id":24213,"userId":"3785"},{"post":"员工","phone":"123","tagId":"BTT32003765","entityType":"staff","departmentId":30,"name":"陈义林","photo":null,"dutyId":6,"pid":"FUPY13005","id":24202,"userId":"3765"},{"post":"员工","phone":"123","tagId":"BTT32003791","entityType":"staff","departmentId":30,"name":"王伟","photo":null,"dutyId":6,"pid":"FUPY18021","id":24217,"userId":"3791"},{"post":"员工","phone":"123","tagId":"BTT32003794","entityType":"staff","departmentId":30,"name":"刘国季","photo":null,"dutyId":6,"pid":"FUPY14034","id":24220,"userId":"3794"},{"post":"员工","phone":"123","tagId":"BTT32003795","entityType":"staff","departmentId":30,"name":"陈凯","photo":null,"dutyId":6,"pid":"FUPY14002","id":24221,"userId":"3795"},{"post":"员工","phone":"123","tagId":"BTT32003800","entityType":"staff","departmentId":30,"name":"陈旭东","photo":null,"dutyId":6,"pid":"FUPY17006","id":24226,"userId":"3800"},{"post":"员工","phone":"123","tagId":"BTT32003802","entityType":"staff","departmentId":30,"name":"姚航","photo":null,"dutyId":6,"pid":"FUPY18027","id":24228,"userId":"3802"},{"post":"员工","tagId":"BTT32003697","entityType":"staff","sex":1,"departmentId":15,"name":"董玲玲","photo":null,"dutyId":1,"pid":"FEITYZ0036","id":24159,"userId":"3697"},{"post":"员工","phone":"123","tagId":"BTT32003714","entityType":"staff","departmentId":6,"name":"米荣刚","photo":null,"dutyId":3,"pid":"FUPY13083","id":24169,"userId":"3714"},{"post":"员工","tagId":"BTT32003698","entityType":"staff","sex":1,"departmentId":4,"name":"唐宇","photo":null,"dutyId":1,"pid":"FUPY14098","id":24160,"userId":"3698"},{"post":"员工","tagId":"BTT32003699","entityType":"staff","sex":1,"departmentId":16,"name":"许青青","photo":null,"dutyId":1,"pid":"FUPY19017","id":24161,"userId":"3699"},{"post":"员工","tagId":"BTT32003700","entityType":"staff","sex":1,"departmentId":4,"name":"陈疆","photo":null,"dutyId":1,"pid":"FUPY14110","id":24162,"userId":"3700"},{"post":"员工","tagId":"BTT32003707","entityType":"staff","sex":1,"departmentId":4,"name":"曹蓉","photo":null,"dutyId":1,"pid":"FUPY18002","id":24163,"userId":"3707"},{"post":"员工","tagId":"BTT32003708","entityType":"staff","sex":1,"departmentId":4,"name":"赵杰","photo":null,"dutyId":1,"pid":"FUPY14097","id":24164,"userId":"3708"},{"post":"员工","tagId":"BTT32003711","entityType":"staff","sex":1,"departmentId":8,"name":"惠慧","photo":null,"dutyId":1,"pid":"FUPY20008","id":24166,"userId":"3711"},{"post":"员工","tagId":"BTT32003712","entityType":"staff","sex":1,"departmentId":16,"name":"李娟","photo":null,"dutyId":1,"pid":"FUPY14189","id":24167,"userId":"3712"},{"post":"员工","tagId":"BTT32003713","entityType":"staff","sex":1,"departmentId":4,"name":"张云亮","photo":null,"dutyId":1,"pid":"FUPY14078","id":24168,"userId":"3713"},{"post":"员工","tagId":"BTT32003716","entityType":"staff","sex":1,"departmentId":18,"name":"申春","photo":null,"dutyId":1,"pid":"YC14001","id":24170,"userId":"3716"},{"post":"员工","tagId":"BTT32003717","entityType":"staff","sex":1,"departmentId":11,"name":"任园园","photo":null,"dutyId":1,"pid":"FUPY18006","id":24171,"userId":"3717"},{"post":"员工","tagId":"BTT32003718","entityType":"staff","sex":1,"departmentId":25,"name":"万珍珍","photo":null,"dutyId":1,"pid":"FUPY18010","id":24172,"userId":"3718"},{"post":"员工","tagId":"BTT32003720","entityType":"staff","sex":1,"departmentId":20,"name":"张灵","photo":null,"dutyId":1,"pid":"YC17003","id":24173,"userId":"3720"},{"post":"员工","tagId":"BTT32003721","entityType":"staff","sex":1,"departmentId":4,"name":"周志东","photo":null,"dutyId":1,"pid":"FUPY20015","id":24174,"userId":"3721"},{"post":"员工","tagId":"BTT32003722","entityType":"staff","sex":1,"departmentId":14,"name":"魏江南","photo":null,"dutyId":1,"pid":"FUPY13057","id":24175,"userId":"3722"}],"id":1,"jsonrpc":"2.0"}
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
	 * 2.6.6 获取实体信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getEntity")
	@ResponseBody
	public Map<String, Object> getEntity(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject bodyParamJO=new JSONObject();
			bodyParamJO.put("jsonrpc", "2.0");
			bodyParamJO.put("method", "getEntity");
			JSONObject paramJO=new JSONObject();
			paramJO.put("id", 128813);
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getEntity",request);
			System.out.println("getEntity:resultJO==="+resultJO.toString());
			/*
			 {"result":{"tagId":"BTT1E7D5EB9","entityType":"staff","sex":1,"departmentId":0,"photo":null,"pid":"phone001","userId":"5EB9","post":"","phone":"1","name":"陈锡棋测试手机","duty":{"entityType":"staff","onlineIcon":"/sc20080092/duty/onlineIcon-1.png?t=1605840646476","name":"员工","offlineIcon":"/sc20080092/duty/offlineIcon-1.png?t=1605840646476","id":1,"cnEntityType":"人员","key":1},"dutyId":1,"id":128813,"tag":{"id":"BTT1E7D5EB9","userId":"5EB9"},"age":"1"},"id":1,"jsonrpc":"2.0"}
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
	 * 2.7.1获取报警触发器列表
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
	 * 2.7.4获取报警触发条件
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
	 * 2.7.3报获取警记录
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
			paramJO.put("endTime", "1718277921076");
			bodyParamJO.put("params", paramJO);
			bodyParamJO.put("id", 1);
			JSONObject resultJO = postBody(SERVICE_URL,bodyParamJO,"getWarnRecords",request);
			//System.out.println("getWarnRecords:resultJO==="+resultJO.toString());
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
	 * 2.8.2 实时在线人数及区域统计
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/summaryOnlineEntity")
	@ResponseBody
	public Map<String, Object> summaryOnlineEntity(HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject resultJO = null;
			/*
			if(LOCAL_Server_NAME.equals(request.getServerName())) {
				resultJO = getRespJson("summaryOnlineEntity", null);
			}
			else {
			*/
				JSONObject bodyParamJO=new JSONObject();
				bodyParamJO.put("jsonrpc", "2.0");
				bodyParamJO.put("method", "summaryOnlineEntity");
				JSONObject paramJO=new JSONObject();
				paramJO.put("areaId", "1");
				bodyParamJO.put("params", paramJO);
				bodyParamJO.put("id", 1);
				resultJO = postBody(SERVICE_URL,bodyParamJO,"summaryOnlineEntity",request);
			//}
			//JSONObject resultJO = APIResultUtil.summaryOnlineEntity();
			//System.out.println("summaryOnlineEntity:resultJO==="+resultJO.toString());
			resultMap=JSON.parseObject(resultJO.toString());
			/*
			 {"result":{"summary":{"online":{"total":106,"car":3,"staff":103}},
			 		   "children":[
				 		   {"summary":{"online":{"total":1,"car":0,"staff":1}},"name":"二层","id":3},
				 		   {"summary":{"online":{"total":0,"car":0,"staff":0}},"name":"三层","id":4},
				 		   {"summary":{"online":{"total":0,"car":0,"staff":0}},"name":"四层","id":5},
				 		   {"summary":{"online":{"total":105,"car":3,"staff":102}},"name":"一层","id":2},
				 		   {"summary":{"online":{"total":0,"car":0,"staff":0}},"name":"五层","id":6}
			 		   ],
			 		   "name":"总图","id":1},
			  "id":1,"jsonrpc":"2.0"}
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
	 * 2.8.3报警记录汇总
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

	/**
	 * 接收推送消息
	 * @param request
	 * @param response
	 * @param json
	 * @return
	 */
	@RequestMapping(value="/receiveDate", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> receiveDate(HttpServletRequest request, HttpServletResponse response, @RequestBody String json) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		com.alibaba.fastjson.JSONObject jsonJO = JSON.parseObject(json);
		if(jsonJO.containsKey("Location")) {//定位消息
			System.out.println("更新定位信息...");
			JSONArray locationJA = jsonJO.getJSONArray("Location");
			
			/*
			System.out.println("size==="+jsonJO.getJSONArray("Location").size());
			System.out.println("deviceType==="+jsonJO.getJSONArray("Location").getJSONObject(0).getString("deviceType"));
			System.out.println("uid==="+jsonJO.getJSONArray("Location").getJSONObject(0).getString("uid"));
			System.out.println("rootAreaId==="+jsonJO.getJSONArray("Location").getJSONObject(0).getInteger("rootAreaId"));
			System.out.println("areaId==="+jsonJO.getJSONArray("Location").getJSONObject(0).getInteger("areaId"));
			System.out.println("locationTime==="+jsonJO.getJSONArray("Location").getJSONObject(0).getLong("locationTime"));
			System.out.println("lostTime==="+jsonJO.getJSONArray("Location").getJSONObject(0).getLong("lostTime"));
			System.out.println("x==="+jsonJO.getJSONArray("Location").getJSONObject(0).getFloat("x"));
			System.out.println("y==="+jsonJO.getJSONArray("Location").getJSONObject(0).getFloat("y"));
			System.out.println("z==="+jsonJO.getJSONArray("Location").getJSONObject(0).getFloat("z"));
			System.out.println("abslute==="+jsonJO.getJSONArray("Location").getJSONObject(0).getBoolean("abslute"));
			System.out.println("speed==="+jsonJO.getJSONArray("Location").getJSONObject(0).getFloat("speed"));
			System.out.println("LocationFloor==="+jsonJO.getJSONArray("Location").getJSONObject(0).getInteger("floor"));
			System.out.println("out==="+jsonJO.getJSONArray("Location").getJSONObject(0).getBoolean("out"));
			System.out.println("longitude==="+jsonJO.getJSONArray("Location").getJSONObject(0).getFloat("longitude"));
			System.out.println("latitude==="+jsonJO.getJSONArray("Location").getJSONObject(0).getFloat("latitude"));
			System.out.println("altitude==="+jsonJO.getJSONArray("Location").getJSONObject(0).getFloat("altitude"));
			*/
			
			for(int i=0;i<locationJA.size();i++) {
				com.alibaba.fastjson.JSONObject locationJO = locationJA.getJSONObject(i);
				String deviceType = locationJO.getString("deviceType");
				String uid = locationJO.getString("uid");
				Integer rootAreaId = locationJO.getInteger("rootAreaId");
				Integer areaId = locationJO.getInteger("areaId");
				Long locationTime = locationJO.getLong("locationTime");
				Long lostTime = locationJO.getLong("lostTime");
				Float x = locationJO.getFloat("x");
				Float y = locationJO.getFloat("y");
				Float z = locationJO.getFloat("z");
				Boolean abslute = locationJO.getBoolean("abslute");
				Float speed = locationJO.getFloat("speed");
				Integer floor = locationJO.getInteger("floor");
				Boolean out = locationJO.getBoolean("out");
				Float longitude = locationJO.getFloat("longitude");
				Float latitude = locationJO.getFloat("latitude");
				Float altitude = locationJO.getFloat("altitude");
				
				Location location = new Location();
				location.setDeviceType(deviceType);
				location.setUid(uid);
				location.setRootAreaId(rootAreaId);
				location.setAreaId(areaId);
				location.setLocationTime(locationTime);
				location.setLostTime(lostTime);
				location.setX(x);
				location.setY(y);
				location.setZ(z);
				location.setAbslute(abslute);
				location.setSpeed(speed);
				location.setFloor(floor);
				location.setOut(out);
				location.setLongitude(longitude);
				location.setLatitude(latitude);
				location.setAltitude(altitude);
				
				locationService.add(location);
			}
		}
		return resultMap;
	}
	
	public static void main(String[] args) {
		//String s = SHA256Utils.getSHA256("ts00000006"+"test001"+"test001"+"3bdfd7b5731143cda58cef2d659a4976");
		//System.out.println("s==="+s);
		//52ac4c72590ec0d129fac7ffa3f0a2c4841875334709fbe0ac9ba65a104cc2ca
		/*
		System.out.println(DateUtil.convertLongToString(1624266053514L));
		try {
			System.out.println(DateUtil.convertStringToLong("2021-06-21 17:00:53:514"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		//double d=(double)19/5;
		//System.out.println(Math.ceil(d));
		System.out.println(APIResultUtil.getDeviceTypes());
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
			//connection.setRequestProperty("Cookie", "JSESSIONID=E1CD97E8E9AA306810805BFF21D7FD7D; Path=/position; HttpOnly");
			String cookie = null;
			Object LoginUserObj = session.getAttribute("loginUser");
			//System.out.println("LoginUserObj==="+LoginUserObj);
			if(LoginUserObj!=null) {
				LoginUser loginUser = (LoginUser)LoginUserObj;
				cookie = loginUser.getCookie();
			}
			
			if(cookie==null)
				cookie = loginUserService.getCookieByUserId(TEST_USER_Id);
				
			if(!StringUtils.isEmpty(cookie))
				connection.setRequestProperty("Cookie", cookie);
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
		//System.out.println("bodyParamStr==="+bodyParamStr);
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
		//System.out.println("result==="+result);
		JSONObject resultJO = null;
		if(result.contains("DOCTYPE")) {
			resultJO = new JSONObject();
			resultJO.put("status", "no");
		}
		else if(result.contains("error")) {
			resultJO = new JSONObject(result);
			resultJO.put("status", "no");
		}
		else {
			resultJO = new JSONObject(result);
			resultJO.put("status", "ok");
		}
		return resultJO;
	}
	
	/**
	 * 验证登录信息是否有效
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/checkCookieValid")
	@ResponseBody
	public Map<String, Object> checkCookieValid(HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> soerMap = summaryOnlineEntity(request);
		if("no".equals(soerMap.get("status").toString())) {
			resultMap.put("status", "no");
			resultMap.put("message", "登录信息已过期，请重新登录");
		}
		else {
			resultMap.put("status", "ok");
		}
		return resultMap;
	}
	
	public boolean checkCookieInSession(HttpSession session) {
		Object loginUserObj = session.getAttribute("loginUser");
		if(loginUserObj==null)
			return false;
		else {
			LoginUser loginUser = (LoginUser)loginUserObj;
			if(loginUser==null)
				return false;
			else {
				String cookie = loginUser.getCookie();
				if(StringUtils.isEmpty(cookie))
					return false;
				else
					return true;
			}
		}
	}
	
	public String getCookieFromHeader(HttpURLConnection connection,HttpSession session) {
		Map<String,List<String>> map = connection.getHeaderFields();
		for (String key : map.keySet()) {
			String value = map.get(key).get(0);
			if(value.contains("JSESSIONID=")) {
				 System.out.println("key==="+value);
				 LoginUser loginUser=new LoginUser();
				 loginUser.setCookie(value);
				 session.setAttribute("loginUser", loginUser);
			}
		}
		return "";
	}
	
	//post请求后端收不到参数的解决方案：https://blog.csdn.net/xu_lo/article/details/90041606
	public JSONObject getRespJson(String method,JSONObject paramJO) throws Exception {
		// TODO Auto-generated method stub
		//POST的URL
		//建立HttpPost对象
		HttpPost httppost=new HttpPost(HWY_URL+method);
		httppost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		//添加参数
		List<NameValuePair> params=new ArrayList<NameValuePair>();
		if(paramJO!=null) {
			Iterator<String> paramJOIter = paramJO.keys();
			int index=0;
			while (paramJOIter.hasNext()) {
				String key = paramJOIter.next();
				String value = paramJO.get(key).toString();
				//System.out.println("key==="+key);
				//System.out.println("value==="+value);
				params.add(index, new BasicNameValuePair(key, value));
				index++;
			}
		}
		if(params!=null)
			httppost.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
		//设置编码
		HttpResponse response=new DefaultHttpClient().execute(httppost);
		//发送Post,并返回一个HttpResponse对象
		JSONObject resultJO = null;
		if(response.getStatusLine().getStatusCode()==200){//如果状态码为200,就是正常返回
			String result=EntityUtils.toString(response.getEntity());
			resultJO = new JSONObject(result);
		}
		return resultJO;
	}
}
