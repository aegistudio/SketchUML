package net.aegistudio.sketchuml;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import net.aegistudio.sketchuml.path.PathView;

public interface LinkView {
	public static class LinkRender {
		public PathView.ArrowStyle beginStyle;
		
		public PathView.ArrowStyle endStyle;
		
		public PathView.LineStyle lineStyle;
		
		public <Path> void paint(Graphics2D g2d, boolean preview,
				Path path, PathView<Path> pathView,
				Rectangle2D boundBegin, Rectangle2D boundEnd) {
			pathView.render(g2d, preview, path, lineStyle, 
					boundBegin, beginStyle, boundEnd, endStyle);
		}
	}
	
	public LinkRender render(Entity source, 
			Entity destination, Entity link);
}
