package net.aegistudio.sketchuml;

import java.awt.Component;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.JPanel;

/**
 * The simplest entity that stores no data or state, and will be 
 * represented as a single icon on screen.
 * 
 * @author Haoran Luo
 */
public abstract class SimpleEntity implements Entity, PropertyView, SketchView, EntityFactory {
	private static final JPanel SIMPLE_PANEL = new JPanel();
	
	public Entity create() { return this; }
	
	@Override
	public final void load(DataInputStream inputStream) throws IOException {
		
	}

	@Override
	public final void save(DataOutputStream outputStream) throws IOException {
		
	}
	
	@Override
	public final Component getViewObject(Consumer<Entity> notifier) {
		return SIMPLE_PANEL;
	}

	@Override
	public final void updateEntity(Entity entity) {
		
	}

	@Override
	public abstract void renderEntity(Graphics g, Entity entity, boolean preview);
}
