package net.aegistudio.sketchuml.abstraction;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class LinkEntry {
	public final String name;
	
	public final String description;
	
	public final Supplier<Entity> factory;
	
	public final BiPredicate<Entity, Entity> filter;
	
	public final PropertyView.Factory propertyFactory;
	
	public final LinkView linkView;
	
	public LinkEntry(String name, String description, 
			Supplier<Entity> factory,
			BiPredicate<Entity, Entity> filter,
			PropertyView.Factory propertyFactory, 
			LinkView linkView) {
		this.name = name;
		this.description = description;
		this.factory = factory;
		this.filter = filter;
		this.propertyFactory = propertyFactory;
		this.linkView = linkView;
	}
}
