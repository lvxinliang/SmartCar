package com.hanry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Point;

public class Utils {

	public static String castByteToHexString(byte b) {
			String hex = Integer.toHexString(b & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			return hex.toUpperCase();
	}
	
	public static String castBytesToHexString(byte[] bytes){
		String res = "";
		for (byte b : bytes) {
			res += castByteToHexString(b);
		}
		return "0x"+res;
	}
	
	public static boolean isEffectiveIpAddr(String ipAddr){
		Pattern pattern = Pattern.compile("^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");
        Matcher matcher = pattern.matcher(ipAddr);  
        return matcher.matches();  
	}
	public static boolean isEffectiveNetPort(String netPort){
		Pattern pattern = Pattern.compile("^([1-9]|[1-9]\\d{3}|[1-6][0-5][0-5][0-3][0-5])$");
		Matcher matcher = pattern.matcher(netPort);  
		return matcher.matches();  
	}
	public static boolean isEffectiveWifiSSID(String ssid){
		if(!isNullOrEmpty(ssid) && ssid.length() <= 20){
			return true;
		}
		return false;
	}

	public static boolean isNullOrEmpty(String string) {
		if(null == string || "".equals(string)){
			return true;
		}
		return false;
	}

	public static int getOffsetValue(int src, int srcRange, int destRange) {
		int value = Double.valueOf(Math.floor(destRange * src / srcRange)).intValue(); //获得比例值
		value  = (value + 4) / 10; //取得对应的离散值
		value = value > 18 ? 18 :value;
		return value;
	}
}
