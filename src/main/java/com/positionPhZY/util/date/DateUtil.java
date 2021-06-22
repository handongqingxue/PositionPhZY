package com.positionPhZY.util.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

	private static DateFormat timeDF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	
	public static String convertLongToString(long longDate) {
		Date date = new Date(longDate);
		return timeDF.format(date);
	}
	
	public static Long convertStringToLong(String fmtDate) throws ParseException {
		Date date = timeDF.parse(fmtDate);
		return date.getTime();
	}
}
