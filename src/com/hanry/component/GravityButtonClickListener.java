package com.hanry.component;

import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;

import com.hanry.Main;
import com.hanry.command.CategoryBit;
import com.hanry.command.Command;
import com.hanry.command.CommandBit;
import com.hanry.command.ValueBit;

public class GravityButtonClickListener implements OnClickListener {
	private Main main = null;

	public Main getMain() {
		return main;
	}

	public void setMain(Main main) {
		this.main = main;
	}

	public GravityButtonClickListener(Main main) {
		this.main = main;
	}

	public GravityButtonClickListener() {
		super();
	}

	public void onClick(View arg0) {
		if (main.getEnableGravity()) {
			main.setEnableGravity(false);
			main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.STOP), new ValueBit()));
			main.getEnableGravityButton().setTextColor(Color.BLACK);
			main.getDirectionSensorEventListener().unregisterListener();
			main.toShowComponent();
		} else {
			main.setEnableGravity(true);
			main.getEnableGravityButton().setTextColor(Color.YELLOW);
			main.getDirectionSensorEventListener().registerListener();
			main.toHideComponent();
		}
	}
}
