package com.positionPhZY.utils;

import org.json.JSONObject;

public class APIResultUtil {

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
}
