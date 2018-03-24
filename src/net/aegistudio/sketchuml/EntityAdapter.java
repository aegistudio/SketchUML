package net.aegistudio.sketchuml;

import java.awt.Component;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The simplest entity that stores no data or state, and will be 
 * represented as a single icon on screen.
 * 
 * @author Haoran Luo
 */
public abstract class EntityAdapter implements Entity, 
	PropertyView, PropertyView.Factory, SketchView, Supplier<Entity> {
	
	public Entity get() { return this; }
	
	@Override
	public void load(DataInputStream inputStream) throws IOException {
		
	}

	@Override
	public void save(DataOutputStream outputStream) throws IOException {
		
	}
	
	@Override
	public PropertyView newPropertyView(Consumer<Entity> notifier) {
		return this;
	}
	
	@Override
	public Component getViewObject() {
		return null;
	}

	@Override
	public void update(Entity entity) {
		
	}

	@Override
	public void select(Entity entity) {
		
	}
	
	@Override
	public String overlayEntity(Entity entity, OverlayDirection old) {
		return null;
	}
	
	@Override
	public abstract void renderEntity(SketchRenderHint hint, 
			Graphics g, Entity entity, boolean preview);
}
