package com.hanry.command;

import com.hanry.Utils;

/**
 ** 6~9λ��ʾȡֵλ��
 ** ������λΪ��`00000000`��������
 *   * 6~7λ��ʾ�ٶȰٷֱȣ���ΧΪʮ����`0~100`������0��ʾ�ٶ�Ϊ0
 *   * 8~9λ��ʾת�����Ƕȣ���ΧΪΪʮ����`0~180`������90��ʾ���λ������λ��
 ** ������λΪ��`00001100`�ƹ���
 *   * 6~7��ʾ�ƹܿ����������`00`��ʾ�صƣ�`0F`��ʾ�ص�
 *   * 8~9λ��ʾ����ֵ
 */
public class ValueBit {
	// ������
	public static final int SPEED0 = 0x0;
	public static final int ANGLE0 = 0x0;
	
	//�ƹ���
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
