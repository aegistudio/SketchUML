package net.aegistudio.sketchuml.statechart;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;

import JP.co.esm.caddies.jomt.jmodel.FinalStatePresentation;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UFinalStateImp;
import net.aegistudio.sketchuml.abstraction.Entity;
import net.aegistudio.sketchuml.abstraction.EntityAdapter;
import net.aegistudio.sketchuml.abstraction.RegularRenderer;
import net.aegistudio.sketchuml.abstraction.RenderUtils;
import net.aegistudio.sketchuml.abstraction.SketchRenderHint;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;

public class EntityStateEnd extends EntityAdapter implements 
	RegularRenderer.Painter, StateEntity {
	private final RegularRenderer renderer = new RegularRenderer(this);
	
	@Override
	public void render(SketchRenderHint hint, Graphics g, 
			int size, boolean preview) {
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(hint.outlineWidth));
		
		// Outer circle.
		int outlineWidth = (int)hint.outlineWidth;
		int outlineOffset = outlineWidth / 2;
		int clampedWidth = size - outlineWidth;
		RenderUtils.beginFill(g2d, hint, preview);
		g2d.fillOval(outlineOffset, outlineOffset, 
				clampedWidth, clampedWidth);
		RenderUtils.beginOutline(g2d, hint, preview);
		g2d.drawOval(outlineOffset, outlineOffset, 
				clampedWidth, clampedWidth);
		
		// Inner circle.
		RenderUtils.beginOutline(g2d, hint, preview);
		int innerOffset = (int)(0.2 * size);
		int innerSize = size - 2 * innerOffset - 1;
		g2d.fillOval(innerOffset, innerOffset,
				innerSize, innerSize);
	}

	@Override
	public void renderEntity(SketchRenderHint hint, Graphics g, 
			Entity entity, boolean preview, int w, int h) {
		renderer.renderEntity(hint, g, entity, preview, w, h);
	}

	@Override
	public AstahStateObject toAstahState(AstahUuidGenerator uuid) {
		UFinalStateImp finalModel = new UFinalStateImp();
		FinalStatePresentation finalView = new FinalStatePresentation();
		finalModel.name.body = "FinalState #" + hashCode();
		
		// Collect the created instance.
		AstahStateObject stateObject = new AstahStateObject();
		stateObject.stateModel = finalModel;
		stateObject.stateView = finalView;
		return stateObject;
	}
}