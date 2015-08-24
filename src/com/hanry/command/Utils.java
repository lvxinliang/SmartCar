package com.hanry.command;

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
}
