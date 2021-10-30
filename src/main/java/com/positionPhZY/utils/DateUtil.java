package com.positionPhZY.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	//private static DateFormat timeDF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	private static DateFormat timeDF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String convertLongToString(long longDate) {
		Date date = new Date(longDate);
		return timeDF.format(date);
	}
	
	public static Long convertStringToLong(String fmtDate) throws ParseException {
		Date date = timeDF.parse(fmtDate);
		return date.getTime();
	}
	
	public static void main(String[] args) {
		//DateUtil.convertLongToString(Long.parseLong("1634626280668"));
		try {
			DateUtil.convertStringToLong("2021-10-20 08:36:36:429");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
