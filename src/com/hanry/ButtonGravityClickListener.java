package com.hanry;

import com.hanry.command.CategoryBit;
import com.hanry.command.Command;
import com.hanry.command.CommandBit;
import com.hanry.command.ValueBit;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;
import android.view.View.OnClickListener;

public class ButtonGravityClickListener implements OnClickListener {
	private Main main = null;

	public Main getMain() {
		return main;
	}

	public void setMain(Main main) {
		this.main = main;
	}

	public ButtonGravityClickListener(Main main) {
		this.main = main;
	}

	public ButtonGravityClickListener() {
		super();
	}

	public void onClick(View arg0) {
		if (main.getEnableGravity()) {
			main.setEnableGravity(false);
			main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.STOP), new ValueBit()).getBytes());
			main.getEnableGravityButton().setTextColor(Color.BLACK);
			main.sensorManager.unregisterListener(main);
			main.toShowComponent();
		} else {
			main.setEnableGravity(true);
			main.getEnableGravityButton().setTextColor(Color.YELLOW);
			main.sensorManager.registerListener(main, main.sensorManager
					.getDefaultSensor(Sensor.TYPE_ORIENTATION),
					SensorManager.SENSOR_DELAY_GAME);
			main.toHideComponent();
		}
	}
}
