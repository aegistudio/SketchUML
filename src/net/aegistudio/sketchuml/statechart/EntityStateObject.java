package net.aegistudio.sketchuml.statechart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import JP.co.esm.caddies.jomt.jmodel.CompositeStatePresentation;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UCompositeStateImp;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;

public class EntityStateObject implements StateEntity {
	public String name = "";
	
	public String actions = "";
	
	public boolean isBrief = false;

	@Override
	public void load(DataInputStream inputStream) throws IOException {
		name = inputStream.readUTF();
		actions = inputStream.readUTF();
		isBrief = inputStream.readBoolean();
	}

	@Override
	public void save(DataOutputStream outputStream) throws IOException {
		outputStream.writeUTF(name);
		outputStream.writeUTF(actions);
		outputStream.writeBoolean(isBrief);
	}

	@SuppressWarnings("unchecked")
	@Override
	public AstahStateObject toAstahState(AstahUuidGenerator uuid) {
		UCompositeStateImp stateModel = new UCompositeStateImp();
		CompositeStatePresentation stateView = new CompositeStatePresentation();
		stateModel.name.body = name;
		stateModel.definition.body = actions;
		stateView.allActionVisibility = !isBrief;
		
		// Caution: the state's color is set to wheat yellow by default.
		stateView.styleMap.put("fill.color.alpha", "FF");
		stateView.styleMap.put("fill.color", "#FFFFCC");
		
		// Collect the created instance.
		AstahStateObject stateObject = new AstahStateObject();
		stateObject.stateModel = stateModel;
		stateObject.stateView = stateView;
		return stateObject;
	}
}
