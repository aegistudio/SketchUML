package net.aegistudio.sketchuml;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class LinkEntry {
	public final String entry;
	
	public final String name;
	
	public final String description;
	
	public final Supplier<Entity> factory;
	
	public final BiPredicate<Entity, Entity> filter;
	
	public final PropertyView propertyView;
	
	public final LinkView linkView;
	
	public LinkEntry(String entry, String name,
			String description, Supplier<Entity> factory,
			BiPredicate<Entity, Entity> filter,
			PropertyView propertyView, LinkView linkView) {
		this.entry = entry;
		this.name = name;
		this.description = description;
		this.factory = factory;
		this.filter = filter;
		this.propertyView = propertyView;
		this.linkView = linkView;
	}
}
