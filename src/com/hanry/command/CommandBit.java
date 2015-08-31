package com.hanry.command;

import com.hanry.Utils;

public class CommandBit {
	
	private byte commandBit = 0;
	
	//方向类
	public static final byte STOP = 0x00;
	public static final byte FORWARD = 0x03;
	public static final byte BACK = 0x0C;
	public static final byte LEFT = 0x0F;
	public static final byte RIGHT = 0x30;
	public static final byte LEFT_FORWARD = (byte)0xC3;
	public static final byte LEFT_BACK = (byte)0xCC;
	public static final byte RIGHT_FORWARD = (byte)0x3C;
	public static final byte RIGHT_BACK = 0x33;

	//舵机类
	public static final byte CHANNEL0 = 0x00;
	public static final byte CHANNEL1 = 0x03;
	public static final byte CHANNEL2 = 0x0C;
	public static final byte CHANNEL3 = 0x0F;
	
	//灯光类
	public static final byte LIGHT0 = 0x00;
	public static final byte LIGHT1 = 0x03;
	public static final byte LIGHT2 = 0x0C;
	public static final byte LIGHT3 = 0x0F;

	//检测类
	public static final byte HEART_BREAK = 0x00;
	public static final byte SELF_CHECK = 0x03;
	
	public CommandBit() {
		
	}
	public CommandBit(byte commandBit) {
		this.commandBit = commandBit;
	}

	public byte getCommandBit() {
		return commandBit;
	}
	
	public void setCommandBit(byte commandBit) {
		this.commandBit = commandBit;
	}
	
	public byte getByte() {
		return getCommandBit();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + commandBit;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandBit other = (CommandBit) obj;
		if (commandBit != other.commandBit)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "CommandBit [commandBit=" + Utils.castByteToHexString(getByte()) + "]";
	}
	
	
}