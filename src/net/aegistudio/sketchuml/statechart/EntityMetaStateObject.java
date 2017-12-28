package net.aegistudio.sketchuml.statechart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.PropertyView;
import net.aegistudio.sketchuml.SketchView;
import net.aegistudio.sketchuml.framework.PropertyPanel;

public class EntityMetaStateObject implements SketchView, PropertyView {
	public static int STATE_ROUNDSIZE = 50;
	public static int STATE_NAMEHEIGHT = 24;
	private PropertyPanel<EntityStateObject> viewObject;
	
	@Override
	public Component getViewObject(Consumer<Entity> notifier) {
		if(viewObject == null) {
			viewObject = new PropertyPanel<EntityStateObject>();
			
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
		viewObject.setNotifier(notifier);
		return viewObject;
	}

	@Override
	public void updateEntity(Entity entity) {
		viewObject.updateEntity((EntityStateObject)entity);
	}

	@Override
	public void renderEntity(Graphics g, Entity entity, boolean preview) {
		Rectangle bound = g.getClipBounds();
		EntityStateObject entityState = (EntityStateObject)entity;
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(2));
		
		// Draw outline.
		g.setColor(Color.WHITE);
		g.fillRoundRect(1, 1, bound.width - 2, bound.height - 2, 
				STATE_ROUNDSIZE, STATE_ROUNDSIZE);
		g.setColor(preview? Color.GRAY : Color.BLACK);
		g.drawRoundRect(1, 1, bound.width - 2, bound.height - 2, 
				STATE_ROUNDSIZE, STATE_ROUNDSIZE);
		
		// Draw internal text.
		Rectangle2D nameMetric = g.getFontMetrics()
				.getStringBounds(entityState.name, g); 
		if(!entityState.isBrief) {
			g.drawLine(0, STATE_NAMEHEIGHT, 
					bound.width, STATE_NAMEHEIGHT);
			g.drawString(entityState.name, 
					(int)(bound.width - nameMetric.getWidth()) / 2, 
					(int)(STATE_NAMEHEIGHT + nameMetric.getHeight() / 2) / 2);
			
			Graphics actionGraphics = g.create(2, STATE_NAMEHEIGHT + 2, 
					bound.width - 4, bound.height - STATE_NAMEHEIGHT - 2);
			actionGraphics.drawString(entityState.actions, 0, 20);
		}
		else {
			// Draw string only.
			g.drawString(entityState.name, 
					(int)(bound.width - nameMetric.getWidth()) / 2, 
					(int)(bound.height + nameMetric.getHeight() / 2) / 2);
		}
	}

}
