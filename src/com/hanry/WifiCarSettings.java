package com.hanry;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class WifiCarSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {  
	
	private EditTextPreference mPrefRouterUrl;
	
	@Override  
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  
		// 所的的值将会自动保存到SharePreferences  
		addPreferencesFromResource(R.layout.wifi_car_settings);
		
		
		mPrefRouterUrl = (EditTextPreference)findPreference(Constant.PREF_KEY_ROUTER_URL);
		
		initValue();
	}  
	 
	 void initValue(){
		 
		 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		 
		 String RouterUrl = settings.getString(Constant.PREF_KEY_ROUTER_URL, Constant.DEFAULT_VALUE_ROUTER_URL);
		 mPrefRouterUrl.setSummary(RouterUrl);
	 }
	 
    @Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
	}
	
 
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
		Preference pref = findPreference(key);
	    if (pref instanceof EditTextPreference) {
	        EditTextPreference etp = (EditTextPreference) pref;
	        etp.setSummary(etp.getText());
	    }
	}
}
