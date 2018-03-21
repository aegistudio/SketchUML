package net.aegistudio.sketchuml.general;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import JP.co.esm.caddies.jomt.jmodel.ChoicePresentation;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UPseudostateImp;
import JP.co.esm.caddies.uml.Foundation.DataTypes.UPseudostateKind;
import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;
import net.aegistudio.sketchuml.statechart.StateEntity;

public class EntityDecision implements Entity, StateEntity {
	public String guard = "";
	
	@Override
	public void load(DataInputStream inputStream) throws IOException {
		guard = inputStream.readUTF();
	}

	@Override
	public void save(DataOutputStream outputStream) throws IOException {
		outputStream.writeUTF(guard);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public AstahStateObject toAstahState(AstahUuidGenerator uuid) {
		UPseudostateImp choiceModel = new UPseudostateImp();
		ChoicePresentation choiceView = new ChoicePresentation();
		choiceModel.name.body = guard;
		choiceModel.kind = new UPseudostateKind();
		choiceModel.kind.label = "choice";
		
		// Caution: the choice's color is set to wheat yellow by default.
		choiceView.styleMap.put("fill.color.alpha", "FF");
		choiceView.styleMap.put("fill.color", "#FFFFCC");
		
		// Collect the created instance.
		AstahStateObject stateObject = new AstahStateObject();
		stateObject.stateModel = choiceModel;
		stateObject.stateView = choiceView;
		return stateObject;
	}
}
