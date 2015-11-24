package com.hanry;

import java.io.BufferedInputStream;
import java.io.IOException;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.FontAwesomeText;
import com.hanry.command.CategoryBit;
import com.hanry.command.Command;
import com.hanry.command.CommandBit;
import com.hanry.command.ValueBit;
import com.hanry.component.DirectionSensorEventListener;
import com.hanry.component.FrontAndBackJoystickView;
import com.hanry.component.GravityButtonClickListener;
import com.hanry.component.LeftAndRightJoystickView;
import com.hanry.component.OnFrontAndBackJoystickMoveListener;
import com.hanry.component.OnLeftAndRightJoystickMoveListener;

public class Main extends Activity implements CommandSender{
	protected static final String TAG = "MainActivity";
	private static final String CAMERA_VIDEO_URL_SUFFIX = ":8080/?action=stream";
	private static final String CAMERA_VIDEO_URL_PREFIX = "http://";
	private final int MSG_ID_ERR_CONN = 1001;
	private final int MSG_ID_ERR_RECEIVE = 1003;
	private final int MSG_ID_CON_READ = 1004;
	private final int MSG_ID_CON_SUCCESS = 1005;
	private final int MSG_ID_START_CHECK = 1006;
	private final int MSG_ID_ERR_INIT_READ = 1007;
	private final int MSG_ID_LOOP_START = 1010;
	private final int MSG_ID_HEART_BREAK_RECEIVE = 1011;
	private final int MSG_ID_HEART_BREAK_SEND = 1012;
	private final int MSG_ID_LOOP_END = 1013;

	private final int WARNING_ICON_OFF_DURATION_MSEC = 600;
	private final int WARNING_ICON_ON_DURATION_MSEC = 800;
	private final int HEART_BREAK_CHECK_INTERVAL = 8000;// ms
	private final int HEART_BREAK_SEND_INTERVAL = 2500;// ms

	private String CAMERA_VIDEO_URL = "http://192.168.2.1:8080/?action=stream";
	private String ROUTER_CONTROL_URL = "192.168.2.1";
	private String WIFI_SSID_PERFIX = "Singular_Wifi-Car"; // SmartCar
	private int ROUTER_CONTROL_PORT = 2001;

	private FontAwesomeText enableGravityButton;
	private FontAwesomeText TakePicture;
	private FontAwesomeText buttonBack;
	private FontAwesomeText buttonLen;
	private TextView notifyText;
	private FrontAndBackJoystickView frontAndBackJoystick;
	private LeftAndRightJoystickView leftAndRightJoystick;

	private ConnectionState connectionState = ConnectionState.STATUS_INIT;
	private boolean animationEnabled = true;
	private boolean isReaddyToSendCmd = false;
	private boolean isLightOn = false;
	private boolean enableGravity = false;
	private boolean haveReceivedHeartBreak = false;
	private int recvHeartBreakCounter = 0;
	private int lastrecvHeartBreakCounter = 0;
	
	private Context appContext;
	private Thread recvMsgThread = null;
	private boolean isEnableRecvMsgThread = false;
	private SocketClient tcpSocket;
	private MjpegView backgroundView = null;
	private DirectionSensorEventListener directionSensorEventListener = null;
	private int lastFrontAndBackCommand = FrontAndBackJoystickView.ORIGIN;
	private int lastLeftAndRightCommand = LeftAndRightJoystickView.ORIGIN;
	private Command lastCommand = null;
	private FontAwesomeText cameraResetButton;
	private ValueBit directionValueBit = new ValueBit(ValueBit.SPEED0, ValueBit.ANGLE0);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appContext = this;

		initSettings();
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐去标题（应用的名字必须要写在setContentView之前,否则会有异常）
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		buttonBack = (FontAwesomeText) findViewById(R.id.buttonBack);
		buttonBack.setOnClickListener(buttonBackClickListener);

		buttonLen = (FontAwesomeText) findViewById(R.id.btnLen);
		buttonLen.setOnClickListener(buttonLenClickListener);
		buttonLen.setLongClickable(true);

		TakePicture = (FontAwesomeText) findViewById(R.id.ButtonTakePic);
		TakePicture.setOnClickListener(buttonTakePicClickListener);
		
		cameraResetButton = (FontAwesomeText) findViewById(R.id.ButtonCameraReset);
		cameraResetButton.setOnClickListener(cameraResetButtonClickListener);
		
