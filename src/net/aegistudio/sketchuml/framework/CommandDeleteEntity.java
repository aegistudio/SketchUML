package net.aegistudio.sketchuml.framework;

import java.util.Vector;

import net.aegistudio.sketchuml.Command;

public class CommandDeleteEntity<Path> implements Command {
	private final SketchModel<Path> model;
	private final SketchSelectionModel<Path> selectionModel;
	private final SketchEntityComponent originalEntity;
	private final Vector<SketchLinkComponent<Path>> links = new Vector<>();
	public CommandDeleteEntity(SketchModel<Path> model, 
			SketchSelectionModel<Path> selectionModel,
			SketchEntityComponent originalComponent) {
		this.model = model; this.selectionModel = selectionModel;
		this.originalEntity = originalComponent;
		if(this.originalEntity == null) throw new AssertionError(
				"The original entity should never be null.");
		for(int i = 0; i < model.numLinks(); ++ i) {
			SketchLinkComponent<Path> link = model.getLink(i);
			if(link.relatedTo(originalComponent)) links.add(link);
		}
	}
	
	public CommandDeleteEntity(SketchModel<Path> model, 
			SketchSelectionModel<Path> selectionModel) {
		this(model, selectionModel, selectionModel.selectedEntity());
	}
	
	@Override
	public void execute() {
		if(selectionModel.selectedEntity() == originalEntity)
			selectionModel.requestUnselect();
		model.destroy(originalEntity);
	}

	@Override
	public void undo() {
		model.create(originalEntity);
		links.forEach(l -> model.link(l));
		selectionModel.requestSelectEntity(originalEntity);
	}

	@Override
	public String name() {
		return "Delete Object " + originalEntity.entry.name;
	}

}
