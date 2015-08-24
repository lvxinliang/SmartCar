package com.hanry;
import java.io.BufferedInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import com.hanry.command.CategoryBit;
import com.hanry.command.Command;
import com.hanry.command.CommandBit;
import com.hanry.command.Utils;
import com.hanry.command.ValueBit;
import com.hanry.views.FrontAndBackJoystickView;
import com.hanry.views.FrontAndBackJoystickView.OnFrontAndBackJoystickMoveListener;
import com.hanry.views.LeftAndRightJoystickView;
import com.hanry.views.LeftAndRightJoystickView.OnLeftAndRightJoystickMoveListener;
/** Remove SeekBar
public class Main extends Activity implements SeekBar.OnSeekBarChangeListener{
*/
public class Main extends Activity implements SensorEventListener{
	protected SensorManager sensorManager;
    protected static final String TAG = "MainActivity";
	private static final String CAMERA_VIDEO_URL_SUFFIX = ":8080/?action=stream";
	private static final String CAMERA_VIDEO_URL_PREFIX = "http://";
	private static final int TRIGGER_VALUE = 12;
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
    
    private final byte COMMAND_PERFIX = -1;
    private final int HEART_BREAK_CHECK_INTERVAL = 8000;//ms
    private final int QUIT_BUTTON_PRESS_INTERVAL = 2500;//ms
    private final int HEART_BREAK_SEND_INTERVAL = 2500;//ms
    
    private String CAMERA_VIDEO_URL = "http://192.168.2.1:8080/?action=stream";
    private String ROUTER_CONTROL_URL = "192.168.2.1";
    private int ROUTER_CONTROL_PORT = 2001;
    private final String WIFI_SSID_PERFIX = "";  //SmartCar
    
    private FontAwesomeText enableGravityButton;
    private FontAwesomeText TakePicture;
    
    private FontAwesomeText mAnimIndicator;
    private boolean bAnimationEnabled = true;
    private boolean bReaddyToSendCmd = false;
    private TextView mLogText;
    
    /** Remove SeekBar
    private SeekBar mSeekBar;
    private int  mSeekBarValue = -1;
    */
    
    private FontAwesomeText buttonSetting;
    private FontAwesomeText buttonLen;
    private boolean bLenon = false;
    private boolean enableGravity = false;
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
	private int lastFrontAndBackCommand = FrontAndBackJoystickView.ORIGIN;
	private int lastLeftAndRightCommand = LeftAndRightJoystickView.ORIGIN;
	private Command lastCommand = null;
    
    private FrontAndBackJoystickView frontAndBackJoystick; 
    private LeftAndRightJoystickView leftAndRightJoystick;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        
        initSettings();
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//隐去标题（应用的名字必须要写在setContentView之前,否则会有异常）
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        buttonSetting = (FontAwesomeText) findViewById(R.id.buttonSetting);
        buttonSetting.setOnClickListener(buttonSettingClickListener);
        buttonSetting.setOnLongClickListener(buttonSettingClickListener2);
        
        buttonLen = (FontAwesomeText)findViewById(R.id.btnLen);
        buttonLen.setOnClickListener(buttonLenClickListener);
        buttonLen.setLongClickable(true);
        
        enableGravityButton = (FontAwesomeText)findViewById(R.id.ButtonGravity);
        TakePicture = (FontAwesomeText)findViewById(R.id.ButtonTakePic);
        TakePicture.setOnClickListener(buttonTakePicClickListener);
        enableGravityButton.setOnClickListener(new ButtonGravityClickListener(this));
        backgroundView = (MjpegView)findViewById(R.id.mySurfaceView1); 
        
