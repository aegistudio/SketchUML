package net.aegistudio.sketchuml.general;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.aegistudio.sketchuml.Entity;

public class EntityDecision implements Entity {
	public String guard = "";
	
	@Override
	public void load(DataInputStream inputStream) throws IOException {
		guard = inputStream.readUTF();
	}

	@Override
	public void save(DataOutputStream outputStream) throws IOException {
		outputStream.writeUTF(guard);
	}
}