		enableGravityButton = (FontAwesomeText) findViewById(R.id.ButtonGravity);
		enableGravityButton.setOnClickListener(new GravityButtonClickListener(
				this));
		backgroundView = (MjpegView) findViewById(R.id.mySurfaceView1);
		backgroundView.setCommandSender(this);
		frontAndBackJoystick = (FrontAndBackJoystickView) findViewById(R.id.frontAndBackJoystickView);
		leftAndRightJoystick = (LeftAndRightJoystickView) findViewById(R.id.leftAndRightJoystickView);

		frontAndBackJoystick
				.setOnFrontAndBackJoystickMoveListener(new OnFrontAndBackJoystickMoveListener() {
					public void onValueChanged(int power, int direction) {
						Command command = null;
						if (power >= 3) {
							directionValueBit.setHighByte(power);
							//System.out.println("frontAndBackJoystick "+ power);
							if (direction == FrontAndBackJoystickView.FRONT) {
								if (lastLeftAndRightCommand == LeftAndRightJoystickView.LEFT) {
									command = new Command(new CategoryBit(
											CategoryBit.DIRECTION),
											new CommandBit(
													CommandBit.LEFT_FORWARD),
											directionValueBit);
								} else if (lastLeftAndRightCommand == LeftAndRightJoystickView.RIGHT) {
									command = new Command(new CategoryBit(
											CategoryBit.DIRECTION),
											new CommandBit(
													CommandBit.RIGHT_FORWARD),
											directionValueBit);
								} else {
									command = new Command(new CategoryBit(
											CategoryBit.DIRECTION),
											new CommandBit(CommandBit.FORWARD),
											directionValueBit);
								}
								sendCommand(command);
								lastFrontAndBackCommand = FrontAndBackJoystickView.FRONT;
							} else if (direction == FrontAndBackJoystickView.BACK) {
								if (lastLeftAndRightCommand == LeftAndRightJoystickView.LEFT) {
									command = new Command(
											new CategoryBit(
													CategoryBit.DIRECTION),
											new CommandBit(CommandBit.LEFT_BACK),
											directionValueBit);
								} else if (lastLeftAndRightCommand == LeftAndRightJoystickView.RIGHT) {
									command = new Command(new CategoryBit(
											CategoryBit.DIRECTION),
											new CommandBit(
													CommandBit.RIGHT_BACK),
											directionValueBit);
								} else {
									command = new Command(new CategoryBit(
											CategoryBit.DIRECTION),
											new CommandBit(CommandBit.BACK),
											directionValueBit);
								}
							sendCommand(command);
							lastFrontAndBackCommand = FrontAndBackJoystickView.BACK;
							}

						}
					}

					public void OnReleased() {
					}

					public void OnReturnedToCenter() {
						Command command = null;
						if (lastLeftAndRightCommand == LeftAndRightJoystickView.LEFT) {
							command = new Command(new CategoryBit(
									CategoryBit.DIRECTION), new CommandBit(
									CommandBit.LEFT), directionValueBit);
						} else if (lastLeftAndRightCommand == LeftAndRightJoystickView.RIGHT) {
							command = new Command(new CategoryBit(
									CategoryBit.DIRECTION), new CommandBit(
									CommandBit.RIGHT), directionValueBit);
						} else {
							command = new Command(new CategoryBit(
									CategoryBit.DIRECTION), new CommandBit(
									CommandBit.STOP), directionValueBit);
						}

						sendCommand(command);
						lastFrontAndBackCommand = FrontAndBackJoystickView.ORIGIN;
					}
				});
		leftAndRightJoystick
				.setOnLeftAndRightJoystickMoveListener(new OnLeftAndRightJoystickMoveListener() {

					public void onValueChanged(int power, int direction) {
						Command command = null;
						if (power >= 3) {
							directionValueBit.setLowByte(power);
							//System.out.println("leftAndRightJoystick "+ power);
							if (LeftAndRightJoystickView.LEFT == direction) {
								if (lastFrontAndBackCommand == FrontAndBackJoystickView.FRONT) {
									command = new Command(new CategoryBit(
											CategoryBit.DIRECTION),
											new CommandBit(
													CommandBit.LEFT_FORWARD),
											directionValueBit);
								} else if (lastFrontAndBackCommand == FrontAndBackJoystickView.BACK) {
									command = new Command(
											new CategoryBit(
													CategoryBit.DIRECTION),
											new CommandBit(CommandBit.LEFT_BACK),
											directionValueBit);
								} else {
									command = new Command(new CategoryBit(
											CategoryBit.DIRECTION),
											new CommandBit(CommandBit.LEFT),
											directionValueBit);
								}
								sendCommand(command);
								lastLeftAndRightCommand = LeftAndRightJoystickView.LEFT;
							} else if (LeftAndRightJoystickView.RIGHT == direction) {
								if (lastFrontAndBackCommand == FrontAndBackJoystickView.FRONT) {
									command = new Command(new CategoryBit(
											CategoryBit.DIRECTION),
											new CommandBit(
													CommandBit.RIGHT_FORWARD),
											directionValueBit);
								} else if (lastFrontAndBackCommand == FrontAndBackJoystickView.BACK) {
									command = new Command(new CategoryBit(
											CategoryBit.DIRECTION),
											new CommandBit(
													CommandBit.RIGHT_BACK),
											directionValueBit);
								} else {
									command = new Command(new CategoryBit(
											CategoryBit.DIRECTION),
											new CommandBit(CommandBit.RIGHT),
											directionValueBit);
								}
								sendCommand(command);
								lastLeftAndRightCommand = LeftAndRightJoystickView.RIGHT;
							}
						}
					}

					public void OnReleased() {
					}

					public void OnReturnedToCenter() {
						Command command = null;
						if (lastFrontAndBackCommand == FrontAndBackJoystickView.FRONT) {
							command = new Command(new CategoryBit(
									CategoryBit.DIRECTION), new CommandBit(
									CommandBit.FORWARD), directionValueBit);
						} else if (lastFrontAndBackCommand == FrontAndBackJoystickView.BACK) {
							command = new Command(new CategoryBit(
									CategoryBit.DIRECTION), new CommandBit(
									CommandBit.BACK), directionValueBit);
						} else {
							command = new Command(new CategoryBit(
									CategoryBit.DIRECTION), new CommandBit(
									CommandBit.STOP), directionValueBit);
						}

						sendCommand(command);
						lastLeftAndRightCommand = LeftAndRightJoystickView.ORIGIN;
					}
				});

