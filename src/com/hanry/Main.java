package com.hanry;
import java.io.BufferedInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.FontAwesomeText;
import com.hanry.Constant.CommandArray;
import com.hanry.views.FrontAndBackJoystickView;
import com.hanry.views.FrontAndBackJoystickView.OnFrontAndBackJoystickMoveListener;
import com.hanry.views.LeftAndRightJoystickView;
import com.hanry.views.LeftAndRightJoystickView.OnLeftAndRightJoystickMoveListener;

public class Main extends Activity implements SeekBar.OnSeekBarChangeListener
{
    protected static final String TAG = "MainActivity";
	private final int MSG_ID_ERR_CONN = 1001;
    //private final int MSG_ID_ERR_SEND = 1002;
    private final int MSG_ID_ERR_RECEIVE = 1003;
    private final int MSG_ID_CON_READ = 1004;
    private final int MSG_ID_CON_SUCCESS = 1005;    
    private final int MSG_ID_START_CHECK = 1006;
    private final int MSG_ID_ERR_INIT_READ = 1007;
    private final int MSG_ID_CLEAR_QUIT_FLAG = 1008;
    
    private final int MSG_ID_LOOP_START = 1010;
    private final int MSG_ID_HEART_BREAK_RECEIVE = 1011;
    private final int MSG_ID_HEART_BREAK_SEND = 1012;
    private final int MSG_ID_LOOP_END = 1013;
    
    private final int STATUS_INIT = 0x2001;
    //private final int STATUS_CONNECTING = 0x2002;
    private final int STATUS_CONNECTED = 0x2003;
    private final int WARNING_ICON_OFF_DURATION_MSEC = 600;
    private final int WARNING_ICON_ON_DURATION_MSEC = 800;    
    
    private final int WIFI_STATE_UNKNOW = 0x3000;
    private final int WIFI_STATE_DISABLED = 0x3001;
    private final int WIFI_STATE_NOT_CONNECTED = 0x3002;
    private final int WIFI_STATE_CONNECTED = 0x3003;
    
    private final int MIN_GEAR_STEP = 5;
    private final int MAX_GEAR_VALUE = 180;
    private final int INIT_GEAR_VALUE = 50;
    
    private final byte COMMAND_PERFIX = -1;
    private final int HEART_BREAK_CHECK_INTERVAL = 8000;//ms
    private final int QUIT_BUTTON_PRESS_INTERVAL = 2500;//ms
    private final int HEART_BREAK_SEND_INTERVAL = 2500;//ms
    
    private boolean m4test = false;

    private String CAMERA_VIDEO_URL = "http://192.168.2.1:8080/?action=stream";
    private String CAMERA_VIDEO_URL_TEST = "";
    private String ROUTER_CONTROL_URL = "192.168.2.1";
    private String ROUTER_CONTROL_URL_TEST = "192.168.1.1";
    private int ROUTER_CONTROL_PORT = 2001;
    private int ROUTER_CONTROL_PORT_TEST = 2001;
    private final String WIFI_SSID_PERFIX = "robot";
    
    private FontAwesomeText TakePicture;
    
    private FontAwesomeText mAnimIndicator;
    private boolean bAnimationEnabled = true;
    private boolean bReaddyToSendCmd = false;
    private TextView mLogText;
    
    private SeekBar mSeekBar;
    private int  mSeekBarValue = -1;
    
    private FontAwesomeText buttonSetting;
    private FontAwesomeText buttonLen;
    private boolean bLenon = false;
    private int mWifiStatus = STATUS_INIT;

    private Thread mThreadClient = null;
    private boolean mThreadFlag = false;

    private boolean mQuitFlag = false;
    private boolean bHeartBreakFlag = false;
    private int mHeartBreakCounter = 0;
    private int mLastCounter = 0;
    
    private Context mContext;
    SocketClient mtcpSocket;
    MjpegView backgroundView = null;
	private int leftAndRightPower;
	private int leftAndRightDirection;
	private int frontAndBackPower;
	private int frontAndBackDirection;
	private byte[] lastFrontAndBackCommand = {0};
	private byte[] lastLeftAndRightCommand = {0};
    
