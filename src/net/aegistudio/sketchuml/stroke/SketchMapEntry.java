package net.aegistudio.sketchuml.stroke;

import net.aegistudio.sketchuml.EntityFactory;

/**
 * A low level entry that serves as candidate of
 * editing and recognizing stroke.
 * 
 * @author Haoran
 */
public class SketchMapEntry {
	public final String entry;
	
	public final String name;
	
	public final String description;
	
	public final EntityFactory factory;
	
	public SketchMapEntry(String entry, String name,
			String description, EntityFactory factory) {
		this.entry = entry;
		this.name = name;
		this.description = description;
		this.factory = factory;
	}
}
