package com.positionPhZY.utils;

import org.json.JSONObject;

public class APIResultUtil {

	public static JSONObject getEntityTypes() {
		StringBuilder resultJOSB=new StringBuilder();
		resultJOSB.append("{\"result\":[");
			resultJOSB.append("{\"css\":\"\",\"icon\":\"sub-menu-icon6\",\"name\":\"��Ա\",\"id\":\"staff\",");
				resultJOSB.append("\"fields\":[");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"name\",\"type\":\"text\",\"title\":\"����\",");
						resultJOSB.append("\"validate\":[");
							resultJOSB.append("{\"validator\":\"required\",\"message\":\"����δ��д\"}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"w\",\"sorting\":false,\"name\":\"sex\",\"type\":\"select\",\"valueField\":\"id\",\"title\":\"�Ա�\",");
						resultJOSB.append("\"items\":[");
							resultJOSB.append("{\"name\":\"��\",\"id\":1},");
							resultJOSB.append("{\"name\":\"Ů\",\"id\":2}");
						resultJOSB.append("],");
					resultJOSB.append("\"filtering\":false,\"textField\":\"name\"},");
					resultJOSB.append("{\"mode\":\"w\",\"sorting\":false,\"name\":\"age\",\"type\":\"text\",\"title\":\"����\"},");
					resultJOSB.append("{\"mode\":\"w\",\"sorting\":false,\"name\":\"phone\",\"type\":\"text\",\"title\":\"�绰\"},");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"pid\",\"type\":\"text\",\"title\":\"����\",");
						resultJOSB.append("\"validate\":[");
							resultJOSB.append("{\"validator\":\"required\",\"message\":\"����δ��д\"}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"post\",\"type\":\"text\",\"title\":\"��λ\"}");
				resultJOSB.append("]");
			resultJOSB.append("},");
			resultJOSB.append("{\"css\":\"\",\"icon\":\"sub-menu-icon6\",\"name\":\"����\",\"id\":\"other\",");
				resultJOSB.append("\"fields\":[");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"name\",\"type\":\"text\",\"title\":\"����\",");
						resultJOSB.append("\"validate\":[");
							resultJOSB.append("{\"validator\":\"required\",\"message\":\"����δ��д\"}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"pid\",\"type\":\"text\",\"title\":\"���\",");
						resultJOSB.append("\"validate\":[");
							resultJOSB.append("{\"validator\":\"required\",\"message\":\"���δ��д\"}");
						resultJOSB.append("]");
					resultJOSB.append("}");
				resultJOSB.append("]");
			resultJOSB.append("},");
			resultJOSB.append("{\"css\":\"\",\"icon\":\"sub-menu-icon6\",\"name\":\"����\",\"id\":\"car\",");
				resultJOSB.append("\"fields\":[");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"name\",\"type\":\"text\",\"title\":\"����\",");
						resultJOSB.append("\"validate\":[");
							resultJOSB.append("{\"validator\":\"required\",\"message\":\"����δ��д\"}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"pid\",\"type\":\"text\",\"title\":\"���ƺ�\",");
						resultJOSB.append("\"validate\":[");
							resultJOSB.append("{\"validator\":\"required\",\"message\":\"���ƺ�δ��д\"}");
						resultJOSB.append("]");
					resultJOSB.append("},");
					resultJOSB.append("{\"mode\":\"w\",\"name\":\"post\",\"type\":\"text\",\"title\":\"˾��\"}");
				resultJOSB.append("]");
			resultJOSB.append("}");
		resultJOSB.append("],\"id\":1,\"jsonrpc\":\"2.0\"}");
		
		return new JSONObject(resultJOSB.toString());
	}
}
