package net.simplydone.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils 
{
	public static final SimpleDateFormat FULL_DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	public static Date ValueToDate(String val) throws ParseException 
	{
		Date resultDate = null;
		if(val != null && !val.equals(StringUtils.EMPTY_STRING))
		{
			resultDate = DateUtils.FULL_DATE_TIME_FORMAT.parse(val.toString());
		}
		return resultDate;
	}
	

	public static  String DateToValue(Object date) throws ParseException 
	{
		return DateUtils.FULL_DATE_TIME_FORMAT.format(date);
	}
	
	
}
