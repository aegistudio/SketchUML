package net.aegistudio.sketchuml.abstraction;

public interface LinkView {
	public enum ArrowStyle {
		NONE,				// --
		FISHBONE,			// ->
		TRIANGLE_EMPTY,		// -+> (EMPTY)
		TRIANGLE_FILLED,	// -+> (FILLED)
		DIAMOND_EMPTY,		// -<> (EMPTY)
		DIAMOND_FILLED,		// -<> (FILLED)
		CIRCLE_EMPTY,		// -o  (EMPTY)
		CIRCLE_FILLED		// -o  (FILLED)
	}
	
	public enum LineStyle {
		COHERENT,			// -
		DASH,				// --
		DOT,				// ..
		DASHDOT				// -.-.
	}
	
	public static class LinkRender {
		public ArrowStyle beginStyle;
		
		public ArrowStyle endStyle;
		
		public LineStyle lineStyle;
		
		public String startText, centerText, endText;
	}
	
	public LinkRender render(Entity source, 
			Entity destination, Entity link);
}
