package com.positionPhZY.util.date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	private static DateFormat timeDF=new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
	
	public static String convertLongToString(long longDate) {
		Date date = new Date(longDate);
		return timeDF.format(date);
	}
}
