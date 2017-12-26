package net.aegistudio.sketchuml.framework;

import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.EntityEntry;

public class SketchEntityComponent {
	public int x, y, w, h;
	
	public final EntityEntry entry;
	
	public final Entity entity;
	
	public SketchEntityComponent(EntityEntry entry, Entity entity) {
		this.entity = entity;
		this.entry = entry;
	}
}
