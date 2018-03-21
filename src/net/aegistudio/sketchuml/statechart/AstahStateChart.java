package net.aegistudio.sketchuml.statechart;

import java.io.IOException;

import JP.co.esm.caddies.jomt.jmodel.FramePresentation;
import JP.co.esm.caddies.jomt.jmodel.StateVertexPresentation;
import JP.co.esm.caddies.jomt.jmodel.TransitionPresentation;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UCompositeStateImp;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UStateChartDiagramImp;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UStateMachineImp;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UStateVertexImp;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UTransitionImp;
import net.aegistudio.sketchuml.astaxpt.AstahProject;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;

public class AstahStateChart extends AstahProject {
	public final UStateMachineImp stateMachineModel;
	public final UCompositeStateImp stateRoot;
	public final UStateChartDiagramImp stateChartDiagram;
	public final FramePresentation stateChartFrame;
	
	@SuppressWarnings("unchecked")
	public AstahStateChart(AstahUuidGenerator uuid, 
			String name) throws IOException {
		super(uuid);
		this.stateMachineModel = new UStateMachineImp();
		this.stateRoot = new UCompositeStateImp();
		this.stateChartDiagram = new UStateChartDiagramImp();
		this.stateChartFrame = new FramePresentation();
		
		// Construct the state machine model.
		stateMachineModel.name.body = name;
		stateMachineModel.id = uuid.nextUuid();
		stateMachineModel.context = model;
		model.behavior.add(stateMachineModel);
		root.store.add(stateMachineModel);
		
		// Construct the dummy super node.
		stateMachineModel.top = stateRoot;
		stateRoot.id = uuid.nextUuid();
		root.store.add(stateMachineModel.top);
		
		// Construct the state chart diagram.
		stateMachineModel.diagram = stateChartDiagram;
		stateChartDiagram.stateMachine = stateMachineModel;
		stateChartDiagram.id = uuid.nextUuid();
		stateChartDiagram.name.body = name;
		stateChartDiagram.type = "StateChart Diagram";
		root.store.add(stateChartDiagram);
		
		// Construct the diagram's frame.
		stateChartFrame.diagram = stateChartDiagram;
		stateChartFrame.visibility = true;
		stateChartFrame.doAutoResize = true;
		stateChartFrame.label = name;
		stateChartFrame.id = uuid.nextUuid();
		stateChartDiagram.presentation.add(stateChartFrame);
		stateChartDiagram.register(stateChartFrame);
		root.store.add(stateChartFrame);
	}
	
	@SuppressWarnings("unchecked")
	public void addState(UStateVertexImp stateModel, 
			StateVertexPresentation stateView, UCompositeStateImp stateParent) {
		// Associate the state with its parent.
		if(stateParent == null) stateParent = stateRoot;
		stateModel.container = stateParent;
		stateParent.subvertex.add(stateModel);
		
		// Allocate UUIDs for the model and the view.
		if(stateModel.id == null) stateModel.id = uuid.nextUuid();
		if(stateView.id == null) stateView.id = uuid.nextUuid();
		
		// Associate different objects.
		stateModel.presentation.add(stateView);
		stateModel.register(stateView);
		stateView.model = stateModel;
		stateView.diagram = stateChartDiagram;
		stateView.visibility = true;
		stateView.register(stateChartFrame);
		
		// Add the model and view to the entity store and presentation.
		root.store.add(stateModel);
		root.store.add(stateView);
		stateChartDiagram.presentation.add(stateView);
	}
	
	@SuppressWarnings("unchecked")
	public void addTransition(
		UTransitionImp transitionModel, TransitionPresentation transitionView,
		UStateVertexImp sourceModel, StateVertexPresentation sourceView,
		UStateVertexImp targetModel, StateVertexPresentation targetView) {

		if(	sourceModel == null || sourceView == null || 
			targetModel == null || targetView == null)
			throw new AssertionError("The ends of transition should never be null!");
		
		// Associate transition with source models and views.
		transitionModel.source = sourceModel;
		sourceModel.outgoing.add(transitionModel);
		transitionView.servers.add(sourceView);
		sourceView.clients.add(transitionView);
		sourceView.register(transitionView);
		
		// Associate transition with target models and views.
		transitionModel.target = targetModel;
		targetModel.incoming.add(transitionModel);
		transitionView.servers.add(targetView);
		targetView.clients.add(transitionView);
		if(sourceView != targetView)
			targetView.register(transitionView);
		
		// Allocation of UUID if not allocated.
		if(transitionModel.id == null) transitionModel.id = uuid.nextUuid();
		if(transitionView.id == null) transitionView.id = uuid.nextUuid();
		
		// Associate transition model with view.
		transitionView.model = transitionModel;
		transitionModel.presentation.add(transitionView);
		transitionModel.register(transitionView);
		
		// Associate transition model and view with other objects.
		stateMachineModel.transition.add(transitionModel);
		transitionModel.transitionsInv = stateMachineModel;
		stateChartDiagram.presentation.add(transitionView);
		transitionView.diagram = stateChartDiagram;
		transitionView.register(stateChartFrame);
		root.store.add(transitionModel);
		root.store.add(transitionView);
		if(transitionModel.trigger != null)
			root.store.add(transitionModel.trigger);
		if(transitionModel.guard != null)
			root.store.add(transitionModel.guard);
		if(transitionModel.effect != null)
			root.store.add(transitionModel.effect);
	}
}
