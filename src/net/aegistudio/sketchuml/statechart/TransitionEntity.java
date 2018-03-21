package net.aegistudio.sketchuml.statechart;

import JP.co.esm.caddies.jomt.jmodel.TransitionPresentation;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UTransitionImp;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;

public interface TransitionEntity {
	/**
	 * Commonly an Astah link object consists of a transition
	 * model and a transition view.
	 */
	public static class AstahTransitionObject {
		public UTransitionImp transitionModel;
		public TransitionPresentation transitionView;
	}
	
	/**
	 * Set the transition model and transition view. 
	 * 
	 * @param uuid provider of next UUID to generate.
	 * @return the corresponding Astah link.
	 */
	public AstahTransitionObject toAstahTransition(AstahUuidGenerator uuid);
}
