package net.aegistudio.sketchuml;

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
	
	public final EntityFactory factory;
	
	public final PropertyView propertyView;
	
	public final SketchView sketchView;
	
	public EntityEntry(String entry, String name,
			String description, EntityFactory factory,
			PropertyView propertyView, SketchView sketchView) {
		this.entry = entry;
		this.name = name;
		this.description = description;
		this.factory = factory;
		this.propertyView = propertyView;
		this.sketchView = sketchView;
	}
}
