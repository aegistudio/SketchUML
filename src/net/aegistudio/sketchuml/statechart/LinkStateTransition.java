package net.aegistudio.sketchuml.statechart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.aegistudio.sketchuml.Entity;

public class LinkStateTransition implements Entity {
	public String trigger = "", guard = "", action = "";

	@Override
	public void load(DataInputStream inputStream) throws IOException {
		trigger = inputStream.readUTF();
		guard = inputStream.readUTF();
		action = inputStream.readUTF();
	}

	@Override
	public void save(DataOutputStream outputStream) throws IOException {
		outputStream.writeUTF(trigger);
		outputStream.writeUTF(guard);
		outputStream.writeUTF(action);
	}
}
