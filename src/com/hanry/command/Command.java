package com.hanry.command;


public class Command {
	public static final byte STARTBIT = (byte) 0xff;
	public static final byte ENDBIT = (byte) 0xff;
	public static final int LENGTH = 12;
	private CategoryBit categoryBit;
	private CommandBit commandBit;
	private ValueBit valueBit;
	
	public Command(){
		
	}
	
	public Command(CategoryBit categoryBit, CommandBit commandBit,
			ValueBit valueBit) {
		this.categoryBit = categoryBit;
		this.commandBit = commandBit;
		this.valueBit = valueBit;
	}

	public CategoryBit getCategoryBit() {
		return categoryBit;
	}

	public void setCategoryBit(CategoryBit categoryBit) {
		this.categoryBit = categoryBit;
	}

	public CommandBit getCommandBit() {
		return commandBit;
	}

	public void setCommandBit(CommandBit commandBit) {
		this.commandBit = commandBit;
	}

	public ValueBit getValueBit() {
		return valueBit;
	}

	public void setValueBit(ValueBit valueBit) {
		this.valueBit = valueBit;
	}

	public byte[] getBytes (){
		return new byte[]{STARTBIT, this.categoryBit.getByte(), this.commandBit.getByte(), (byte)this.valueBit.getHighByte(), (byte)this.valueBit.getLowByte(), ENDBIT};
	}
	
	@Override
	public String toString() {
		return Utils.castBytesToHexString(getBytes());
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((categoryBit == null) ? 0 : categoryBit.hashCode());
		result = prime * result
				+ ((commandBit == null) ? 0 : commandBit.hashCode());
		result = prime * result
				+ ((valueBit == null) ? 0 : valueBit.hashCode());
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
		Command other = (Command) obj;
		if (categoryBit == null) {
			if (other.categoryBit != null)
				return false;
		} else if (!categoryBit.equals(other.categoryBit))
			return false;
		if (commandBit == null) {
			if (other.commandBit != null)
				return false;
		} else if (!commandBit.equals(other.commandBit))
			return false;
		if (valueBit == null) {
			if (other.valueBit != null)
				return false;
		} else if (!valueBit.equals(other.valueBit))
			return false;
		return true;
	}

	public static void main(String[] args) {
		Command command = new Command(new CategoryBit(CategoryBit.DIRECTION), new CommandBit(CommandBit.LEFT_BACK), new ValueBit(0x20, 0x10));
		System.out.println(command);
	}
}
