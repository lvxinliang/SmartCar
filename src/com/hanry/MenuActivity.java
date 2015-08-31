package com.hanry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.beardedhen.androidbootstrap.BootstrapButton;

public class MenuActivity extends Activity {

	private BootstrapButton startButton;
	private BootstrapButton settingsButton;
	private BootstrapButton aboutButton;
	private BootstrapButton exitButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		startButton = (BootstrapButton)this.findViewById(R.id.startButton);
		settingsButton = (BootstrapButton)this.findViewById(R.id.settingsButton);
		aboutButton = (BootstrapButton)this.findViewById(R.id.aboutButton);
		exitButton = (BootstrapButton)this.findViewById(R.id.exitButton);
		this.startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(MenuActivity.this, Main.class);
				startActivity(intent);
			}
		});
		this.settingsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});
		this.aboutButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(MenuActivity.this, AboutActivity.class);
				startActivity(intent);
			}
		});
		this.exitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
	}
}
