package net.aegistudio.sketchuml.framework;

import java.awt.geom.Rectangle2D;

import net.aegistudio.sketchuml.abstraction.Entity;
import net.aegistudio.sketchuml.abstraction.EntityEntry;

public class SketchEntityComponent {
	public int x, y, w, h;
	
	public final EntityEntry entry;
	
	public final Entity entity;
	
	public SketchEntityComponent(EntityEntry entry, Entity entity) {
		this.entity = entity;
		this.entry = entry;
	}
	
	public Rectangle2D getBoundRectangle() {
		return new Rectangle2D.Double(x, y, w, h);
	}
}
