package com.hanry.component;

public interface OnLeftAndRightJoystickMoveListener {
	public void onValueChanged(int power, int direction);

	public void OnReleased();

	public void OnReturnedToCenter();

}
