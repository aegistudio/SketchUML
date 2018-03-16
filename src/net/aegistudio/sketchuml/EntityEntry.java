package net.aegistudio.sketchuml;

import java.util.function.Supplier;

/**
 * A low level entry that serves as candidate of
 * editing and recognizing stroke.
 * 
 * @author Haoran
 */
public class EntityEntry {
	public final String entry;
	
	public final String name;
	
	public final String description;
	
	public final Supplier<Entity> factory;
	
	public final PropertyView.Factory propertyFactory;
	
	public final SketchView sketchView;
	
	public EntityEntry(String entry, String name,
			String description, Supplier<Entity> factory,
			PropertyView.Factory propertyFactory, 
			SketchView sketchView) {
		this.entry = entry;
		this.name = name;
		this.description = description;
		this.factory = factory;
		this.propertyFactory = propertyFactory;
		this.sketchView = sketchView;
	}
}
