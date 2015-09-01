package com.hanry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
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
				dialog_Exit(MenuActivity.this);
			}
		});
	}

	@Override
	public void onBackPressed() {
		dialog_Exit(MenuActivity.this);
	}

	public static void dialog_Exit(Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setMessage("确定要退出吗?");
		builder.setTitle("提示");
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});

		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builder.create().show();
	}
}
