package net.aegistudio.sketchuml.statechart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.EntityEntry;
import net.aegistudio.sketchuml.LinkEntry;
import net.aegistudio.sketchuml.LinkView;
import net.aegistudio.sketchuml.Template;
import net.aegistudio.sketchuml.general.EntityDecision;
import net.aegistudio.sketchuml.general.EntityMetaDecision;
import net.aegistudio.sketchuml.path.PathView;

public class TemplateStateChart implements Template {
	private final EntityStateStart entityStart = new EntityStateStart();
	private final EntityStateEnd entityEnd = new EntityStateEnd();
	private final EntityStateExit entityExit = new EntityStateExit();
	private final EntityMetaStateObject entityMetaState = new EntityMetaStateObject();
	private final EntityMetaDecision entityMetaDecision = new EntityMetaDecision();
	
	@Override
	public EntityEntry[] entities() {
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
		
		return new EntityEntry[]{ stateStart, stateEnd, stateExit, 
				stateBrief, stateComplete, decision };
	}

	@Override
	public LinkEntry[] links() {
		LinkEntry transition = new LinkEntry("Transition", 
				"Transformation from a state to another.", 
				() -> new Entity() {
					@Override
					public void load(DataInputStream inputStream) throws IOException {

					}

					@Override
					public void save(DataOutputStream outputStream) throws IOException {
						
					}
					
				}, (e1, e2) -> true, null, (s, d, l) -> new LinkView.LinkRender() { {
					beginStyle = PathView.ArrowStyle.NONE;
					endStyle = PathView.ArrowStyle.FISHBONE;
					lineStyle = PathView.LineStyle.COHERENT;
				} });
		return new LinkEntry[] { transition };
	}

}
