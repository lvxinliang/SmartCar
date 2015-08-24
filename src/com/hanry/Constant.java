package com.hanry;


public class Constant {
	public static final String PREF_KEY_ROUTER_URL = "pref_key_router_url";
	
	public static final String DEFAULT_VALUE_ROUTER_URL = "192.168.1.1:2001";
	
	public static final int MIN_COMMAND_REC_INTERVAL = 1000;//ms
	
    public static final String ACTION_TAKE_PICTURE_DONE = "hanry.take_picture_done";
    public static final String EXTRA_RES = "res";
    public static final String EXTRA_PATH = "path";
   
    public final static int CAM_RES_OK = 6;
    public final static int CAM_RES_FAIL_FILE_WRITE_ERROR = 7;
    public final static int CAM_RES_FAIL_FILE_NAME_ERROR = 8;
    public final static int CAM_RES_FAIL_NO_SPACE_LEFT = 9;
    public final static int CAM_RES_FAIL_BITMAP_ERROR = 10;
    public final static int CAM_RES_FAIL_UNKNOW = 20;
    
}