    private byte[] COMM_FORWARD = {(byte) 0xFF, (byte)0x00, (byte)0x01, (byte)0x00, (byte) 0xFF};
    private byte[] COMM_BACKWARD = {(byte) 0xFF, 0x00, 0x02, 0x00, (byte) 0xFF};
    private byte[] COMM_STOP = {(byte) 0xFF, 0x00, 0x00, 0x00, (byte) 0xFF};
    private byte[] COMM_LEFT = {(byte) 0xFF, 0x00, 0x03, 0x00, (byte) 0xFF};
    private byte[] COMM_RIGHT = {(byte) 0xFF, 0x00, 0x04, 0x00, (byte) 0xFF};
    
    private byte[] COMM_LEN_ON = {(byte) 0xFF, 0x04, 0x03, 0x00, (byte) 0xFF};
    private byte[] COMM_LEN_OFF = {(byte) 0xFF, 0x04, 0x02, 0x00, (byte) 0xFF};

    private byte[] COMM_GEAR_CONTROL = {(byte) 0xFF, 0x01, 0x01, 0x00, (byte) 0xFF};
    
    private byte[] COMM_SELF_CHECK = {(byte) 0xFF, (byte)0xEE, (byte)0xEE, 0x00, (byte) 0xFF};
    private byte[] COMM_SELF_CHECK_ALL = {(byte) 0xFF, (byte)0xEE, (byte)0xE0, 0x00, (byte) 0xFF};

    private byte[] COMM_HEART_BREAK = {(byte) 0xFF, (byte)0xEE, (byte)0xE1, 0x00, (byte) 0xFF};
    
    private FrontAndBackJoystickView frontAndBackJoystick; 
    private LeftAndRightJoystickView leftAndRightJoystick; 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        
        initSettings();
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//隐去标题（应用的名字必须要写在setContentView之前，否则会有异常）
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        buttonSetting = (FontAwesomeText) findViewById(R.id.buttonSetting);
        buttonSetting.setOnClickListener(buttonSettingClickListener);
        buttonSetting.setOnLongClickListener(buttonSettingClickListener2);
        
        buttonLen = (FontAwesomeText)findViewById(R.id.btnLen);
        buttonLen.setOnClickListener(buttonLenClickListener);
        buttonLen.setLongClickable(true);
        
        TakePicture = (FontAwesomeText)findViewById(R.id.ButtonTakePic);
        TakePicture.setOnClickListener(buttonTakePicClickListener);
        mAnimIndicator = (FontAwesomeText)findViewById(R.id.btnIndicator);
        
        backgroundView = (MjpegView)findViewById(R.id.mySurfaceView1); 
        
        frontAndBackJoystick = (FrontAndBackJoystickView)findViewById(R.id.frontAndBackJoystickView);
        leftAndRightJoystick = (LeftAndRightJoystickView)findViewById(R.id.leftAndRightJoystickView);
        
        frontAndBackJoystick.setOnFrontAndBackJoystickMoveListener(new OnFrontAndBackJoystickMoveListener() {
			public void onValueChanged(int power, int direction) {
        		frontAndBackDirection = direction;
        		frontAndBackPower = power;
        		if(frontAndBackPower != 0){
    				if (frontAndBackDirection == FrontAndBackJoystickView.FRONT) {
    					if(COMM_FORWARD != lastFrontAndBackCommand){
							sendCommand(COMM_FORWARD);
							lastFrontAndBackCommand = COMM_FORWARD;
    					}
    				}else if(frontAndBackDirection == FrontAndBackJoystickView.BACK){
    					if(COMM_BACKWARD != lastFrontAndBackCommand){
    						sendCommand(COMM_BACKWARD);
    						lastFrontAndBackCommand = COMM_BACKWARD;
    					}
    				}
        		}
			}
			public void OnReleased(){
				frontAndBackDirection = FrontAndBackJoystickView.ORIGIN;
				frontAndBackPower = 0;
                sendCommand(COMM_STOP);
			}
			public void OnReturnedToCenter(){}
		});
        leftAndRightJoystick.setOnLeftAndRightJoystickMoveListener(new OnLeftAndRightJoystickMoveListener() {

			public void onValueChanged(int power, int direction) {
        		leftAndRightDirection = direction;
        		leftAndRightPower = power;
        		if(leftAndRightPower != 0){
					if (LeftAndRightJoystickView.LEFT == leftAndRightDirection) {
    					if(COMM_LEFT != lastLeftAndRightCommand){
    						sendCommand(COMM_LEFT);
    						lastLeftAndRightCommand = COMM_LEFT;
    					}
					}else if(LeftAndRightJoystickView.RIGHT == leftAndRightDirection){
						if(COMM_RIGHT != lastLeftAndRightCommand){
							sendCommand(COMM_RIGHT);
							lastLeftAndRightCommand = COMM_RIGHT;
						}
					}
        		}
        	}
			public void OnReleased() {
				frontAndBackDirection = LeftAndRightJoystickView.ORIGIN;
				frontAndBackPower = 0;
                sendCommand(COMM_STOP);
			}

			public void OnReturnedToCenter() {}
        });
        