		notifyText = (TextView) findViewById(R.id.logTextView);
		if (null != notifyText) {
			notifyText.setBackgroundColor(Color.argb(0, 0, 0, 0));// 0~255透明度值
			notifyText.setTextColor(Color.argb(90, 0, 0, 0));
		}
		directionSensorEventListener = new DirectionSensorEventListener(this);
		// connect
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

	public DirectionSensorEventListener getDirectionSensorEventListener() {
		return directionSensorEventListener;
	}

	public void toHideComponent() {
		this.frontAndBackJoystick.setVisibility(View.GONE);
		this.leftAndRightJoystick.setVisibility(View.GONE);
	}

	public void toShowComponent() {
		this.frontAndBackJoystick.setVisibility(View.VISIBLE);
		this.leftAndRightJoystick.setVisibility(View.VISIBLE);
	}
	
	private OnClickListener buttonLenClickListener = new OnClickListener() {
		public void onClick(View arg0) {
			if (isLightOn) {
				isLightOn = false;
				sendCommand(new Command(new CategoryBit(CategoryBit.LIGHT),
						new CommandBit(CommandBit.LIGHT0), new ValueBit(ValueBit.TURNOFF, 0)));
				buttonLen.setTextColor(Color.BLACK);
			} else {
				isLightOn = true;
				sendCommand(new Command(new CategoryBit(CategoryBit.LIGHT),
						new CommandBit(CommandBit.LIGHT0),
						new ValueBit(ValueBit.TURNON, 0)));
				buttonLen.setTextColor(Color.YELLOW);
			}

		}
	};
	
	private OnClickListener cameraResetButtonClickListener = new OnClickListener() {
		public void onClick(View arg0) {
			backgroundView.doCameraReset();
		}
	};

	private OnClickListener buttonTakePicClickListener = new OnClickListener() {
		public void onClick(View arg0) {
			if (null != backgroundView) {
				backgroundView.saveBitmap();
			}
		}
	};

	private OnClickListener buttonBackClickListener = new OnClickListener() {
		public void onClick(View arg0) {
			Intent setIntent = new Intent();
			setIntent.setClass(appContext, MenuActivity.class);
			startActivity(setIntent);
		}
	};

	private void selfcheck() {
		sendCommand(new Command(new CategoryBit(CategoryBit.CHECK),
				new CommandBit(CommandBit.SELF_CHECK), new ValueBit(0, 0)));
	}

