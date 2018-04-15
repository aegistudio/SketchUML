package net.aegistudio.sketchuml.statechart;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;

import JP.co.esm.caddies.jomt.jmodel.InitialStatePresentation;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UPseudostateImp;
import JP.co.esm.caddies.uml.Foundation.DataTypes.UPseudostateKind;
import net.aegistudio.sketchuml.abstraction.Entity;
import net.aegistudio.sketchuml.abstraction.EntityAdapter;
import net.aegistudio.sketchuml.abstraction.RegularRenderer;
import net.aegistudio.sketchuml.abstraction.RenderUtils;
import net.aegistudio.sketchuml.abstraction.SketchRenderHint;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;

public class EntityStateStart extends EntityAdapter implements 
	RegularRenderer.Painter, StateEntity {
	private final RegularRenderer renderer = new RegularRenderer(this);
	
	@Override
	public void render(SketchRenderHint hint, Graphics g, 
			int size, boolean preview) {
		Graphics2D g2d = (Graphics2D)g;
		RenderUtils.beginOutline(g2d, hint, preview);
		g2d.setStroke(new BasicStroke(1));
		g.fillOval(0, 0, size - 1, size - 1);
	}
	
	@Override
	public void renderEntity(SketchRenderHint hint, Graphics g, 
			Entity entity, boolean preview, int w, int h) {
		renderer.renderEntity(hint, g, entity, preview, w, h);
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