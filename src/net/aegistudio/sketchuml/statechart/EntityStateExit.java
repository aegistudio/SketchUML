package net.aegistudio.sketchuml.statechart;

import java.awt.Graphics;
import java.awt.Graphics2D;

import JP.co.esm.caddies.jomt.jmodel.FinalStatePresentation;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UFinalStateImp;
import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.EntityAdapter;
import net.aegistudio.sketchuml.SketchRenderHint;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;
import net.aegistudio.sketchuml.framework.RegularRenderer;
import net.aegistudio.sketchuml.framework.RenderUtils;

public class EntityStateExit extends EntityAdapter implements 
	RegularRenderer.Painter, StateEntity {
	private final RegularRenderer renderer = new RegularRenderer(this);
	
	@Override
	public void render(SketchRenderHint hint, Graphics g, 
			int size, boolean preview) {
		
		Graphics2D g2d = (Graphics2D)g;

		// Fill the outer circle.
		int outlineWidth = (int)hint.outlineWidth;
		int outlineOffset = outlineWidth / 2;
		int clampedWidth = size - outlineWidth;
		RenderUtils.beginFill(g2d, hint, preview);
		g2d.fillOval(outlineOffset, outlineOffset, 
				clampedWidth, clampedWidth);
		RenderUtils.beginOutline(g2d, hint, preview);
		g2d.drawOval(outlineOffset, outlineOffset, 
				clampedWidth, clampedWidth);
		
		// Fill the cross lines.
		int sqr2Size = (int)(Math.sqrt(2) * clampedWidth / 4) - 1;
		int div2Size = size / 2;
		
		g2d.drawLine(div2Size - sqr2Size, div2Size - sqr2Size, 
				div2Size + sqr2Size, div2Size + sqr2Size);
		g2d.drawLine(div2Size + sqr2Size, div2Size - sqr2Size, 
				div2Size - sqr2Size, div2Size + sqr2Size);
	}
	
	@Override
	public void renderEntity(SketchRenderHint hint, Graphics g, 
			Entity entity, boolean preview, int w, int h) {
		renderer.renderEntity(hint, g, entity, preview, w, h);
	}

	@Override
	public AstahStateObject toAstahState(AstahUuidGenerator uuid) {
		UFinalStateImp exitModel = new UFinalStateImp();
		FinalStatePresentation exitView = new FinalStatePresentation();
		exitModel.name.body = "ExitState #" + hashCode();
		
		// Collect the created instance.
		AstahStateObject stateObject = new AstahStateObject();
		stateObject.stateModel = exitModel;
		stateObject.stateView = exitView;
		return stateObject;
	}
}