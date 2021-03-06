package net.aegistudio.sketchuml.framework;

import net.aegistudio.sketchuml.abstraction.Entity;
import net.aegistudio.sketchuml.abstraction.LinkEntry;

public class SketchLinkComponent<Path> {
	public final SketchEntityComponent source, destination;
	
	public final Path pathObject;
	
	public LinkEntry entry;
	
	public Entity link;	
	
	public SketchLinkComponent(
			SketchEntityComponent source, 
			SketchEntityComponent destination,
			Path pathObject) { 
		
		this.source = source;
		this.destination = destination;
		this.pathObject = pathObject;
	}
	
	public boolean relatedTo(SketchEntityComponent c) {
		return source == c || destination == c;
	}
}
