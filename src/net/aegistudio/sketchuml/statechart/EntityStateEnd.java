package net.aegistudio.sketchuml.statechart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import JP.co.esm.caddies.jomt.jmodel.FinalStatePresentation;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UFinalStateImp;
import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.EntityAdapter;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;
import net.aegistudio.sketchuml.framework.RegularRenderer;

public class EntityStateEnd extends EntityAdapter implements 
	RegularRenderer.Painter, StateEntity {
	private final RegularRenderer renderer = new RegularRenderer(this);
	
	@Override
	public void render(Graphics g, int size, boolean preview) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(2));
		
		// Outer circle.
		g2d.setColor(Color.WHITE);
		g2d.fillOval(1, 1, size - 2, size - 2);
		g2d.setColor(preview? Color.GRAY : Color.BLACK);
		g2d.drawOval(0, 0, size, size);
		
		// Inner circle.
		g2d.setStroke(new BasicStroke(1));
		int innerOffset = (int)(0.2 * size);
		int innerSize = size - 2 * innerOffset - 1;
		g2d.fillOval(innerOffset, innerOffset,
				innerSize, innerSize);
	}

	@Override
	public void renderEntity(Graphics g, Entity entity, boolean preview) {
		renderer.renderEntity(g, entity, preview);
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