package net.aegistudio.sketchuml.astaxpt;

import java.util.UUID;

public class DefaultUuidGenerator implements AstahUuidGenerator {
	private int id = 1;
	private final String uuidString;
	
	public DefaultUuidGenerator() {
		this(UUID.randomUUID());
	}
	
	public DefaultUuidGenerator(UUID uuidObject) {
		String mostSignificants = Long.toHexString(
				uuidObject.getMostSignificantBits());
		String leastSignificants = Long.toHexString(
				uuidObject.getLeastSignificantBits());
		
		// Append the UUID string.
		String uuidString = "-";
		uuidString += mostSignificants;
		uuidString += leastSignificants;
		this.uuidString = uuidString;
	}
	
	@Override
	public synchronized String nextUuid() {
		int currentId = id ++;
		return Integer.toUnsignedString(currentId, 
				Character.MAX_RADIX) + uuidString;
	}
}
