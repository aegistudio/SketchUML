package net.aegistudio.sketchuml.statechart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import JP.co.esm.caddies.jomt.jmodel.LabelPresentation;
import JP.co.esm.caddies.jomt.jmodel.TransitionPresentation;
import JP.co.esm.caddies.uml.BehavioralElements.CommonBehavior.UActionImp;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UEventImp;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UGuardImp;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UTransitionImp;
import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;

public class LinkStateTransition implements Entity, TransitionEntity {
	public String trigger = "", guard = "", action = "";

	@Override
	public void load(DataInputStream inputStream) throws IOException {
		trigger = inputStream.readUTF();
		guard = inputStream.readUTF();
		action = inputStream.readUTF();
	}

	@Override
	public void save(DataOutputStream outputStream) throws IOException {
		outputStream.writeUTF(trigger);
		outputStream.writeUTF(guard);
		outputStream.writeUTF(action);
	}

	public String toPresentationString() {
		String centerText = "";
		
		// Add the trigger if any.
		if(trigger.length() > 0) centerText = trigger;
		
		// Replace the guard condition if any.
		if(guard.length() > 0) centerText = (centerText == null? "" : 
				centerText + " ") + "[" + guard + "]";
		
		// Append the action to the end.
		if(action.length() > 0) centerText = (centerText == null? "" : 
				centerText + " ") + "/ " + action;
		
		return centerText;
	}
	
	@Override
	public AstahTransitionObject toAstahTransition(AstahUuidGenerator uuid) {
		// Initialize the transition model.
		TransitionPresentation transitionView = new TransitionPresentation();
		UTransitionImp transitionModel = new UTransitionImp();
		
		// Initialize the namePresentation and weightPresentation.
		LabelPresentation namePresentation = new LabelPresentation(); 
		transitionView.namePresentation = namePresentation;
		prepareTransitionLabel(uuid, transitionView, namePresentation);
		LabelPresentation weightPresentation = new LabelPresentation();
		transitionView.weightPresentation = weightPresentation;
		prepareTransitionLabel(uuid, transitionView, weightPresentation);
		
		// Initialize the trigger.
		if(this.trigger.length() > 0) {
			UEventImp trigger = new UEventImp();
			trigger.name.body = this.trigger;
			trigger.id = uuid.nextUuid();
			transitionModel.trigger = trigger;
		}
		
		// Initialize the guard.
		if(this.guard.length() > 0) {
			UGuardImp guard = new UGuardImp();
			guard.name.body = this.guard;
			guard.id = uuid.nextUuid();
			transitionModel.guard = guard;
		}
		
		// Initialize the action.
		if(this.action.length() > 0) {
			UActionImp action = new UActionImp();
			action.name.body = this.action;
			action.id = uuid.nextUuid();
			transitionModel.effect = action;
		}
		
		// Construct the link's presentation string.
		String presentationString = toPresentationString();
		transitionModel.name.body = presentationString;
		namePresentation.label = presentationString;
		
		// Construct the transition entity.
		AstahTransitionObject transitionObject = new AstahTransitionObject();
		transitionObject.transitionModel = transitionModel;
		transitionObject.transitionView = transitionView;
		return transitionObject;
	}
	
	/**
	 * Initialize the label (namePresentation and weightPresentation) in the phase
	 * of Astah model preparation.
	 *
	 * @param uuid used to generate a new UUID for the label.
	 * @param transition the transition presentation holding it.
	 * @param label the presentation of label.
	 */
	private void prepareTransitionLabel(AstahUuidGenerator uuid, 
			TransitionPresentation transition, LabelPresentation label) {
		label.depth = 0;
		label.doAutoResize = true;
		label.label = "";
		label.compositeParent = transition;
		label.id = uuid.nextUuid();
		label.visibility = true;
		label.constraintVisibility = true;
		label.stereotypeVisibility = true;
		label.styleMap = null;
	}
}