	public void sendCommand(Command cmd) {
		if (cmd.equals(lastCommand)) {
			return;
		}
		if (connectionState != ConnectionState.STATUS_CONNECTED || null == tcpSocket) {
			return;
		}

		if (!isReaddyToSendCmd) {
			notifyText.setText("自检中,请稍后…");
			return;
		} else {
			notifyText.setText("");
		}

		try {
			tcpSocket.sendMsg(cmd.getBytes());
			Log.i("sendCommand", Utils.castBytesToHexString(cmd.getBytes()));
			// Toast.makeText(mContext, "发送成功", 1);
		} catch (Exception e) {
			Log.i("Socket", e.getMessage() != null ? e.getMessage().toString()
					: "sendCommand error!");
			// Toast.makeText(mContext, "发送消息给小车失败  ：" + e.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}
		lastCommand = cmd;

	}

	private void handleCallback(byte[] command) {
		if (null == command || command.length != Command.LENGTH) {
			return;
		}

		byte cmd1 = command[1];
		byte cmd2 = command[2];

		if (command[0] != Command.STARTBIT
				|| command[Command.LENGTH - 1] != Command.ENDBIT) {
			return;
		}

		if(cmd1 == CategoryBit.CHECK && cmd2 == CommandBit.HEART_BREAK){
			handleHeartBreak();
		}else{
			handleRecvMessage();
		}
	}

	private void handleRecvMessage() {
		// TODO Auto-generated method stub
		
	}

	private boolean isEffectiveWiFi(String wifiName) {
		return wifiName.toLowerCase().contains(WIFI_SSID_PERFIX.toLowerCase());
//		int status = -1;
//		// TODO 通过ping判读是否可以获取小车的视屏流
//		if (wifiName.toLowerCase().contains(WIFI_SSID_PERFIX.toLowerCase())) {
//			// m_strForNetAddress是输入的网址或者Ip地址
//			Process p;
//			try {
//				p = Runtime.getRuntime()
//						.exec("ping -c 1 " + ROUTER_CONTROL_URL);
//				// status 只能获取是否成功，无法获取更多的信息
//				status = p.waitFor();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
//			if (status == 0)
//				return true;
//		}
//		return false;
	}

	private ConnectionState getWifiStatus() {
		ConnectionState status = ConnectionState.WIFI_STATE_UNKNOW;
		WifiManager mWifiMng = (WifiManager) appContext
				.getSystemService(Context.WIFI_SERVICE);
		switch (mWifiMng.getWifiState()) {
		case WifiManager.WIFI_STATE_DISABLED:
		case WifiManager.WIFI_STATE_DISABLING:
		case WifiManager.WIFI_STATE_ENABLING:
		case WifiManager.WIFI_STATE_UNKNOWN:
			status = ConnectionState.WIFI_STATE_DISABLED;
			break;
		case WifiManager.WIFI_STATE_ENABLED:
			status = ConnectionState.WIFI_STATE_NOT_CONNECTED;
			ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			State wifiState = conMan.getNetworkInfo(
					ConnectivityManager.TYPE_WIFI).getState();
			if (State.CONNECTED == wifiState) {
				WifiInfo info = mWifiMng.getConnectionInfo();
				if (null != info) {
					String bSSID = info.getBSSID();
					String SSID = info.getSSID();
					Log.i("Socket", "getWifiStatus bssid=" + bSSID + " ssid="
							+ SSID);
					if (null != SSID && SSID.length() > 0) {
						if (isEffectiveWiFi(SSID))
							status = ConnectionState.WIFI_STATE_CONNECTED;
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
		ConnectionState status = getWifiStatus();
		if (ConnectionState.WIFI_STATE_CONNECTED == status) {
			isEnableRecvMsgThread = true;
			recvMsgThread = new Thread(recvMsgRunnable);
			recvMsgThread.start();
		} else if (ConnectionState.WIFI_STATE_NOT_CONNECTED == status) {
			notifyText.setText("wifi未连接,或者小车状态异常!");
		} else {
			notifyText.setText("wifi未开启,请手动开启后重试!");
		}
	}

	private void initWifiConnection() {
		connectionState = ConnectionState.STATUS_INIT;
		Log.i("Socket", "initWifiConnection");
		try {
			if (tcpSocket != null) {
				tcpSocket.closeSocket();
			}
			String clientUrl = ROUTER_CONTROL_URL;
			int clientPort = ROUTER_CONTROL_PORT;
			tcpSocket = new SocketClient(clientUrl, clientPort);
			Log.i("Socket", "Wifi Connect created ip=" + clientUrl + " port="
					+ clientPort);
			connectionState = ConnectionState.STATUS_CONNECTED;
		} catch (Exception e) {
			Log.d("Socket", "initWifiConnection return exception! ");
		}

		Message msg = new Message();
		if (connectionState != ConnectionState.STATUS_CONNECTED || null == tcpSocket) {
			msg.what = MSG_ID_ERR_CONN;
		} else {
			msg.what = MSG_ID_CON_SUCCESS;
		}

		mHandler.sendMessage(msg);
	}

	private Runnable recvMsgRunnable = new Runnable() {
		public void run() {
			byte[] buffer = new byte[256];
			long lastTicket = System.currentTimeMillis();
			byte[] command = { 0, 0, 0, 0, 0 ,0};
			int commandLength = 0;
			int i = 0;
			int ret = 0;
			BufferedInputStream is = null;
			try {
				// 与小车建立socket连接，获取小车心跳包
				initWifiConnection();
				is = new BufferedInputStream(tcpSocket.getInputStream());
			} catch (Exception e) {
				Message msg = new Message();
				msg.what = MSG_ID_ERR_INIT_READ;
				mHandler.sendMessage(msg);
				return;
			}

			while (isEnableRecvMsgThread) {
				try {
					ret = is.read(buffer);
					if (ret > 0) {

						printRecBuffer("receive buffer", buffer, ret);

						if (ret > 0 && ret <= Command.LENGTH) {
							long newTicket = System.currentTimeMillis();
							long ticketInterval = newTicket - lastTicket;
							Log.d("Socket", "time ticket interval ="
									+ ticketInterval);

							if (ticketInterval < Constant.MIN_COMMAND_REC_INTERVAL) {
								if (buffer[0] == Command.STARTBIT) {
									for (i = 0; i < ret; i++) {
										command[i] = buffer[i];
									}
									commandLength = ret;
								} else {
									//接收到了不能识别的数据包
									Log.d("Socket", "not recognized command-1");
									commandLength = 0;
								}
							} else {
								//心跳包接收超时
							}

							lastTicket = newTicket;
							printRecBuffer("print command", command,
									commandLength);

							if (commandLength >= Command.LENGTH) {
								Message msg = new Message();
								msg.what = MSG_ID_CON_READ;
								msg.obj = command;
								mHandler.sendMessage(msg);
								commandLength = 0;
							}
						}
					}
				} catch (IOException e) {
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
		for (int i = 0; i < len; i++) {
			sb.append(buffer[i]);
			sb.append(", ");
		}
		Log.i("Socket", sb.toString());
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.i("Main", "handle internal Message, id=" + msg.what);

			switch (msg.what) {
			case MSG_ID_ERR_RECEIVE:
				isEnableRecvMsgThread = false;
				notifyText.setText("数据接收错误!");
				break;
			case MSG_ID_CON_READ:
				byte[] command = (byte[]) msg.obj;
				handleCallback(command);
				break;
			case MSG_ID_ERR_INIT_READ:
				notifyText.setText("打开监听失败!");
				break;
			case MSG_ID_CON_SUCCESS:
				notifyText.setText("成功连接到小车!");

				Message msgStartCheck = new Message();
				msgStartCheck.what = MSG_ID_START_CHECK;
				mHandler.sendMessageDelayed(msgStartCheck, 3000);

				Message msgHB1 = new Message();
				msgHB1.what = MSG_ID_HEART_BREAK_RECEIVE;// 启动心跳包检测循环
				mHandler.sendMessage(msgHB1);

				Message msgHB2 = new Message();
				msgHB2.what = MSG_ID_HEART_BREAK_SEND;// 启动心跳包循环发送
				mHandler.sendMessage(msgHB2);

				break;
			case MSG_ID_START_CHECK:
				isReaddyToSendCmd = true;
				selfcheck();
				break;
			case MSG_ID_ERR_CONN:
				notifyText.setText("连接小车失败!");
				break;
			case MSG_ID_HEART_BREAK_RECEIVE:
				if (recvHeartBreakCounter == 0) {
					haveReceivedHeartBreak = false;
				} else if (recvHeartBreakCounter > 0) {
					haveReceivedHeartBreak = true; //接收到了小车发来的心跳包
				} else {
					notifyText.setText("心跳包出现异常,已经忽略");
				}
				Log.i("main", "handle MSG_ID_HEART_BREAK_RECEIVE :flag="
						+ haveReceivedHeartBreak);

				if (lastrecvHeartBreakCounter == 0 && recvHeartBreakCounter > 0) {
					startIconAnimation();
				}
				lastrecvHeartBreakCounter = recvHeartBreakCounter;
				recvHeartBreakCounter = 0;
				Message msgHB = new Message();
				msgHB.what = MSG_ID_HEART_BREAK_RECEIVE;// 启动心跳包检测循环
				mHandler.sendMessageDelayed(msgHB, HEART_BREAK_CHECK_INTERVAL);
				break;
			case MSG_ID_HEART_BREAK_SEND:
				/** 暂时取消心跳包发送
				Message msgSB = new Message();
				msgSB.what = MSG_ID_HEART_BREAK_SEND;// 循环向小车发送心跳包
				Log.i("main", "handle MSG_ID_HEART_BREAK_SEND");

				sendCommand(new Command(new CategoryBit(CategoryBit.CHECK),
						new CommandBit(CommandBit.HEART_BREAK), new ValueBit(0,
								0)));
				mHandler.sendMessageDelayed(msgSB, HEART_BREAK_SEND_INTERVAL);
				*/
				break;
			default:
				break;
			}
			super.handleMessage(msg);

		}
	};

	private boolean isIconAnimationEnabled() {
		return animationEnabled && haveReceivedHeartBreak;
	}

	private boolean mIconAnimationState = false;
	/** Icon animation handler for flashing warning alerts. */
	private final Handler mAnimationHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mIconAnimationState) {
				if (isIconAnimationEnabled()) {
					mAnimationHandler.sendEmptyMessageDelayed(0,
							WARNING_ICON_ON_DURATION_MSEC);
				}
			} else {
				if (isIconAnimationEnabled()) {
					mAnimationHandler.sendEmptyMessageDelayed(0,
							WARNING_ICON_OFF_DURATION_MSEC);
				}
			}
			mIconAnimationState = !mIconAnimationState;
		}
	};

	private void startIconAnimation() {
		Log.i("Animation", "startIconAnimation handler : " + mAnimationHandler);
		if (isIconAnimationEnabled())
			mAnimationHandler.sendEmptyMessageDelayed(0,
					WARNING_ICON_ON_DURATION_MSEC);
	}

	private void handleHeartBreak() {
		Log.i("Main", "handleHeartBreak");
		recvHeartBreakCounter++;
		haveReceivedHeartBreak = true;
	}

	private void stopIconAnimation() {
		mAnimationHandler.removeMessages(0);
	}

	public void onDestroy() {
		if (null != tcpSocket) {
			try {
				tcpSocket.closeSocket();
			} catch (Exception e) {
				e.printStackTrace();
			}
			isEnableRecvMsgThread = false;
			recvMsgThread.interrupt();
		}

		if (null != mHandler) {
			int i;
			for (i = MSG_ID_LOOP_START + 1; i < MSG_ID_LOOP_END; i++) {
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
		if (enableGravity) {
			this.directionSensorEventListener.unregisterListener();
		}
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ConnectionState status = getWifiStatus();
		if (ConnectionState.WIFI_STATE_CONNECTED == status) {
			String cameraUrl = null;
			cameraUrl = CAMERA_VIDEO_URL;
			if (null != cameraUrl && cameraUrl.length() > 4) {
				backgroundView.setSource(cameraUrl);// 初始化Camera
			}
		}
		backgroundView.resumePlayback();
		if (enableGravity) {
			this.directionSensorEventListener.registerListener();
		}
	}

	void initSettings() {
		SharedPreferences sharedPreferences = getSharedPreferences(SettingsActivity.CONFIG_FILE_NAME, Activity.MODE_PRIVATE); 
		ROUTER_CONTROL_URL = sharedPreferences.getString(SettingsActivity.IP_ADDR_KEY, SettingsActivity.IP_ADDR_DEFAULT);
		ROUTER_CONTROL_PORT = Integer.parseInt(sharedPreferences.getString(SettingsActivity.NET_PORT_KEY, SettingsActivity.NET_PORT_DEFAULT));
		WIFI_SSID_PERFIX = sharedPreferences.getString(SettingsActivity.WIFI_SSID_KEY, SettingsActivity.WIFI_SSID_DEFAULT);

		if (ROUTER_CONTROL_URL != null && !"".equals(ROUTER_CONTROL_URL)) {
			CAMERA_VIDEO_URL = CAMERA_VIDEO_URL_PREFIX + ROUTER_CONTROL_URL
					+ CAMERA_VIDEO_URL_SUFFIX;
		}

	}
}
