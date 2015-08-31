package com.hanry;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.FontAwesomeText;

public class SettingsActivity extends Activity{
	
	private FontAwesomeText buttonBack;
	private BootstrapEditText ipAddressText;
	private BootstrapEditText netPortText;
	private BootstrapButton resetButton;
	private BootstrapButton applyButton;
	private BootstrapEditText wifiSsidText;
	public static final String CONFIG_FILE_NAME = "config";
	public static final String IP_ADDR_KEY ="ipAddr";
	public static final String NET_PORT_KEY = "netPort";
	public static final String WIFI_SSID_KEY = "wifiSsid";
	public static final String IP_ADDR_DEFAULT ="192.168.1.1";
	public static final String NET_PORT_DEFAULT = "2001";
	public static final String WIFI_SSID_DEFAULT = "Singular_Wifi-Car";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		buttonBack = (FontAwesomeText)this.findViewById(R.id.buttonBack);
		ipAddressText = (BootstrapEditText)this.findViewById(R.id.ipAddress);
		netPortText = (BootstrapEditText)this.findViewById(R.id.netPort);
		wifiSsidText = (BootstrapEditText)this.findViewById(R.id.wifiSsid);
		resetButton = (BootstrapButton)this.findViewById(R.id.resetButton);
		applyButton = (BootstrapButton)this.findViewById(R.id.applyButton);
		
		loadConfig();
		
		ipAddressText.clearFocus();
		netPortText.clearFocus();
		wifiSsidText.clearFocus();
		applyButton.requestFocus();
		buttonBack.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent setIntent = new Intent();
				setIntent.setClass(SettingsActivity.this, MenuActivity.class);
				startActivity(setIntent);
			}
		});
		
		applyButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				ipAddressText.setTextColor(Color.BLACK);
				netPortText.setTextColor(Color.BLACK);
				wifiSsidText.setTextColor(Color.BLACK);
				if(!Utils.isEffectiveIpAddr(ipAddressText.getText().toString())){
					ipAddressText.setTextColor(Color.RED);
					Toast.makeText(getApplicationContext(), "IP地址格式不正确",
							Toast.LENGTH_SHORT).show();
					return;
				}else if(!Utils.isEffectiveNetPort(netPortText.getText().toString())){
					netPortText.setTextColor(Color.RED);
					Toast.makeText(getApplicationContext(), "网络端口号格式不正确",
							Toast.LENGTH_SHORT).show();
					return;
				}else if(!Utils.isEffectiveWifiSSID(wifiSsidText.getText().toString())){
					wifiSsidText.setTextColor(Color.RED);
					Toast.makeText(getApplicationContext(), "Wifi名称格式不正确",
							Toast.LENGTH_SHORT).show();
				}
				SharedPreferences mySharedPreferences = getSharedPreferences(CONFIG_FILE_NAME, Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = mySharedPreferences.edit(); 
				editor.putString(IP_ADDR_KEY, getIpAddr());
				editor.putString(NET_PORT_KEY, getNetPort());
				editor.putString(WIFI_SSID_KEY, wifiSsidText.getText().toString());
				editor.commit(); 
				Toast.makeText(getApplicationContext(), "应用成功",
						Toast.LENGTH_SHORT).show();
			}

		});
		
		resetButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				loadConfig();
			}
		});
	}

	protected void loadConfig() {
		SharedPreferences sharedPreferences = getSharedPreferences(CONFIG_FILE_NAME, Activity.MODE_PRIVATE); 
		ipAddressText.setText(sharedPreferences.getString(IP_ADDR_KEY, IP_ADDR_DEFAULT));
		netPortText.setText(sharedPreferences.getString(NET_PORT_KEY, NET_PORT_DEFAULT));
		wifiSsidText.setText(sharedPreferences.getString(WIFI_SSID_KEY, WIFI_SSID_DEFAULT));
		ipAddressText.setTextColor(Color.BLACK);
	}

	protected boolean validationData() {
		return Utils.isEffectiveIpAddr(ipAddressText.getText().toString()) && Utils.isEffectiveNetPort(netPortText.getText().toString());
	}

	protected String getIpAddr() {
		return ipAddressText.getText().toString();
	}
	protected String getNetPort() {
		return netPortText.getText().toString();
	}
}
