package com.hanry.command;

import com.hanry.Utils;

/**
 ** 6~9位表示取值位。
 ** 当分类位为：`00000000`方向盘类
 *   * 6~7位表示速度百分比，范围为十进制`0~100`，其中0表示速度为0
 *   * 8~9位表示转向舵机角度，范围为为十进制`0~180`，其中90表示舵机位于中心位置
 ** 当分类位为：`00001100`灯光类
 *   * 6~7表示灯管开关命令，其中`00`表示关灯，`0F`表示关灯
 *   * 8~9位表示亮度值
 */
public class ValueBit {
	// 方向类
	public static final int SPEED0 = 0x0;
	public static final int ANGLE0 = 0x0;
	
	//灯光类
	public static final int TURNOFF = 0x0;
	public static final int TURNON = 0x0F;
	public static final int LIGHTENESS0 = 0x0;

	private int highByte = SPEED0;
	private int lowByte = ANGLE0;
	
	public ValueBit(){

	}
	
	public ValueBit(int highByte, int lowByte) {
		this.highByte = highByte;
		this.lowByte = lowByte;
	}
	
	public int getHighByte() {
		return highByte;
	}
	public void setHighByte(int highByte) {
		this.highByte = highByte;
	}
	public int getLowByte() {
		return lowByte;
	}
	public void setLowByte(int lowByte) {
		this.lowByte = lowByte;
	}
	
	public byte[] getBytes(){
		return new byte[]{(byte) this.highByte, (byte) this.lowByte};
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + highByte;
		result = prime * result + lowByte;
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
		ValueBit other = (ValueBit) obj;
		if (highByte != other.highByte)
			return false;
		if (lowByte != other.lowByte)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return Utils.castBytesToHexString(this.getBytes());
	}
	
}
