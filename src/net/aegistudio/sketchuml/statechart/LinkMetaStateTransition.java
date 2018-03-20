package net.aegistudio.sketchuml.statechart;

import java.awt.Component;
import java.util.function.Consumer;

import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.LinkView;
import net.aegistudio.sketchuml.PropertyView;
import net.aegistudio.sketchuml.framework.PropertyPanel;
import net.aegistudio.sketchuml.path.PathView;

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
			beginStyle = PathView.ArrowStyle.NONE;
			endStyle = PathView.ArrowStyle.FISHBONE;
			lineStyle = PathView.LineStyle.COHERENT;
			
			// Add the trigger if any.
			if(transition.trigger.length() > 0)
				centerText = transition.trigger;
			
			// Replace the guard condition if any.
			if(transition.guard.length() > 0)
				centerText = (centerText == null? "" : 
					centerText + " ") + "[" + transition.guard + "]";
			
			// Append the action to the end.
			if(transition.action.length() > 0)
				centerText = (centerText == null? "" : 
					centerText + " ") + "/ " + transition.action;
		} };
	}

}
