package com.alvin.health.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilTime {
	public static String getCurrentDateTime(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}
}