        frontAndBackJoystick = (FrontAndBackJoystickView)findViewById(R.id.frontAndBackJoystickView);
        leftAndRightJoystick = (LeftAndRightJoystickView)findViewById(R.id.leftAndRightJoystickView);
        
        
        frontAndBackJoystick.setOnFrontAndBackJoystickMoveListener(new OnFrontAndBackJoystickMoveListener() {
			public void onValueChanged(int power, int direction) {
        		Command command = null;
        		if(power >= 3){
    				if (direction == FrontAndBackJoystickView.FRONT) {
    					if(lastLeftAndRightCommand == LeftAndRightJoystickView.LEFT){
    						command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.LEFT_FORWARD), new ValueBit());
    					}else if(lastLeftAndRightCommand == LeftAndRightJoystickView.RIGHT){
    						command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.RIGHT_FORWARD), new ValueBit());
    					}else{
    						command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.FORWARD), new ValueBit());
    					}
    					if(!command.equals(lastCommand)){
    						sendCommand(command.getBytes());
    						lastCommand = command;
    						lastFrontAndBackCommand = FrontAndBackJoystickView.FRONT;
    					}
    				}else if(direction == FrontAndBackJoystickView.BACK){
    					if(lastLeftAndRightCommand == LeftAndRightJoystickView.LEFT){
    						command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.LEFT_BACK), new ValueBit());
    					}else if(lastLeftAndRightCommand == LeftAndRightJoystickView.RIGHT){
    						command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.RIGHT_BACK), new ValueBit());
    					}else{
    						command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.BACK), new ValueBit());
    					}
    					if(!command.equals(lastCommand)){
    						sendCommand(command.getBytes());
    						lastCommand = command;
    						lastFrontAndBackCommand = FrontAndBackJoystickView.BACK;
    					}
    				}

        		}
			}
			public void OnReleased(){}
			public void OnReturnedToCenter(){
        		Command command = null;
				if(lastLeftAndRightCommand == LeftAndRightJoystickView.LEFT){
					command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.LEFT), new ValueBit());
				}else if(lastLeftAndRightCommand == LeftAndRightJoystickView.RIGHT){
					command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.RIGHT), new ValueBit());
				}else{
					command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.STOP), new ValueBit());
				}

				if(!command.equals(lastCommand)){
					sendCommand(command.getBytes());
					lastCommand = command;
					lastFrontAndBackCommand = FrontAndBackJoystickView.ORIGIN;
				}
			}
		});
        leftAndRightJoystick.setOnLeftAndRightJoystickMoveListener(new OnLeftAndRightJoystickMoveListener() {

			public void onValueChanged(int power, int direction) {
        		Command command = null;
        		if(power >= 3){
					if (LeftAndRightJoystickView.LEFT == direction) {
    					if(lastFrontAndBackCommand == FrontAndBackJoystickView.FRONT){
    						command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.LEFT_FORWARD), new ValueBit());
    					}else if(lastFrontAndBackCommand == FrontAndBackJoystickView.BACK){
    						command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.LEFT_BACK), new ValueBit());
    					}else{
    						command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.LEFT), new ValueBit());
    					}
    					if(!command.equals(lastCommand)){
    						sendCommand(command.getBytes());
    						lastCommand = command;
    						lastLeftAndRightCommand = LeftAndRightJoystickView.LEFT;
    					}
					}else if(LeftAndRightJoystickView.RIGHT == direction){
    					if(lastFrontAndBackCommand == FrontAndBackJoystickView.FRONT){
    						command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.RIGHT_FORWARD), new ValueBit());
    					}else if(lastFrontAndBackCommand == FrontAndBackJoystickView.BACK){
    						command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.RIGHT_BACK), new ValueBit());
    					}else{
    						command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.RIGHT), new ValueBit());
    					}
    					if(!command.equals(lastCommand)){
    						sendCommand(command.getBytes());
    						lastCommand = command;
    						lastLeftAndRightCommand = LeftAndRightJoystickView.RIGHT;
    					}
					}
        		}
        	}
			public void OnReleased() {}

			public void OnReturnedToCenter() {
        		Command command = null;
				if(lastFrontAndBackCommand == FrontAndBackJoystickView.FRONT){
					command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.FORWARD), new ValueBit());
				}else if(lastFrontAndBackCommand == FrontAndBackJoystickView.BACK){
					command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.BACK), new ValueBit());
				}else{
					command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.STOP), new ValueBit());
				}

				if(!command.equals(lastCommand)){
					sendCommand(command.getBytes());
					lastCommand = command;
					lastLeftAndRightCommand = LeftAndRightJoystickView.ORIGIN;
				}
			}
        });
        
        mLogText = (TextView)findViewById(R.id.logTextView);
        if (null != mLogText) {
            mLogText.setBackgroundColor(Color.argb(0, 0, 255, 0));//0~255透明度值
            mLogText.setTextColor(Color.argb(90, 0, 0, 0));
        }
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //connect  
        connectToRouter();
    }

    public FontAwesomeText getEnableGravityButton() {
		return enableGravityButton;
	}

	public void setEnableGravityButton(FontAwesomeText enableGravityButton) {
		this.enableGravityButton = enableGravityButton;
	}

	public boolean getEnableGravity() {
		return enableGravity;
	}

	public void setEnableGravity(boolean enableGravity) {
		this.enableGravity = enableGravity;
	}

	private OnClickListener buttonLenClickListener = new OnClickListener() {
        public void onClick(View arg0) {            
              if (bLenon) {
                  bLenon = false;
                  sendCommand(new Command(new CategoryBit(CategoryBit.LIGHT), new CommandBit(CommandBit.LIGHT0), new ValueBit(0, 0)).getBytes());
                  buttonLen.setTextColor(Color.BLACK);
              } else  {
                  bLenon = true;
                  sendCommand(new Command(new CategoryBit(CategoryBit.LIGHT), new CommandBit(CommandBit.LIGHT0), new ValueBit(0xff, 0)).getBytes());
                  buttonLen.setTextColor(Color.YELLOW);
              }
            
        }
    };
    
    protected void toHideComponent() {
    	this.frontAndBackJoystick.setVisibility(View.GONE);
    	this.leftAndRightJoystick.setVisibility(View.GONE);
    }
    
    protected void toShowComponent() {
    	this.frontAndBackJoystick.setVisibility(View.VISIBLE);
    	this.leftAndRightJoystick.setVisibility(View.VISIBLE);
    }
    
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
                mLogText.setText("关闭小车监听进程失败");
                Log.e("OnLongClickListener", e.getMessage());
            }
            
            connectToRouter();
            return false;
        }
    };
    
    private void selfcheck() {
        sendCommand(new Command(new CategoryBit(CategoryBit.CHECK), new CommandBit(CommandBit.SELF_CHECK), new ValueBit(0, 0)).getBytes());
    }
    
    protected void sendCommand(byte[] data) {
    	Log.i("sendCommand", Utils.castBytesToHexString(data));
        if ( mWifiStatus != STATUS_CONNECTED || null == mtcpSocket) {
            return;
        }
        
        if (!bReaddyToSendCmd) {
        	mLogText.setText("自检中,请稍后…");
        	return;
        }else{
        	mLogText.setText("");
        }
        	
        try {
            mtcpSocket.sendMsg(data);
            //Toast.makeText(mContext, "发送成功", 1);
        } catch (Exception e) {
            Log.i("Socket", e.getMessage() != null ? e.getMessage().toString() : "sendCommand error!");
            //Toast.makeText(mContext, "发送消息给小车失败  ：" + e.getMessage(),
            //        Toast.LENGTH_SHORT).show();
        }

    }
    
    private void handleCallback(byte[] command) {
        if (null == command || command.length != Command.LENGTH) {
            return;
        }
        
        byte cmd1 = command[1];
        byte cmd2 = command[2];
        //byte cmd3 = command[3];
        
        if (command[0] != COMMAND_PERFIX || command[Command.LENGTH-1] != COMMAND_PERFIX) {
        	return;	
        }
        
        if (cmd1 != 0x03) {
        	Log.i("Socket", "unknow command from router, ignor it! cmd1=" + cmd1);
        	return;
        }
        
        switch (cmd2) {
        case (byte)0x01:
            mLogText.setText("收到小车心跳包!");
        	handleHeartBreak();
        	break;
        case (byte)0x02:
            handleHeartBreak();
            break;
        default:
        	
            break;
        }
    }
    
	private boolean isEffectiveWiFi(String wifiName){
        return wifiName.toLowerCase().contains(WIFI_SSID_PERFIX.toLowerCase());
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
                    	if(isEffectiveWiFi(SSID))
                            status = WIFI_STATE_CONNECTED;
                    }
                }
            }
            break;
        default:
            break;
        }
        return status;
    }
    
    private void connectToRouter() {
        int status = getWifiStatus();
        if (WIFI_STATE_CONNECTED == status) {
            mThreadFlag = true;
            mThreadClient = new Thread(mRunnable);
            mThreadClient.start();
        } else if (WIFI_STATE_NOT_CONNECTED == status) {
            mLogText.setText("wifi未连接,或者小车状态异常!");
        } else {
            mLogText.setText("wifi未开启,请手动开启后重试!");
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
    	for (i = dstLen; i < Command.LENGTH && j < len; i++) {
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
	                    
	                    if(ret > 0 && ret <= Command.LENGTH ) { 
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
	                    	
	                    	if (commandLength >= Command.LENGTH) {
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
                  handleCallback(command);
                  break;
              case MSG_ID_ERR_INIT_READ:
                  mLogText.setText("打开监听失败!");
                  break;
              case MSG_ID_CON_SUCCESS:
                  mLogText.setText("成功连接到小车!");
    
                  Message msgStartCheck = new Message();
                  msgStartCheck.what = MSG_ID_START_CHECK;
                  mHandler.sendMessageDelayed(msgStartCheck, 3000);
                  
                  Message msgHB1 = new Message();
                  msgHB1.what = MSG_ID_HEART_BREAK_RECEIVE;//启动心跳包检测循环
                  mHandler.sendMessage(msgHB1);
                  
                  Message msgHB2 = new Message();
                  msgHB2.what = MSG_ID_HEART_BREAK_SEND;//启动心跳包循环发送
                  mHandler.sendMessage(msgHB2);
                  
                  break;
              case MSG_ID_START_CHECK:
                  bReaddyToSendCmd = true;
                  selfcheck();
                  break;
              case MSG_ID_ERR_CONN:
                  mLogText.setText("连接小车失败!");
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
                      mLogText.setText("心跳包出现异常,已经忽略");
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
                  msgSB.what = MSG_ID_HEART_BREAK_SEND;//循环向小车发送心跳包
                  Log.i("main", "handle MSG_ID_HEART_BREAK_SEND");
                  
                  sendCommand(new Command(new CategoryBit(CategoryBit.CHECK), new CommandBit(CommandBit.HEART_BREAK), new ValueBit(0, 0)).getBytes());
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
            mAnimIndicator.setTextColor(Color.BLACK);
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
    
	public void onPause() {
		backgroundView.stopPlayback();
		super.onPause();
	}
	public void onStop() {
		if(enableGravity){
			sensorManager.unregisterListener(this);
		}
		super.onStop();
	}
	
    @Override
    protected void onResume() {
    	super.onResume();
    	int status = getWifiStatus();
        if (WIFI_STATE_CONNECTED == status) {
            String cameraUrl = null;
            cameraUrl = CAMERA_VIDEO_URL;
            if (null != cameraUrl && cameraUrl.length() > 4) {
            	backgroundView.setSource(cameraUrl);//初始化Camera
            }
        }
        backgroundView.resumePlayback();
        if(enableGravity){
        	this.sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        }
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
		 
		 
		 String RouterUrl = settings.getString(Constant.PREF_KEY_ROUTER_URL, Constant.DEFAULT_VALUE_ROUTER_URL);
		 int index = RouterUrl.indexOf(":");
		 String routerPort = "";
		 if (index > 0) {
			 ROUTER_CONTROL_URL = RouterUrl.substring(0, index);
			 routerPort = RouterUrl.substring(index+1, RouterUrl.length() );
			 ROUTER_CONTROL_PORT = Integer.parseInt(routerPort);
		 }
		 
		 if(ROUTER_CONTROL_URL != null && !"".equals(ROUTER_CONTROL_URL)){
			 CAMERA_VIDEO_URL = CAMERA_VIDEO_URL_PREFIX + ROUTER_CONTROL_URL + CAMERA_VIDEO_URL_SUFFIX;
		 }
		 
    }

    /**
     * 传感器精度发生改变时的回调方法
     */
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	/**
	 * 当传感器值发生改变时的回调方法
	 */
	public void onSensorChanged(SensorEvent event) {
		
		float[] values = event.values;
		float xValue = values[1];
		float yValue = values[2];
		StringBuilder sb = new StringBuilder();
		sb.append("Z轴转过的角度:");
		sb.append(values[0]);
		sb.append("\nX轴转过的角度:");
		sb.append(values[1]);
		sb.append("\nY轴转过的角度:");
		sb.append(values[2]);
		this.mLogText.setText(sb.toString());
		
		if(yValue < -TRIGGER_VALUE){
			if(xValue > TRIGGER_VALUE){
				//left_forward
				sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.LEFT_FORWARD), new ValueBit()).getBytes());
				
			}else if(xValue < -TRIGGER_VALUE){
				//right_forward
				sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.RIGHT_FORWARD), new ValueBit()).getBytes());
			}else{
				//forward
				sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.FORWARD), new ValueBit()).getBytes());
				
			}
		}else if(yValue > TRIGGER_VALUE){
			if(xValue > TRIGGER_VALUE){
				sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.LEFT_BACK), new ValueBit()).getBytes());
				//left_back
				
			}else if(xValue < -TRIGGER_VALUE){
				sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.RIGHT_BACK), new ValueBit()).getBytes());
				//right_back
			}else{
				//back
				sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.BACK), new ValueBit()).getBytes());
				
			}
		}
		if(xValue < -TRIGGER_VALUE){
			if(yValue < -TRIGGER_VALUE){
				//right_forward
				sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.RIGHT_FORWARD), new ValueBit()).getBytes());
				
			}else if(yValue > TRIGGER_VALUE){
				//right_back
				sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.RIGHT_BACK), new ValueBit()).getBytes());
			}else{
				//right
				sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.RIGHT), new ValueBit()).getBytes());
			}
		}else if(xValue > TRIGGER_VALUE){
			if(yValue < -TRIGGER_VALUE){
				//left_forward
				sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.LEFT_FORWARD), new ValueBit()).getBytes());
				
			}else if(yValue > TRIGGER_VALUE){
				//left_back
				sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.LEFT_BACK), new ValueBit()).getBytes());
			}else{
				//left
				sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.LEFT), new ValueBit()).getBytes());
			}
		}
		
		if(xValue > -TRIGGER_VALUE && xValue < TRIGGER_VALUE && yValue > -TRIGGER_VALUE && yValue < TRIGGER_VALUE){
			//stop
			sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.STOP), new ValueBit()).getBytes());
		}
	}
}
