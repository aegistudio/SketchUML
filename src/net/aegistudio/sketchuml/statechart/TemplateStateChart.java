package net.aegistudio.sketchuml.statechart;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import JP.co.esm.caddies.jomt.jmodel.PathPresentation;
import JP.co.esm.caddies.jomt.jmodel.RectPresentation;
import JP.co.esm.caddies.jomt.jmodel.StateVertexPresentation;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UStateVertexImp;
import net.aegistudio.sketchuml.EntityEntry;
import net.aegistudio.sketchuml.LinkEntry;
import net.aegistudio.sketchuml.Template;
import net.aegistudio.sketchuml.astaxpt.AstahProject;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;
import net.aegistudio.sketchuml.framework.SketchEntityComponent;
import net.aegistudio.sketchuml.framework.SketchLinkComponent;
import net.aegistudio.sketchuml.general.EntityDecision;
import net.aegistudio.sketchuml.general.EntityMetaDecision;
import net.aegistudio.sketchuml.statechart.StateEntity.AstahStateObject;
import net.aegistudio.sketchuml.statechart.TransitionEntity.AstahTransitionObject;

public class TemplateStateChart implements Template {
	private final EntityStateStart entityStart = new EntityStateStart();
	private final EntityStateEnd entityEnd = new EntityStateEnd();
	private final EntityStateExit entityExit = new EntityStateExit();
	private final EntityMetaStateObject entityMetaState = new EntityMetaStateObject();
	private final EntityMetaDecision entityMetaDecision = new EntityMetaDecision();
	private final LinkMetaStateTransition linkMetaTransition = new LinkMetaStateTransition();
	
	private final EntityEntry[] entities;
	private final LinkEntry[] links;
	public TemplateStateChart() {
		// The entities initialization.
		EntityEntry stateStart = new EntityEntry(
				"statechart/start", "Start",
				"A start point of a state machine",
				entityStart, entityStart, entityStart);
		
		EntityEntry stateEnd = new EntityEntry(
				"statechart/end", "End",
				"A normal exit point of a state machine",
				entityEnd, entityEnd, entityEnd);
		
		EntityEntry stateExit = new EntityEntry(
				"statechart/exit", "Exit",
				"A exceptional exit point of a state machine",
				entityExit, entityExit, entityExit);
		
		EntityEntry stateBrief = new EntityEntry(
				"statechart/state_brief", "State (Brief)",
				"A brief notation of state, with actions hidden.", () -> { 
					EntityStateObject obj = new EntityStateObject();
					obj.isBrief = true; return obj;
				}, entityMetaState, entityMetaState);
		
		EntityEntry stateComplete = new EntityEntry(
				"statechart/state", "State",
				"A complete notation of state.", () -> { 
					EntityStateObject obj = new EntityStateObject();
					obj.isBrief = false; return obj;
				}, entityMetaState, entityMetaState);
		
		EntityEntry decision = new EntityEntry(
				"general/decision", "Decision",
				"A conditional branch of decision", 
				EntityDecision::new, entityMetaDecision, 
				entityMetaDecision);
		
		this.entities = new EntityEntry[] { 
				stateStart, stateEnd, stateExit, 
				stateBrief, stateComplete, decision };
		
		// The links initialization.
		LinkEntry transition = new LinkEntry("Transition", 
				"Transformation from a state to another.", 
				() -> new LinkStateTransition(), 
				(e1, e2) -> true, linkMetaTransition, linkMetaTransition);
		this.links = new LinkEntry[] { transition };
	}
	
	@Override
	public EntityEntry[] entities() {
		return this.entities;
	}

	@Override
	public LinkEntry[] links() {
		return this.links;
	}

	@Override
	public boolean canExportAstah() {
		return false;
	}


	@Override
	public <Path> AstahProject prepareAstahProject(
			AstahUuidGenerator uuid, String projectName, 
			SketchEntityComponent[] entities,
			Map<SketchEntityComponent, RectPresentation> entitiesView, 
			SketchLinkComponent<Path>[] links,
			Map<SketchLinkComponent<Path>, PathPresentation> linksView) throws IOException {
		AstahStateChart astah = new AstahStateChart(uuid, projectName);
		
		// Conversion of the entities into state object or others.
		Map<SketchEntityComponent, UStateVertexImp> stateModels = new HashMap<>();
		for(SketchEntityComponent entity : entities) {
			if(entity.entity instanceof StateEntity) {
				StateEntity stateEntity = (StateEntity)entity;
				AstahStateObject stateObject = stateEntity.toAstahState(uuid);
				astah.addState(stateObject.stateModel, stateObject.stateView, null);
				entitiesView.put(entity, stateObject.stateView);
			}
			else ; // Other conversion.
		}
		
		// Conversion of the links into transition or others.
		for(SketchLinkComponent<Path> link : links) {
			if(link.link instanceof TransitionEntity) {
				// Retrieve the source state model and view.
				UStateVertexImp sourceModel = stateModels.get(link.source);
				StateVertexPresentation sourceView = (StateVertexPresentation) 
						entitiesView.get(link.source);
				if(sourceModel == null || sourceView == null) 
					throw new IOException("The source object is not in the model.");
				
				// Retrieve the target state model and view.
				UStateVertexImp targetModel = stateModels.get(link.destination);
				StateVertexPresentation targetView = (StateVertexPresentation)
						entitiesView.get(link.destination);
				if(targetModel == null || targetView == null)
					throw new IOException("The target object is not in the model.");
				
				// Initialize the transition model.
				TransitionEntity transition = (TransitionEntity)link.link;
				AstahTransitionObject transitionObject = transition.toAstahTransition(uuid);
				astah.addTransition(transitionObject.transitionModel, 
						transitionObject.transitionView, sourceModel,
						sourceView, targetModel, targetView);
			}
		}
		
		return null;
	}
	
	
}
