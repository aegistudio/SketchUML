package net.aegistudio.sketchuml.statechart;

import JP.co.esm.caddies.jomt.jmodel.StateVertexPresentation;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UStateVertexImp;
import net.aegistudio.sketchuml.abstraction.Entity;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;

/**
 * An entity that guarantees to represent a state in
 * state chart. The object should be capable of creating
 * its model and view.
 * 
 * @author Haoran Luo
 */
public interface StateEntity extends Entity {
	/**
	 * Commonly an Astah state object consists of a state
	 * model and a state view.
	 */
	public static class AstahStateObject {
		public UStateVertexImp stateModel;
		public StateVertexPresentation stateView;
	}
	
	/**
	 * Set the state model and state view. 
	 * 
	 * @param uuid the provider of next UUID for internal objects.
	 * @return the corresponding Astah state.
	 */
	public AstahStateObject toAstahState(AstahUuidGenerator uuid);
}
