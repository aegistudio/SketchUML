package net.aegistudio.sketchuml.framework;

import java.util.Vector;

import net.aegistudio.sketchuml.Command;

public class CommandDeleteEntity<Path> implements Command {
	private final SketchModel<Path> model;
	private final SketchEntityComponent originalEntity;
	private final Vector<SketchLinkComponent<Path>> links = new Vector<>();
	public CommandDeleteEntity(SketchModel<Path> model, 
			SketchEntityComponent originalComponent) {
		this.model = model;
		this.originalEntity = originalComponent;
		if(this.originalEntity == null) throw new AssertionError(
				"The original entity should never be null.");
		for(int i = 0; i < model.numLinks(); ++ i) {
			SketchLinkComponent<Path> link = model.getLink(i);
			if(link.relatedTo(originalComponent)) links.add(link);
		}
	}
	
	public CommandDeleteEntity(SketchModel<Path> model) {
		this(model, model.getOriginalEntity());
	}
	
	@Override
	public void execute() {
		if(model.getOriginalEntity() == originalEntity)
			model.selectEntity(null, null);
		model.destroy(null, originalEntity);
	}

	@Override
	public void undo() {
		model.create(null, originalEntity);
		links.forEach(l -> model.link(null, l));
		model.selectEntity(null, originalEntity);
	}

	@Override
	public String name() {
		return "Delete Object " + originalEntity.entry.name;
	}

}
