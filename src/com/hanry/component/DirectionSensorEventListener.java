package com.hanry.component;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.hanry.Main;
import com.hanry.command.CategoryBit;
import com.hanry.command.Command;
import com.hanry.command.CommandBit;
import com.hanry.command.ValueBit;

public class DirectionSensorEventListener implements SensorEventListener {

	private static final int TRIGGER_VALUE = 12;
	protected SensorManager sensorManager;
	private Main main = null;

	public DirectionSensorEventListener(Main main) {
		super();
		this.main  = main;
		this.sensorManager = (SensorManager) this.main
				.getSystemService(Context.SENSOR_SERVICE);
	}

	/**
	 * ���������ȷ����ı�ʱ�Ļص�����
	 */
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	/**
	 * ��������ֵ�����ı�ʱ�Ļص�����
	 */
	public void onSensorChanged(SensorEvent event) {

		float[] values = event.values;
		float xValue = values[1];
		float yValue = values[2];
		// StringBuilder sb = new StringBuilder();
		// sb.append("Z��ת���ĽǶ�:");
		// sb.append(values[0]);
		// sb.append("\nX��ת���ĽǶ�:");
		// sb.append(values[1]);
		// sb.append("\nY��ת���ĽǶ�:");
		// sb.append(values[2]);
		// this.mLogText.setText(sb.toString());

		if (yValue < -TRIGGER_VALUE) {
			if (xValue > TRIGGER_VALUE) {
				// left_forward
				this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
						new CommandBit(CommandBit.LEFT_FORWARD), new ValueBit())
						);

			} else if (xValue < -TRIGGER_VALUE) {
				// right_forward
				this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
						new CommandBit(CommandBit.RIGHT_FORWARD),
						new ValueBit()));
			} else {
				// forward
				this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
						new CommandBit(CommandBit.FORWARD), new ValueBit())
						);

			}
		} else if (yValue > TRIGGER_VALUE) {
			if (xValue > TRIGGER_VALUE) {
				this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
						new CommandBit(CommandBit.LEFT_BACK), new ValueBit())
						);
				// left_back

			} else if (xValue < -TRIGGER_VALUE) {
				this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
						new CommandBit(CommandBit.RIGHT_BACK), new ValueBit())
						);
				// right_back
			} else {
				// back
				this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
						new CommandBit(CommandBit.BACK), new ValueBit())
						);

			}
		}
		if (xValue < -TRIGGER_VALUE) {
			if (yValue < -TRIGGER_VALUE) {
				// right_forward
				this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
						new CommandBit(CommandBit.RIGHT_FORWARD),
						new ValueBit()));

			} else if (yValue > TRIGGER_VALUE) {
				// right_back
				this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
						new CommandBit(CommandBit.RIGHT_BACK), new ValueBit())
						);
			} else {
				// right
				this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
						new CommandBit(CommandBit.RIGHT), new ValueBit())
						);
			}
		} else if (xValue > TRIGGER_VALUE) {
			if (yValue < -TRIGGER_VALUE) {
				// left_forward
				this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
						new CommandBit(CommandBit.LEFT_FORWARD), new ValueBit())
						);

			} else if (yValue > TRIGGER_VALUE) {
				// left_back
				this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
						new CommandBit(CommandBit.LEFT_BACK), new ValueBit())
						);
			} else {
				// left
				this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
						new CommandBit(CommandBit.LEFT), new ValueBit())
						);
			}
		}

		if (xValue > -TRIGGER_VALUE && xValue < TRIGGER_VALUE
				&& yValue > -TRIGGER_VALUE && yValue < TRIGGER_VALUE) {
			// stop
			this.main.sendCommand(new Command(new CategoryBit(CategoryBit.DIRECTION),
					new CommandBit(CommandBit.STOP), new ValueBit()));
		}
	}

	public void unregisterListener() {
		this.sensorManager.unregisterListener(this);
	}

	@SuppressWarnings("deprecation")
	public void registerListener() {
		this.sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);
	}

}
