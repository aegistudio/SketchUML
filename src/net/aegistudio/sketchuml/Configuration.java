package net.aegistudio.sketchuml;

import java.awt.Font;

public class Configuration {
	/**	The number of items to be shown or browse. */
	public int MAX_CANDIDATE = 5;
	
	/** Will the program recognize strokes soon after painting */
	public boolean INSTANT_RECOGNIZE = true;

	/** The path to find the gesture notations. */
	public String GESTURE_PATH = "data";
	
	/** The maximum distance to select a link line. */ 
	public double MAX_LINKTHRESHOLD = 10.0;
	
	// Begin handwriting font's configuration.
	/** The hand writing font's name */
	public String HANDWRITING_FONTNAME = "Comic Sans MS";
	
	/** The hand writing font's decoration */
	public int HANDWRITING_FONTSTYLE = 0;
	
	/** The hand writing font's size */
	public float HANDWRITING_FONTSIZE = 16.f;
	
	/** The font instance of the handwriting font. */
	public Font HANDWRITING_FONT;
	
	// Begin editting font's configuration.
	/** The font for annotation button used for editting */
	public String EDITING_FONTNAME = "Consolas";
	
	/** The editing tag font's decoration */
	public int EDITING_FONTSTYLE = 0;
	
	/** The hand writing font's size */
	public float EDITING_FONTSIZE = 12.f;
	
	/** The tag for removing a component */
	public String EDITING_DELETE = "Delete";
	
	/** The tag for move a component to front*/
	public String EDITING_MOVEFRONT = "Move Front";
	
	/** The tag for send a component to back*/
	public String EDITING_SENDBACK = "Send Back";
	
	/** The font instance of the editing font. */
	public Font EDITING_FONT;
	
	// Begin property panel's font's configuration.
	/** The font for property panel */
	public String PROPERTY_FONTNAME = "Consolas";
	
	/** The property panel font's decoration */
	public int PROPERTY_FONTSTYLE = 0;
	
	/** The property panel font's size */
	public float PROPERTY_FONTSIZE = 14.f;
	
	/** The font instance of the property font. */
	public Font PROPERTY_FONT;
	
	private static final Configuration instance = new Configuration();
	public static Configuration getInstance() {
		return instance;	
	}
}
