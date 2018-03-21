package net.aegistudio.sketchuml.statechart;

import java.awt.Color;
import java.awt.Graphics;

import JP.co.esm.caddies.jomt.jmodel.InitialStatePresentation;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UPseudostateImp;
import JP.co.esm.caddies.uml.Foundation.DataTypes.UPseudostateKind;
import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.EntityAdapter;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;
import net.aegistudio.sketchuml.framework.RegularRenderer;

public class EntityStateStart extends EntityAdapter implements 
	RegularRenderer.Painter, StateEntity {
	private final RegularRenderer renderer = new RegularRenderer(this);
	
	@Override
	public void render(Graphics g, int size, boolean preview) {
		g.setColor(preview? Color.GRAY : Color.BLACK);
		g.fillOval(0, 0, size, size);
	}
	
	@Override
	public void renderEntity(Graphics g, Entity entity, boolean preview) {
		renderer.renderEntity(g, entity, preview);
	}

	@Override
	public AstahStateObject toAstahState(AstahUuidGenerator uuid) {
		UPseudostateImp initialModel = new UPseudostateImp();
		InitialStatePresentation initialView = new InitialStatePresentation();
		initialModel.name.body = "PseudoInitial #" + hashCode();
		initialModel.kind = new UPseudostateKind();
		initialModel.kind.label = "initial";
		
		// Collect the created instance.
		AstahStateObject stateObject = new AstahStateObject();
		stateObject.stateModel = initialModel;
		stateObject.stateView = initialView;
		return stateObject;
	}
}