package net.aegistudio.sketchuml.general;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.Consumer;

import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.PropertyView;
import net.aegistudio.sketchuml.SketchRenderHint;
import net.aegistudio.sketchuml.SketchView;
import net.aegistudio.sketchuml.framework.PropertyPanel;
import net.aegistudio.sketchuml.framework.RegularRenderer;
import net.aegistudio.sketchuml.framework.RenderUtils;

public class EntityMetaDecision implements 
	SketchView, PropertyView.Factory, RegularRenderer.Painter {
	private final RegularRenderer renderer = new RegularRenderer(this);
	public static double DECISION_AXISRATIO = 2.0;	// Height by width.

	@Override
	public PropertyView newPropertyView(Consumer<Entity> notifier) {
		return new PropertyView() {
			private PropertyPanel<EntityDecision> decisionPanel; {
				decisionPanel = new PropertyPanel<>(notifier);
				decisionPanel.registerTextField("Guard: ", 
						obj -> obj.guard, 
						(obj, str) -> obj.guard = str);
			}
			
			@Override
			public Component getViewObject() {
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
		};
	}
	
	@Override
	public void renderEntity(SketchRenderHint hint, Graphics g, 
			Entity entity, boolean preview, int w, int h) {
		renderer.renderEntity(hint, g, entity, preview, w, h);
	}

	@Override
	public String overlayEntity(Entity entity, OverlayDirection old) {
		EntityDecision entityDecision = (EntityDecision)entity;
		if(!OverlayDirection.UP.equals(old)) return null;
		if(entityDecision.guard.length() == 0) return null;
		return "[" + entityDecision.guard + "]";
	}

	@Override
	public void render(SketchRenderHint hint, Graphics g, 
			int length, boolean preview) {
		
		Graphics2D g2d = (Graphics2D)g;
		int outlineWidth = (int)hint.outlineWidth;
		int outlineOffset = outlineWidth / 2 + 1;
		int clampedWidth = length - outlineWidth + 1;
		int semiLength = length / 2;
		
		// Retrieve the line point coordinates..
		int[] xs = new int[4];
		int[] ys = new int[4];
		xs[0] = semiLength; ys[0] = outlineOffset;	// Top point.
		xs[1] = clampedWidth; ys[1] = semiLength;	// Center point.
		xs[2] = semiLength; ys[2] = clampedWidth;	// Bottom point.
		xs[3] = outlineOffset; ys[3] = semiLength;	// Left point.
		
		// Fill the diamond.
		RenderUtils.beginFill(g2d, hint, preview);
		g2d.fillPolygon(xs, ys, 4);
		
		// Draw the diamond.
		RenderUtils.beginOutline(g2d, hint, preview);
		g2d.drawPolygon(xs, ys, 4);
	}
}
