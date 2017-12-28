package net.aegistudio.sketchuml.statechart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.aegistudio.sketchuml.Entity;

public class EntityStateObject implements Entity {
	public String name = "";
	
	public String actions = "";
	
	public boolean isBrief = false;

	@Override
	public void load(DataInputStream inputStream) throws IOException {
		
	}

	@Override
	public void save(DataOutputStream outputStream) throws IOException {
		
	}
}
