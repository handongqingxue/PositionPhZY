package com.positionPhZY.utils;

import org.json.JSONObject;

/**
 * �����ķ������صĽ���ǹ̶��ģ���������̱�����ӿڲ��ܷ���ʱ,��ʱ������Щ����
 * */
public class APIResultUtil {

	/**
	 * 2.2.4 ��ȡϵͳʵ������
	 * @return
	 */
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
	
	/**
	 * 2.8.2 ʵʱ��������������ͳ��
	 * @return
	 */
	public static JSONObject summaryOnlineEntity() {
		StringBuilder resultJOSB=new StringBuilder();
		resultJOSB.append("{\"result\":{\"summary\":{\"online\":{\"total\":106,\"car\":3,\"staff\":103}},");
			resultJOSB.append("\"children\":[");
				resultJOSB.append("{\"summary\":{\"online\":{\"total\":1,\"car\":0,\"staff\":1}},\"name\":\"����\",\"id\":3},");
				resultJOSB.append("{\"summary\":{\"online\":{\"total\":0,\"car\":0,\"staff\":0}},\"name\":\"����\",\"id\":4},");
				resultJOSB.append("{\"summary\":{\"online\":{\"total\":0,\"car\":0,\"staff\":0}},\"name\":\"�Ĳ�\",\"id\":5},");
				resultJOSB.append("{\"summary\":{\"online\":{\"total\":105,\"car\":3,\"staff\":102}},\"name\":\"һ��\",\"id\":2},");
				resultJOSB.append("{\"summary\":{\"online\":{\"total\":0,\"car\":0,\"staff\":0}},\"name\":\"���\",\"id\":6}");
			resultJOSB.append("],");
			resultJOSB.append("\"name\":\"��ͼ\",\"id\":1},");
		resultJOSB.append("\"id\":1,\"jsonrpc\":\"2.0\"}");
		
		return new JSONObject(resultJOSB.toString());
	}
}
