package com.hanry.command;

import com.hanry.Utils;

public class CategoryBit {

	public static final byte DIRECTION = 0x00;
	public static final byte SERVO = 0x03;  //0011
	public static final byte LIGHT = 0x0C;  //1100
	public static final byte CHECK = (byte)0x0F; //1111
	
	private byte categoryBit = 0;
	public CategoryBit(){
		
	}
	
	public CategoryBit(byte categoryBit) {
		this.categoryBit = categoryBit;
	}

	public byte getCategoryBit() {
		return categoryBit;
	}

	public void setCategoryBit(byte categoryBit) {
		this.categoryBit = categoryBit;
	}
	
	public byte getByte(){
		return this.categoryBit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + categoryBit;
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
		CategoryBit other = (CategoryBit) obj;
		if (categoryBit != other.categoryBit)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CategoryBit [categoryBit=" + Utils.castByteToHexString(getByte()) + "]";
	}
}
