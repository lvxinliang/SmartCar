package com.hanry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.beardedhen.androidbootstrap.FontAwesomeText;

public class AboutActivity extends Activity {

	private FontAwesomeText buttonBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.about);
		buttonBack = (FontAwesomeText)this.findViewById(R.id.buttonBack);
		buttonBack.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent setIntent = new Intent();
				setIntent.setClass(AboutActivity.this, MenuActivity.class);
				startActivity(setIntent);
			}
		});
	}
}
