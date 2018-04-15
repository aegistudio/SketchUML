package net.aegistudio.sketchuml.statechart;

import java.awt.Component;
import java.util.function.Consumer;

import net.aegistudio.sketchuml.abstraction.Entity;
import net.aegistudio.sketchuml.abstraction.LinkView;
import net.aegistudio.sketchuml.abstraction.PropertyPanel;
import net.aegistudio.sketchuml.abstraction.PropertyView;

public class LinkMetaStateTransition implements PropertyView.Factory, LinkView {
	
	public PropertyView newPropertyView(Consumer<Entity> notifier) {
		return new PropertyView() {
			private PropertyPanel<LinkStateTransition> viewObject; {
				viewObject = new PropertyPanel<>(notifier);
				
				// Add the trigger.
				viewObject.registerTextField("Trigger: ", 
						(entity) -> entity.trigger, 
						(entity, trigger) -> entity.trigger = trigger);
				
				// Add the guard.
				viewObject.registerTextField("Guard: ", 
						(entity) -> entity.guard, 
						(entity, guard) -> entity.guard = guard);
				
				// Add the action.
				viewObject.registerTextField("Action: ",
						(entity) -> entity.action, 
						(entity, action) -> entity.action = action);
			}
			
			@Override
			public Component getViewObject() {
				return viewObject;
			}

			@Override
			public void update(Entity entity) {
				viewObject.updateEntity((LinkStateTransition)entity);
			}
			
			@Override
			public void select(Entity entity) {
				viewObject.selectEntity((LinkStateTransition)entity);
			}
		};
	}
	

	@Override
	public LinkRender render(Entity source, Entity destination, Entity link) {
		LinkStateTransition transition = (LinkStateTransition)link;
		return new LinkView.LinkRender() { {
			beginStyle = LinkView.ArrowStyle.NONE;
			endStyle = LinkView.ArrowStyle.FISHBONE;
			lineStyle = LinkView.LineStyle.COHERENT;
			
			// Retrieve the presentation string of transition.
			centerText = transition.toPresentationString();
			if(centerText.length() == 0) centerText = null;
		} };
	}

}
