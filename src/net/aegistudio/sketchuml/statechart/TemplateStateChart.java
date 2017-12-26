package net.aegistudio.sketchuml.statechart;

import net.aegistudio.sketchuml.EntityEntry;
import net.aegistudio.sketchuml.Template;

public class TemplateStateChart implements Template {
	private final EntityStateStart entityStart = new EntityStateStart();
	private final EntityStateEnd entityEnd = new EntityStateEnd();
	
	@Override
	public EntityEntry[] entities() {
		EntityEntry stateStart = new EntityEntry(
				"statechart/start", "StateChart Start",
				"A start point of a state machine",
				entityStart, entityStart, entityStart);
		
		EntityEntry stateEnd = new EntityEntry(
				"statechart/end", "StateChart End",
				"A normal exit point of a state machine",
				entityEnd, entityEnd, entityEnd);
		
		return new EntityEntry[]{ stateStart, stateEnd };
	}

}
