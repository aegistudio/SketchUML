package net.aegistudio.sketchuml;

import java.awt.Font;

public class Configuration {
	/**	The number of items to be shown or browse. */
	public int MAX_CANDIDATE = 5;
	
	/** The hand writing font's name */
	public String HANDWRITING_FONTNAME = "Comic Sans MS";
	
	/** The hand writing font's decoration */
	public int HANDWRITING_FONTSTYLE = 0;
	
	/** The hand writing font's size */
	public float HANDWRITING_FONTSIZE = 16.f;
	
	/** The font instance of the handwriting font. */
	public Font HANDWRITING_FONT;
	
	private static final Configuration instance = new Configuration();
	public static Configuration getInstance() {
		return instance;	
	}
}
