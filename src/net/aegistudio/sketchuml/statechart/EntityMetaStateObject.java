package net.aegistudio.sketchuml.statechart;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.PropertyView;
import net.aegistudio.sketchuml.SketchRenderHint;
import net.aegistudio.sketchuml.SketchView;
import net.aegistudio.sketchuml.framework.PropertyPanel;
import net.aegistudio.sketchuml.framework.RenderUtils;

public class EntityMetaStateObject implements SketchView, PropertyView.Factory {
	public static int STATE_ROUNDSIZE = 50;
	public static int STATE_ROUNDOFFSET = 10;
	public static int STATE_NAMEHEIGHT = 24;
	
	@Override
	public PropertyView newPropertyView(Consumer<Entity> notifier) {
		return new PropertyView() {
			private final PropertyPanel<EntityStateObject> viewObject; {
				viewObject = new PropertyPanel<EntityStateObject>(notifier);
				
				// Add the state name.
				viewObject.registerTextField("State Name: ", 
						(entity) -> entity.name, 
						(entity, name) -> entity.name = name);
				
				// Add is brief option.
				viewObject.registerCheckBox("Hide actions",
						(entity) -> entity.isBrief, 
						(entity, isBrief) -> entity.isBrief = isBrief);
				
				// The concrete actions area.
				viewObject.registerTextArea("Actions:", 
						(entity) -> entity.actions,
						(entity, actions) -> entity.actions = actions);
			}
			
			@Override
			public Component getViewObject() {
				return viewObject;
			}
		
			@Override
			public void select(Entity entity) {
				viewObject.selectEntity((EntityStateObject)entity);
			}
		
			@Override
			public void update(Entity entity) {
				viewObject.updateEntity((EntityStateObject)entity);
			}
		};
	}

	@Override
	public void renderEntity(SketchRenderHint hint, 
			Graphics g, Entity entity, boolean preview,
			int entityWidth, int entityHeight) {
		EntityStateObject entityState = (EntityStateObject)entity;
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(hint.outlineWidth));
		
		// Draw outline.
		int outlineWidth = (int)hint.outlineWidth;
		int outlineOffset = outlineWidth / 2;
		RenderUtils.beginFill(g2d, hint, preview);
		g.fillRoundRect(outlineOffset, outlineOffset, 
				entityWidth - outlineWidth, 
				entityHeight - outlineWidth, 
				STATE_ROUNDSIZE, STATE_ROUNDSIZE);
		RenderUtils.beginOutline(g2d, hint, preview);
		g.drawRoundRect(outlineOffset, outlineOffset, 
				entityWidth - outlineWidth, 
				entityHeight - outlineWidth, 
				STATE_ROUNDSIZE, STATE_ROUNDSIZE);
		
		// Draw internal text.
		Rectangle2D nameMetric = g.getFontMetrics()
				.getStringBounds(entityState.name, g); 
		if(!entityState.isBrief) {
			RenderUtils.beginInline(g2d, hint, preview);
			g.drawLine(0, STATE_NAMEHEIGHT, 
					entityWidth, STATE_NAMEHEIGHT);

			// Draw the state text and actions.
			g.setColor(hint.getLineColor(SketchRenderHint.innerLabelColor, preview));
			Graphics actionGraphics = g.create(STATE_ROUNDOFFSET, 
					STATE_NAMEHEIGHT + 2, entityWidth - STATE_ROUNDOFFSET * 2, 
					entityHeight - STATE_NAMEHEIGHT - 2);
			g.drawString(entityState.name, 
					(int)(entityWidth - nameMetric.getWidth()) / 2, 
					(int)(STATE_NAMEHEIGHT + nameMetric.getHeight() / 2) / 2);
			RenderUtils.drawLines(actionGraphics, entityState.actions.split("\n"));
		}
		else {
			// Draw string only.
			g.setColor(hint.getLineColor(SketchRenderHint.innerLabelColor, preview));
			g.drawString(entityState.name, 
					(int)(entityWidth - nameMetric.getWidth()) / 2, 
					(int)(entityHeight + nameMetric.getHeight() / 2) / 2);
		}
	}

	@Override
	public String overlayEntity(Entity entity, OverlayDirection old) {
		return null;
	}
}
