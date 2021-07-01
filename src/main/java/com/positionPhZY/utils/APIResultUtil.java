package com.positionPhZY.utils;

import java.util.List;

import org.json.JSONObject;

import com.positionPhZY.entity.*;

/**
 * 这个类的方法返回的结果是固定的，当服务器瘫痪、接口不能访问时,临时调用这些方法
 * */
public class APIResultUtil {
	
	/**
	 * 2.2.1 获取定位设备类型表
	 * @return
	 */
	public static JSONObject getDeviceTypes() {
		StringBuilder resultJOSB=new StringBuilder();
		resultJOSB.append("{\"result\":[");
			resultJOSB.append("{\"css\":\"\",\"icon\":\"sub-menu-icon6\",\"name\":\"汇聚网关\",\"id\":\"BTG\",");
				/*
				resultJOSB.append("\"fields\":[");
					resultJOSB.append("{\"mode\":\"\",\"default\":3600000,\"name\":\"超时值\",\"id\":\"overtime\",\"type\":\"double\"},");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"网关号\",\"id\":\"labelId\",\"type\":\"double\"},");
					resultJOSB.append("{\"mode\":\"e\",\"name\":\"状态\",\"expr\":\"(time+overtime) > new Date()\",\"id\":\"online\",\"type\":\"bool\",");
						resultJOSB.append("\"list\":[");
							resultJOSB.append("{\"html\":\"在线\",\"value\":true},");
							resultJOSB.append("{\"html\":\"离线\",\"value\":false}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"r\",\"name\":\"最近激活时间\",\"id\":\"time\",\"type\":\"datetime\"}");
				resultJOSB.append("],");
				*/
			resultJOSB.append("\"engineMask\":255},");
			resultJOSB.append("{\"css\":\"\",\"icon\":\"sub-menu-icon6\",\"name\":\"定位器\",\"id\":\"BTI\",");
				/*
				resultJOSB.append("\"fields\":[");
					resultJOSB.append("{\"mode\":\"e\",\"name\":\"电量\",\"expr\":\"(volt == null) ? \\\"\\\" : ((volt >= 2700) ? (volt / 1000 + \\\"V\\\") : (\\\"<span style= \\\\\\\"color:red;\\\\\\\">\\\" + (volt / 1000) + \\\"V(低压)<\\/span>\\\"))\",\"id\":\"volt\",\"type\":\"string\"},");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"信标号\",\"id\":\"labelId\",\"type\":\"double\"},");
					resultJOSB.append("{\"mode\":\"\",\"default\":0,\"name\":\"x轴\",\"id\":\"x\",\"type\":\"double\"},");
					resultJOSB.append("{\"mode\":\"\",\"default\":0,\"name\":\"y轴\",\"id\":\"y\",\"type\":\"double\"},");
					resultJOSB.append("{\"mode\":\"\",\"default\":0,\"name\":\"z轴\",\"id\":\"z\",\"type\":\"double\"},");
					resultJOSB.append("{\"mode\":\"w\",\"default\":86400000,\"name\":\"超时值\",\"id\":\"overtime\",\"type\":\"double\"},");
					resultJOSB.append("{\"mode\":\"e\",\"name\":\"状态\",\"expr\":\"(time+overtime) > new Date()\",\"id\":\"online\",\"type\":\"bool\",");
						resultJOSB.append("\"list\":[");
							resultJOSB.append("{\"html\":\"在线\",\"value\":true},");
							resultJOSB.append("{\"html\":\"离线\",\"value\":false}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"r\",\"name\":\"最近激活时间\",\"id\":\"time\",\"type\":\"datetime\"}");
				resultJOSB.append("],");
				*/
			resultJOSB.append("\"engineMask\":2},");
			resultJOSB.append("{\"css\":\"\",\"icon\":\"sub-menu-icon6\",\"name\":\"通讯中继\",\"id\":\"BTR\",");
				/*
				resultJOSB.append("\"fields\":[");
					resultJOSB.append("{\"mode\":\"w\",\"default\":3600000,\"name\":\"超时值\",\"id\":\"overtime\",\"type\":\"double\"},");
					resultJOSB.append("{\"mode\":\"e\",\"name\":\"状态\",\"expr\":\"(time+overtime) > new Date()\",\"id\":\"online\",\"type\":\"bool\",");
						resultJOSB.append("\"list\":[");
							resultJOSB.append("{\"html\":\"在线\",\"value\":true},");
							resultJOSB.append("{\"html\":\"离线\",\"value\":false}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"r\",\"name\":\"最近激活时间\",\"id\":\"time\",\"type\":\"datetime\"}");
				resultJOSB.append("],");
				*/
			resultJOSB.append("\"engineMask\":2},");
			resultJOSB.append("{\"css\":\"\",\"icon\":\"sub-menu-icon6\",\"name\":\"闸机\",\"id\":\"GAT\",");
				resultJOSB.append("\"fields\":[");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"名称\",\"id\":\"name\",\"type\":\"string\"}");
				resultJOSB.append("],");
			resultJOSB.append("\"engineMask\":255},");
			resultJOSB.append("{\"name\":\"指示牌\",\"id\":\"LAB\",\"engineMask\":255},");
			resultJOSB.append("{\"css\":\"\",\"icon\":\"sub-menu-icon6\",\"name\":\"监控摄像头\",\"id\":\"SXT\",");
				resultJOSB.append("\"fields\":[");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"摄像头编号\",\"id\":\"labelId\",\"type\":\"double\"}");
				resultJOSB.append("],");
			resultJOSB.append("\"engineMask\":255}");
		resultJOSB.append("],");
		resultJOSB.append("\"id\":1,\"jsonrpc\":\"2.0\"}");
		
		return new JSONObject(resultJOSB.toString());
	}

	/**
	 * 2.2.4 获取系统实体类型
	 * @return
	 */
	public static JSONObject getEntityTypes() {
		StringBuilder resultJOSB=new StringBuilder();
		resultJOSB.append("{\"result\":[");
			resultJOSB.append("{\"css\":\"\",\"icon\":\"sub-menu-icon6\",\"name\":\"人员\",\"id\":\"staff\",");
				resultJOSB.append("\"fields\":[");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"name\",\"type\":\"text\",\"title\":\"名称\",");
						resultJOSB.append("\"validate\":[");
							resultJOSB.append("{\"validator\":\"required\",\"message\":\"名称未填写\"}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"w\",\"sorting\":false,\"name\":\"sex\",\"type\":\"select\",\"valueField\":\"id\",\"title\":\"性别\",");
						resultJOSB.append("\"items\":[");
							resultJOSB.append("{\"name\":\"男\",\"id\":1},");
							resultJOSB.append("{\"name\":\"女\",\"id\":2}");
						resultJOSB.append("],");
					resultJOSB.append("\"filtering\":false,\"textField\":\"name\"},");
					resultJOSB.append("{\"mode\":\"w\",\"sorting\":false,\"name\":\"age\",\"type\":\"text\",\"title\":\"年龄\"},");
					resultJOSB.append("{\"mode\":\"w\",\"sorting\":false,\"name\":\"phone\",\"type\":\"text\",\"title\":\"电话\"},");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"pid\",\"type\":\"text\",\"title\":\"工号\",");
						resultJOSB.append("\"validate\":[");
							resultJOSB.append("{\"validator\":\"required\",\"message\":\"工号未填写\"}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"post\",\"type\":\"text\",\"title\":\"岗位\"}");
				resultJOSB.append("]");
			resultJOSB.append("},");
			resultJOSB.append("{\"css\":\"\",\"icon\":\"sub-menu-icon6\",\"name\":\"其他\",\"id\":\"other\",");
				resultJOSB.append("\"fields\":[");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"name\",\"type\":\"text\",\"title\":\"名称\",");
						resultJOSB.append("\"validate\":[");
							resultJOSB.append("{\"validator\":\"required\",\"message\":\"名称未填写\"}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"pid\",\"type\":\"text\",\"title\":\"编号\",");
						resultJOSB.append("\"validate\":[");
							resultJOSB.append("{\"validator\":\"required\",\"message\":\"编号未填写\"}");
						resultJOSB.append("]");
					resultJOSB.append("}");
				resultJOSB.append("]");
			resultJOSB.append("},");
			resultJOSB.append("{\"css\":\"\",\"icon\":\"sub-menu-icon6\",\"name\":\"车辆\",\"id\":\"car\",");
				resultJOSB.append("\"fields\":[");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"name\",\"type\":\"text\",\"title\":\"名称\",");
						resultJOSB.append("\"validate\":[");
							resultJOSB.append("{\"validator\":\"required\",\"message\":\"名称未填写\"}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"pid\",\"type\":\"text\",\"title\":\"车牌号\",");
						resultJOSB.append("\"validate\":[");
							resultJOSB.append("{\"validator\":\"required\",\"message\":\"车牌号未填写\"}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"post\",\"type\":\"text\",\"title\":\"司机\"}");
				resultJOSB.append("]");
			resultJOSB.append("}");
		resultJOSB.append("],\"id\":1,\"jsonrpc\":\"2.0\"}");
		
		return new JSONObject(resultJOSB.toString());
	}
	
	/**
	 * 2.2.19获取历史轨迹
	 * @return
	 */
	public static JSONObject getLocationRecords() {
		StringBuilder resultJOSB=new StringBuilder();
		resultJOSB.append("{\"result\":[");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":1,\"tagId\":\"BTT32003897\",\"latitude\":32.26538412419374,\"engineType\":null,\"speed\":0.185,\"recordId\":210610029309169657,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7034\",\"routerId\":\"BTR0794E313\",\"routerMark\":274,\"jGateId\":\"7034\",\"id\":63726006,\"state\":0,\"floor\":1,\"routerFlowId\":144213,\"flowId\":603981723,\"direction\":6,\"longitude\":119.10902258516153,\"entityId\":24318,\"raiseTime2\":1624241823586,\"uploadTime\":1624241823588,\"userId\":\"3897\",\"beacons\":\"BTI24007034(4500)\",\"blockId\":null,\"intensity\":4500,\"areaId\":2,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:17:03.586+0800\",\"x\":74.959,\"y\":316.72,\"gpsType\":\"wgs84\",\"z\":0,\"step\":1,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.26538412419374,\"engineType\":null,\"speed\":0,\"recordId\":210610029309171037,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7034\",\"routerId\":\"BTR0794E313\",\"routerMark\":274,\"jGateId\":\"7034\",\"id\":63726446,\"state\":0,\"floor\":1,\"routerFlowId\":144213,\"flowId\":603981723,\"direction\":6,\"longitude\":119.10902258516153,\"entityId\":24318,\"raiseTime2\":1624241854675,\"uploadTime\":1624241854685,\"userId\":\"3897\",\"beacons\":\"BTI24007034(4500)\",\"blockId\":null,\"intensity\":4500,\"areaId\":2,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:17:34.675+0800\",\"x\":74.959,\"y\":316.72,\"gpsType\":\"wgs84\",\"z\":0,\"step\":1,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.265325310116005,\"engineType\":null,\"speed\":0.099,\"recordId\":210610029309171395,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7043\",\"routerId\":\"BTR0794E313\",\"routerMark\":274,\"jGateId\":\"7043\",\"id\":63726551,\"state\":0,\"floor\":2,\"routerFlowId\":144980,\"flowId\":603981769,\"direction\":6,\"longitude\":119.10906042099948,\"entityId\":24318,\"raiseTime2\":1624241863226,\"uploadTime\":1624241863232,\"userId\":\"3897\",\"beacons\":\"BTI24007043(5500),BTI24007042(5500),BTI24007044(5900)\",\"blockId\":null,\"intensity\":5633,\"areaId\":3,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:17:43.226+0800\",\"x\":78.524,\"y\":310.198,\"gpsType\":\"wgs84\",\"z\":0,\"step\":3,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.26526649602639,\"engineType\":null,\"speed\":0.162,\"recordId\":210610029309171484,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7043\",\"routerId\":\"BTR0794E313\",\"routerMark\":274,\"jGateId\":\"7043\",\"id\":63726587,\"state\":0,\"floor\":2,\"routerFlowId\":144980,\"flowId\":603981769,\"direction\":6,\"longitude\":119.10909825678866,\"entityId\":24318,\"raiseTime2\":1624241864738,\"uploadTime\":1624241864748,\"userId\":\"3897\",\"beacons\":\"BTI24007043(5500),BTI24007042(5500),BTI24007044(5900)\",\"blockId\":null,\"intensity\":5633,\"areaId\":3,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:17:44.738+0800\",\"x\":82.089,\"y\":303.676,\"gpsType\":\"wgs84\",\"z\":0,\"step\":1,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.26533807026467,\"engineType\":null,\"speed\":0.212,\"recordId\":210610029309172571,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7044\",\"routerId\":\"BTR9F81F8A6\",\"routerMark\":270,\"jGateId\":\"7044\",\"id\":63726973,\"state\":0,\"floor\":2,\"routerFlowId\":114817,\"flowId\":603981795,\"direction\":6,\"longitude\":119.10907188365,\"entityId\":24318,\"raiseTime2\":1624241889102,\"uploadTime\":1624241889110,\"userId\":\"3897\",\"beacons\":\"BTI24007044(1800)\",\"blockId\":null,\"intensity\":1800,\"areaId\":3,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:18:09.102+0800\",\"x\":79.604,\"y\":311.613,\"gpsType\":\"wgs84\",\"z\":0,\"step\":3,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.265409644496614,\"engineType\":null,\"speed\":0.347,\"recordId\":210610029309172661,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7044\",\"routerId\":\"BTR9F81F8A6\",\"routerMark\":270,\"jGateId\":\"7044\",\"id\":63726998,\"state\":0,\"floor\":2,\"routerFlowId\":114817,\"flowId\":603981795,\"direction\":6,\"longitude\":119.10904551046995,\"entityId\":24318,\"raiseTime2\":1624241891163,\"uploadTime\":1624241891173,\"userId\":\"3897\",\"beacons\":\"BTI24007044(1800)\",\"blockId\":null,\"intensity\":1800,\"areaId\":3,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:18:11.163+0800\",\"x\":77.119,\"y\":319.55,\"gpsType\":\"wgs84\",\"z\":0,\"step\":1,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.265409644496614,\"engineType\":null,\"speed\":0,\"recordId\":210610029309173832,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7044\",\"routerId\":\"BTR0794E313\",\"routerMark\":274,\"jGateId\":\"7044\",\"id\":63727394,\"state\":0,\"floor\":2,\"routerFlowId\":145771,\"flowId\":603981819,\"direction\":6,\"longitude\":119.10904551046995,\"entityId\":24318,\"raiseTime2\":1624241917493,\"uploadTime\":1624241924130,\"userId\":\"3897\",\"beacons\":\"BTI24007044(2050)\",\"blockId\":null,\"intensity\":2050,\"areaId\":3,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:18:37.493+0800\",\"x\":77.119,\"y\":319.55,\"gpsType\":\"wgs84\",\"z\":0,\"step\":1,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.265409644496614,\"engineType\":null,\"speed\":0,\"recordId\":210610029309173926,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7044\",\"routerId\":\"BTR0794E313\",\"routerMark\":274,\"jGateId\":\"7044\",\"id\":63727481,\"state\":0,\"floor\":2,\"routerFlowId\":145771,\"flowId\":603981819,\"direction\":6,\"longitude\":119.10904551046995,\"entityId\":24318,\"raiseTime2\":1624241919626,\"uploadTime\":1624241924245,\"userId\":\"3897\",\"beacons\":\"BTI24007044(2050)\",\"blockId\":null,\"intensity\":2050,\"areaId\":3,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:18:39.626+0800\",\"x\":77.119,\"y\":319.55,\"gpsType\":\"wgs84\",\"z\":0,\"step\":1,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.265409644496614,\"engineType\":null,\"speed\":0,\"recordId\":210610029309174025,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7044\",\"routerId\":\"BTR0794E313\",\"routerMark\":274,\"jGateId\":\"7044\",\"id\":63727564,\"state\":0,\"floor\":2,\"routerFlowId\":145771,\"flowId\":603981819,\"direction\":6,\"longitude\":119.10904551046995,\"entityId\":24318,\"raiseTime2\":1624241921814,\"uploadTime\":1624241924412,\"userId\":\"3897\",\"beacons\":\"BTI24007044(2050)\",\"blockId\":null,\"intensity\":2050,\"areaId\":3,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:18:41.814+0800\",\"x\":77.119,\"y\":319.55,\"gpsType\":\"wgs84\",\"z\":0,\"step\":1,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.265409644496614,\"engineType\":null,\"speed\":0,\"recordId\":210610029309173559,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7044\",\"routerId\":\"BTR0794E313\",\"routerMark\":274,\"jGateId\":\"7044\",\"id\":63727266,\"state\":0,\"floor\":2,\"routerFlowId\":145771,\"flowId\":603981819,\"direction\":6,\"longitude\":119.10904551046995,\"entityId\":24318,\"raiseTime2\":1624241922252,\"uploadTime\":1624241922263,\"userId\":\"3897\",\"beacons\":\"BTI24007044(2050)\",\"blockId\":null,\"intensity\":2050,\"areaId\":3,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:18:42.252+0800\",\"x\":77.119,\"y\":319.55,\"gpsType\":\"wgs84\",\"z\":0,\"step\":1,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.26542256257703,\"engineType\":null,\"speed\":0.034,\"recordId\":210610029309178920,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7035\",\"routerId\":\"BTR0794E313\",\"routerMark\":274,\"jGateId\":\"7035\",\"id\":63727699,\"state\":0,\"floor\":1,\"routerFlowId\":147676,\"flowId\":603981940,\"direction\":6,\"longitude\":119.10903871796692,\"entityId\":24318,\"raiseTime2\":1624242034177,\"uploadTime\":1624242034429,\"userId\":\"3897\",\"beacons\":\"BTI24007035(4800)\",\"blockId\":null,\"intensity\":4800,\"areaId\":2,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:20:34.177+0800\",\"x\":76.479,\"y\":320.982,\"gpsType\":\"wgs84\",\"z\":0,\"step\":4,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.26543951755698,\"engineType\":null,\"speed\":1.221,\"recordId\":210610029309179006,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7035\",\"routerId\":\"BTR7129ED92\",\"routerMark\":107,\"jGateId\":\"7035\",\"id\":63727741,\"state\":0,\"floor\":1,\"routerFlowId\":73783,\"flowId\":603981941,\"direction\":6,\"longitude\":119.10902980280378,\"entityId\":24318,\"raiseTime2\":1624242038570,\"uploadTime\":1624242038576,\"userId\":\"3897\",\"beacons\":\"BTI24007035(3700)\",\"blockId\":null,\"intensity\":4250,\"areaId\":2,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:20:38.570+0800\",\"x\":75.64,\"y\":322.862,\"gpsType\":\"wgs84\",\"z\":0,\"step\":4,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.26545716794778,\"engineType\":null,\"speed\":3.8,\"recordId\":210610029309179175,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7035\",\"routerId\":\"BTRCE59E8D3\",\"routerMark\":113,\"jGateId\":\"7035\",\"id\":63727833,\"state\":0,\"floor\":1,\"routerFlowId\":56128,\"flowId\":603981943,\"direction\":6,\"longitude\":119.10908048915192,\"entityId\":24318,\"raiseTime2\":1624242039488,\"uploadTime\":1624242043381,\"userId\":\"3897\",\"beacons\":\"BTI24007036(3700),BTI24007037(12750)\",\"blockId\":null,\"intensity\":7066,\"areaId\":2,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:20:39.488+0800\",\"x\":80.415,\"y\":324.819,\"gpsType\":\"wgs84\",\"z\":0,\"step\":3,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.265450765289486,\"engineType\":null,\"speed\":2.258,\"recordId\":210610029309179092,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7035\",\"routerId\":\"BTR0794E313\",\"routerMark\":274,\"jGateId\":\"7035\",\"id\":63727771,\"state\":0,\"floor\":1,\"routerFlowId\":147707,\"flowId\":603981942,\"direction\":6,\"longitude\":119.10904079456402,\"entityId\":24318,\"raiseTime2\":1624242043132,\"uploadTime\":1624242043141,\"userId\":\"3897\",\"beacons\":\"BTI24007035(3950),BTI24007036(4500),BTI24007037(12750)\",\"blockId\":null,\"intensity\":3700,\"areaId\":2,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:20:43.132+0800\",\"x\":76.675,\"y\":324.109,\"gpsType\":\"wgs84\",\"z\":0,\"step\":4,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.265502557014734,\"engineType\":null,\"speed\":7.71,\"recordId\":210610029309179263,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7036\",\"routerId\":\"BTR63F5C243\",\"routerMark\":270,\"jGateId\":\"7036\",\"id\":63727878,\"state\":0,\"floor\":1,\"routerFlowId\":146946,\"flowId\":603981945,\"direction\":6,\"longitude\":119.10915563978497,\"entityId\":24318,\"raiseTime2\":1624242045600,\"uploadTime\":1624242047468,\"userId\":\"3897\",\"beacons\":\"BTI24007036(2650),BTI24007037(12750),BTI24007304(12750)\",\"blockId\":null,\"intensity\":8225,\"areaId\":2,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:20:45.600+0800\",\"x\":87.496,\"y\":329.853,\"gpsType\":\"wgs84\",\"z\":0,\"step\":3,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.26550101563167,\"engineType\":null,\"speed\":7.699,\"recordId\":210610029309179347,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7036\",\"routerId\":\"BTR63F5C243\",\"routerMark\":270,\"jGateId\":\"7036\",\"id\":63727901,\"state\":0,\"floor\":1,\"routerFlowId\":147007,\"flowId\":603981949,\"direction\":6,\"longitude\":119.10916645512913,\"entityId\":24318,\"raiseTime2\":1624242047798,\"uploadTime\":1624242047812,\"userId\":\"3897\",\"beacons\":\"BTI24007037(3050),BTI24007036(3250)\",\"blockId\":null,\"intensity\":6650,\"areaId\":2,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:20:47.798+0800\",\"x\":88.515,\"y\":329.682,\"gpsType\":\"wgs84\",\"z\":0,\"step\":4,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.265480300316725,\"engineType\":null,\"speed\":6.899,\"recordId\":210610029309179429,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7036\",\"routerId\":\"BTR13E2D61D\",\"routerMark\":273,\"jGateId\":\"7036\",\"id\":63727920,\"state\":0,\"floor\":1,\"routerFlowId\":120898,\"flowId\":603981950,\"direction\":6,\"longitude\":119.10916402631705,\"entityId\":24318,\"raiseTime2\":1624242048703,\"uploadTime\":1624242048707,\"userId\":\"3897\",\"beacons\":\"BTI24007038(3050),BTI24007037(3250),BTI24007036(4500)\",\"blockId\":null,\"intensity\":3150,\"areaId\":2,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:20:48.703+0800\",\"x\":88.286,\"y\":327.385,\"gpsType\":\"wgs84\",\"z\":0,\"step\":3,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.26546306070541,\"engineType\":null,\"speed\":8.294,\"recordId\":210610029309179778,\"voltUnit\":\"V\",\"labelId\":null,\"gateId\":null,\"routerId\":\"BTR13E2D61D\",\"routerMark\":273,\"jGateId\":null,\"id\":63728051,\"state\":0,\"floor\":1,\"routerFlowId\":121008,\"flowId\":603981959,\"direction\":1,\"longitude\":119.10932164579933,\"entityId\":24318,\"raiseTime2\":1624242054335,\"uploadTime\":1624242060236,\"userId\":\"3897\",\"beacons\":\"BTI24007039(3450),BTI24006196(12750)\",\"blockId\":null,\"intensity\":6866,\"areaId\":2,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:20:54.335+0800\",\"x\":103.137,\"y\":325.473,\"gpsType\":\"wgs84\",\"z\":0,\"step\":4,\"rootAreaId\":1,\"engineId\":\"a1\"},");
			resultJOSB.append("{\"altitude\":5.9,\"flag\":0,\"tagId\":\"BTT32003897\",\"latitude\":32.26546383313037,\"engineType\":null,\"speed\":5.871,\"recordId\":210610029309179515,\"voltUnit\":\"V\",\"labelId\":\"2160\",\"gateId\":\"7036\",\"routerId\":\"BTR0794E313\",\"routerMark\":274,\"jGateId\":\"7036\",\"id\":63727945,\"state\":0,\"floor\":1,\"routerFlowId\":147867,\"flowId\":604047488,\"direction\":6,\"longitude\":119.10917778514415,\"entityId\":24318,\"raiseTime2\":1624242054474,\"uploadTime\":1624242054480,\"userId\":\"3897\",\"beacons\":\"BTI24007038(3050),BTI24007037(3250),BTI24007036(4500)\",\"blockId\":null,\"intensity\":3600,\"areaId\":2,\"absolute\":true,\"volt\":3.7,\"raiseTime\":\"2021-06-21T10:20:54.474+0800\",\"x\":89.582,\"y\":325.559,\"gpsType\":\"wgs84\",\"z\":0,\"step\":1,\"rootAreaId\":1,\"engineId\":\"a1\"}");
		resultJOSB.append("],\"id\":1,\"jsonrpc\":\"2.0\"}");

		return new JSONObject(resultJOSB.toString());
	}
	
	/**
	 * 2.8.2 实时在线人数及区域统计
	 * @return
	 */
	public static JSONObject summaryOnlineEntity() {
		StringBuilder resultJOSB=new StringBuilder();
		resultJOSB.append("{\"result\":{\"summary\":{\"online\":{\"total\":106,\"car\":3,\"staff\":103}},");
			resultJOSB.append("\"children\":[");
				resultJOSB.append("{\"summary\":{\"online\":{\"total\":1,\"car\":0,\"staff\":1}},\"name\":\"二层\",\"id\":3},");
				resultJOSB.append("{\"summary\":{\"online\":{\"total\":0,\"car\":0,\"staff\":0}},\"name\":\"三层\",\"id\":4},");
				resultJOSB.append("{\"summary\":{\"online\":{\"total\":0,\"car\":0,\"staff\":0}},\"name\":\"四层\",\"id\":5},");
				resultJOSB.append("{\"summary\":{\"online\":{\"total\":105,\"car\":3,\"staff\":102}},\"name\":\"一层\",\"id\":2},");
				resultJOSB.append("{\"summary\":{\"online\":{\"total\":0,\"car\":0,\"staff\":0}},\"name\":\"五层\",\"id\":6}");
			resultJOSB.append("],");
			resultJOSB.append("\"name\":\"总图\",\"id\":1},");
		resultJOSB.append("\"id\":1,\"jsonrpc\":\"2.0\"}");
		
		return new JSONObject(resultJOSB.toString());
	}

	public static void addMoreLocationRecord(LocationRecord nextLR,
			List<LocationRecord> locRecList) {
		// TODO Auto-generated method stub
		if(locRecList.size()>0) {
			LocationRecord preLR = locRecList.get(locRecList.size()-1);
			Float preX = preLR.getX();
			Float preY = preLR.getY();
			Long preUploadTime = preLR.getUploadTime();
			
			Float nextX = nextLR.getX();
			Float nextY = nextLR.getY();
			Long nextUploadTime = nextLR.getUploadTime();
			//System.out.println("+++"+(nextUploadTime-preUploadTime));
			float zbcX=0;
			float zbcY=0;
			float zbSpaceX=0;
			float zbSpaceY=0;
			int lrCount=3;
			if(nextX>preX) {
				zbcX=nextX-preX;
				zbSpaceX=zbcX/lrCount;
			}
			
			if(nextY>preY) {
				zbcY=nextY-preY;
				zbSpaceY=zbcY/lrCount;
			}
			
			if(zbSpaceX>0||zbSpaceY>0) {
				for (int i = 0; i < lrCount; i++) {
					LocationRecord lr = locRecList.get(locRecList.size()-1);
					System.out.println(zbSpaceX+"==="+lr.getX()+"==="+(lr.getX()+zbSpaceX));
					if(nextX>preX)
						lr.setX(lr.getX()+zbSpaceX);
					else if(nextX<preX)
						lr.setX(lr.getX()-zbSpaceX);
					
					if(nextY>preY)
						lr.setY(lr.getY()+zbSpaceY);
					else if(nextY<preY)
						lr.setY(lr.getY()-zbSpaceY);
					locRecList.add(lr);
				}
				//System.out.println(zbSpaceX+","+zbSpaceY);
			}
			System.out.println("x1="+preX+",y2="+preY+",uploadTime2="+preUploadTime+",x2="+nextX+",y2="+nextY+",uploadTime2="+nextUploadTime);
		}
	}
}