        mLogText = (TextView)findViewById(R.id.logTextView);
        if (null != mLogText) {
            mLogText.setBackgroundColor(Color.argb(0, 0, 255, 0));//0~255透明度值
            mLogText.setTextColor(Color.argb(90, 0, 255, 0));
        }
        
        mSeekBar = (SeekBar)findViewById(R.id.gear1);
        mSeekBar.setMax(MAX_GEAR_VALUE);
        mSeekBar.setProgress(INIT_GEAR_VALUE);
        mSeekBar.setOnSeekBarChangeListener(this);
        buttonLen.setKeepScreenOn(true);
        
        //connect  
        connectToRouter(m4test);
        
    }

    private OnClickListener buttonLenClickListener = new OnClickListener() {
        public void onClick(View arg0) {            
              if (bLenon) {
                  bLenon = false;
                  sendCommand(COMM_LEN_OFF);
                  buttonLen.setTextColor(Color.BLACK);
              } else  {
                  bLenon = true;
                  sendCommand(COMM_LEN_ON);
                  buttonLen.setTextColor(Color.YELLOW);
              }
            
        }
    };
    
    private OnClickListener buttonTakePicClickListener = new OnClickListener() {
    	public void onClick(View arg0) {       
            if (null != backgroundView) {
            	backgroundView.saveBitmap();
            }
        }
    };
    
    private OnClickListener buttonSettingClickListener = new OnClickListener() {
        public void onClick(View arg0) {       
            Intent setIntent = new Intent();
            setIntent.setClass(mContext, WifiCarSettings.class);
            startActivity(setIntent);
        }
    };
    
    private OnLongClickListener buttonSettingClickListener2 = new OnLongClickListener() {
        public boolean onLongClick(View arg0) {
            mThreadFlag = false;
            try {
                if (null != mThreadClient)
                    mThreadClient.join(); // wait for secondary to finish
            } catch (InterruptedException e) {
                mLogText.setText("关闭路由器监听进程失败。。。" +  e.getMessage());
            }
            
            connectToRouter(m4test);
            return false;
        }
    };
    
    private void selfcheck() {
        sendCommand(COMM_SELF_CHECK);
    }
    
    private void sendCommand(byte[] data) {
        if ( mWifiStatus != STATUS_CONNECTED || null == mtcpSocket) {
            mLogText.setText(R.string.STATE_ERROR +  data.toString());
            return;
        }
        
        if (!bReaddyToSendCmd) {
        	mLogText.setText("please wait 1 second to send msg ....");
        	return;
        }
        	
        try {
            mtcpSocket.sendMsg(data);
            //Toast.makeText(mContext, "发送成功", 1);
        } catch (Exception e) {
            Log.i("Socket", e.getMessage() != null ? e.getMessage().toString() : "sendCommand error!");
            //Toast.makeText(mContext, "发送消息给路由器失败  ：" + e.getMessage(),
            //        Toast.LENGTH_SHORT).show();
        }

    }
    
    private void handleCallback(byte[] command) {
        if (null == command || command.length != Constant.COMMAND_LENGTH) {
            return;
        }
        
        byte cmd1 = command[1];
        byte cmd2 = command[2];
        //byte cmd3 = command[3];
        
        if (command[0] != COMMAND_PERFIX || command[Constant.COMMAND_LENGTH-1] != COMMAND_PERFIX) {
        	return;	
        }
        
        if (cmd1 != 0x03) {
        	Log.i("Socket", "unknow command from router, ignor it! cmd1=" + cmd1);
        	return;
        }
        
        switch (cmd2) {
        case (byte)0x01:
            mLogText.setText("收到小车心跳包 ！");
        	handleHeartBreak();
        	break;
        case (byte)0x02:
            handleHeartBreak();
            break;
        default:
        	
            break;
        }
    }
    
    private int getWifiStatus () {
        int status = WIFI_STATE_UNKNOW;
        WifiManager mWifiMng = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        switch (mWifiMng.getWifiState()) {
        case WifiManager.WIFI_STATE_DISABLED:
        case WifiManager.WIFI_STATE_DISABLING:    
        case WifiManager.WIFI_STATE_ENABLING:
        case WifiManager.WIFI_STATE_UNKNOWN:
            status = WIFI_STATE_DISABLED;
            break;
        case WifiManager.WIFI_STATE_ENABLED:
            status = WIFI_STATE_NOT_CONNECTED;
            ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            State wifiState = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            if (State.CONNECTED == wifiState) {
                WifiInfo info = mWifiMng.getConnectionInfo();
                if (null != info) {
                    String bSSID = info.getBSSID();
                    String SSID = info.getSSID();
                    Log.i("Socket", "getWifiStatus bssid=" + bSSID + " ssid=" + SSID);
                    if (null != SSID && SSID.length() > 0) {
                        if (SSID.toLowerCase().contains(WIFI_SSID_PERFIX)) {
                            status = WIFI_STATE_CONNECTED;
                        }
                    }
                }
            }
            break;
        default:
            break;
        }
        return status;
    }
    
    private void connectToRouter(boolean isTest) {
        int status = getWifiStatus();
        if (WIFI_STATE_CONNECTED == status || isTest) {
            mThreadFlag = true;
            mThreadClient = new Thread(mRunnable);
            mThreadClient.start();
            String cameraUrl = null;
            if (m4test) {
            	cameraUrl = CAMERA_VIDEO_URL_TEST;
            } else {
            	cameraUrl = CAMERA_VIDEO_URL;
            }
            if (null != cameraUrl && cameraUrl.length() > 4) {
            	backgroundView.setSource(cameraUrl);//初始化Camera
            }
        } else if (WIFI_STATE_NOT_CONNECTED == status) {
            mLogText.setText("初始化连接路由器失败，wifi未连接，或者路由器状态异常！");
        } else {
            mLogText.setText("初始化连接路由器失败，wifi未开启，请手动开启后重试！");
        }
    }
    private void initWifiConnection() {
        mWifiStatus = STATUS_INIT;
        Log.i("Socket", "initWifiConnection");
        try {
            if (mtcpSocket != null) {
                mtcpSocket.closeSocket();
            }
            String clientUrl = ROUTER_CONTROL_URL;
            int clientPort = ROUTER_CONTROL_PORT;
            if (m4test) {
            	clientUrl = ROUTER_CONTROL_URL_TEST;
                clientPort = ROUTER_CONTROL_PORT_TEST;
            }
            mtcpSocket = new SocketClient(clientUrl, clientPort);
            Log.i("Socket", "Wifi Connect created ip=" + clientUrl
            		+ " port=" + clientPort);
            mWifiStatus = STATUS_CONNECTED;
        } catch (Exception e) {
            Log.d("Socket", "initWifiConnection return exception! ");
        }
        
        Message msg = new Message();
        if (mWifiStatus != STATUS_CONNECTED || null == mtcpSocket) {          
            msg.what = MSG_ID_ERR_CONN;
        } else {
            msg.what = MSG_ID_CON_SUCCESS;
        }
        
        mHandler.sendMessage(msg);
    }
    
    private int appendBuffer (byte[] buffer, int len, byte[] dstBuffer, int dstLen) {
    	int j = 0;
    	int i = dstLen;
    	for (i = dstLen; i < Constant.COMMAND_LENGTH && j < len; i++) {
    		dstBuffer[i] = buffer[j];
    		j++;
    	}
    	return i;
    }
    
    private Runnable mRunnable = new Runnable() 
    {
        public void run()
        {   
            BufferedInputStream is = null;
            try {                
                //连接服务器
                initWifiConnection();
                       
                //取得输入、输出流
                //mBufferedReaderClient = new BufferedReader(new InputStreamReader(mtcpSocket.getInputStream()));
                
                is = new BufferedInputStream(mtcpSocket.getInputStream());
            } catch (Exception e) {
                Message msg = new Message();
                msg.what = MSG_ID_ERR_INIT_READ;
                mHandler.sendMessage(msg);
                return;
            }            

            byte[] buffer = new byte[256];
            long lastTicket = System.currentTimeMillis();
            byte[] command = {0,0,0,0,0};
            int commandLength = 0;
            int i = 0;
            while (mThreadFlag)
            {
                try
                {
                    //if ( (recvMessageClient = mBufferedReaderClient.readLine()) != null )
                    //int ret = mBufferedReaderClient.read(buffer);
                    int ret = is.read(buffer);
                    if (ret > 0) {
                    	
	                    printRecBuffer ("receive buffer", buffer, ret);
	                    
	                    if(ret > 0 && ret <= Constant.COMMAND_LENGTH ) { 
	                    	long newTicket = System.currentTimeMillis();
	                    	long ticketInterval = newTicket - lastTicket;
	                    	Log.d("Socket", "time ticket interval =" + ticketInterval);
	                		
	                    	if (ticketInterval < Constant.MIN_COMMAND_REC_INTERVAL) {
	                    		if (commandLength > 0) {
	                    			commandLength = appendBuffer(buffer, ret, command, commandLength);
	                    		} else {
	                    			Log.d("Socket", "not recognized command-1");
	                    		}
	                    	} else {
	                    		if (buffer[0] == COMMAND_PERFIX ) {
	                    			for (i = 0; i < ret; i++) {
	                                    command[i] = buffer[i];
	                                }
	                    			commandLength = ret;
	                    		} else {
	                    			Log.d("Socket", "not recognized command-2");
	                    			commandLength = 0;
	                    		}
	                    	}
	                        
	                    	lastTicket = newTicket;
	                    	printRecBuffer ("print command", command, commandLength);
	                    	
	                    	if (commandLength >= Constant.COMMAND_LENGTH) {
	                    		Message msg = new Message();
	                            msg.what = MSG_ID_CON_READ;
	                            msg.obj = command;
	                            mHandler.sendMessage(msg);
	                            commandLength = 0;
	                    	} 
	                    }
                    }
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = MSG_ID_ERR_RECEIVE;
                    mHandler.sendMessage(msg);
                }
            }
        }
    };
    
    void printRecBuffer(String tag, byte[] buffer, int len) {
    	StringBuffer sb = new StringBuffer();
    	sb.append(tag);
    	sb.append(" len = ");
    	sb.append(len);
    	sb.append(" :");
    	for (int i =0 ;i < len; i++) {
    		sb.append(buffer[i]);
    		sb.append(", ");
    	}
    	Log.i("Socket", sb.toString());
    }
    
    Handler mHandler = new Handler()
    {                                        
          public void handleMessage(Message msg)                                        
          {  
              Log.i("Main", "handle internal Message, id=" + msg.what);
              
              switch (msg.what) {
              case MSG_ID_ERR_RECEIVE:
                  break;
              case MSG_ID_CON_READ:
                  byte[] command = (byte[])msg.obj;
                  //mLogText.setText("handle response from router: " + command.toString() );
                  handleCallback(command);
                  break;
              case MSG_ID_ERR_INIT_READ:
                  mLogText.setText("打开监听失败！！");
                  break;
              case MSG_ID_CON_SUCCESS:
                  mLogText.setText("成功连接到路由器!");
    
                  Message msgStartCheck = new Message();
                  msgStartCheck.what = MSG_ID_START_CHECK;
                  mHandler.sendMessageDelayed(msgStartCheck, 3000);
                  
                  Message msgHB1 = new Message();
                  msgHB1.what = MSG_ID_HEART_BREAK_RECEIVE;//启动心跳包检测循环
                  mHandler.sendMessage(msgHB1);
                  
                  Message msgHB2 = new Message();
                  msgHB2.what = MSG_ID_HEART_BREAK_SEND;//启动心跳包循环发送
                  //mHandler.sendMessage(msgHB2);
                  
                  break;
              case MSG_ID_START_CHECK:
                  mLogText.setText("开始进行自检，请稍等。。。。!!");
                  bReaddyToSendCmd = true;
                  //selfcheck();
                  break;
              case MSG_ID_ERR_CONN:
                  mLogText.setText("连接路由器失败!");
                  break;
              case MSG_ID_CLEAR_QUIT_FLAG:
                  mQuitFlag = false;
                  break;
              case MSG_ID_HEART_BREAK_RECEIVE:
                  if (mHeartBreakCounter == 0) {
                      bHeartBreakFlag = false;
                      
                  } else if (mHeartBreakCounter > 0) {
                      bHeartBreakFlag = true;
                  } else {
                      mLogText.setText("心跳包出现异常，已经忽略***");
                  }
                  Log.i("main", "handle MSG_ID_HEART_BREAK_RECEIVE :flag=" + bHeartBreakFlag);
                  
                  if (mLastCounter == 0 && mHeartBreakCounter > 0) {
                      startIconAnimation();
                  }
                  mLastCounter = mHeartBreakCounter;
                  mHeartBreakCounter = 0;
                  Message msgHB = new Message();
                  msgHB.what = MSG_ID_HEART_BREAK_RECEIVE;//启动心跳包检测循环
                  mHandler.sendMessageDelayed (msgHB, HEART_BREAK_CHECK_INTERVAL);
                  break;
              case MSG_ID_HEART_BREAK_SEND:
            	  Message msgSB = new Message();
                  msgSB.what = MSG_ID_HEART_BREAK_SEND;//循环向路由器发送心跳包
                  Log.i("main", "handle MSG_ID_HEART_BREAK_SEND");
                  
                  sendCommand(COMM_HEART_BREAK);
                  mHandler.sendMessageDelayed (msgSB, HEART_BREAK_SEND_INTERVAL);
            	  break;
              default :
                  break;
              }
              super.handleMessage(msg);            

          }                                    
     };
     
     private boolean isIconAnimationEnabled () {
         return bAnimationEnabled && bHeartBreakFlag;
     }
     private boolean mIconAnimationState = false;
     /** Icon animation handler for flashing warning alerts. */
     private final Handler mAnimationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mIconAnimationState) {
                if (isIconAnimationEnabled()) {
                    mAnimationHandler.sendEmptyMessageDelayed(0, WARNING_ICON_ON_DURATION_MSEC);
                }
            } else {
                if (isIconAnimationEnabled()) {
                    mAnimationHandler.sendEmptyMessageDelayed(0, WARNING_ICON_OFF_DURATION_MSEC);
                }
            }
            mIconAnimationState = !mIconAnimationState;
            mAnimIndicator.setTextColor(Color.GREEN);
        }
    };
    
    private void startIconAnimation() {
        Log.i("Animation", "startIconAnimation handler : " + mAnimationHandler);
        if (isIconAnimationEnabled())
            mAnimationHandler.sendEmptyMessageDelayed(0, WARNING_ICON_ON_DURATION_MSEC);
    }
    
    private void handleHeartBreak() {
        Log.i("Main", "handleHeartBreak");
        mHeartBreakCounter++;
        bHeartBreakFlag = true;
    }
    
    private void stopIconAnimation() {
        mAnimationHandler.removeMessages(0);
    }
    
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUserh) {
        
        if (Math.abs(progress - mSeekBarValue) > MIN_GEAR_STEP) {
            mLogText.setText("change angle: " + progress);
            mSeekBarValue = progress;
            COMM_GEAR_CONTROL[3] = (byte)progress;
            sendCommand(COMM_GEAR_CONTROL);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
      
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
   
    }
    public void onDestroy() {     
        if(null != mtcpSocket) {                
            try {
                mtcpSocket.closeSocket();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mThreadFlag = false;
            mThreadClient.interrupt();
        }
        
        if (null != mHandler) {
        	int i;
        	for (i = MSG_ID_LOOP_START + 1; i < MSG_ID_LOOP_END; i++ ) {
        		mHandler.removeMessages(i);
        	}
        }
        stopIconAnimation();
        super.onDestroy();
    }
    
    @Override
    protected void onResume() {
    	int status = getWifiStatus();
        if (WIFI_STATE_CONNECTED == status || m4test) {
            String cameraUrl = null;
            if (m4test) {
            	cameraUrl = CAMERA_VIDEO_URL_TEST;
            } else {
            	cameraUrl = CAMERA_VIDEO_URL;
            }
            if (null != cameraUrl && cameraUrl.length() > 4) {
            	backgroundView.setSource(cameraUrl);//初始化Camera
            }
        }
        super.onResume();
    }
    
    @Override
    public void onBackPressed() {
        if (mQuitFlag) {
            finish();
        } else {
            mQuitFlag = true;
            Toast.makeText(mContext, "请再次按返回键退出应用", Toast.LENGTH_LONG).show();
            Message msg = new Message();    
            msg.what = MSG_ID_CLEAR_QUIT_FLAG;
            mHandler.sendMessageDelayed(msg, QUIT_BUTTON_PRESS_INTERVAL);
        }
    }
    
    void initSettings () {
		 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		 
		 CAMERA_VIDEO_URL = settings.getString(Constant.PREF_KEY_CAMERA_URL, Constant.DEFAULT_VALUE_CAMERA_URL);
		 CAMERA_VIDEO_URL_TEST = settings.getString(Constant.PREF_KEY_CAMERA_URL_TEST, Constant.DEFAULT_VALUE_CAMERA_URL_TEST);
		 
		 String RouterUrl = settings.getString(Constant.PREF_KEY_ROUTER_URL, Constant.DEFAULT_VALUE_ROUTER_URL);
		 int index = RouterUrl.indexOf(":");
		 String routerPort = "";
		 if (index > 0) {
			 ROUTER_CONTROL_URL = RouterUrl.substring(0, index);
			 routerPort = RouterUrl.substring(index+1, RouterUrl.length() );
			 ROUTER_CONTROL_PORT = Integer.parseInt(routerPort);
		 }
		 
		 RouterUrl = settings.getString(Constant.PREF_KEY_ROUTER_URL_TEST, Constant.DEFAULT_VALUE_ROUTER_URL_TEST);
		 index = RouterUrl.indexOf(":");
		 if (index > 0) {
			 ROUTER_CONTROL_URL_TEST = RouterUrl.substring(0, index);
			 routerPort = RouterUrl.substring(index+1, RouterUrl.length() );
			 ROUTER_CONTROL_PORT_TEST = Integer.parseInt(routerPort);
		 }
		 
		 m4test =  settings.getBoolean(Constant.PREF_KEY_TEST_MODE_ENABLED, false);
		 
		 initLenControl(Constant.PREF_KEY_LEN_ON, Constant.DEFAULT_VALUE_LEN_ON);
		 initLenControl(Constant.PREF_KEY_LEN_OFF, Constant.DEFAULT_VALUE_LEN_OFF);
    }
    
    void initLenControl (String prefKey, String defaultValue) {
   	 	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

    	String comm = settings.getString(prefKey, defaultValue);
    	CommandArray cmd = new CommandArray(comm);
		if (cmd.isValid() ) {
			if (Constant.PREF_KEY_LEN_ON.equalsIgnoreCase(prefKey)) {
				COMM_LEN_ON[1] = cmd.mCmd1;
				COMM_LEN_ON[2] = cmd.mCmd2;
				COMM_LEN_ON[3] = cmd.mCmd3;
			} else if (Constant.PREF_KEY_LEN_OFF.equalsIgnoreCase(prefKey)) {
				COMM_LEN_OFF[1] = cmd.mCmd1;
				COMM_LEN_OFF[2] = cmd.mCmd2;
				COMM_LEN_OFF[3] = cmd.mCmd3;	
			} else {
				Log.i("Main", "unknow prefKey:" + prefKey); 
			}
		} else {
			Log.i("Main", "error format of command:" + comm); 
		}
    }
}
