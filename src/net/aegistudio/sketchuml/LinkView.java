package net.aegistudio.sketchuml;

import net.aegistudio.sketchuml.path.PathView;

public interface LinkView {
	public class LinkRender {
		public PathView.ArrowStyle beginStyle;
		
		public PathView.ArrowStyle endStyle;
		
		public PathView.LineStyle lineStyle;
	}
	
	public LinkRender render(Entity source, 
			Entity destination, Entity link);
}
