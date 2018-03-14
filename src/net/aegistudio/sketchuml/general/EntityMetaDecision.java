package net.aegistudio.sketchuml.general;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.Consumer;

import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.PropertyView;
import net.aegistudio.sketchuml.SketchView;
import net.aegistudio.sketchuml.framework.PropertyPanel;
import net.aegistudio.sketchuml.framework.RegularRenderer;

public class EntityMetaDecision implements 
	SketchView, PropertyView, RegularRenderer.Painter {
	
	private PropertyPanel<EntityDecision> decisionPanel;
	private final RegularRenderer renderer = new RegularRenderer(this);
	public static double DECISION_AXISRATIO = 2.0;	// Height by width.
	
	@Override
	public Component getViewObject(Consumer<Entity> notifier) {
		if(decisionPanel == null) {
			decisionPanel = new PropertyPanel<>();
			
			decisionPanel.registerTextField("Guard: ", 
					obj -> obj.guard, 
					(obj, str) -> obj.guard = str);
		}
		decisionPanel.setNotifier(notifier);
		return decisionPanel;
	}

	@Override
	public void update(Entity entity) {
		decisionPanel.updateEntity((EntityDecision)entity);
	}


	@Override
	public void select(Entity entity) {
		decisionPanel.selectEntity((EntityDecision)entity);
	}
	
	@Override
	public void renderEntity(Graphics g, Entity entity, boolean preview) {
		renderer.renderEntity(g, entity, preview);
	}

	@Override
	public String overlayEntity(Entity entity, OverlayDirection old) {
		EntityDecision entityDecision = (EntityDecision)entity;
		if(!OverlayDirection.UP.equals(old)) return null;
		if(entityDecision.guard.length() == 0) return null;
		return "[" + entityDecision.guard + "]";
	}

	@Override
	public void render(Graphics g, int length, boolean preview) {
		Graphics2D g2d = (Graphics2D)g;
		int[] xs = new int[] { length / 2, length - 3, length / 2, 2 };
		int[] ys = new int[] { 2, length / 2, length - 3, length / 2 };

		// Fill the diamond.
		g2d.setStroke(new BasicStroke(1));
		g2d.setColor(Color.WHITE);
		g2d.fillPolygon(xs, ys, 4);
		
		// Draw the diamond.
		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(preview? Color.GRAY : Color.BLACK);
		g2d.drawPolygon(xs, ys, 4);
	}
}
